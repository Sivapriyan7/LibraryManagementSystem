package Zoho.LibraryManagementSystem.Model.Enum;

public enum MembershipType {
    PUBLIC,
    STUDENT,
    FACULTY,
    SENIOR,
    YOUTH;

    /**
     * A helper method to check if a given string matches one of the enum constants.
     * This is useful for validating user input.
     * @param input The string to validate.
     * @return true if the string is a valid membership type, false otherwise.
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