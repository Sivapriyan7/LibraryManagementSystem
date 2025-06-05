package Zoho.LibraryManagementSystem.Model.Enum;


/**
 * Represents the different types of memberships a library member can have.
 * Each type might confer different privileges or rules within the system.
 * Using an enum ensures type safety and a predefined set of valid membership categories.
 */
public enum MembershipType {
    PUBLIC,
    STUDENT,
    FACULTY,
    SENIOR,
    YOUTH;

    /**
     * Checks if the provided string input corresponds to a valid membership type,
     * ignoring case and leading/trailing whitespace. This method is useful for
     * validating user input before attempting to convert it to a {@code MembershipType} enum constant.
     *
     * @param input The string to validate.
     * @return {@code true} if the input matches a defined membership type, {@code false} otherwise.
     */
    public static boolean isValid(String input) {
        if (input == null) {
            return false;
        }
        for (MembershipType type : values()) {
            if (type.name().equalsIgnoreCase(input.trim())) {
                return true;
            }
        }
        return false;
    }
}