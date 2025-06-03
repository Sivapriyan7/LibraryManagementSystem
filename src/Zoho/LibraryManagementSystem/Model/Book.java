package Zoho.LibraryManagementSystem.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Book {
    private int bookId;
    private String title;
    private String isbn;
    private String publisher;
    private LocalDate publicationDate;
    private int pageCount;
    private String description;
    private int totalCopies;
    private int copiesAvailable;
    private int timesBorrowed;
    private List<Author> authors;
    private List<Subject> subjects;

    public Book(String title, String isbn, String publisher, LocalDate publicationDate, int pageCount, String description, int totalCopies) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.pageCount = pageCount;
        this.description = description;
        this.totalCopies = totalCopies;
        this.copiesAvailable = totalCopies;
        this.timesBorrowed = 0;
    }

    public Book(int bookId, String title, String isbn, String publisher, LocalDate publicationDate, int pageCount, String description, int totalCopies, int copiesAvailable, int timesBorrowed) {
        this(title, isbn, publisher, publicationDate, pageCount, description, totalCopies);
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

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
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
        String authorString = (authors != null) ? authors.stream().map(Author::getAuthorName).collect(Collectors.joining(", ")) : "N/A";
        String subjectString = (subjects != null) ? subjects.stream().map(Subject::getSubjectName).collect(Collectors.joining(", ")) : "N/A";
        return "ID: " + bookId + " | Title: '" + title + "'\n  Authors: [" + authorString + "] | Subjects: [" + subjectString + "]\n  ISBN: " + isbn + " | Publisher: " + publisher + " | Available: " + copiesAvailable + "/" + totalCopies;
    }
}