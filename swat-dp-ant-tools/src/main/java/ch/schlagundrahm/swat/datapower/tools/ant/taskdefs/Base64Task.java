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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;

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
    private boolean binary;

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public void setInfile(File infile) {
        this.infile = infile;
    }

    public void setOutfile(File outfile) {
        this.outfile = outfile;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setDecode(boolean decode) {
        this.decode = decode;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * 
     */
    public Base64Task() {
        this.decode = false;
        this.override = false;
        this.binary = false;

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

        if (in != null && property != null) {
            log("The binary flag does not apply to strings (properties)!", Project.MSG_WARN);
            binary = false;
        }

        // initialize local variables
        String textInput = null;
        byte[] binaryInput = null;

        String textOutput = null;
        byte[] binaryOutput = null;

        if ((infile != null) && infile.exists()) {
            log((decode ? "decoding" : "encoding") + " file '" + infile + "' ...");

            if (binary || isBinaryInputFile()) {
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

            } else {
                if (decode) {
                    writeFile(decode(binaryInput), outfile);
                } else {
                    writeFile(decode(binaryInput), outfile);
                }
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

    private String encode(String value) {
        return Base64.getMimeEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        byte[] decodedValue = Base64.getMimeDecoder().decode(value); // Basic Base64 decoding
        return new String(decodedValue, StandardCharsets.UTF_8);
    }

    private byte[] encode(byte[] value) {
        return Base64.getMimeEncoder().encode(value);
    }

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

    private String identifyFileTypeUsingFilesProbeContentType(File file) {
        String fileType = "Undetermined";
        try {
            fileType = Files.probeContentType(file.toPath());
        } catch (IOException ioException) {
            System.out.println("ERROR: Unable to determine file type for '" + file.getAbsolutePath()
                    + "' due to exception " + ioException);
        }
        return fileType;
    }

    private boolean isBinaryInputFile() {
        String fileType = identifyFileTypeUsingFilesProbeContentType(infile);
        if (fileType.startsWith("text")) {
            return false;
        } else {
            return true;
        }
    }

    private void writeViaFileOutuputStream(byte[] data, File file) throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final FileOutputStream fos = new FileOutputStream(file);
        int bytes = 0;
        try (FileChannel channel = fos.getChannel()) {
            bytes = writeToChannel(channel, buffer);
        } finally {
            if (!Objects.isNull(fos)) {
                fos.close();
            }
        }
    }

    private int writeToChannel(final FileChannel channel, final ByteBuffer buffer) throws IOException {
        int bytes = 0;
        while (buffer.hasRemaining()) {
            bytes += channel.write(buffer);
        }
        return bytes;
    }

    private void wrapping() throws IOException {

        String src = "This is the content of any resource read from somewhere"
                + " into a stream. This can be text, image, video or any other stream.";

        // An encoder wraps an OutputStream. The content of /tmp/buff-base64.txt will be the
        // Base64 encoded form of src.
        try (OutputStream os = Base64.getEncoder().wrap(new FileOutputStream("/tmp/buff-base64.txt"))) {
            os.write(src.getBytes("utf-8"));
        }

        // The section bellow illustrates a wrapping of an InputStream and decoding it as the stream
        // is being consumed. There is no need to buffer the content of the file just for decoding it.
        try (InputStream is = Base64.getDecoder().wrap(new FileInputStream("/tmp/buff-base64.txt"))) {
            int len;
            byte[] bytes = new byte[100];
            while ((len = is.read(bytes)) != -1) {
                System.out.print(new String(bytes, 0, len, "utf-8"));
            }
        }
    }

}
