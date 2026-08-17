package com.zybooks.cs360_warehouse_inventory_app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
    *  Password Hashing for the Warehouse Inventory App
    *
    *   Enhancement : Converted to PBKDF2-HMAC-SHA256;
    *
    *   Stored format: pbkdf2sha256$<iterations>$<base64 salt>$<base64 hash>
                        algorithm - iterationCount - rand 16byte salt - 32 byte PBKDF2 Output
    *   Date: 7/24/2026
    *   Author: Dylan P Harmon
    * */
public final class PasswordUtils {

    // Security Parameters
    private static final String PBKDF2_ALGORITHM    = "PBKDF2WithHmacSHA256";
    private static final int    ITERATIONS          = 120_000;
    private static final int    SALT_LENGTH_BYTES   = 16;
    private static final int    KEY_LENGTH_BITS     = 256;

    private static final String FORMAT_ID           = "pbkdf2sha256";
    private static final String SEP                 = "$";

    private static final SecureRandom   SECURE_RANDOM = new SecureRandom();

    /** Prevents Instantiation - this is a static utility class */
    private PasswordUtils() {}

    /**
    *   Hashes a new password with a fresh random salt and current work factor
    *
    *   @param password the plaintext password to hash
    *   @return a self-describing record string safe to store in users table
    * */
    public static String hash(String password) {
        // generate new random salt
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        // Derive the hash from password + salt with current work factor
        byte[] derived = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        // Join tag, iterations, salt, and hash into one "$" delimited record.
        return FORMAT_ID + SEP + ITERATIONS + SEP
                + Base64.getEncoder().encodeToString(salt) + SEP
                + Base64.getEncoder().encodeToString(derived);
    }

    /**
     *  Verifies a password against a stored record. Fails closed:
     *  any null, malformed, or unrecognized record is rejected
     *  rather than trusted.
     *
     * @param password the plaintext password entered at login
     * @param stored the stored record
     * @return true if the password matched the stored record.
     *
     * */
    public static boolean verify(String password, String stored) {
        // check user created and has password stored
        if (stored == null) return false;

        // Legacy accounts store unsalted SHA-256; Compare in constant time.
        if (isLegacySha256(stored)) {
            return MessageDigest.isEqual(
                    legacySha256(password).getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8)
            );
        }

        // Split the record into its four pats; reject improper format.
        String[] storedParts = stored.split("\\$");
        // Check stored password has correct format
        if (storedParts.length != 4 || !FORMAT_ID.equals(storedParts[0])) return false;

        int iterations;
        byte[] salt;
        byte[] expected;
        // Decode the stored iterations, salt, and hash; Malformed text fails, no crash.
        try{
            iterations  = Integer.parseInt(storedParts[1]);
            salt        = Base64.getDecoder().decode(storedParts[2]);
            expected    = Base64.getDecoder().decode(storedParts[3]);
        }   catch (IllegalArgumentException e) {
            return false;
        }

        // Re-derive with stored salt/iterations and compare in constant time.
        byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
        return MessageDigest.isEqual(expected, actual);
    }

    /**
     *  Reports whether a stored record is weaker than the current implemented security and
     *  should be transparently re-hashed on the user's next successful login
     *
     * @param stored the stored record to inspect
     * @return true if the record is legacy, malformed, or below current work factor, false
     * if it already meets current security.
     * */
    public static boolean needsRehash(String stored) {
        /*  check for missing record, or Legacy Password Security */
        if (stored == null || isLegacySha256(stored)) return true;

        /* Check for invalid stored format */
        String[] storedParts = stored.split("\\$");
        if (storedParts.length !=4 || !FORMAT_ID.equals(storedParts[0])) return  true;

        /*  Compare the stored iteration count against the current
        *   Iterations constant. If stored with fewer iterations than
        *   the current implementation, the password needs re-hashing */
        try {
            return Integer.parseInt(storedParts[1]) < ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     *  Runs the PBKDF2-HMAC-SHA256 key derivation shared by hash() and verify()
     *
     * @param password      the password characters to stretch
     * @param salt          the salt to derive against
     * @param iterations    the work factor
     * @param keyBits       the desired derived-key length in bits
     * @return the derived hash bytes
     * */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) {
        // Bundle the inputs into the spec PBKDF2 expects
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        try {
            // get algorithm implementation, and run the derivation
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Missing algorithm or invalid spec is a config error
            throw new RuntimeException("PBKDF2 hashing failed", e);
        } finally {
            // Clear the password from the spec's internal array
            spec.clearPassword();
        }
    }


    /**
     * Detects the old storage format
     *
     * @param stored the stored value to test
     * @return true if the value looks like a legacy SHA-256 hex
     * */
    private static boolean isLegacySha256(String stored) {
        return stored.length() == 64 && stored.matches("[0-9a-fA-F]{64}");
    }

    /**
     * Reproduces the original unsalted SHA-256 hex so a legacy account
     * can still be verified (and then their password will be rehashed)
     *
     * @param password the plaintext password to has
     * @return the lowercase hex SHA-256 of the password
     */
    private static String legacySha256(String password) {
        try {
            // Hash the raw password bytes with SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes =  md.digest(password.getBytes(StandardCharsets.UTF_8));
            // Convert each byte to hex
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not retrieved", e);
        }
    }
}