package Zoho.LibraryManagementSystem.Repository;

import Zoho.LibraryManagementSystem.Model.*;
import Zoho.LibraryManagementSystem.Model.Enum.MembershipType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryDB {

    // --- Member Methods ---
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
     * [NEWLY ADDED] Retrieves a list of all members from the database.
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
     * [NEWLY ADDED] Checks if a member has any loans with an 'ACTIVE' status.
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
     * [NEWLY ADDED] Deletes a member from the database by their ID.
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
     * This method should be called after business logic checks in the service layer.
     * @param conn A valid database connection.
     * @param bookId The ID of the book to delete.
     * @return true if the book was deleted successfully (1 row affected), false otherwise.
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

    public void linkBookToAuthor(Connection conn, int bookId, int authorId) throws SQLException {
        String sql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, authorId);
            pstmt.executeUpdate();
        }
    }

    public void linkBookToSubject(Connection conn, int bookId, int subjectId) throws SQLException {
        String sql = "INSERT INTO book_subjects (book_id, subject_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, subjectId);
            pstmt.executeUpdate();
        }
    }

    // --- Transaction (Loan) Methods ---
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

    // Also, ensure you have a simple findMemberById method for the search feature.
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

    public void updateReservationStatus(Connection conn, int reservationId, String newStatus) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reservationId);
            pstmt.executeUpdate();
        }
    }

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


    // Helper method to map ResultSet row to Reservation object
    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getTimestamp("reservation_date").toLocalDateTime(),
                rs.getString("status")
        );
    }

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