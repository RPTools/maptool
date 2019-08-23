<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE stylesheet [
  <!ENTITY bull "&#8226;">
  <!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output omit-xml-declaration="yes" indent="yes" method="xml"/>

  <xsl:template match="@* | node()">
     <xsl:apply-templates select="@* | node()"/>
  </xsl:template>

  <xsl:template match="//div[@id = 'mw-content-text']">
     <xsl:copy>
        <xsl:apply-templates select="@* | node()" mode="found"/>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="comment() | div[@id='toc'] | div[@class='template_stub']" mode="found">
     <xsl:apply-templates select="@* | node() | text()" mode="remove"/>
  </xsl:template>

  <xsl:template match="@* | node() | text()" mode="found">
     <xsl:copy>
        <xsl:apply-templates select="@* | node() | text()" mode="found"/>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="@* | node() | text()" mode="remove" />
</xsl:stylesheet>
