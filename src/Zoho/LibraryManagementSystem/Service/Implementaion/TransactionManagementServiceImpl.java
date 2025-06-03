package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Model.Book;
import Zoho.LibraryManagementSystem.Model.Fine;
import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Transaction;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.TransactionManagementService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class TransactionManagementServiceImpl implements TransactionManagementService {
    private final LibraryDB libraryDB;
    private static final int LOAN_PERIOD_DAYS = 14;

    public TransactionManagementServiceImpl(LibraryDB libraryDB) {
        this.libraryDB = libraryDB;
    }

    /**
     * Handles the entire process of a member borrowing a book within a single database transaction.
     * @param currentMember The member borrowing the book.
     * @param bookId The ID of the book to be borrowed.
     */
    @Override
    public void borrowBook(Member currentMember, int bookId) throws SQLException, IllegalStateException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start transaction

            Book book = libraryDB.findBookById(conn, bookId)
                    .orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " not found."));

            if (book.getCopiesAvailable() < 1) {
                throw new IllegalStateException("No copies of '" + book.getTitle() + "' are available.");
            }

            if (libraryDB.findActiveLoan(conn, currentMember.getMemberId(), bookId).isPresent()) {
                throw new IllegalStateException("You already have an active loan for this book.");
            }

            // 1. Update book stock
            book.borrowCopy();
            libraryDB.updateBook(conn, book);

            // 2. Create the loan transaction record
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plus(LOAN_PERIOD_DAYS, ChronoUnit.DAYS);
            Transaction newLoan = new Transaction(currentMember.getMemberId(), bookId, borrowDate, dueDate);
            libraryDB.createLoanTransaction(conn, newLoan);

            conn.commit(); // Commit both changes
            System.out.println("Book '" + book.getTitle() + "' borrowed successfully. It is due on: " + dueDate);

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) conn.rollback(); // Rollback on any error
            throw e; // Re-throw exception to be handled by the UI layer
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Handles the entire process of a member returning a book within a single database transaction.
     * @param currentMember The member returning the book.
     * @param bookId The ID of the book being returned.
     */
    @Override
    public void returnBook(Member currentMember, int bookId, int transactionId) throws SQLException, IllegalStateException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // 1. Find the loan transaction by its unique ID
            Transaction activeLoan = libraryDB.findTransactionById(conn, transactionId)
                    .orElseThrow(() -> new IllegalStateException("No transaction found with ID " + transactionId));

            // 2. Perform validation checks
            if (activeLoan.getMemberId() != currentMember.getMemberId()) {
                throw new IllegalStateException("This transaction does not belong to you.");
            }
            if (activeLoan.getBookId() != bookId) {
                throw new IllegalStateException("Transaction ID " + transactionId + " does not correspond to book ID " + bookId + ".");
            }
            if (!activeLoan.getTransactionStatus().equals("ACTIVE")) {
                throw new IllegalStateException("This loan is not active. It may have already been returned or marked as overdue.");
            }

            Book book = libraryDB.findBookById(conn, bookId).get();

            // 3. Update book stock
            book.returnCopy();
            libraryDB.updateBook(conn, book);

            // 4. Update the loan transaction to mark it as returned
            libraryDB.updateTransactionOnReturn(conn, activeLoan.getTransactionId());

            conn.commit();
            System.out.println("Book '" + book.getTitle() + "' returned successfully.");

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // --- Add these methods to your TransactionManagementService.java ---
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00"); // Use BigDecimal for currency

    @Override
    public List<Transaction> getAllTransactions() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.getAllTransactions(conn);
        }
    }

    @Override
    public List<Transaction> getMyTransactions(Member currentMember) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findTransactionsByMemberId(conn, currentMember.getMemberId());
        }
    }

    // --- Add this method to your TransactionManagementService.java ---

    @Override
    public Optional<Transaction> findTransactionById(int transactionId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findTransactionById(conn, transactionId);
        }
    }

// Also ensure you have a simple findMemberById in MemberManagementService
// and findBookById in BookManagementService that can be called from the main class.

    /**
     * Scans for overdue books and creates fines for them.
     * @return The number of new fines that were created.
     */
    @Override
    public int generateFinesForOverdueBooks() throws SQLException {
        Connection conn = null;
        int finesCreated = 0;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            List<Transaction> overdueLoans = libraryDB.findOverdueLoans(conn);

            for (Transaction loan : overdueLoans) {
                long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                if (daysOverdue > 0) {
                    BigDecimal fineAmount = FINE_PER_DAY.multiply(new BigDecimal(daysOverdue));
                    Fine newFine = new Fine(loan.getMemberId(), loan.getTransactionId(), fineAmount);
                    libraryDB.createFine(conn, newFine);
                    finesCreated++;
                }
            }

            conn.commit();
            return finesCreated;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}