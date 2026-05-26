<?xml version="1.0" encoding="UTF-8"?>
<!-- Licensed Materials - Property of IBM -->
<!-- Copyright IBM Corporation 2026. All Rights Reserved. -->
<!-- US Government Users Restricted Rights - Use, duplication or disclosure -->
<!-- restricted by GSA ADP Schedule Contract with IBM Corp. -->
<!-- 
    CONFIGURABLE TOKENIZATION STYLESHEET
    This stylesheet uses an external properties file to define tokenization rules.
    Rules are defined in tokenization-rules.properties with format: XPath=token.name
-->
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:env="http://www.w3.org/2003/05/soap-envelope" 
    xmlns:dp="http://www.datapower.com/schemas/management"
    xmlns:swat="http://schlagundrahm.ch/swat4dp"
    exclude-result-prefixes="xs env swat">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />
    
    <!-- Parameters -->
    <xsl:param name="properties-output-file" select="'tokens.properties'" />
    <xsl:param name="rules-file" select="'tokenization-rules.properties'" />
    <xsl:param name="index-groups-file" select="'tokenization-index-groups.properties'" />
    
    <!-- Convert Windows path to proper file URI -->
    <xsl:variable name="rules-file-uri">
        <xsl:choose>
            <!-- If already a URI, use as-is -->
            <xsl:when test="starts-with($rules-file, 'file:')">
                <xsl:value-of select="$rules-file" />
            </xsl:when>
            <!-- If Windows absolute path (C:\...), convert to file URI -->
            <xsl:when test="matches($rules-file, '^[A-Za-z]:\\')">
                <xsl:value-of select="concat('file:///', translate($rules-file, '\', '/'))" />
            </xsl:when>
            <!-- Otherwise use as relative path -->
            <xsl:otherwise>
                <xsl:value-of select="$rules-file" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <!-- Convert index groups file path to URI -->
    <xsl:variable name="index-groups-file-uri">
        <xsl:choose>
            <xsl:when test="starts-with($index-groups-file, 'file:')">
                <xsl:value-of select="$index-groups-file" />
            </xsl:when>
            <xsl:when test="matches($index-groups-file, '^[A-Za-z]:\\')">
                <xsl:value-of select="concat('file:///', translate($index-groups-file, '\', '/'))" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$index-groups-file" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <!-- Load tokenization rules from properties file -->
    <xsl:variable name="rules-doc" select="unparsed-text($rules-file-uri)" />
    
    <!-- Load index groups configuration (optional) -->
    <xsl:variable name="index-groups-doc">
        <xsl:choose>
            <xsl:when test="unparsed-text-available($index-groups-file-uri)">
                <xsl:value-of select="unparsed-text($index-groups-file-uri)" />
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <!-- Parse index groups into a map structure -->
    <xsl:variable name="index-groups" as="element()*">
        <xsl:for-each select="tokenize($index-groups-doc, '&#10;')">
            <xsl:variable name="line" select="normalize-space(.)" />
            <!-- Skip comments and empty lines -->
            <xsl:if test="$line != '' and not(starts-with($line, '#'))">
                <xsl:variable name="parts" select="tokenize($line, '=')" />
                <xsl:if test="count($parts) = 2 and starts-with($parts[1], 'group.')">
                    <xsl:variable name="group-name" select="substring-after($parts[1], 'group.')" />
                    <xsl:variable name="object-types" select="tokenize($parts[2], ',')" />
                    <xsl:for-each select="$object-types">
                        <group-member>
                            <object-type><xsl:value-of select="normalize-space(.)" /></object-type>
                            <group-name><xsl:value-of select="$group-name" /></group-name>
                        </group-member>
                    </xsl:for-each>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    
    <!-- Parse rules into a map structure -->
    <xsl:variable name="tokenization-rules" as="element()*">
        <xsl:for-each select="tokenize($rules-doc, '&#10;')">
            <xsl:variable name="line" select="normalize-space(.)" />
            <!-- Skip comments and empty lines -->
            <xsl:if test="$line != '' and not(starts-with($line, '#'))">
                <xsl:variable name="parts" select="tokenize($line, '=')" />
                <xsl:if test="count($parts) = 2">
                    <rule>
                        <xpath><xsl:value-of select="normalize-space($parts[1])" /></xpath>
                        <token><xsl:value-of select="normalize-space($parts[2])" /></token>
                    </rule>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>

    <!-- Root template -->
    <xsl:template match="/datapower-configuration">
        <!-- Generate the tokenized XML (primary output) -->
        <xsl:copy>
            <xsl:attribute name="version">
                <xsl:value-of select="@version" />
            </xsl:attribute>
            <xsl:apply-templates />
        </xsl:copy>
        
        <!-- Generate the properties file (secondary output) -->
        <xsl:result-document href="{$properties-output-file}" method="text" encoding="UTF-8">
            <xsl:text># Auto-generated properties file from configurable tokenization&#xa;</xsl:text>
            <xsl:text># Generated: </xsl:text>
            <xsl:value-of select="current-dateTime()" />
            <xsl:text>&#xa;</xsl:text>
            <xsl:text># Rules file: </xsl:text>
            <xsl:value-of select="$rules-file" />
            <xsl:text>&#xa;&#xa;</xsl:text>
            <xsl:apply-templates mode="properties" />
        </xsl:result-document>
    </xsl:template>

    <!-- Configuration element -->
    <xsl:template match="configuration">
        <xsl:copy>
            <!-- Check if domain attribute should be tokenized -->
            <xsl:variable name="domain-rule" select="$tokenization-rules[xpath = 'configuration.@domain']" />
            <xsl:choose>
                <xsl:when test="$domain-rule">
                    <xsl:attribute name="domain">
                        <xsl:value-of select="concat('@', $domain-rule/token, '@')" />
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="@domain" />
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <!-- Generic element processing -->
    <xsl:template match="*">
        <xsl:variable name="element-path" select="swat:get-element-path(.)" />
        <xsl:variable name="text-rule" select="$tokenization-rules[xpath = $element-path]" />
        
        <!-- Check if any attributes have rules -->
        <xsl:variable name="has-attr-rules" select="some $attr in @* satisfies $tokenization-rules[xpath = swat:get-attribute-path($attr)]" />
        
        <xsl:choose>
            <!-- If this element has text rule OR attribute rules, handle specially -->
            <xsl:when test="$text-rule or $has-attr-rules">
                <xsl:element name="{name()}">
                    <!-- Copy namespaces to parent element -->
                    <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
                    <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
                    
                    <!-- Process attributes (will apply tokenization if rules exist) -->
                    <xsl:apply-templates select="@*" />
                    
                    <!-- Process text content -->
                    <xsl:choose>
                        <xsl:when test="$text-rule">
                            <!-- Tokenize the text value -->
                            <xsl:variable name="final-token-name">
                                <xsl:choose>
                                    <xsl:when test="contains($text-rule/token, '{index}')">
                                        <xsl:value-of select="replace($text-rule/token, '\{index\}', string(swat:get-grouped-index(.)))" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$text-rule/token" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:value-of select="concat('@', $final-token-name, '@')" />
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- No text rule, process child nodes normally -->
                            <xsl:apply-templates select="node()" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
            </xsl:when>
            <!-- Otherwise, copy as-is -->
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()" />
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Attribute processing -->
    <xsl:template match="@*">
        <xsl:variable name="attr-path" select="swat:get-attribute-path(.)" />
        <xsl:variable name="matching-rule" select="$tokenization-rules[xpath = $attr-path]" />
        
        <xsl:choose>
            <xsl:when test="$matching-rule">
                <!-- Handle indexed attributes -->
                <xsl:variable name="token-name">
                    <xsl:choose>
                        <xsl:when test="contains($matching-rule/token, '{index}')">
                            <xsl:value-of select="replace($matching-rule/token, '\{index\}', string(swat:get-grouped-index(..)))" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$matching-rule/token" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:attribute name="{name()}">
                    <xsl:value-of select="concat('@', $token-name, '@')" />
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Template to tokenize an element -->
    <xsl:template name="tokenize-element">
        <xsl:param name="token-name" />
        
        <!-- Handle indexed tokens -->
        <xsl:variable name="final-token-name">
            <xsl:choose>
                <xsl:when test="contains($token-name, '{index}')">
                    <xsl:value-of select="replace($token-name, '\{index\}', string(swat:get-grouped-index(.)))" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$token-name" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*" />
            <xsl:value-of select="concat('@', $final-token-name, '@')" />
        </xsl:element>
    </xsl:template>

    <!-- Function to build element path -->
    <xsl:function name="swat:get-element-path" as="xs:string">
        <xsl:param name="element" as="element()" />
        <xsl:variable name="ancestors" select="$element/ancestor::*[parent::configuration]" />
        <xsl:choose>
            <xsl:when test="$ancestors">
                <xsl:value-of select="string-join(($ancestors/name(), $element/name()), '.')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$element/name()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- Function to build attribute path -->
    <xsl:function name="swat:get-attribute-path" as="xs:string">
        <xsl:param name="attribute" as="attribute()" />
        <xsl:variable name="element" select="$attribute/.." />
        <xsl:variable name="element-path" select="swat:get-element-path($element)" />
        <xsl:value-of select="concat($element-path, '.@', $attribute/name())" />
    </xsl:function>
    
    <!-- Function to calculate grouped index for an element -->
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

    <!-- Properties mode templates -->
    <xsl:template match="*" mode="properties">
        <xsl:variable name="element-path" select="swat:get-element-path(.)" />
        <xsl:variable name="text-rule" select="$tokenization-rules[xpath = $element-path]" />
        
        <!-- Determine if this is a top-level element (direct child of configuration) -->
        <xsl:variable name="is-top-level" select="exists(parent::*[local-name() = 'configuration'])" as="xs:boolean" />
        
        <!-- Check if this element has siblings with the same name -->
        <xsl:variable name="has-siblings-with-same-name"
                      select="count(preceding-sibling::*[name() = current()/name()]) + count(following-sibling::*[name() = current()/name()]) > 0" />
        
        <!-- Check if any attributes have rules -->
        <xsl:variable name="has-attr-rules" select="some $attr in @* satisfies $tokenization-rules[xpath = swat:get-attribute-path($attr)]" />
        
        <!-- Process attributes first -->
        <xsl:for-each select="@*">
            <xsl:variable name="attr-path" select="swat:get-attribute-path(.)" />
            <xsl:variable name="attr-rule" select="$tokenization-rules[xpath = $attr-path]" />
            <xsl:if test="$attr-rule">
                <xsl:variable name="final-token-name">
                    <xsl:choose>
                        <xsl:when test="contains($attr-rule/token, '{index}')">
                            <!-- Attributes use same logic as element text:
                                 - Top-level elements: use own index
                                 - Elements with siblings of same name: use own index
                                 - Other child elements: use parent's index -->
                            <xsl:variable name="index-to-use">
                                <xsl:choose>
                                    <xsl:when test="$is-top-level">
                                        <xsl:value-of select="swat:get-grouped-index(..)" />
                                    </xsl:when>
                                    <xsl:when test="$has-siblings-with-same-name">
                                        <!-- Multiple siblings with same name - use own index -->
                                        <xsl:value-of select="swat:get-grouped-index(..)" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <!-- Single child element - use parent's parent index -->
                                        <xsl:value-of select="swat:get-grouped-index(../..)" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:value-of select="replace($attr-rule/token, '\{index\}', string($index-to-use))" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$attr-rule/token" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="concat($final-token-name, '=', ., '&#xa;')" />
            </xsl:if>
        </xsl:for-each>
        
        <!-- Process element text value (only if it has text content and a text rule) -->
        <xsl:if test="$text-rule and text()[normalize-space()]">
            <xsl:variable name="final-token-name">
                <xsl:choose>
                    <xsl:when test="contains($text-rule/token, '{index}')">
                        <!-- Determine which index to use:
                             - Top-level elements: use their own grouped index
                             - Child elements with multiple siblings of same name: use their own index
                             - Other child elements: use parent's index -->
                        <xsl:variable name="index-to-use">
                            <xsl:choose>
                                <xsl:when test="$is-top-level">
                                    <xsl:value-of select="swat:get-grouped-index(.)" />
                                </xsl:when>
                                <xsl:when test="$has-siblings-with-same-name">
                                    <!-- Multiple siblings with same name - use own index -->
                                    <xsl:value-of select="swat:get-grouped-index(.)" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <!-- Single child element - use parent's index -->
                                    <xsl:value-of select="swat:get-grouped-index(..)" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:value-of select="replace($text-rule/token, '\{index\}', string($index-to-use))" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$text-rule/token" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:value-of select="concat($final-token-name, '=', normalize-space(text()), '&#xa;')" />
        </xsl:if>
        
        <!-- Process child elements - always process unless element has ONLY text rule and text content -->
        <xsl:choose>
            <xsl:when test="$text-rule and text()[normalize-space()] and not(*)">
                <!-- Element has text rule and only text content, no children to process -->
            </xsl:when>
            <xsl:otherwise>
                <!-- Process child elements -->
                <xsl:apply-templates select="*" mode="properties" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Suppress text nodes in properties mode -->
    <xsl:template match="text()" mode="properties" />

</xsl:stylesheet>

<!-- Made with Bob -->
