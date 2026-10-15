package fh.swen.paperless.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hash_doesNotReturnPlainPassword() {
        String hash = passwordHasher.hash("geheim1234");

        assertNotNull(hash);
        assertNotEquals("geheim1234", hash);
    }

    @Test
    void verify_returnsTrue_forCorrectPassword() {
        String hash = passwordHasher.hash("geheim1234");

        assertTrue(passwordHasher.verify("geheim1234", hash));
    }

    @Test
    void verify_returnsFalse_forWrongPassword() {
        String hash = passwordHasher.hash("geheim1234");

        assertFalse(passwordHasher.verify("falsch999", hash));
    }

    @Test
    void hash_returnsDifferentHashes_forSamePassword() {
        // BCrypt verwendet ein zufälliges Salt -> jedes Mal ein anderer Hash
        String first = passwordHasher.hash("geheim1234");
        String second = passwordHasher.hash("geheim1234");

        assertNotEquals(first, second);
        assertTrue(passwordHasher.verify("geheim1234", first));
        assertTrue(passwordHasher.verify("geheim1234", second));
    }
}