<?xml version="1.0" encoding="UTF-8"?>
<!-- use with: xmlstarlet tr mscx-cln.xsl - <in.mscx >out.mscx #-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="@*">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="museScore">
    <xsl:copy>
      <xsl:apply-templates select="@*|programVersion|programRevision|Score"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="programVersion">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="programRevision">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Score">
    <xsl:copy>
      <xsl:apply-templates select="@*|Division|Part|Staff"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Division">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Part">
    <xsl:copy>
      <xsl:apply-templates select="@*|Staff"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Staff">
    <xsl:copy>
      <xsl:apply-templates select="@*|Measure"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Measure">
    <xsl:copy>
      <xsl:apply-templates select="@*|irregular|KeySig|TimeSig|Chord[not(track) and not(appoggiatura)]|Rest[not(track)]"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="KeySig">
    <xsl:copy>
      <xsl:apply-templates select="@*|accidental"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="accidental">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="TimeSig">
    <xsl:copy>
      <xsl:apply-templates select="@*|subtype|sigN|sigD"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="subtype">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="sigN">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="sigD">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Chord">
    <xsl:choose>
      <xsl:when test="not(Note[not(play)])">
        <Rest>
          <xsl:apply-templates select="@*|dots|durationType"/>
        </Rest>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|dots|durationType|Note[not(play)]"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="dots">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="durationType">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="duration">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Note">
    <xsl:copy>
      <xsl:apply-templates select="@*|Accidental|pitch|tpc"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Accidental">
    <xsl:copy>
      <xsl:apply-templates select="@*|subtype"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="subtype">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="pitch">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="tpc">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="Rest">
    <xsl:copy>
      <xsl:apply-templates select="@*|durationType|duration"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
