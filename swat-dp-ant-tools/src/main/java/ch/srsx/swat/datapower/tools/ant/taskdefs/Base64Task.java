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
package ch.srsx.swat.datapower.tools.ant.taskdefs;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.codec.binary.Base64;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Copyright (C) 2011 schlag&rahm AG, Switzerland. All rights reserved.
 * 
 * The Base64Task class is a basic ANT task that encodes or decodes a given string
 * using the sun.misc.BASE64xyz classes.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class Base64Task extends Task {

	private String infile;
	private String outfile;
	private String in;
	private String property;
	private boolean override;
	private boolean decode;

	public void setInfile(String infile) {
		this.infile = infile;
	}

	public void setOutfile(String outfile) {
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

	}

	@Override
	public void execute() throws BuildException {

		String input = null;
		if (infile != null && infile.length() > 0) {
			// read input from file
			StringBuilder sb = new StringBuilder();

			try {
				// use buffering, reading one line at a time
				// FileReader always assumes default encoding is OK!
				BufferedReader inbuf = new BufferedReader(new FileReader(infile));
				try {
					String line = null;
					/*
					 * readLine is a bit quirky : it returns the content of a
					 * line MINUS the newline. it returns null only for the END
					 * of the stream. it returns an empty String if two newlines
					 * appear in a row.
					 */
					while ((line = inbuf.readLine()) != null) {
						sb.append(line);
						sb.append(System.getProperty("line.separator"));
					}
				} finally {
					inbuf.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			input = sb.toString();

		} else if (in != null && in.length() > 0) {
			input = in;
		} else {
			throw new BuildException("Either 'infile' or 'in' has to be provided!", getLocation());
		}

		String output = null;
		if (decode) {
			log("decoding input: " + input, 1);		
			output = new String(Base64.decodeBase64(input.getBytes()));
		} else {
			log("encoding input: " + input, 1);
			output = new String(Base64.encodeBase64(input.getBytes()));
		}

		if (outfile != null && outfile.length() > 0) {

			try {
				Writer out = new OutputStreamWriter(new FileOutputStream(outfile));
				try {

					out.write(output);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (property != null && property.length() > 0) {
			if (this.override) {
				if (getProject().getUserProperty(property) == null) {
					getProject().setProperty(property, output);
				} else {
					getProject().setUserProperty(property, output);
				}
			} else {
				Property p = (Property) getProject().createTask("property");
				p.setName(property);
				p.setValue(output);
				p.execute();
			}

		} else {
			throw new BuildException("Either 'outfile' or 'property' has to be provided!", getLocation());
		}

	}
}
