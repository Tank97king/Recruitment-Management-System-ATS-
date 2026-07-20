package com.example.ats.util;

/**
 * Utility class for common user name operations and string parsing.
 */
public final class NameUtils {

    private NameUtils() {
        // Prevent instantiation
    }

    /**
     * Splits a full name into a First Name and a Last Name.
     * Splitting is done by parsing from the last whitespace index.
     * Also trims values and limits both names to a maximum of 100 characters.
     *
     * @param fullName the full name string to parse
     * @return a ParsedName containing the separated first and last names
     */
    public static ParsedName parseFullName(String fullName) {
        if (fullName == null) {
            return new ParsedName("", "");
        }
        
        String trimmedName = fullName.trim();
        int lastSpaceIdx = trimmedName.lastIndexOf(' ');
        
        if (lastSpaceIdx > 0) {
            String firstName = trimmedName.substring(0, lastSpaceIdx).trim();
            String lastName = trimmedName.substring(lastSpaceIdx + 1).trim();
            
            return new ParsedName(
                firstName.length() > 100 ? firstName.substring(0, 100) : firstName,
                lastName.length() > 100 ? lastName.substring(0, 100) : lastName
            );
        } else {
            return new ParsedName(
                trimmedName.length() > 100 ? trimmedName.substring(0, 100) : trimmedName,
                ""
            );
        }
    }

    /**
     * Data carrier representation of parsed first and last names.
     */
    public static class ParsedName {
        private final String firstName;
        private final String lastName;

        public ParsedName(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
