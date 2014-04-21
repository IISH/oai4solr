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

<!-- Declare this schema in:
    solr home/conf/solrconfig.xml/config/requestHandler[@class='org.socialhistory.solr.sru.SRURequestHandler']/lst[@name='srw_properties']
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:marc="http://www.loc.gov/MARC21/slim"
        >

<xsl:template name="dc2marc">
        <xsl:param name="metadata" />

        <xsl:for-each select="$metadata">

        <xsl:variable name="ln" select="local-name()" />

        <xsl:choose>
            <xsl:when test="$ln='title'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">245</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='contributor' or $ln='creator'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">100</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='coverage'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">651</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$ln='date'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">260</xsl:with-param>
                    <xsl:with-param name="code">c</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='description'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">500</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='format'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">340</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='language'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">041</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='publisher'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">260</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='relation'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">530</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='rights'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">506</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='source'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">534</xsl:with-param>
                    <xsl:with-param name="code">t</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='subject'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">650</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$ln='type'">
                <xsl:call-template name="datafield">
                    <xsl:with-param name="tag">655</xsl:with-param>
                    <xsl:with-param name="code">a</xsl:with-param>
                    <xsl:with-param name="value" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise />
        </xsl:choose>

        </xsl:for-each>

    </xsl:template>

    <xsl:template name="datafield">
        <xsl:param name="tag"/>
        <xsl:param name="code"/>
        <xsl:param name="value"/>

        <marc:datafield tag="{$tag}" ind1="" ind2="">
            <marc:subfield code="{$code}">
                <xsl:value-of select="$value" />
            </marc:subfield>
        </marc:datafield>

    </xsl:template>

    </xsl:stylesheet>