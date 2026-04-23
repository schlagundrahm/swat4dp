# Properties File Merging Guide

## Overview

The `mergeproperties` Ant task combines multiple properties files into a single file with:
- **Deduplication**: Removes duplicate properties (keeps last occurrence)
- **Ordering**: Orders properties according to a template file
- **Filtering**: Excludes properties matching regex pattern
- **Comments**: Preserves section comments from template

## Usage

### Basic Merge (No Template)

Combines properties files and removes duplicates:

```xml
<mergeproperties
    srcDir="dist/tokenized/device-domain-properties"
    dstFile="dist/tokenized/device-domain.properties"
/>
```

### Merge with Alphabetical Sorting

```xml
<mergeproperties
    srcDir="dist/tokenized/device-domain-properties"
    dstFile="dist/tokenized/device-domain.properties"
    sort="true"
/>
```

### Merge with Template Ordering

Orders properties according to a template file:

```xml
<mergeproperties
    srcDir="dist/tokenized/device-domain-properties"
    dstFile="dist/tokenized/device-domain.properties"
    templateFile="${swat.dp.tools}/templates/properties-template.properties"
/>
```

### Merge with Property Exclusion

Excludes properties matching a regex pattern:

```xml
<mergeproperties
    srcDir="dist/tokenized/device-domain-properties"
    dstFile="dist/tokenized/device-domain.properties"
    templateFile="${swat.dp.tools}/templates/properties-template.properties"
    excludePattern="(service\.object\.name|service\.summary)"
/>
```

### Complete Example

```xml
<mergeproperties
    srcDir="dist/tokenized/device-domain-properties"
    dstFile="dist/tokenized/device-domain.properties"
    templateFile="${swat.dp.tools}/templates/properties-template.properties"
    excludePattern="(service\.object\.name|service\.summary|debug\.mode|debug\.history)"
    sort="false"
/>
```

## Task Attributes

| Attribute | Required | Default | Description |
|-----------|----------|---------|-------------|
| `srcDir` | Yes | - | Directory containing `.properties` files to merge |
| `dstFile` | Yes | - | Output file path for merged properties |
| `templateFile` | No | - | Template file defining property order and sections |
| `excludePattern` | No | - | Regex pattern for properties to exclude |
| `sort` | No | `false` | Sort properties alphabetically (if no template) |

## How It Works

### Step 1: File Discovery

All `.properties` files in `srcDir` are found and sorted alphabetically by filename:

```
srcDir/
  ├── 01-service.properties
  ├── 02-network.properties
  └── 03-overrides.properties
```

### Step 2: Merging

Properties are loaded in alphabetical order. Duplicate keys are overridden by later files:

```properties
# From 01-service.properties
service.state=enabled
local.port=8080

# From 02-network.properties
service.state=disabled  # Overrides previous value
remote.port=9443
```

**Result after merge:**
```properties
service.state=disabled  # Last occurrence wins
local.port=8080
remote.port=9443
```

### Step 3: Exclusion (if specified)

Properties matching `excludePattern` regex are removed:

```xml
excludePattern="(service\.summary|debug\..*)"
```

This excludes:
- `service.summary`
- `debug.mode`
- `debug.history`
- Any property starting with `debug.`

### Step 4: Ordering

**Without template**: Properties remain in merge order (or sorted if `sort="true"`)

**With template**: Properties are reordered according to template structure

**Template:**
```properties
# Service Configuration
service.state=
service.summary=

# Network Configuration
local.port=
remote.port=
```

**Output:**
```properties
# Merged properties file
# Generated: 2026-04-23 16:00:00
# Source: /path/to/srcDir
# Template: /path/to/template.properties

# Service Configuration
service.state=disabled

# Network Configuration
local.port=8080
remote.port=9443
```

## Template File Format

The template file defines:
1. **Order**: Properties appear in the order listed
2. **Comments**: Section headers are preserved
3. **Grouping**: Related properties grouped together

**Example Template:**

```properties
# ================================================================================================
# SERVICE CONFIGURATION
# ================================================================================================
service.object.name=
service.state=
service.summary=

# ================================================================================================
# NETWORK CONFIGURATION
# ================================================================================================
local.address=
local.port=
remote.address=
remote.port=

# ================================================================================================
# DEBUG SETTINGS
# ================================================================================================
debug.mode=
debug.history=
```

### Template Best Practices

1. **Group Related Properties**: Use section comments
2. **Include All Common Properties**: List all expected properties
3. **Leave Values Empty**: Template values are ignored
4. **Use Clear Section Headers**: Make structure obvious

## Exclusion Pattern Syntax

The `excludePattern` uses Java regex syntax:

### Simple Patterns

```xml
<!-- Exclude single property -->
excludePattern="service\.summary"

<!-- Exclude multiple specific properties -->
excludePattern="(service\.summary|debug\.mode)"
```

### Wildcard Patterns

```xml
<!-- Exclude all debug properties -->
excludePattern="debug\..*"

<!-- Exclude properties starting with 'temp' -->
excludePattern="temp.*"

<!-- Exclude indexed properties -->
excludePattern="fsh\.\d+\.name"
```

### Complex Patterns

```xml
<!-- Exclude service.summary and all debug properties -->
excludePattern="(service\.summary|debug\..*)"

<!-- Exclude properties ending with '.summary' -->
excludePattern=".*\.summary"
```

## Integration with Build Scripts

### Replace Concat Operation

**Before:**
```xml
<!-- Old approach: concat with filtering -->
<concat destfile="dist/tokenized/@{device}-@{domain}.properties">
    <fileset dir="dist/tokenized/@{device}-@{domain}-properties">
        <include name="*.properties"/>
    </fileset>
    <filterchain>
        <linecontainsregexp>
            <regexp pattern="="/>
        </linecontainsregexp>
        <linecontains negate="true" matchAny="true">
          <contains value="service.object.name="/>
          <contains value="service.summary="/>
        </linecontains>
    </filterchain>
</concat>
```

**After:**
```xml
<!-- New approach: mergeproperties task -->
<mergeproperties
    srcDir="dist/tokenized/@{device}-@{domain}-properties"
    dstFile="dist/tokenized/@{device}-@{domain}.properties"
    templateFile="${swat.dp.tools}/templates/properties-template.properties"
    excludePattern="(service\.object\.name|service\.summary)"
/>
```

### With Default Value Replacement

```xml
<!-- Merge properties -->
<mergeproperties
    srcDir="dist/tokenized/@{device}-@{domain}-properties"
    dstFile="dist/tokenized/@{device}-@{domain}.properties"
    templateFile="${swat.dp.tools}/templates/properties-template.properties"
    excludePattern="(service\.object\.name|service\.summary)"
/>

<!-- Apply default values -->
<replaceregexp file="dist/tokenized/@{device}-@{domain}.properties"
               match="^debug\.mode=.*$"
               replace="debug.mode=off"
               byline="true"/>
<replaceregexp file="dist/tokenized/@{device}-@{domain}.properties"
               match="^debug\.history=.*$"
               replace="debug.history=25"
               byline="true"/>
```

## Benefits

### 1. Deduplication

**Problem**: Multiple properties files contain the same property with different values.

**Solution**: Automatically removes duplicates, keeping the last occurrence.

**Example:**
```properties
# File 1
service.state=enabled

# File 2
service.state=disabled

# Result: service.state=disabled (last wins)
```

### 2. Consistent Ordering

**Problem**: Properties appear in random order, making files hard to read and compare.

**Solution**: Template-based ordering ensures consistent structure.

### 3. Easy Maintenance

**Problem**: Adding new properties requires updating multiple files.

**Solution**: Update template once, all merged files follow the same structure.

### 4. Better Readability

**Problem**: Related properties scattered throughout file.

**Solution**: Template groups related properties with section comments.

### 5. Flexible Filtering

**Problem**: Need to exclude certain properties from final output.

**Solution**: Regex-based exclusion pattern provides powerful filtering.

## Examples

### Example 1: Basic Merge

**Input Files:**

`service1.properties`:
```properties
service.state=enabled
local.port=8080
```

`service2.properties`:
```properties
remote.port=9443
service.state=disabled
```

**Command:**
```xml
<mergeproperties
    srcDir="properties"
    dstFile="merged.properties"
/>
```

**Output:**
```properties
# Merged properties file
# Generated: 2026-04-23 16:00:00
# Source: /path/to/properties

service.state=disabled
local.port=8080
remote.port=9443
```

### Example 2: Template-Based Ordering

**Template:**
```properties
# Service
service.state=
service.summary=

# Network
local.port=
remote.port=
```

**Command:**
```xml
<mergeproperties
    srcDir="properties"
    dstFile="merged.properties"
    templateFile="template.properties"
/>
```

**Output:**
```properties
# Merged properties file
# Generated: 2026-04-23 16:00:00
# Source: /path/to/properties
# Template: /path/to/template.properties

# Service
service.state=disabled

# Network
local.port=8080
remote.port=9443
```

### Example 3: With Exclusions

**Command:**
```xml
<mergeproperties
    srcDir="properties"
    dstFile="merged.properties"
    templateFile="template.properties"
    excludePattern="(service\.summary|debug\..*)"
/>
```

Properties containing "service.summary" or starting with "debug." are excluded.

### Example 4: Alphabetical Sorting

**Command:**
```xml
<mergeproperties
    srcDir="properties"
    dstFile="merged.properties"
    sort="true"
/>
```

**Output:**
```properties
# Merged properties file
# Generated: 2026-04-23 16:00:00
# Source: /path/to/properties

local.port=8080
remote.port=9443
service.state=disabled
```

## Troubleshooting

### Issue: Properties Not Ordered

**Cause**: Template file not found or path incorrect.

**Solution**: Verify template file exists:
```xml
<available file="${swat.dp.tools}/templates/properties-template.properties" 
           property="template.exists"/>
<echo message="Template exists: ${template.exists}"/>
```

### Issue: Duplicates Not Removed

**Cause**: Properties have different whitespace or formatting.

**Solution**: The task trims whitespace automatically. Check for special characters.

### Issue: Properties Missing

**Cause**: Excluded by `excludePattern`.

**Solution**: Review exclusion pattern:
```xml
<echo message="Exclude pattern: ${exclude.pattern}"/>
```

### Issue: Wrong Property Values

**Cause**: File processing order affects which value is kept.

**Solution**: Use numeric prefixes to control order:
```
01-base.properties
02-environment.properties
03-overrides.properties
```

## Advanced Usage

### Environment-Specific Templates

```xml
<property name="env" value="dev"/>
<mergeproperties
    srcDir="properties"
    dstFile="merged-${env}.properties"
    templateFile="templates/properties-template-${env}.properties"
/>
```

### Conditional Exclusions

```xml
<condition property="exclude.pattern" value="debug\..*" else="">
    <equals arg1="${env}" arg2="prod"/>
</condition>

<mergeproperties
    srcDir="properties"
    dstFile="merged.properties"
    excludePattern="${exclude.pattern}"
/>
```

### Multiple Merge Operations

```xml
<!-- Merge service properties -->
<mergeproperties
    srcDir="dist/service-properties"
    dstFile="dist/service.properties"
    templateFile="templates/service-template.properties"
/>

<!-- Merge device properties -->
<mergeproperties
    srcDir="dist/device-properties"
    dstFile="dist/device.properties"
    templateFile="templates/device-template.properties"
/>
```

## Technical Implementation

### Java Ant Task

The merge functionality is implemented as [`MergeProperties.java`](swat-dp-ant-tools/src/main/java/ch/schlagundrahm/swat/datapower/tools/ant/taskdefs/MergeProperties.java)

**Key Features:**
- Native Java processing (faster than XSLT)
- No external dependencies
- Type-safe implementation
- Seamless Ant integration

### Processing Steps

1. **Validation**: Check required parameters and file existence
2. **File Discovery**: Find all `.properties` files in source directory
3. **Merging**: Load and merge properties (last occurrence wins)
4. **Filtering**: Apply regex exclusion pattern if specified
5. **Ordering**: Apply template-based ordering or alphabetical sorting
6. **Output**: Write final file with header and section comments

### Deduplication Logic

- Uses Java's `LinkedHashMap` to maintain insertion order
- Keeps the **last occurrence** of duplicate keys
- Allows later files to override earlier ones
- Useful for environment-specific overrides

### File Processing Order

Files are processed in **alphabetical order** by filename.

To control precedence:
- Use numeric prefixes: `01-base.properties`, `02-env.properties`
- Or descriptive names: `base.properties`, `override.properties`

### Performance

- Efficient for typical property file sizes (< 10,000 lines)
- Uses Java's built-in Properties class
- LinkedHashMap provides O(1) lookups
- Minimal memory footprint
- No temporary files needed

## Task Registration

The `mergeproperties` task is registered in [`antlib.xml`](swat-dp-ant-tools/src/main/resources/ch/schlagundrahm/swat/datapower/tools/ant/antlib.xml):

```xml
<taskdef name="mergeproperties" 
         classname="ch.schlagundrahm.swat.datapower.tools.ant.taskdefs.MergeProperties" 
         onerror="ignore" />
```

## Building the Task

The task is part of the `swat-dp-ant-tools` module:

```bash
# Build the JAR
cd swat-dp-ant-tools
mvn clean install

# Copy to lib directory
cp target/swat-dp-ant-tools-*.jar swat-dp-tools/lib/
```

See [`JDK-SETUP-GUIDE.md`](JDK-SETUP-GUIDE.md) for JDK installation requirements.

## See Also

- **Tokenization**: [`TOKENIZATION-SOLUTION-SUMMARY.md`](TOKENIZATION-SOLUTION-SUMMARY.md)
- **Properties Template**: [`templates/properties-template.properties`](templates/properties-template.properties)
- **Java Source**: [`MergeProperties.java`](swat-dp-ant-tools/src/main/java/ch/schlagundrahm/swat/datapower/tools/ant/taskdefs/MergeProperties.java)

---

**Last Updated**: 2026-04-23
**Author**: Bob (DataPower Mode)