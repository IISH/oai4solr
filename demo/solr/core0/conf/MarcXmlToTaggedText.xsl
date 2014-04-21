<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:marc="http://www.loc.gov/MARC21/slim"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>

    <xsl:template match="marc:record">
        <div class="recordWrapper">
            <div class="entry">
                <span class="field">LDR</span>
                <span class="data">
                    <xsl:value-of select="marc:leader"/>
                </span>
            </div>
            <xsl:apply-templates select="marc:datafield|marc:controlfield"/>
        </div>
    </xsl:template>

    <xsl:template match="marc:controlfield">
        <div class="entry">
            <span class="field">
                <xsl:value-of select="@tag"/>
            </span>
            <span class="data">
                <xsl:value-of select="."/>
            </span>
        </div>
    </xsl:template>

    <xsl:template match="marc:datafield">
        <div class="entry">
            <div class="fixed">
                <span class="field">
                    <xsl:value-of select="@tag"/>
                </span>
                <span class="indicator">
                    <xsl:choose>
                        <xsl:when test="@ind1=' '">
                            <xsl:text>_</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@ind1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
                <span class="indicator">
                    <xsl:choose>
                        <xsl:when test="@ind2=' '">
                            <xsl:text>_</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@ind1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </div>
            <div class="subfieldset">
                <xsl:apply-templates select="marc:subfield"/>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="marc:subfield">
        <span class="subfield">$<xsl:value-of select="@code"/>
        </span>
        <xsl:choose>
            <xsl:when test="@code='u'">
                <span class="data">
                    <a href="{.}">
                        <xsl:value-of select="."/>
                    </a>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <span class="data">
                    <xsl:value-of select="."/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

        <!-- Stylus Studio meta-information - (c)1998-2002 eXcelon Corp.
        <metaInformation>
        <scenarios ><scenario default="no" name="Ray Charles" userelativepaths="yes" externalpreview="no" url="..\xml\MARC21slim\raycharles.xml" htmlbaseurl="" outputurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/><scenario default="yes" name="s7" userelativepaths="yes" externalpreview="no" url="..\ifla\sally7.xml" htmlbaseurl="" outputurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
        </metaInformation>
        -->