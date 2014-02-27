/*
 * Created by Pierce Shah
 * 
 * SWAT - Schlag&rahm WebSphere Administration Toolkit
 *        -           -         -              -
 * 
 * Copyright (c) 2009-2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 *
 *      http://www.schlagundrahm.ch
 *
 */
package ch.srsx.swat.datapower.dpbuddy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.jdom.Element;

import com.myarch.datapower.DPResponse;

/**
 * Copyright (C) 2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * The FilestoreResponse class is an extension for the Myarch dpbuddy framework
 * in order to to get file store information (i.e. a directory listing) from
 * particular DataPower device.
 * 
 * FilestoreResponse and {@link FilestoreRequest} are the implementation for the
 * DataPower SOMA 'get-filestore' command.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class FilestoreResponse extends DPResponse {

	private static Log logger = LogFactory.getLog(FilestoreResponse.class);
	private final String DP_SEPARATOR = "/";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.myarch.datapower.DPResponse#process()
	 */
	@Override
	public void process() {
		validate();
	}

	public void validate() {
		getFiles();
	}

	public List<Element> getFiles() {
		List<Element> files = getRequiredResponseElements("file");
		return files;
	}

	public List<Element> getTopLevelFiles() {
		List<Element> files = getResponseElements("location/file");
		return files;
	}

	public List<Element> getDirectories() {
		List<Element> directories = getResponseElements("directory");
		return directories;
	}

	public List<String> getFilesInDirectory(String location, String subdir) {
		List<String> filesInDirectory = new ArrayList<String>();
		if (subdir == null || subdir.length() == 0) {
			List<Element> files = getTopLevelFiles();
			for (Element file : files) {
				filesInDirectory.add(location + DP_SEPARATOR
						+ file.getAttributeValue("name"));
			}
			return filesInDirectory;
		} else {
			List<Element> directories = getDirectories();
			for (Element directory : directories) {
				String directoryName = directory.getAttributeValue("name");
				if (directoryName.equals(location + DP_SEPARATOR + subdir)) {
					@SuppressWarnings("unchecked")
					List<Element> files = directory.getChildren("file");
					for (Element file : files) {
						filesInDirectory.add(directoryName + DP_SEPARATOR
								+ file.getAttributeValue("name"));
					}
					return filesInDirectory;
				} else {
					continue;
				}
			}
		}
		return filesInDirectory;

	}

	public List<String> getAllFiles(String location) {
		List<String> filesInDirectory = new ArrayList<String>();

		List<Element> files = getTopLevelFiles();
		for (Element file : files) {
			filesInDirectory.add(location + DP_SEPARATOR
					+ file.getAttributeValue("name"));
		}

		List<Element> directories = getDirectories();
		for (Element directory : directories) {
			String directoryName = directory.getAttributeValue("name");
			@SuppressWarnings("unchecked")
			List<Element> children = directory.getChildren("file");
			for (Element file : children) {
				filesInDirectory.add(directoryName + DP_SEPARATOR
						+ file.getAttributeValue("name"));
			}
		}

		return filesInDirectory;

	}

	public void writeResponseFile(String responseFile) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(responseFile));
			out.write(this.toString());
		} catch (IOException iox) {
			throw new BuildException("Could not write to response file - "
					+ iox.getMessage(), iox);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new BuildException(
							"Could not close buffered writer - "
									+ e.getMessage(), e);
				}
			}
		}
	}

}
