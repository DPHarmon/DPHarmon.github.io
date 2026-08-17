package com.zybooks.cs360_warehouse_inventory_app;

/**
 *  Centralized input validation for user and item text fields
 *
 *  Enhancement: the Room DAOs already parameterize every query.
 *  This layer is additional defense in depth. It rejects malformed, oversized, or
 *  nonsensical input before it reaches persistence, caps length to bound the cost of the PBKDF2
 *  hash, and prevents crashes such as parsing a non-numeric quantity.
 *
 *  Each @code *Error method returns @code null when the input is valid, or when the input is
 *  valid, or a short human-readable message describing the problem when it is not.
 *  This pairs naturally with a Toast or a field error
 */
public class Validators {

    // Username: 3-32 chars, letters/digits/dot/underscore/hyphen only/at sign only.
    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 32;
    private static final String USERNAME_PATTERN = "[A-Za-z0-9._-]+";

    // Password length bounds only
    // composition rules: the maximum also caps PBKDF2 work per attempt
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 128;

    // Item name: 1-64 chars.
    public static final int ITEM_NAME_MAX = 64;

    // Quantity: 0 ... 1,000,000.
    public static final int QUANTITY_MAX = 1_000_000;

    private Validators() { }

    /**
     *  Validates a username.
     *
     * @param username the raw username input
     * @return null if valid, otherwise a message describing the problem
     */
    public static String usernameError(String username) {
        // Reject null/blank
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }

        String u = username.trim();
        // ENFORCE - length bounds.
        if (u.length() < USERNAME_MIN || u.length() > USERNAME_MAX) {
            return "Useername must be " + USERNAME_MIN + "-" + USERNAME_MAX + " characters";
        }
        //restrict to a safe character set.
        if (!u.matches(USERNAME_PATTERN)) {
            return "Username may only use letters, digits, and . _ - @";
        }
        return  null;
    }

    /**
     *  Validates a password by length only.
     *
     * @param password the raw password input
     * @return null if valid, otherwise a message describing the problem
     */
    public static String passwordError(String password) {
        // reject null/empty
        if (password == null || password.isEmpty()) {
            return "Password Required";
        }
        if (password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            return "Password must be " + PASSWORD_MIN + "-" + PASSWORD_MAX + "characters";
        }

        return null;
    }

    /**
     *  Validates an inventory item name
     *
     * @param name the raw item name
     * @return null if valid, otherwise a message describing the problem
     */
    public static String itemNameError(String name) {
        // Reject null/blank
        if (name == null || name.trim().isEmpty()) {
            return "Item name requires";
        }
        // Enforce a max length
        if (name.trim().length() > ITEM_NAME_MAX) {
            return "Item name must be " + ITEM_NAME_MAX + " characters or fewer";
        }
        return null;
    }

    /**
     *  Validates a quantity string as a non-negative whole number in range.
     *
     * @param raw raw the raw quantity input
     * @return null if valid, otherwise a message describing the problem
     */
    public static String quantityError(String raw) {
        // Reject null/blank
        if (raw == null || raw.trim().isEmpty()) {
            return "Quantity is required";
        }

        String q = raw.trim();
        // Digits only
        if (!q.matches("\\d+")) {
            return "Quantity must be a whole number (0 or more)";
        }

        int value;
        // A digit string can still overflow int; parse to catch.
        try {
            value = Integer.parseInt(q);
        } catch (NumberFormatException e) {
            return "Quantity is too large (max " + QUANTITY_MAX + ")";
        }
        if (value > QUANTITY_MAX) {
            return "Quantity is too large (max " + QUANTITY_MAX + ")";
        }
        return null;
    }

    /**
     *  Safely parses a validated quantity string.
     *
     * @param raw the quantity input; call only after quantityError returns null
     * @return the parsed non-negative quantity, or -1 if the input is invalid
     */
    public static int parseQuantity(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (RuntimeException e) {
            return -1;
        }
    }
}
