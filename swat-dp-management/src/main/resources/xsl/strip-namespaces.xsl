<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:env="http://www.w3.org/2003/05/soap-envelope"
	xmlns:dp="http://www.datapower.com/schemas/management" exclude-result-prefixes="env dp">

	<xsl:output method="xml" version="1.0" encoding="UTF-8" />

	<!-- identity template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
