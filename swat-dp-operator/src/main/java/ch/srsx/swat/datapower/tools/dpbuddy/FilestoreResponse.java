/**
 * Copyright (c) 2011 schlag&rahm gmbh
 */
package ch.srsx.swat.datapower.tools.dpbuddy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myarch.datapower.DPResponse;

/**
 * The FilestoreResponse class is an extension for the dpbuddy library in order
 * to to get file store information (i.e. a directory listing) from particular
 * DataPower device.
 * 
 * FilstoreResponse and FilestoreRequest are the implementation for the
 * DataPower SOMA 'filestore' command- {@link FilestoreRequest}
 * 
 * @author pshah@schlagundrahm.ch
 * 
 */
public class FilestoreResponse extends DPResponse {


	private static Logger logger = LoggerFactory.getLogger(FilestoreResponse.class);
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
				filesInDirectory.add(location + DP_SEPARATOR + file.getAttributeValue("name"));
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
						filesInDirectory.add(directoryName + DP_SEPARATOR + file.getAttributeValue("name"));
					}
					return filesInDirectory;
				} else {
					continue;
				}
			}
		}
		return filesInDirectory;

	}

	@SuppressWarnings("unchecked")
	public List<String> getAllFiles(String location) {
		List<String> filesInDirectory = new ArrayList<String>();

		List<Element> files = getTopLevelFiles();
		for (Element file : files) {
			filesInDirectory.add(location + DP_SEPARATOR + file.getAttributeValue("name"));
		}

		List<Element> directories = getDirectories();
		for (Element directory : directories) {
			String directoryName = directory.getAttributeValue("name");
			files = directory.getChildren("file");
			for (Element file : files) {
				filesInDirectory.add(directoryName + DP_SEPARATOR + file.getAttributeValue("name"));
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
			throw new BuildException("Could not write to response file - " + iox.getMessage(), iox);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new BuildException("Could not close buffered writer - " + e.getMessage(), e);
				}
			}
		}
	}

}
