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

public class MemberManagementServiceImpl implements MemberManagementService {
    private final LibraryDB libraryDB;
    private final PasswordService passwordService;

    public MemberManagementServiceImpl(LibraryDB libraryDB, PasswordService passwordService) {
        this.libraryDB = libraryDB;
        this.passwordService = passwordService;
    }

    /**
     * Creates a new member, hashes their password, and saves them to the database.
     * It checks for username uniqueness before proceeding.
     * @param newMember The member object with all profile details (name, username, email, etc.).
     * @param plainTextPassword The member's desired password in plain text.
     * @return The created Member object, now including its database-generated ID.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if the username is already taken.
     */
    // MODIFIED: Method signature now uses the Enum
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
     * Finds a single member by their unique ID.
     *
     * @param memberId The ID of the member to find.
     * @return An Optional containing the Member object if found, otherwise an empty Optional.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public Optional<Member> findMemberById(int memberId) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.findMemberById(conn, memberId);
        }
    }

    /**
     * Retrieves a list of all members currently in the library system.
     * @return A List of Member objects.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public List<Member> getAllMembers() throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return libraryDB.getAllMembers(conn);
        }
    }

    /**
     * Removes a member from the system, but only if they have no outstanding books.
     * @param memberId The ID of the member to remove.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if the member has open borrows or is not found.
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