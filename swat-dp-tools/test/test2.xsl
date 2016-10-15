<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" omit-xml-declaration="yes" />

    <xsl:variable name="numBegin" select="count(//*[local-name()='del-config']/*)" />
    
    <xsl:template match="//dp:del-config">
        Enter the template with <xsl:value-of select="$numBegin" /> elements.
        <xsl:variable name="num-elements" select="count(*)" />
        Finished the template with <xsl:value-of select="$num-elements" /> elements. 
    </xsl:template>

</xsl:stylesheet>