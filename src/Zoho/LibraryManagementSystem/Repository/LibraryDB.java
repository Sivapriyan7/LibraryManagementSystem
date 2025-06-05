package Zoho.LibraryManagementSystem.Repository;

import Zoho.LibraryManagementSystem.Model.*;
import Zoho.LibraryManagementSystem.Model.Enum.MembershipType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for the Library Management System.
 * This class handles all direct database interactions (CRUD operations) for
 * all entities like Members, Books, Transactions, Authors, Subjects, Fines, and Reservations.
 * All public methods in this class that perform database operations expect an active
 * {@link Connection} object to be passed, allowing for external transaction management
 * by the service layer.
 */
public class LibraryDB {

    // --- Member Methods ---

    /**
     * Inserts a new member into the database along with their hashed password.
     *
     * @param conn The active database connection.
     * @param member The {@link Member} object to be added (without ID, which will be generated).
     * @param hashedPassword The BCrypt hashed password for the member.
     * @return The {@link Member} object updated with the database-generated memberId.
     * @throws SQLException if a database access error occurs.
     */
    public Member addMember(Connection conn, Member member, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO members (name, username, password_hash, email, phone_number, address, membership_type, membership_status, registration_date, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING member_id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getUsername());
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, member.getEmail());
            pstmt.setString(5, member.getPhoneNumber());
            pstmt.setString(6, member.getAddress());
            pstmt.setString(7, member.getMembershipType().name());
            pstmt.setString(8, member.getMembershipStatus());
            pstmt.setDate(9, Date.valueOf(member.getRegistrationDate()));
            pstmt.setDate(10, member.getExpiryDate() != null ? Date.valueOf(member.getExpiryDate()) : null);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                member.setMemberId(rs.getInt("member_id"));
            }
            return member;
        }
    }
    /**
     * Finds a member by their unique username.
     *
     * @param conn The active database connection.
     * @param username The username to search for.
     * @return An {@link Optional} containing the {@link Member} if found, otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<Member> findMemberByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT * FROM members WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToMember(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a single member from the database based on their primary key.
     *
     * @param conn A valid database connection.
     * @param memberId The ID of the member to retrieve.
     * @return An {@link Optional} containing the {@link Member} if found, otherwise empty.
     * @throws SQLException if a database error occurs.
     */
    public Optional<Member> findMemberById(Connection conn, int memberId) throws SQLException {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToMember(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of all members from the database, ordered by name.
     *
     * @param conn The active database connection.
     * @return A {@link List} of {@link Member} objects; an empty list if no members exist.
     * @throws SQLException if a database access error occurs.
     */
    public List<Member> getAllMembers(Connection conn) throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(mapRowToMember(rs));
            }
        }
        return members;
    }

    /**
     * Checks if a member has any loans with an 'ACTIVE' status.
     * Used to prevent deletion of members with outstanding loans.
     *
     * @param conn The active database connection.
     * @param memberId The ID of the member to check.
     * @return {@code true} if the member has open borrows, {@code false} otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean hasOpenBorrows(Connection conn, int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE member_id = ? AND transaction_status = 'ACTIVE'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Deletes a member from the database by their ID.
     *
     * @param conn The active database connection.
     * @param memberId The ID of the member to delete.
     * @return {@code true} if the member was successfully deleted (row affected), {@code false} otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean removeMember(Connection conn, int memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }



    // --- Book Methods ---

    /**
     * Inserts a new book into the database.
     * The book's ID is generated by the database and updated in the passed Book object.
     * Associated authors and subjects must be linked in separate operations.
     *
     * @param conn The active database connection.
     * @param book The {@link Book} object to be added.
     * @return The {@link Book} object updated with the database-generated bookId.
     * @throws SQLException if a database access error occurs.
     */
    public Book addBook(Connection conn, Book book) throws SQLException {
        // Removed isbn, page_count, description, language, cover_image_url from SQL
        String sql = "INSERT INTO books (title, publisher, publication_date, total_copies, copies_available, times_borrowed) VALUES (?, ?, ?, ?, ?, ?) RETURNING book_id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getPublisher());
            pstmt.setDate(3, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);
            pstmt.setInt(4, book.getTotalCopies());
            pstmt.setInt(5, book.getCopiesAvailable()); // Should be same as total for a new book
            pstmt.setInt(6, 0); // New book hasn't been borrowed

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                book.setBookId(rs.getInt("book_id"));
            }
            return book;
        }
    }

    /**
     * Deletes a book from the database by its ID.
     * This method should be called after business logic checks (e.g., no active loans for the book).
     *
     * @param conn A valid database connection.
     * @param bookId The ID of the book to delete.
     * @return {@code true} if the book was deleted successfully, {@code false} otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean removeBook(Connection conn, int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Finds a book by its ID and populates its associated authors and subjects.
     *
     * @param conn A valid database connection.
     * @param bookId The ID of the book to find.
     * @return An {@link Optional} containing the {@link Book} if found, otherwise empty.
     * @throws SQLException if a database error occurs.
     */
    public Optional<Book> findBookById(Connection conn, int bookId) throws SQLException {
        String bookSQL = "SELECT * FROM books WHERE book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(bookSQL)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Book book = mapRowToBook(rs);
                book.setAuthors(findAuthorsForBook(conn, bookId));
                book.setSubjects(findSubjectsForBook(conn, bookId));
                return Optional.of(book);
            }
        }
        return Optional.empty();
    }
    /**
     * Retrieves all books from the database, ordered by title.
     * Each book is populated with its associated authors and subjects.
     *
     * @param conn The active database connection.
     * @return A {@link List} of {@link Book} objects; an empty list if no books exist.
     * @throws SQLException if a database access error occurs.
     */
    public List<Book> getAllBooks(Connection conn) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = mapRowToBook(rs);
                book.setAuthors(findAuthorsForBook(conn, book.getBookId()));
                book.setSubjects(findSubjectsForBook(conn, book.getBookId()));
                books.add(book);
            }
        }
        return books;
    }

    /**
     * Updates the details of an existing book in the database.
     *
     * @param conn The active database connection.
     * @param book The {@link Book} object containing the updated information.
     * @throws SQLException if a database access error occurs.
     */
    public void updateBook(Connection conn, Book book) throws SQLException {
        // Removed isbn, page_count, description, language, cover_image_url from SQL
        String sql = "UPDATE books SET title=?, publisher=?, publication_date=?, total_copies=?, copies_available=?, times_borrowed=? WHERE book_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getPublisher());
            pstmt.setDate(3, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);
            pstmt.setInt(4, book.getTotalCopies());
            pstmt.setInt(5, book.getCopiesAvailable());
            pstmt.setInt(6, book.getTimesBorrowed());
            pstmt.setInt(7, book.getBookId());
            pstmt.executeUpdate();
        }
    }

    // --- Author and Subject Linking Methods ---

    /**
     * Finds an author by name. If the author does not exist, creates a new author record.
     *
     * @param conn The active database connection.
     * @param name The name of the author to find or create.
     * @return The found or newly created {@link Author} object.
     * @throws SQLException if a database access error occurs or if creation fails.
     */
    public Author findOrCreateAuthorByName(Connection conn, String name) throws SQLException {
        String findSql = "SELECT author_id, author_name FROM authors WHERE author_name = ?";
        try (PreparedStatement findPstmt = conn.prepareStatement(findSql)) {
            findPstmt.setString(1, name);
            ResultSet rs = findPstmt.executeQuery();
            if (rs.next()) {
                return new Author(rs.getInt("author_id"), rs.getString("author_name"));
            } else {
                String insertSql = "INSERT INTO authors (author_name) VALUES (?) RETURNING author_id, author_name";
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    insertPstmt.setString(1, name);
                    rs = insertPstmt.executeQuery();
                    if (rs.next()) {
                        return new Author(rs.getInt("author_id"), rs.getString("author_name"));
                    }
                }
            }
        }
        throw new SQLException("Could not find or create author: " + name);
    }
    /**
     * Finds a subject by name. If the subject does not exist, creates a new subject record.
     *
     * @param conn The active database connection.
     * @param name The name of the subject to find or create.
     * @return The found or newly created {@link Subject} object.
     * @throws SQLException if a database access error occurs or if creation fails.
     */
    public Subject findOrCreateSubjectByName(Connection conn, String name) throws SQLException {
        String findSql = "SELECT subject_id, subject_name FROM subjects WHERE subject_name = ?";
        try (PreparedStatement findPstmt = conn.prepareStatement(findSql)) {
            findPstmt.setString(1, name);
            ResultSet rs = findPstmt.executeQuery();
            if (rs.next()) {
                return new Subject(rs.getInt("subject_id"), rs.getString("subject_name"));
            } else {
                String insertSql = "INSERT INTO subjects (subject_name) VALUES (?) RETURNING subject_id, subject_name";
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    insertPstmt.setString(1, name);
                    rs = insertPstmt.executeQuery();
                    if (rs.next()) {
                        return new Subject(rs.getInt("subject_id"), rs.getString("subject_name"));
                    }
                }
            }
        }
        throw new SQLException("Could not find or create subject: " + name);
    }
    /**
     * Creates a link between a book and an author in the `book_authors` association table.
     *
     * @param conn The active database connection.
     * @param bookId The ID of the book.
     * @param authorId The ID of the author.
     * @throws SQLException if a database access error occurs.
     */
    public void linkBookToAuthor(Connection conn, int bookId, int authorId) throws SQLException {
        String sql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, authorId);
            pstmt.executeUpdate();
        }
    }
    /**
     * Creates a link between a book and a subject in the `book_subjects` association table.
     *
     * @param conn The active database connection.
     * @param bookId The ID of the book.
     * @param subjectId The ID of the subject.
     * @throws SQLException if a database access error occurs.
     */
    public void linkBookToSubject(Connection conn, int bookId, int subjectId) throws SQLException {
        String sql = "INSERT INTO book_subjects (book_id, subject_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, subjectId);
            pstmt.executeUpdate();
        }
    }

    // --- Transaction (Loan) Methods ---
    /**
     * Inserts a new loan transaction into the database.
     *
     * @param conn The active database connection.
     * @param transaction The {@link Transaction} object representing the loan.
     * @return The {@link Transaction} object updated with the database-generated transactionId.
     * @throws SQLException if a database access error occurs.
     */
    public Transaction createLoanTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (member_id, book_id, borrow_date, due_date, transaction_status) VALUES (?, ?, ?, ?, ?) RETURNING transaction_id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getMemberId());
            pstmt.setInt(2, transaction.getBookId());
            pstmt.setDate(3, Date.valueOf(transaction.getBorrowDate()));
            pstmt.setDate(4, Date.valueOf(transaction.getDueDate()));
            pstmt.setString(5, transaction.getTransactionStatus());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                transaction.setTransactionId(rs.getInt("transaction_id"));
            }
            return transaction;
        }
    }

    /**
     * Finds an active loan for a specific member and book.
     * An active loan has a status of 'ACTIVE'.
     *
     * @param conn The active database connection.
     * @param memberId The ID of the member.
     * @param bookId The ID of the book.
     * @return An {@link Optional} containing the {@link Transaction} if an active loan is found,
     * otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<Transaction> findActiveLoan(Connection conn, int memberId, int bookId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE member_id = ? AND book_id = ? AND transaction_status = 'ACTIVE'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToTransaction(rs));
            }
        }
        return Optional.empty();
    }
    /**
     * Updates an existing loan transaction when a book is returned.
     * Sets the return_date to the current date and transaction_status to 'RETURNED'.
     *
     * @param conn The active database connection.
     * @param transactionId The ID of the transaction to update.
     * @throws SQLException if a database access error occurs.
     */
    public void updateTransactionOnReturn(Connection conn, int transactionId) throws SQLException {
        String sql = "UPDATE transactions SET return_date = ?, transaction_status = ? WHERE transaction_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "RETURNED");
            pstmt.setInt(3, transactionId);
            pstmt.executeUpdate();
        }
    }

    // --- Private Helper & Mapper Methods ---

    private List<Author> findAuthorsForBook(Connection conn, int bookId) throws SQLException {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT a.author_id, a.author_name FROM authors a JOIN book_authors ba ON a.author_id = ba.author_id WHERE ba.book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                authors.add(new Author(rs.getInt("author_id"), rs.getString("author_name")));
            }
        }
        return authors;
    }

    private List<Subject> findSubjectsForBook(Connection conn, int bookId) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT s.subject_id, s.subject_name FROM subjects s JOIN book_subjects bs ON s.subject_id = bs.subject_id WHERE bs.book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subjects.add(new Subject(rs.getInt("subject_id"), rs.getString("subject_name")));
            }
        }
        return subjects;
    }
    // Maps a ResultSet row to a Member object.
    private Member mapRowToMember(ResultSet rs) throws SQLException {
        Date expiryDateSQL = rs.getDate("expiry_date");
        LocalDate expiryDate = (expiryDateSQL != null) ? expiryDateSQL.toLocalDate() : null;
        Date regDateSQL = rs.getDate("registration_date");
        LocalDate regDate = (regDateSQL != null) ? regDateSQL.toLocalDate() : null;

        // MODIFIED: Convert string from DB back to enum
        MembershipType membershipType = MembershipType.valueOf(rs.getString("membership_type").toUpperCase());

        return new Member(
                rs.getInt("member_id"),
                rs.getString("name"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("address"),
                membershipType,
                rs.getString("membership_status"),
                regDate,
                expiryDate
        );
    }

    // --- Add these methods to your LibraryDB.java ---
    /**
     * Retrieves all loan transactions from the database, ordered by borrow date descending.
     *
     * @param conn The active database connection.
     * @return A {@link List} of all {@link Transaction} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> getAllTransactions(Connection conn) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY borrow_date DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(mapRowToTransaction(rs));
            }
        }
        return transactions;
    }
    /**
     * Retrieves all loan transactions for a specific member, ordered by borrow date descending.
     *
     * @param conn The active database connection.
     * @param memberId The ID of the member whose transactions are to be fetched.
     * @return A {@link List} of the member's {@link Transaction} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> findTransactionsByMemberId(Connection conn, int memberId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE member_id = ? ORDER BY borrow_date DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapRowToTransaction(rs));
            }
        }
        return transactions;
    }

    // --- Add this method to your LibraryDB.java ---
    /**
     * Finds a transaction by its unique ID.
     *
     * @param conn The active database connection.
     * @param transactionId The ID of the transaction to find.
     * @return An {@link Optional} containing the {@link Transaction} if found, otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<Transaction> findTransactionById(Connection conn, int transactionId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToTransaction(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves active loans that are past their due date and do not yet have a fine issued.
     * @param conn The active database connection.
     * @return A list of overdue {@link Transaction} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Transaction> findOverdueLoans(Connection conn) throws SQLException {
        List<Transaction> overdueLoans = new ArrayList<>();
        // Find active loans where due date is in the past and no fine has been issued yet.
        String sql = "SELECT t.* FROM transactions t " +
                "LEFT JOIN fines f ON t.transaction_id = f.transaction_id " +
                "WHERE t.transaction_status = 'ACTIVE' AND t.due_date < CURRENT_DATE AND f.fine_id IS NULL";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                overdueLoans.add(mapRowToTransaction(rs));
            }
        }
        return overdueLoans;
    }
    /**
     * Inserts a new fine record into the database.
     * @param conn The active database connection.
     * @param fine The {@link Fine} object to be added.
     * @throws SQLException if a database access error occurs.
     */
    public void createFine(Connection conn, Fine fine) throws SQLException {
        String sql = "INSERT INTO fines (member_id, transaction_id, fine_amount, fine_status, date_issued) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fine.getMemberId());
            pstmt.setInt(2, fine.getTransactionId());
            pstmt.setBigDecimal(3, fine.getFineAmount());
            pstmt.setString(4, "OUTSTANDING");
            pstmt.setDate(5, Date.valueOf(fine.getDateIssued()));
            pstmt.executeUpdate();
        }
    }

    // --- Add these Reservation methods to your LibraryDB.java ---
    /**
     * Inserts a new reservation record into the database.
     * @param conn The active database connection.
     * @param reservation The {@link Reservation} object to be added.
     * @return The {@link Reservation} object updated with its database-generated ID.
     * @throws SQLException if a database access error occurs.
     */
    public Reservation addReservation(Connection conn, Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (book_id, member_id, reservation_date, status) VALUES (?, ?, ?, ?) RETURNING reservation_id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservation.getBookId());
            pstmt.setInt(2, reservation.getMemberId());
            pstmt.setTimestamp(3, Timestamp.valueOf(reservation.getReservationDate()));
            pstmt.setString(4, reservation.getStatus());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                reservation.setReservationId(rs.getInt("reservation_id"));
            }
            return reservation;
        }
    }
    /**
     * Finds all active reservations (status 'WAITING' or 'AVAILABLE') for a specific member.
     * @param conn The active database connection.
     * @param memberId The ID of the member.
     * @return A list of the member's active {@link Reservation} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Reservation> findActiveReservationsByMember(Connection conn, int memberId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        // Assuming 'WAITING' and 'AVAILABLE' are considered active
        String sql = "SELECT * FROM reservations WHERE member_id = ? AND (status = 'WAITING' OR status = 'AVAILABLE') ORDER BY reservation_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapRowToReservation(rs));
            }
        }
        return reservations;
    }
    /**
     * Finds all active reservations (status 'WAITING' or 'AVAILABLE') in the system.
     * @param conn The active database connection.
     * @return A list of all active {@link Reservation} objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Reservation> findAllActiveReservations(Connection conn) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = 'WAITING' OR status = 'AVAILABLE' ORDER BY book_id, reservation_date ASC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reservations.add(mapRowToReservation(rs));
            }
        }
        return reservations;
    }
    /**
     * Finds the next reservation in 'WAITING' status for a specific book, ordered by reservation date.
     * @param conn The active database connection.
     * @param bookId The ID of the book.
     * @return An {@link Optional} containing the next {@link Reservation} if one exists.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<Reservation> findNextWaitingReservationForBook(Connection conn, int bookId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE book_id = ? AND status = 'WAITING' ORDER BY reservation_date ASC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToReservation(rs));
            }
        }
        return Optional.empty();
    }
    /**
     * Updates the status of an existing reservation.
     * @param conn The active database connection.
     * @param reservationId The ID of the reservation to update.
     * @param newStatus The new status for the reservation (e.g., "AVAILABLE", "FULFILLED").
     * @throws SQLException if a database access error occurs.
     */
    public void updateReservationStatus(Connection conn, int reservationId, String newStatus) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reservationId);
            pstmt.executeUpdate();
        }
    }
    /**
     * Finds an active reservation (status 'WAITING' or 'AVAILABLE') for a specific member and book.
     * Useful for preventing duplicate reservations or for fulfilling an 'AVAILABLE' one.
     * @param conn The active database connection.
     * @param memberId The ID of the member.
     * @param bookId The ID of the book.
     * @return An {@link Optional} containing the {@link Reservation} if found.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<Reservation> findActiveReservationByMemberAndBook(Connection conn, int memberId, int bookId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE member_id = ? AND book_id = ? AND (status = 'WAITING' OR status = 'AVAILABLE')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToReservation(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a specific reservation for a member and book with a given status.
     * Used, for example, to find an 'AVAILABLE' reservation when a member borrows a book.
     * @param conn A valid database connection.
     * @param memberId The ID of the member.
     * @param bookId The ID of the book.
     * @param status The status of the reservation to look for (e.g., "AVAILABLE").
     * @return An Optional containing the Reservation if found.
     * @throws SQLException if a database error occurs.
     */
    public Optional<Reservation> findSpecificReservationByMemberAndBook(Connection conn, int memberId, int bookId, String status) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE member_id = ? AND book_id = ? AND status = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, bookId);
            pstmt.setString(3, status);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToReservation(rs)); // Assumes mapRowToReservation exists
            }
        }
        return Optional.empty();
    }


    // Maps a ResultSet row to a Reservation object.
    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getTimestamp("reservation_date").toLocalDateTime(),
                rs.getString("status")
        );
    }
    // Maps a ResultSet row to a Book object.
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Date pubDateSQL = rs.getDate("publication_date");
        LocalDate pubDate = (pubDateSQL != null) ? pubDateSQL.toLocalDate() : null;

        // Removed isbn, page_count, description, language, cover_image_url from mapping
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("publisher"),
                pubDate,
                rs.getInt("total_copies"),
                rs.getInt("copies_available"),
                rs.getInt("times_borrowed")
        );
    }
    // Maps a ResultSet row to a Transaction object.
    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        Date returnDateSQL = rs.getDate("return_date");
        LocalDate returnDate = (returnDateSQL != null) ? returnDateSQL.toLocalDate() : null;

        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("member_id"),
                rs.getInt("book_id"),
                rs.getDate("borrow_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returnDate,
                rs.getString("transaction_status")
        );
    }
}