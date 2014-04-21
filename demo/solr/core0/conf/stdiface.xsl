<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srw="http://www.loc.gov/zing/srw/"
                xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/">

    <xsl:output method="html" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <xsl:template name="stdiface">
        <xsl:param name="title" />
        <xsl:param name="dbname" />
        <head>
            <title>
                <xsl:value-of select="$title"/>
            </title>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <!--link type="text/css" rel="stylesheet" href="http://shipengrover-j.oa.oclc.org/terminologies/temp/scifi-ff-footer_files/researchproject_oclc.css"/-->
            <style type="text/css">
/* OCLC Web Site Main Style Sheet */
/* basic_oclc.css */

/* basics */
a:link, a:visited { background-color: transparent; }
a:link { color: #C95000; }
a:hover { color: #02008D; }
a:visited { color: #C95000; }

a:visited:hover { color: #02008D; }

a:link img, a:hover img, a:visited img, a:visited:hover img { border: none; }

body {
	color: #000;
	font-family: Verdana, Geneva, "Arial Unicode MS", Arial, Helvetica, sans-serif;
}

p {
	line-height: 125%;
	margin: 0;
	padding: 6px 0 9px 0;
}

ul {
	margin: 0 0 0 16px;
	padding: 0 0 9px 0;
}

ol {
	margin: 0 0 0 24px;
	padding: 0 0 9px 0;
}

ul li { list-style-type: square; }
ul li li { list-style-type: circle; }

li {
	line-height: 130%;
	margin: 0;
	padding: 3px 0;
}

h1 {
	border-bottom: 1px solid #999;
	color: #666;
	font-weight: bold;
	line-height: 1.2em;
	margin: 0 0 8px 0;
	padding: 5px 0 8px 0;
}

h2 {
	clear: left;
	color: #222;
	font-weight: bold;
	margin: 0;
	padding: 16px 0 2px 0;
}

h3 {
	color: #666;
	font-weight: bold;
	margin: 0;
	padding: 10px 0 2px 0;
}

h4 {
	color: #666;
	font-style: italic;
	font-weight: bold;
	padding: 8px 0 2px 0;
}

.runinhead {
	color: #000;
	display: inline;
	font-weight: bold;
}

h5 {
	color: #777;
	font-family: Tahoma, Geneva, "Arial Unicode MS", Arial, Helvetica, sans-serif;
	font-weight: bold;
	margin: 0;
	padding: 10px 0 0 0;
}

.flush {
	padding-top: 0px;
	margin-top: 0px;
}

pre, code {
	color: #008000;
	font-family: Courier, "Courier New", "Andale Mono", Monoco, monospace;
	margin: 0;
	padding: 1.5em 0;
}

hr {
	color: #999;
	height: 1px;
	width: 100%;
}

form {
	margin: 0;
	padding: 0;
}

dt {
	font-weight: bold;
	padding-top: 10px;
}

acronym { border-bottom: 1px dotted #666; cursor: help }

.superscript {
	font-size: 70%;
	vertical-align: text-top;
}

.subscript {
	font-size: 70%;
	vertical-align: text-bottom;
}

/* colors */
.blue { background-color: #2700A5; }
.green { background-color: #62C448; }
.orange { background-color: #FF7600; }

/* hidden OCLC name for print */
p#oclcprint { display: none; }

table.layouttable  {
	border: none;
	margin: 0;
	width: 100%;
}

.layouttable th, .layouttable td {
	border: none;
	padding: 0;
	vertical-align: top;
}

table {
	border-right: 1px solid #999;
	border-bottom: 1px solid #999;
	margin: 12px 0;
}

th, td {
	border-top: 1px solid #999;
	border-left: 1px solid #999;
	color: #333;
	line-height: 140%;
	padding: 9px;
	text-align: left;
	vertical-align: top;
}

th { background-color: #FFD5B0; }
img { padding: 0; }
button { cursor: auto; }

input {
	color: #000;
	cursor: auto;
}

select {
	color: #000;
	font-family: Arial, Helvetica, Geneva, Verdana, sans-serif;
	font-size: x-small;
}

textarea {
	background-color: #FFF;
	color: #000;
	cursor: text;
}

fieldset {
	border: thin #CCC none;
	margin: 0;
}

legend {
	font-size: small;
	font-weight: bold;
	line-height: normal;
	margin: 0;
	text-indent: 0;
}

label { font-weight: normal; }

table.mclist  {
	background-color: transparent;
	border: none;
	margin: 0;
	padding: 9px 0;
}

.mclist th, .mclist td {
	background-color: transparent;
	border: none;
	padding: 0 12px 12px 0;
	vertical-align: top;
}

.tablerowcolor, .tablerowcolor td, .trcolor, .trcolor td { background-color: #FFEDDE; }
.tablerowgray, .tablerowgray td, .trgray, .trgray td { background-color: #EEE; }
.trmedgray, .trmedgray td { background-color: #DEDEDE; }
.trwhite, .trwhite td { background-color: #FFF; }

.imageright, div.imageright, table.imageright {
	color: #666;
	float: right;
	font-weight: bold;
	position: relative;
}

.imageright { padding: 0 0 2px 10px; }

div.imageright, table.imageright { padding: 12px 0 10px 10px; }

.imageleft, div.imageleft, table.imageleft {
	color: #666;
	float: left;
	font-weight: bold;
	position: relative;
}

.imageleft { padding: 0 10px 2px 0; }

div.imageleft, table.imageleft { padding: 12px 10px 0 0; }

.imagecenter, div.imagecenter, td.imagecenter {
	color: #666;
	font-weight: bold;
	position: relative;
	text-align: center;
}

.imagecenter { padding: 0 10px 2px 10px; }

div.imagecenter, table.imagecenter { padding: 12px 10px 0 10px; }

.imageleft p, .imagecenter p, .imageright p { padding: 0 0 3px 0; }

.w65 { width: 65px; }
.w75 { width: 75px; }
.w100 { width: 100px; }
.w125 { width: 125px; }
.w150 { width: 150px; }
.w175 { width: 175px; }
.w200 { width: 200px; }
.w225 { width: 225px; }
.w250 { width: 250px; }
.w275 { width: 275px; }
.w300 { width: 300px; }
.w325 { width: 325px; }
.w350 { width: 350px; }
.w375 { width: 375px; }
.w400 { width: 400px; }
.w425 { width: 425px; }
.w450 { width: 450px; }
.w475 { width: 475px; }
.w500 { width: 500px; }
.w525 { width: 525px; }
.w550 { width: 550px; }
.w575 { width: 575px; }
.w600 { width: 600px; }
.w100pct { width: 100%; }

/* ----------------------font sizes---------------------- */

/*   Todd Fahrner's keyword hack for IE           */
/*   http://alistapart.com/stories/sizematters/   */

body, body div, body p, body th, body td, body li, body dd    {
	/* more specific to override rule in importing sheet */
	font-size: x-small;
	/* false value for WinIE4/5 */
	voice-family: "\"}\"";
	/* trick WinIE4/5 into thinking the rule is over */
	voice-family: inherit;
	/* recover from trick */
	font-size: small;
	/* intended value for better browsers */
}

html>body, html>body div, html>body p, html>body th, html>body td, html>body li, html>body dd {
	font-size: small;
	/* be nice to Opera */
}

/* end Todd Fahrner's keyword hack for IE */

h1 { font-size: 135%; }
h2 { font-size: 120%; }
h3 { font-size: 105%; }
h4 { font-size: 100%; }
h5 { font-size: 80%; }

.medium, .medium p, .medium li, .medium th, .medium td { font-size: 85%; }

.imageright, .imageleft, .imagecenter, .small, .small p, .small li, .small th, .small td, .datatable p, .datatable th, .datatable td, .datatable li {
	font-size: xx-small;
	voice-family: "\"}\"";
	voice-family: inherit;
	font-size: x-small;
}

pre, code, .normal {
	font-size: x-small;
	voice-family: "\"}\"";
	voice-family: inherit;
	font-size: small;
}

/* END BASICS */

/* CSS Document */

/* CSS used by OCLC Research projects outside of the CMS */

body {padding:0px;margin:0px;background:#eee;}
#content {padding:10px;clear:both;background:#fff;padding-bottom:40px;min-height:700px;}

div#masthead {background:#666;height:82px;padding:0px 0px 0px 10px;}
div#masthead a:link {color: #CCC; text-decoration: none;}
div#masthead a:visited {color: #CCC; text-decoration: none;}
div#masthead a:hover {color: #FFF; text-decoration: underline;}
div#masthead a:visited:hover {color: #FFF; text-decoration: underline;}
div#or {padding:3px 0px 3px 0px;font-size:small;}
div#project {color:#fff;font-weight:bold;font-size:large;margin:12px 0px 12px 0px;}

div#logo {position:absolute;top:0px;right:0px;}

.formtable { background: #F5F5F5; }
.noborder { border: none; }
.noborder td { border: none; padding: 6px; }
.button {
	font-family: Verdana, Geneva, "Arial Unicode MS", Arial, Helvetica, sans-serif;
	font-weight: bold;
	color: #FFF;
	background: #E66A00;
	margin: 2px 0;
	padding: 0;
	cursor: pointer;
}


#nav {padding:4px;clear:both;font-size:x-small;color:#666;background:#eee;}
#nav span.label {padding-right:3px;font-weight:bold;}
#nav a {padding:10px;}
#nav a:link {color:#666;text-decoration:none;}
#nav a:hover {color:#666;text-decoration:underline;}
#nav a:visited{color:#666;text-decoration:none;}
#nav a:visited:hover {color:#666;text-decoration:underline;}
span.bullet {color:#666;padding:0px 10px 0px 10px;}


/*footer */
#footer {clear:both;border-top:1px dashed #666;background:#eee;padding:10px;}

#footer a:link, #footer:visited {text-decoration:none;color:#551a8b;}
#footer a:hover, #footer a:visited:hover {text-decoration:underline;color:#551a8b;}
#logo-footer {padding:6px;margin-right:5px;float:left;}

#legal {margin:6px 10px 0px 0px;}

#legal div {margin:0px 0px 4px 0px;}
#copyright {}
#TandC {padding-left:0px;clear:both;}

#legal-small {margin:6px 10px 0px 0px;}
#TandC-single {padding-left:100px;}

#badges {padding:2px 0px 2px 0px;}
#badges img {padding:0px 8px 0px 0px;}


/* MARC */

	  .recordDesc {margin:0px 0px 5px 0px;}
	  .recordCount {float:left;padding:0px;margin:0px;}
	  .recordSchema {margin:4px 0px 4px 140px;padding:0px}

	  #recordDisplayDetails {margin:0px 0px 10px 0px;}
	  #recordDisplayDetails p {margin:4px 0px 4px 0px;padding:0px}
	  #recordDisplayDetails a {margin:5px 0px 25px 0px;line-height:2;}





#recordsWrapper {clear:both;}
.recordWrapper {margin:0px 3px 15px 0px;padding:10px;border:1px solid #ccc;}
.entry {margin:3px 0px 5px 0px;clear:both;}
.field {font-weight:bold;margin-right:3px;}
.indicator {margin:0px 2px 0px 2px;}
.subfield {font-weight:bold;}
.data {margin-right:10px;}

.fixed {float:left;width:72px;}
.subfieldset {margin:0px 0px 0px 70px;}

#bottomLinks {clear:both;margin:10px 0px 0px 0px;}

/* *************** */

#content table.layout {border:none;width:100%;}</style>
            <style type="text/css">
                &lt;!--
                table.layout { border: none; margin: 0; padding: 0; width: 100%; }
                table.layout td { border: none; margin: 0; padding: 0; }
                table.formtable th, table.formtable td { border-top: 1px solid #999; border-left: 1px solid #999; color:
                #333; padding: 4px; text-align: left; vertical-align: top}
                table.formtable td { width: 100%}
                input.button { margin: 0; }

                #masthead
                {
                height:100px;
                }

                .sru
                {
                    display:none;
                }

                --&gt;
            </style>
            <script type="text/javascript">
                <!--
                function setQuery(displayLabel, index, relation, value, recordSchema)
                {
                    var query = &quot;?operation=searchRetrieve&amp;query=&quot;.concat(index, ' ', relation, ' ', '\&quot;', value, '\&quot;', &quot;&amp;recordSchema=&quot;, recordSchema);
                    alert(query);
                    return query ;
                }

                function runQuery(query)
                {
                    document.location=query;
                }
                -->
                function setSearchItem(arr)
                {
                    var query = &quot;?operation=searchRetrieve&amp;query=&quot;.concat(arr.index, ' ', arr.relation, ' ', '\&quot;', arr.actualTerm, '\&quot;', &quot;&amp;recordSchema=&quot;, arr.recordSchema,'&amp;maximumRecords=', arr.maximumRecords );
                    document.location=query;
                }
            </script>
        </head>
        <body>
            <div id="masthead">

                <div id="or">
                    <a href="http://api.socialhistoryservices.org/">IISH SRU\SRW api</a>
                </div>
                <div id="project">
                    <xsl:value-of select="$dbname"/>
                </div>
                <div id="logo">
                    <a href="http://api.socialhistoryservices.org/">
                        <img src="http://api.socialhistoryservices.org/images/banner-home.png" alt="IISH logo"
                             title="International Institute of Social History"/>
                    </a>
                </div>

            </div>
            <!-- close masthead -->
            <xsl:apply-templates/>
            <div id="footer">
                <div id="legal">
                    <div id="copyright">&#169; 2010 IISH</div>
                    <div id="TandC">Many thanks to the OCLC, who developed the SRW interface:
                        <a href="http://www.oclc.org/research/researchworks/terms.htm">OCLC ResearchWorks Terms and
                            Conditions
                        </a>
                    </div>
                    <!-- add or hide badges as required -->
                    <div id="badges">
                        <!-- sru/w -->
                        <a href="http://www.oclc.org/research/software/srw">
                            <img src="http://www.oclc.org/research/images/badges/oclc_srwu.gif"/>
                        </a>
                        <!-- errol -->
                        <!--img src="http://www.oclc.org/research/images/badges/oclc_errol.gif"-->
                        <!-- Gwen -->
                        <img src="http://www.oclc.org/research/images/badges/oclc_gwen.gif"/>
                        <!-- oaicat -->
                        <!--img src="http://www.oclc.org/research/images/badges/oclc_oaicat.gif"-->
                        <!-- pears -->
                        <img src="http://www.oclc.org/research/images/badges/oclc_pears.gif"/>
                        <!-- xsltproc -->
                        <!--img src="http://www.oclc.org/research/images/badges/oclc_xsltproc.gif"-->


                    </div>
                </div>
            </div>
        </body>

    </xsl:template>

    <xsl:template match="srw:version">
    </xsl:template>

    <xsl:template match="srw:diagnostics">
        <tr>
            <td>
                <h2>Diagnostics</h2>
            </td>
        </tr>
        <tr>
            <td width="50%" style="padding-right: 10px;">
                <xsl:apply-templates/>
            </td>
            <td></td>
        </tr>
    </xsl:template>

    <xsl:template match="diag:diagnostic">
        <table cellspacing="0" class="formtable">
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <xsl:template match="diag:uri">
        <tr>
            <th>Identifier:</th>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
        <tr>
            <th>Meaning:</th>
            <xsl:variable name="diag" select="."/>
            <td>
                <xsl:choose>
                    <xsl:when test="$diag='info:srw/diagnostic/1/1'">
                        <xsl:text>General System Error</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/4'">
                        <xsl:text>Unsupported Operation</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/6'">
                        <xsl:text>Unsupported Parameter Value</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/7'">
                        <xsl:text>Mandatory Parameter Not Supplied</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/10'">
                        <xsl:text>Query Syntax Error</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/16'">
                        <xsl:text>Unsupported Index</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/22'">
                        <xsl:text>Unsupported Combination of Relation and Index</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/39'">
                        <xsl:text>Proximity Not Supported</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/51'">
                        <xsl:text>Result Set Does Not Exist</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/61'">
                        <xsl:text>First Record Position Out Of Range</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/64'">
                        <xsl:text>Record temporarily unavailable</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/66'">
                        <xsl:text>Unknown Schema For Retrieval</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/71'">
                        <xsl:text>Unsupported record packing</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/93'">
                        <xsl:text>Sort Ended Due To Missing Value</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/94'">
                        <xsl:text>When resultSetTTL=0, Sort Only Legal When startRec=1</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/110'">
                        <xsl:text>Stylesheets Not Supported</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/120'">
                        <xsl:text>Response Position Out Of Range</xsl:text>
                    </xsl:when>
                    <xsl:when test="$diag='info:srw/diagnostic/1/130'">
                        <xsl:text>Too Many Terms Matched By Masked Query Term</xsl:text>
                    </xsl:when>
                </xsl:choose>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="diag:details">
        <tr>
            <th>Details:</th>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="diag:message">
        <tr>
            <td>
                <b>Message:</b>
            </td>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
