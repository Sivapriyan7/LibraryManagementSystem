-- File: sample_data.sql
-- Ensure you are connected to the 'library_system_db' database before running this.
-- \c library_system_db

-- Sample Authors
INSERT INTO authors (author_name) VALUES ('F. Scott Fitzgerald'), ('Harper Lee'), ('George Orwell'), ('J.K. Rowling');

-- Sample Subjects
INSERT INTO subjects (subject_name) VALUES ('Classic'), ('Dystopian'), ('Fantasy'), ('Coming-of-Age');

-- Sample Books
-- For simplicity, not adding publisher, publication_date here, but you can.
INSERT INTO books (title, total_copies, copies_available) VALUES
('The Great Gatsby', 3, 3),
('To Kill a Mockingbird', 2, 2),
('1984', 5, 5),
('Harry Potter and the Sorcerer''s Stone', 10, 10);

-- Link Books to Authors (Example: The Great Gatsby by F. Scott Fitzgerald)
INSERT INTO book_authors (book_id, author_id) VALUES
((SELECT book_id FROM books WHERE title = 'The Great Gatsby'), (SELECT author_id FROM authors WHERE author_name = 'F. Scott Fitzgerald')),
((SELECT book_id FROM books WHERE title = 'To Kill a Mockingbird'), (SELECT author_id FROM authors WHERE author_name = 'Harper Lee')),
((SELECT book_id FROM books WHERE title = '1984'), (SELECT author_id FROM authors WHERE author_name = 'George Orwell')),
((SELECT book_id FROM books WHERE title = 'Harry Potter and the Sorcerer''s Stone'), (SELECT author_id FROM authors WHERE author_name = 'J.K. Rowling'));

-- Link Books to Subjects (Example: 1984 is Dystopian and Classic)
INSERT INTO book_subjects (book_id, subject_id) VALUES
((SELECT book_id FROM books WHERE title = 'The Great Gatsby'), (SELECT subject_id FROM subjects WHERE subject_name = 'Classic')),
((SELECT book_id FROM books WHERE title = 'To Kill a Mockingbird'), (SELECT subject_id FROM subjects WHERE subject_name = 'Classic')),
((SELECT book_id FROM books WHERE title = 'To Kill a Mockingbird'), (SELECT subject_id FROM subjects WHERE subject_name = 'Coming-of-Age')),
((SELECT book_id FROM books WHERE title = '1984'), (SELECT subject_id FROM subjects WHERE subject_name = 'Dystopian')),
((SELECT book_id FROM books WHERE title = '1984'), (SELECT subject_id FROM subjects WHERE subject_name = 'Classic')),
((SELECT book_id FROM books WHERE title = 'Harry Potter and the Sorcerer''s Stone'), (SELECT subject_id FROM subjects WHERE subject_name = 'Fantasy'));

\echo "Sample data inserted successfully."