# Tokenization Approach Comparison

## Problem Analysis

When enhancing SWAT4DP tokenization to combine two XSLT files into one operation, we explored two approaches:

1. **DOM-based** (XPath-aware, correct tokenization)
2. **Streaming-based** (Format-preserving, but context-blind)

## The Fundamental Trade-off

### DOM-Based Approach (`TokenizeService.java`)

**How it works**:
- Parses XML into Document Object Model (DOM)
- Traverses the tree structure maintaining full context
- Applies rules based on XPath: `ServiceType.ElementName`
- Correctly distinguishes `MultiProtocolGateway.mAdminState` from `HTTPSSourceProtocolHandler.mAdminState`

**Pros**:
✅ **Correct tokenization** - Respects XPath context  
✅ **Handles complex rules** - Supports nested paths  
✅ **Reliable** - XML structure validated  
✅ **Maintainable** - Clear logic flow  

**Cons**:
❌ **Minor formatting changes** - DOM normalization affects whitespace  
❌ **Attribute spacing** - May reorder or reformat attributes  

**Example Issue**:
```xml
<!-- Original XSLT output -->
<Element attr1="value1"  attr2="value2">

<!-- DOM output -->
<Element attr2="value2"  attr1="value1">
```

### Streaming-Based Approach (`TokenizeServiceStreaming.java`)

**How it works**:
- Reads XML as plain text
- Uses regex patterns to find and replace values
- No XML parsing or structure awareness
- Preserves exact formatting byte-for-byte

**Pros**:
✅ **Perfect formatting preservation** - Byte-for-byte identical  
✅ **Faster processing** - No XML parsing overhead  
✅ **Lower memory** - No DOM tree in memory  

**Cons**:
❌ **Context-blind** - Cannot distinguish element context  
❌ **Incorrect tokenization** - Replaces ALL matching elements  
❌ **Fragile** - Regex patterns can break with complex XML  

**Critical Issue**:
```properties
# Rule: Only tokenize service mAdminState
MultiProtocolGateway.mAdminState=service.state
```

```xml
<!-- Input -->
<MultiProtocolGateway>
  <mAdminState>enabled</mAdminState>  <!-- Should tokenize -->
</MultiProtocolGateway>
<HTTPSSourceProtocolHandler>
  <mAdminState>enabled</mAdminState>  <!-- Should NOT tokenize -->
</HTTPSSourceProtocolHandler>

<!-- Streaming output (WRONG) -->
<MultiProtocolGateway>
  <mAdminState>@service.state@</mAdminState>  ✅ Correct
</MultiProtocolGateway>
<HTTPSSourceProtocolHandler>
  <mAdminState>@service.state@</mAdminState>  ❌ WRONG! Should be 'enabled'
</HTTPSSourceProtocolHandler>
```

## Recommendation: Use DOM-Based Approach

### Why DOM is the Right Choice

1. **Correctness is paramount** - Wrong tokenization breaks deployments
2. **Formatting differences are cosmetic** - XML is semantically identical
3. **Comparison tools exist** - Use XML-aware diff tools
4. **XSLT also normalizes** - Original XSLT has similar formatting behavior

### Handling Formatting Differences

The formatting differences are **cosmetic only** - the XML is semantically identical. Here are strategies to handle them:

#### Option 1: Use XML-Aware Comparison

Instead of `diff`, use tools that understand XML structure:

```bash
# xmllint normalizes both files before comparison
xmllint --format file1.xcfg > file1-normalized.xml
xmllint --format file2.xcfg > file2-normalized.xml
diff file1-normalized.xml file2-normalized.xml
```

#### Option 2: Semantic Comparison Script

Create a comparison script that ignores whitespace:

```bash
# Compare ignoring whitespace differences
diff -w <(xmllint --format file1.xcfg) <(xmllint --format file2.xcfg)
```

#### Option 3: Accept Minor Differences

The formatting differences are:
- Extra/missing spaces between attributes
- Line break variations
- Whitespace normalization

These do **NOT** affect:
- XML validity
- DataPower functionality
- Configuration semantics
- Deployment success

#### Option 4: Canonical XML

Use XML canonicalization (C14N) for exact comparison:

```bash
# Canonicalize both files
xmllint --c14n file1.xcfg > file1-canonical.xml
xmllint --c14n file2.xcfg > file2-canonical.xml
diff file1-canonical.xml file2-canonical.xml
```

## Implementation Decision

**Use `TokenizeService.java` (DOM-based)**

### Rationale

1. **Correctness**: Properly handles XPath context
2. **Reliability**: Validated XML structure
3. **Maintainability**: Clear, understandable code
4. **Compatibility**: Matches XSLT behavior semantically

### Configuration

**File**: `swat-dp-tools/ant/tokenize-java.xml`

```xml
<tokenizeservice
    srcDir="@{src-dir}"
    dstDir="@{dst-dir}"
    propertiesDir="@{properties-dir}"
    rulesFile="@{rules-file}"
/>
```

## Comparison Workflow

### For Development/Testing

When comparing tokenized outputs:

```bash
# 1. Tokenize with Java
ant tokenize-service-java

# 2. Normalize both outputs
xmllint --format xslt-output/service.xcfg > xslt-normalized.xml
xmllint --format java-output/service.xcfg > java-normalized.xml

# 3. Compare normalized versions
diff xslt-normalized.xml java-normalized.xml
```

### For Production

The formatting differences don't matter in production:
- DataPower parses XML semantically
- Whitespace variations are ignored
- Configuration functions identically

## Technical Details

### Why DOM Normalizes Formatting

DOM parsers must:
1. Parse XML into a tree structure
2. Validate structure and syntax
3. Normalize whitespace per XML spec
4. Serialize back to text

This process inherently changes formatting while preserving semantics.

### Why Streaming Can't Handle Context

Regex patterns match text, not structure:

```regex
<mAdminState>([^<]+)</mAdminState>
```

This matches **every** `<mAdminState>` element, regardless of:
- Parent element
- Nesting level
- Sibling elements
- Document context

To distinguish context, you need:
- XML parsing (DOM/SAX)
- Tree traversal
- Path tracking
- XPath evaluation

## Conclusion

**Use DOM-based `TokenizeService.java`** for correct, reliable tokenization.

**Accept minor formatting differences** as they:
- Don't affect functionality
- Are semantically identical
- Can be normalized for comparison
- Match XSLT behavior semantically

**Avoid streaming approach** because:
- Incorrect tokenization breaks deployments
- Context-blindness causes wrong replacements
- Regex fragility with complex XML
- Correctness > formatting preservation

## Summary Table

| Aspect | DOM-Based | Streaming | Winner |
|--------|-----------|-----------|--------|
| **Correctness** | ✅ XPath-aware | ❌ Context-blind | **DOM** |
| **Tokenization** | ✅ Accurate | ❌ Over-replaces | **DOM** |
| **Formatting** | ⚠️ Normalized | ✅ Preserved | Streaming |
| **Reliability** | ✅ Validated | ⚠️ Fragile | **DOM** |
| **Maintainability** | ✅ Clear | ⚠️ Complex regex | **DOM** |
| **Performance** | ✅ Fast enough | ✅ Faster | Tie |
| **Production Use** | ✅ Safe | ❌ Risky | **DOM** |

**Overall Winner**: **DOM-Based Approach**

---

**Recommendation**: Use [`TokenizeService.java`](../swat-dp-ant-tools/src/main/java/ch/schlagundrahm/swat/datapower/tools/ant/taskdefs/TokenizeService.java) for all tokenization operations.

**Created**: 2026-04-23  
**Author**: Bob (DataPower Mode)