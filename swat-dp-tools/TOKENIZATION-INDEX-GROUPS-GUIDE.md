# Tokenization Index Groups Guide

## Overview

Index groups allow multiple DataPower object types to share a continuous index sequence during tokenization. This is essential when different object types represent alternative implementations of the same logical concept.

## Problem Statement

### Without Index Groups

When a service contains multiple related object types, each type gets its own independent index:

**Example Configuration:**
```xml
<configuration>
  <HTTPSourceProtocolHandler name="fsh-http">...</HTTPSourceProtocolHandler>
  <HTTPSSourceProtocolHandler name="fsh-https">...</HTTPSSourceProtocolHandler>
  <HTTPSourceProtocolHandler name="fsh-http-2">...</HTTPSourceProtocolHandler>
</configuration>
```

**Tokenization Result (WRONG)**:
```properties
fsh.1.name=fsh-http      # HTTPSourceProtocolHandler index 1
fsh.1.name=fsh-https     # HTTPSSourceProtocolHandler index 1 (DUPLICATE!)
fsh.2.name=fsh-http-2    # HTTPSourceProtocolHandler index 2
```

### With Index Groups

When object types are grouped, they share a continuous index:

**Tokenization Result (CORRECT)**:
```properties
fsh.1.name=fsh-http      # Index 1
fsh.2.name=fsh-https     # Index 2 (continuous)
fsh.3.name=fsh-http-2    # Index 3 (continuous)
```

## Configuration

### Index Groups File

**Location**: `swat-dp-tools/config/tokenization-index-groups.properties`

**Format**:
```properties
group.name=ObjectType1,ObjectType2,ObjectType3
```

### Example Configuration

```properties
# Front-side handlers share the same index sequence
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler

# Backend handlers share the same index sequence
group.backend.handlers=HTTPHandler,HTTPSHandler,MQHandler

# Policy attachments share the same index sequence
group.policy.attachments=PolicyAttachment,WSPolicyAttachment
```

## How It Works

### Index Calculation Logic

The XSLT function `swat:get-grouped-index()` determines the index for each element:

1. **Check Group Membership**: Is this object type part of a group?
2. **If Grouped**: Count all preceding siblings that are members of the same group
3. **If Not Grouped**: Count only preceding siblings with the same name (standard behavior)

### XSLT Implementation

```xslt
<xsl:function name="swat:get-grouped-index" as="xs:integer">
    <xsl:param name="element" as="element()" />
    <xsl:variable name="element-name" select="$element/name()" />
    
    <!-- Check if this element type is part of a group -->
    <xsl:variable name="group-info" select="$index-groups[object-type = $element-name]" />
    
    <xsl:choose>
        <xsl:when test="$group-info">
            <!-- Element is part of a group - count all preceding siblings in the same group -->
            <xsl:variable name="group-name" select="$group-info/group-name" />
            <xsl:variable name="group-members" select="$index-groups[group-name = $group-name]/object-type" />
            
            <!-- Count preceding siblings that are members of the same group -->
            <xsl:value-of select="count($element/preceding-sibling::*[name() = $group-members]) + 1" />
        </xsl:when>
        <xsl:otherwise>
            <!-- Element is not part of a group - use standard counting -->
            <xsl:value-of select="count($element/preceding-sibling::*[name() = $element-name]) + 1" />
        </xsl:otherwise>
    </xsl:choose>
</xsl:function>
```

### Java Implementation

The Java Ant task [`TokenizeService.java`](../swat-dp-ant-tools/src/main/java/ch/schlagundrahm/swat/datapower/tools/ant/taskdefs/TokenizeService.java) provides equivalent functionality:

```java
// Load index groups configuration
private void loadIndexGroups() throws IOException {
    if (indexGroupsFile == null || !indexGroupsFile.exists()) {
        return; // Optional file
    }
    
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(indexGroupsFile)) {
        props.load(fis);
    }
    
    // Parse group definitions
    for (String key : props.stringPropertyNames()) {
        if (key.startsWith("group.")) {
            String groupName = key.substring(6); // Remove "group." prefix
            String[] objectTypes = props.getProperty(key).split(",");
            
            List<String> typeList = new ArrayList<>();
            for (String type : objectTypes) {
                String trimmedType = type.trim();
                typeList.add(trimmedType);
                objectTypeToGroup.put(trimmedType, groupName);
            }
            groupToObjectTypes.put(groupName, typeList);
        }
    }
}

// Use grouped indexing during processing
private void processChildren(Element parent, String parentPath,
                            Map<String, Integer> elementCounts,
                            Map<String, Integer> groupCounts) {
    NodeList children = parent.getChildNodes();
    
    for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) child;
            String elementName = element.getNodeName();
            
            // Check if element is part of a group
            String groupName = objectTypeToGroup.get(elementName);
            int index;
            
            if (groupName != null) {
                // Use group-based indexing
                index = groupCounts.getOrDefault(groupName, 0) + 1;
                groupCounts.put(groupName, index);
            } else {
                // Use standard element-based indexing
                index = elementCounts.getOrDefault(elementName, 0) + 1;
                elementCounts.put(elementName, index);
            }
            
            // Continue processing with calculated index...
        }
    }
}
```

## Usage

### Basic Usage (XSLT)

The index groups file is automatically loaded when using the configurable tokenization macro:

```xml
<tokenize-service-config-configurable-macro
    src-dir="dist/xcfg/common"
    dst-dir="dist/xcfg/tokenized"
    properties-dir="dist/properties"
    rules-file="${swat.dp.tools}/config/tokenization-rules.properties"
    index-groups-file="${swat.dp.tools}/config/tokenization-index-groups.properties"
/>
```

### Basic Usage (Java)

The Java-based tokenization macro also supports index groups:

```xml
<tokenize-service-java-macro
    src-dir="dist/xcfg/common"
    dst-dir="dist/xcfg/tokenized"
    properties-dir="dist/properties"
    rules-file="${swat.dp.tools}/config/tokenization-rules.properties"
    index-groups-file="${swat.dp.tools}/config/tokenization-index-groups.properties"
/>
```

### Custom Index Groups File

You can specify a custom index groups file for either XSLT or Java:

```xml
<tokenize-service-config-configurable-macro
    src-dir="dist/xcfg/common"
    dst-dir="dist/xcfg/tokenized"
    properties-dir="dist/properties"
    index-groups-file="${project.dir}/custom-index-groups.properties"
/>
```

### Optional Usage

The index groups file is **optional**. If not provided or not found:
- Standard indexing behavior is used (each object type has independent indexes)
- No error is thrown
- Tokenization proceeds normally

## Use Cases

### 1. Front-Side Protocol Handlers

**Problem**: Services may use HTTP or HTTPS handlers interchangeably

**Solution**:
```properties
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler
```

**Result**: All front-side handlers get continuous indexes regardless of protocol

### 2. Backend Handlers

**Problem**: Services may connect to different backend types

**Solution**:
```properties
group.backend.handlers=HTTPHandler,HTTPSHandler,MQHandler,JMSHandler
```

**Result**: All backend handlers share the same index sequence

### 3. Policy Attachments

**Problem**: Different policy types should be numbered sequentially

**Solution**:
```properties
group.policy.attachments=PolicyAttachment,WSPolicyAttachment,RESTAttachment
```

**Result**: All policy attachments get continuous indexes

### 4. Certificate Objects

**Problem**: Different certificate types should be numbered together

**Solution**:
```properties
group.certificates=CryptoCertificate,CryptoKey,CryptoIdentCred
```

**Result**: All certificate-related objects share indexes

## Examples

### Example 1: Mixed HTTP/HTTPS Handlers

**Input Configuration**:
```xml
<configuration>
  <MultiProtocolGateway name="MyService">
    <FrontProtocol class="HTTPSourceProtocolHandler">fsh-http-1</FrontProtocol>
    <FrontProtocol class="HTTPSSourceProtocolHandler">fsh-https-1</FrontProtocol>
    <FrontProtocol class="HTTPSourceProtocolHandler">fsh-http-2</FrontProtocol>
  </MultiProtocolGateway>
  
  <HTTPSourceProtocolHandler name="fsh-http-1">
    <LocalAddress>0.0.0.0</LocalAddress>
    <LocalPort>8080</LocalPort>
  </HTTPSourceProtocolHandler>
  
  <HTTPSSourceProtocolHandler name="fsh-https-1">
    <LocalAddress>0.0.0.0</LocalAddress>
    <LocalPort>8443</LocalPort>
  </HTTPSSourceProtocolHandler>
  
  <HTTPSourceProtocolHandler name="fsh-http-2">
    <LocalAddress>0.0.0.0</LocalAddress>
    <LocalPort>8081</LocalPort>
  </HTTPSourceProtocolHandler>
</configuration>
```

**Index Groups Configuration**:
```properties
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler
```

**Tokenization Rules**:
```properties
MultiProtocolGateway.FrontProtocol.@class=fsh.{index}.class
MultiProtocolGateway.FrontProtocol=fsh.{index}.name
HTTPSourceProtocolHandler.@name=fsh.{index}.name
HTTPSourceProtocolHandler.LocalAddress=fsh.{index}.host
HTTPSourceProtocolHandler.LocalPort=fsh.{index}.port
HTTPSSourceProtocolHandler.@name=fsh.{index}.name
HTTPSSourceProtocolHandler.LocalAddress=fsh.{index}.host
HTTPSSourceProtocolHandler.LocalPort=fsh.{index}.port
```

**Generated Properties**:
```properties
# Service references
fsh.1.class=HTTPSourceProtocolHandler
fsh.1.name=fsh-http-1
fsh.2.class=HTTPSSourceProtocolHandler
fsh.2.name=fsh-https-1
fsh.3.class=HTTPSourceProtocolHandler
fsh.3.name=fsh-http-2

# Handler configurations
fsh.1.host=0.0.0.0
fsh.1.port=8080
fsh.2.host=0.0.0.0
fsh.2.port=8443
fsh.3.host=0.0.0.0
fsh.3.port=8081
```

### Example 2: Without Index Groups (Standard Behavior)

**Same Input Configuration** (as Example 1)

**No Index Groups Configuration** (file not provided or empty)

**Generated Properties (WRONG)**:
```properties
# Service references
fsh.1.class=HTTPSourceProtocolHandler
fsh.1.name=fsh-http-1
fsh.1.class=HTTPSSourceProtocolHandler  # DUPLICATE KEY!
fsh.1.name=fsh-https-1                  # DUPLICATE KEY!
fsh.2.class=HTTPSourceProtocolHandler
fsh.2.name=fsh-http-2

# Handler configurations
fsh.1.host=0.0.0.0
fsh.1.port=8080
fsh.1.host=0.0.0.0                      # DUPLICATE KEY!
fsh.1.port=8443                         # DUPLICATE KEY!
fsh.2.host=0.0.0.0
fsh.2.port=8081
```

## Best Practices

### 1. Group Related Object Types

Only group object types that represent alternative implementations of the same concept:

✅ **Good**: `HTTPSourceProtocolHandler` and `HTTPSSourceProtocolHandler` (same purpose, different protocol)

❌ **Bad**: `HTTPSourceProtocolHandler` and `XMLManager` (unrelated objects)

### 2. Use Descriptive Group Names

```properties
# Good - clear purpose
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler

# Bad - unclear purpose
group.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler
```

### 3. Document Group Purpose

Add comments explaining why objects are grouped:

```properties
# Front-side handlers share the same index sequence because they represent
# alternative protocols for the same logical front-side handler
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler
```

### 4. Keep Groups Small

Only include object types that truly need shared indexing:

```properties
# Good - specific grouping
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler

# Bad - too broad
group.all.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler,HTTPHandler,HTTPSHandler,MQHandler
```

### 5. Test Thoroughly

Always test tokenization with and without index groups to ensure correct behavior:

```bash
# Test with index groups
ant tokenize-service-config-configurable

# Verify properties file has continuous indexes
cat dist/properties/MyService.properties | grep "fsh\."
```

## Troubleshooting

### Issue: Duplicate Property Keys

**Symptom**: Properties file contains duplicate keys

**Cause**: Related object types not grouped

**Solution**: Add object types to an index group

```properties
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler
```

### Issue: Index Groups Not Applied

**Symptom**: Objects still get separate indexes despite being grouped

**Cause**: Index groups file not found or not passed to XSLT

**Solution**: Verify file path and macro parameters

```xml
<tokenize-service-config-configurable-macro
    index-groups-file="${swat.dp.tools}/config/tokenization-index-groups.properties"
/>
```

### Issue: Wrong Objects Grouped

**Symptom**: Unrelated objects share indexes

**Cause**: Objects incorrectly added to same group

**Solution**: Review group membership and separate unrelated objects

```properties
# Wrong - these are unrelated
group.all=HTTPSourceProtocolHandler,XMLManager,StylePolicy

# Correct - only related objects
group.frontside.handlers=HTTPSourceProtocolHandler,HTTPSSourceProtocolHandler
```

### Issue: Index Groups File Not Found

**Symptom**: Warning or error about missing file

**Cause**: File path incorrect or file doesn't exist

**Solution**: The file is optional - if not needed, remove the parameter:

```xml
<tokenize-service-config-configurable-macro
    src-dir="..."
    dst-dir="..."
    <!-- index-groups-file parameter omitted - uses default or none -->
/>
```

## Technical Details

### File Format

- **Encoding**: UTF-8
- **Line Separator**: LF or CRLF
- **Comment Character**: `#`
- **Format**: `group.name=Type1,Type2,Type3`

### Parsing Logic

1. Read file line by line
2. Skip empty lines and comments (starting with `#`)
3. Split each line on `=` character
4. Extract group name from key (after `group.` prefix)
5. Split value on `,` to get object types
6. Create group membership entries for each object type

### Performance

- Index groups are loaded once at stylesheet initialization
- Lookup is O(n) where n is number of group members
- Minimal performance impact for typical configurations (< 100 groups)

## See Also

- [Tokenization Rules Guide](TOKENIZATION-CONFIGURABLE-GUIDE.md)
- [Tokenization Quick Reference](TOKENIZATION-QUICK-REFERENCE.md)
- [SWAT4DP User Guide](../Swat4DP-User-Guide_v01.00.pdf)

---

**Created**: 2026-05-26  
**Version**: 1.2.1-SNAPSHOT  
**Author**: Bob (DataPower Mode)