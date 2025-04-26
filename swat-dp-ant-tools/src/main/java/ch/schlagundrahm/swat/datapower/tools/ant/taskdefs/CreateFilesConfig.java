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
package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This ANT task generates a DataPower configuration specific files element.
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class CreateFilesConfig extends Task {

    private File file;
    private Vector<FileSet> filesets;
    private FileUtils fileUtils;
    private String env;
    private String domain;
    private String comment;
    private String location = "local";
    private boolean useBaseDir = true;
    private boolean createDpConfig = false;
    private String dpBaseDir;
    private String targetdir;
    private String targetfile;
    private final String dpFileSeparator = "/";
    private final String systemFileSeparator = System.getProperty("file.separator");
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");

    /**
     * CreateFilesConfig constructor
     */
    public CreateFilesConfig() {
        fileUtils = FileUtils.getFileUtils();
        file = null;
        filesets = new Vector<FileSet>();

    }

    /**
     * Sets the input file property.
     * @param file The file to use
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Sets the file set property.
     * @param set The file set to use
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

    /**
     * Sets the environment property
     * @param env The runtime environment (e.g. test)
     */
    public void setEnv(String env) {
        this.env = env;
    }

    /**
     * Sets the target directory property.
     * @param targetdir The directory to use
     */
    public void setTargetdir(String targetdir) {
        this.targetdir = targetdir;
    }

    /**
     * Sets the target file property.
     * @param targetfile The file to use
     */
    public void setTargetfile(String targetfile) {
        this.targetfile = targetfile;
    }

    /**
     * Sets the location property.
     * @param location The location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the flag property to use the base directory.
     * @param useBaseDir set to true to use the base directory
     */
    public void setUseBaseDir(boolean useBaseDir) {
        this.useBaseDir = useBaseDir;
    }

    /**
     * Sets the basedir property.
     * @param dpBaseDir The base directory
     */
    public void setDpBaseDir(String dpBaseDir) {
        this.dpBaseDir = dpBaseDir;
    }

    /**
     * Sets the falg property to create a DataPower configuration file.
     * @param createDpConfig set to true to create a configuration file
     */
    public void setCreateDpConfig(boolean createDpConfig) {
        this.createDpConfig = createDpConfig;
    }

    /**
     * Sets the domain property
     * @param domain The DataPower domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Sets the comment property.
     * @param comment The comment to use
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Main task to execute.
     */
    public void execute() throws BuildException {

        if ((file != null) && (filesets.size() > 0)) {
            throw new BuildException("You cannot supply the 'file' attribute and filesets at the same time.");
        }

        if ((file != null) && file.exists()) {
            log(file + " ==> " + file.getAbsolutePath());
        } else if (file != null) {
            log("The following file is missing: '" + file.getAbsolutePath() + "'", 0);
        }

        int sz = filesets.size();

        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // initialize files element
        Element filesNode = doc.createElement("files");

        if (createDpConfig) {
            // create datapower-configuration element
            Element dpNode = doc.createElement("datapower-configuration");
            dpNode.setAttribute("version", "3");

            // create export-details element
            Element detailsNode = doc.createElement("export-details");

            Element descNode = doc.createElement("description");
            descNode.setTextContent("SWAT generated configuration");
            detailsNode.appendChild(descNode);

            Element userNode = doc.createElement("user");
            userNode.setTextContent(System.getProperty("user.name"));
            detailsNode.appendChild(userNode);

            Element domainNode = doc.createElement("domain");
            domainNode.setTextContent(domain);
            detailsNode.appendChild(domainNode);

            Element commentNode = doc.createElement("comment");
            commentNode.setTextContent(comment);
            detailsNode.appendChild(commentNode);

            Calendar cal = Calendar.getInstance();
            Element dateNode = doc.createElement("current-date");
            dateNode.setTextContent(df.format(cal.getTime()));
            detailsNode.appendChild(dateNode);

            Element timeNode = doc.createElement("current-time");
            timeNode.setTextContent(tf.format(cal.getTime()));
            detailsNode.appendChild(timeNode);

            dpNode.appendChild(detailsNode);

            // create configuration element
            Element configNode = doc.createElement("configuration");
            configNode.setAttribute("domain", domain);

            dpNode.appendChild(configNode);

            // append empty files node
            dpNode.appendChild(filesNode);

            // append root node to the document
            doc.appendChild(dpNode);

        } else {
            doc.appendChild(filesNode);
        }

        for (int i = 0; i < sz; i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();

            log("number of files: " + files.length);

            for (int j = 0; j < files.length; j++) {

                log("file[" + j + "] = " + files[j]);

                File f = new File(fs.getDir(getProject()), files[j]);
                String dpFileSrc = files[j].replace(systemFileSeparator, dpFileSeparator);
                String dpFileName = dpFileSrc;
                if (useBaseDir) {
                    int pos = dpFileName.indexOf(dpFileSeparator);
                    dpBaseDir = dpFileName.substring(0, pos);
                    dpFileName = dpFileName.substring(pos + 1);
                } else {
                    dpBaseDir = location;
                }

                if (f.exists()) {
                    try {
                        log("File: " + f);

                        Element child = doc.createElement("file");
                        filesNode.appendChild(child);
                        child.setAttribute("name", dpBaseDir + ":///" + dpFileName);
                        child.setAttribute("src", dpFileSrc);
                        child.setAttribute("location", location);
                        child.setAttribute("hash", calculateFileHash(f));

                    } catch (Exception e) {
                        log("An error occurred processing file: '" + f.getAbsolutePath() + "': " + e.toString(), 0);
                    }
                } else {
                    log("The following file is missing: '" + f.getAbsolutePath() + "'", 0);
                }
            }
        }
        /*
         * OutputFormat format = new OutputFormat(document); format.setLineWidth(65); format.setIndenting(true);
         * format.setIndent(2);
         */

        try {
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            File f = new File(targetdir + systemFileSeparator + targetfile);
            log("write DataPower files config to: " + f);

            StreamResult sr = new StreamResult(new FileOutputStream(f));
            serializer.transform(new DOMSource(doc), sr);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Calculates the sha1 hash of the given file.
     * @param file The input file
     * @return
     * @throws IOException
     */
    public String calculateFileHash(File file) throws IOException {

        InputStream is = new FileInputStream(file);

        byte[] hash = null;
        try {
            hash = DigestUtils.sha1(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        if (hash == null) {
            throw new BuildException("Could not calcualte SHA-1 hash for file: " + file);
        }

        return Base64.encodeBase64String(hash).trim();
    }

}
