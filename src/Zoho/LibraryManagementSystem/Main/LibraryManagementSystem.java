package Zoho.LibraryManagementSystem.Main;

import Zoho.LibraryManagementSystem.Model.Book;
import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Enum.MembershipType;
import Zoho.LibraryManagementSystem.Model.Reservation;
import Zoho.LibraryManagementSystem.Model.Transaction;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.*; // Import all service interfaces
import Zoho.LibraryManagementSystem.Service.Implementaion.*;
import Zoho.LibraryManagementSystem.Service.ReservationManagementServiceImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class LibraryManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthenticationService authService;
    private final MemberManagementService memberService;
    private final BookManagementService bookService;
    private final TransactionManagementService transactionService;
    private final ReservationManagementService reservationService;

    public LibraryManagementSystem(AuthenticationService authService, MemberManagementService memberService,
                                   BookManagementService bookService, TransactionManagementService transactionService,
                                   ReservationManagementService reservationService) {
        this.authService = authService;
        this.memberService = memberService;
        this.bookService = bookService;
        this.transactionService = transactionService;
        this.reservationService = reservationService;
    }

    public static void main(String[] args) {
        // --- Dependency Injection Setup ---
        LibraryDB libraryDB = new LibraryDB();
        PasswordService passwordService = new PasswordServiceImpl();
        AuthenticationService authService = new AuthenticationServiceImpl(libraryDB, passwordService);
        MemberManagementService memberService = new MemberManagementServiceImpl(libraryDB, passwordService);
        BookManagementService bookService = new BookManagementServiceImpl(libraryDB);
        TransactionManagementService transactionService = new TransactionManagementServiceImpl(libraryDB);
        ReservationManagementService reservationService = new ReservationManagementServiceImpl(libraryDB);

        LibraryManagementSystem app = new LibraryManagementSystem(authService, memberService, bookService, transactionService, reservationService);

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
                    System.out.println("Thank you for using the Library Management System. Goodbye!");
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
            System.out.println("4. Manage Reservations");
            System.out.println("5. Generate Fines for Overdue Books");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": manageMembersMenu(); break;
                case "2": manageBooksMenu(); break;
                case "3": manageTransactionsMenu(); break;
                case "4": manageReservationsMenu(); break;
                case "5": generateFines(); break;
                case "6": System.out.println("Logging out librarian..."); return;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void manageTransactionsMenu() {
        while (true) {
            System.out.println("\n--- Transaction Management ---");
            System.out.println("1. View the list of all borrow/return transactions");
            System.out.println("2. Search a transaction by Transaction ID");
            System.out.println("3. Back to Librarian Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch(choice) {
                case "1": viewAllTransactions(); break;
                case "2": searchTransactionById(); break;
                case "3": return;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void manageReservationsMenu() {
        while (true) {
            System.out.println("\n--- Manage Reservations ---");
            System.out.println("1. View All Active Reservations");
            System.out.println("2. Notify Next Member for Available Book");
            System.out.println("3. Back to Librarian Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": viewAllActiveReservations(); break;
                case "2": notifyNextMemberForBook(); break;
                case "3": return;
                default: System.out.println("Invalid option. Please try again.");
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
                case "1": viewAllMembers(); break;
                case "2": addMember(); break;
                case "3": removeMember(); break;
                case "4": return;
                default: System.out.println("Invalid option. Please try again.");
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
                case "1": viewAllBooks(); break;
                case "2": addBook(); break;
                case "3": removeBook(); break;
                case "4": updateBookStock(); break;
                case "5": return;
                default: System.out.println("Invalid option. Please try again.");
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
            System.out.println("4. Place a Reservation for a Book");
            System.out.println("5. View My Active Reservations");
            System.out.println("6. View All Books");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": borrowBook(currentMember); break;
                case "2": returnBook(currentMember); break;
                case "3": viewMyTransactions(currentMember); break;
                case "4": placeReservation(currentMember); break;
                case "5": viewMyActiveReservations(currentMember); break;
                case "6": viewAllBooks(); break;
                case "7": System.out.println("Logging out member..."); return;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // --- UI Methods for Book Management ---
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
            System.out.println("\n--- Add New Book ---");
            System.out.print("Enter book title: ");
            String title = scanner.nextLine();
            System.out.print("Enter Publisher: ");
            String publisher = scanner.nextLine();
            LocalDate pubDate = getDateInput("Enter Publication Date (YYYY-MM-DD): ");
            int totalCopies = getNumericInput("Enter Total Copies: ");

            System.out.print("Enter author(s) (comma-separated, press Enter if none): ");
            String authorsInput = scanner.nextLine();
            List<String> authorNames = Arrays.asList(authorsInput.split("\\s*,\\s*"));

            System.out.print("Enter subject(s) (comma-separated, press Enter if none): ");
            String subjectsInput = scanner.nextLine();
            List<String> subjectNames = Arrays.asList(subjectsInput.split("\\s*,\\s*"));

            Book newBook = new Book(title, publisher, pubDate, totalCopies); // Uses the simplified constructor
            bookService.addBook(newBook, authorNames, subjectNames);

            System.out.println("Book added successfully! New Book ID: " + newBook.getBookId());

        } catch (SQLException e) {
            System.err.println("Database error adding book: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred while adding book: " + e.getMessage());
        }
    }

    private void removeBook() {
        try {
            System.out.println("\n--- Remove Book ---");
            int bookId = getNumericInput("Enter the ID of the book to remove: ");
            bookService.removeBook(bookId);
            System.out.println("Book with ID " + bookId + " was removed successfully.");
        } catch (SQLException e) {
            System.err.println("Database error removing book: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage()); // Service method provides user-friendly error
        }
    }

    private void updateBookStock() {
        try {
            System.out.println("\n--- Update Book Stock ---");
            int bookId = getNumericInput("Enter the ID of the book to update: ");
            int newStock = getNumericInput("Enter the new total stock count: ");
            bookService.updateBookStock(bookId, newStock);
            System.out.println("Stock for book ID " + bookId + " has been updated.");
        } catch (SQLException e) {
            System.err.println("Database error updating stock: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage()); // Service method provides user-friendly error
        }
    }

    // --- UI Methods for Member Management ---
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

    private void addMember() {
        try {
            System.out.println("\n--- Add New Member ---");
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

            MembershipType membershipType;
            while (true) {
                System.out.println("Available Membership Types: " + Arrays.toString(MembershipType.values()));
                System.out.print("Enter membership type: ");
                String typeInput = scanner.nextLine().toUpperCase();
                if (MembershipType.isValid(typeInput)) {
                    membershipType = MembershipType.valueOf(typeInput);
                    break;
                } else {
                    System.out.println("Invalid membership type. Please choose from the available options.");
                }
            }

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
            System.out.println("\n--- Remove Member ---");
            int memberId = getNumericInput("Enter Member ID to remove: ");
            memberService.removeMember(memberId);
            System.out.println("Member removed successfully.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // --- UI Methods for Transactions and Fines ---
    private void borrowBook(Member currentMember) {
        try {
            System.out.println("\n--- Borrow Book ---");
            System.out.println("You can view your active transactions under 'View My Transactions' to get the Transaction ID for returning.");
            int bookId = getNumericInput("Enter Book ID to borrow: ");
            transactionService.borrowBook(currentMember, bookId);
            // Success message is printed inside the service method
        } catch (SQLException e) {
            System.err.println("Database error during borrow operation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Could not borrow book. Reason: " + e.getMessage());
        }
    }

    private void returnBook(Member currentMember) {
        try {
            System.out.println("\n--- Return Book ---");
            System.out.println("Please provide the details for the book you are returning.");
            int bookId = getNumericInput("Enter Book ID: ");
            int transactionId = getNumericInput("Enter the Transaction ID from your loan (view 'My Transactions'): ");

            transactionService.returnBook(currentMember, bookId, transactionId);
            // Success message is printed inside the service method
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

    private void searchTransactionById() {
        try {
            System.out.println("\n--- Search Transaction ---");
            int transactionId = getNumericInput("Enter Transaction ID to search: ");
            Optional<Transaction> transactionOpt = transactionService.findTransactionById(transactionId);

            if (transactionOpt.isPresent()) {
                Transaction t = transactionOpt.get();
                System.out.println("\n--- Transaction Details ---");
                System.out.println(t);

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
            System.out.println("\n--- Generate Fines ---");
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

    // --- UI Methods for Reservations ---
    private void placeReservation(Member currentMember) {
        try {
            System.out.println("\n--- Place Reservation ---");
            int bookId = getNumericInput("Enter Book ID to reserve: ");
            reservationService.placeReservation(currentMember, bookId);
            System.out.println("Reservation placed successfully for Book ID: " + bookId);
        } catch (SQLException e) {
            System.err.println("Database error placing reservation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Could not place reservation: " + e.getMessage());
        }
    }

    private void viewMyActiveReservations(Member currentMember) {
        try {
            List<Reservation> reservations = reservationService.getMyActiveReservations(currentMember);
            if (reservations.isEmpty()) {
                System.out.println("You have no active reservations.");
                return;
            }
            System.out.println("\n--- Your Active Reservations ---");
            for (Reservation res : reservations) {
                System.out.println(res);
                bookService.findBookById(res.getBookId())
                        .ifPresent(book -> System.out.println("  -> Book Title: '" + book.getTitle() + "'"));
            }
        } catch (SQLException e) {
            System.err.println("Database error viewing your reservations: " + e.getMessage());
        }
    }

    private void viewAllActiveReservations() {
        try {
            List<Reservation> reservations = reservationService.getAllActiveReservations();
            if (reservations.isEmpty()) {
                System.out.println("There are no active reservations in the system.");
                return;
            }
            System.out.println("\n--- All Active Reservations ---");
            for (Reservation res : reservations) {
                System.out.println(res);
                bookService.findBookById(res.getBookId())
                        .ifPresent(book -> System.out.print("  -> Book Title: '" + book.getTitle() + "'"));
                memberService.findMemberById(res.getMemberId())
                        .ifPresent(member -> System.out.println(" | Member: " + member.getName() + " (ID: " + member.getMemberId() + ")"));
            }
        } catch (SQLException e) {
            System.err.println("Database error viewing all reservations: " + e.getMessage());
        }
    }

    private void notifyNextMemberForBook() {
        try {
            System.out.println("\n--- Notify Next Member for Reservation ---");
            int bookId = getNumericInput("Enter Book ID that has become available: ");
            Optional<Reservation> nextReservationOpt = reservationService.getNextWaitingReservationForBook(bookId);

            if (nextReservationOpt.isPresent()) {
                Reservation nextReservation = nextReservationOpt.get();
                System.out.println("\n--- Next Reservation for Book ID " + bookId + " ---");
                System.out.println(nextReservation);
                memberService.findMemberById(nextReservation.getMemberId())
                        .ifPresent(member -> System.out.println("  -> Member to notify: " + member.getName() + " (ID: " + member.getMemberId() + ")"));

                System.out.print("Do you want to mark this reservation as 'AVAILABLE' (member notified)? (yes/no): ");
                String confirm = scanner.nextLine();
                if ("yes".equalsIgnoreCase(confirm.trim())) {
                    reservationService.updateReservationStatus(nextReservation.getReservationId(), "AVAILABLE");
                    System.out.println("Reservation status updated to AVAILABLE. Member should be notified to pick up the book.");
                } else {
                    System.out.println("Reservation status not changed.");
                }
            } else {
                System.out.println("No waiting reservations found for Book ID: " + bookId);
            }
        } catch (SQLException e) {
            System.err.println("Database error processing reservation notification: " + e.getMessage());
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