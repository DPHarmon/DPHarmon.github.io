package com.zybooks.cs360_warehouse_inventory_app;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  Unit tests for validators. Confirms valid input passes, malformed or oversized input is
 *  rejected with a message, and the previously crashing non-numeric quantity is now handled safely.
 *
 */
public class ValidatorsTest {

    // Username

    /** A well-formed username passes. */
    @Test
    public void username_valid_returnsNull() {
        assertNull(Validators.usernameError("dylan_h"));
    }

    /** Null, blank, too-short, too-long, and bad-character usernames are rejected. */
    @Test
    public void username_invalid_returnsMessage() {
        assertNotNull(Validators.usernameError(null));
        assertNotNull(Validators.usernameError("  "));
        assertNotNull(Validators.usernameError("ab"));
        assertNotNull(Validators.usernameError(repeat("x", 33)));
        assertNotNull(Validators.usernameError("bad name!"));
    }

    /** An injection-style username is rejected by the character rule */
    @Test
    public void username_injectionAttempt_isReject() {
        assertNotNull(Validators.usernameError("a' OR '1'='1"));
    }

    // Password
    /** A password within the length bounds passes. */
    @Test public void password_valid_returnsNull() {
        assertNull(Validators.passwordError("hunter2!!"));
    }

    /** Empty, null, too-short, and too-long passwords are rejected. */
    @Test
    public void password_invalid_returnsMessage() {
        assertNotNull(Validators.passwordError(null));
        assertNotNull(Validators.passwordError(""));
        assertNotNull(Validators.passwordError("short"));
        // Oversized input is rejected to bound PBKDF2 cost per attempt.
        assertNotNull(Validators.passwordError(repeat("p", 129)));
    }

    // Item Name
    /** A normal item name passes. */
    @Test
    public void itemName_valid_returnsNull() {
        assertNull(Validators.itemNameError("Widget A"));
    }

    /** Blank and over-length item names are rejected. */
    @Test
    public void itemName_invalid_returnsMessage() {
        assertNotNull(Validators.itemNameError("   "));
        assertNotNull(Validators.itemNameError(repeat("n", 65)));
    }

    // Quantity
    /** Zero and normal positive quantities pass. */
    @Test
    public void quantity_valid_returnsNull() {
        assertNull(Validators.quantityError("0"));
        assertNull(Validators.quantityError("42"));
    }

    /** The non-numeric input that used to crash the app is now rejected. */
    @Test
    public void quantity_nonNumeric_isRejectedNotCrashing() {
        assertNotNull(Validators.quantityError("abc"));
    }

    /** Blank, negative, decimal, overflowing, and over-max quantities are rejected. */
    @Test
    public void quantity_invalid_returnsMessage() {
        assertNotNull(Validators.quantityError(""));
        assertNotNull(Validators.quantityError("-5"));
        assertNotNull(Validators.quantityError("3.5"));
        assertNotNull(Validators.quantityError("99999999999999999999"));
        assertNotNull(Validators.quantityError("1000001"));
    }

    /** parseQuantity parses validated input and returns -1 on bad input. */
    @Test
    public void parseQuantity_parsesOrReturnsSentinel() {
        assertEquals(42, Validators.parseQuantity("42"));
        assertEquals(7, Validators.parseQuantity("  7  "));
        assertEquals(-1, Validators.parseQuantity("abc"));
    }

    // Helper: repeats a string n times (Java 8 compatible).
    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
