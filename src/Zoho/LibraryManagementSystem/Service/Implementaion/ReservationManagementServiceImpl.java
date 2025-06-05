package Zoho.LibraryManagementSystem.Service;

import Zoho.LibraryManagementSystem.Model.Book;
import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Reservation;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implements the {@link ReservationManagementService} interface.
 * This service handles all business logic related to book reservations,
 * including placing, viewing, and updating the status of reservations.
 * It uses {@link LibraryDB} for data persistence.
 */
public class ReservationManagementServiceImpl implements ReservationManagementService {
    private final LibraryDB libraryDB;

    /**
     * Constructs a ReservationManagementServiceImpl with the necessary data access object.
     *
     * @param libraryDB The {@link LibraryDB} instance for database operations.
     */
    public ReservationManagementServiceImpl(LibraryDB libraryDB) {
        this.libraryDB = libraryDB;
    }

    /**
     * {@inheritDoc}
     * This implementation checks if the book is out of stock and if the member
     * doesn't already have an active reservation for the same book before placing it.
     */
    @Override
    public void placeReservation(Member member, int bookId) throws SQLException, IllegalStateException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            Book book = libraryDB.findBookById(conn, bookId)
                    .orElseThrow(() -> new IllegalStateException("Book with ID " + bookId + " not found."));

            if (book.getCopiesAvailable() > 0) {
                throw new IllegalStateException("Book '" + book.getTitle() + "' is currently in stock. Reservation not needed.");
            }

            if (libraryDB.findActiveReservationByMemberAndBook(conn, member.getMemberId(), bookId).isPresent()) {
                throw new IllegalStateException("You already have an active reservation for this book.");
            }

            Reservation newReservation = new Reservation(bookId, member.getMemberId());
            libraryDB.addReservation(conn, newReservation);

            conn.commit();
            System.out.println("Reservation placed successfully for Book ID: " + bookId); // Moved success message here

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back due to: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Error closing connection: " + ex.getMessage());
                }
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Reservation> getMyActiveReservations(Member member) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findActiveReservationsByMember(conn, member.getMemberId());
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Reservation> getAllActiveReservations() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findAllActiveReservations(conn);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Reservation> getNextWaitingReservationForBook(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findNextWaitingReservationForBook(conn, bookId);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateReservationStatus(int reservationId, String newStatus) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            libraryDB.updateReservationStatus(conn, reservationId, newStatus);
        }
    }
}