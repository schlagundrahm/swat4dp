<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
	xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
	<xsl:output method="xml" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
	<xsl:strip-space elements="datapower-configuration"/>
	<xsl:param name="filterObjectTypes" />
	<xsl:variable name="filterObjectTypesVar" select="$filterObjectTypes" />
	<xsl:template match="/datapower-configuration">
		<!--  create new file? -->
		<xsl:copy>
		    <xsl:attribute name="version">
                <xsl:value-of select="@version" />
            </xsl:attribute>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="configuration">
		<xsl:copy>
			<xsl:attribute name="domain">@domain.name@</xsl:attribute>
           <xsl:apply-templates />
		</xsl:copy>
	</xsl:template>		
	<!-- Filter out all objects, that have the attribute intrinsic (assumed it's value is set to 'true') -->
	<xsl:template match="*[@intrinsic]" />
	<xsl:template match="*">
	   <xsl:choose>
	       <xsl:when test="contains($filterObjectTypesVar, local-name())">
	       </xsl:when>
	       <xsl:otherwise>
	           <xsl:copy-of select="." />
	       </xsl:otherwise>
	   </xsl:choose>
	</xsl:template>
</xsl:stylesheet>