package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Model.Author;
import Zoho.LibraryManagementSystem.Model.Book;
import Zoho.LibraryManagementSystem.Model.Subject;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.BookManagementService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BookManagementServiceImpl implements BookManagementService {
    private final LibraryDB libraryDB;

    public BookManagementServiceImpl(LibraryDB libraryDB) {
        this.libraryDB = libraryDB;
    }

    /**
     * Retrieves a list of all books from the library, fully populated with their authors and subjects.
     * @return A list of Book objects.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public List<Book> getAllBooks() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.getAllBooks(conn);
        }
    }

    /**
     * Finds a single book by its ID.
     * @param bookId The ID of the book to find.
     * @return An Optional containing the Book if found.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public Optional<Book> findBookById(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findBookById(conn, bookId);
        }
    }

    /**
     * Adds a new book to the database, including finding/creating its authors and subjects
     * and linking them in a single database transaction.
     * @param newBook The Book object with core details.
     * @param authorNames A list of author names for the book.
     * @param subjectNames A list of subject names for the book.
     * @return The created Book object, now with a database-generated ID.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public Book addBook(Book newBook, List<String> authorNames, List<String> subjectNames) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Add the core book details to get its generated ID
            libraryDB.addBook(conn, newBook);

            // 2. Handle authors
            for (String authorName : authorNames) {
                if (authorName != null && !authorName.trim().isEmpty()) {
                    Author author = libraryDB.findOrCreateAuthorByName(conn, authorName.trim());
                    libraryDB.linkBookToAuthor(conn, newBook.getBookId(), author.getAuthorId());
                }
            }

            // 3. Handle subjects
            for (String subjectName : subjectNames) {
                if (subjectName != null && !subjectName.trim().isEmpty()) {
                    Subject subject = libraryDB.findOrCreateSubjectByName(conn, subjectName.trim());
                    libraryDB.linkBookToSubject(conn, newBook.getBookId(), subject.getSubjectId());
                }
            }

            conn.commit(); // Commit all changes if successful
            return newBook;

        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // Rollback on any error
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Removes a book from the system after checking business rules (e.g., no copies are on loan).
     * @param bookId The ID of the book to remove.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., book not found, copies on loan).
     */
    @Override
    public void removeBook(int bookId) throws SQLException, IllegalStateException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Find the book to check its status first
            Book book = libraryDB.findBookById(conn, bookId)
                    .orElseThrow(() -> new IllegalStateException("Error: Book with ID " + bookId + " not found."));

            // Business Rule: A book cannot be removed if any copies are currently on loan.
            if (book.getCopiesAvailable() < book.getTotalCopies()) {
                throw new IllegalStateException("Error: Cannot remove book. Some copies are currently on loan.");
            }

            // If checks pass, proceed with deletion.
            // The ON DELETE CASCADE in the database schema will handle removing links
            // from book_authors and book_subjects automatically.
            if (!libraryDB.removeBook(conn, bookId)) {
                throw new SQLException("Failed to remove the book from the database. It might have been deleted by another user.");
            }
        }
    }

    /**
     * Updates the total stock for a book after checking business rules.
     * @param bookId The ID of the book to update.
     * @param newTotalCopies The new total number of copies for the book.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if a business rule is violated (e.g., book not found, new stock too low).
     */
    @Override
    public void updateBookStock(int bookId, int newTotalCopies) throws SQLException, IllegalStateException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            Book book = libraryDB.findBookById(conn, bookId)
                    .orElseThrow(() -> new IllegalStateException("Error: Book with ID " + bookId + " not found."));

            int borrowedCopies = book.getTotalCopies() - book.getCopiesAvailable();

            // Business Rule: The new total cannot be less than the number of copies currently out on loan.
            if (newTotalCopies < borrowedCopies) {
                throw new IllegalStateException("Error: New total copies (" + newTotalCopies + ") cannot be less than the number of currently borrowed copies (" + borrowedCopies + ").");
            }

            int newAvailableCopies = newTotalCopies - book.getTotalCopies() + book.getCopiesAvailable();

            // The Book model's setter handles the logic of updating available copies relative to the new total.
            book.setTotalCopies(newTotalCopies);
            book.setCopiesAvailable(newAvailableCopies);

            libraryDB.updateBook(conn, book);
        }
    }
}