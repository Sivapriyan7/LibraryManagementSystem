package Zoho.LibraryManagementSystem.Main;

import Zoho.LibraryManagementSystem.Model.Book;
import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Enum.MembershipType; // Make sure this is imported
import Zoho.LibraryManagementSystem.Model.Transaction;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.*; // Import the whole package, including interfaces and new Impl classes
import Zoho.LibraryManagementSystem.Service.Implementaion.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class LibraryManagementSystem {
    private final Scanner scanner = new Scanner(System.in);

    // --- MODIFICATION 1: DECLARE FIELDS USING INTERFACE TYPES ---
    private final AuthenticationService authService;
    private final MemberManagementService memberService;
    private final BookManagementService bookService;
    private final TransactionManagementService transactionService;

    // The constructor now accepts the INTERFACE types
    public LibraryManagementSystem(AuthenticationService authService, MemberManagementService memberService, BookManagementService bookService, TransactionManagementService transactionService) {
        this.authService = authService;
        this.memberService = memberService;
        this.bookService = bookService;
        this.transactionService = transactionService;
    }

    public static void main(String[] args) {
        // --- Dependency Injection Setup ---
        LibraryDB libraryDB = new LibraryDB();
        PasswordService passwordService = new PasswordServiceImpl(); // Use new Impl class

        // --- MODIFICATION 2: INSTANTIATE Impl CLASS, ASSIGN TO INTERFACE TYPE ---
        AuthenticationService authService = new AuthenticationServiceImpl(libraryDB, passwordService);
        MemberManagementService memberService = new MemberManagementServiceImpl(libraryDB, passwordService);
        BookManagementService bookService = new BookManagementServiceImpl(libraryDB);
        TransactionManagementService transactionService = new TransactionManagementServiceImpl(libraryDB);

        // The rest of the application is initialized with the interfaces
        LibraryManagementSystem app = new LibraryManagementSystem(authService, memberService, bookService, transactionService);

        System.out.println("Welcome to the Advanced Library Management System!");
        app.run();
    }

    public void run() {
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login as Librarian");
            System.out.println("2. Login as Member");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    librarianLogin();
                    break;
                case "2":
                    memberLogin();
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void librarianLogin() {
        System.out.println("\n--- Librarian Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authService.librarianLogin(username, password)) {
            System.out.println("Librarian login successful!");
            librarianMenu();
        } else {
            System.out.println("Invalid librarian credentials.");
        }
    }

    private void librarianMenu() {
        while (true) {
            System.out.println("\n--- Librarian Menu ---");
            System.out.println("1. Manage Members");
            System.out.println("2. Manage Books");
            System.out.println("3. Transactions");
            System.out.println("4. Generate Fines for Overdue Books");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    manageMembersMenu();
                    break;
                case "2":
                    manageBooksMenu();
                    break;
                case "3":
                    manageTransactionsMenu();
                    break;
                case "4":
                    generateFines();
                    break;
                case "5":
                    System.out.println("Logging out librarian...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void manageTransactionsMenu() {
        while (true) {
            System.out.println("\n--- Transaction Management ---");
            System.out.println("1. View All transactions");
            System.out.println("2. Search Transaction by ID");
            System.out.println("3. Back to Librarian Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch(choice) {
                case "1":
                    viewAllTransactions();
                    break;
                case "2":
                    searchTransactionById();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void manageMembersMenu() {
        while (true) {
            System.out.println("\n--- Manage Members ---");
            System.out.println("1. View List of Library Members");
            System.out.println("2. Add a New Library Member");
            System.out.println("3. Remove an Existing Member");
            System.out.println("4. Back to Librarian Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllMembers();
                    break;
                case "2":
                    addMember();
                    break;
                case "3":
                    removeMember();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void manageBooksMenu() {
        while (true) {
            System.out.println("\n--- Manage Books ---");
            System.out.println("1. View All Books in Library");
            System.out.println("2. Add a New Book");
            System.out.println("3. Remove a Book");
            System.out.println("4. Update Book Stock");
            System.out.println("5. Back to Librarian Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllBooks();
                    break;
                case "2":
                    addBook();
                    break;
                case "3":
                    removeBook();
                    break;
                case "4":
                    updateBookStock();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void memberLogin() {
        System.out.println("\n--- Member Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            Optional<Member> memberOpt = authService.memberLogin(username, password);
            if (memberOpt.isPresent()) {
                Member currentMember = memberOpt.get();
                System.out.println("Member login successful! Welcome, " + currentMember.getName());
                memberMenu(currentMember);
            } else {
                System.out.println("Invalid member username or password.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
        }
    }

    private void memberMenu(Member currentMember) {
        while (true) {
            System.out.println("\n--- Member Menu (" + currentMember.getName() + ") ---");
            System.out.println("1. Borrow Book");
            System.out.println("2. Return Book");
            System.out.println("3. View My Transactions");
            System.out.println("4. View All Books");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    borrowBook(currentMember);
                    break;
                case "2":
                    returnBook(currentMember);
                    break;
                case "3":
                    viewMyTransactions(currentMember);
                    break;
                case "4":
                    viewAllBooks();
                    break;
                case "5":
                    System.out.println("Logging out member...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // --- UI Methods for Functionality ---

    private void removeBook() {
        try {
            int bookId = getNumericInput("Enter the ID of the book to remove: ");
            bookService.removeBook(bookId);
            System.out.println("Book with ID " + bookId + " was removed successfully.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }

    private void updateBookStock() {
        try {
            int bookId = getNumericInput("Enter the ID of the book to update: ");
            int newStock = getNumericInput("Enter the new total stock count: ");
            bookService.updateBookStock(bookId, newStock);
            System.out.println("Stock for book ID " + bookId + " has been updated.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }

    private void searchTransactionById() {
        try {
            int transactionId = getNumericInput("Enter Transaction ID to search: ");
            Optional<Transaction> transactionOpt = transactionService.findTransactionById(transactionId);

            if (transactionOpt.isPresent()) {
                Transaction t = transactionOpt.get();
                System.out.println("\n--- Transaction Details ---");
                System.out.println(t);

                // Fetch and display associated data for more detail
                // Note: In a real app, a dedicated service/repo method to get a "TransactionDetail" DTO would be more efficient.
                memberService.findMemberById(t.getMemberId())
                        .ifPresent(member -> System.out.println("  -> Member Name: " + member.getName()));
                bookService.findBookById(t.getBookId())
                        .ifPresent(book -> System.out.println("  -> Book Title: '" + book.getTitle() + "'"));

            } else {
                System.out.println("Error: Transaction ID " + transactionId + " not found.");
            }
        } catch (SQLException e) {
            System.err.println("Database error searching for transaction: " + e.getMessage());
        }
    }

    // --- Helper Methods for UI Functionality ---

    private void viewAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("There are no books in the library.");
                return;
            }
            System.out.println("\n--- All Library Books ---");
            books.forEach(book -> System.out.println(book + "\n"));
        } catch (SQLException e) {
            System.err.println("Database error viewing books: " + e.getMessage());
        }
    }

    private void addBook() {
        try {
            System.out.print("Enter book title: ");
            String title = scanner.nextLine();
            System.out.print("Enter Publisher: ");
            String publisher = scanner.nextLine();
            LocalDate pubDate = getDateInput("Enter Publication Date (YYYY-MM-DD): ");
            int totalCopies = getNumericInput("Enter Total Copies: ");

            // Removed prompts for: isbn, pageCount, description, language

            System.out.print("Enter author(s) (comma-separated, press Enter if none): ");
            String authorsInput = scanner.nextLine();
            List<String> authorNames = Arrays.asList(authorsInput.split(","));

            System.out.print("Enter subject(s) (comma-separated, press Enter if none): ");
            String subjectsInput = scanner.nextLine();
            List<String> subjectNames = Arrays.asList(subjectsInput.split(","));

            // Create Book object with the simplified constructor
            Book newBook = new Book(title, publisher, pubDate, totalCopies);
            bookService.addBook(newBook, authorNames, subjectNames);

            System.out.println("Book added successfully! New Book ID: " + newBook.getBookId());

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace(); // for debugging
        }
    }

    private void viewAllMembers() {
        try {
            List<Member> members = memberService.getAllMembers();
            if (members.isEmpty()) {
                System.out.println("There are no members in the library system.");
                return;
            }
            System.out.println("\n--- All Library Members ---");
            members.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database error viewing members: " + e.getMessage());
        }
    }

// In LibraryManagementSystem.java

    private void addMember() {
        try {
            System.out.print("Enter member name: ");
            String name = scanner.nextLine();
            System.out.print("Enter a unique username: ");
            String username = scanner.nextLine();
            System.out.print("Enter a password: ");
            String password = scanner.nextLine();
            System.out.print("Enter email address: ");
            String email = scanner.nextLine();
            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine();
            System.out.print("Enter address: ");
            String address = scanner.nextLine();

            // --- MODIFIED SECTION: Input validation loop for Membership Type ---
            MembershipType membershipType;
            while (true) {
                System.out.println("Available Membership Types: " + Arrays.toString(MembershipType.values()));
                System.out.print("Enter membership type: ");
                String typeInput = scanner.nextLine();

                // Use the static helper method from the enum to validate
                if (MembershipType.isValid(typeInput)) {
                    membershipType = MembershipType.valueOf(typeInput.trim().toUpperCase());
                    break; // Exit loop if input is valid
                } else {
                    System.out.println("Invalid membership type. Please choose from the available options.");
                }
            }
            // --- END OF MODIFIED SECTION ---

            Member newMember = new Member(name, username, email, phone, address, membershipType);
            memberService.addMember(newMember, password);
            System.out.println("Member added successfully! New Member ID: " + newMember.getMemberId());

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void removeMember() {
        try {
            int memberId = getNumericInput("Enter Member ID to remove: ");
            memberService.removeMember(memberId);
            System.out.println("Member removed successfully.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void borrowBook(Member currentMember) {
        try {
            int bookId = getNumericInput("Enter Book ID to borrow: ");
            transactionService.borrowBook(currentMember, bookId);
        } catch (SQLException e) {
            System.err.println("Database error during borrow operation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Could not borrow book. Reason: " + e.getMessage());
        }
    }

    private void returnBook(Member currentMember) {
        try {
            System.out.println("Please provide the details for the book you are returning.");
            int bookId = getNumericInput("Enter Book ID: ");
            int transactionId = getNumericInput("Enter the Transaction ID from your loan: ");

            transactionService.returnBook(currentMember, bookId, transactionId);

        } catch (SQLException e) {
            System.err.println("Database error during return operation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Could not return book. Reason: " + e.getMessage());
        }
    }

    private void viewAllTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            if (transactions.isEmpty()) {
                System.out.println("No transactions found in the system.");
                return;
            }
            System.out.println("\n--- All System Transactions ---");
            transactions.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database error viewing transactions: " + e.getMessage());
        }
    }

    private void viewMyTransactions(Member currentMember) {
        try {
            List<Transaction> transactions = transactionService.getMyTransactions(currentMember);
            if (transactions.isEmpty()) {
                System.out.println("You have no transaction history.");
                return;
            }
            System.out.println("\n--- Your Transaction History ---");
            transactions.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database error viewing your transactions: " + e.getMessage());
        }
    }

    private void generateFines() {
        try {
            System.out.println("Scanning for overdue books and generating fines...");
            int finesCreated = transactionService.generateFinesForOverdueBooks();
            if (finesCreated > 0) {
                System.out.println("Successfully created " + finesCreated + " new fine(s).");
            } else {
                System.out.println("No new overdue books found to fine.");
            }
        } catch (SQLException e) {
            System.err.println("Database error while generating fines: " + e.getMessage());
        }
    }

    // --- Input Helper Methods ---

    private int getNumericInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }
}