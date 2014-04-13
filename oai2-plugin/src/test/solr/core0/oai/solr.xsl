<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
  ~
  ~     Copyright (c) 2011-2014  International Institute of Social History
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.openarchives.org/OAI/2.0/">

    <!--
    Demonstrate how a Solr response can be mapped into an raw format.

    This is not a proper OAI2 response, because the solr namespace is not added anywhere.
    -->

    <xsl:import href="oai.xsl"/>

    <xsl:template name="header">
        <header>
            <identifier>
                oai:localhost:<xsl:value-of select="$doc//str[@name='identifier']"/>
            </identifier>
            <datestamp>
                <xsl:value-of select="$doc//date[@name='datestamp']"/>
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
            <solr:doc xmlns:solr="http://wiki.apache.org/solr/">
                <xsl:for-each select="$doc/*">
                    <xsl:element name="solr:{local-name(.)}">
                        <xsl:choose>
                            <xsl:when test="local-name()='arr'">
                                <xsl:copy-of select="@*"/>
                                <xsl:for-each select="str">
                                    <solr:str>
                                        <xsl:value-of select="text()"/>
                                    </solr:str>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="text()|@*"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                </xsl:for-each>
            </solr:doc>
        </metadata>
    </xsl:template>

    <xsl:template name="about"/>

</xsl:stylesheet>