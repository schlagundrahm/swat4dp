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
package ch.srsx.swat.datapower.dpbuddy.ant.taskdefs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tools.ant.BuildException;
import org.jdom.Element;

import com.myarch.datapower.ant.BaseDPTask;
import com.myarch.datapower.status.StatusRequest;
import com.myarch.datapower.status.StatusResponse;

/**
 * Copyright (C) 2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * The DownloadWebServicesOperationMetrics ANT task is a Myarch dpbuddy
 * extension that exports the WebService statistics as CSV file.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class DownloadWebServicesOperationMetrics extends BaseDPTask {

	private final String TYPE = "WSOperationMetrics";
	private final String CSV_SEPARATOR = "; ";
	private final String FILE_EXTENSION = ".csv";
	private final String FILE_SEPARATOR = System.getProperty("file.separator");
	private String targetDir;
	private String fileName;
	private String suffix;

	public DownloadWebServicesOperationMetrics() {
		targetDir = null;
		fileName = "WebServicesOperationMetrics";
		suffix = "";

	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.myarch.datapower.ant.BaseDPTask#executeDPTask()
	 */
	@Override
	protected void executeDPTask() {

		Log logger = initLogger();

		if ((targetDir == null) || (targetDir.length() == 0)) {
			targetDir = this.getProject().getBaseDir().getAbsolutePath();
		}

		/*
		 * <type>WSOperationMetrics</type>
		 * <type>WSOperationMetricsSimpleIndex</type>
		 */
		StatusRequest statusRequest = new StatusRequest(TYPE);
		StatusResponse response = (StatusResponse) executeRequest(statusRequest);
		logger.info(String.format("Status for the class '%s':",
				new Object[] { getDisplayClass(TYPE) }));

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(targetDir
					+ FILE_SEPARATOR + fileName + suffix + FILE_EXTENSION));
			out.write(writeOutput(response));
			out.close();
		} catch (IOException iox) {
			throw new BuildException("Could not write to target file - "
					+ iox.getMessage(), iox);
		}

		String statusString = response.toStatusString();
		if (statusString.length() > 0) {
			logger.info(statusString);
		} else {
			logger.info("No object of the given class was found");
		}
		deregisterLogger();
	}

	private String getDisplayClass(String clazz) {
		int statusPos = clazz.lastIndexOf("Status");
		if (statusPos > 0) {
			return clazz.substring(0, statusPos);
		}
		return clazz;
	}

	private String writeOutput(StatusResponse r) {
		List<Element> statusElts = r.getStatusElements();
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Element statusElt : statusElts) {
			@SuppressWarnings("unchecked")
			List<Element> statusChildren = statusElt.getChildren();

			StringBuilder headerBuf = new StringBuilder();
			StringBuilder lineBuf = new StringBuilder();

			for (Element statusChildElt : statusChildren) {
				if (lineBuf.length() > 0) {
					lineBuf.append(CSV_SEPARATOR);
				}
				if (first && headerBuf.length() > 0) {
					headerBuf.append(CSV_SEPARATOR);
				}

				if (first) {
					headerBuf.append(statusChildElt.getName());
				}

				lineBuf.append(statusChildElt.getTextTrim());
			}

			if (first) {
				buf.append(headerBuf + "\n");
				first = false;
			}
			buf.append(lineBuf + "\n");
		}

		return buf.toString();
	}

}
