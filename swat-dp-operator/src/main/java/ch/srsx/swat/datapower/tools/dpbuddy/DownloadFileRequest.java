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
package ch.srsx.swat.datapower.tools.dpbuddy;

import org.jdom.Element;

import com.myarch.datapower.DPConnection;
import com.myarch.datapower.DPException;
import com.myarch.datapower.DPRequest;
import com.myarch.datapower.DPResponse;
import com.myarch.datapower.client.DPClient;

/**
 * Copyright (C) 2012 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * TODO DownloadFileRequest class
 *
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 *
 */
public class DownloadFileRequest extends DPRequest {

	private String dpFileName = null;
	private Element getFileElement;

	public DownloadFileRequest() {
		createGetFileElement();
		setEmptyResponse(new DownloadFileResponse());
	}

	public void setFromFile(String dpFileName) {
		if (!(dpFileName.contains(":/"))) {
			dpFileName = "local:/" + dpFileName;
		}

		this.dpFileName = dpFileName;

		this.getFileElement.setAttribute("name", dpFileName);
	}

	private void createGetFileElement() {
		this.getFileElement = new Element("get-file", "http://www.datapower.com/schemas/management");
		getRequestElement().addContent(this.getFileElement);
	}

	public void validate() {
		if (this.dpFileName == null) {
			throw new DPException("'fromFile' is required", new Object[0]);
		}
	}

	public DPResponse execute(DPConnection connection) {
		DPClient client = new DPClient();
		DownloadFileResponse response = (DownloadFileResponse) client.executeRequest(connection, this);
		return response;
	}

}
