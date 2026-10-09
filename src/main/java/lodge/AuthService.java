package lodge;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Handles user accounts and log in. Passwords are never stored as text:
 * each one is salted and hashed with PBKDF2 (built into Java).
 * Accounts live in memory only (a HashMap), like the saved bills.
 *
 * Anyone who creates an account through the app is GUEST. The only ADMIN
 * account is the one built in below.
 */
public class AuthService {

    private static final int ITERATIONS = 65_536;
    private static final int KEY_BITS = 256;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String USERNAME_RULE = "[a-z0-9_]{3,20}";

    private record Credential(byte[] salt, byte[] hash, Role role) {
    }

    private final Map<String, Credential> users = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    /** Starts with an admin and a guest account so the lab can be used straight away. */
    public AuthService() {
        addAccount("admin", "admin123".toCharArray(), Role.ADMIN);
        addAccount("reception", "lodge123".toCharArray(), Role.GUEST);
    }

    /** Creates a new GUEST account (this is what the Create account window uses). */
    public void register(String username, char[] password) {
        addAccount(username, password, Role.GUEST);
    }

    private void addAccount(String username, char[] password, Role role) {
        String name = normalise(username);
        if (!name.matches(USERNAME_RULE)) {
            throw new IllegalArgumentException(
                    "Username must be 3 to 20 letters, numbers or underscores (no spaces).");
        }
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (users.containsKey(name)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        users.put(name, new Credential(salt, hash(password, salt), role));
    }

    /** Returns the account's role if the username and password match, otherwise empty. */
    public Optional<Role> authenticate(String username, char[] password) {
        Credential stored = users.get(normalise(username));
        if (stored == null || password == null || password.length == 0) {
            return Optional.empty();
        }
        byte[] attempt = hash(password, stored.salt());
        return MessageDigest.isEqual(attempt, stored.hash())   // constant-time compare
                ? Optional.of(stored.role())
                : Optional.empty();
    }

    /** True only if the username exists and the password matches. */
    public boolean login(String username, char[] password) {
        return authenticate(username, password).isPresent();
    }

    private static String normalise(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing is not available.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
