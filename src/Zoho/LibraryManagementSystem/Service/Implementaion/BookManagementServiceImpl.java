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
/**
 * Implements the {@link BookManagementService} interface.
 * This service handles all business logic related to book management,
 * including creating books with their authors and subjects, retrieving book information,
 * removing books, and updating stock levels. It uses {@link LibraryDB} for data persistence.
 */
public class BookManagementServiceImpl implements BookManagementService {
    private final LibraryDB libraryDB;

    /**
     * Constructs a BookManagementServiceImpl with the necessary data access object.
     *
     * @param libraryDB The {@link LibraryDB} instance for database operations.
     */
    public BookManagementServiceImpl(LibraryDB libraryDB) {
        this.libraryDB = libraryDB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Book> getAllBooks() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.getAllBooks(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Book> findBookById(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findBookById(conn, bookId);
        }
    }

    /**
     * {@inheritDoc}
     * This implementation ensures that adding a book and linking its authors and
     * subjects are performed as a single atomic database transaction.
     */
    @Override
    public Book addBook(Book newBook, List<String> authorNames, List<String> subjectNames) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            libraryDB.addBook(conn, newBook); // newBook object is already simpler

            for (String authorName : authorNames) {
                if (authorName != null && !authorName.trim().isEmpty()) {
                    Author author = libraryDB.findOrCreateAuthorByName(conn, authorName.trim());
                    libraryDB.linkBookToAuthor(conn, newBook.getBookId(), author.getAuthorId());
                }
            }

            for (String subjectName : subjectNames) {
                if (subjectName != null && !subjectName.trim().isEmpty()) {
                    Subject subject = libraryDB.findOrCreateSubjectByName(conn, subjectName.trim());
                    libraryDB.linkBookToSubject(conn, newBook.getBookId(), subject.getSubjectId());
                }
            }

            conn.commit();
            return newBook;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     * This implementation first retrieves the book to check if it has any copies currently on loan.
     * If all copies are available (not loaned out), the book is removed.
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
     * {@inheritDoc}
     * This implementation ensures that the new total stock is not less than the number
     * of copies currently borrowed.
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