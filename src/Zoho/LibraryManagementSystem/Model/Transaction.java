package Zoho.LibraryManagementSystem.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single loan transaction in the library system.
 * This class tracks the borrowing of a specific book by a specific member,
 * including borrow date, due date, return date, and the overall status of the loan.
 */
public class Transaction {
    private int transactionId;
    private int memberId;
    private int bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String transactionStatus;

    /**
     * Constructs a new Transaction when a book is borrowed.
     * The transaction status defaults to "ACTIVE".
     *
     * @param memberId The ID of the {@link Member} borrowing the book.
     * @param bookId The ID of the {@link Book} being borrowed.
     * @param borrowDate The date the book is borrowed.
     * @param dueDate The date the book is due to be returned.
     */
    public Transaction(int memberId, int bookId, LocalDate borrowDate, LocalDate dueDate) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.transactionStatus = "ACTIVE";
    }
    /**
     * Constructs a Transaction instance with all fields, typically used when
     * reconstructing a transaction object from database data.
     *
     * @param transactionId The unique identifier for the transaction.
     * @param memberId The ID of the member.
     * @param bookId The ID of the book.
     * @param borrowDate The date the book was borrowed.
     * @param dueDate The date the book was due.
     * @param returnDate The date the book was returned, or null if still active/overdue.
     * @param transactionStatus The current status of the loan.
     */
    public Transaction(int transactionId, int memberId, int bookId, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, String transactionStatus) {
        this(memberId, bookId, borrowDate, dueDate);
        this.transactionId = transactionId;
        this.returnDate = returnDate;
        this.transactionStatus = transactionStatus;
    }
    // Getters and Setters
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