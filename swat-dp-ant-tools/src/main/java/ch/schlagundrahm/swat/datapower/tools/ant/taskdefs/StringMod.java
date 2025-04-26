//Assisted by watsonx Code Assistant 
/*
* (C) Copyright IBM Corp. 2023.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import org.apache.commons.text.CaseUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/**
 * An Apache Ant task to modify strings.
 */
public class StringMod extends Task {

    private String input;
    private String property;
    private boolean override;
    private Mode action;
    private char[] delimiter;

    /**
     * String modification actions.
     */
    private enum Mode {
        lowercase, uppercase, camelcase
    };

    /**
     * StringMod constructor.
     */
    public StringMod() {
        this.override = false;
        this.delimiter = " -_".toCharArray();
    }

    /**
     * Main execution task.
     */
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
            break;
        case camelcase:
            textOutput = CaseUtils.toCamelCase(input, false, delimiter);
            break;
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

    /**
     * Set the input string to modify.
     * 
     * @param in The input string.
     */
    public void setInput(String in) {
        this.input = in;
    }

    /**
     * Set the name of the property to set with the modified string.
     * 
     * @param property The name of the property.
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Set whether to override an existing property or not.
     * 
     * @param override True to override, false otherwise.
     */
    public void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * Set the action to perform on the input string.
     * 
     * @param action The action to perform.
     */
    public void setAction(Mode action) {
        this.action = action;
    }

    /**
     * Set the delimiter characters to use when converting to camel case.
     * 
     * @param delimiter The delimiter characters.
     */
    public void setDelimiter(char[] delimiter) {
        this.delimiter = delimiter;
    }

}
