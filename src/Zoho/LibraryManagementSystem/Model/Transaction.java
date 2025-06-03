package Zoho.LibraryManagementSystem.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private int transactionId;
    private int memberId;
    private int bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String transactionStatus;

    public Transaction(int memberId, int bookId, LocalDate borrowDate, LocalDate dueDate) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.transactionStatus = "ACTIVE";
    }

    public Transaction(int transactionId, int memberId, int bookId, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, String transactionStatus) {
        this(memberId, bookId, borrowDate, dueDate);
        this.transactionId = transactionId;
        this.returnDate = returnDate;
        this.transactionStatus = transactionStatus;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "Txn ID: " + transactionId + " | Book ID: " + bookId + " | Member ID: " + memberId +
                " | Status: " + transactionStatus + " | Borrowed: " + borrowDate.format(formatter) +
                " | Due: " + dueDate.format(formatter) +
                " | Returned: " + (returnDate != null ? returnDate.format(formatter) : "N/A");
    }
}