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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:zr="http://explain.z3950.org/dtd/2.0/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:iisg="http://www.iisg.nl/api/sru/"
                exclude-result-prefixes="dc iisg zr saxon"
        >

    <xsl:import href="marc2dc.xsl"/>

    <xsl:template match="response">

        <!-- Initialize the Solr document variables -->
        <xsl:variable name="record" select="saxon:parse(//doc/str[@name='resource']/text())/node()"/>
        <xsl:variable name="transport" select="str[@name='transport']/text()"/>
        <xsl:variable name="header" select="$record/extraRecordData"/>
        <xsl:variable name="metadata" select="$record/recordData"/>

        <!-- This is how the record inside the recordData will look like. -->
        <record>
            <recordData>
                <srw_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:srw_dc="info:srw/schema/1/dc-v1.1">
                    <xsl:call-template name="marc2dc">
                        <xsl:with-param name="metadata" select="$metadata"/>
                    </xsl:call-template>
                </srw_dc:dc>
            </recordData>
            <xsl:if test="not($transport='JSON')">
                <extraRecordData>
                    <xsl:copy-of select="$header/*/*"/>
                </extraRecordData>
                <Identifier>
                    <xsl:value-of select="$header/*/iisg:identifier"/>
                </Identifier>
            </xsl:if>
        </record>
    </xsl:template>

</xsl:stylesheet>