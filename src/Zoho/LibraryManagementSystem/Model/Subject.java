package Zoho.LibraryManagementSystem.Model;

/**
 * Represents a subject or genre category for books in the library system.
 * This helps in classifying and searching for books.
 */
public class Subject {
    private int subjectId;
    private String subjectName;

    public Subject() {}

    /**
     * Constructs a Subject with a specified ID and name.
     * Used typically when retrieving subject data from the database.
     *
     * @param subjectId The unique identifier for the subject.
     * @param subjectName The name of the subject (e.g., "Science Fiction", "History").
     */
    public Subject(int subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }
    // Getters and Setters
    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        return subjectName;
    }
}