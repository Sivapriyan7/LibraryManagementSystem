package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.AuthenticationService;
import Zoho.LibraryManagementSystem.Service.PasswordService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Implements the {@link AuthenticationService} interface.
 * This service handles the authentication logic for both librarians and members,
 * interacting with the database via {@link LibraryDB} and using {@link PasswordService}
 * for member password verification.
 */
public class AuthenticationServiceImpl implements AuthenticationService{
    private static final String LIBRARIAN_USERNAME = "admin";
    private static final String LIBRARIAN_PASSWORD = "admin";

    private final LibraryDB libraryDB;
    private final PasswordService passwordService;

    /**
     * Constructs an AuthenticationServiceImpl with necessary dependencies.
     *
     * @param libraryDB The data access object for database interactions.
     * @param passwordService The service for hashing and verifying passwords.
     */
    public AuthenticationServiceImpl(LibraryDB libraryDB, PasswordService passwordService) {
        this.libraryDB = libraryDB;
        this.passwordService = passwordService;
    }
    /**
     * {@inheritDoc}
     * This implementation compares against hardcoded credentials.
     */
    @Override
    public boolean librarianLogin(String username, String password) {
        return LIBRARIAN_USERNAME.equals(username) && LIBRARIAN_PASSWORD.equals(password);
    }
    /**
     * {@inheritDoc}
     * This implementation retrieves the member by username from the database
     * and uses the PasswordService to verify the provided password against the stored hash.
     */
    @Override
    public Optional<Member> memberLogin(String username, String password) throws SQLException {
        try (Connection conn = DatabaseConnector.getConnection()) {
            Optional<Member> memberOpt = libraryDB.findMemberByUsername(conn, username);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                if (passwordService.checkPassword(password, member.getPasswordHash())) {
                    return Optional.of(member);
                }
            }
        }
        return Optional.empty();
    }
}