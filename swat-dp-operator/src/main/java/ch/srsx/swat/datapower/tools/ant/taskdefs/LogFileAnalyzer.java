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
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;


/**
 * Copyright (C) 2012 schlag&rahm gmbh, Switzerland. All rights reserved.
 * 
 * TODO LogFileAnalyzer class
 *
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 *
 */
public class LogFileAnalyzer extends Task {

	private File file;
	private Vector<FileSet> filesets;
	public enum FileType {XML, CSV};
	@SuppressWarnings("unused")
	private FileType fileType;

	public LogFileAnalyzer() {
		file = null;
		filesets = new Vector<FileSet>();
		fileType = FileType.XML;
	}

	@Override
	public void execute() throws BuildException {

		if ((file != null) && (filesets.size() > 0)) {
			throw new BuildException("You cannot supply the 'file' attribute and filesets at the same time.");
		}
		
		

	}

}
