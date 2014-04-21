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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:solr="info:srw/cql-context-set/2/solr">

    <xsl:template match="solr:doc">
        <table>
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <xsl:template match="arr">
        <tr>
            <td align="right" width="25%" valign="top">
                <b>
                    <xsl:value-of select="@name"/>
                </b>
                :<xsl:text> </xsl:text>
            </td>
            <td>
                <xsl:if test="str">
                    <ul>
                        <xsl:for-each select="str">
                            <li>
                                <xsl:value-of select="."/>
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
                <xsl:if test="int">
                    <ul>
                        <xsl:for-each select="int">
                            <li>
                                <xsl:value-of select="."/>
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
                <xsl:if test="date">
                    <ul>
                        <xsl:for-each select="date">
                            <li>
                                <xsl:value-of select="."/>
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="str|int|date">
        <tr>
            <td align="right" width="25%" valign="top">
                <b>
                    <xsl:value-of select="@name"/>
                </b>
                :<xsl:text> </xsl:text>
            </td>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>