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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64OutputStream;
import org.jdom.Element;

import com.myarch.datapower.DPException;
import com.myarch.datapower.DPResponse;
import com.myarch.datapower.files.FileResponse;

/**
 * Copyright (C) 2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * The DownloadFileResponse class is an extension for the Myarch dpbuddy
 * framework that implements a "get-file" SOMA response.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class DownloadFileResponse extends DPResponse {
	private static Log logger = LogFactory.getLog(FileResponse.class);

	public void process() {
		validate();
	}

	public void validate() {
		getFiles();
	}

	public void saveFile(String file) {
		try {
			logger.info("Saving the file to '" + file + "'");
			File f = new File(file);
			File dir = f.getParentFile();
			if (dir != null) {
				dir.mkdirs();
			}
			Base64OutputStream fos = new Base64OutputStream(
					new BufferedOutputStream(new FileOutputStream(f)), false);

			writeFileContent(fos);
			fos.close();
		} catch (IOException e) {
			throw new DPException(String.format(
					"Error saving file to '%s'. Caused by:\n%s", new Object[] {
							file, e.getMessage() }), e, new Object[0]);
		}
	}

	public void saveFile(File f) {
		try {
			logger.info("Saving the file to '" + f + "'");
			File dir = f.getParentFile();
			if (dir != null) {
				dir.mkdirs();
			}
			Base64OutputStream fos = new Base64OutputStream(
					new BufferedOutputStream(new FileOutputStream(f)), false);

			writeFileContent(fos);
			fos.close();
		} catch (IOException e) {
			throw new DPException(String.format(
					"Error saving file to '%s'. Caused by:\n%s", new Object[] {
							f.getAbsoluteFile(), e.getMessage() }), e,
					new Object[0]);
		}
	}

	public void writeFileContent(OutputStream outStream) throws IOException {
		List<Element> files = getFiles();
		if (files.size() > 1) {
			throw new DPException(
					"Detected multiple file entries in the response. This is not allowed by the schema. XML of the response:\n"
							+ this, new Object[0]);
		}

		for (Element file : files) {
			String dpFileName = file.getAttributeValue("name");
			if (dpFileName != null)
				logger.debug("Saving the file from '" + dpFileName + "'");
			outStream.write(file.getText().getBytes());
		}
	}

	private List<Element> getFiles() {
		List<Element> files = getRequiredResponseElements("dp:file");
		return files;
	}
}
