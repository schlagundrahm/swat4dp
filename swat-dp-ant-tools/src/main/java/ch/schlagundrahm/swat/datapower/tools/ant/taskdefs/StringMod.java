package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import org.apache.commons.text.CaseUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

public class StringMod extends Task {

    private String input;
    private String property;
    private boolean override;
    private Mode action;
    private char[] delimiter;

    private enum Mode {
        lowercase, uppercase, camelcase
    };

    public StringMod() {
        this.override = false;
        this.delimiter = " -_".toCharArray();
    }

    @Override
    public void execute() throws BuildException {
        if (input == null) {
            throw new BuildException("No input to modify.");
        }

        if (property == null) {
            throw new BuildException("Property name is missing.");
        }

        String textOutput;

        switch (action) {
        case lowercase:
            textOutput = input.toLowerCase();
            break;
        case uppercase:
            textOutput = input.toUpperCase();
        case camelcase:
            textOutput = CaseUtils.toCamelCase(input, false, delimiter);
        default:
            throw new BuildException("Unsupported action: " + action);
        }

        if (this.override) {
            if (getProject().getUserProperty(property) == null) {
                getProject().setProperty(property, new String(textOutput));
            } else {
                getProject().setUserProperty(property, new String(textOutput));
            }
        } else {
            Property p = (Property) getProject().createTask("property");
            p.setName(property);
            p.setValue(new String(textOutput));
            p.execute();
        }
    }

    public void setInput(String in) {
        this.input = in;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public void setAction(Mode action) {
        this.action = action;
    }

    public void setDelimiter(char[] delimiter) {
        this.delimiter = delimiter;
    }

}
