<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:marc="http://www.loc.gov/MARC21/slim">

    <xsl:output omit-xml-declaration="yes" media-type="text"/>

    <xsl:template match="marc:record">

        <xsl:for-each select="marc:controlfield">
            <xsl:value-of select="concat('marc_controlfield', ':', text())"/>
            <xsl:text>_Separator_ </xsl:text>
            <xsl:value-of select="concat('marc_controlfield_', @tag, ':', text())"/>
            <xsl:text>_Separator_ </xsl:text>
        </xsl:for-each>

        <xsl:for-each select="marc:datafield">
            <xsl:variable name="tag" select="@tag"/>
            <xsl:for-each select="marc:subfield">
                <xsl:value-of select="concat('marc_', $tag, ':', text())"/>
                <xsl:text>_Separator_ </xsl:text>
                <xsl:value-of select="concat('marc_', $tag, '_', @code, ':', text())"/>
                <xsl:text>_Separator_ </xsl:text>
            </xsl:for-each>
        </xsl:for-each>

    </xsl:template>

</xsl:stylesheet>