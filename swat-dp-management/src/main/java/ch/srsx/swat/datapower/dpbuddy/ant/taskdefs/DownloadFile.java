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

import java.io.File;

import org.apache.tools.ant.BuildException;

import ch.srsx.swat.datapower.dpbuddy.DownloadFileRequest;
import ch.srsx.swat.datapower.dpbuddy.DownloadFileResponse;

import com.myarch.datapower.DPException;
import com.myarch.datapower.ant.BaseDPTask;

/**
 * Copyright (C) 2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * The DownloadFile ANT task is a Myarch dpduddy extension that downloads a
 * single file from a DataPower device to a defined target directory.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class DownloadFile extends BaseDPTask {

	private final String FILE_SEPARATOR = System.getProperty("file.separator");
	private final String DP_SEPARATOR = "/";
	private String targetDir;
	private String file;
	private String out;
	private boolean overwrite = false;
	private boolean failOnError = true;

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * 
	 */
	public DownloadFile() {
		targetDir = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.myarch.datapower.ant.BaseDPTask#executeDPTask()
	 */
	@Override
	protected void executeDPTask() {

		if ((targetDir == null) || (targetDir.length() == 0)) {
			targetDir = this.getProject().getBaseDir().getAbsolutePath();
		}

		if (this.file == null) {
			throw new DPException("'file' is required", new Object[0]);
		}

		DownloadFileResponse response = null;

		logger.info("download file '" + file + "'");
		DownloadFileRequest fileRequest = new DownloadFileRequest();
		fileRequest.setFromFile(file);

		String toFile = null;
		if (out == null || out.length() == 0) {
			toFile = targetDir + FILE_SEPARATOR
					+ file.substring(file.lastIndexOf(DP_SEPARATOR));
		} else {
			toFile = targetDir + FILE_SEPARATOR + out;
		}

		File outfile = new File(toFile);
		if (!overwrite && outfile.exists()) {
			handleError("Target file '" + toFile + "'already exists!");
		} else {
			response = (DownloadFileResponse) executeRequest(fileRequest);
			response.saveFile(outfile);
		}
	}

	protected void handleError(String msg) {
		if (this.failOnError) {
			throw new BuildException(msg, getLocation());
		}
		logger.warn(msg);
	}

}
