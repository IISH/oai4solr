<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ OAI4Solr exposes your Solr indexes using an OAI2 protocol handler.
  ~
  ~     Copyright (c) 2011-2017  International Institute of Social History
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns:saxon="http://saxon.sf.net/"
                exclude-result-prefixes="saxon">

    <!-- Demonstrate how a Solr response can be mapped into a  marcxml format.

   The header values are taken from stored Lucene index fields as set in the schema.xml document:

       header/identifier:
       <field name="identifier" type="string" indexed="true" stored="true" required="true"/>

       header/datestamp
       <field name="datestamp" type="date" indexed="true" stored="true" required="true" default="NOW"/>

       header/setSpec
       <field name="theme" type="string" indexed="true" stored="true" required="true" multiValued="true" />

   The metadata element is created by taking a data dump from one particular index field called 'resource'. It is
   assumed that resource contains a valid marcxml document with a marcxml namespace. -->

    <xsl:import href="oai.xsl"/>

    <xsl:template name="header">
        <header>
            <identifier>
                oai:localhost:<xsl:value-of select="$doc//str[@name='identifier']"/>
            </identifier>
            <datestamp>
                <xsl:call-template name="datestamp">
                    <xsl:with-param name="solrdate" select="$doc//date[@name='datestamp']"/>
                </xsl:call-template>
            </datestamp>
            <xsl:for-each select="$doc//arr[@name='theme']/str">
                <setSpec>
                    <xsl:value-of select="."/>
                </setSpec>
            </xsl:for-each>
        </header>
    </xsl:template>

    <xsl:template name="metadata">
        <metadata>
            <xsl:copy-of select="saxon:parse($doc//str[@name='resource']/text())/node()"/>
        </metadata>
    </xsl:template>

    <xsl:template name="about"/>

</xsl:stylesheet>