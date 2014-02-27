<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:strip-space elements="*"/>

	<xsl:template match="/datapower-configuration">
		<xsl:copy>
			<xsl:attribute name="version"><xsl:value-of select="@version" /></xsl:attribute>
			<xsl:attribute name="domain"><xsl:value-of select="@domain" /></xsl:attribute>
			<xsl:for-each select="*">
				<xsl:sort select="concat(name(),@type,@parent,@name)" />
				<xsl:apply-templates select="." />
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|node()">
    	<xsl:copy>
      		<xsl:apply-templates select="@*|node()"/>
    	</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>