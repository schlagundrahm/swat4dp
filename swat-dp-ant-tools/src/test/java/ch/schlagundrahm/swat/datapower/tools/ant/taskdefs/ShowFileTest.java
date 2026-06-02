package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for ShowFile Ant task.
 * 
 * @author Pierce Shah
 */
class ShowFileTest {

    @TempDir
    File tempDir;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
    }

    private File createTestFile(String filename, String... lines) throws IOException {
        File file = new File(tempDir, filename);
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
        return file;
    }

    @Test
    @DisplayName("Search for pattern in single file")
    void testSearchSingleFile(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt",
                "This is line 1",
                "This is line 2 with pattern",
                "This is line 3");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("pattern");
        task.execute();

        // Test passes if no exception is thrown
        System.out.println("Search completed successfully");
    }

    @Test
    @DisplayName("Search for pattern by line")
    void testSearchByLine(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt",
                "First line",
                "Second line with match",
                "Third line");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("match");
        task.setByLine("true");
        task.execute();

        System.out.println("Search by line completed successfully");
    }

    @Test
    @DisplayName("Search with case-insensitive flag")
    void testCaseInsensitiveSearch(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt",
                "This contains UPPERCASE",
                "This contains lowercase");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("uppercase");
        task.setFlags("i");
        task.execute();

        System.out.println("Case-insensitive search completed successfully");
    }

    @Test
    @DisplayName("Search with multiline flag")
    void testMultilineSearch(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt",
                "Line 1",
                "Line 2",
                "Line 3");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("Line.*Line");
        task.setFlags("m");
        task.execute();

        System.out.println("Multiline search completed successfully");
    }

    @Test
    @DisplayName("Search in fileset")
    void testSearchFileset(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createTestFile("file1.txt", "Content with pattern");
        createTestFile("file2.txt", "Another pattern here");
        createTestFile("file3.log", "No match here");

        FileSet fileset = new FileSet();
        fileset.setDir(tempDir);
        fileset.setIncludes("*.txt");
        fileset.setProject(project);

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.addFileset(fileset);
        task.setMatch("pattern");
        task.execute();

        System.out.println("Fileset search completed successfully");
    }

    @Test
    @DisplayName("Search with verbose output")
    void testVerboseOutput(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt", "Test content");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("content");
        // Note: ShowFile doesn't have a setVerbose method, verbose output is controlled by log level
        task.execute();

        System.out.println("Search completed successfully");
    }

    @Test
    @DisplayName("Fail when no expression provided")
    void testMissingExpression(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        ShowFile task = new ShowFile();
        task.setProject(project);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Fail when both file and fileset provided")
    void testFileAndFilesetConflict(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt", "Content");

        FileSet fileset = new FileSet();
        fileset.setDir(tempDir);
        fileset.setProject(project);

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.addFileset(fileset);
        task.setMatch("pattern");

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    @DisplayName("Handle missing file gracefully")
    void testMissingFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File nonExistent = new File(tempDir, "nonexistent.txt");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(nonExistent);
        task.setMatch("pattern");
        task.execute();

        // Should log warning but not throw exception
        System.out.println("Missing file handled gracefully");
    }

    @Test
    @DisplayName("Search with regex pattern")
    void testRegexPattern(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt",
                "email: test@example.com",
                "email: another@test.org");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        task.execute();

        System.out.println("Regex pattern search completed successfully");
    }

    @Test
    @DisplayName("Search empty file")
    void testEmptyFile(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File emptyFile = createTestFile("empty.txt");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(emptyFile);
        task.setMatch("pattern");
        task.execute();

        System.out.println("Empty file search completed successfully");
    }

    @Test
    @DisplayName("Search with multiple flags")
    void testMultipleFlags(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt",
                "First LINE",
                "Second line",
                "Third LINE");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        task.setMatch("line");
        task.setFlags("im"); // case-insensitive and multiline
        task.execute();

        System.out.println("Multiple flags search completed successfully");
    }

    @Test
    @DisplayName("Create regexp using createRegexp method")
    void testCreateRegexp(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File testFile = createTestFile("test.txt", "Test pattern");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.setFile(testFile);
        
        // Use createRegexp instead of setMatch
        task.createRegexp().setPattern("pattern");
        task.execute();

        System.out.println("createRegexp method test completed successfully");
    }

    @Test
    @DisplayName("Fail when multiple regexps are created")
    void testMultipleRegexpsFail(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        ShowFile task = new ShowFile();
        task.setProject(project);
        task.createRegexp().setPattern("pattern1");

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.createRegexp().setPattern("pattern2");
        });

        System.out.println("Exception message: " + exception.getMessage());
    }
}

// Made with Bob
