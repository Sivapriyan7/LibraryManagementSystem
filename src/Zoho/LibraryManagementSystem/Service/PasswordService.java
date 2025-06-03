package Zoho.LibraryManagementSystem.Service;

/**
 * Defines the contract for a service that handles password hashing and verification.
 */
public interface PasswordService {
    /**
     * Hashes a plain-text password using a secure, one-way algorithm.
     * @param plainTextPassword The password to hash.
     * @return A salted and hashed password string suitable for database storage.
     */
    String hashPassword(String plainTextPassword);

    /**
     * Checks if a given plain-text password matches a stored hashed password.
     * @param plainTextPassword The password provided by the user during login.
     * @param hashedPassword The stored hash from the database.
     * @return true if the password is correct, false otherwise.
     */
    boolean checkPassword(String plainTextPassword, String hashedPassword);
}