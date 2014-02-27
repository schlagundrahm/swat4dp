<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:strip-space elements="*"/>
	
	<xsl:template match="/datapower-configuration">
		<xsl:copy>
		<xsl:attribute name="version"><xsl:value-of select="@version" /></xsl:attribute>
		<xsl:attribute name="domain"><xsl:value-of select="configuration/@domain" /></xsl:attribute>

		<xsl:element name="Object">
			<xsl:attribute name="type"><xsl:text>File</xsl:text></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="normalize-space(export-details/custom-ui-file)" /></xsl:attribute>
			<xsl:attribute name="parent"><xsl:text>Header</xsl:text></xsl:attribute>
		</xsl:element>
								
		<xsl:for-each select="configuration/*">
			<xsl:choose>
				<xsl:when test="@intrinsic='true'"></xsl:when>
				<xsl:otherwise>
					<xsl:element name="Object">
						<xsl:attribute name="type"><xsl:value-of select="name()" /></xsl:attribute>
						<xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
					</xsl:element>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:for-each select="child::*">
				<xsl:choose>
					<xsl:when test="starts-with(normalize-space(text()),'local:///') or starts-with(normalize-space(text()),'cert:///') or starts-with(normalize-space(text()),'sharedcert:///') or starts-with(normalize-space(text()),'pubcert:///')">
						<xsl:value-of select="current()" />
						<xsl:element name="Object">
							<xsl:attribute name="type"><xsl:text>File</xsl:text></xsl:attribute>
							<xsl:attribute name="name"><xsl:value-of select="normalize-space(text())" /></xsl:attribute>
							<xsl:attribute name="parent"><xsl:value-of select="./../@name" /></xsl:attribute>
						</xsl:element>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
		</xsl:for-each>
		<xsl:for-each select="files/*">
			<xsl:choose>
				<xsl:when test="@internal='true' or starts-with(@name,'store:///') or starts-with(@name,'webgui:///')"></xsl:when>
				<xsl:otherwise>
					<xsl:element name="Object">
						<xsl:attribute name="type"><xsl:text>File</xsl:text></xsl:attribute>
						<xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
						<xsl:attribute name="parent"><xsl:text>Files</xsl:text></xsl:attribute>
					</xsl:element>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*|node()">
    	<xsl:copy>
      		<xsl:apply-templates select="@*|node()"/>
    	</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>