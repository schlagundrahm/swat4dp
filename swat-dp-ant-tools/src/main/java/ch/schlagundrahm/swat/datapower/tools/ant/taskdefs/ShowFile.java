/*
 * Created by Pierce Shah
 * 
 * Ant Tools for Swat4DP
 * Schlag&rahm WebSphere Administration Toolkit for DataPower
 * 
 * Copyright (c) 2009-2013 schlag&rahm AG, Switzerland. All rights reserved.
 *
 *      http://www.schlagundrahm.ch
 *
 */
package ch.srsx.swat.datapower.tools.ant.taskdefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.RegularExpression;
import org.apache.tools.ant.util.regexp.Regexp;

/**
 * 
 * The ShowFile ANT task scans a given fileset for the specified regular
 * expression and prints the result to the console.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class ShowFile extends Task {
	private File file;
	private String flags;
	private boolean byline;
	private Vector<FileSet> filesets;
	private RegularExpression regex;
	private int logLevel = 1;

	// output separator (80 dashes)
	private String separator = "--------------------------------------------------------------------------------";

	public ShowFile() {
		file = null;
		filesets = new Vector<FileSet>();
		flags = "";
		byline = false;
		regex = null;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setMatch(String match) {
		if (regex != null) {
			throw new BuildException("Only one regular expression is allowed");
		} else {
			regex = new RegularExpression();
			regex.setPattern(match);

			return;
		}
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public void setByLine(String byline) {
		Boolean res = Boolean.valueOf(byline);

		if (res == null) {
			res = Boolean.FALSE;
		}

		this.byline = res.booleanValue();
	}

	public void addFileset(FileSet set) {
		filesets.addElement(set);
	}

	public RegularExpression createRegexp() {
		if (regex != null) {
			throw new BuildException("Only one regular expression is allowed.");
		} else {
			regex = new RegularExpression();

			return regex;
		}
	}

	protected boolean doMatch(RegularExpression r, String input, int options) {
		Regexp regexp = r.getRegexp(getProject());

		return regexp.matches(input, options);
	}

	protected void doMatch(File f, int options, int counter) throws IOException {
		FileReader r;

		r = null;
		r = new FileReader(f);

		BufferedReader br = new BufferedReader(r);

		log("Searching for pattern '"
				+ regex.getPattern(getProject())
				+ "' in '"
				+ f.getPath()
				+ "'"
				+ (byline ? " by line" : "")
				+ ((flags.length() <= 0) ? ""
						: (" with flags: '" + flags + "'")) + ".", 3);

		if (byline) {
			StringBuffer linebuf = new StringBuffer();
			String line = null;
			boolean hasCR = false;
			int c;

			do {
				c = br.read();

				if (c == 13) {
					if (hasCR) {
						line = linebuf.toString();

						if (doMatch(regex, line, options)) {
							log("#" + counter + " File: " + f.getPath(),
									logLevel - 1);
							log(line, logLevel);
							log(separator, logLevel);
						}

						linebuf.setLength(0);
					} else {
						hasCR = true;
					}
				} else if (c == 10) {
					line = linebuf.toString();

					if (doMatch(regex, line, options)) {
						log("#" + counter + " File: " + f.getPath(),
								logLevel - 1);
						log(line, logLevel);
						log(separator, logLevel);
					}

					if (hasCR) {
						hasCR = false;
					}

					// System.out.print('\n');
					linebuf.setLength(0);
				} else {
					if (hasCR || (c < 0)) {
						line = linebuf.toString();

						if (doMatch(regex, line, options)) {
							log("#" + counter + " File: " + f.getPath(),
									logLevel - 1);
							log(line, logLevel);
							log(separator, logLevel);
						}

						if (hasCR) {
							// System.out.print('\r');
							hasCR = false;
						}

						linebuf.setLength(0);
					}

					if (c >= 0) {
						linebuf.append((char) c);
					}
				}
			} while (c >= 0);
		} else {
			int flen = (int) f.length();
			char[] tmpBuf = new char[flen];
			int numread = 0;

			for (int totread = 0; (numread != -1) && (totread < flen); totread += numread) {
				numread = br.read(tmpBuf, totread, flen);
			}

			String buf = new String(tmpBuf);

			if (doMatch(regex, buf, options)) {
				log("#" + counter + " File: " + f.getPath(), logLevel - 1);
				log("", logLevel);
				log(buf, logLevel);
				log(separator, logLevel);
			}
		}

		r.close();
		r = null;
	}

	public void execute() throws BuildException {
		if (regex == null) {
			throw new BuildException("No expression to match.");
		}

		if ((file != null) && (filesets.size() > 0)) {
			throw new BuildException(
					"You cannot supply the 'file' attribute and filesets at the same time.");
		}

		int options = 0;

		if (flags.indexOf('g') != -1) {
			options |= 0x10;
		}

		if (flags.indexOf('i') != -1) {
			options |= 0x100;
		}

		if (flags.indexOf('m') != -1) {
			options |= 0x1000;
		}

		if (flags.indexOf('s') != -1) {
			options |= 0x10000;
		}

		int counter = 0;

		if ((file != null) && file.exists()) {
			try {
				doMatch(file, options, counter);
			} catch (IOException e) {
				log("An error occurred processing file: '"
						+ file.getAbsolutePath() + "': " + e.toString(), 0);
			}
		} else if (file != null) {
			log("The following file is missing: '" + file.getAbsolutePath()
					+ "'", 0);
		}

		int sz = filesets.size();

		for (int i = 0; i < sz; i++) {
			FileSet fs = (FileSet) filesets.elementAt(i);
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			String[] files = ds.getIncludedFiles();

			for (int j = 0; j < files.length; j++) {
				++counter;

				File f = new File(fs.getDir(getProject()), files[j]);

				if (f.exists()) {
					try {
						doMatch(f, options, counter);
					} catch (Exception e) {
						log("An error occurred processing file: '"
								+ f.getAbsolutePath() + "': " + e.toString(), 0);
					}
				} else {
					log("The following file is missing: '"
							+ f.getAbsolutePath() + "'", 0);
				}
			}
		}
	}
}
