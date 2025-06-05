package Zoho.LibraryManagementSystem.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a fine issued to a member for an overdue book loan.
 * This class tracks the fine amount, its status, and related dates.
 */
public class Fine {
    private int fineId;
    private int memberId;
    private int transactionId; // The ID of the overdue loan transaction
    private BigDecimal fineAmount;
    private String fineStatus; // e.g., OUTSTANDING, PAID
    private LocalDate dateIssued;
    private LocalDate datePaid;

    /**
     * Constructs a new Fine instance when a fine is initially created.
     * The status defaults to "OUTSTANDING" and the issue date to the current date.
     *
     * @param memberId The ID of the member who incurred the fine.
     * @param transactionId The ID of the loan {@link Transaction} that resulted in this fine.
     * @param fineAmount The calculated amount of the fine.
     */
    public Fine(int memberId, int transactionId, BigDecimal fineAmount) {
        this.memberId = memberId;
        this.transactionId = transactionId;
        this.fineAmount = fineAmount;
        this.fineStatus = "OUTSTANDING";
        this.dateIssued = LocalDate.now();
    }

    /**
     * Constructs a Fine instance with all fields, typically used when reconstructing
     * a fine object from database data.
     *
     * @param fineId The unique identifier for the fine.
     * @param memberId The ID of the member.
     * @param transactionId The ID of the related loan transaction.
     * @param fineAmount The amount of the fine.
     * @param fineStatus The current status of the fine (e.g., "OUTSTANDING", "PAID").
     * @param dateIssued The date the fine was issued.
     * @param datePaid The date the fine was paid, or null if unpaid.
     */
    public Fine(int fineId, int memberId, int transactionId, BigDecimal fineAmount, String fineStatus, LocalDate dateIssued, LocalDate datePaid) {
        this(memberId, transactionId, fineAmount);
        this.fineId = fineId;
        this.fineStatus = fineStatus;
        this.dateIssued = dateIssued;
        this.datePaid = datePaid;
    }

    // Getters and Setters
    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }
    public String getFineStatus() { return fineStatus; }
    public void setFineStatus(String fineStatus) { this.fineStatus = fineStatus; }
    public LocalDate getDateIssued() { return dateIssued; }
    public void setDateIssued(LocalDate dateIssued) { this.dateIssued = dateIssued; }
    public LocalDate getDatePaid() { return datePaid; }
    public void setDatePaid(LocalDate datePaid) { this.datePaid = datePaid; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "Fine ID: " + fineId + " | Member ID: " + memberId + " | Loan Txn ID: " + transactionId +
                " | Amount: " + fineAmount + " | Status: " + fineStatus +
                " | Issued: " + dateIssued.format(formatter) +
                " | Paid: " + (datePaid != null ? datePaid.format(formatter) : "N/A");
    }
}