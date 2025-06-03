package Zoho.LibraryManagementSystem.Service;

import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Reservation;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for services that manage book reservations.
 */
public interface ReservationManagementService {
    /**
     * Allows a member to place a reservation for a book.
     * @param member The member placing the reservation.
     * @param bookId The ID of the book to reserve.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., book in stock, already reserved).
     */
    void placeReservation(Member member, int bookId) throws SQLException, IllegalStateException;

    /**
     * Retrieves a list of active reservations for a specific member.
     * @param member The member whose reservations are to be fetched.
     * @return A list of active Reservation objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Reservation> getMyActiveReservations(Member member) throws SQLException;

    /**
     * Retrieves a list of all active reservations in the system.
     * @return A list of all active Reservation objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Reservation> getAllActiveReservations() throws SQLException;

    /**
     * Finds the next waiting reservation for a specific book (oldest first).
     * @param bookId The ID of the book.
     * @return An Optional containing the next Reservation if one exists.
     * @throws SQLException if a database access error occurs.
     */
    Optional<Reservation> getNextWaitingReservationForBook(int bookId) throws SQLException;

    /**
     * Updates the status of an existing reservation.
     * @param reservationId The ID of the reservation to update.
     * @param newStatus The new status (e.g., "AVAILABLE", "FULFILLED", "EXPIRED").
     * @throws SQLException if a database access error occurs.
     */
    void updateReservationStatus(int reservationId, String newStatus) throws SQLException;
}