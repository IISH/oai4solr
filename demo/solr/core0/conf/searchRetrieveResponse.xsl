<?xml version='1.0'?>

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:srw="http://www.loc.gov/zing/srw/">

<xsl:import href="stdiface.xsl"/>
<xsl:import href="dublinCoreRecord.xsl"/>
<xsl:import href="MarcXmlToTaggedText.xsl"/>

<xsl:variable name="title">Results for Search: <xsl:value-of select="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:query"/></xsl:variable>
<xsl:variable name="dbname"><xsl:value-of select="/srw:searchRetrieveResponse/srw:extraResponseData/databaseTitle"/></xsl:variable>
<xsl:variable name="count"><xsl:value-of select="/srw:searchRetrieveResponse/srw:numberOfRecords"/></xsl:variable>

<xsl:template match="/">
<xsl:call-template name="stdiface">
<xsl:with-param name="title" select="$title"/>
<xsl:with-param name="dbname" select="$dbname"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="srw:searchRetrieveResponse">
  <div id="nav">
	  <span class="label">Links:</span><a href="?">&lt;&lt; Back to Search</a><!--span class="bullet">&bull;</span><a href="">Terminologies Home</a--> 
    </div>
  <div id="content">
    <xsl:apply-templates select="srw:diagnostics"/>
    <h1><xsl:value-of select="$title"/></h1>
  	<div id="recordDisplayDetails">
      <xsl:apply-templates select="srw:resultSetId"/>
      <xsl:apply-templates select="srw:numberOfRecords"/>
      <!--xsl:apply-templates select="srw:extraResponseData"/-->
      <xsl:call-template name="prev-nextRecord"/>
      </div>
    <xsl:apply-templates select="srw:records"/>
    <div id="bottomlinks">
      <xsl:call-template name="prev-nextRecord"/>
      </div>
    </div> <!--content-->
  </xsl:template>

<xsl:template match="srw:numberOfRecords">
  Records found:<xsl:text> </xsl:text><xsl:value-of select="."/><br/>
</xsl:template>

<xsl:template match="srw:resultSetId">
  Result Set Identifier:<xsl:text> </xsl:text><xsl:value-of select="."/><br/>
  <xsl:apply-templates select="srw:resultSetIdleTime"/>
</xsl:template>

<xsl:template match="srw:resultSetIdleTime">
  <xsl:text> </xsl:text>(Will last for<xsl:text> </xsl:text><xsl:value-of select="."/><xsl:text> </xsl:text>seconds)
</xsl:template>

<xsl:template match="srw:records">
  <div id="recordsWrapper">
    <xsl:apply-templates/>
    </div>
  </xsl:template>

<xsl:template match="srw:record">
  <div class="recordDesc">
		<h3 class="recordCount">Record: <xsl:value-of select="srw:recordPosition"/> of <xsl:value-of select="$count"/></h3>
    <xsl:apply-templates select="child::srw:recordSchema"/>
    </div>
    <xsl:apply-templates select="child::srw:recordData"/>
</xsl:template>

<xsl:template match="srw:record/srw:recordSchema">
  <div class="recordSchema">
    Schema: <xsl:variable name="schema" select="."/> 
    <xsl:choose>
      <xsl:when test="$schema = 'http://www.openarchives.org/OAI/2.0/#header'">
        OAI Header
        </xsl:when>
      <xsl:when test="$schema = 'info:srw/schema/1/dc-v1.1'">
	      Dublin Core
        </xsl:when>
      <xsl:when test="$schema = 'info:srw/schema/1/marcxml-v1.1'">
	      MARC XML
        </xsl:when>
      <xsl:when test="$schema = 'info:srw/schema/1/mods-v3.0'">
	      MODS
        </xsl:when>
      <xsl:when test="$schema = 'http://srw.o-r-g.org/schemas/ccg/1.0/'">
	      Collectable Card Schema
        </xsl:when>
      <xsl:otherwise>
	      <xsl:value-of select="$schema"/>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

<xsl:template match="srw:recordPosition">
  Position: <xsl:value-of select="."/> <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="srw:nextRecordPosition">
  <!-- Not used -->
</xsl:template>

<xsl:template match="srw:recordData">
  <div class="recordWrapper">
    <xsl:choose>
      <xsl:when test="../srw:recordPacking = 'string'">
        <pre><xsl:value-of select="."/></pre>
        </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>


<xsl:template name="prev-nextRecord">
  <xsl:variable name="startRecord"
    select="number(/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:startRecord)"/>

  <xsl:variable name="resultSetTTL">
    <xsl:if test="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:resultSetTTL">
      <xsl:text>&amp;resultSetTTL=</xsl:text>
      <xsl:value-of select="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:resultSetTTL"/>
      </xsl:if>
    </xsl:variable>

  <xsl:variable name="recordPacking">
    <xsl:text>&amp;recordPacking=</xsl:text>
    <xsl:value-of select="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:recordPacking"/>
    </xsl:variable>

  <xsl:variable name="numRecs">
    <xsl:value-of select="number(/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:maximumRecords)"/>
    </xsl:variable>

  <xsl:variable name="maximumRecords">
    <xsl:text>&amp;maximumRecords=</xsl:text>
    <xsl:value-of select="$numRecs"/>
    </xsl:variable>

  <xsl:variable name="prev" select="$startRecord - $numRecs"/>

  <xsl:variable name="recordSchema">
    <xsl:if test="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:recordSchema">&amp;recordSchema=<xsl:value-of select="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:recordSchema"/></xsl:if>
    </xsl:variable>

  <xsl:variable name="sortKeys"><xsl:if test="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:sortKeys">&amp;sortKeys=<xsl:value-of select="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:sortKeys"/></xsl:if></xsl:variable>
  <xsl:variable name="query">
    <xsl:choose>
      <xsl:when test="/srw:searchRetrieveResponse/srw:resultSetId">
          <xsl:text>&amp;query=cql.resultSetId=</xsl:text>
          <xsl:value-of select="/srw:searchRetrieveResponse/srw:resultSetId"/>
          </xsl:when>
      <xsl:otherwise>
        <xsl:text>&amp;query=</xsl:text>
        <xsl:value-of select="/srw:searchRetrieveResponse/srw:echoedSearchRetrieveRequest/srw:query"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

  <xsl:if test="$prev>0">
    <a href="?operation=searchRetrieve{$query}{$maximumRecords}{$resultSetTTL}{$recordSchema}{$sortKeys}{$recordPacking}&amp;startRecord={$prev}">
      <xsl:text>&lt;&lt;Previous Record(s)</xsl:text>
      </a>
      <xsl:text> </xsl:text>
    </xsl:if>


  <xsl:if test="/srw:searchRetrieveResponse/srw:nextRecordPosition">
    <a href="?operation=searchRetrieve{$query}{$maximumRecords}{$resultSetTTL}{$recordSchema}{$sortKeys}{$recordPacking}&amp;startRecord={/srw:searchRetrieveResponse/srw:nextRecordPosition}">
      <xsl:text>Next Record(s)&gt;&gt;</xsl:text>
      </a>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
