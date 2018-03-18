/**
 * 
 */
package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;

import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author Pierce Shah
 *
 */
class CreateFilesConfigTest {

    /**
     * Test method for {@link ch.schlagundrahm.swat.datapower.tools.ant.taskdefs.CreateFilesConfig#execute()}.
     */
    @Test
    @Disabled
    void testExecute() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link ch.schlagundrahm.swat.datapower.tools.ant.taskdefs.CreateFilesConfig#calculateFileHash(java.io.File)}.
     * 
     * @throws IOException
     */
    @Test
    @DisplayName("Calculate File Hash")
    void testCalculateFileHash(TestInfo info) throws IOException {

        File file = new File("src/test/resources/plaintext.txt");
        System.out.println("TEST [" + info.getDisplayName() + "]: input file=" + file.getAbsolutePath());

        CreateFilesConfig task = new CreateFilesConfig();
        task.setProject(new Project());

        String result = task.calculateFileHash(file);

        System.out.println("result: " + result);
        assertEquals("49Z6Jn+hTWsrbob8IFT+3vMtQXM=", result, "menno!");
    }

    @Test
    void testFilehash() throws Exception {
        File file = new File("src/test/resources/plaintext.txt");

        assertNotNull("Input file is NULL!", file);

        byte[] data = Files.readAllBytes(file.toPath());
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashedBytes = digest.digest(data);

        assertEquals("49Z6Jn+hTWsrbob8IFT+3vMtQXM=", Base64.getEncoder().encodeToString(hashedBytes), "menno!");

    }

}
