<?xml version='1.0'?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                xmlns:srw_dc="info:srw/schema/1/dc-v1.1" xmlns:dc="http://purl.org/dc/elements/1.1/">

    <!-- Dublin Core -->

    <xsl:template match="srw_dc:dc">
        <table>
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <xsl:template match="oai_dc:dc">
        <table>
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <xsl:template match="dc:identifier">
        <xsl:if test="not(name()=dc)">
            <tr>
                <td align="right" width="25%" valign="top">
                    <b>
                        <xsl:value-of select="name()"/>
                    </b>
                    :<xsl:text> </xsl:text>
                </td>
                <td>
                    <xsl:text> </xsl:text>
                    <xsl:choose>
                        <xsl:when test="substring(., 1, 7)='http://'">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="."/>
                                </xsl:attribute>
                                <xsl:value-of select="."/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dc:*">
        <xsl:if test="not(name()=dc)">
            <tr>
                <td align="right" width="25%" valign="top">
                    <b>
                        <xsl:value-of select="name()"/>
                    </b>
                    :<xsl:text> </xsl:text>
                </td>
                <td>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="."/>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
