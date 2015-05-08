<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
   xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
   <xsl:strip-space elements="*"/>

   <xsl:param name="domain"/>

   <xsl:template match="/">
      <xsl:message>
         <xsl:value-of select="concat('starting for-each loop on ', name(.))"/>
      </xsl:message>
      <xsl:for-each select="//file">
         <xsl:variable name="index" select="position()"/>
         <xsl:variable name="filename" select="@name"/>
         <xsl:variable name="request-filename" select="concat($index,$filename,'-request.xml')"/>
         <xsl:message>
            <xsl:value-of select="concat('filename=',$filename)"/>
         </xsl:message>

         <xsl:result-document method="xml" href="{$request-filename}" omit-xml-declaration="yes">
            <xsl:comment>
               <xsl:value-of select="concat(' Export file #', $index, ' ')"/>
            </xsl:comment>
            <xsl:element name="soapenv:Envelope">
               <xsl:element name="soapenv:Body">
                  <xsl:element name="dp:request">
                     <xsl:attribute name="domain"><xsl:value-of select="$domain"/></xsl:attribute>

                     <xsl:element name="dp:get-file">
                        <xsl:attribute name="name"><xsl:value-of select="$filename"/></xsl:attribute>
                     </xsl:element>
                  </xsl:element>
               </xsl:element>
            </xsl:element>
         </xsl:result-document>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>