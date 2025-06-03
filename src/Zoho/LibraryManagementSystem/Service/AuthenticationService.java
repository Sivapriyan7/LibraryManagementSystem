package Zoho.LibraryManagementSystem.Service;

import Zoho.LibraryManagementSystem.Model.Member;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Defines the contract for handling user and librarian authentication.
 */
public interface AuthenticationService {
    /**
     * Attempts to log in a librarian with the given credentials.
     * @param username The librarian's username.
     * @param password The librarian's password.
     * @return true if the credentials are valid, false otherwise.
     */
    boolean librarianLogin(String username, String password);

    /**
     * Attempts to log in a member by verifying their credentials against the database.
     * @param username The member's username.
     * @param password The member's password.
     * @return An Optional containing the Member object if login is successful, otherwise an empty Optional.
     * @throws SQLException if a database access error occurs.
     */
    Optional<Member> memberLogin(String username, String password) throws SQLException;
}