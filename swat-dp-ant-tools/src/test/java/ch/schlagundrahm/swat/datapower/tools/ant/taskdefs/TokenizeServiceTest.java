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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for TokenizeService Ant task.
 * 
 * @author Pierce Shah
 */
class TokenizeServiceTest {

    @TempDir
    File tempDir;

    private Project project;
    private File srcDir;
    private File dstDir;
    private File propertiesDir;
    private File rulesFile;

    @BeforeEach
    void setUp() throws IOException {
        project = new Project();
        srcDir = new File(tempDir, "src");
        dstDir = new File(tempDir, "dst");
        propertiesDir = new File(tempDir, "properties");
        rulesFile = new File(tempDir, "rules.properties");

        srcDir.mkdirs();
        dstDir.mkdirs();
        propertiesDir.mkdirs();
    }

    private void createRulesFile(String... rules) throws IOException {
        try (FileWriter writer = new FileWriter(rulesFile)) {
            for (String rule : rules) {
                writer.write(rule + "\n");
            }
        }
    }

    private void createXcfgFile(String filename, String content) throws IOException {
        File file = new File(srcDir, filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private String createSimpleXcfg(String domain, String objectName, String objectValue) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<datapower-configuration version=\"3\">\n" +
               "  <configuration domain=\"" + domain + "\">\n" +
               "    <TestObject name=\"" + objectName + "\">\n" +
               "      <TestValue>" + objectValue + "</TestValue>\n" +
               "    </TestObject>\n" +
               "  </configuration>\n" +
               "</datapower-configuration>";
    }

    @Test
    @DisplayName("Tokenize simple configuration")
    void testTokenizeSimpleConfig(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile(
                "configuration.@domain=domain.name",
                "TestObject.@name=object.{index}.name",
                "TestObject.TestValue=object.{index}.value");

        createXcfgFile("test.xcfg", createSimpleXcfg("myDomain", "myObject", "myValue"));

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Verify tokenized file exists
        File tokenizedFile = new File(dstDir, "test.xcfg");
        assertTrue(tokenizedFile.exists());

        // Verify properties file exists
        File propsFile = new File(propertiesDir, "test.properties");
        assertTrue(propsFile.exists());

        // Load and verify properties
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));
        assertEquals("myDomain", props.getProperty("domain.name"));
        assertEquals("myObject", props.getProperty("object.1.name"));
        assertEquals("myValue", props.getProperty("object.1.value"));

        // Verify tokenized content
        String tokenizedContent = new String(Files.readAllBytes(tokenizedFile.toPath()));
        assertTrue(tokenizedContent.contains("@domain.name@"));
        assertTrue(tokenizedContent.contains("@object.1.name@"));
        assertTrue(tokenizedContent.contains("@object.1.value@"));
    }

    @Test
    @DisplayName("Tokenize multiple objects with indexing")
    void testTokenizeMultipleObjects(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile(
                "TestObject.@name=object.{index}.name",
                "TestObject.TestValue=object.{index}.value");

        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <TestObject name=\"object1\">\n" +
                "      <TestValue>value1</TestValue>\n" +
                "    </TestObject>\n" +
                "    <TestObject name=\"object2\">\n" +
                "      <TestValue>value2</TestValue>\n" +
                "    </TestObject>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Verify properties
        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));

        assertEquals("object1", props.getProperty("object.1.name"));
        assertEquals("value1", props.getProperty("object.1.value"));
        assertEquals("object2", props.getProperty("object.2.name"));
        assertEquals("value2", props.getProperty("object.2.value"));
    }

    @Test
    @DisplayName("Tokenize with index groups")
    void testTokenizeWithIndexGroups(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile(
                "ObjectA.@name=obj.{index}.name",
                "ObjectB.@name=obj.{index}.name");

        File indexGroupsFile = new File(tempDir, "index-groups.properties");
        try (FileWriter writer = new FileWriter(indexGroupsFile)) {
            writer.write("group.objects=ObjectA,ObjectB\n");
        }

        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <ObjectA name=\"objA1\"/>\n" +
                "    <ObjectB name=\"objB1\"/>\n" +
                "    <ObjectA name=\"objA2\"/>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.setIndexGroupsFile(indexGroupsFile);
        task.execute();

        // Verify properties - grouped objects should share index sequence
        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));

        assertEquals("objA1", props.getProperty("obj.1.name"));
        assertEquals("objB1", props.getProperty("obj.2.name"));
        assertEquals("objA2", props.getProperty("obj.3.name"));
    }

    @Test
    @DisplayName("Process multiple xcfg files")
    void testMultipleXcfgFiles(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.{index}.name");

        createXcfgFile("file1.xcfg", createSimpleXcfg("domain1", "obj1", "val1"));
        createXcfgFile("file2.xcfg", createSimpleXcfg("domain2", "obj2", "val2"));

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Verify both files were processed
        assertTrue(new File(dstDir, "file1.xcfg").exists());
        assertTrue(new File(dstDir, "file2.xcfg").exists());
        assertTrue(new File(propertiesDir, "file1.properties").exists());
        assertTrue(new File(propertiesDir, "file2.properties").exists());
    }

    @Test
    @DisplayName("Handle empty source directory")
    void testEmptySourceDirectory(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.name");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Should complete without error
        System.out.println("Empty directory handled successfully");
    }

    @Test
    @DisplayName("Fail when srcDir is missing")
    void testMissingSrcDir(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.name");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("srcDir"));
    }

    @Test
    @DisplayName("Fail when dstDir is missing")
    void testMissingDstDir(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.name");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("dstDir"));
    }

    @Test
    @DisplayName("Fail when propertiesDir is missing")
    void testMissingPropertiesDir(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.name");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setRulesFile(rulesFile);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("propertiesDir"));
    }

    @Test
    @DisplayName("Fail when rulesFile is missing")
    void testMissingRulesFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("rulesFile"));
    }

    @Test
    @DisplayName("Fail when rulesFile does not exist")
    void testNonExistentRulesFile(TestInfo info) {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        File nonExistent = new File(tempDir, "nonexistent.properties");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(nonExistent);

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    @DisplayName("Create output directories if they don't exist")
    void testCreateOutputDirectories(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.name");
        createXcfgFile("test.xcfg", createSimpleXcfg("domain", "obj", "val"));

        // Delete output directories
        dstDir.delete();
        propertiesDir.delete();

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Verify directories were created
        assertTrue(dstDir.exists());
        assertTrue(propertiesDir.exists());
    }

    @Test
    @DisplayName("Tokenize nested elements")
    void testTokenizeNestedElements(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile(
                "Parent.@name=parent.{index}.name",
                "Parent.Child.@name=child.{index}.name",
                "Parent.Child.Value=child.{index}.value");

        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <Parent name=\"parent1\">\n" +
                "      <Child name=\"child1\">\n" +
                "        <Value>value1</Value>\n" +
                "      </Child>\n" +
                "    </Parent>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Verify properties
        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));

        assertEquals("parent1", props.getProperty("parent.1.name"));
        assertEquals("child1", props.getProperty("child.1.name"));
        assertEquals("value1", props.getProperty("child.1.value"));
    }

    @Test
    @DisplayName("Generate properties file with header")
    void testPropertiesFileHeader(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.@name=object.name");
        createXcfgFile("test.xcfg", createSimpleXcfg("domain", "obj", "val"));

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        // Verify properties file has header
        File propsFile = new File(propertiesDir, "test.properties");
        List<String> lines = Files.readAllLines(propsFile.toPath());

        boolean hasGeneratedComment = lines.stream().anyMatch(l -> l.contains("Auto-generated"));
        boolean hasSourceComment = lines.stream().anyMatch(l -> l.contains("Source:"));
        boolean hasRulesComment = lines.stream().anyMatch(l -> l.contains("Rules:"));

        assertTrue(hasGeneratedComment, "Should have 'Auto-generated' comment");
        assertTrue(hasSourceComment, "Should have 'Source:' comment");
        assertTrue(hasRulesComment, "Should have 'Rules:' comment");
    }

    @Test
    @DisplayName("Custom rules file overrides base rules")
    void testCustomRulesFileOverridesBase(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Base rules file: maps TestValue to 'base.value'
        createRulesFile("TestObject.TestValue=base.value");

        // Custom rules file: overrides the same XPath with a different token name
        File customRulesFile = new File(tempDir, "custom-rules.properties");
        try (FileWriter writer = new FileWriter(customRulesFile)) {
            writer.write("TestObject.TestValue=custom.value\n");
        }

        createXcfgFile("test.xcfg", createSimpleXcfg("domain", "obj", "val"));

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.setCustomRulesFile(customRulesFile);
        task.execute();

        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));

        // Custom rule should win: token name is 'custom.value', not 'base.value'
        assertEquals("val", props.getProperty("custom.value"), "Custom rule should override base rule");
        assertEquals(null, props.getProperty("base.value"), "Base rule should be replaced by custom rule");
    }

    @Test
    @DisplayName("Custom rules file adds new rules alongside base rules")
    void testCustomRulesFileAddsNewRules(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Base rules: maps TestValue only
        createRulesFile("TestObject.TestValue=base.value");

        // Custom rules: adds a rule for a second element not covered by base
        File customRulesFile = new File(tempDir, "custom-rules.properties");
        try (FileWriter writer = new FileWriter(customRulesFile)) {
            writer.write("ExtraObject.ExtraValue=extra.value\n");
        }

        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <TestObject name=\"obj1\">\n" +
                "      <TestValue>val1</TestValue>\n" +
                "    </TestObject>\n" +
                "    <ExtraObject name=\"extra1\">\n" +
                "      <ExtraValue>extra1</ExtraValue>\n" +
                "    </ExtraObject>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.setCustomRulesFile(customRulesFile);
        task.execute();

        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));

        assertEquals("val1", props.getProperty("base.value"), "Base rule should still produce its token");
        assertEquals("extra1", props.getProperty("extra.value"), "Custom rule should add new token");
    }

    @Test
    @DisplayName("Custom rules file is silently ignored when it does not exist")
    void testMissingCustomRulesFileIsIgnored(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("TestObject.TestValue=base.value");
        createXcfgFile("test.xcfg", createSimpleXcfg("domain", "obj", "val"));

        File nonExistentCustomRules = new File(tempDir, "nonexistent-custom-rules.properties");

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.setCustomRulesFile(nonExistentCustomRules);
        task.execute();

        // Should complete without error using only the base rules
        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));
        assertEquals("val", props.getProperty("base.value"), "Base rule should still work");
    }

    @Test
    @DisplayName("Custom index groups file extends base index groups")
    void testCustomIndexGroupsFileExtension(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Base rules: all three types map to the same token pattern
        createRulesFile(
                "ObjectA.@name=obj.{index}.name",
                "ObjectB.@name=obj.{index}.name",
                "ObjectC.@name=obj.{index}.name");

        // Base index groups: only A and B grouped
        File baseIndexGroups = new File(tempDir, "base-index-groups.properties");
        try (FileWriter writer = new FileWriter(baseIndexGroups)) {
            writer.write("group.objects=ObjectA,ObjectB\n");
        }

        // Custom index groups: extend the group to include C as well
        File customIndexGroups = new File(tempDir, "custom-index-groups.properties");
        try (FileWriter writer = new FileWriter(customIndexGroups)) {
            writer.write("group.objects=ObjectA,ObjectB,ObjectC\n");
        }

        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <ObjectA name=\"a1\"/>\n" +
                "    <ObjectB name=\"b1\"/>\n" +
                "    <ObjectC name=\"c1\"/>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.setIndexGroupsFile(baseIndexGroups);
        task.setCustomIndexGroupsFile(customIndexGroups);
        task.execute();

        File propsFile = new File(propertiesDir, "test.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(propsFile.toPath()));

        // All three should share one continuous index sequence
        assertEquals("a1", props.getProperty("obj.1.name"), "ObjectA should be index 1");
        assertEquals("b1", props.getProperty("obj.2.name"), "ObjectB should be index 2");
        assertEquals("c1", props.getProperty("obj.3.name"), "ObjectC should be index 3 (added by custom group)");
    }

    @Test
    @DisplayName("Unused namespace declarations are stripped from tokenized output")
    void testUnusedNamespacesAreStripped(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("MPGateway.LocalAddress=local.address");

        // xcfg with xmlns:env and xmlns:dp on a service-object element — neither prefix
        // is used by any element or attribute in the subtree (mimics a real DataPower split export)
        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <MPGateway name=\"my-service\"\n" +
                "               xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                "               xmlns:dp=\"http://www.datapower.com/schemas/management\">\n" +
                "      <LocalAddress>1.2.3.4</LocalAddress>\n" +
                "    </MPGateway>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        String tokenized = new String(Files.readAllBytes(new File(dstDir, "test.xcfg").toPath()));

        // The unused namespace declarations must be absent from the output
        assertTrue(!tokenized.contains("xmlns:env="), "xmlns:env should be stripped (unused)");
        assertTrue(!tokenized.contains("xmlns:dp="),  "xmlns:dp should be stripped (unused)");

        // The tokenized value must still be present — stripping namespaces must not affect content
        assertTrue(tokenized.contains("@local.address@"), "Token placeholder must still be present");
    }

    @Test
    @DisplayName("Used namespace declarations are preserved in tokenized output")
    void testUsedNamespacesArePreserved(TestInfo info) throws IOException {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        createRulesFile("Envelope.Body.value=body.value");

        // xcfg where xmlns:env IS used — the element itself is env:prefixed
        String xcfgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<datapower-configuration version=\"3\">\n" +
                "  <configuration domain=\"test\">\n" +
                "    <Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "      <env:Body>\n" +
                "        <value>hello</value>\n" +
                "      </env:Body>\n" +
                "    </Envelope>\n" +
                "  </configuration>\n" +
                "</datapower-configuration>";

        createXcfgFile("test.xcfg", xcfgContent);

        TokenizeService task = new TokenizeService();
        task.setProject(project);
        task.setSrcDir(srcDir);
        task.setDstDir(dstDir);
        task.setPropertiesDir(propertiesDir);
        task.setRulesFile(rulesFile);
        task.execute();

        String tokenized = new String(Files.readAllBytes(new File(dstDir, "test.xcfg").toPath()));

        // xmlns:env must be kept because env:Body uses it
        assertTrue(tokenized.contains("xmlns:env="), "xmlns:env must be preserved when the prefix is used");
    }
}

// Made with Bob
