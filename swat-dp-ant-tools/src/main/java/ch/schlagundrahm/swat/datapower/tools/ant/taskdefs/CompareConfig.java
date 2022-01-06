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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * The CompareConfig ANT task compares two DataPower configuration archives (ZIP). It compares the content list of the
 * archives and the content of the actual configuration file (usually the export.xml).
 * 
 * @author <a href="mailto:pshah@schlagundrahm.ch">Pierce Shah</a>
 * 
 */
public class CompareConfig extends Task {

    /**
     * @param includeIntrinsic the includeIntrinsic to set
     */
    public void setIncludeIntrinsic(boolean includeIntrinsic) {
        this.includeIntrinsic = includeIntrinsic;
    }

    /**
     * @param nodeTypeDiff the nodeTypeDiff to set
     */
    public void setNodeTypeDiff(boolean nodeTypeDiff) {
        this.nodeTypeDiff = nodeTypeDiff;
    }

    /**
     * @param nodeValueDiff the nodeValueDiff to set
     */
    public void setNodeValueDiff(boolean nodeValueDiff) {
        this.nodeValueDiff = nodeValueDiff;
    }

    static private String FILE_SEPARATOR = System.getProperty("file.separator");
    static private String URL_SEPARATOR = "/";
    static private String OUTPUT_SEPARATOR_1 = "-------------------------------------------------------------------------------";
    static private String OUTPUT_SEPARATOR_2 = "===============================================================================";
    static private String OUTPUT_SEPARATOR_3 = "_______________________________________________________________________________";

    static public enum DP_ATTRIBUTE {
        name, intrinsic
    };

    private File cfgFile1;
    private File cfgFile2;
    private boolean includeExportDetails = false;
    private boolean includeDpAuxDir = false;
    private boolean includeFiles = false;
    private boolean includeConfigFolder = false;
    private boolean includeIntrinsic = false;

    private boolean nodeTypeDiff = false;
    private boolean nodeValueDiff = true;

    public void setCfgFile1(File cfgFile1) {
        this.cfgFile1 = cfgFile1;
    }

    public void setCfgFile2(File cfgFile2) {
        this.cfgFile2 = cfgFile2;
    }

    /**
     * If set to true the &lt;export-details&gt; section within the DataPower configuration will be compared as well.
     * 
     * Default is false.
     * 
     * @param includeExportDetails include export details in comparison
     */
    public void setIncludeExportDetails(boolean includeExportDetails) {
        this.includeExportDetails = includeExportDetails;
    }

    /**
     * If set to true the dp-aux folder within the DataPower configuration archive will be compared as well.
     * 
     * Default is false.
     * 
     * @param includeDpAuxDir include dp-aux files in comparison
     */
    public void setIncludeDpAuxDir(boolean includeDpAuxDir) {
        this.includeDpAuxDir = includeDpAuxDir;
    }

    /**
     * If set to true the &lt;files&gt; section within the DataPower configuration will be compared as well.
     * 
     * Default is false.
     * 
     * @param includeFiles include files section in comparison
     */
    public void setIncludeFiles(boolean includeFiles) {
        this.includeFiles = includeFiles;
    }

    /**
     * If set to true the config folder within the DataPower configuration archive will be compared as well.
     * 
     * Default is false.
     * 
     * @param includeConfigFolder include config files in comparison
     */
    public void setIncludeConfigFolder(boolean includeConfigFolder) {
        this.includeConfigFolder = includeConfigFolder;
    }

    @Override
    public void execute() throws BuildException {

        // both input files are mandatory
        if (cfgFile1 == null || cfgFile2 == null) {
            throw new BuildException("You have to provide two files to be compared.");
        }

        if ((isArchiveFile(cfgFile1) && !isArchiveFile(cfgFile2))
                || (!isArchiveFile(cfgFile1) && isArchiveFile(cfgFile2))) {
            throw new BuildException("You cannot compare an archive with an XML configuration!");

        } else if (isArchiveFile(cfgFile1) && isArchiveFile(cfgFile2)) {
            // read ZIP config #1
            log("reading file " + cfgFile1.getName() + " ...", 0);

            Map<String, String> fileContent1 = null;
            try {
                fileContent1 = readContent(new FileInputStream(cfgFile1), "", "[1]");
            } catch (FileNotFoundException fnfx) {
                throw new BuildException("File '" + cfgFile1.getAbsolutePath() + "' does not exist!", fnfx);
            }
            log(OUTPUT_SEPARATOR_2, 0);

            // read ZIP config #2
            log("reading file " + cfgFile2.getName() + " ...", 0);
            Map<String, String> fileContent2 = null;
            try {
                fileContent2 = readContent(new FileInputStream(cfgFile2), "", "[2]");
            } catch (FileNotFoundException fnfx) {
                throw new BuildException("File '" + cfgFile1.getAbsolutePath() + "' does not exist!", fnfx);
            }

            // compare the content of both ZIP archives
            boolean hasSameContent = CollectionUtils.isEqualCollection(fileContent1.entrySet(),
                    fileContent2.entrySet());
            if (!hasSameContent) {
                log("the two provided ZIP files do not have the same content!", 0);
                compareContent(fileContent1, fileContent2);
            } else {
                log("OK - The two provided DataPower ZIP files have the same content.", 0);
            }
        } else {
            // we compare two XML configuration files
            includeConfigFolder = false;
            includeDpAuxDir = false;

            try {
                compareConfig(new FileInputStream(cfgFile1), new FileInputStream(cfgFile2),
                        cfgFile1.getName() + URL_SEPARATOR + cfgFile2.getName());
            } catch (FileNotFoundException fnfx) {
                throw new BuildException("File does not exist!", fnfx);
            }
        }
    }

    /**
     * Read the content of the given ZIP file and store the result in a TreeMap for further comparison.
     * 
     * @param in InputStream pointing to a ZIP file
     * @param indent print prefix for nested ZIP files
     * @return sorted map containing the file names (as keys) and corresponding CRC checksum (as value)
     */
    private TreeMap<String, String> readContent(InputStream in, String parent, String indent) {

        TreeMap<String, String> content = new TreeMap<String, String>();

        try {
            ZipInputStream zis = new ZipInputStream(in);
            ZipEntry ze = null;

            while ((ze = zis.getNextEntry()) != null) {
                printContentInfo(ze, indent);
                if (!includeDpAuxDir && ze.getName().contains("dp-aux")) {
                    // continue;
                } else if (!includeConfigFolder && ze.getName().startsWith("config")) {
                    // continue;
                } else if (isZipEntry(ze)) {
                    String newParent = ze.getName();
                    content.putAll(readContent(zis, newParent, indent + "-->"));

                } else if (!ze.isDirectory()) {
                    content.put(parent + URL_SEPARATOR + ze.getName(), Long.toString(ze.getCrc()));
                }

                zis.closeEntry();

            } // while

        } catch (IOException iox) {
            // TODO Auto-generated catch block
            iox.printStackTrace();
        }
        return content;

    }

    /**
     * Print the ZipEntry info to the console.
     * 
     * @param ze ZIP file entry
     * @param indent prefix
     */
    private void printContentInfo(ZipEntry ze, String indent) {
        log(indent + ze.getName() + " [size: " + ze.getSize() + ", CRC: " + ze.getCrc() + "]", Project.MSG_INFO);
    }

    /**
     * Compare the files contained in the given two archive maps. Print identical and exclusive files. Files that exist
     * in both archives but are different will be compared content wise.
     * 
     * @param c1 file map of archive 1
     * @param c2 file map of archive 2
     * @throws IOException when a file that should be compared content wise can not be read/extracted from the archive
     */
    private void compareContent(Map<String, String> c1, Map<String, String> c2) {

        log(OUTPUT_SEPARATOR_3, 1);

        // check for identical files
        Collection<Entry<String, String>> same = CollectionUtils.intersection(c1.entrySet(), c2.entrySet());

        if (same != null && !same.isEmpty()) {
            log("these files are identical", 0);
            for (Entry<String, String> entry : same) {
                log("[1=2]: " + entry.getKey());
            }
        }

        // check for files that exist in one archive only (exclusive)
        final Collection<String> only1 = CollectionUtils.subtract(c1.keySet(), c2.keySet());

        if (!only1.isEmpty()) {
            log("these files only exist in " + cfgFile1.getName(), 0);
            for (String fn : only1) {
                log("[1]: " + fn);
            }
        }

        Collection<String> only2 = CollectionUtils.subtract(c2.keySet(), c1.keySet());

        if (!only2.isEmpty()) {
            log("these files only exist in " + cfgFile2.getName(), 0);
            for (String fn : only2) {
                log("[2]: " + fn);
            }
        }

        // check for files that exist in both archives but are different
        // content-wise
        Collection<Entry<String, String>> difference1 = CollectionUtils.subtract(c1.entrySet(), c2.entrySet());

        @SuppressWarnings("unchecked")
        Collection<Entry<String, String>> difference = CollectionUtils.select(difference1, new Predicate<Object>() {

            public boolean evaluate(Object object) {
                Entry<String, String> entry = (Entry<String, String>) object;
                if (only1.contains(entry.getKey())) {
                    return false;
                } else {
                    return true;
                }
            }
        });

        if (!difference.isEmpty()) {
            log("these files are different", 0);
            for (Entry<String, String> entry : difference) {
                log("[1<>2]: " + entry.getKey());
            }

            // analyze the differences
            for (Entry<String, String> entry : difference) {

                log(OUTPUT_SEPARATOR_2, 0);

                // file #1
                TFileInputStream in1 = null;
                TFile f1 = new TFile(cfgFile1.getAbsoluteFile() + URL_SEPARATOR + entry.getKey());
                try {
                    in1 = new TFileInputStream(f1);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                log("loading file '" + f1.getAbsolutePath() + "'\n");

                // file #2
                TFileInputStream in2 = null;
                TFile f2 = new TFile(cfgFile2.getAbsoluteFile() + URL_SEPARATOR + entry.getKey());
                try {
                    in2 = new TFileInputStream(f2);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                log("loading file '" + f2.getAbsolutePath() + "'");

                compareConfig(in1, in2, entry.getKey());
            }
        }

    }

    private void compareConfig(InputStream in1, InputStream in2, String fileNameToBePrinted) {
        // reset result list
        List<String> diffs = new ArrayList<String>();
        boolean hasDifferences = false;
        try {
            hasDifferences = diff(in1, in2, diffs);
        } catch (IOException iox) {
            // TODO Auto-generated catch block
            iox.printStackTrace();
        } finally {
            try {
                in1.close();
            } catch (IOException iox) {
                throw new BuildException("Cannot close file #1!", iox);
            }
            try {
                in2.close();
            } catch (IOException iox) {
                throw new BuildException("Cannot close file #2!", iox);
            }
        }

        if (hasDifferences) {
            int i = 0;
            log(OUTPUT_SEPARATOR_1, 1);
            log("list of differences within " + fileNameToBePrinted, Project.MSG_WARN);
            log("doc1=" + cfgFile1.getName(), Project.MSG_WARN);
            log("doc2=" + cfgFile2.getName(), Project.MSG_WARN);

            for (String d : diffs) {
                log("[" + i + "] :" + d);
                i++;
            }
        } else {
            log("no differences found in " + fileNameToBePrinted + "!?!", 0);
        }
    }

    /**
     * Returns true if the given ZIP file entry is an XML file.
     * 
     * @param zipEntry
     * @return returns true if given ZIP file entry is an XML file
     */
    private boolean isXmlEntry(ZipEntry zipEntry) {
        if (!zipEntry.isDirectory()) {
            String[] mimeTypes = { ".xml", ".xcfg", ".xsd", ".wsdl", ".xslt", ".xsl" };
            String lowerCaseName = zipEntry.getName().toLowerCase();
            for (String mimeType : mimeTypes) {
                if (lowerCaseName.endsWith(mimeType))
                    return true;
            }
        }
        return false;
    }

    /**
     * Check the file name extension of the ZIP file entry.
     * 
     * @param zipEntry
     * @return returns true if given ZIP file entry is a ZIP file
     */
    private boolean isZipEntry(ZipEntry zipEntry) {
        if (!zipEntry.isDirectory()) {
            String[] mimeTypes = { ".zip" };
            String lowerCaseName = zipEntry.getName().toLowerCase();
            for (String mimeType : mimeTypes) {
                if (lowerCaseName.endsWith(mimeType))
                    return true;
            }
        }
        return false;
    }

    /**
     * Check if the given XML document is a DataPower configuration file.
     * 
     * @param doc XML document
     * @return returns true if the given XML document contains a 'datapower-configuration' element
     */
    private boolean isDataPowerConfig(Document doc) {
        if (doc != null && doc.getElementsByTagName("datapower-configuration") != null
                && doc.getElementsByTagName("datapower-configuration").getLength() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether given XML node is a whitespace node i.e. does not contain any data and therefore can be ignored.
     * 
     * @param n
     * @return
     */
    private boolean isWhitespaceNode(Node n) {
        if (n != null && n.getNodeType() == Node.TEXT_NODE) {
            String val = n.getNodeValue();
            return val.trim().length() == 0;
        } else {
            return false;
        }
    }

    /**
     * Check if the given file is an archive (ZIP) file.
     * 
     * @param f
     * @return
     */
    private boolean isArchiveFile(File f) {
        if (f == null) {
            throw new BuildException("File has not been specified!");
        }

        if (f.isDirectory()) {
            return false;
        }
        if (!f.canRead()) {
            throw new BuildException("Cannot read file " + f.getAbsolutePath());
        }
        if (f.length() < 4) {
            return false;
        }
        ZipFile zf = null;
        try {
            zf = new ZipFile(f);
        } catch (ZipException zx) {
            log("File is not a ZipFile - " + zx.getLocalizedMessage());
        } catch (IOException iox) {
            throw new BuildException("Cannot read file " + f.getAbsolutePath(), iox);
        }

        return (zf == null ? false : true);

    }

    private boolean diff(InputStream is1, InputStream is2, List<String> diffs) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);

        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new BuildException("Failed to instantiate a new DocuementBuilder : " + e.getLocalizedMessage());
        }

        Document doc1;
        try {
            doc1 = db.parse(is1);
        } catch (SAXException e) {
            throw new BuildException("Cannot parse InputStream #1!", e, getLocation());
        }
        Document doc2;
        try {
            doc2 = db.parse(is2);
        } catch (SAXException e) {
            throw new BuildException("Cannot parse InputStream #2!", e, getLocation());
        }

        doc1.normalizeDocument();
        doc2.normalizeDocument();

        log("doc1: " + doc1.getFirstChild().getNodeName() + " encoding: " + doc1.getXmlEncoding(), Project.MSG_VERBOSE);
        log("doc2: " + doc2.getFirstChild().getNodeName() + " encoding: " + doc2.getXmlEncoding(), Project.MSG_VERBOSE);

        if (isDataPowerConfig(doc1) && isDataPowerConfig(doc2)) {
            if (includeExportDetails) {
                NodeList nl1 = doc1.getElementsByTagName("export-details");
                NodeList nl2 = doc2.getElementsByTagName("export-details");
                if (nl1 != null && nl1.getLength() == 1 && nl2 != null && nl2.getLength() == 1) {
                    log("comparing export details section ...");
                    diff(nl1.item(0), nl2.item(0), diffs);
                } else {
                    log("no export details section found", 1);
                }
            }

            if (includeFiles) {
                NodeList nl1 = doc1.getElementsByTagName("files");
                NodeList nl2 = doc2.getElementsByTagName("files");
                if (nl1 != null && nl1.getLength() == 1 && nl2 != null && nl2.getLength() == 1) {
                    log("comparing files section ...");
                    diffDataPowerFiles(nl1.item(0), nl2.item(0), diffs);
                } else {
                    log("no files section found", 1);
                }
            }

            NodeList nl1 = doc1.getElementsByTagName("configuration");
            NodeList nl2 = doc2.getElementsByTagName("configuration");
            if (nl1 != null && nl1.getLength() == 1 && nl2 != null && nl2.getLength() == 1) {
                log("comparing configuration section ...");
                diffDataPowerConfiguration(nl1.item(0), nl2.item(0), diffs);
            } else {
                log("no configuration section found", 1);
            }
            return diffs.size() > 0;
        } else {
            log("the files to be compared are not DataPower configuration files");
            return diff(doc1, doc2, diffs);
        }

    }

    /**
     * Generate a list of differences for the two given nodes.
     * 
     * @param node1 XML node #1
     * @param node2 XML node #2
     * @param diffs list to store the found differences
     * @return returns true if the two nodes have differences
     */
    private boolean diff(Node node1, Node node2, List<String> diffs) {

        if (diffNodeExists(node1, node2, diffs)) {
            return true;
        }

        if (nodeTypeDiff) {
            diffNodeType(node1, node2, diffs);
        }

        if (nodeValueDiff) {
            diffNodeValue(node1, node2, null, diffs);
        }

        diffAttributes(node1, node2, diffs);
        diffNodes(node1, node2, diffs);

        return diffs.size() > 0;
    }

    /**
     * Generate a list of differences for the two given DataPower configuration sections.
     * 
     * @param node1 <configuration> node of the first XML DataPower configuration file
     * @param node2 <configuration> node of the second XML DataPower configuration file
     * @param diffs list to store the found differences
     * @return returns true if the two nodes have differences
     */
    private boolean diffDataPowerConfiguration(Node node1, Node node2, List<String> diffs) {

        if (node1 == node2) {
            throw new RuntimeException("Fatal Exception: node1 and node2 are the same instance!");
        }

        if (diffDataPowerNodeExists(node1, node2, diffs)) {
            return true;
        }

        if (isWhitespaceNode(node1) && isWhitespaceNode(node2)) {
            return false;
        }

        if (nodeTypeDiff) {
            diffNodeType(node1, node2, diffs);
        }

        if (nodeValueDiff) {
            diffDataPowerNodeValue(node1, node2, diffs);
        }

        diffAttributes(node1, node2, diffs);
        diffDataPowerNodes(node1, node2, diffs);

        return diffs.size() > 0;
    }

    /**
     * Generate a list of differences for the two given DataPower files sections.
     * 
     * @param node1 <files> or <file> node of a XML DataPower configuration file
     * @param node2 <files> or <file> node of a XML DataPower configuration file
     * @param diffs list to store the found differences
     * @return returns true if the two nodes have differences
     */
    private boolean diffDataPowerFiles(Node node1, Node node2, List<String> diffs) {

        if (diffDataPowerFileNodeExists(node1, node2, diffs)) {
            return true;
        }

        if (isWhitespaceNode(node1) && isWhitespaceNode(node2)) {
            return false;
        }

        diffFileAttributes(node1, node2, diffs);
        diffDataPowerFileNodes(node1, node2, diffs);

        return diffs.size() > 0;
    }

    /**
     * Diff the nodes
     */
    private boolean diffNodes(Node node1, Node node2, List<String> diffs) {
        // Sort by Name
        Map<String, Node> children1 = new LinkedHashMap<String, Node>();
        for (Node child1 = node1.getFirstChild(); child1 != null; child1 = child1.getNextSibling()) {
            if (isWhitespaceNode(child1)) {
                continue;
            }
            children1.put(child1.getNodeName(), child1);
        }

        // Sort by Name
        Map<String, Node> children2 = new LinkedHashMap<String, Node>();
        for (Node child2 = node2.getFirstChild(); child2 != null; child2 = child2.getNextSibling()) {
            if (isWhitespaceNode(child2)) {
                continue;
            }
            children2.put(child2.getNodeName(), child2);
        }

        // Diff all the children1
        for (Node child1 : children1.values()) {
            Node child2 = children2.remove(child1.getNodeName());
            diff(child1, child2, diffs);
        }

        // Diff all the children2 left over
        for (Node child2 : children2.values()) {
            Node child1 = children1.get(child2.getNodeName());
            diff(child1, child2, diffs);
        }

        return diffs.size() > 0;
    }

    /**
     * Diff two DataPower configuration nodes
     */
    private boolean diffDataPowerNodes(Node node1, Node node2, List<String> diffs) {
        // Sort by Name
        Map<String, Node> children1 = new LinkedHashMap<String, Node>();
        for (Node child1 = node1.getFirstChild(); child1 != null; child1 = child1.getNextSibling()) {
            if (isWhitespaceNode(child1)) {
                continue;
            }
            children1.put(child1.getNodeName() + getAttributeValue(child1, DP_ATTRIBUTE.name, false), child1);
        }

        // Sort by Name
        Map<String, Node> children2 = new LinkedHashMap<String, Node>();
        for (Node child2 = node2.getFirstChild(); child2 != null; child2 = child2.getNextSibling()) {
            if (isWhitespaceNode(child2)) {
                continue;
            }
            children2.put(child2.getNodeName() + getAttributeValue(child2, DP_ATTRIBUTE.name, false), child2);
        }

        // Diff all the children1
        for (Node child1 : children1.values()) {
            Node child2 = children2.remove(child1.getNodeName() + getAttributeValue(child1, DP_ATTRIBUTE.name, false));
            diffDataPowerConfiguration(child1, child2, diffs);
        }

        // Diff all the children2 left over
        for (Node child2 : children2.values()) {
            Node child1 = children1.get(child2.getNodeName() + getAttributeValue(child2, DP_ATTRIBUTE.name, false));
            diffDataPowerConfiguration(child1, child2, diffs);
        }

        return diffs.size() > 0;
    }

    /**
     * Diff two DataPower file nodes
     */
    private boolean diffDataPowerFileNodes(Node node1, Node node2, List<String> diffs) {
        // Sort by Name
        Map<String, Node> children1 = new LinkedHashMap<String, Node>();
        for (Node child1 = node1.getFirstChild(); child1 != null; child1 = child1.getNextSibling()) {
            if (isWhitespaceNode(child1)) {
                continue;
            }
            children1.put(child1.getNodeName() + getAttributeValue(child1, DP_ATTRIBUTE.name, false), child1);
        }

        // Sort by Name
        Map<String, Node> children2 = new LinkedHashMap<String, Node>();
        for (Node child2 = node2.getFirstChild(); child2 != null; child2 = child2.getNextSibling()) {
            if (isWhitespaceNode(child2)) {
                continue;
            }
            children2.put(child2.getNodeName() + getAttributeValue(child2, DP_ATTRIBUTE.name, false), child2);
        }

        // Diff all the children1
        for (Node child1 : children1.values()) {
            Node child2 = children2.remove(child1.getNodeName() + getAttributeValue(child1, DP_ATTRIBUTE.name, false));
            diffDataPowerFiles(child1, child2, diffs);
        }

        // Diff all the children2 left over
        for (Node child2 : children2.values()) {
            Node child1 = children1.get(child2.getNodeName() + getAttributeValue(child2, DP_ATTRIBUTE.name, false));
            diffDataPowerFiles(child1, child2, diffs);
        }

        return diffs.size() > 0;
    }

    /**
     * Diff attributes of two DataPower file nodes
     */
    private boolean diffFileAttributes(Node node1, Node node2, List<String> diffs) {
        // Sort by Name
        NamedNodeMap nodeMap1 = node1.getAttributes();
        Map<String, Node> attributes1 = new LinkedHashMap<String, Node>();
        for (int index = 0; nodeMap1 != null && index < nodeMap1.getLength(); index++) {
            attributes1.put(nodeMap1.item(index).getNodeName(), nodeMap1.item(index));
        }

        // Sort by Name
        NamedNodeMap nodeMap2 = node2.getAttributes();
        Map<String, Node> attributes2 = new LinkedHashMap<String, Node>();
        for (int index = 0; nodeMap2 != null && index < nodeMap2.getLength(); index++) {
            attributes2.put(nodeMap2.item(index).getNodeName(), nodeMap2.item(index));

        }

        // Diff all the attributes1
        for (Node attribute1 : attributes1.values()) {
            Node attribute2 = attributes2.remove(attribute1.getNodeName());
            diffDataPowerFileNodeAttributeValue(attribute1, attribute2, node1, diffs);
        }

        // Diff all the attributes2 left over
        for (Node attribute2 : attributes2.values()) {
            Node attribute1 = attributes1.get(attribute2.getNodeName());
            diffDataPowerFileNodeAttributeValue(attribute1, attribute2, node2, diffs);
        }

        return diffs.size() > 0;
    }

    /**
     * Diff attributes of two given XML nodes.
     * 
     * @param node1
     * @param node2
     * @param diffs
     * @return
     */
    private boolean diffAttributes(Node node1, Node node2, List<String> diffs) {
        // Sort by Name
        NamedNodeMap nodeMap1 = node1.getAttributes();
        Map<String, Node> attributes1 = new LinkedHashMap<String, Node>();
        for (int index = 0; nodeMap1 != null && index < nodeMap1.getLength(); index++) {
            attributes1.put(nodeMap1.item(index).getNodeName(), nodeMap1.item(index));
        }

        // Sort by Name
        NamedNodeMap nodeMap2 = node2.getAttributes();
        Map<String, Node> attributes2 = new LinkedHashMap<String, Node>();
        for (int index = 0; nodeMap2 != null && index < nodeMap2.getLength(); index++) {
            attributes2.put(nodeMap2.item(index).getNodeName(), nodeMap2.item(index));
        }

        // Diff all the attributes1
        for (Node attribute1 : attributes1.values()) {
            Node attribute2 = attributes2.remove(attribute1.getNodeName());
            diffNodeValue(attribute1, attribute2, node1, diffs);
        }

        // Diff all the attributes2 left over
        for (Node attribute2 : attributes2.values()) {
            Node attribute1 = attributes1.get(attribute2.getNodeName());
            diffNodeValue(attribute1, attribute2, node2, diffs);
        }

        return diffs.size() > 0;
    }

    /**
     * Check that the nodes exist
     */
    private boolean diffNodeExists(Node node1, Node node2, List<String> diffs) {
        if (node1 == null && node2 == null) {
            diffs.add(getPath(node2) + ":missing nodes " + node1 + "!=" + node2 + "\n");
            return true;
        }

        if (node1 == null && node2 != null) {
            diffs.add(getPath(node2) + ":missing in doc1 " + node1 + "!=" + node2.getNodeName());
            return true;
        }

        if (node1 != null && node2 == null) {
            diffs.add(getPath(node1) + ":missing in doc2 " + node1.getNodeName() + "!=" + node2);
            return true;
        }

        return false;
    }

    /**
     * Check that the DataPower nodes exist in both files.
     */
    private boolean diffDataPowerNodeExists(Node node1, Node node2, List<String> diffs) {
        if (node1 == null && node2 == null) {
            log("both nodes are null!", Project.MSG_ERR);
            return false;
        }

        if (node1 == null && node2 != null) {
            if (!includeIntrinsic && getAttributeValue(node2, DP_ATTRIBUTE.intrinsic, false).length() == 0) {
                diffs.add(getPath(node2) + ":missing in doc1 " + node2.getNodeName() + "[name="
                        + getAttributeValue(node2, DP_ATTRIBUTE.name, true) + "]");
            }
            return true;
        }

        if (node1 != null && node2 == null) {
            if (!includeIntrinsic && getAttributeValue(node1, DP_ATTRIBUTE.intrinsic, false).length() == 0) {
                diffs.add(getPath(node1) + ":missing in doc2 " + node1.getNodeName() + "[name="
                        + getAttributeValue(node1, DP_ATTRIBUTE.name, true) + "]");
            }
            return true;
        }

        return false;
    }

    /**
     * Check that the DataPower file node exist in both configuration files.
     */
    private boolean diffDataPowerFileNodeExists(Node node1, Node node2, List<String> diffs) {
        if (node1 == null && node2 == null) {
            log("both nodes are null!", Project.MSG_ERR);
            return false;
        }

        if (node1 == null && node2 != null) {
            if (!includeIntrinsic && getAttributeValue(node2, DP_ATTRIBUTE.intrinsic, false).length() == 0) {
                diffs.add(node2.getNodeName() + " @name=" + getAttributeValue(node2, DP_ATTRIBUTE.name, true)
                        + " missing in doc1");
            }
            return true;
        }

        if (node1 != null && node2 == null) {
            if (!includeIntrinsic && getAttributeValue(node1, DP_ATTRIBUTE.intrinsic, false).length() == 0) {
                diffs.add(node1.getNodeName() + " @name=" + getAttributeValue(node1, DP_ATTRIBUTE.name, true)
                        + " missing in doc2");
            }
            return true;
        }

        return false;
    }

    /**
     * Diff the Node Type
     */
    private boolean diffNodeType(Node node1, Node node2, List<String> diffs) {
        if (node1.getNodeType() != node2.getNodeType()) {
            diffs.add(getPath(node1) + ":type " + node1.getNodeType() + "!=" + node2.getNodeType());
            return true;
        }
        return false;
    }

    /**
     * Diff the Node Values.
     * 
     * Provide the parent node if you compare two attribute nodes.
     */
    private boolean diffNodeValue(Node node1, Node node2, Node parent, List<String> diffs) {

        if (node1 == null) {
            throw new BuildException("One of the configuration files seems to be invalid. Check '"
                    + (parent != null
                            ? getPath(parent) + getPath(node2) + "[name="
                                    + getAttributeValue(parent, DP_ATTRIBUTE.name, true) + "]"
                            : getPath(node2))
                    + "'!", getLocation());
        }

        if (node2 == null) {
            throw new BuildException("One of the configuration files seems to be invalid. Check '"
                    + (parent != null
                            ? getPath(parent) + getPath(node1) + "[name="
                                    + getAttributeValue(parent, DP_ATTRIBUTE.name, true) + "]"
                            : getPath(node1))
                    + "'!", getLocation());
        }

        if (node1.getNodeValue() == null && node2.getNodeValue() == null) {
            return false;
        }

        if (node1.getNodeValue() == null && node2.getNodeValue() != null) {
            diffs.add((parent == null ? getPath(node1) : getPath(parent) + "/@" + node1.getNodeName()) + ":value "
                    + node1 + "!=" + node2.getNodeValue());
            return true;
        }

        if (node1.getNodeValue() != null && node2.getNodeValue() == null) {
            diffs.add((parent == null ? getPath(node1) : getPath(parent) + "/@" + node1.getNodeName()) + ":value "
                    + node1.getNodeValue() + "!=" + node2);
            return true;
        }

        if (!node1.getNodeValue().equals(node2.getNodeValue())) {
            diffs.add((parent == null ? getPath(node1) : getPath(parent) + "/@" + node1.getNodeName()) + ":value "
                    + node1.getNodeValue() + "!=" + node2.getNodeValue());
            return true;
        }

        return false;
    }

    /**
     * Diff the Node Value
     */
    private boolean diffDataPowerNodeValue(Node node1, Node node2, List<String> diffs) {
        if (node1.getNodeValue() == null && node2.getNodeValue() == null) {
            return false;
        }

        if (node1.getNodeValue() == null && node2.getNodeValue() != null) {
            diffs.add(getPath(node1) + "[name=" + getAttributeValue(node1, DP_ATTRIBUTE.name, true) + "]" + ":value "
                    + node1 + "!=" + node2.getNodeValue());
            return true;
        }

        if (node1.getNodeValue() != null && node2.getNodeValue() == null) {
            diffs.add(getPath(node1) + "[name=" + getAttributeValue(node1, DP_ATTRIBUTE.name, true) + "]" + ":value "
                    + node1.getNodeValue() + "!=" + node2);
            return true;
        }

        if (!node1.getNodeValue().equals(node2.getNodeValue())) {
            diffs.add(getPath(node1) + "[name=" + getAttributeValue(node1, DP_ATTRIBUTE.name, true) + "]" + ":value "
                    + node1.getNodeValue() + "!=" + node2.getNodeValue());
            return true;
        }

        return false;
    }

    private boolean diffDataPowerFileNodeAttributeValue(Node node1, Node node2, Node parent, List<String> diffs) {
        if (node1.getNodeValue() == null && node2.getNodeValue() == null) {
            return false;
        }

        if (node1.getNodeValue() == null && node2.getNodeValue() != null) {
            diffs.add("file @name=" + getAttributeValue(parent, DP_ATTRIBUTE.name, true) + ", @" + node1.getNodeName()
                    + ":value " + node1 + "!=" + node2.getNodeValue());
            return true;
        }

        if (node1.getNodeValue() != null && node2.getNodeValue() == null) {
            diffs.add("file @name=" + getAttributeValue(parent, DP_ATTRIBUTE.name, true) + ", @" + node1.getNodeName()
                    + ":value " + node1.getNodeValue() + "!=" + node2);
            return true;
        }

        if (!node1.getNodeValue().equals(node2.getNodeValue())) {
            diffs.add("file @name=" + getAttributeValue(parent, DP_ATTRIBUTE.name, true) + ", @" + node1.getNodeName()
                    + ":value " + node1.getNodeValue() + "!=" + node2.getNodeValue());
            return true;
        }

        return false;
    }

    /**
     * Get the node path
     */
    private String getPath(Node node) {
        StringBuilder path = new StringBuilder();

        do {
            path.insert(0, node.getNodeName());
            path.insert(0, URL_SEPARATOR);
        } while ((node = node.getParentNode()) != null);

        return path.toString();
    }

    /**
     * Get the value of the attribute with the given name within the given node.
     * 
     * @param node
     * @param attributeName
     * @return
     */
    private String getAttributeValue(Node node, DP_ATTRIBUTE attr, boolean inherit) {
        String value = "";
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap != null) {
            Node attribute = nodeMap.getNamedItem(attr.name());
            if (attribute != null) {
                value = attribute.getNodeValue();
            }
        }

        if (inherit && value.length() == 0) {
            value = getAttributeValue(node.getParentNode(), attr, inherit);
            value = "<" + value;
        }

        return value;
    }

}
