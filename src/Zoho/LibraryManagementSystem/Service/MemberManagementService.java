package Zoho.LibraryManagementSystem.Service;

import Zoho.LibraryManagementSystem.Model.Member;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for services that manage library members.
 */
public interface MemberManagementService {
    /**
     * Adds a new member to the system.
     * @param newMember The Member object containing profile information.
     * @param plainTextPassword The desired password for the new member.
     * @return The created Member object with its new database-generated ID.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if the username is already taken.
     */
    Member addMember(Member newMember, String plainTextPassword) throws SQLException, IllegalStateException;

    /**
     * Retrieves a list of all members in the library.
     * @return A List of all Member objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Member> getAllMembers() throws SQLException;

    /**
     * Finds a single member by their unique ID.
     * @param memberId The ID of the member to find.
     * @return An Optional containing the Member if found.
     * @throws SQLException if a database access error occurs.
     */
    Optional<Member> findMemberById(int memberId) throws SQLException;

    /**
     * Removes a member from the system by their ID.
     * @param memberId The ID of the member to remove.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if the member has outstanding loans.
     */
    void removeMember(int memberId) throws SQLException, IllegalStateException;
}