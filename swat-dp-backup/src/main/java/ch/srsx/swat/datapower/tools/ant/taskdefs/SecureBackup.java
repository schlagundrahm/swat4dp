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
package ch.srsx.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.ibm.datapower.wamt.clientAPI.DeletedException;
import com.ibm.datapower.wamt.clientAPI.Device;
import com.ibm.datapower.wamt.clientAPI.FullException;
import com.ibm.datapower.wamt.clientAPI.Manager;
import com.ibm.datapower.wamt.clientAPI.ProgressContainer;


/**
 * Copyright (C) 2012 schlag&amp;rahm gmbh, Switzerland. All rights reserved.
 * 
 * TODO SecureBackup class
 *
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 *
 */
public class SecureBackup extends Task {

	private String hostname;
	private String username;
	private String password;
	private String certname;
	private int port;
	private boolean includeISCSI;
	private boolean includeRAID;
	private String targetBaseDir;
	private boolean forceOverwrite;
	private final String separator = System.getProperty("file.separator");
	private final String dpseparator = "/";

	/**
	 * 
	 */
	public SecureBackup() {
		hostname = null;
		port = 5550;
		username = System.getProperty("user.name");
		certname = "SecureBackup";
		includeISCSI = false;
		includeRAID = false;
		targetBaseDir = null;
		forceOverwrite = false;
		
		// Optionally remove existing handlers attached to j.u.l root logger
		SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

		// add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
		// the initialization phase of your application
		SLF4JBridgeHandler.install();
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setCertname(String certname) {
		this.certname = certname;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setIncludeISCSI(boolean includeISCSI) {
		this.includeISCSI = includeISCSI;
	}

	public void setIncludeRAID(boolean includeRAID) {
		this.includeRAID = includeRAID;
	}

	public void setTargetBaseDir(String targetBaseDir) {
		this.targetBaseDir = targetBaseDir;
	}

	public void setForceOverwrite(boolean forceOverwrite) {
		this.forceOverwrite = forceOverwrite;
	}

	public void execute() throws BuildException {

		if ((hostname == null) || (hostname.length() == 0)) {
			throw new BuildException("Hostname is mandatory.");
		} else if (!hostname.contains(".")) {
			throw new BuildException("Hostname needs to be full-qualified.");
		}

		String symbolicName = hostname.substring(0, hostname.indexOf("."));

		if ((targetBaseDir == null) || (targetBaseDir.length() == 0)) {
			throw new BuildException("targetBaseDir is mandatory.");
		} else if (!(new File(targetBaseDir).exists())) {
			throw new BuildException("Target base dir does not exist.");
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateSubdir = df.format(cal.getTime());
		String targetDir = targetBaseDir + separator + dateSubdir + separator + symbolicName;

		File target = new File(targetDir);
		boolean result = target.mkdirs();

		if (!result && !target.exists()) {
			throw new BuildException("Could not create target direcotory '" + targetDir + "'");
		} else if (!forceOverwrite && target.list().length > 0) {
			throw new BuildException("The target direcotory '" + targetDir
					+ "' is not empty! Clean the directory or use the forceOverwrite attribute.");
		}

		// Get an instance of the manager. All subsequent calls to getInstance
		// will return the same
		// instance since the manager is a singleton.
		Manager manager = null;
		log("Create WAMT Manager");
		try {
			manager = Manager.getInstance(null);
		} catch (Exception x) {
			log("WAMT Manager creation failed : " + x.getCause().getLocalizedMessage(), 0);
			if (manager != null) {
				manager.shutdown();
			}
			throw new BuildException("Can not create Manager instance.", x);
		}

		// Go ahead and declare the progressContainer var, it will be used a few
		// times.
		ProgressContainer progressContainer = null;

		// Create device object, note the use of a ProgressContainer since the
		// amount of time needed
		// to communicate with the device is indeterminate.

		Device device = null;

		log("Get device with symbolic name '" + symbolicName + "'");
		try {
			device = manager.getDeviceBySymbolicName(symbolicName);
		} catch (DeletedException dx) {
			log("Device has been deleted from store!", dx, 1);
		}

		if (device == null) {
			log("Device does not yet exist, create new one");

			if ((password == null) || (password.length() == 0)) {
				manager.shutdown();
				throw new BuildException("Password needs to be specified for devices that do not yet exist.");
			}

			try {
				progressContainer = Device.createDevice(symbolicName, hostname, username, password, port);
			} catch (FullException fx) {
				manager.shutdown();
				throw new BuildException("Could not create device", fx);
			}
			try {
				progressContainer.blockAndTrace(Level.FINER);
			} catch (Exception e) {
				log(e.getCause().getMessage());
			}

			if (progressContainer.hasError()) {
				Exception e = progressContainer.getError();
				manager.shutdown();
				log("An error occurred creating device object");
				throw new BuildException("Could not create device", e);
			}
			device = (Device) progressContainer.getResult();
			
		}  else if ((password != null) && (password.length() > 0)) {
			log("Device exists but password will be changed.");
			
			try {
				manager.remove(device);
			} catch (Exception e) {
				manager.shutdown();
				throw new BuildException("Could not remove device!", e);
			} 
			try {
				progressContainer = Device.createDevice(symbolicName, hostname, username, password, port);
			} catch (FullException fx) {
				manager.shutdown();
				throw new BuildException("Could not create device", fx);
			}
			try {
				progressContainer.blockAndTrace(Level.FINER);
			} catch (Exception e) {
				log(e.getCause().getMessage());
			}

			if (progressContainer.hasError()) {
				Exception e = progressContainer.getError();
				manager.shutdown();
				log("An error occurred creating device object");
				throw new BuildException("Could not create device", e);
			}
			device = (Device) progressContainer.getResult();
			
		}

		// log("Saving secure backup to '" + targetDir + " using certificate '"
		// + certname + "'.");
		targetDir = targetDir.replace(separator, dpseparator);
		log("Saving secure backup to '" + targetDir + " using certificate '" + certname + "'.");

		try {
			progressContainer = device.backup(certname, null, new URI("file:///" + targetDir), includeISCSI, includeRAID);
			progressContainer.blockAndTrace(Level.FINER);
			if (progressContainer.hasError()) {
				log("An error occurred during secure backup!", 0);
				return;
			}
		} catch (Exception e) {
			log("Secure Backup failed : " + e.getMessage(), 0);
			throw new BuildException("Could not execute backup method on device " + device.getDisplayName());
		} finally {
			// shutdown manager
			manager.shutdown();
		}

		log("SecureBackup COMPLETED.");

	}
}
