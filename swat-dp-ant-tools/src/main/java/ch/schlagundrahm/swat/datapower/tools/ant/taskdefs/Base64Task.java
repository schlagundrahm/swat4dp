/*
 * Created by Pierce Shah
 * 
 * Ant Tools for Swat4DP
 * Schlag&rahm WebSphere Administration Toolkit for DataPower
 * 
 * Copyright (c) 2009-2013 schlag&rahm AG, Switzerland. All rights reserved.
 *
 *      http://www.schlagundrahm.ch
 *
 */
package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/**
 * The Base64Task class is a basic ANT task that encodes or decodes a given string or file using the @java.util.Base64
 * MIME methods.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 */
public class Base64Task extends Task {

    private File infile;
    private File outfile;
    private String in;
    private String property;
    private boolean override;
    private boolean decode;
    private Boolean binary;

    /**
     * Sets the binary flag for the input data.
     * @param binary If the true the input data is treated as binary.
     */
    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    /**
     * Sets the input file.
     * @param infile The input file
     */
    public void setInfile(File infile) {
        this.infile = infile;
    }

    /**
     * Sets the output file
     * @param outfile The output file
     */
    public void setOutfile(File outfile) {
        this.outfile = outfile;
    }

    /**
     * Sets the input string that should be decode or encoded.
     * @param in The input string
     */
    public void setIn(String in) {
        this.in = in;
    }

    /**
     * Sets the property name for the return value.
     * @param property The name of the property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Sets the decode/encode mode.
     * @param decode Whether to decode or encode the input.
     */
    public void setDecode(boolean decode) {
        this.decode = decode;
    }

    /**
     * Whether to override an existing property with the new value.
     * @param override Override the given property.
     */
    public void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * By default, do base64 encoding and do not override existing properties.
     */
    public Base64Task() {
        this.decode = false;
        this.override = false;
    }

    @SuppressWarnings("unused")
    @Override
    public void execute() throws BuildException {

        if (infile != null && in != null) {
            throw new BuildException("Either 'infile' or 'in' has to be provided!", getLocation());
        }

        if (outfile != null && property != null) {
            throw new BuildException("Either 'outfile' or 'property' has to be provided!", getLocation());
        }

        if (in != null && binary != null && binary == true) {
            log("The binary flag does not apply to string input (properties)!", Project.MSG_WARN);
            binary = false;
        }

        // initialize local variables
        String textInput = null;
        byte[] binaryInput = null;

        String textOutput = null;
        byte[] binaryOutput = null;

        if ((infile != null) && infile.exists()) {
            log((decode ? "decoding" : "encoding") + " file '" + infile + "' ...");
            if ((binary != null && binary == true)) {
                binaryInput = readBinary(infile);
            } else if (binary != null && binary == false) {
                textInput = readText(infile);
            } else if (isBinaryInputFile()) {
                binaryInput = readBinary(infile);
            } else {
                textInput = readText(infile);
            }
        } else if (infile != null) {
            throw new BuildException("The infile does not exist: '" + infile.getAbsolutePath() + "'!", getLocation());
        }

        if (in != null && in.length() > 0) {
            log((decode ? "decoding" : "encoding") + " input string '" + in + "' ...");
            textInput = in;
        }

        if (outfile != null) {
            log("writing " + (decode ? "decoded" : "encoded") + " output to file '" + outfile + "' ...");

            if (textInput != null) {

                if (decode) {
                    writeFile(decode(textInput).getBytes(StandardCharsets.UTF_8), outfile);
                } else {
                    writeFile(encode(textInput).getBytes(StandardCharsets.UTF_8), outfile);
                }

            } else if (binaryInput != null) {
                if (decode) {
                    writeFile(decode(binaryInput), outfile);
                } else {
                    writeFile(encode(binaryInput), outfile);
                }
            } else {
                throw new BuildException("Internal error! The computed input ('textInput' and 'binaryInput') is NULL!");
            }

        } else if (property != null && property.length() > 0) {
            log("writing " + (decode ? "decoded" : "encoded") + " output to property '" + property + "' ...");

            if (binaryInput != null) {
                if (decode) {
                    textOutput = new String(decode(binaryInput), StandardCharsets.UTF_8);
                } else {
                    textOutput = new String(encode(binaryInput), StandardCharsets.UTF_8);
                }
            } else if (textInput != null) {
                if (decode) {
                    textOutput = decode(textInput);
                } else {
                    textOutput = encode(textInput);
                }
            } else {
                throw new BuildException("Internal error! The computed input ('textInput' and 'binaryInput') is NULL!");
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

        } else {
            throw new BuildException("Either 'outfile' or 'property' has to be provided!", getLocation());
        }

    }

    /**
     * Encodes a string into a Base64 encoded string.
     * 
     * @param value The string to encode
     * @return The Base64 encoded string
     */
    private String encode(String value) {
        return Base64.getMimeEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a base64 encoded string.
     * @param value The string to decode
     * @return The decoded string
     */
    private String decode(String value) {
        byte[] decodedValue = Base64.getMimeDecoder().decode(value);
        return new String(decodedValue, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a byte array into a Base64 encoded byte array.
     * @param value The byte array to encode
     * @return The Base64 encoded byte array
     */
    private byte[] encode(byte[] value) {
        return Base64.getMimeEncoder().encode(value);
    }

    /**
     * Decode Base64 encoded byte array.
     * @param value The encoded byte array
     * @return The decoded byte array
     */
    private byte[] decode(byte[] value) {
        return Base64.getMimeDecoder().decode(value);
    }

    private byte[] readBinary(File file) {
        byte[] result = null;
        try {
            result = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Reads the data from the specified file.
     * 
     * @param file The file to read
     * @return The text in the file
     */
    private String readText(File file) {
        String result = null;
        try {
            result = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
    * Writes the given data to the specified file.
    * 
    * @param data The data to write.
    * @param file The file to write to.
    */
    private void writeFile(byte[] data, File file) {
        if (Files.exists(file.toPath()) && !override) {
            throw new BuildException("The 'outfile' already exists! Delete it or set 'override' to true.",
                    getLocation());
        }

        try {
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Identifes the file type by probing the content.
     * @param file The file to inspect
     * @return The file type
     */
    private String identifyFileTypeUsingFilesProbeContentType(File file) {
        String fileType = "Undetermined";
        try {
            fileType = Files.probeContentType(file.toPath());
        } catch (IOException ioException) {
            log("Unable to determine file type for '" + file.getAbsolutePath() + "' due to exception " + ioException,
                    Project.MSG_WARN);
        }
        return fileType;
    }

    /**
     * Determines whether an input file is binary or not.
     * @return True for binary files
     */
    private boolean isBinaryInputFile() {
        String fileType = identifyFileTypeUsingFilesProbeContentType(infile);
        if (fileType != null && fileType.startsWith("text")) {
            log("Identified file input as text.", Project.MSG_INFO);
            return false;
        } else {
            log("Identified file input as binary.", Project.MSG_INFO);
            return true;
        }
    }

}
