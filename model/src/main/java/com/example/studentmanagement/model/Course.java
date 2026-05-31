package com.example.studentmanagement.model;

/**
 * The 7 predefined courses a student can be registered for.
 * Stored in the DB by {@link Enum#name()} (see {@code @Enumerated(EnumType.STRING)} on the entity),
 * shown to users via {@link #getDisplayName()}.
 *
 * <p>These are placeholders &mdash; swap in real course names as needed.
 */
public enum Course {
    COMPUTER_SCIENCE("Computer Science"),
    SOFTWARE_ENGINEERING("Software Engineering"),
    INFORMATION_TECHNOLOGY("Information Technology"),
    DATA_SCIENCE("Data Science"),
    CYBERSECURITY("Cybersecurity"),
    BUSINESS_INFORMATICS("Business Informatics"),
    ELECTRICAL_ENGINEERING("Electrical Engineering");

    private final String displayName;

    Course(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
