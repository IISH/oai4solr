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

    <xsl:import href="oai.xsl"/>

    <xsl:template name="header">
        <header>
            <identifier>
                <xsl:value-of select="$doc//str[@name='iisg_oai']"/>
            </identifier>
            <datestamp>
                <xsl:value-of select="$doc//date[@name='iisg_datestamp']"/>
            </datestamp>
            <xsl:for-each select="$doc//arr[@name='iisg_collectionName']/str">
                <setSpec>
                    <xsl:value-of select="."/>
                </setSpec>
            </xsl:for-each>
        </header>
    </xsl:template>

    <xsl:template name="metadata">
        <metadata>
            <xsl:copy-of select="$doc"/>
        </metadata>
    </xsl:template>

    <xsl:template name="about"/>

</xsl:stylesheet>