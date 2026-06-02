package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Test class for StringMod Ant task.
 *
 * @author Pierce Shah
 */
class StringModTest {

    private Project project;
    private static final String TEST_PROPERTY = "testProperty";

    @BeforeEach
    void setUp() {
        project = new Project();
    }

    private void setAction(StringMod task, String action) throws Exception {
        Method setActionMethod = StringMod.class.getMethod("setAction", Class.forName("ch.schlagundrahm.swat.datapower.tools.ant.taskdefs.StringMod$Mode"));
        Class<?> modeClass = Class.forName("ch.schlagundrahm.swat.datapower.tools.ant.taskdefs.StringMod$Mode");
        Object modeValue = Enum.valueOf((Class<Enum>) modeClass, action);
        setActionMethod.invoke(task, modeValue);
    }

    @Test
    @DisplayName("Convert string to lowercase")
    void testLowercase(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("HELLO WORLD");
        task.setProperty(TEST_PROPERTY);
        setAction(task, "lowercase");
        task.execute();

        String result = project.getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("hello world", result);
    }

    @Test
    @DisplayName("Convert string to uppercase")
    void testUppercase(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("hello world");
        task.setProperty(TEST_PROPERTY);
        setAction(task, "uppercase");
        task.execute();

        String result = project.getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("HELLO WORLD", result);
    }

    @Test
    @DisplayName("Convert string to camelCase with default delimiters")
    void testCamelCaseDefaultDelimiters(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("hello-world_test case");
        task.setProperty(TEST_PROPERTY);
        setAction(task, "camelcase");
        task.execute();

        String result = project.getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("helloWorldTestCase", result);
    }

    @Test
    @DisplayName("Convert string to camelCase with custom delimiters")
    void testCamelCaseCustomDelimiters(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("hello.world.test");
        task.setProperty(TEST_PROPERTY);
        setAction(task, "camelcase");
        task.setDelimiter(new char[] { '.' });
        task.execute();

        String result = project.getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("helloWorldTest", result);
    }

    @Test
    @DisplayName("Override existing property")
    void testOverrideProperty(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        // Set initial property
        project.setProperty(TEST_PROPERTY, "initial");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("OVERRIDE");
        task.setProperty(TEST_PROPERTY);
        setAction(task, "lowercase");
        task.setOverride(true);
        task.execute();

        String result = project.getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("override", result);
    }

    @Test
    @DisplayName("Fail when input is missing")
    void testMissingInput(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setProperty(TEST_PROPERTY);
        setAction(task, "lowercase");

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertEquals("No input to modify.", exception.getMessage());
    }

    @Test
    @DisplayName("Fail when property name is missing")
    void testMissingProperty(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("test");
        setAction(task, "lowercase");

        BuildException exception = assertThrows(BuildException.class, () -> {
            task.execute();
        });

        System.out.println("Exception message: " + exception.getMessage());
        assertEquals("Property name is missing.", exception.getMessage());
    }

    @Test
    @DisplayName("Handle empty string input")
    void testEmptyStringInput(TestInfo info) throws Exception {
        System.out.println("TEST [" + info.getDisplayName() + "]");

        StringMod task = new StringMod();
        task.setProject(project);
        task.setInput("");
        task.setProperty(TEST_PROPERTY);
        setAction(task, "uppercase");
        task.execute();

        String result = project.getProperty(TEST_PROPERTY);
        System.out.println("result: " + result);
        assertEquals("", result);
    }
}

// Made with Bob
