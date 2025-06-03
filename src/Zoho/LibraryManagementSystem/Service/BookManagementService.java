package Zoho.LibraryManagementSystem.Service;

import Zoho.LibraryManagementSystem.Model.Book;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for services that manage the library's book catalog.
 */
public interface BookManagementService {
    /**
     * Retrieves a list of all books in the library.
     * @return A List of all Book objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Book> getAllBooks() throws SQLException;

    /**
     * Finds a single book by its unique ID.
     * @param bookId The ID of the book to find.
     * @return An Optional containing the Book if found.
     * @throws SQLException if a database access error occurs.
     */
    Optional<Book> findBookById(int bookId) throws SQLException;

    /**
     * Adds a new book to the database, including its authors and subjects.
     * @param newBook The core book object to add.
     * @param authorNames A list of names for the book's authors.
     * @param subjectNames A list of names for the book's subjects.
     * @return The fully created Book object, including its new database-generated ID.
     * @throws SQLException if a database access error occurs.
     */
    Book addBook(Book newBook, List<String> authorNames, List<String> subjectNames) throws SQLException;

    /**
     * Removes a book from the system based on its ID.
     * @param bookId The ID of the book to remove.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., book has copies on loan).
     */
    void removeBook(int bookId) throws SQLException, IllegalStateException;

    /**
     * Updates the total stock count for a specific book.
     * @param bookId The ID of the book to update.
     * @param newTotalCopies The new total number of copies.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., new stock is less than borrowed copies).
     */
    void updateBookStock(int bookId, int newTotalCopies) throws SQLException, IllegalStateException;
}

