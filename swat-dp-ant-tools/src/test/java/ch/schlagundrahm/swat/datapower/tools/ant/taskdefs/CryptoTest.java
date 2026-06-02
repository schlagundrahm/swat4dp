package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.NoSuchAlgorithmException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CryptoTest {

    @Test
    @DisplayName("constructor initializes crypto task with strong secure random")
    void testConstructorInitializesTask() {
        Crypto crypto = assertDoesNotThrow(Crypto::new);
        assertNotNull(crypto);
    }

    @Test
    @DisplayName("execute fails when keyfile is missing")
    void testExecuteFailsWhenKeyfileMissing() throws NoSuchAlgorithmException {
        Crypto crypto = new Crypto();
        crypto.setProject(new Project());
        crypto.setMode("encrypt");
        crypto.setInput("plain-text");
        crypto.setProperty("encrypted.value");

        BuildException exception = assertThrows(BuildException.class, crypto::execute);
        assertEquals("You have to provide the 'keyfile' attribute.", exception.getMessage());
    }
}

// Made with Bob
