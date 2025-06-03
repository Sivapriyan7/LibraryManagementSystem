package Zoho.LibraryManagementSystem.Model;

import Zoho.LibraryManagementSystem.Model.Enum.MembershipType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    public Member(int memberId, String name, String username, String passwordHash, String email, String phoneNumber, String address, MembershipType membershipType, String membershipStatus, LocalDate registrationDate, LocalDate expiryDate) {
        this(name, username, email, phoneNumber, address, membershipType);
        this.memberId = memberId;
        this.passwordHash = passwordHash;
        this.membershipStatus = membershipStatus;
        this.registrationDate = registrationDate;
        this.expiryDate = expiryDate;
    }

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