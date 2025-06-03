package Zoho.LibraryManagementSystem.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Fine {
    private int fineId;
    private int memberId;
    private int transactionId;
    private BigDecimal fineAmount;
    private String fineStatus;
    private LocalDate dateIssued;
    private LocalDate datePaid;

    public Fine(int memberId, int transactionId, BigDecimal fineAmount) {
        this.memberId = memberId;
        this.transactionId = transactionId;
        this.fineAmount = fineAmount;
        this.fineStatus = "OUTSTANDING";
        this.dateIssued = LocalDate.now();
    }

    public Fine(int fineId, int memberId, int transactionId, BigDecimal fineAmount, String fineStatus, LocalDate dateIssued, LocalDate datePaid) {
        this(memberId, transactionId, fineAmount);
        this.fineId = fineId;
        this.fineStatus = fineStatus;
        this.dateIssued = dateIssued;
        this.datePaid = datePaid;
    }

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