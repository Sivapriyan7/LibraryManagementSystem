package Zoho.LibraryManagementSystem.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a reservation placed by a member for a book that is currently unavailable.
 * This class tracks the book, the member, the date of reservation, and its current status.
 */
public class Reservation {
    private int reservationId;
    private int bookId;
    private int memberId;
    private LocalDateTime reservationDate;
    private String status;

    /**
     * Constructs a new Reservation instance when a member places a reservation.
     * The reservation date defaults to the current time and status to "WAITING".
     *
     * @param bookId The ID of the {@link Book} being reserved.
     * @param memberId The ID of the {@link Member} placing the reservation.
     */
    public Reservation(int bookId, int memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservationDate = LocalDateTime.now();
        this.status = "WAITING";
    }

    /**
     * Constructs a Reservation instance with all fields, typically used when reconstructing
     * a reservation object from database data.
     *
     * @param reservationId The unique identifier for the reservation.
     * @param bookId The ID of the book.
     * @param memberId The ID of the member.
     * @param reservationDate The date and time the reservation was placed.
     * @param status The current status of the reservation.
     */
    public Reservation(int reservationId, int bookId, int memberId, LocalDateTime reservationDate, String status) {
        this(bookId, memberId);
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    // Getters and Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Reservation ID: " + reservationId + " | Book ID: " + bookId + " | Member ID: " + memberId +
                " | Status: " + status + " | Date: " + reservationDate.format(formatter);
    }
}