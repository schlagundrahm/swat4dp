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

    @Test
    @DisplayName("Template with {index} expands to all matching indexed keys")
    void testIndexedTemplateExpansion(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Simulate what the tokenizer produces for 3 FSH instances
        createPropertiesFile(srcDir, "service.properties",
                "fsh.1.name=handler-a",
                "fsh.1.host=1.2.3.4",
                "fsh.1.port=8080",
                "fsh.2.name=handler-b",
                "fsh.2.host=5.6.7.8",
                "fsh.2.port=8443",
                "fsh.3.name=handler-c",
                "fsh.3.host=9.9.9.9",
                "fsh.3.port=443",
                "service.state=enabled");

        File templateFile = new File(tempDir, "template.properties");
        createPropertiesFile(tempDir, "template.properties",
                "# Service",
                "service.state=",
                "# FSH Instances",
                "fsh.{index}.name=",
                "fsh.{index}.host=",
                "fsh.{index}.port=");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setTemplateFile(templateFile);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());

        // All fsh.N.* lines should be present
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("fsh.1.name=")), "fsh.1.name should be in output");
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("fsh.2.name=")), "fsh.2.name should be in output");
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("fsh.3.name=")), "fsh.3.name should be in output");
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("fsh.1.host=")), "fsh.1.host should be in output");
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("fsh.2.host=")), "fsh.2.host should be in output");

        // No {index} literal should survive in the output
        assertTrue(lines.stream().noneMatch(l -> l.contains("{index}")), "No literal {index} should appear in output");

        // service.state should also be present (non-indexed key)
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("service.state=")), "service.state should be in output");

        // Index-first ordering: all fields for fsh.1 must come before any field for fsh.2
        List<String> fshLines = lines.stream()
                .filter(l -> l.startsWith("fsh."))
                .collect(java.util.stream.Collectors.toList());
        int last1 = -1, first2 = Integer.MAX_VALUE;
        for (int idx = 0; idx < fshLines.size(); idx++) {
            if (fshLines.get(idx).startsWith("fsh.1.")) last1 = idx;
            if (fshLines.get(idx).startsWith("fsh.2.") && idx < first2) first2 = idx;
        }
        assertTrue(last1 < first2, "All fsh.1.* fields must appear before any fsh.2.* field");
    }

    @Test
    @DisplayName("Template with {index} produces numeric ascending order")
    void testIndexedTemplateNumericOrder(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Properties with indexes 1, 3, 10 — must sort numerically, not lexicographically
        createPropertiesFile(srcDir, "service.properties",
                "item.10.value=ten",
                "item.3.value=three",
                "item.1.value=one");

        File templateFile = new File(tempDir, "template.properties");
        createPropertiesFile(tempDir, "template.properties",
                "item.{index}.value=");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setTemplateFile(templateFile);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());
        List<String> valueLines = lines.stream()
                .filter(l -> l.startsWith("item."))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(3, valueLines.size(), "All three items should be present");
        // 1 < 3 < 10 — numeric order, not lexicographic (which would give 1 < 10 < 3)
        assertTrue(valueLines.get(0).startsWith("item.1."), "First should be item.1");
        assertTrue(valueLines.get(1).startsWith("item.3."), "Second should be item.3");
        assertTrue(valueLines.get(2).startsWith("item.10."), "Third should be item.10");
    }

    @Test
    @DisplayName("Template with {index} and no matching keys produces no output for that line")
    void testIndexedTemplateNoMatchProducesNoOutput(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createPropertiesFile(srcDir, "service.properties",
                "service.state=enabled");

        File templateFile = new File(tempDir, "template.properties");
        createPropertiesFile(tempDir, "template.properties",
                "service.state=",
                "# FSH (may be absent)",
                "fsh.{index}.name=",
                "fsh.{index}.port=");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setTemplateFile(templateFile);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());

        // No fsh lines — the {index} placeholder produces nothing when there are no matches
        assertTrue(lines.stream().noneMatch(l -> l.startsWith("fsh.")), "No fsh lines should appear when none are present");
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("service.state=")), "Non-indexed key should still appear");
    }

    @Test
    @DisplayName("Template with {index} block is ordered index-first across multiple fields")
    void testIndexedTemplateIndexFirstOrdering(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Two FSH instances, three fields each
        createPropertiesFile(srcDir, "service.properties",
                "fsh.1.name=http-in",
                "fsh.1.host=0.0.0.0",
                "fsh.1.port=8080",
                "fsh.2.name=https-in",
                "fsh.2.host=0.0.0.0",
                "fsh.2.port=8443");

        File templateFile = new File(tempDir, "template.properties");
        createPropertiesFile(tempDir, "template.properties",
                "# FSH instances",
                "fsh.{index}.name=",
                "fsh.{index}.host=",
                "fsh.{index}.port=");

        MergeProperties task = new MergeProperties();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstFile(dstFile);
        task.setTemplateFile(templateFile);
        task.execute();

        List<String> lines = Files.readAllLines(dstFile.toPath());
        List<String> propLines = lines.stream()
                .filter(l -> l.startsWith("fsh."))
                .collect(java.util.stream.Collectors.toList());

        // Expect exactly 6 lines: fsh.1.name, fsh.1.host, fsh.1.port, fsh.2.name, fsh.2.host, fsh.2.port
        assertEquals(6, propLines.size(), "All 6 fsh.* properties should be present");
        assertTrue(propLines.get(0).startsWith("fsh.1.name="), "Line 1 should be fsh.1.name");
        assertTrue(propLines.get(1).startsWith("fsh.1.host="), "Line 2 should be fsh.1.host");
        assertTrue(propLines.get(2).startsWith("fsh.1.port="), "Line 3 should be fsh.1.port");
        assertTrue(propLines.get(3).startsWith("fsh.2.name="), "Line 4 should be fsh.2.name");
        assertTrue(propLines.get(4).startsWith("fsh.2.host="), "Line 5 should be fsh.2.host");
        assertTrue(propLines.get(5).startsWith("fsh.2.port="), "Line 6 should be fsh.2.port");
    }
}

// Made with Bob
