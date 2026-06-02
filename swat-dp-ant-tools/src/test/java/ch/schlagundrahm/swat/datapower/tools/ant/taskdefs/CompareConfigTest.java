package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for CompareConfig Ant task.
 * 
 * Note: CompareConfig is a complex class that compares DataPower configuration archives.
 * This test provides basic coverage of the main functionality. Full testing would require
 * creating realistic DataPower export archives with proper structure.
 * 
 * @author Pierce Shah
 */
class CompareConfigTest {

    @TempDir
    File tempDir;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Give Windows time to release file handles
        // This is necessary because CompareConfig uses TrueZip which may keep files open
        System.gc();
        Thread.sleep(100);
    }

    /**
     * Create a simple ZIP file for testing.
     */
    private File createTestZip(String filename, String... entries) throws IOException {
        File zipFile = new File(tempDir, filename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String entry : entries) {
                ZipEntry ze = new ZipEntry(entry);
                zos.putNextEntry(ze);
                
                // Add some content based on entry name
                if (entry.endsWith(".xml")) {
                    String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<datapower-configuration version=\"3\">\n" +
                            "  <configuration domain=\"test\">\n" +
                            "    <TestObject name=\"test\"/>\n" +
                            "  </configuration>\n" +
                            "</datapower-configuration>";
                    zos.write(xmlContent.getBytes());
                } else {
                    zos.write(("Content of " + entry).getBytes());
                }
                
                zos.closeEntry();
            }
        }
        return zipFile;
    }

    /**
     * Create a DataPower-like export ZIP with export.xml.
     */
    private File createDataPowerExport(String filename, String domain, String objectName) throws IOException {
        File zipFile = new File(tempDir, filename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Add export.xml
            ZipEntry exportXml = new ZipEntry("export.xml");
            zos.putNextEntry(exportXml);
            
            String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<datapower-configuration version=\"3\">\n" +
                    "  <configuration domain=\"" + domain + "\">\n" +
                    "    <TestObject name=\"" + objectName + "\">\n" +
                    "      <Property>value</Property>\n" +
                    "    </TestObject>\n" +
                    "  </configuration>\n" +
                    "</datapower-configuration>";
            zos.write(xmlContent.getBytes());
            zos.closeEntry();
            
            // Add some files
            ZipEntry file1 = new ZipEntry("local/test.txt");
            zos.putNextEntry(file1);
            zos.write("Test content".getBytes());
            zos.closeEntry();
        }
        return zipFile;
    }

    @Test
    @DisplayName("Compare two identical configuration archives")
    void testCompareIdenticalArchives(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.execute();

        System.out.println("Comparison of identical archives completed");
    }

    @Test
    @DisplayName("Compare two different configuration archives")
    void testCompareDifferentArchives(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "domain1", "object1");
        File config2 = createDataPowerExport("config2.zip", "domain2", "object2");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.execute();

        System.out.println("Comparison of different archives completed");
    }

    @Test
    @DisplayName("Compare with includeIntrinsic flag")
    void testCompareWithIncludeIntrinsic(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.setIncludeIntrinsic(true);
        task.execute();

        System.out.println("Comparison with includeIntrinsic completed");
    }

    @Test
    @DisplayName("Compare with nodeTypeDiff flag")
    void testCompareWithNodeTypeDiff(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.setNodeTypeDiff(true);
        task.execute();

        System.out.println("Comparison with nodeTypeDiff completed");
    }

    @Test
    @DisplayName("Compare with nodeValueDiff flag")
    void testCompareWithNodeValueDiff(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.setNodeValueDiff(true);
        task.execute();

        System.out.println("Comparison with nodeValueDiff completed");
    }

    @Test
    @DisplayName("Compare with includeExportDetails flag")
    void testCompareWithIncludeExportDetails(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.setIncludeExportDetails(true);
        task.execute();

        System.out.println("Comparison with includeExportDetails completed");
    }

    @Test
    @DisplayName("Compare with includeFiles flag")
    void testCompareWithIncludeFiles(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.setIncludeFiles(true);
        task.execute();

        System.out.println("Comparison with includeFiles completed");
    }

    @Test
    @DisplayName("Fail when cfgFile1 is missing")
    void testMissingCfgFile1(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile2(config2);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Fail when cfgFile2 is missing")
    void testMissingCfgFile2(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Fail when cfgFile1 does not exist")
    void testNonExistentCfgFile1(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File nonExistent = new File(tempDir, "nonexistent1.zip");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(nonExistent);
        task.setCfgFile2(config2);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Fail when cfgFile2 does not exist")
    void testNonExistentCfgFile2(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File nonExistent = new File(tempDir, "nonexistent2.zip");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(nonExistent);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Compare archives with different file counts")
    void testDifferentFileCounts(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createTestZip("config1.zip", "export.xml", "file1.txt");
        File config2 = createTestZip("config2.zip", "export.xml", "file1.txt", "file2.txt");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.execute();

        System.out.println("Comparison of archives with different file counts completed");
    }

    @Test
    @DisplayName("Compare with all flags enabled")
    void testCompareWithAllFlags(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File config1 = createDataPowerExport("config1.zip", "testDomain", "testObject");
        File config2 = createDataPowerExport("config2.zip", "testDomain", "testObject");

        CompareConfig task = new CompareConfig();
        task.setProject(project);
        task.setCfgFile1(config1);
        task.setCfgFile2(config2);
        task.setIncludeIntrinsic(true);
        task.setNodeTypeDiff(true);
        task.setNodeValueDiff(true);
        task.setIncludeExportDetails(true);
        task.setIncludeFiles(true);
        task.execute();

        System.out.println("Comparison with all flags enabled completed");
    }
}

// Made with Bob
