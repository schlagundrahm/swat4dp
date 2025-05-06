/**
 * 
 */
package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author Pierce Shah
 *
 */
class Base64TaskTest {

    // @BeforeAll
    // public static void setUp() {
    // final BuildFileRule buildRule = new BuildFileRule();
    // // initialize Ant
    // buildRule.configureProject("src/test/resources/build.xml");
    // }

    static final String PLAIN_TEXT = "I like Apache ANT :-)";
    static final String B64_TEXT = "SSBsaWtlIEFwYWNoZSBBTlQgOi0p";
    static final String TEST_PROPERTY = "testProperty";
    static final String TEMP_FOLDER = "target" + File.separator + "test-tmp" + File.separator;

    @BeforeAll
    static void createTempDirectory() {
        try {
            Files.createDirectory(new File(TEMP_FOLDER).toPath());
        } catch (FileAlreadyExistsException e) {
            // do nothing
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link ch.schlagundrahm.swat.datapower.tools.ant.taskdefs.Base64Task#execute()}.
     */
    @Test
    @DisplayName("encode inpurt string")
    void testEncodeStringToProperty(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=string; output=property;");

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(false);
        task.setIn(PLAIN_TEXT);
        task.setProperty(TEST_PROPERTY);
        task.execute();
        String result = task.getProject().getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals(B64_TEXT, result, "menno!");
    }

    @Test
    @DisplayName("decode input string")
    void testDecodeStringToProperty(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=string; output=property;");

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(true);
        task.setIn(B64_TEXT);
        task.setProperty(TEST_PROPERTY);
        task.execute();
        String result = task.getProject().getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals(PLAIN_TEXT, result, "menno!");

        // fail("Not yet implemented");
    }

    @Test
    @DisplayName("encode input string and write to a text file")
    void testEncodeStringToFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=string; output=file;");
        String fileName = "encodedString.b64";
        File outfile = new File(TEMP_FOLDER + fileName);

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(false);
        task.setIn(PLAIN_TEXT);
        task.setOutfile(outfile);
        task.setOverride(true);
        task.execute();
        System.out.println("writing result to file : " + outfile);
        List<String> content = new ArrayList<>();
        try {
            content = Files.readAllLines(outfile.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(content);
        assertEquals(B64_TEXT, content.get(0), "menno!");
    }

    @Test
    @DisplayName("read text file and write encoded string to file")
    void testEncodeFileToFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=text file; output=file;");
        File infile = new File("src/test/resources/plaintext.txt");
        File outfile = new File(TEMP_FOLDER + "encodedFile.b64");

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(false);
        task.setInfile(infile);
        task.setOutfile(outfile);
        task.setOverride(true);
        task.execute();
        System.out.println("writing result to file : " + outfile);
        List<String> content = new ArrayList<>();
        try {
            content = Files.readAllLines(outfile.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(content);
        assertEquals(B64_TEXT, content.get(0), "menno!");

        // fail("Not yet implemented");
    }

    @Test
    @DisplayName("read encoded zip file and write decoded string to file (zip)")
    void testDecodeZipFileToFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=encoded zip file; output=file;");
        File infile = new File("src/test/resources/encodedzip-singleline.b64");
        File outfile = new File(TEMP_FOLDER + "decodedzip-singleline.zip");

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(true);
        task.setInfile(infile);
        task.setOutfile(outfile);
        task.setOverride(true);
        task.execute();
        System.out.println("writing result to file : " + outfile);
        byte[] content = new byte[0]; 
        try {
            content = Files.readAllBytes(outfile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(80, content[0], "menno!");

        // fail("Not yet implemented");
    }

    @Test
    @DisplayName("read zip file and write encoded string to property")
    void testEncodeZipFileToProperty(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=zip file; output=property;");
        File infile = new File("src/test/resources/plaintext.zip");

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(false);
        task.setInfile(infile);
        task.setProperty(TEST_PROPERTY);
        task.setBinary(true);
        task.execute();

        String result = task.getProject().getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("UEsDBAoAAAAAAGiMlUshDNRMFQAAABUAAAANAAAAcGxhaW50ZXh0LnR4dEkgbGlrZSBBcGFjaGUg\r\n"
                + "QU5UIDotKVBLAQI/AAoAAAAAAGiMlUshDNRMFQAAABUAAAANACQAAAAAAAAAIAAAAAAAAABwbGFp\r\n"
                + "bnRleHQudHh0CgAgAAAAAAABABgAgiJorXl60wG+7sqfeXrTAb7uyp95etMBUEsFBgAAAAABAAEA\r\n"
                + "XwAAAEAAAAAAAA==", result, "menno!");

    }

    @Test
    @DisplayName("read zip file and write encoded string to file")
    void testEncodeZipFileToFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]: input=zip file; output= b64 file;");
        File infile = new File("src/test/resources/plaintext.zip");
        File outfile = new File(TEMP_FOLDER + "encodedZIP.b64");

        Base64Task task = new Base64Task();
        task.setProject(new Project());
        task.setDecode(false);
        task.setInfile(infile);
        task.setOutfile(outfile);
        task.setOverride(true);
        // task.setBinary(true);
        task.execute();
        System.out.println("writing result to file : " + outfile);
        List<String> content = new ArrayList<>();
        try {
            content = Files.readAllLines(outfile.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("result: " + content);
        assertEquals("UEsDBAoAAAAAAGiMlUshDNRMFQAAABUAAAANAAAAcGxhaW50ZXh0LnR4dEkgbGlrZSBBcGFjaGUg", content.get(0),
                "menno!");
        assertEquals("XwAAAEAAAAAAAA==", content.get(3), "menno!");
    }

}
