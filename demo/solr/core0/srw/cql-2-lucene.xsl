<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2010 International Institute for Social History, The Netherlands.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="utf-8" indent="no"/>

    <xsl:strip-space elements="*"/>

    <!-- The Document being passed to this stylesheet has the structure:
    
    <cql2lucene>
        <indices>
            <field cql="index.1">solr_index_1a</field>
            <field cql="index.1">solr_index_2b</field>
            <field cql="index.2">solr_index_2a</field>
            <field cql="index.2">solr_index_2b</field>
            <field cql="index.3">solr_index_3</field>
        </indices>
        <triple or searchClause>
            CQL structure
        </triple or searchClause>
    </cql2lucene>

    Which we will convert into a Solr query string.
    
    -->

    <xsl:template match="cql2lucene">
        <xsl:apply-templates select="triple | searchClause"/>
    </xsl:template>

    <!-- The triple is: boolean, leftOperand and rightOperand -->
    <xsl:template match="triple">

        <!-- Do not use <xsl:apply-templates /> because we want things in the right order. -->
        <xsl:apply-templates select="leftOperand"/>
        <xsl:apply-templates select="boolean"/>
        <xsl:apply-templates select="rightOperand"/>

    </xsl:template>

    <xsl:template match="leftOperand">(
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="boolean">
        <xsl:choose>
            <xsl:when test="value='and'"><xsl:call-template name="spacing"><xsl:with-param name="text">AND</xsl:with-param></xsl:call-template></xsl:when>
            <xsl:otherwise><xsl:call-template name="spacing"><xsl:with-param name="text">OR</xsl:with-param></xsl:call-template></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="rightOperand"><xsl:apply-templates/>)
    </xsl:template>

    <xsl:template match="searchClause">

        <xsl:variable name="index" select="index/text()"/>
        <xsl:variable name="relation">
            <xsl:choose>
                <xsl:when test="not(relation/value) or normalize-space(relation/value/text())=''">scr</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(relation/value/text())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="term" select="term/text()"/>

        <xsl:choose>

            <xsl:when test="$relation='any' or $relation='all' or $relation='scr'">
                <xsl:variable name="operator">
                    <xsl:choose>
                        <xsl:when test="$relation='all'"><xsl:call-template name="spacing"><xsl:with-param name="text">AND</xsl:with-param></xsl:call-template></xsl:when>
                        <xsl:when test="$relation='any'"><xsl:call-template name="spacing"><xsl:with-param name="text">OR</xsl:with-param></xsl:call-template></xsl:when>
                        <!--
                            Server choice relation: choose AND or OR to set the default ( when any or all are not specified in the sru query ).
                         -->
                        <xsl:otherwise><xsl:call-template name="spacing"><xsl:with-param name="text">AND</xsl:with-param></xsl:call-template></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:call-template name="output-tokens">
                    <xsl:with-param name="delimiter" select="' '"/>
                    <xsl:with-param name="list" select="term/text()"/>
                    <xsl:with-param name="index" select="$index"/>
                    <xsl:with-param name="operator" select="$operator"/>
                    <xsl:with-param name="relation" select="$relation"/>
                </xsl:call-template>

            </xsl:when>

            <xsl:when test="contains($relation,'&lt;') or contains($relation,'&gt;')">
                <xsl:call-template name="MultiRangeQuery">
                    <xsl:with-param name="relation" select="$relation"/>
                    <xsl:with-param name="index" select="$index"/>
                    <xsl:with-param name="term" select="$term"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$relation='exact' or $relation='=' or $relation='&lt;&gt;'">
                <xsl:if test="$relation='&lt;&gt;'"><xsl:text> -</xsl:text></xsl:if>
                <xsl:call-template name="MultiPhraseQuery">
                    <xsl:with-param name="index" select="$index"/>
                    <xsl:with-param name="term" select="$term"/>
                    <xsl:with-param name="relation" select="$relation"/>
                </xsl:call-template>
            </xsl:when>

            <!-- server choice -->
            <xsl:otherwise>
                <xsl:call-template name="TermQuery">
                    <xsl:with-param name="index" select="$index"/>
                    <xsl:with-param name="term" select="$term"/>
                    <xsl:with-param name="relation" select="$relation"/>
                </xsl:call-template>
            </xsl:otherwise>

        </xsl:choose>
    </xsl:template>

    <xsl:template name="MultiPhraseQuery">
        <xsl:param name="index"/>
        <xsl:param name="term"/>
        <xsl:param name="relation"/>

        <xsl:variable name="xpath">
            <xsl:call-template name="getindexandrelation">
                <xsl:with-param name="index" select="$index" />
                <xsl:with-param name="relation" select="$relation" />
            </xsl:call-template>
        </xsl:variable>

        (
        <xsl:for-each select="//indices/field[@cql=$xpath]">
            <xsl:value-of select="."/>:&quot;<xsl:value-of select="$term"/>&quot;
            <xsl:if test="not(position()=last())"><xsl:call-template name="spacing"><xsl:with-param name="text">OR</xsl:with-param></xsl:call-template></xsl:if>
        </xsl:for-each>
        )

    </xsl:template>

    <!--
        We can make a range query with the relations: <,>,<=, and => for strings and numbers
        But we cannot reliable support ( not without knowing the data type that is of the index ) < and >
        So:
        Relation        Type            Term        Query will be       Supported
        <               Integer         1000        [* TO 999]          No
        <               String          apple       [* TO appld]        No
        <=              Integer         1000        [* TO 1000]         Yes
        <=              String          apple       [* TO apple]        Yes
    -->
    <xsl:template name="MultiRangeQuery">
        <xsl:param name="relation"/>
        <xsl:param name="index"/>
        <xsl:param name="term"/>

         <xsl:variable name="xpath">
            <xsl:call-template name="getindexandrelation">
                <xsl:with-param name="index" select="$index" />
                <xsl:with-param name="relation" select="$relation" />
            </xsl:call-template>
        </xsl:variable>

        (
        <xsl:for-each select="//indices/field[@cql=$xpath]">

            <xsl:value-of select="."/>:[

            <xsl:choose>
                <xsl:when test="contains($relation, '&lt;')">* TO
                    <xsl:value-of select="$term"/>
                </xsl:when>
                <xsl:when test="contains($relation,'&gt;')">
                    <xsl:value-of select="$term"/> TO *
                </xsl:when>
            </xsl:choose>
            ]
            <xsl:if test="not(position()=last())"><xsl:call-template name="spacing"><xsl:with-param name="text">OR</xsl:with-param></xsl:call-template></xsl:if>

        </xsl:for-each>
        )
    </xsl:template>

    <xsl:template name="TermQuery">
        <xsl:param name="index"/>
        <xsl:param name="term"/>
        <xsl:param name="relation"/>

         <xsl:variable name="xpath">
            <xsl:call-template name="getindexandrelation">
                <xsl:with-param name="index" select="$index" />
                <xsl:with-param name="relation" select="$relation" />
            </xsl:call-template>
        </xsl:variable>

        (
        <xsl:for-each select="//indices/field[@cql=$xpath]">
            <xsl:value-of select="."/>:<xsl:value-of select="$term"/>
            <xsl:if test="not(position()=last())"><xsl:call-template name="spacing"><xsl:with-param name="text">OR</xsl:with-param></xsl:call-template></xsl:if>
        </xsl:for-each>
        )
    </xsl:template>

    <xsl:template name="spacing">
        <xsl:param name="text"/>

        <xsl:text> </xsl:text>
        <xsl:value-of select="$text" />
        <xsl:text> </xsl:text>

    </xsl:template>

    <xsl:template name="getindexandrelation">
        <xsl:param name="index"/>
        <xsl:param name="relation"/>

        <xsl:choose>
                <xsl:when test="$relation='exact'"><xsl:value-of select="concat('search_exact.', $index)" /></xsl:when>
                <xsl:when test="contains($relation,'&lt;') or contains($relation, '&gt;')"><xsl:value-of select="concat('search_range.', $index)" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat('search.', $index)"/></xsl:otherwise>
            </xsl:choose>

    </xsl:template>



    <!--
        http://www.abbeyworkshop.com/howto/xslt/xslt-split-values/index.html
    -->
<xsl:template name="output-tokens">
    <xsl:param name="list" />
    <xsl:param name="delimiter" />
    <xsl:param name="index" />
    <xsl:param name="operator" />
    <xsl:param name="relation" />

    <xsl:variable name="newlist">
      <xsl:choose>
        <xsl:when test="contains($list, $delimiter)">
          <xsl:value-of select="normalize-space($list)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat(normalize-space($list), $delimiter)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
    <xsl:value-of select="$first" />


    <xsl:call-template name="TermQuery">
        <xsl:with-param name="index" select="$index"/>
        <xsl:with-param name="term" select="$first"/>
        <xsl:with-param name="relation" select="$relation"/>
    </xsl:call-template>
    <xsl:if test="not(position()=last())">
        <xsl:value-of select="$operator"/>
    </xsl:if>

    <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />

    <xsl:if test="string-length($remaining) > 0">
      <xsl:call-template name="output-tokens">
        <xsl:with-param name="list" select="$remaining" />
        <xsl:with-param name="delimiter">
          <xsl:value-of select="$delimiter"/>
        </xsl:with-param>
          <xsl:with-param name="index" select="$index"/>
          <xsl:with-param name="operator" select="operator"/>
          <xsl:with-param name="relation" select="$relation"/>
      </xsl:call-template>

    </xsl:if>
  </xsl:template>




</xsl:stylesheet>