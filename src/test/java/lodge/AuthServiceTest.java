package lodge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService auth = new AuthService();

    @Test
    void correctDetailsLogIn() {
        assertTrue(auth.login("admin", "admin123".toCharArray()));
    }

    @Test
    void usernameIsNotCaseSensitive() {
        assertTrue(auth.login("  ADMIN ", "admin123".toCharArray()));
    }

    @Test
    void passwordIsCaseSensitive() {
        assertFalse(auth.login("admin", "ADMIN123".toCharArray()));
    }

    @Test
    void wrongPasswordFails() {
        assertFalse(auth.login("admin", "wrong".toCharArray()));
    }

    @Test
    void unknownUserFails() {
        assertFalse(auth.login("nobody", "admin123".toCharArray()));
    }

    @Test
    void emptyPasswordFails() {
        assertFalse(auth.login("admin", new char[0]));
    }

    @Test
    void newAccountCanLogIn() {
        auth.register("mary", "secret99".toCharArray());
        assertTrue(auth.login("mary", "secret99".toCharArray()));
    }

    @Test
    void newAccountWorksAfterRegistering_butNotBefore() {
        assertFalse(auth.login("chanda", "mypass1".toCharArray()));
        auth.register("Chanda", "mypass1".toCharArray());
        assertTrue(auth.login("chanda", "mypass1".toCharArray()));
        assertFalse(auth.login("chanda", "mypass2".toCharArray()));
    }

    @Test
    void badUsernamesRejected() {
        for (String name : new String[]{"", "  ", "ab", "has space", "bad!name", "a".repeat(21)}) {
            assertThrows(IllegalArgumentException.class,
                    () -> auth.register(name, "secret99".toCharArray()), "name: [" + name + "]");
        }
    }

    @Test
    void adminAccountHasAdminRole() {
        assertEquals(Role.ADMIN, auth.authenticate("admin", "admin123".toCharArray()).orElseThrow());
    }

    @Test
    void receptionAccountIsGuest() {
        assertEquals(Role.GUEST, auth.authenticate("reception", "lodge123".toCharArray()).orElseThrow());
    }

    @Test
    void newAccountsAreAlwaysGuest() {
        auth.register("chanda", "mypass1".toCharArray());
        assertEquals(Role.GUEST, auth.authenticate("chanda", "mypass1".toCharArray()).orElseThrow());
    }

    @Test
    void authenticateIsEmptyForWrongDetails() {
        assertTrue(auth.authenticate("admin", "nope123".toCharArray()).isEmpty());
        assertTrue(auth.authenticate("ghost", "admin123".toCharArray()).isEmpty());
    }

    @Test
    void nobodyCanRegisterTheAdminUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("admin", "another1".toCharArray()));
    }

    @Test
    void duplicateUsernameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("Admin", "another1".toCharArray()));
    }

    @Test
    void shortPasswordRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("mary", "abc".toCharArray()));
    }
}
