-- File: database_setup.sql

-- -----------------------------------------------------------------------------
-- Optional: Create a dedicated database user (recommended)
-- The need to run this part as a superuser (e.g., postgres)
-- Or prefer to use their existing postgres user.
-- -----------------------------------------------------------------------------
-- CREATE USER library_manager WITH PASSWORD 'strong_password_here';
-- Note: If creating a user, ensure they have privileges to create databases or are made owner later.
-- -----------------------------------------------------------------------------
-- Create the Database
-- Ensure you are connected as a user with privileges to create databases
-- (e.g., postgres or the user created above if they have the right permissions).
-- -----------------------------------------------------------------------------
-- DROP DATABASE IF EXISTS LibraryDB; -- Optional: for a clean start
-- CREATE DATABASE LibraryDB;
-- -- If you created a dedicated user:
-- -- ALTER DATABASE LibraryDB OWNER TO library_manager;

-- -----------------------------------------------------------------------------
-- Connect to the newly created database before creating tables.
-- In psql, you would type: \c LibraryDB
-- In pgAdmin, ensure you are running the following DDL in the context of 'LibraryDB'.
-- -----------------------------------------------------------------------------

-- Drop old tables if they exist to avoid conflicts (useful for re-running script)
DROP TABLE IF EXISTS fines CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS book_authors CASCADE;
DROP TABLE IF EXISTS book_subjects CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS subjects CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS members CASCADE;

-- Create the members table
CREATE TABLE members (
    member_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20),
    address TEXT,
    membership_type VARCHAR(50) DEFAULT 'PUBLIC' NOT NULL,
    membership_status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    registration_date DATE DEFAULT CURRENT_DATE,
    expiry_date DATE
);

-- Create the books table
CREATE TABLE books (
    book_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    publication_date DATE,
    total_copies INT NOT NULL CHECK (total_copies >= 0),
    copies_available INT NOT NULL CHECK (copies_available >= 0),
    times_borrowed INT DEFAULT 0
    -- isbn, language, etc., were removed as per your request
);

-- Create the authors table
CREATE TABLE authors (
    author_id SERIAL PRIMARY KEY,
    author_name VARCHAR(255) UNIQUE NOT NULL
);

-- Linking table for books and authors
CREATE TABLE book_authors (
    book_id INT NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    author_id INT NOT NULL REFERENCES authors(author_id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

-- Create the subjects table
CREATE TABLE subjects (
    subject_id SERIAL PRIMARY KEY,
    subject_name VARCHAR(100) UNIQUE NOT NULL
);

-- Linking table for books and subjects
CREATE TABLE book_subjects (
    book_id INT NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    subject_id INT NOT NULL REFERENCES subjects(subject_id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, subject_id)
);

-- Create the transactions table (representing loans)
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(member_id) ON DELETE CASCADE,
    book_id INT NOT NULL REFERENCES books(book_id) ON DELETE RESTRICT, -- Prevent book deletion if active loan
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    transaction_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' -- e.g., ACTIVE, RETURNED, OVERDUE
);

-- Create the fines table
CREATE TABLE fines (
    fine_id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(member_id) ON DELETE CASCADE,
    transaction_id INT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    fine_amount DECIMAL(10, 2) NOT NULL,
    fine_status VARCHAR(20) DEFAULT 'OUTSTANDING' NOT NULL, -- e.g., OUTSTANDING, PAID
    date_issued DATE NOT NULL DEFAULT CURRENT_DATE,
    date_paid DATE,
    UNIQUE(transaction_id)
);

-- Create the reservations table
CREATE TABLE reservations (
    reservation_id SERIAL PRIMARY KEY,
    book_id INT NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    member_id INT NOT NULL REFERENCES members(member_id) ON DELETE CASCADE,
    reservation_date TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) DEFAULT 'WAITING' NOT NULL -- e.g., WAITING, AVAILABLE, FULFILLED, EXPIRED
);

-- Optional: Grant privileges if you created a dedicated user
-- Make sure these are run by a superuser or the database owner if needed.
-- If 'library_manager' owns the database 'LibraryDB', these might not all be necessary
-- or could be more specific.
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO library_manager;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO library_manager;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO library_manager; -- More restrictive

COMMIT;

\echo "Database schema created successfully for LibraryDB."