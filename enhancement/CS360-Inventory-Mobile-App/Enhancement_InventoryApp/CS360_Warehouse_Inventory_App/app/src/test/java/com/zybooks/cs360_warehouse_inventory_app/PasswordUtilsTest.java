package com.zybooks.cs360_warehouse_inventory_app;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

/* Enhancement - Rewritten Unit tests for newly implemented PBKDF2 and Legacy Lazy Migration
*
*   Date: 7/24/2026
*   Author: Dylan P Harmon
* */

/**
 * Unit tests for PasswordUtils
 *
 * Previous unit tests fail with Salted PBKDF2 with purposeful intent.
 * New tests for properties of import include: unique salts, correct verification,
 * failing closed on bad input, and legacy migration.
 */
public class PasswordUtilsTest {

    /** A hashed password is never null */
    @Test
    public void hash_returnsNonNullResult() {
        assertNotNull(PasswordUtils.hash("password987"));
    }

    /** hash() produces the 4 pieces, self-describing record format */
    @Test
    public void hash_producesSelfDescribingFormat() {
        String record = PasswordUtils.hash("Bond007");
        String[] storedParts = record.split("\\$");
        assertEquals(4, storedParts.length);
        assertEquals("pbkdf2sha256", storedParts[0]);
    }

    /** A random per-user salt makes identical passwords hash differently */
    @Test
    public void hash_samePass_producesDifferentHashes() {
        String hash1 = PasswordUtils.hash("Bond007");
        String hash2 = PasswordUtils.hash("Bond007");
        assertNotEquals(hash1, hash2);
    }

    /** verify() rejects a wrong password. */
    @Test
    public void verify_rejectsWrongPass() {
        String record = PasswordUtils.hash("Bond007");
        assertFalse(PasswordUtils.verify("MoneyPenny01", record));
    }

    /** verify() fails closed on null and malformed records. */
    @Test
    public void verify_failsClosedOnNullAndMalformedRecords() {
        assertFalse(PasswordUtils.verify("Bond007", null));
        assertFalse(PasswordUtils.verify("Bond007", "not-a-valid-password"));
        assertFalse(PasswordUtils.verify("Bond007", "pbkdf2sha256$abc$wrong$data"));
    }

    /** A legacy account still authenticates via the legacy path */
    @Test
    public void verify_authenticatesLegacySha256Account() {
        String legacy = legacySha256Hex("Bond007");
        assertTrue(PasswordUtils.verify("Bond007", legacy));
        assertFalse(PasswordUtils.verify("notBond", legacy));
    }

    /** Legacy records need re-hashing: fresh PBKDF2 records do not.   */
    @Test
    public void needsRehash_trueForLegacy_falseForFreshHash() {
        String legacy = legacySha256Hex("Bond007");
        assertTrue(PasswordUtils.needsRehash(legacy));
        assertFalse(PasswordUtils.needsRehash(PasswordUtils.hash("Bond007")));
    }

    /**
     * Reproduces the original unsalted SHA-256 encoding
     * test only
     *
     * @param password the plaintext password to hash
     * @return the lowercase hex SHA-256 of the password
     */
    private static String legacySha256Hex(String password) {
        try {
            java.security.MessageDigest md =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes ) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
