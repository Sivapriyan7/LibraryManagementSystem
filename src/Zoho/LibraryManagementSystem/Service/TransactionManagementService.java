package Zoho.LibraryManagementSystem.Service;

import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Transaction;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for services that handle book borrowing, returning, and fines.
 */
public interface TransactionManagementService {
    /**
     * Manages the process of a member borrowing a book.
     * @param currentMember The member who is borrowing.
     * @param bookId The ID of the book to be borrowed.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., book unavailable).
     */
    void borrowBook(Member currentMember, int bookId) throws SQLException, IllegalStateException;

    /**
     * Manages the process of a member returning a book.
     * @param currentMember The member who is returning.
     * @param bookId The ID of the book being returned.
     * @param transactionId The ID of the specific loan transaction being closed.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., loan not found).
     */
    void returnBook(Member currentMember, int bookId, int transactionId) throws SQLException, IllegalStateException;

    /**
     * Finds a single transaction by its unique ID.
     * @param transactionId The ID of the transaction to find.
     * @return An Optional containing the Transaction if found.
     * @throws SQLException if a database access error occurs.
     */
    Optional<Transaction> findTransactionById(int transactionId) throws SQLException;

    /**
     * Retrieves a list of all transactions in the entire system.
     * @return A List of all Transaction objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Transaction> getAllTransactions() throws SQLException;

    /**
     * Retrieves the transaction history for a specific member.
     * @param currentMember The member whose history is being requested.
     * @return A List of the member's Transaction objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Transaction> getMyTransactions(Member currentMember) throws SQLException;

    /**
     * Scans for overdue loans and creates corresponding records in the fines table.
     * @return The number of new fines that were generated.
     * @throws SQLException if a database access error occurs.
     */
    int generateFinesForOverdueBooks() throws SQLException;
}