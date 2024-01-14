package at.technikum.apps.mtcg.service;

import org.mindrot.jbcrypt.BCrypt;

public class HashingService {

    public HashingService() {
    }

    /**
     * Encrypts (hashes) a password using the BCrypt hashing algorithm.
     *
     * @param password The plaintext password to be hashed.
     * @return The hashed password.
     */
    public String encrypt(String password) {
        // Generate a salt using BCrypt's gensalt method.
        // (source: https://education.launchcode.org/java-web-development/chapters/auth/hashing-passwords.html#:~:text=You%20should%20use%20bcrypt.,as%20long%20as%20you%20like.)

        // This ensures each hashed password is unique, even if two users have the same password.
        String salt = BCrypt.gensalt();

        // Hash the plaintext password using the generated salt and return the hashed password.
        // BCrypt internally applies the salt and produces a hashed output.
        return BCrypt.hashpw(password, salt);
    }


    /**
     * Compares a plaintext password with a hashed password to verify if they match.
     *
     * @param plaintextPassword The plaintext password to verify.
     * @param hashedPassword    The hashed password against which the plaintext password is compared.
     * @return True if the plaintext password matches the hashed password, false otherwise.
     */
    public boolean compareHash(String plaintextPassword, String hashedPassword) {
        // Use BCrypt's checkpw method to compare the plaintext password with the hashed password.
        // The method returns true if the plaintext password, once hashed, matches the provided hashed password.
        return BCrypt.checkpw(plaintextPassword, hashedPassword);
    }

}
