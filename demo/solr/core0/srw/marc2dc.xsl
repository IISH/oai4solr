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
    solr home/conf/solrconfig.xml/config/requestHandler[@class='org.socialhistory.solr.sru.SRURequestHandler' and marc:subfieldlst[@name='srw_properties']
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:marc="http://www.loc.gov/MARC21/slim"
                xmlns:iisg="http://www.iisg.nl/api/sru/"
                exclude-result-prefixes="dc marc"
        >


    <!-- http://www.loc.gov/marc/marc2dc.html -->
<xsl:template name="marc2dc">
        <xsl:param name="metadata" />

    <xsl:if test="$metadata/../extraRecordData/iisg:iisg/iisg:isShownAt">
        <dc:identifier><xsl:value-of select="$metadata/../extraRecordData/iisg:iisg/iisg:isShownAt/text()"/></dc:identifier>
    </xsl:if>
    <xsl:if test="$metadata/marc:record/marc:controlfield[@tag='001']">
        <dc:identifier><xsl:value-of select="$metadata/marc:record/marc:controlfield[@tag='001']"/></dc:identifier>
    </xsl:if>

        <xsl:for-each select="$metadata/marc:record/marc:datafield">
    
            <xsl:choose>
    <xsl:when test="@tag='100' or @tag='110' or @tag='111' or @tag='700' or @tag='710' or tag='711' or @tag='720'">
        <dc:contributor><xsl:value-of select="marc:subfield/text()"/></dc:contributor>
    </xsl:when>

     <xsl:when test="@tag='651' or @tag='662' or @tag='751' or @tag='752'">
        <dc:coverage><xsl:value-of select="marc:subfield/text()"/></dc:coverage>
    </xsl:when>

    <xsl:when test="@tag='260' and marc:subfield[@code='c' or @code='g']">
        <dc:date><xsl:value-of select="marc:subfield/text()"/></dc:date>
    </xsl:when>

    <xsl:when test="@tag&gt;499 and @tag&lt;600 and not(@tag=506 or @tag=530 or @tag=540 or @tag=546)">
        <dc:description><xsl:value-of select="marc:subfield/text()"/></dc:description>
    </xsl:when>

    <xsl:when test="@tag='340'">
        <dc:format><xsl:value-of select="marc:subfield/text()"/></dc:format>
    </xsl:when>
    <xsl:when test="@tag='856' and marc:subfield[@code='q']">
        <dc:format><xsl:value-of select="marc:subfield/text()"/></dc:format>
    </xsl:when>

    <xsl:when test="@tag='020' or @tag='022' or @tag='024'">
        <dc:identifier><xsl:value-of select="marc:subfield/text()"/></dc:identifier>
    </xsl:when>

    <xsl:when test="@tag='41' and marc:subfield[@code='a' or @code='b' or @code='d' or @code='e' or @code='f' or @code='g' or @code='h' or @code='j']">
        <dc:language><xsl:value-of select="marc:subfield/text()"/></dc:language>
    </xsl:when>
    <xsl:when test="@tag='546'">
        <dc:language><xsl:value-of select="marc:subfield/text()"/></dc:language>
    </xsl:when>

    <xsl:when test="@tag='260' and marc:subfield[@code='a' or @code='b']">
        <dc:publisher><xsl:value-of select="marc:subfield/text()"/></dc:publisher>
    </xsl:when>

    <xsl:when test="@tag='530'">
        <dc:relation><xsl:value-of select="marc:subfield/text()"/></dc:relation>
    </xsl:when>
    <xsl:when test="@tag&gt;759 and @tag&lt;788 and marc:subfield[@code='o' or @code='t']">
        <dc:relation><xsl:value-of select="marc:subfield/text()"/></dc:relation>
    </xsl:when>

    <xsl:when test="@tag='506' or @tag='540'">
        <dc:rights><xsl:value-of select="marc:subfield/text()"/></dc:rights>
    </xsl:when>

    <xsl:when test="@tag='534' and marc:subfield[@code='t']">
        <dc:source><xsl:value-of select="marc:subfield/text()"/></dc:source>
    </xsl:when>
    <xsl:when test="@tag='786' and marc:subfield[@code='o' or @code='t']">
        <dc:source><xsl:value-of select="marc:subfield/text()"/></dc:source>
    </xsl:when>

    <xsl:when test="@tag='050' or @tag='060' or @tag='080' or @tag='082' or @tag='600' or @tag='610' or @tag='611' or @tag='630' or @tag='650' or @tag='653'">
        <dc:subject><xsl:value-of select="marc:subfield/text()"/></dc:subject>
    </xsl:when>

    <xsl:when test="@tag='245' or @tag='246'">
        <dc:title><xsl:value-of select="marc:subfield/text()"/></dc:title>
    </xsl:when>

    <xsl:when test="@tag='655'">
        <dc:type><xsl:value-of select="marc:subfield/text()"/></dc:type>
    </xsl:when>

            <xsl:otherwise/>
    
    </xsl:choose></xsl:for-each>
    
    </xsl:template>

    </xsl:stylesheet>