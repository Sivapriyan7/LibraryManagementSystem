package Zoho.LibraryManagementSystem.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private int reservationId;
    private int bookId;
    private int memberId;
    private LocalDateTime reservationDate;
    private String status;

    public Reservation(int bookId, int memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservationDate = LocalDateTime.now();
        this.status = "WAITING";
    }

    public Reservation(int reservationId, int bookId, int memberId, LocalDateTime reservationDate, String status) {
        this(bookId, memberId);
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.status = status;
    }

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