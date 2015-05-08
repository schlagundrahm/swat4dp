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
import java.net.URI;
import java.util.logging.Level;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.ibm.datapower.wamt.clientAPI.DeletedException;
import com.ibm.datapower.wamt.clientAPI.Device;
import com.ibm.datapower.wamt.clientAPI.FullException;
import com.ibm.datapower.wamt.clientAPI.Manager;
import com.ibm.datapower.wamt.clientAPI.ProgressContainer;


/**
 * Copyright (C) 2012 schlag&amp;rahm gmbh, Switzerland. All rights reserved.
 * 
 * TODO SecureRestore class
 *
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 *
 */
public class SecureRestore extends Task {

	private String hostname;
	private String username;
	private String password;
	private String credentials;
	private int port;
	private boolean validate;
	private String sourceBaseDir;
	private final String separator = System.getProperty("file.separator");
	private final String dpseparator = "/";

	/**
	 * 
	 */
	public SecureRestore() {
		hostname = null;
		port = 5550;
		username = System.getProperty("user.name");
		credentials = "SecureRestore";
		validate = true;
		sourceBaseDir = null;
		
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

	public void setCertname(String credentials) {
		this.credentials = credentials;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public void setSourceBaseDir(String sourceBaseDir) {
		this.sourceBaseDir = sourceBaseDir;
	}

	public void execute() throws BuildException {

		if ((hostname == null) || (hostname.length() == 0)) {
			throw new BuildException("Hostname is mandatory.");
		} else if (!hostname.contains(".")) {
			throw new BuildException("Hostname needs to be full-qualified.");
		}

		String symbolicName = hostname.substring(0, hostname.indexOf("."));

		if ((sourceBaseDir == null) || (sourceBaseDir.length() == 0)) {
			throw new BuildException("sourceBaseDir is mandatory.");
		} else if (!(new File(sourceBaseDir).exists())) {
			throw new BuildException("Source base dir does not exist.");
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
		}

		sourceBaseDir = sourceBaseDir.replace(separator, dpseparator);
		log("Loading secure backup from '" + sourceBaseDir + " using certificate '" + credentials + "'.");

		try {
			progressContainer = device.restore(credentials, new URI("file:///" + sourceBaseDir), validate);
			progressContainer.blockAndTrace(Level.FINER);
			if (progressContainer.hasError()) {
				log("An error occurred during secure restore!", 0);
				return;
			}
		} catch (Exception e) {
			log("Secure Restore failed : " + e.getMessage(), 0);
			throw new BuildException("Could not execute restore method on device " + device.getDisplayName());
		} finally {
			// shutdown manager
			manager.shutdown();
		}

		log("SecureRestore COMPLETED.");

	}
}
