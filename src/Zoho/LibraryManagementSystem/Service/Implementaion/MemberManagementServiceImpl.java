package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.MemberManagementService;
import Zoho.LibraryManagementSystem.Service.PasswordService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implements the {@link MemberManagementService} interface.
 * This service handles the business logic for managing library members,
 * including registration, retrieval, and removal, ensuring data integrity
 * and application of business rules (e.g., username uniqueness, checking for open loans).
 * It uses {@link LibraryDB} for data persistence and {@link PasswordService} for security.
 */
public class MemberManagementServiceImpl implements MemberManagementService {
    private final LibraryDB libraryDB;
    private final PasswordService passwordService;

    /**
     * Constructs a MemberManagementServiceImpl with necessary dependencies.
     *
     * @param libraryDB The data access object for member data.
     * @param passwordService The service for password hashing.
     */
    public MemberManagementServiceImpl(LibraryDB libraryDB, PasswordService passwordService) {
        this.libraryDB = libraryDB;
        this.passwordService = passwordService;
    }

    /**
     * {@inheritDoc}
     * This implementation ensures the username is unique and hashes the password
     * before saving the new member to the database.
     */
    @Override
    public Member addMember(Member newMember, String plainTextPassword) throws SQLException, IllegalStateException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            Optional<Member> existingMember = libraryDB.findMemberByUsername(conn, newMember.getUsername());
            if (existingMember.isPresent()) {
                throw new IllegalStateException("Username '" + newMember.getUsername() + "' is already taken. Please choose another.");
            }

            String hashedPassword = passwordService.hashPassword(plainTextPassword);

            return libraryDB.addMember(conn, newMember, hashedPassword);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findMemberById(int memberId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findMemberById(conn, memberId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> getAllMembers() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.getAllMembers(conn);
        }
    }

    /**
     * {@inheritDoc}
     * This implementation checks if the member has any outstanding loans
     * before allowing removal.
     */
    @Override
    public void removeMember(int memberId) throws SQLException, IllegalStateException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Business logic check: Does the member have any open borrows?
            if (libraryDB.hasOpenBorrows(conn, memberId)) {
                throw new IllegalStateException("Member has unreturned books and cannot be removed.");
            }

            // If the check passes, attempt to remove the member
            if (!libraryDB.removeMember(conn, memberId)) {
                // This case handles if the member was deleted by another librarian between checks
                throw new IllegalStateException("Member ID " + memberId + " not found or could not be removed.");
            }
        }
    }
}