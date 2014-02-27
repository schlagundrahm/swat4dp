<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common" version="1.0">

  <xsl:param name="fileSelection" />
  <xsl:variable name="fileSelectionVar" select="$fileSelection" />
  
  <!-- Replace // with / everywhere if we're only interested
   	in immediate children of /RootElement. -->

  <xsl:variable name="docA" select="/" />
  <xsl:variable name="docB" select="document($fileSelectionVar)"/>

  <!-- This produces a whole nother copy of both docs!
       So, is the performance cost worth it?? -->

  <xsl:variable name="sortedNodesA">
    <!-- produce a sorted, flattened RTF of A's nodes -->
    <xsl:for-each select="$docA/RootElement//*">
      <xsl:sort select="name()" />
      <xsl:copy-of select="." />
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="sortedNodesB">
    <!-- produce a sorted, flattened RTF of B's nodes -->
    <xsl:for-each select="$docB/RootElement//*">
      <xsl:sort select="name()" />
      <xsl:copy-of select="." />
    </xsl:for-each>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:call-template name="recurse">
      <xsl:with-param name="nodesA"
        select="exslt:node-set($sortedNodesA)/*" />
      <xsl:with-param name="nodesB"
        select="exslt:node-set($sortedNodesB)/*" />
    </xsl:call-template>
  </xsl:template>
 
  <xsl:template name="recurse">
    <xsl:param name="nodesA" />
    <xsl:param name="nodesB" />
    <xsl:if test="$nodesA | $nodesB">
      <xsl:variable name="nameA" select="name($nodesA[1])" />
      <xsl:variable name="nameB" select="name($nodesB[1])" />
      <xsl:variable name="compar">
        <xsl:call-template name="compare-names">
          <xsl:with-param name="a" select="$nodesA[1]" />
          <xsl:with-param name="b" select="$nodesB[1]" />
        </xsl:call-template>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="0 > $compar"> <!-- $nodesA[1] is alph. first -->
          <p><xsl:value-of select="$nameA" /> is only in doc A.</p>
          <xsl:call-template name="recurse">
            <xsl:with-param name="nodesA" select="$nodesA[position()>1]" />
            <xsl:with-param name="nodesB" select="$nodesB" />
          </xsl:call-template>
        </xsl:when>

        <xsl:when test="$compar > 0"> <!-- $nodesB[1] is alph. first -->
          <p><xsl:value-of select="$nameB" /> is only in doc B.</p>
          <xsl:call-template name="recurse">
            <xsl:with-param name="nodesA" select="$nodesA" />
            <xsl:with-param name="nodesB" select="$nodesB[position()>1]" />
          </xsl:call-template>
        </xsl:when>

        <xsl:otherwise>
          <p><xsl:value-of select="$nameB" /> is in both documents.
            <!-- Do I need string(text(...))? -->
            <xsl:if
              test="string($nodesA[1]/text()) != string($nodesB[1]/text())">
              But their contents differ:
              '<xsl:value-of select="$nodesA[1]/text()" />' !=
              '<xsl:value-of select="$nodesB[1]/text()" />'.
            </xsl:if>
          </p>
          <xsl:call-template name="recurse">
            <xsl:with-param name="nodesA" select="$nodesA[position()>1]" />
            <xsl:with-param name="nodesB" select="$nodesB[position()>1]" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template name="compare-names">
    <!-- Output -1, 0, or 1 as name of node A sorts before, equal to,
         or after name of node B. -->
    <xsl:param name="a" />
    <xsl:param name="b" />
    <xsl:choose>
      <xsl:when test="name($a) = name($b)"> 0 </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="$a|$b">
          <xsl:sort select="name()" />
          <xsl:if test="position() = 1">
            <xsl:choose>
              <xsl:when test="name(.) = name($a)"> -1 </xsl:when>
              <xsl:otherwise> 1 </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
