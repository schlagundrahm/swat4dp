<?xml version="1.0" encoding="UTF-8" ?>
<!--
	apply to dp-aux/drMgmt.xml from any application domain backup
	author: Hermann Stamm-Wilbrandt (stammw@de.ibm.com)
-->
<xsl:stylesheet
	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:func="http://exslt.org/functions" xmlns:dot="http://graphwiz.org/dot">

	<xsl:output method="text" />

	<func:function name="dot:label" >
		<xsl:param name="str" />
		<func:result>
			"
			<xsl:value-of select="$str" />
			"
		</func:result>
	</func:function>

	<func:function name="dot:class">
		<xsl:param name="node" />
		<func:result>
			<xsl:value-of select="concat($node/@name,'(',$node/@classID,')')" />
		</func:result>
	</func:function>

	<xsl:template match="text()" />
	<xsl:template match="/management-information/config-objects//object[name(..)!='config-objects']">
		<xsl:variable
			name="parent"
			select="dot:label(dot:class(../..))" />
		<xsl:variable
			name="node"
			select="dot:label(dot:class(.))" />
		<xsl:value-of select="$parent" />
		->
		<xsl:value-of select="$node" />
		[tailport=e,headport=w];
		<xsl:apply-templates select="*" />
	</xsl:template>

	<xsl:template match="/">
		digraph G {
		size="7,10";
		rankdir=LR;
		node [shape=none,fontsize=50];
		concentrate=true;
		<xsl:apply-templates select="*" />
		}
	</xsl:template>

</xsl:stylesheet>