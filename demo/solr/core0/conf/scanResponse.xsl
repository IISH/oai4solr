<?xml version='1.0'?>

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:srw="http://www.loc.gov/zing/srw/"
     xmlns:xcql="http://www.loc.gov/zing/cql/xcql/">

<xsl:import href="?file=stdiface.xsl&amp;contentType=text/xml"/>

<xsl:variable name="title">Result of scan for term: <xsl:value-of select="/srw:scanResponse/srw:echoedScanRequest/srw:scanClause"/></xsl:variable>
<xsl:variable name="maximumTerms"><xsl:value-of select="/srw:scanResponse/srw:echoedScanRequest/srw:maximumTerms"/></xsl:variable>
<xsl:variable name="indexRelation"> <xsl:value-of select="/srw:scanResponse/srw:echoedScanRequest/srw:xScanClause/xcql:index"/><xsl:text> </xsl:text><xsl:value-of select="/srw:scanResponse/srw:echoedScanRequest/srw:xScanClause/xcql:relation/xcql:value"/><xsl:text> </xsl:text></xsl:variable>
<xsl:variable name="dbname"><xsl:value-of select="/srw:scanResponse/srw:extraResponseData/databaseTitle"/></xsl:variable>

<xsl:template match="/">
<xsl:call-template name="stdiface">
<xsl:with-param name="title" select="$title"/>
<xsl:with-param name="dbname" select="$dbname"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="srw:scanResponse">
  <div id="nav">
	  <span class="label">Links:</span><a href="?">&lt;&lt; Back to Search</a><!--span class="bullet">&bull;</span><a href="">Terminologies Home</a--> 
    </div>
  <div id="content">
    <xsl:apply-templates select="srw:diagnostics"/>
    <h1><xsl:value-of select="$title"/></h1>
    <xsl:apply-templates select="srw:terms"/>
    </div>
  </xsl:template>

<xsl:template match="srw:terms">
  <xsl:call-template name="prev-nextTerms"/>
  <table width="50%" cellspacing="0" class="formtable">
    <tr><th>Term</th><th>Frequency</th></tr>
    <xsl:apply-templates/>
    </table>
  <xsl:call-template name="prev-nextTerms"/>
  </xsl:template>

<xsl:template match="srw:term">
<tr>
  <xsl:apply-templates/>
</tr>
</xsl:template>

<xsl:template match="srw:value"><xsl:variable name="encoded"><xsl:call-template name="encode-uri"><xsl:with-param name="text" select="."/></xsl:call-template></xsl:variable>

<td>
  <a href="?operation=searchRetrieve&amp;version=1.1&amp;recordPacking=xml&amp;query={$indexRelation}&quot;{normalize-space($encoded)}&quot;&amp;maximumRecords=1&amp;startRecord=1"><xsl:value-of select="."/></a>
</td>
</xsl:template>

<xsl:template match="srw:term/srw:numberOfRecords">
<td><xsl:value-of select="."/></td>
</xsl:template>

<xsl:template match="srw:echoedScanRequest"/>

<xsl:template name="prev-nextTerms">
<p>
&lt;--
<a><xsl:attribute name="href">?operation=scan&amp;scanClause=<xsl:value-of select="$indexRelation"/>"<xsl:value-of select ="./srw:term[1]/srw:value"/>"&amp;responsePosition=<xsl:value-of select="$maximumTerms"/>&amp;version=1.1<xsl:if test="/srw:scanResponse/srw:echoedScanRequest/srw:maximumTerms">&amp;maximumTerms=<xsl:value-of select="$maximumTerms"/></xsl:if></xsl:attribute>
Previous
</a>

|

<a>
<xsl:attribute name="href">?operation=scan&amp;scanClause=<xsl:value-of select="$indexRelation"/>"<xsl:value-of select ="./srw:term[count(//srw:scanResponse/srw:terms/srw:term)]/srw:value"/>"&amp;responsePosition=1&amp;version=1.1<xsl:if test="/srw:scanResponse/srw:echoedScanRequest/srw:maximumTerms">&amp;maximumTerms=<xsl:value-of select="$maximumTerms"/></xsl:if>
</xsl:attribute>
Next 
</a>
--&gt;
</p>
</xsl:template>

<!-- generate entities by replacing &, ", < and > in $text -->
<xsl:template name="encode-uri">
  <xsl:param name="text" />
  <xsl:variable name="tmp">
    <xsl:call-template name="replace-substring">
      <xsl:with-param name="from" select="'&gt;'" />
      <xsl:with-param name="to" select="'%3E'" />
      <xsl:with-param name="value">
        <xsl:call-template name="replace-substring">
          <xsl:with-param name="from" select="'&lt;'" />
          <xsl:with-param name="to" select="'%3C'" />
          <xsl:with-param name="value">
            <xsl:call-template name="replace-substring">
              <xsl:with-param name="from" select="'&amp;'" />
              <xsl:with-param name="to" select="'%26'" />
              <xsl:with-param name="value" select="$text" />
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of select="$tmp" />
  </xsl:template>

<!-- replace in $value substring $from with $to -->
<xsl:template name="replace-substring">
  <xsl:param name="value" />
  <xsl:param name="from" />
  <xsl:param name="to" />
  <xsl:choose>
    <xsl:when test="contains($value,$from)">
      <xsl:value-of select="substring-before($value,$from)" />
      <xsl:value-of select="$to" />
      <xsl:call-template name="replace-substring">
        <xsl:with-param name="value" select="substring-after($value,$from)" />
        <xsl:with-param name="from" select="$from" />
        <xsl:with-param name="to" select="$to" />
        </xsl:call-template>
      </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$value" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
