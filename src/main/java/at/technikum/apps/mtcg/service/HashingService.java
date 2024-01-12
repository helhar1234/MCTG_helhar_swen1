package at.technikum.apps.mtcg.service;

import org.mindrot.jbcrypt.BCrypt;

public class HashingService {

    public HashingService() {
    }

    public String encrypt(String password) {
        // using bcrypt (source: https://education.launchcode.org/java-web-development/chapters/auth/hashing-passwords.html#:~:text=You%20should%20use%20bcrypt.,as%20long%20as%20you%20like.)
        // Generieren eines Salt und Durchführen des Hashing-Prozesses
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    public boolean compareHash(String plaintextPassword, String hashedPassword) {
        return BCrypt.checkpw(plaintextPassword, hashedPassword);
    }
}
