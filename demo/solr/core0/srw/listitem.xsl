<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:zr="http://explain.z3950.org/dtd/2.0/"
                xmlns:mets="http://www.loc.gov/METS/"
                exclude-result-prefixes="zr mets">

    <xsl:template name="listitem">
        <xsl:param name="index"/>
        <xsl:param name="explain"/>
        <xsl:param name="lang"/>
        <xsl:param name="recordSchema"/>
        <xsl:param name="maximumRecords"/>
        <xsl:param name="relation"/>
        <xsl:param name="query"/>
        <xsl:param name="shortlist"/>
        <xsl:param name="value"/>

        <xsl:variable name="displayLabel">
            <xsl:variable name="tmp"
                          select="$explain/zr:explain/zr:indexInfo/zr:index[zr:map/zr:name=$index]/zr:title[@lang=$lang or not(@lang)]/text()"/>
            <xsl:choose>
                <xsl:when test="string-length($tmp)=0"/>
                <xsl:otherwise>
                    <xsl:value-of select="$tmp"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="map">
            <xsl:if test="$explain">
                <xsl:if test="$explain/zr:explain/zr:indexInfo/zr:index[zr:map/zr:name=$index]/zr:map/zr:name[@set='solr' and @scan_exact]">
                    <xsl:value-of
                            select="concat($explain/zr:explain/zr:indexInfo/zr:index/zr:map/zr:name[.=$index]/@set, '.', $index)"/>
                </xsl:if>
            </xsl:if>
        </xsl:variable>

        <xsl:if test="string-length($value)>0 and string-length($displayLabel)>0">
            <dt>
                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="$shortlist='yes'">
                            <xsl:choose>
                                <xsl:when test="$query=false() or string-length($map)=0">label sl</xsl:when>
                                <xsl:otherwise>label hyperlink sl</xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="$query=false() or string-length($map)=0">label</xsl:when>
                                <xsl:otherwise>label hyperlink</xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:attribute>
                <xsl:value-of select="$displayLabel"/>
            </dt>
            <xsl:choose>
                <xsl:when test="$value/dd">
                    <xsl:copy-of select="$value"/>
                </xsl:when>
                <xsl:otherwise>
                    <dd>
                        <xsl:value-of select="$value"/>
                    </dd>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>

        <xsl:choose>
            <xsl:when test="$query=false() or string-length($map)=0"/>
            <xsl:otherwise>
                <dt class="sru">query</dt>
                <dd class="sru index">
                    <xsl:value-of select="$map"/>
                </dd>
                <dd class="sru relation">
                    <xsl:value-of select="$relation"/>
                </dd>
                <dd class="sru interfieldoperator"/>
                <dd class="sru recordSchema">
                    <xsl:value-of select="$recordSchema"/>
                </dd>
                <dd class="sru maximumRecords">
                    <xsl:value-of select="$maximumRecords"/>
                </dd>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="imageitem">
        <xsl:param name="image_url"/>
        <dt>images</dt>
        <xsl:for-each select="$image_url">
            <dd class="image">
                <a href="{text()}" target="_blank">
                    <img src="{text()}" height="75"/>
                </a>
            </dd>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>