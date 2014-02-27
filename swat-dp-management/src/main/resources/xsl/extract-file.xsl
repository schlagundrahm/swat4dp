<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:xalan="http://xml.apache.org/xslt" 
	xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
	<xsl:output method="text" omit-xml-declaration="yes" />
	<xsl:template match="/">
       <xsl:value-of select="soapenv:Envelope/soapenv:Body/dp:response/dp:file" />
	</xsl:template>	       
</xsl:stylesheet>
