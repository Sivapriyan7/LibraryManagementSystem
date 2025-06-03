package Zoho.LibraryManagementSystem.Service.Implementaion;

import Zoho.LibraryManagementSystem.Service.PasswordService;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordServiceImpl implements PasswordService {
    /**
     * Hashes a plain-text password using BCrypt.
     * @param plainTextPassword The password to hash.
     * @return A salted and hashed password string.
     */
    @Override
    public String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Checks a plain-text password against a stored BCrypt hash.
     * @param plainTextPassword The password to check.
     * @param hashedPassword The stored hash from the database.
     * @return true if the password matches the hash, false otherwise.
     */
    @Override
    public boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            // Protect against null hashes or non-BCrypt hashes
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}