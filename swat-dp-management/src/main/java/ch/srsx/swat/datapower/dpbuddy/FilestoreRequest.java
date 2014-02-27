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

import org.jdom.Element;

import com.myarch.datapower.DPRequest;

/**
 * Copyright (C) 2011 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * The FilestoreRequest class is an extension for the Myarch dpbuddy framework
 * in order to to get file store information (i.e. a directory listing) from
 * particular DataPower device.
 * 
 * {@link FilestoreResponse} and FilestoreRequest are the implementation for the
 * DataPower SOMA 'get-filestore' command.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class FilestoreRequest extends DPRequest {

	private Element getFilestoreElement;

	/*
	 * <xsd:enumeration value="local:"/> <xsd:enumeration value="store:"/>
	 * <xsd:enumeration value="export:"/> <xsd:enumeration value="cert:"/>
	 * <xsd:enumeration value="sharedcert:"/> <xsd:enumeration
	 * value="pubcert:"/> <xsd:enumeration value="image:"/> <xsd:enumeration
	 * value="config:"/> <xsd:enumeration value="chkpoints:"/> <xsd:enumeration
	 * value="logtemp:"/> <xsd:enumeration value="logstore:"/> <xsd:enumeration
	 * value="temporary:"/> <xsd:enumeration value="tasktemplates:"/>
	 */
	private String location = "local:";
	private boolean annotated = false;
	private boolean noSubdirectories = false;
	private boolean layoutOnly = false;

	/*
	 * xsd:attribute name="location" type="tns:filestore-location"/>
	 * <xsd:attribute name="annotated" type="xsd:boolean"/> <xsd:attribute
	 * name="layout-only" type="xsd:boolean"/> <xsd:attribute
	 * name="no-subdirectories" type="xsd:boolean"/>
	 */

	public FilestoreRequest() {
		createGetFilestoreElement();
		setEmptyResponse(new FilestoreResponse());
	}

	public void setLocation(String location) {
		this.location = location;
		this.getFilestoreElement.setAttribute("location", this.location);
	}

	public void setAnnotated(boolean annotated) {
		this.annotated = annotated;
		this.getFilestoreElement.setAttribute("annotated",
				Boolean.toString(this.annotated));
	}

	public void setNoSubdirectories(boolean noSubdirectories) {
		this.noSubdirectories = noSubdirectories;
		this.getFilestoreElement.setAttribute("no-subdirectories",
				Boolean.toString(this.noSubdirectories));
	}

	public void setLayoutOnly(boolean layoutOnly) {
		this.layoutOnly = layoutOnly;
		this.getFilestoreElement.setAttribute("layout-only",
				Boolean.toString(this.layoutOnly));
	}

	private void createGetFilestoreElement() {
		this.getFilestoreElement = new Element("get-filestore",
				"http://www.datapower.com/schemas/management");
		getRequestElement().addContent(this.getFilestoreElement);
	}
}
