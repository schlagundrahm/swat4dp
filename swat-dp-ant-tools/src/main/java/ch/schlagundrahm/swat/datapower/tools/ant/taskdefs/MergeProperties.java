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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Apache Ant task to merge multiple properties files with optional template-based ordering.
 * 
 * Features:
 * - Merges all .properties files from a source directory
 * - Removes duplicate entries (keeps last occurrence)
 * - Optional template-based ordering to maintain consistent structure
 * - Optional alphabetical sorting as fallback
 * - Optional exclusion of properties by regex pattern
 * - Generates header with timestamp and source information
 */
public class MergeProperties extends Task {

    private File srcDir;
    private File dstFile;
    private File templateFile;
    private boolean sort = false;
    private String excludePattern;

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
            throw new BuildException("srcDir must be an existing directory: " + srcDir);
        }
        if (dstFile == null) {
            throw new BuildException("dstFile attribute is required");
        }

        try {
            log("Merging properties files from: " + srcDir.getAbsolutePath());
            
            // Collect all .properties files
            List<File> propertyFiles = findPropertyFiles(srcDir);
            if (propertyFiles.isEmpty()) {
                log("Warning: No .properties files found in " + srcDir);
                return;
            }
            
            log("Found " + propertyFiles.size() + " properties file(s)");
            
            // Load and merge properties
            LinkedHashMap<String, String> mergedProps = mergePropertyFiles(propertyFiles);
            log("Merged " + mergedProps.size() + " unique properties");
            
            // Apply exclusion pattern if specified
            if (excludePattern != null && !excludePattern.trim().isEmpty()) {
                Pattern pattern = Pattern.compile(excludePattern);
                int beforeSize = mergedProps.size();
                mergedProps.keySet().removeIf(key -> pattern.matcher(key).matches());
                int excluded = beforeSize - mergedProps.size();
                if (excluded > 0) {
                    log("Excluded " + excluded + " properties matching pattern: " + excludePattern);
                }
            }
            
            // Order properties
            LinkedHashMap<String, String> orderedProps;
            if (templateFile != null && templateFile.exists()) {
                log("Ordering properties according to template: " + templateFile.getName());
                orderedProps = orderByTemplate(mergedProps, templateFile);
            } else if (sort) {
                log("Sorting properties alphabetically");
                orderedProps = sortProperties(mergedProps);
            } else {
                orderedProps = mergedProps;
            }
            
            // Write output file
            writePropertiesFile(orderedProps, dstFile);
            log("Merged properties written to: " + dstFile.getAbsolutePath());
            
        } catch (IOException e) {
            throw new BuildException("Error merging properties: " + e.getMessage(), e);
        }
    }

    /**
     * Find all .properties files in the source directory.
     */
    private List<File> findPropertyFiles(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".properties"));
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> result = Arrays.asList(files);
        Collections.sort(result, Comparator.comparing(File::getName));
        return result;
    }

    /**
     * Merge multiple properties files, keeping last occurrence of duplicates.
     */
    private LinkedHashMap<String, String> mergePropertyFiles(List<File> files) throws IOException {
        LinkedHashMap<String, String> merged = new LinkedHashMap<>();
        
        for (File file : files) {
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            }
            
            // Add/override properties (last file wins for duplicates)
            for (String key : props.stringPropertyNames()) {
                merged.put(key, props.getProperty(key));
            }
        }
        
        return merged;
    }

    /**
     * Sort properties alphabetically by key.
     */
    private LinkedHashMap<String, String> sortProperties(LinkedHashMap<String, String> props) {
        return props.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * Order properties according to a template file.
     *
     * <p>Template keys may contain the literal {@code {index}} placeholder. Consecutive
     * template lines that all contain {@code {index}} form an <em>indexed block</em>.
     * A block is expanded index-first: all fields for index 1, then all fields for
     * index 2, etc.  For example, the template block:
     * <pre>
     *   fsh.{index}.name=
     *   fsh.{index}.host=
     *   fsh.{index}.port=
     * </pre>
     * with three FSH instances produces:
     * <pre>
     *   fsh.1.name=...  fsh.1.host=...  fsh.1.port=...
     *   fsh.2.name=...  fsh.2.host=...  fsh.2.port=...
     *   fsh.3.name=...  fsh.3.host=...  fsh.3.port=...
     * </pre>
     */
    private LinkedHashMap<String, String> orderByTemplate(
            LinkedHashMap<String, String> props, File template) throws IOException {

        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        Set<String> processedKeys = new HashSet<>();

        List<String> templateLines = Files.readAllLines(template.toPath(), StandardCharsets.UTF_8);
        int i = 0;
        while (i < templateLines.size()) {
            String trimmed = templateLines.get(i).trim();

            // Skip comments and blank lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++;
                continue;
            }

            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex > 0) {
                String key = trimmed.substring(0, equalsIndex).trim();

                if (key.contains("{index}")) {
                    // Collect the full run of consecutive {index} lines starting at i
                    List<String> blockKeys = new ArrayList<>();
                    int j = i;
                    while (j < templateLines.size()) {
                        String bt = templateLines.get(j).trim();
                        // A blank line or comment breaks the block
                        if (bt.isEmpty() || bt.startsWith("#")) {
                            break;
                        }
                        int eq = bt.indexOf('=');
                        if (eq <= 0) {
                            break;
                        }
                        String bk = bt.substring(0, eq).trim();
                        if (!bk.contains("{index}")) {
                            break;
                        }
                        blockKeys.add(bk);
                        j++;
                    }

                    // Determine the union of all index values present across the block
                    SortedSet<Integer> allIndices = new TreeSet<>();
                    for (String bk : blockKeys) {
                        for (Map.Entry<Integer, String> e : findIndexedEntries(props, bk)) {
                            allIndices.add(e.getKey());
                        }
                    }

                    // Emit: for each index, emit all fields that exist
                    for (int idx : allIndices) {
                        for (String bk : blockKeys) {
                            String concrete = bk.replace("{index}", String.valueOf(idx));
                            if (props.containsKey(concrete)) {
                                ordered.put(concrete, props.get(concrete));
                                processedKeys.add(concrete);
                            }
                        }
                    }

                    i = j; // skip past all consumed block lines
                } else {
                    // Exact match — non-indexed key
                    if (props.containsKey(key)) {
                        ordered.put(key, props.get(key));
                        processedKeys.add(key);
                    }
                    i++;
                }
            } else {
                i++;
            }
        }

        // Append properties not covered by the template
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (!processedKeys.contains(entry.getKey())) {
                ordered.put(entry.getKey(), entry.getValue());
            }
        }

        return ordered;
    }

    /**
     * Find all (index, key) pairs in {@code props} that match a template key containing
     * {@code {index}}, sorted in ascending numeric index order.
     *
     * @param props       the merged properties map
     * @param templateKey the template key, e.g. {@code fsh.{index}.host}
     * @return list of (numericIndex, concreteKey) entries in ascending index order
     */
    private List<Map.Entry<Integer, String>> findIndexedEntries(
            LinkedHashMap<String, String> props, String templateKey) {
        // Pattern.quote wraps in \Q...\E; splice (\d+) capture group in place of {index}
        String escapedTemplate = Pattern.quote(templateKey).replace("{index}", "\\E(\\d+)\\Q");
        Pattern indexPattern = Pattern.compile(escapedTemplate);

        List<Map.Entry<Integer, String>> matches = new ArrayList<>();
        for (String key : props.keySet()) {
            Matcher m = indexPattern.matcher(key);
            if (m.matches()) {
                matches.add(new AbstractMap.SimpleEntry<>(Integer.parseInt(m.group(1)), key));
            }
        }
        matches.sort(Map.Entry.comparingByKey());
        return matches;
    }

    /**
     * Convenience wrapper: returns only the concrete keys, in ascending index order.
     *
     * @param props       the merged properties map
     * @param templateKey the template key, e.g. {@code fsh.{index}.host}
     * @return concrete keys in ascending numeric index order
     */
    private List<String> findIndexedKeys(LinkedHashMap<String, String> props, String templateKey) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, String> e : findIndexedEntries(props, templateKey)) {
            result.add(e.getValue());
        }
        return result;
    }

    /**
     * Write properties to file with header.
     */
    private void writePropertiesFile(LinkedHashMap<String, String> props, File file) throws IOException {
        // Create parent directories if needed
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            
            // Write header
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("# Merged properties file\n");
            writer.write("# Generated: " + sdf.format(new Date()) + "\n");
            writer.write("# Source: " + srcDir.getAbsolutePath() + "\n");
            if (templateFile != null && templateFile.exists()) {
                writer.write("# Template: " + templateFile.getAbsolutePath() + "\n");
            }
            writer.write("\n");
            
            // If using template, include section headers
            if (templateFile != null && templateFile.exists()) {
                writeWithTemplateSections(props, templateFile, writer);
            } else {
                // Write properties without sections
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
                }
            }
        }
    }

    /**
     * Write properties with section headers from template.
     *
     * <p>Consecutive template lines that all contain {@code {index}} form an indexed block
     * that is expanded index-first (all fields for index&nbsp;1, then index&nbsp;2, …).
     * Comment and blank lines that precede or follow a block are written as-is; they do
     * not break a block that has already started, but they do end a block before it starts.
     */
    private void writeWithTemplateSections(
            LinkedHashMap<String, String> props, File template, BufferedWriter writer) throws IOException {

        List<String> templateLines = Files.readAllLines(template.toPath(), StandardCharsets.UTF_8);
        Set<String> writtenKeys = new HashSet<>();

        int i = 0;
        while (i < templateLines.size()) {
            String line = templateLines.get(i);
            String trimmed = line.trim();

            // Pass through comment and blank lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                writer.write(line + "\n");
                i++;
                continue;
            }

            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex <= 0) {
                writer.write(line + "\n");
                i++;
                continue;
            }

            String key = trimmed.substring(0, equalsIndex).trim();

            if (key.contains("{index}")) {
                // Collect the full run of consecutive {index} lines (blank/comment lines break it)
                List<String> blockKeys = new ArrayList<>();
                int j = i;
                while (j < templateLines.size()) {
                    String bt = templateLines.get(j).trim();
                    if (bt.isEmpty() || bt.startsWith("#")) {
                        break;
                    }
                    int eq = bt.indexOf('=');
                    if (eq <= 0) {
                        break;
                    }
                    String bk = bt.substring(0, eq).trim();
                    if (!bk.contains("{index}")) {
                        break;
                    }
                    blockKeys.add(bk);
                    j++;
                }

                // Union of all index values present for any field in the block
                SortedSet<Integer> allIndices = new TreeSet<>();
                for (String bk : blockKeys) {
                    for (Map.Entry<Integer, String> e : findIndexedEntries(props, bk)) {
                        allIndices.add(e.getKey());
                    }
                }

                // Emit: for each index, write all fields that are present
                for (int idx : allIndices) {
                    for (String bk : blockKeys) {
                        String concrete = bk.replace("{index}", String.valueOf(idx));
                        if (props.containsKey(concrete)) {
                            writer.write(concrete + "=" + props.get(concrete) + "\n");
                            writtenKeys.add(concrete);
                        }
                    }
                }

                i = j; // skip past all consumed block lines
            } else {
                // Non-indexed key
                if (props.containsKey(key)) {
                    writer.write(key + "=" + props.get(key) + "\n");
                    writtenKeys.add(key);
                }
                i++;
            }
        }

        // Write any remaining properties not covered by the template
        List<String> remainingKeys = props.keySet().stream()
            .filter(k -> !writtenKeys.contains(k))
            .collect(Collectors.toList());

        if (!remainingKeys.isEmpty()) {
            writer.write("\n# Additional properties not in template\n");
            for (String k : remainingKeys) {
                writer.write(k + "=" + props.get(k) + "\n");
            }
        }
    }

    // Setters for Ant attributes

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDstFile(File dstFile) {
        this.dstFile = dstFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public void setExcludePattern(String excludePattern) {
        this.excludePattern = excludePattern;
    }
}

// Made with Bob
