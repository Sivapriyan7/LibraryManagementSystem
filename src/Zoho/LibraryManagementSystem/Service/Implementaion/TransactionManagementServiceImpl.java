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

/**
 * Implements the {@link TransactionManagementService} interface.
 * This service handles the core business logic for book loan transactions (borrowing and returning),
 * viewing transaction history, and generating fines for overdue books.
 * It ensures operations are atomic and business rules are enforced.
 */
public class TransactionManagementServiceImpl implements TransactionManagementService {
    private final LibraryDB libraryDB;
    private static final int LOAN_PERIOD_DAYS = 14;
    private final ReservationManagementService reservationService;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    /**
     * Constructs a TransactionManagementServiceImpl with necessary dependencies.
     *
     * @param libraryDB The data access object for database operations.
     * @param reservationService The service for managing reservations, used to automate fulfillment.
     */
    public TransactionManagementServiceImpl(LibraryDB libraryDB, ReservationManagementService reservationService) {
        this.libraryDB = libraryDB;
        this.reservationService = reservationService; // NEW: Assign
    }

    /**
     * {@inheritDoc}
     * This implementation updates book stock, creates a loan record, and attempts to
     * automatically fulfill any 'AVAILABLE' reservation for the member and book,
     * all within a single database transaction.
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

                Optional<Reservation> availableReservation = libraryDB.findSpecificReservationByMemberAndBook(conn, currentMember.getMemberId(), bookId, "AVAILABLE");
                if (!availableReservation.isPresent()) { // No available reservation, and book is out of stock
                    throw new IllegalStateException("No copies of '" + book.getTitle() + "' are available, and you do not have an active 'AVAILABLE' reservation for it.");
                }
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

            // 3. Check for and fulfill an 'AVAILABLE' reservation for this member and book
            Optional<Reservation> reservationToFulfill = libraryDB.findSpecificReservationByMemberAndBook(conn, currentMember.getMemberId(), bookId, "AVAILABLE");
            if (reservationToFulfill.isPresent()) {
                libraryDB.updateReservationStatus(conn, reservationToFulfill.get().getReservationId(), "FULFILLED");
                System.out.println("Reservation ID " + reservationToFulfill.get().getReservationId() + " for this book has been automatically marked as FULFILLED.");
            }

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
     * {@inheritDoc}
     * This implementation updates book stock and marks the specified loan transaction
     * as 'RETURNED' within a single database transaction.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getAllTransactions() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.getAllTransactions(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getMyTransactions(Member currentMember) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findTransactionsByMemberId(conn, currentMember.getMemberId());
        }
    }

    /**
     * {@inheritDoc}
     * This implementation updates book stock and marks the specified loan transaction
     * as 'RETURNED' within a single database transaction.
     */
    @Override
    public Optional<Transaction> findTransactionById(int transactionId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findTransactionById(conn, transactionId);
        }
    }


    /**
     * {@inheritDoc}
     * This implementation identifies overdue loans that haven't been fined yet,
     * calculates the fine amount based on days overdue and a predefined rate,
     * and creates new fine records in the database. It may also update the
     * loan transaction status to 'OVERDUE'. This is performed in a transaction.
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