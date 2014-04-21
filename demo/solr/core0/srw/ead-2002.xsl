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
                version="2.0"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:ead="urn:isbn:1-931666-22-9"
                xmlns:iisg="http://www.iisg.nl/api/sru/"
                xmlns:zr="http://explain.z3950.org/dtd/2.0/"
                exclude-result-prefixes="ead iisg zr saxon"
        >



    <xsl:template match="response">

        <!-- Initialize the Solr document variables -->
        <xsl:variable name="record" select="saxon:parse(//doc/str[@name='resource']/text())/node()"/>
        <xsl:variable name="transport" select="str[@name='transport']/text()"/>
        <xsl:variable name="header" select="$record/extraRecordData"/>
        <xsl:variable name="metadata" select="$record/recordData"/>

        <!-- This is how the record inside the recordData will look like. -->
        <record><recordData>
            <!-- If the recordSchema ( expressed in recordSchema_Prefix ) is equal to the native record, we can dump it from the resource field. -->
            <xsl:choose>
                <!-- RecordSchema was set to show dc. Therefore we can data dump records,
      provided that the metadata has implemented the same metadata schema. -->
                <xsl:when test="$header/recordSchema_namespace[.='urn:isbn:1-931666-22-9']">
                    <xsl:copy-of select="$metadata"/>
                </xsl:when>
                <!-- Otherwise assemble the metadata from the solr stored fields -->
                <xsl:otherwise>
                    <ead:ead xmlns:ead="urn:isbn:1-931666-22-9">
                    </ead:ead>
                </xsl:otherwise>
            </xsl:choose>
        </recordData>
            <extraRecordData><xsl:copy-of select="$header"/></extraRecordData>

            <!-- Setting an identifier does not seem to work... -->
            <Identifier><xsl:value-of select="$header/iisg:identifier"/></Identifier>

            </record>

    </xsl:template>

</xsl:stylesheet>