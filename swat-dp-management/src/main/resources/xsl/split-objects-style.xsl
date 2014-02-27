<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:xalan="http://xml.apache.org/xslt"
	xmlns:dp="http://www.datapower.com/schemas/management" 
	xmlns:str="http://exslt.org/strings" 
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
	
	<xsl:output method="xml" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
	
	<xsl:strip-space elements="datapower-configuration"/>
	
	<xsl:param name="filterObjectTypes" />
	
	<xsl:variable name="filterObjectTypesVar" select="$filterObjectTypes" />
    <!--  <xsl:variable name="filterObjectTypeList" select="tokenize($filterObjectTypes, ',')" />  -->
    
	<xsl:template match="/datapower-configuration">
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

	<!-- Filter out all objects, that have the attribute intrinsic assuming it's value is 'true' -->
	<xsl:template match="*[@intrinsic]" />
	
	<xsl:template match="*">
	   <!-- TODO: May be do it elegant for exact type matchin
                  Searching <xsl:value-of select="local-name()" /> in <xsl:value-of select="$filterObjectTypeList" />, result: <xsl:value-of select="index-of($filterObjectTypeList, local-name())" />.
       <xsl:if test="exists(index-of($filterObjectTypeList, local-name()))">
	       <xsl:copy-of select="." />
	   </xsl:if>
        -->
       <xsl:if test="contains($filterObjectTypesVar, concat(local-name(),','))">
           <xsl:copy-of select="." />
       </xsl:if>
	</xsl:template>
</xsl:stylesheet>