package Zoho.LibraryManagementSystem.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Book {
    private int bookId;
    private String title;
    // Removed: isbn, language, cover_image_url, page_count, description
    private String publisher;
    private LocalDate publicationDate;
    private int totalCopies;
    private int copiesAvailable;
    private int timesBorrowed;
    private List<Author> authors;
    private List<Subject> subjects;

    // MODIFIED: Constructor for service layer creation (removed fields)
    public Book(String title, String publisher, LocalDate publicationDate, int totalCopies) {
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.totalCopies = totalCopies;
        this.copiesAvailable = totalCopies;
        this.timesBorrowed = 0; // Default for new book
    }

    // MODIFIED: Full constructor for repository layer creation (removed fields)
    public Book(int bookId, String title, String publisher, LocalDate publicationDate, int totalCopies, int copiesAvailable, int timesBorrowed) {
        this(title, publisher, publicationDate, totalCopies); // Call the simpler constructor
        this.bookId = bookId;
        this.copiesAvailable = copiesAvailable;
        this.timesBorrowed = timesBorrowed;
    }

    public boolean borrowCopy() {
        if (copiesAvailable > 0) {
            copiesAvailable--;
            timesBorrowed++;
            return true;
        }
        return false;
    }

    public void returnCopy() {
        if (copiesAvailable < totalCopies) {
            copiesAvailable++;
        }
    }

    // --- Getters and Setters ---
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public int getCopiesAvailable() { return copiesAvailable; }
    public void setCopiesAvailable(int copiesAvailable) { this.copiesAvailable = copiesAvailable; }
    public int getTimesBorrowed() { return timesBorrowed; }
    public void setTimesBorrowed(int timesBorrowed) { this.timesBorrowed = timesBorrowed; }
    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    @Override
    public String toString() {
        String authorString = (authors != null && !authors.isEmpty()) ? authors.stream().map(Author::getAuthorName).collect(Collectors.joining(", ")) : "N/A";
        String subjectString = (subjects != null && !subjects.isEmpty()) ? subjects.stream().map(Subject::getSubjectName).collect(Collectors.joining(", ")) : "N/A";
        String pubDateStr = (publicationDate != null) ? publicationDate.toString() : "N/A";

        return "ID: " + bookId + " | Title: '" + title + "'\n" +
                "  Authors: [" + authorString + "] | Subjects: [" + subjectString + "]\n" +
                "  Publisher: " + (publisher != null ? publisher : "N/A") + " | Publication Date: " + pubDateStr + "\n" +
                "  Available: " + copiesAvailable + "/" + totalCopies + " | Borrowed " + timesBorrowed + " time(s)";
    }
}