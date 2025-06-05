package Zoho.LibraryManagementSystem.Main;

import Zoho.LibraryManagementSystem.Model.Book;
import Zoho.LibraryManagementSystem.Model.Member;
import Zoho.LibraryManagementSystem.Model.Enum.MembershipType;
import Zoho.LibraryManagementSystem.Model.Reservation;
import Zoho.LibraryManagementSystem.Model.Transaction;
import Zoho.LibraryManagementSystem.Repository.LibraryDB;
import Zoho.LibraryManagementSystem.Service.*;
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


/**
 * Main class for the Library Management System application.
 * This class is responsible for handling all user interactions, displaying menus,
 * processing user input, and orchestrating calls to the various service layers
 * to perform library operations. It acts as the presentation layer of the application.
 */
public class LibraryManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    // Service dependencies, injected via the constructor
    private final AuthenticationService authService;
    private final MemberManagementService memberService;
    private final BookManagementService bookService;
    private final TransactionManagementService transactionService;
    private final ReservationManagementService reservationService;

    /**
     * Constructs the LibraryManagementSystem application with all necessary service dependencies.
     * This uses dependency injection to decouple the main application logic from concrete service implementations.
     *
     * @param authService Service for handling user authentication.
     * @param memberService Service for managing library members.
     * @param bookService Service for managing books in the library.
     * @param transactionService Service for handling loan transactions and fines.
     * @param reservationService Service for managing book reservations.
     */
    public LibraryManagementSystem(AuthenticationService authService, MemberManagementService memberService,
                                   BookManagementService bookService, TransactionManagementService transactionService,
                                   ReservationManagementService reservationService) {
        this.authService = authService;
        this.memberService = memberService;
        this.bookService = bookService;
        this.transactionService = transactionService;
        this.reservationService = reservationService;
    }

    /**
     * The main entry point for the Library Management System application.
     * Initializes all dependencies (database repository, service implementations)
     * and starts the application's primary interaction loop.
     *
     * @param args Command line arguments (not used by this application).
     */
    public static void main(String[] args) {
        // --- Dependency Injection Setup ---
        // Create the single instance of the database repository
        LibraryDB libraryDB = new LibraryDB();
        // Create service implementations, injecting their dependencies
        PasswordService passwordService = new PasswordServiceImpl();
        AuthenticationService authService = new AuthenticationServiceImpl(libraryDB, passwordService);
        MemberManagementService memberService = new MemberManagementServiceImpl(libraryDB, passwordService);
        BookManagementService bookService = new BookManagementServiceImpl(libraryDB);
        ReservationManagementService reservationService = new ReservationManagementServiceImpl(libraryDB);
        TransactionManagementService transactionService = new TransactionManagementServiceImpl(libraryDB,reservationService);

        // Create the main application instance with all injected services
        LibraryManagementSystem app = new LibraryManagementSystem(authService, memberService, bookService, transactionService, reservationService);

        System.out.println("Welcome to the Advanced Library Management System!");
        // Start the main application loop
        app.run();
    }
    /**
     * Runs the main application loop, displaying the top-level menu and directing
     * user choices to the appropriate login methods or exiting the application.
     */
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
    /**
     * Handles the librarian login process. Prompts for username and password,
     * authenticates using the {@link AuthenticationService}, and upon success,
     * navigates to the librarian menu.
     */
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
    /**
     * Displays the main menu for logged-in librarians, allowing them to navigate
     * to various management sub-menus (members, books, transactions, reservations, fines)
     * or log out.
     */
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
    /**
     * Displays the menu for managing library members.
     * Allows librarians to view, add, or remove members.
     */
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
    /**
     * Displays the menu for managing library books.
     * Allows librarians to view, add, remove books, or update book stock.
     */
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
    /**
     * Displays the menu for managing library loan transactions.
     * Allows librarians to view all transactions or search for a specific transaction by ID.
     */
    private void manageTransactionsMenu() {
        while (true) {
            System.out.println("\n--- Transaction Management ---");
            System.out.println("1. View all transactions");
            System.out.println("2. Search Transaction by ID");
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
    /**
     * Displays the menu for managing book reservations.
     * Allows librarians to view all active reservations and notify members
     * when a reserved book becomes available.
     */
    private void manageReservationsMenu() {
        while (true) {
            System.out.println("\n--- Manage Reservations ---");
            System.out.println("1. View All Active Reservations");
            System.out.println("2. Notify Next Member for Available Book (Mark as AVAILABLE)");
            System.out.println("3. Manually Fulfill Reservation (Mark as FULFILLED)");
            System.out.println("4. Back to Librarian Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": viewAllActiveReservations(); break;
                case "2": notifyNextMemberForBook(); break;
                case "3": fulfillReservationManually(); break;
                case "4": return;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }
    /**
     * Triggers the process to scan for overdue books and generate fines.
     * Intended for librarian use. Uses the {@link TransactionManagementService}.
     */
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

    /**
     * Handles the UI for a librarian to manually mark a reservation as 'FULFILLED'.
     * This is typically done after a member borrows a book for which they had an
     * 'AVAILABLE' reservation, though the borrowing process now automates this.
     * This serves as a manual override or for specific cases.
     * Uses the {@link ReservationManagementService}.
     */
    private void fulfillReservationManually() {
        System.out.println("\n--- Manually Fulfill a Reservation ---");
        System.out.println("NOTE: Borrowing a book for which a member has an 'AVAILABLE' reservation now AUTOMATICALLY fulfills it.");
        System.out.println("This option is for manual overrides or specific cases.");
        try {
            int reservationId = getNumericInput("Enter the Reservation ID to mark as FULFILLED: ");
            reservationService.updateReservationStatus(reservationId, "FULFILLED");
            System.out.println("Reservation ID " + reservationId + " has been successfully marked as FULFILLED.");
        } catch (SQLException e) {
            System.err.println("Database error fulfilling reservation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    /**
     * Handles the member login process. Prompts for username and password,
     * authenticates using the {@link AuthenticationService}, and upon success,
     * navigates to the member menu.
     */
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
    /**
     * Displays the main menu for logged-in members.
     * Allows members to borrow/return books, view their transactions,
     * manage their reservations, view all books, or log out.
     * @param currentMember The currently logged-in {@link Member} object.
     */
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
    /**
     * Retrieves and displays all books currently in the library catalog.
     * Uses the {@link BookManagementService}.
     */
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
    /**
     * Handles the UI for adding a new book to the library.
     * Prompts the librarian for book details (title, publisher, authors, subjects, etc.)
     * and then calls the {@link BookManagementService} to add the book.
     */
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

            Book newBook = new Book(title, publisher, pubDate, totalCopies);
            bookService.addBook(newBook, authorNames, subjectNames);

            System.out.println("Book added successfully! New Book ID: " + newBook.getBookId());

        } catch (SQLException e) {
            System.err.println("Database error adding book: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred while adding book: " + e.getMessage());
        }
    }
    /**
     * Handles the UI for removing a book from the library.
     * Prompts the librarian for the book ID and calls the {@link BookManagementService}
     * to remove the book, after business rule checks (e.g., no copies on loan).
     */
    private void removeBook() {
        try {
            System.out.println("\n--- Remove Book ---");
            int bookId = getNumericInput("Enter the ID of the book to remove: ");
            bookService.removeBook(bookId);
            System.out.println("Book with ID " + bookId + " was removed successfully.");
        } catch (SQLException e) {
            System.err.println("Database error removing book: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }
    /**
     * Handles the UI for updating the stock (total copies) of a book.
     * Prompts the librarian for the book ID and the new stock count, then calls
     * the {@link BookManagementService}. Business rules prevent reducing stock
     * below the number of currently borrowed copies.
     */
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
            System.err.println(e.getMessage());
        }
    }

    // --- UI Methods for Member Management ---
    /**
     * Retrieves and displays a list of all registered library members.
     * Uses the {@link MemberManagementService}.
     */
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
    /**
     * Handles the UI for adding a new member to the library system.
     * Prompts the librarian for member details and password, then calls the
     * {@link MemberManagementService} to register the member.
     */
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
    /**
     * Handles the UI for removing a member from the system.
     * Prompts the librarian for the member ID and calls the {@link MemberManagementService}.
     * Business rules prevent removing members with outstanding loans.
     */
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
    /**
     * Handles the UI for a member borrowing a book.
     * Prompts for the book ID and calls the {@link TransactionManagementService}.
     * This process also automatically fulfills 'AVAILABLE' reservations for the member and book.
     * @param currentMember The member performing the borrow action.
     */
    private void borrowBook(Member currentMember) {
        try {
            System.out.println("\n--- Borrow Book ---");
            System.out.println("You can view your active transactions under 'View My Transactions' to get the Transaction ID for returning.");
            int bookId = getNumericInput("Enter Book ID to borrow: ");
            transactionService.borrowBook(currentMember, bookId);

        } catch (SQLException e) {
            System.err.println("Database error during borrow operation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Could not borrow book. Reason: " + e.getMessage());
        }
    }
    /**
     * Handles the UI for a member returning a book.
     * Prompts for the book ID and the specific transaction ID of the loan,
     * then calls the {@link TransactionManagementService}.
     * @param currentMember The member performing the return action.
     */
    private void returnBook(Member currentMember) {
        try {
            System.out.println("\n--- Return Book ---");
            System.out.println("Please provide the details for the book you are returning.");
            int bookId = getNumericInput("Enter Book ID: ");
            int transactionId = getNumericInput("Enter the Transaction ID from your loan (view 'My Transactions'): ");

            transactionService.returnBook(currentMember, bookId, transactionId);

        } catch (SQLException e) {
            System.err.println("Database error during return operation: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Could not return book. Reason: " + e.getMessage());
        }
    }
    /**
     * Retrieves and displays all loan transactions in the system.
     * Intended for librarian use. Uses the {@link TransactionManagementService}.
     */
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
    /**
     * Handles the UI for searching a specific transaction by its ID.
     * Displays detailed information about the transaction, including associated
     * member name and book title. Uses multiple services.
     */
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
    /**
     * Retrieves and displays the transaction history for the currently logged-in member.
     * Uses the {@link TransactionManagementService}.
     * @param currentMember The currently logged-in member.
     */
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


    // --- UI Methods for Reservations ---
    /**
     * Handles the UI for a member placing a reservation for a book.
     * Prompts for the book ID and calls the {@link ReservationManagementService}.
     * @param currentMember The member placing the reservation.
     */
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
    /**
     * Retrieves and displays the active reservations for the currently logged-in member.
     * Uses the {@link ReservationManagementService}.
     * @param currentMember The currently logged-in member.
     */
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
    /**
     * Retrieves and displays all active reservations in the system.
     * Intended for librarian use. Uses the {@link ReservationManagementService}.
     */
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
    /**
     * Handles the UI for notifying the next member in the queue for a reserved book
     * that has become available. Allows the librarian to update the reservation status
     * to 'AVAILABLE'. Uses the {@link ReservationManagementService}.
     */
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
    /**
     * Prompts the user for numeric input and ensures an integer is entered.
     * Loops until valid input is received.
     * @param prompt The message to display to the user.
     * @return The validated integer input from the user.
     */
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
    /**
     * Prompts the user for date input in "YYYY-MM-DD" format and ensures a valid date is entered.
     * Loops until valid input is received.
     * @param prompt The message to display to the user.
     * @return The validated {@link LocalDate} input from the user.
     */
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