<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:param name="domain" />
    <xsl:param name="filestore" />

    <xsl:template match="//file">

        <xsl:variable name="index" select="position()" />
        <xsl:variable name="filename" select="@name" />

        <xsl:variable name="request-filename"
            select="concat('get-file-', $index,'-',translate(translate($filename,':',''),'/','_'),'.xml')" />
        <xsl:variable name="response-filename"
            select="concat('get-file-', $index,'-',translate(translate($filename,':',''),'/','_'),'-response.xml')" />
        <xsl:message>
            <xsl:value-of select="concat('filename=',$filename)" />
        </xsl:message>

        <xsl:result-document method="xml" href="{$request-filename}" omit-xml-declaration="yes">
            <xsl:comment>
                <xsl:value-of select="concat(' Export file #', $index, ' ')" />
            </xsl:comment>
            <xsl:element name="soapenv:Envelope">
                <xsl:element name="soapenv:Body">
                    <xsl:element name="dp:request">
                        <xsl:attribute name="domain"><xsl:value-of select="$domain" /></xsl:attribute>

                        <xsl:element name="dp:get-file">
                            <xsl:attribute name="name"><xsl:value-of select="$filename" /></xsl:attribute>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:result-document>
        <xsl:element name="fileresponse">
            <xsl:attribute name="path">
            <xsl:value-of select="substring-before(substring-after($filename, ':/'), '/')" />
         </xsl:attribute>
            <xsl:attribute name="location">
            <xsl:call-template name="get-file-location">
               <xsl:with-param name="input" select="$filename" />
            </xsl:call-template>
         </xsl:attribute>
            <xsl:attribute name="file">
            <xsl:call-template name="get-file-name">
               <xsl:with-param name="input" select="$filename" />
            </xsl:call-template>
         </xsl:attribute>
            <xsl:value-of select="$response-filename" />
        </xsl:element>

    </xsl:template>

    <xsl:template match="//dir">

        <xsl:variable name="dirname" select="@name" />
        <xsl:message>
            <xsl:value-of select="concat('dirname=',$dirname)" />
        </xsl:message>
        <xsl:message>
            <xsl:value-of select="concat('filestore=',$filestore)" />
        </xsl:message>
        <xsl:for-each select="document($filestore)//directory[@name=$dirname]/file/@name">
            <xsl:variable name="index" select="position()" />
            <xsl:variable name="filename" select="concat($dirname, '/', .)" />
            <xsl:variable name="request-filename"
                select="concat('get-file-', $index,'-',translate(translate($filename,':',''),'/','_'),'.xml')" />
            <xsl:variable name="response-filename"
                select="concat('get-file-', $index,'-',translate(translate($filename,':',''),'/','_'),'-response.xml')" />
            <xsl:message>
                <xsl:value-of select="concat('filename=',$filename)" />
            </xsl:message>

            <xsl:result-document method="xml" href="{$request-filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' Export file #', $index, ' ')" />
                </xsl:comment>
                <xsl:element name="soapenv:Envelope">
                    <xsl:element name="soapenv:Body">
                        <xsl:element name="dp:request">
                            <xsl:attribute name="domain"><xsl:value-of select="$domain" /></xsl:attribute>

                            <xsl:element name="dp:get-file">
                                <xsl:attribute name="name"><xsl:value-of select="$filename" /></xsl:attribute>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:result-document>
            <xsl:element name="fileresponse">
                <xsl:attribute name="path">
            <xsl:value-of select="substring-before(substring-after($filename, ':/'), '/')" />
         </xsl:attribute>
                <xsl:attribute name="location">
            <xsl:call-template name="get-file-location">
               <xsl:with-param name="input" select="$filename" />
            </xsl:call-template>
         </xsl:attribute>
                <xsl:attribute name="file">
            <xsl:call-template name="get-file-name">
               <xsl:with-param name="input" select="$filename" />
            </xsl:call-template>
         </xsl:attribute>
                <xsl:value-of select="$response-filename" />
            </xsl:element>
        </xsl:for-each>

    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template name="get-file-name">
        <xsl:param name="input" />

        <xsl:choose>
            <xsl:when test="contains($input, '/')">
                <xsl:call-template name="get-file-name">
                    <xsl:with-param name="input" select="substring-after($input, '/')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="get-file-location">
        <xsl:param name="input" />

        <xsl:choose>
            <xsl:when test="contains($input, ':///')">
                <xsl:value-of select="substring-after($input, '://')" />
            </xsl:when>
            <xsl:when test="contains($input, '://')">
                <xsl:value-of select="substring-after($input, ':/')" />
            </xsl:when>
            <xsl:when test="contains($input, ':/')">
                <xsl:value-of select="substring-after($input, ':')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

</xsl:stylesheet>