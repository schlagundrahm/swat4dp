package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for MergeProperties Ant task.
 * 
 * @author Pierce Shah
 */
class MergePropertiesTest {

    @TempDir
    File tempDir;

    private Project project;
    private File srcDir;
    private File dstFile;

    @BeforeEach
    void setUp() throws IOException {
        project = new Project();
        srcDir = new File(tempDir, "src");
        srcDir.mkdirs();
        dstFile = new File(tempDir, "merged.properties");
    }

    @AfterEach
    void tearDown() {
        // Cleanup is handled by @TempDir
    }

    private void createPropertiesFile(File dir, String filename, String... lines) throws IOException {
        File file = new File(dir, filename);
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    @Test
    @DisplayName("Merge multiple properties files")
    void testMergeMultipleFiles(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties",
                "key1=value1",
                "key2=value2");
        createPropertiesFile(srcDir, "file2.properties",
                "key3=value3",
                "key4=value4");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.execute();

        assertTrue(dstFile.exists());
        Properties result = new Properties();
        result.load(Files.newInputStream(dstFile.toPath()));

        assertEquals("value1", result.getProperty("key1"));
        assertEquals("value2", result.getProperty("key2"));
        assertEquals("value3", result.getProperty("key3"));
        assertEquals("value4", result.getProperty("key4"));
    }

    @Test
    @DisplayName("Last file wins for duplicate keys")
    void testDuplicateKeys(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties",
                "key1=value1",
                "duplicate=first");
        createPropertiesFile(srcDir, "file2.properties",
                "key2=value2",
                "duplicate=second");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.execute();

        Properties result = new Properties();
        result.load(Files.newInputStream(dstFile.toPath()));

        assertEquals("second", result.getProperty("duplicate"));
    }

    @Test
    @DisplayName("Sort properties alphabetically")
    void testSortProperties(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties",
                "zebra=z",
                "apple=a",
                "banana=b");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setSort(true);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());
        
        // Find property lines (skip header comments)
        int appleIndex = -1, bananaIndex = -1, zebraIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("apple=")) appleIndex = i;
            if (line.startsWith("banana=")) bananaIndex = i;
            if (line.startsWith("zebra=")) zebraIndex = i;
        }

        assertTrue(appleIndex < bananaIndex && bananaIndex < zebraIndex,
                "Properties should be sorted alphabetically");
    }

    @Test
    @DisplayName("Order properties by template")
    void testOrderByTemplate(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties",
                "key1=value1",
                "key2=value2",
                "key3=value3");

        File templateFile = new File(tempDir, "template.properties");
        createPropertiesFile(tempDir, "template.properties",
                "# Section 1",
                "key3=",
                "key1=",
                "# Section 2",
                "key2=");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setTemplateFile(templateFile);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());
        
        // Find property lines
        int key1Index = -1, key2Index = -1, key3Index = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("key1=")) key1Index = i;
            if (line.startsWith("key2=")) key2Index = i;
            if (line.startsWith("key3=")) key3Index = i;
        }

        assertTrue(key3Index < key1Index && key1Index < key2Index,
                "Properties should follow template order");
    }

    @Test
    @DisplayName("Exclude properties by pattern")
    void testExcludePattern(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties",
                "keep.this=value1",
                "exclude.this=value2",
                "exclude.that=value3",
                "keep.that=value4");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setExcludePattern("exclude\\..*");
        task.execute();

        Properties result = new Properties();
        result.load(Files.newInputStream(dstFile.toPath()));

        assertEquals(2, result.size());
        assertEquals("value1", result.getProperty("keep.this"));
        assertEquals("value4", result.getProperty("keep.that"));
        assertEquals(null, result.getProperty("exclude.this"));
        assertEquals(null, result.getProperty("exclude.that"));
    }

    @Test
    @DisplayName("Handle empty source directory")
    void testEmptySourceDirectory(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.execute();

        // Should complete without error but not create output file
        assertTrue(!dstFile.exists() || dstFile.length() == 0);
    }

    @Test
    @DisplayName("Fail when srcDir is missing")
    void testMissingSrcDir(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setDstFile(dstFile);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("srcDir"));
    }

    @Test
    @DisplayName("Fail when dstFile is missing")
    void testMissingDstFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("dstFile"));
    }

    @Test
    @DisplayName("Fail when srcDir does not exist")
    void testNonExistentSrcDir(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File nonExistent = new File(tempDir, "nonexistent");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(nonExistent);
        task.setDstFile(dstFile);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("directory"));
    }

    @Test
    @DisplayName("Create parent directories for output file")
    void testCreateParentDirectories(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties", "key1=value1");

        File nestedDstFile = new File(tempDir, "nested/dir/output.properties");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(nestedDstFile);
        task.execute();

        assertTrue(nestedDstFile.exists());
        assertTrue(nestedDstFile.getParentFile().exists());
    }

    @Test
    @DisplayName("Preserve comments from template")
    void testPreserveTemplateComments(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "file1.properties",
                "key1=value1",
                "key2=value2");

        File templateFile = new File(tempDir, "template.properties");
        createPropertiesFile(tempDir, "template.properties",
                "# Important Section",
                "key1=",
                "",
                "# Another Section",
                "key2=");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setTemplateFile(templateFile);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());
        boolean hasImportantSection = lines.stream().anyMatch(l -> l.contains("Important Section"));
        boolean hasAnotherSection = lines.stream().anyMatch(l -> l.contains("Another Section"));

        assertTrue(hasImportantSection, "Should preserve 'Important Section' comment");
        assertTrue(hasAnotherSection, "Should preserve 'Another Section' comment");
    }
}

// Made with Bob
