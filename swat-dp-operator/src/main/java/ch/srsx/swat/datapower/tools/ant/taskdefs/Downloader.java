/**
 * 
 */
package ch.srsx.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FlatFileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.jdom.Element;

import ch.srsx.swat.datapower.tools.dpbuddy.DownloadFileRequest;
import ch.srsx.swat.datapower.tools.dpbuddy.DownloadFileResponse;
import ch.srsx.swat.datapower.tools.dpbuddy.FilestoreRequest;
import ch.srsx.swat.datapower.tools.dpbuddy.FilestoreResponse;

import com.myarch.datapower.DPResponse;
import com.myarch.datapower.ant.BaseDPTask;

/**
 * @author pshah
 * 
 */
public class Downloader extends BaseDPTask {

	private final String FILE_SEPARATOR = System.getProperty("file.separator");
	private final String DP_SEPARATOR = "/";
	protected String location = "local:";
	protected String subDir = "";
	protected String targetDir = null;
	private String targetFile;
	protected String fileName;
	protected boolean listFiles = false;
	protected boolean regex = false;
	private Pattern pattern;
	private Matcher matcher;
	protected boolean overwrite = false;
	protected boolean failOnError = true;
	protected Mapper mapperElement = null;
	protected boolean flatten = true;

	public Downloader() {
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setSubDir(String subDir) {
		this.subDir = subDir;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setListFiles(boolean listFiles) {
		this.listFiles = listFiles;
	}

	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}

	public void setRegex(boolean regex) {
		this.regex = regex;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public void add(FileNameMapper fileNameMapper) {
		if (this.mapperElement != null) {
			throw new BuildException("Cannot define more than one mapper", getLocation());
		}
		this.mapperElement = new Mapper(getProject());
	}

	public void setFlatten(boolean flatten) {
		this.flatten = flatten;
	}

	@Override
	protected void executeDPTask() {

		if ((targetDir == null) || (targetDir.length() == 0)) {
			targetDir = this.getProject().getBaseDir().getAbsolutePath();
		}

		if ((targetFile == null) || (targetFile.length() == 0)) {
			targetFile = fileName;
		}

		DPResponse response = null;

		if (fileName == null || fileName.length() == 0 || regex) {
			if (listFiles) {
				logger.info("getting file store information for location '" + location + "'");
			} else {
				if (regex) {
					logger.info("download files from location '" + location + DP_SEPARATOR + subDir + "' using regular expression '" + fileName + "'");
					pattern = Pattern.compile(fileName);
				} else {
					logger.info("download all files from location '" + location + DP_SEPARATOR + subDir + "'");
				}
			}

			FilestoreRequest filestoreRequest = new FilestoreRequest();
			// filestoreRequest.setLayoutOnly(false);
			// filestoreRequest.setAnnotated(false);
			filestoreRequest.setLocation(location);

			response = (FilestoreResponse) executeRequest(filestoreRequest);

			if (listFiles) {
				logFilestoreResponse((FilestoreResponse) response);
				((FilestoreResponse) response).writeResponseFile(targetDir + FILE_SEPARATOR + getHostName() + "-filestore-info.xml");
			} else {
				for (String file : ((FilestoreResponse) response).getAllFiles(location)) {
					logger.info("check file '" + file + "'");
					if (regex) {
						matcher = pattern.matcher(file);
						if (!matcher.matches()) {
							continue;
						}
					}
					// File toFile = new File(targetDir + FILE_SEPARATOR +
					// file.substring(file.lastIndexOf(DP_SEPARATOR)));
					File toFile = getOutpuFile(getMapper(), file.substring(file.indexOf(":") + 2));
					if (!overwrite && toFile.exists()) {
						handleError("Target file '" + toFile + "' already exists! Use overwrite mode if needed.");
					}
					logger.info("download file '" + file + "'");
					DownloadFileRequest fileRequest = new DownloadFileRequest();
					fileRequest.setFromFile(file);

					response = (DownloadFileResponse) executeRequest(fileRequest);
					((DownloadFileResponse) response).saveFile(toFile);
				}

			}

		} else {
			logger.info("download file '" + fileName + "' from location '" + location + subDir + "'");
			if ((targetFile == null) || (targetFile.length() == 0)) {
				targetFile = fileName;
			}
			File toFile = new File(targetDir, targetFile);
			if (!overwrite && toFile.exists()) {
				handleError("Target file '" + toFile + "' already exists! Use overwrite mode if needed.");
			}
			DownloadFileRequest fileRequest = new DownloadFileRequest();
			fileRequest.setFromFile(location + "///" + subDir + DP_SEPARATOR + fileName);
			response = (DownloadFileResponse) executeRequest(fileRequest);
			((DownloadFileResponse) response).saveFile(targetDir + FILE_SEPARATOR + targetFile);
		}

		deregisterLogger();
	}

	private String getHostName() {
		String envPrefix = getProject().getProperty(envPrefixPropertyName);
		String url = getProject().getProperty(envPrefix + "." + urlPropertyName);

		if (url != null) {
			return url.substring(url.indexOf("//"), url.indexOf("."));
		}
		return "";
	}

	private void logFilestoreResponse(FilestoreResponse filestoreResponse) {
		logger.info(location);
		for (Element file : filestoreResponse.getTopLevelFiles()) {
			logger.info("\t" + file.getAttributeValue("name") + "\t(" + file.getChildText("modified") + ")");
		}

		for (Element directory : filestoreResponse.getDirectories()) {
			String currentDir = directory.getAttributeValue("name");
			if (currentDir.contains(subDir)) {
				logger.info(currentDir);
				@SuppressWarnings("unchecked")
				List<Element> files = directory.getChildren("file");
				for (Element file : files) {
					logger.info("\t" + file.getAttributeValue("name") + "\t(" + file.getChildText("modified") + ")");
				}
			}

		}

	}

	private FileNameMapper getMapper() {
		FileNameMapper mapper = null;
		if (this.mapperElement != null)
			mapper = this.mapperElement.getImplementation();
		else if (this.flatten)
			mapper = new FlatFileNameMapper();
		else {
			mapper = new IdentityMapper();
		}
		return mapper;
	}

	private File getOutpuFile(FileNameMapper mapper, String fileName) {
		String[] outFileName = mapper.mapFileName(fileName);
		if ((outFileName == null) || (outFileName.length == 0)) {
			log("Skipping " + this.fileName + " it cannot get mapped to output.", 3);
			// throw new BuildException(msg, getLocation());
		}
		if ((outFileName == null) || (outFileName.length > 1)) {
			log("Skipping " + this.fileName + " its mapping is ambiguos.", 3);
			// throw new BuildException(msg, getLocation());
		}
		return new File(targetDir, outFileName[0]);
	}

	protected void handleError(String msg) {
		if (this.failOnError) {
			throw new BuildException(msg, getLocation());
		}
		log(msg, 1);
	}

}
