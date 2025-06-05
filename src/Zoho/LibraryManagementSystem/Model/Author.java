package Zoho.LibraryManagementSystem.Model;

/**
 * Represents an author of a book in the library system.
 * This class stores the unique identifier and name of an author.
 */
public class Author {
    private int authorId;
    private String authorName;

    public Author() {}


    /**
     * Constructs an Author with a specified ID and name.
     * Used typically when retrieving author data from the database.
     *
     * @param authorId The unique identifier for the author.
     * @param authorName The full name of the author.
     */
    public Author(int authorId, String authorName) {
        this.authorId = authorId;
        this.authorName = authorName;
    }

    // Getters and Setters
    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public String toString() {
        return authorName;
    }
}