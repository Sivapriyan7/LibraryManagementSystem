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

public class ReservationManagementServiceImpl implements ReservationManagementService {
    private final LibraryDB libraryDB;

    public ReservationManagementServiceImpl(LibraryDB libraryDB) {
        this.libraryDB = libraryDB;
    }

    @Override
    public void placeReservation(Member member, int bookId) throws SQLException, IllegalStateException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // It's good practice for service methods to manage their own transactions
            // if they involve multiple DB operations or critical business rules.
            // For simplicity, if LibraryDB methods are atomic, this might not need explicit transaction handling here.
            // However, for consistency with other services, let's assume it could be needed.
            conn.setAutoCommit(false); // Example of starting a transaction

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
        } catch (SQLException | IllegalStateException e) {
            // A real application might have a shared utility to handle rollback
            // For now, we rethrow. If conn was used, it should be rolled back in a finally block if auto-commit was false.
            throw e;
        }
        // Note: Connection handling (closing, rollback on exception if autoCommit=false)
        // should be robustly managed, typically in a finally block if not using try-with-resources for the Connection itself.
        // Since LibraryDB methods take Connection, the service methods often manage the Connection lifecycle.
    }

    @Override
    public List<Reservation> getMyActiveReservations(Member member) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findActiveReservationsByMember(conn, member.getMemberId());
        }
    }

    @Override
    public List<Reservation> getAllActiveReservations() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findAllActiveReservations(conn);
        }
    }

    @Override
    public Optional<Reservation> getNextWaitingReservationForBook(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findNextWaitingReservationForBook(conn, bookId);
        }
    }

    @Override
    public void updateReservationStatus(int reservationId, String newStatus) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            libraryDB.updateReservationStatus(conn, reservationId, newStatus);
        }
    }
}