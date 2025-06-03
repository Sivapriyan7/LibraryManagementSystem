package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Repository.DatabaseConnector;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.AuthenticationService;
import Zoho.LibraryManagementSystem.Service.PasswordService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService{
    private static final String LIBRARIAN_USERNAME = "admin";
    private static final String LIBRARIAN_PASSWORD = "admin";

    private final LibraryDB libraryDB;
    private final PasswordService passwordService;

    public AuthenticationServiceImpl(LibraryDB libraryDB, PasswordService passwordService) {
        this.libraryDB = libraryDB;
        this.passwordService = passwordService;
    }

    @Override
    public boolean librarianLogin(String username, String password) {
        return LIBRARIAN_USERNAME.equals(username) && LIBRARIAN_PASSWORD.equals(password);
    }

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