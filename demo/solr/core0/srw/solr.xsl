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
                xmlns:solr="info:srw/cql-context-set/1/solr"
        >

    <xsl:template match="response">

        <!-- constant parameters can be set in solrconfig.xml
            <xsl:param name="p1" />
            <xsl:param name="p2" />
        -->

        <record><recordData>
            <solr:doc>
                <xsl:call-template name="copy-with-recursion">
                    <xsl:with-param name="xml" select="doc"/>
                </xsl:call-template>
            </solr:doc>

            <!-- Setting an identifier does not seem to work... -->
        </recordData></record>

    </xsl:template>

    <xsl:template name="copy-with-recursion">
        <xsl:param name="xml"/>

        <xsl:apply-templates select="$xml" mode="all"/>

    </xsl:template>

    <xsl:template match="@*|node()" mode="all">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="all"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>