package Zoho.LibraryManagementSystem.Model;

import Zoho.LibraryManagementSystem.Model.Enum.MembershipType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a registered member of the library.
 * This class stores personal details, login credentials (hashed password),
 * membership information, and status.
 */
public class Member {
    private int memberId;
    private String name;
    private String username;
    private String passwordHash;
    private String email;
    private String phoneNumber;
    private String address;
    private MembershipType membershipType;
    private String membershipStatus;
    private LocalDate registrationDate;
    private LocalDate expiryDate;

    /**
     * Constructs a new Member instance, typically used when a new member registers.
     * The passwordHash is set separately by the service layer after hashing.
     * Membership status defaults to "ACTIVE" and registration date to current date.
     *
     * @param name The full name of the member.
     * @param username The unique username for login.
     * @param email The member's email address.
     * @param phoneNumber The member's phone number.
     * @param address The member's physical address.
     * @param membershipType The type of membership (e.g., PUBLIC, STUDENT).
     */
    public Member(String name, String username, String email, String phoneNumber, String address, MembershipType membershipType) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.membershipType = membershipType;
        this.membershipStatus = "ACTIVE";
        this.registrationDate = LocalDate.now();
    }

    /**
     * Constructs a Member instance with all fields, typically used when reconstructing
     * a member object from database data.
     *
     * @param memberId The unique identifier for the member.
     * @param name Full name.
     * @param username Unique username.
     * @param passwordHash The stored hashed password.
     * @param email Email address.
     * @param phoneNumber Phone number.
     * @param address Physical address.
     * @param membershipType Type of membership.
     * @param membershipStatus Current status of the membership.
     * @param registrationDate Date of registration.
     * @param expiryDate Date of membership expiry, or null if not applicable.
     */
    public Member(int memberId, String name, String username, String passwordHash, String email, String phoneNumber, String address, MembershipType membershipType, String membershipStatus, LocalDate registrationDate, LocalDate expiryDate) {
        this(name, username, email, phoneNumber, address, membershipType);
        this.memberId = memberId;
        this.passwordHash = passwordHash;
        this.membershipStatus = membershipStatus;
        this.registrationDate = registrationDate;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public MembershipType getMembershipType() { return membershipType; }
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }
    public String getMembershipStatus() { return membershipStatus; }
    public void setMembershipStatus(String membershipStatus) { this.membershipStatus = membershipStatus; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "ID: " + memberId + " | Name: " + name + " | Username: " + username + " | Status: " + membershipStatus + " | Joined: " + registrationDate.format(formatter);
    }
}