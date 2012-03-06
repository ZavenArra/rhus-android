<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- remove existing references to OtpErlang jar -->
<xsl:template match="classpathentry[contains(@path, 'libs/OtpErlang')]"></xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="classpath">
  <xsl:copy>
    <xsl:apply-templates select="classpathentry|text()"/>
    <classpathentry kind="lib" path="libs/OtpErlang-r14b03.jar"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
