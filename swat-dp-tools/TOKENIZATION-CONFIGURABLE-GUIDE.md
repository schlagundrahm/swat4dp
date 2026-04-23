# Configurable Tokenization Guide

## Overview

The **configurable tokenization system** allows you to define tokenization rules in a simple properties file instead of hardcoding them in XSLT. This makes the system much more maintainable and easier to extend.

## Key Benefits

1. **No XSLT Knowledge Required**: Add new tokenization rules by editing a properties file
2. **Centralized Configuration**: All tokenization rules in one place
3. **Easy to Maintain**: Simple key=value format
4. **Version Control Friendly**: Properties files are easy to diff and merge
5. **Self-Documenting**: Rules file serves as documentation of what gets tokenized
6. **Flexible**: Support for simple elements, nested elements, attributes, and indexed objects

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Tokenization Process                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Input XCFG ──┐                                             │
│               │                                              │
│               ├──► tokenize-service-configurable.xsl ──┐    │
│               │           ▲                             │    │
│  Rules File ──┘           │                             │    │
│  (.properties)            │ Reads rules at runtime      │    │
│                           │                             │    │
│                                                         │    │
│                                                         ├──► Tokenized XCFG
│                                                         │    │
│                                                         └──► Properties File
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Files

### Core Files

- **`config/tokenization-rules.properties`**: Defines what to tokenize and token names
- **`soma/xsl/tokenize-service-configurable.xsl`**: XSLT that reads rules and applies them
- **`ant/tokenize-configurable.xml`**: Ant macros for configurable tokenization

### Supporting Files

- **`soma/xsl/tokenize-service-unified.xsl`**: Original hardcoded approach (still supported)
- **`ant/tokenize-unified.xml`**: Ant macros for unified approach

## Tokenization Rules Format

### Basic Syntax

```properties
# Format: XPath.expression=token.name
ServiceType.ElementName=token.name
```

### Path Notation

The XPath uses dot notation relative to `//configuration/`:

```properties
# Simple element
XMLFirewallService.LocalPort=local.port

# Nested element
WebAppFW.FrontSide.LocalAddress=frontend.server.host

# Attribute (use @ prefix)
WSEndpointRewritePolicy.@name=wsp.endpoint.rewrite.policy.name
```

### Indexed Objects

For objects that can appear multiple times (like protocol handlers), use `{index}` placeholder:

```properties
# The {index} will be replaced with position() at runtime
HTTPSSourceProtocolHandler.@name=fsh.{index}.name
HTTPSSourceProtocolHandler.LocalPort=fsh.{index}.local.port
```

This generates tokens like:
- `fsh.1.name`, `fsh.1.local.port`
- `fsh.2.name`, `fsh.2.local.port`
- etc.

### Elements with Both Attribute and Text Value

When an element has both an attribute and text content that need tokenization, define both rules:

```properties
# Both the @class attribute and text value will be tokenized
MultiProtocolGateway.FrontProtocol.@class=fsh.{index}.class
MultiProtocolGateway.FrontProtocol=fsh.{index}.name
```

Input XML:
```xml
<FrontProtocol class="HTTPSSourceProtocolHandler">myHandler</FrontProtocol>
```

Output XML:
```xml
<FrontProtocol class="@fsh.1.class@">@fsh.1.name@</FrontProtocol>
```

Generated properties:
```properties
fsh.1.class=HTTPSSourceProtocolHandler
fsh.1.name=myHandler
```

### Comments

```properties
# This is a comment
# Comments start with # and are ignored

# You can use comments to organize rules
# ================================
# SERVICE TYPES
# ================================
```

## Usage

### Basic Usage

```xml
<tokenize-service-config-configurable-macro
    src-dir="dist/export/device-domain/xcfg/common"
    dst-dir="dist/tokenized/device-domain"
    properties-dir="dist/tokenized/device-domain-properties"
/>
```

### Custom Rules File

```xml
<tokenize-service-config-configurable-macro
    src-dir="dist/export/device-domain/xcfg/common"
    dst-dir="dist/tokenized/device-domain"
    rules-file="config/my-custom-rules.properties"
/>
```

### Validate Rules

```bash
ant -f swat-dp-tools/ant/tokenize-configurable.xml validate-rules
```

### List All Rules

```bash
ant -f swat-dp-tools/ant/tokenize-configurable.xml list-rules
```

## Adding New Tokenization Rules

### Example: Adding a New Service Type

Let's say you want to tokenize a new service type called `MyCustomService`:

1. **Edit `config/tokenization-rules.properties`**:

```properties
# ================================================================================================
# MY CUSTOM SERVICE
# ================================================================================================
MyCustomService.mAdminState=service.state
MyCustomService.UserSummary=service.summary
MyCustomService.CustomProperty=my.custom.property
MyCustomService.NestedElement.SubElement=my.nested.value
```

2. **That's it!** No XSLT changes needed.

3. **Test it**:

```bash
ant -f swat-dp-tools/ant/tokenize-configurable.xml validate-rules
```

### Example: Adding Indexed Objects

For objects that appear multiple times:

```properties
# ================================================================================================
# MY CUSTOM HANDLER (indexed)
# ================================================================================================
MyCustomHandler.@name=handler.{index}.name
MyCustomHandler.Property1=handler.{index}.property1
MyCustomHandler.Property2=handler.{index}.property2
```

This will generate:
- First instance: `handler.1.name`, `handler.1.property1`, `handler.1.property2`
- Second instance: `handler.2.name`, `handler.2.property1`, `handler.2.property2`

## Rule Organization Best Practices

### 1. Group by Service Type or Object Type

```properties
# ================================================================================================
# XMLFIREWALL SERVICE
# ================================================================================================
XMLFirewallService.mAdminState=service.state
XMLFirewallService.LocalPort=local.port

# ================================================================================================
# MULTIPROTOCOL GATEWAY SERVICE
# ================================================================================================
MultiProtocolGateway.mAdminState=service.state
MultiProtocolGateway.LocalPort=local.port
```

### 2. Use Consistent Token Naming

```properties
# Good: Consistent naming pattern
XMLFirewallService.LocalPort=local.port
MultiProtocolGateway.LocalPort=local.port
WSGateway.LocalPort=local.port

# Avoid: Inconsistent naming
XMLFirewallService.LocalPort=xmlfw.port
MultiProtocolGateway.LocalPort=mpgw.local.port
WSGateway.LocalPort=ws.gateway.port.local
```

### 3. Document Complex Rules

```properties
# Frontend protocol handlers are indexed because multiple can exist
# The {index} placeholder is replaced with the handler's position
HTTPSSourceProtocolHandler.@name=fsh.{index}.name
HTTPSSourceProtocolHandler.LocalPort=fsh.{index}.local.port
```

## Migration from Hardcoded XSLT

### Step 1: Identify Current Tokenization Rules

Look at your current XSLT templates and identify patterns like:

```xml
<xsl:when test="local-name(.)='LocalPort'">
    <xsl:call-template name="set-token">
        <xsl:with-param name="key" select="'local.port'" />
    </xsl:call-template>
</xsl:when>
```

### Step 2: Convert to Properties Format

```properties
XMLFirewallService.LocalPort=local.port
```

### Step 3: Test Both Approaches

Run both tokenization methods and compare outputs:

```bash
# Old approach
ant tokenize-service-unified

# New approach
ant tokenize-service-configurable

# Compare results
diff -r dist/tokenized-old dist/tokenized-new
```

## Troubleshooting

### Rule Not Applied

**Problem**: Added a rule but element is not being tokenized.

**Solutions**:
1. Check the XPath is correct (case-sensitive)
2. Verify the element exists in your XCFG
3. Check for typos in the properties file
4. Run `validate-rules` to check syntax

### Indexed Objects Not Working

**Problem**: `{index}` not being replaced correctly.

**Solutions**:
1. Ensure you're using `{index}` not `{n}` or other variants
2. Check that the object actually appears multiple times
3. Verify the XPath matches the parent element

### Properties File Not Found

**Problem**: Error about missing rules file.

**Solution**:
```xml
<!-- Specify full path to rules file -->
<tokenize-service-config-configurable-macro
    rules-file="${basedir}/config/tokenization-rules.properties"
    ...
/>
```

## Advanced Features

### Multiple Rules Files

You can maintain different rules files for different scenarios:

```properties
# tokenization-rules-dev.properties - Development environment
XMLFirewallService.DebugMode=debug.mode
XMLFirewallService.DebugHistory=debug.history

# tokenization-rules-prod.properties - Production environment
# (no debug settings)
```

Usage:
```xml
<tokenize-service-config-configurable-macro
    rules-file="config/tokenization-rules-${env}.properties"
    ...
/>
```

### Conditional Tokenization

You can comment out rules to disable them:

```properties
# Temporarily disable this tokenization
# XMLFirewallService.DebugMode=debug.mode

# Or use environment-specific rules
XMLFirewallService.LocalPort=local.port.${env}
```

## Performance Considerations

- **Rule Loading**: Rules are loaded once per XSLT transformation
- **Memory**: All rules are kept in memory during transformation
- **Speed**: Configurable approach is slightly slower than hardcoded XSLT but difference is negligible for typical configurations

## Comparison: Configurable vs. Hardcoded

| Aspect | Configurable | Hardcoded XSLT |
|--------|-------------|----------------|
| **Ease of Adding Rules** | ✅ Very Easy | ❌ Requires XSLT knowledge |
| **Maintainability** | ✅ Excellent | ⚠️ Moderate |
| **Performance** | ✅ Good | ✅ Excellent |
| **Flexibility** | ✅ Very High | ⚠️ Moderate |
| **Learning Curve** | ✅ Low | ❌ High |
| **Version Control** | ✅ Easy to diff | ⚠️ Harder to diff |
| **Documentation** | ✅ Self-documenting | ❌ Requires separate docs |

## Recommendations

### Use Configurable Approach When:
- ✅ You frequently add new tokenization rules
- ✅ Multiple team members need to maintain rules
- ✅ You want centralized, easy-to-understand configuration
- ✅ You need to support multiple environments with different rules

### Use Hardcoded XSLT When:
- ✅ You need maximum performance (processing thousands of files)
- ✅ Rules are stable and rarely change
- ✅ You need complex conditional logic beyond simple XPath matching
- ✅ You're already comfortable with XSLT

## Examples

### Complete Example: Tokenizing a New Service

**Scenario**: You have a new `APIGateway` service type with these elements:
- `mAdminState` → `service.state`
- `APIEndpoint` → `api.endpoint`
- `RateLimitPolicy` → `rate.limit.policy`

**Solution**:

1. Add to `tokenization-rules.properties`:
```properties
# ================================================================================================
# API GATEWAY SERVICE
# ================================================================================================
APIGateway.mAdminState=service.state
APIGateway.APIEndpoint=api.endpoint
APIGateway.RateLimitPolicy=rate.limit.policy
```

2. Run tokenization:
```bash
ant tokenize-service-configurable
```

3. Result in tokenized XCFG:
```xml
<APIGateway name="MyAPI">
    <mAdminState>@service.state@</mAdminState>
    <APIEndpoint>@api.endpoint@</APIEndpoint>
    <RateLimitPolicy>@rate.limit.policy@</RateLimitPolicy>
</APIGateway>
```

4. Generated properties file:
```properties
service.state=enabled
api.endpoint=https://api.example.com
rate.limit.policy=default-rate-limit
```

## Support and Feedback

For questions or issues with configurable tokenization:
1. Check this guide
2. Run `validate-rules` to check syntax
3. Compare with working examples in `tokenization-rules.properties`
4. Review XSLT processing messages for debugging

---

**Last Updated**: 2026-04-23
**Author**: Bob (DataPower Mode)