# Tokenization Quick Reference

## Quick Start

### Add a New Tokenization Rule

1. Edit `config/tokenization-rules.properties`
2. Add line: `ServiceType.ElementName=token.name`
3. Done! No XSLT changes needed.

## Rule Syntax

```properties
# Simple element
XMLFirewallService.LocalPort=local.port

# Nested element
WebAppFW.FrontSide.LocalAddress=frontend.server.host

# Attribute
WSEndpointRewritePolicy.@name=policy.name

# Indexed (multiple instances)
HTTPSSourceProtocolHandler.LocalPort=fsh.{index}.local.port
```

## Common Patterns

### Service Types

```properties
# Standard service properties
ServiceType.mAdminState=service.state
ServiceType.UserSummary=service.summary
ServiceType.DebugMode=debug.mode
ServiceType.DebugHistory=debug.history

# Network settings
ServiceType.LocalAddress=local.address
ServiceType.LocalPort=local.port
ServiceType.RemoteAddress=remote.address
ServiceType.RemotePort=remote.port
```

### Configuration Objects

```properties
# Simple object
ObjectType.Property=object.property

# Indexed object (appears multiple times)
ObjectType.@name=object.{index}.name
ObjectType.Property=object.{index}.property
```

## Usage

### Tokenize with Default Rules

```xml
<tokenize-service-config-configurable-macro
    src-dir="dist/export/device-domain/xcfg/common"
    dst-dir="dist/tokenized/device-domain"
/>
```

### Tokenize with Custom Rules

```xml
<tokenize-service-config-configurable-macro
    src-dir="dist/export/device-domain/xcfg/common"
    rules-file="config/my-rules.properties"
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

## Files

| File | Purpose |
|------|---------|
| `config/tokenization-rules.properties` | Define tokenization rules |
| `soma/xsl/tokenize-service-configurable.xsl` | XSLT processor |
| `ant/tokenize-configurable.xml` | Ant macros |

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Rule not applied | Check XPath case-sensitivity |
| `{index}` not replaced | Verify object appears multiple times |
| File not found | Use full path in `rules-file` attribute |

## Examples

### Example 1: Add New Service Type

```properties
# Add to tokenization-rules.properties
MyService.mAdminState=service.state
MyService.CustomProperty=my.property
```

### Example 2: Add Indexed Object

```properties
# Multiple instances with position-based tokens
MyHandler.@name=handler.{index}.name
MyHandler.Port=handler.{index}.port
```

Result:
- Instance 1: `handler.1.name`, `handler.1.port`
- Instance 2: `handler.2.name`, `handler.2.port`

### Example 3: Nested Elements

```properties
# Deep nesting with dot notation
Service.Section.Subsection.Element=token.name
```

## Token Naming Conventions

✅ **Good**:
```properties
XMLFirewallService.LocalPort=local.port
MultiProtocolGateway.LocalPort=local.port
```

❌ **Avoid**:
```properties
XMLFirewallService.LocalPort=xmlfw.lp
MultiProtocolGateway.LocalPort=mpgw.local.port.value
```

## Key Benefits

- ✅ No XSLT knowledge required
- ✅ Centralized configuration
- ✅ Easy to maintain and extend
- ✅ Self-documenting
- ✅ Version control friendly

## See Also

- **Full Guide**: `TOKENIZATION-CONFIGURABLE-GUIDE.md`
- **Migration Guide**: `TOKENIZATION-MIGRATION-GUIDE.md`