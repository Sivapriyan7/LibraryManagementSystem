package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Model.*;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.ReservationManagementService;
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
    private final ReservationManagementService reservationService;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    public TransactionManagementServiceImpl(LibraryDB libraryDB, ReservationManagementService reservationService) {
        this.libraryDB = libraryDB;
        this.reservationService = reservationService; // NEW: Assign
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
            conn.setAutoCommit(false);

            Book book = libraryDB.findBookById(conn, bookId)
                    .orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " not found."));

            if (book.getCopiesAvailable() < 1) {
                // Before throwing, check if this member has an 'AVAILABLE' reservation for this specific book.
                // If they do, and this borrow action is to fulfill it, copiesAvailable might be 0 if held.
                // For now, this simple check assumes copiesAvailable is decremented only upon actual borrow.
                // A more complex scenario might involve "holding" a copy.
                Optional<Reservation> availableReservation = libraryDB.findSpecificReservationByMemberAndBook(conn, currentMember.getMemberId(), bookId, "AVAILABLE");
                if (!availableReservation.isPresent()) { // No available reservation, and book is out of stock
                    throw new IllegalStateException("No copies of '" + book.getTitle() + "' are available, and you do not have an active 'AVAILABLE' reservation for it.");
                }
                // If an 'AVAILABLE' reservation exists, proceed, the copy is implicitly held for them.
            }

            // Prevent borrowing if an active loan already exists, unless this borrow fulfills a specific reservation
            // and the previous active loan logic needs to be more nuanced.
            // For simplicity, the original check is kept. If a member has an active loan, they can't borrow again.
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

            // --- AUTOMATION LOGIC ---
            // 3. Check for and fulfill an 'AVAILABLE' reservation for this member and book
            Optional<Reservation> reservationToFulfill = libraryDB.findSpecificReservationByMemberAndBook(conn, currentMember.getMemberId(), bookId, "AVAILABLE");
            if (reservationToFulfill.isPresent()) {
                // Use the injected reservationService to update status (encapsulates logic better)
                // However, to do this within the same DB transaction, reservationService.updateReservationStatus
                // would need to accept a Connection object.
                // For now, directly call libraryDB method to ensure atomicity with the borrow.
                libraryDB.updateReservationStatus(conn, reservationToFulfill.get().getReservationId(), "FULFILLED");
                System.out.println("Reservation ID " + reservationToFulfill.get().getReservationId() + " for this book has been automatically marked as FULFILLED.");
            }
            // --- END OF AUTOMATION LOGIC ---

            conn.commit();
            System.out.println("Book '" + book.getTitle() + "' borrowed successfully. Due on: " + dueDate);

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Error during rollback: " + ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { System.err.println("Error closing connection: " + ex.getMessage()); }
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