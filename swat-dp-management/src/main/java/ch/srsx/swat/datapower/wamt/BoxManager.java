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
package ch.srsx.swat.datapower.wamt;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import com.ibm.datapower.wamt.clientAPI.Blob;
import com.ibm.datapower.wamt.clientAPI.Device;
import com.ibm.datapower.wamt.clientAPI.DeviceTypeIncompatibilityException;
import com.ibm.datapower.wamt.clientAPI.Domain;
import com.ibm.datapower.wamt.clientAPI.FeaturesNotEqualException;
import com.ibm.datapower.wamt.clientAPI.MacroProgressContainer;
import com.ibm.datapower.wamt.clientAPI.ManagedSet;
import com.ibm.datapower.wamt.clientAPI.Manager;
import com.ibm.datapower.wamt.clientAPI.MissingFeaturesInFirmwareException;
import com.ibm.datapower.wamt.clientAPI.ModelTypeIncompatibilityException;
import com.ibm.datapower.wamt.clientAPI.ProgressContainer;
import com.ibm.datapower.wamt.clientAPI.URLSource;
import com.ibm.datapower.wamt.clientAPI.UndeployableVersionException;
import com.ibm.datapower.wamt.clientAPI.UnlicensedFeaturesInFirmwareException;


/**
 * Copyright (C) 2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * TODO BoxManager class
 *
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 *
 */
public class BoxManager {

	private BoxManager() throws Exception {
		throw (new Exception("don't instantiate one"));
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws UndeployableVersionException
	 * @throws UnlicensedFeaturesInFirmwareException
	 * @throws MissingFeaturesInFirmwareException
	 * @throws FeaturesNotEqualException
	 * @throws ModelTypeIncompatibilityException
	 * @throws DeviceTypeIncompatibilityException
	 */
	public static void main(String[] args)
			throws DeviceTypeIncompatibilityException,
			ModelTypeIncompatibilityException, FeaturesNotEqualException,
			MissingFeaturesInFirmwareException,
			UnlicensedFeaturesInFirmwareException,
			UndeployableVersionException, Exception {

		// Get an instance of the manager. All subsequent calls to getInstance
		// will return the same
		// instance since the manager is a singleton.
		Manager manager = Manager.getInstance(null);

		// Go ahead and declare the progressContainer var, it will be used a few
		// times.
		ProgressContainer progressContainer = null;

		// Create device object, note the use of a ProgressContainer since the
		// amount of time needed
		// to communicate with the device is indeterminate.
		System.out.println("Create Device: device1");
		progressContainer = null;
		progressContainer = Device.createDevice("tstod0002", "tstod0002.sctst.intra",
				System.getProperty("userid"), args[0], 8888);
		try {
			progressContainer.blockAndTrace(Level.FINER);
		} catch (Exception e) {
			System.out.println(e.getCause().getMessage());

		}
		if (progressContainer.hasError()) {
			Exception e = progressContainer.getError();
			System.out.println("An error occurred creating device object");
			return;
		}
		Device device1 = (Device) progressContainer.getResult();

		// NOTE: At this point you can perform operations on the device like
		// backup/restore, delete a domain, and update firmware. In this
		// example, the device will be added to a managed set.

		// Create managed set object and add device1
		System.out.println("Create Managed Set: set1");
		ManagedSet set1 = new ManagedSet("set1");
		progressContainer = set1.addDevice(device1);
		progressContainer.blockAndTrace(Level.FINER);

		// Create a managed domain in the Device object
		System.out.println("Create domain on device1");
		Domain domain1 = device1.createManagedDomain("myDomain");

		// The following lines of code illustrate how to retrieve
		// the explanation and recommended user action from a
		// DMI exception. If you uncomment these lines, the code
		// will attempt to create the same Domain more than once
		// and it will throw an AlreadyExistsInRepositoryException
		// try{
		// Domain domain2 = device1.createManagedDomain("myDomain");
		// }catch(com.ibm.datapower.wamt.dataAPI.AlreadyExistsInRepositoryException
		// e){
		// System.out.println(e.getMessageExplanation());
		// System.out.println(e.getMessageUseraction());
		// return;
		// }

		// Reference a domain config source via file (note we could also load
		// policy config source via http)
		URLSource domainSource = new URLSource(
				"file:///C:/DPClientAPI_Sample/myDomain.zip");

		// Set the domain and then deploy, the deployment policy setting is
		// optional.
		domain1.setSourceConfiguration(domainSource);

		progressContainer = domain1.deployConfiguration();
		System.out.println("OPTIONAL: PC Total steps="
				+ progressContainer.getTotalSteps());
		progressContainer.blockAndTrace(Level.FINER);
		if (progressContainer.getError() == null) {
			System.out.println("  OK deploy:");
		} else {
			System.out.println("  Failed deploy:"
					+ progressContainer.getError().getMessage());
		}

		// The following line illustrate how to deploy firmware. You can
		// modify the code to provide a firmware image with the correct level
		// for the device you are using.
		//
		System.out.println("Load firmware");
		File firmwareFile = new File(
				"C:/DPClientAPI_Sample/xi-firmware-3.8.1.1.scrypt2");
		Blob blob = new Blob(firmwareFile);
		progressContainer = manager.addFirmware(blob, "New XI firmware");
		progressContainer.blockAndTrace(Level.FINER);
		if (progressContainer.hasError()) {
			System.out.println("An error occurred loading firmware");
			return;
		}

		// Set the firmware level for all devices in the managed set...
		// This sample has only 1 device in the managed set
		System.out.println("Set firmware level and deploy it");
		@SuppressWarnings("unchecked")
		Hashtable<Device, Exception> failedDevices = set1
				.setSourceFirmwareLevel("3.8.1.2");
		if (!failedDevices.isEmpty()) {
			Set<Device> set = failedDevices.keySet();
			Iterator<Device> itr = set.iterator();
			while (itr.hasNext()) {
				Device dev = itr.next();
				Exception e = failedDevices.get(dev);
				System.out.println(e.getLocalizedMessage());
			}
			System.out
					.println("Hmm, a device failed. Maybe we dont have 3.8.1.2 for it?");
			return;
		}
		MacroProgressContainer mpc = set1.deploySourceFirmwareVersion();
		mpc.blockAndTrace(Level.FINER);
		if (mpc.hasError()) {
			// Note that if a device in the set had a problem with the
			// seSourceFirmwareLevel, it will probably fail here too
			System.out.println("An error occurred deploying firmware");
			return;
		}
		// * This can also be done on the device directly
		// device1.setSourceFirmwareLevel("3.8.1.1");
		// progressContainer = device1.deploySourceFirmwareVersion();
		// progressContainer.blockAndTrace(Level.FINER);

		// ////////////////////////////////////////////
		// OPTIONAL REMOVAL - begin
		// The code between the "OPTIONAL" comments
		// removes the newly created device & managed set
		// from the manager. If you skip these then they
		// will remain persisted and be available the next
		// time the manager is started. Thus creating them
		// again will cause an error.
		//
		// remove device from the managed set
		System.out.println("Remove device1");
		progressContainer = set1.removeDevice(device1);
		progressContainer.blockAndTrace(Level.FINER);
		//
		// remove managed set from manager
		System.out.println("Remove managed set1");
		manager.remove(set1);
		//
		// remove device from manager
		System.out.println("Remove device1 from manager");
		manager.remove(device1);
		//
		// OPTIONAL REMOVAL - end
		// ////////////////////////////////////////////

		// shutdown manager
		manager.shutdown();
		System.out.println("Sample COMPLETED.  Exiting.");
	}

}
