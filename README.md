# Library Management System

## Description

This is a console-based Library Management System application built with Java. It allows librarians to manage books, members, and track book loans. Members can log in to borrow and return books, and view their transaction history. The system also includes features for managing book reservations and generating fines for overdue books.

The application demonstrates a layered architecture (Presentation, Service, Repository, Model) and incorporates object-oriented programming principles, including SOLID.

## Features

* **User Roles:** Separate functionalities for Librarians and Members.
* **Authentication:** Secure login for librarians and members using BCrypt for password hashing.
* **Book Management (Librarian):**
    * Add new books with details like title, publisher, publication date, authors, and subjects.
    * View all books in the library.
    * Remove books from the catalog (with checks for outstanding loans).
    * Update book stock information.
* **Member Management (Librarian):**
    * Add new members with detailed profiles (name, contact info, membership type).
    * View all registered members.
    * Remove members (with checks for outstanding loans/fines).
* **Loan Management (Member & Librarian):**
    * Members can borrow available books.
    * Due dates are automatically calculated.
    * Members can return borrowed books using a transaction ID for accuracy.
* **Transaction Tracking:**
    * Librarians can view all loan transactions in the system.
    * Librarians can search for specific transactions by ID to see details (member, book, dates).
    * Members can view their own transaction history.
* **Reservation Management:**
    * Members can place reservations for books that are currently out of stock.
    * Librarians can view all active reservations.
    * Librarians can "notify" the next member in the queue when a reserved book becomes available (updates reservation status to 'AVAILABLE').
    * Borrowing a book for which a member has an 'AVAILABLE' reservation automatically marks the reservation as 'FULFILLED'.
    * Librarians can manually mark reservations as 'FULFILLED'.
* **Fine Management (Librarian):**
    * Librarians can trigger a process to generate fines for overdue books.
* **Data Integrity:**
    * Uses database transactions for critical operations like borrowing and returning books to ensure atomicity.
    * Strict membership types enforced using Java Enums.

## Technologies Used

* **Java:** JDK 11 (or newer recommended)
* **PostgreSQL:** Relational Database Management System
* **JDBC:** Java Database Connectivity for interacting with PostgreSQL
* **jBCrypt:** Library for secure password hashing

## Prerequisites

Before you begin, ensure you have the following installed:
1.  **Java Development Kit (JDK):** Version 11 or newer.
2.  **PostgreSQL Server:** Version 12 or newer recommended. Make sure the server is running.
3.  **(Optional but Recommended) SQL Client:** pgAdmin (often bundled with PostgreSQL) or DBeaver for database management.
4.  **jBCrypt Library:** The `jbcrypt.jar` file needs to be available in your project's classpath. You can download it from Maven Central (groupId: `org.mindrot`, artifactId: `jbcrypt`, version: `0.4`).

## Setup Instructions

Follow these steps to set up and run the application:

### 1. Get the Code
Clone the repository or download/unzip the project files to your local machine.

### 2. Database Setup
You will need to create a PostgreSQL database and the necessary tables. Two SQL scripts are provided in the `sql/` directory (you'll need to create this directory or place the scripts at the project root):
* `database_setup.sql`: Creates the database schema (tables, relationships, constraints).
* `sample_data.sql`: (Optional) Inserts some sample data to get you started.

**Steps:**
1.  **Connect to PostgreSQL:**
    * Open your SQL client (e.g., pgAdmin, or `psql` command line) and connect to your PostgreSQL server as a superuser (e.g., `postgres`) or a user with database creation privileges.

2.  **(Optional but Recommended) Create a Dedicated User & Database:**
     ```sql
     -- Run as a PostgreSQL superuser
     CREATE USER library_app_user WITH PASSWORD 'your_secure_password_here';
     CREATE DATABASE library_system_db OWNER library_app_user;
     -- Grant privileges (if not owner, or for specific schema access if needed)
     -- GRANT ALL PRIVILEGES ON DATABASE library_system_db TO library_app_user;
     ```
    If you choose not to create a dedicated user, you can use your existing `postgres` user, but you'll still need to create the database:
     ```sql
     CREATE DATABASE library_system_db;
     ```

3.  **Connect to `library_system_db`:**
    * In your SQL client, switch your connection to the newly created `library_system_db` database.
    * If using `psql`: `\c library_system_db`

4.  **Execute `database_setup.sql`:**
    * Open the `database_setup.sql` file.
    * Copy its entire content and execute it in your SQL client connected to `library_system_db`. This will create all the tables.

5.  **(Optional) Execute `sample_data.sql`:**
    * If you want to populate the database with some initial data, open `sample_data.sql`.
    * Copy its content and execute it in your SQL client (still connected to `library_system_db`).

### 3. Application Configuration
You need to configure the Java application to connect to your PostgreSQL database.
1.  Navigate to the file: `src/Zoho/LibraryManagementSystem/Repository/DatabaseConnector.java` (Adjust path if your source folder is different).
2.  Update the following constants with your database details:
     ```java
     // Inside DatabaseConnector.java
     private static final String DB_URL = "jdbc:postgresql://localhost:5432/library_system_db"; // Ensure 'library_system_db' matches your DB name.
     private static final String DB_USER = "your_postgres_user"; // e.g., "postgres" or "library_app_user"
     private static final String DB_PASSWORD = "your_postgres_password"; // The password for the DB_USER
     ```

### 4. Dependencies (jBCrypt)
Ensure the `jbcrypt-0.4.jar` (or the version you downloaded) is included in your project's classpath when compiling and running.
* If using an IDE (IntelliJ, Eclipse): Add the JAR as a library to your project.
* If compiling from the command line: You'll need to include it using the `-cp` or `-classpath` option.

## How to Compile and Run

**Using an IDE (Recommended):**
1.  Import the project into your IDE (e.g., IntelliJ IDEA, Eclipse).
2.  Ensure the JDK is correctly configured for the project.
3.  Ensure `jbcrypt.jar` is added to the project's libraries/dependencies.
4.  Locate the `LibraryManagementSystem.java` file in the `Zoho.LibraryManagementSystem.Main` package.
5.  Right-click on the file and select "Run 'LibraryManagementSystem.main()'".


## Usage

* **Default Librarian Credentials:**
    * Username: `admin`
    * Password: `admin`
* **Member Accounts:**
    * Member accounts must be created by the librarian through the "Manage Members" -> "Add a New Library Member" option. This is because the application handles password hashing during creation.
    * If you used the `sample_data.sql` script, it primarily populates books, authors, and subjects. For members, it's best to create them via the application UI to ensure passwords are properly hashed.

## Project Structure Overview

* **`Zoho.LibraryManagementSystem.Main`:** Contains the main application class (`LibraryManagementSystem.java`) responsible for user interface and interaction.
* **`Zoho.LibraryManagementSystem.Service`:** Contains service interfaces (e.g., `BookManagementService`) and their implementations (e.g., `BookManagementServiceImpl`). This layer holds the business logic.
* **`Zoho.LibraryManagementSystem.Repository`:** Contains classes responsible for data access (`LibraryDB.java`) and database connection (`DatabaseConnector.java`).
* **`Zoho.LibraryManagementSystem.Model`:** Contains Plain Old Java Objects (POJOs) representing the data entities (e.g., `Book.java`, `Member.java`) and Enums (e.g., `MembershipType.java`).

---