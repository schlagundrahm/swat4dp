/*
* (C) Copyright IBM Corp. 2026.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package ch.schlagundrahm.swat.datapower.tools.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Apache Ant task to tokenize DataPower service configurations.
 *
 * Reads tokenization rules from a properties file and processes all .xcfg files
 * in a source directory:
 * 1. Tokenizes each XML configuration by replacing values with @token.name@ placeholders
 * 2. Generates for each .xcfg file a corresponding properties file with the extracted values
 *
 */
public class TokenizeService extends Task {

    private File srcDir;
    private File dstDir;
    private File propertiesDir;
    private File rulesFile;
    
    // Tokenization rules loaded from properties file
    private Map<String, TokenRule> rules = new LinkedHashMap<>();
    
    /**
     * Represents a tokenization rule
     */
    private static class TokenRule {
        String xpath;           // e.g., "MultiProtocolGateway.FrontProtocol"
        String tokenName;       // e.g., "fsh.{index}.name"
        boolean isAttribute;    // true if xpath ends with @attribute
        boolean hasIndex;       // true if tokenName contains {index}
        
        String[] pathParts;     // Split xpath for matching
        String attributeName;   // If isAttribute, the attribute name
        
        TokenRule(String xpath, String tokenName) {
            this.xpath = xpath;
            this.tokenName = tokenName;
            this.hasIndex = tokenName.contains("{index}");
            
            // Check if this is an attribute rule
            if (xpath.contains(".@")) {
                this.isAttribute = true;
                int atIndex = xpath.lastIndexOf(".@");
                this.attributeName = xpath.substring(atIndex + 2);
                String elementPath = xpath.substring(0, atIndex);
                this.pathParts = elementPath.split("\\.");
            } else {
                this.isAttribute = false;
                this.pathParts = xpath.split("\\.");
            }
        }
        
        String getTokenName(int index) {
            if (hasIndex) {
                return tokenName.replace("{index}", String.valueOf(index));
            }
            return tokenName;
        }
    }

    /**
     * Main execution task.
     */
    @Override
    public void execute() throws BuildException {
        // Validate parameters
        if (srcDir == null) {
            throw new BuildException("srcDir attribute is required");
        }
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            throw new BuildException("Source directory does not exist: " + srcDir);
        }
        if (dstDir == null) {
            throw new BuildException("dstDir attribute is required");
        }
        if (propertiesDir == null) {
            throw new BuildException("propertiesDir attribute is required");
        }
        if (rulesFile == null) {
            throw new BuildException("rulesFile attribute is required");
        }
        if (!rulesFile.exists()) {
            throw new BuildException("Rules file does not exist: " + rulesFile);
        }

        try {
            log("Tokenizing service configurations from: " + srcDir.getAbsolutePath());
            log("Using rules from: " + rulesFile.getName());
            
            // Load tokenization rules
            loadRules();
            log("Loaded " + rules.size() + " tokenization rules");
            
            // Find all .xcfg files
            List<File> xcfgFiles = findXcfgFiles(srcDir);
            if (xcfgFiles.isEmpty()) {
                log("Warning: No .xcfg files found in " + srcDir);
                return;
            }
            
            log("Found " + xcfgFiles.size() + " .xcfg file(s) to process");
            
            // Create destination directories
            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }
            if (!propertiesDir.exists()) {
                propertiesDir.mkdirs();
            }
            
            // Process each file
            int processedCount = 0;
            int totalTokens = 0;
            for (File srcFile : xcfgFiles) {
                try {
                    log("Processing: " + srcFile.getName());
                    
                    // Create new token map for this file
                    Map<String, String> tokens = new LinkedHashMap<>();
                    
                    // Parse XML document
                    Document doc = parseXmlFile(srcFile);
                    
                    // Process document and tokenize
                    processDocument(doc, tokens);
                    
                    // Write tokenized XML to destination
                    File dstFile = new File(dstDir, srcFile.getName());
                    writeXmlFile(doc, dstFile);
                    
                    // Write properties file for this xcfg
                    String propertiesFileName = srcFile.getName().replace(".xcfg", ".properties");
                    File propertiesFile = new File(propertiesDir, propertiesFileName);
                    writePropertiesFile(propertiesFile, srcFile.getName(), tokens);
                    
                    log("  Extracted " + tokens.size() + " tokens -> " + propertiesFileName);
                    totalTokens += tokens.size();
                    processedCount++;
                } catch (Exception e) {
                    log("Error processing " + srcFile.getName() + ": " + e.getMessage());
                    throw new BuildException("Failed to process " + srcFile.getName(), e);
                }
            }
            
            log("Successfully tokenized " + processedCount + " file(s)");
            log("Total tokens extracted: " + totalTokens);
            
        } catch (Exception e) {
            throw new BuildException("Error tokenizing services: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find all .xcfg files in the source directory.
     */
    private List<File> findXcfgFiles(File dir) {
        List<File> xcfgFiles = new ArrayList<>();
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".xcfg"));
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            xcfgFiles.addAll(Arrays.asList(files));
        }
        return xcfgFiles;
    }

    /**
     * Load tokenization rules from properties file.
     */
    private void loadRules() throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(rulesFile)) {
            props.load(in);
        }
        
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            TokenRule rule = new TokenRule(key, value);
            rules.put(key, rule);
        }
    }

    /**
     * Parse XML file into DOM document.
     */
    private Document parseXmlFile(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    /**
     * Process the document and apply tokenization rules.
     */
    private void processDocument(Document doc, Map<String, String> tokens) {
        Element root = doc.getDocumentElement();
        
        // Find configuration element
        NodeList configNodes = root.getElementsByTagName("configuration");
        if (configNodes.getLength() > 0) {
            Element config = (Element) configNodes.item(0);
            
            // Process domain attribute if rule exists
            TokenRule domainRule = rules.get("configuration.@domain");
            if (domainRule != null) {
                String originalValue = config.getAttribute("domain");
                if (originalValue != null && !originalValue.isEmpty()) {
                    String tokenName = domainRule.getTokenName(1);
                    config.setAttribute("domain", "@" + tokenName + "@");
                    tokens.put(tokenName, originalValue);
                }
            }
            
            // Process child elements
            processChildren(config, new ArrayList<>(), tokens);
        }
    }

    /**
     * Recursively process child elements.
     */
    private void processChildren(Element parent, List<String> path, Map<String, String> tokens) {
        NodeList children = parent.getChildNodes();
        Map<String, Integer> elementCounts = new HashMap<>();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String elementName = element.getLocalName();
                if (elementName == null) {
                    elementName = element.getNodeName();
                }
                
                // Track element index for indexed rules
                int index = elementCounts.getOrDefault(elementName, 0) + 1;
                elementCounts.put(elementName, index);
                
                // Build current path
                List<String> currentPath = new ArrayList<>(path);
                currentPath.add(elementName);
                
                // Check for matching rules
                processElement(element, currentPath, index, tokens);
                
                // Recurse into children
                processChildren(element, currentPath, tokens);
            }
        }
    }

    /**
     * Process a single element against tokenization rules.
     */
    private void processElement(Element element, List<String> path, int index, Map<String, String> tokens) {
        String pathStr = String.join(".", path);
        
        // Check for text content rule
        TokenRule textRule = rules.get(pathStr);
        if (textRule != null) {
            String textContent = getElementText(element);
            if (textContent != null && !textContent.trim().isEmpty()) {
                String tokenName = textRule.getTokenName(index);
                setElementText(element, "@" + tokenName + "@");
                tokens.put(tokenName, textContent);
            }
        }
        
        // Check for attribute rules
        for (TokenRule rule : rules.values()) {
            if (rule.isAttribute && matchesPath(rule.pathParts, path)) {
                String attrValue = element.getAttribute(rule.attributeName);
                if (attrValue != null && !attrValue.isEmpty()) {
                    String tokenName = rule.getTokenName(index);
                    element.setAttribute(rule.attributeName, "@" + tokenName + "@");
                    tokens.put(tokenName, attrValue);
                }
            }
        }
    }

    /**
     * Check if a rule path matches the current element path.
     */
    private boolean matchesPath(String[] rulePath, List<String> elementPath) {
        if (rulePath.length != elementPath.size()) {
            return false;
        }
        for (int i = 0; i < rulePath.length; i++) {
            if (!rulePath[i].equals(elementPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get text content of an element (direct text nodes only).
     */
    private String getElementText(Element element) {
        StringBuilder text = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                text.append(node.getNodeValue());
            }
        }
        return text.toString().trim();
    }

    /**
     * Set text content of an element.
     */
    private void setElementText(Element element, String text) {
        // Remove existing text nodes
        NodeList children = element.getChildNodes();
        List<Node> toRemove = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                toRemove.add(node);
            }
        }
        for (Node node : toRemove) {
            element.removeChild(node);
        }
        
        // Add new text node
        if (text != null && !text.isEmpty()) {
            Text textNode = element.getOwnerDocument().createTextNode(text);
            element.appendChild(textNode);
        }
    }

    /**
     * Write XML document to file.
     */
    private void writeXmlFile(Document doc, File file) throws TransformerException, IOException {
        // Create parent directories if needed
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /**
     * Write properties file with extracted tokens.
     */
    private void writePropertiesFile(File propertiesFile, String sourceFileName, Map<String, String> tokens) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(propertiesFile), StandardCharsets.UTF_8))) {
            
            // Write header
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("# Auto-generated properties file from configurable tokenization\n");
            writer.write("# Generated: " + sdf.format(new Date()) + "\n");
            writer.write("# Source: " + sourceFileName + "\n");
            writer.write("# Rules: " + rulesFile.getName() + "\n");
            writer.write("\n");
            
            // Write tokens in order they were collected
            for (Map.Entry<String, String> entry : tokens.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        }
    }

    // Setters for Ant attributes

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDstDir(File dstDir) {
        this.dstDir = dstDir;
    }

    public void setPropertiesDir(File propertiesDir) {
        this.propertiesDir = propertiesDir;
    }

    public void setRulesFile(File rulesFile) {
        this.rulesFile = rulesFile;
    }
}

// Made with Bob
