#This is a OAI2 library plugin for Solr

##What is it ?
A plugin that exposes your Solr 4.x indexes with the OAI2 protocol.

##How it works
You can use simple xslt documents to map the raw Solr XML response into the oai_dc metadata format; and any other
metadata schema you may which to offer to your harvesting public. This way you can expose your index, regardless of your
solr schema. It can be used for single and multicore instances.

##Declare the request and response handlers
In the solrconfig.xml add a requestHandler and response writer:

    <requestHandler name="/oai" default="false" class="org.socialhistoryservices.solr.oai.OAIRequestHandler">
        <!-- WT is the key for the queryResponseWriter (see below) -->
        <str name="wt">oai</str>
        <str name="oai_home">/oai</str>
        <! -- more configuration in this readme below -->
    </requestHandler>

    <queryResponseWriter name="oai" default="false" class="org.socialhistoryservices.solr.oai.OAIQueryResponseWriter"/>

##The oai folder
For each metadata schema you want to support, add an associated xslt document to the "oai" folder. There are sample xslt
documents already in the folder of this distribution.

The oai folder can be placed in each core; or higher up in de solr_home directory. If your directory structure look like this:

    ---
    -solr
        -core0
            -conf
                schema.xml
                solrconfig.xml
        -core1
             -conf
                 schema.xml
                 solrconfig.xml
    -oai
        Identify.xml
        ListSets.xml
        ListMetadataFormats.xml
        oai.xsl
        oai_dc.xsl
        solr.xsl
    -lib
        oai2-plugin-4.1.jar

then the setting ought to be

    <str name="oai_home">/oai</str>

Alternatively, you can place a oai folder in each core as well. For example:

    <str name="oai_home">/core0/oai</str>

    ---
    -solr
        -core0
            -conf
                schema.xml
                solrconfig.xml
            +oai

###Non dynamic documents
The Identify, ListSets and ListMetadataPrefix verbs are
 xml documents you need to set manually in the oai folder:

###The Identify verb
Place a suitable Identify.xml document in the -oai folder. For example:

    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">

        <!-- You may leave out the responseDate and request elements -->

        <Identify>
            <repositoryName>Your repo name</repositoryName>
            <baseURL>http://localhost:8983/solr/example/oai?</baseURL>
            <protocolVersion>2.0</protocolVersion>
            <adminEmail>admin@domain.org</adminEmail>
            <earliestDatestamp>2011-06-01T12:00:00Z</earliestDatestamp>
            <deletedRecord>transient</deletedRecord>
            <granularity>YYYY-MM-DDThh:mm:ssZ</granularity>
            <compression>none</compression>
            <description>
                <oai-identifier
                        xmlns="http://www.openarchives.org/OAI/2.0/oai-identifier"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai-identifier http://www.openarchives.org/OAI/2.0/oai-identifier.xsd">
                    <scheme>oai</scheme>
                    <repositoryIdentifier>localhost</repositoryIdentifier>
                    <delimiter>:</delimiter>
                    <sampleIdentifier>oai:localhost:12345/12345-abcde</sampleIdentifier>
                </oai-identifier>
            </description>
        </Identify>
    </OAI-PMH>


###The ListMetadataFormats verb
The ListMetadataPrefix.xml document contains your metadata schema definitions. At startup the oai4Solr plugin will look
for a 'ListMetadataPrefix.xml' document in the oai folder and use for the ListMetadataFormats response.

The plugin will derive and load all declared xslt documents from it. For example, consider the fragment:


    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">

        <!-- You may leave out the responseDate and request elements -->

        <ListMetadataFormats>
            <metadataFormat>
                <metadataPrefix>oai_dc</metadataPrefix>
                <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>
                <metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>
            </metadataFormat>
            <metadataFormat>
                <metadataPrefix>marcxml</metadataPrefix>
                <schema>http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd</schema>
                <metadataNamespace>http://www.loc.gov/MARC21/slim</metadataNamespace>
            </metadataFormat>
            <metadataFormat>
                <metadataPrefix>solr</metadataPrefix>
                <schema>http://wiki.apache.org/solr/solr.xsd</schema>
                <metadataNamespace>http://wiki.apache.org/solr/</metadataNamespace>
            </metadataFormat>
        </ListMetadataFormats>
    </OAI-PMH>

The -metadataPrefix element value will be used to load an associate xslt document from the oai folder. Here:
oai_dc.xsl, marcxml.xsl and solr.xsl

Valid OAI2 metadataPrefix parameter values will then be oai_dc, marcxml and solr. The OAI2 response will be manufactured by
applying the corresponding (cached) xslt template.

###The ListSets verb
ListSets are not constructed dynamically from facets. Rather, like 'ListMetadataFormats' and 'Identify' they are
declared in the file ListSets.xml. For example like:

    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
        http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">

        <!-- You may leave out the responseDate and request elements -->

        <ListSets>
            <set>
                <setSpec>books</setSpec>
                <setName>Catalog</setName>
                <setDescription>All the books from the library</setDescription>
            </set>
            <set>
                <setSpec>video</setSpec>
                <setName>Video and audio</setName>
                <setDescription>All digitally available audio and video</setDescription>
            </set>
        </ListSets>
    </OAI-PMH>

You specify the solr index field for sets in the solrconfig.xml document with the "field_index_set" field. For example
if your Solr schema has an appropriate field for set queries such as 'catalog_source'

    <field name="catalog_source" type="string" indexed="true" stored="true" multiValued="true" />

Then indicate the value in your solrconfig.xml document so:

    <str name="field_index_set">catalog_source</str>

##OAI Identifier
An OAI identifier has the format 'oai:[domain]:[identifier]'. When this value is passed on with the GetRecord verb using the -identifier parameter,
the 'oai:[domain]:' bit is stripped of and the remaining (low local) identifier is used for the Lucene query.

Indicate the domain with the 'prefix' parameter. For example like so:

    <str name="prefix">oai:localhost:</str>

And the local identifier with the 'identifier' parameter. For example:

    <str name="field_index_identifier">my-unique-record-identifier</str>

So in this example an oai identifier like 'oai:localhost:12345' would translate in a Lucene query 'my-unique-record-identifier:12345'


##Datestamps
The -from and -until OAI2 parameters need to be mapped also in the solrconfig.xml document. Make sure the solr fields
that contain the indexed datestamps are of type 'date' to allow for sorting. For example:

    <fieldType name="date" class="solr.TrieDateField" sortMissingLast="true" omitNorms="true"/>
    <field name="my_datestamp" type="date" indexed="true" stored="true" required="true" default="NOW"/>

The plugin will need to know which index fields can be used for querying and sorting. Set it  so:

        <str name="field_sort_datestamp">my_datestamp</str>
        <str name="field_index_datestamp">my_datestamp</str>

##Resultset length
The default is 200 records per response before the resumptionToken kicks in. You can set a maximum record length per
 schema with the maxrecords parameter:

    <lst name="maxrecords">
        <int name="default">200</int>

        <!-- EAD documents are large, so we want to page per document -->
        <int name="ead">1</int>
    </lst>

##Solrconfig.xml configuration in full
Set the following in the solrconfig.xml document:

    <config>

    <!-- configuration -->

    <requestHandler name="/oai" default="false" class="org.socialhistoryservices.solr.oai.OAIRequestHandler">

        <!-- WT is the key for the queryResponseWriter (see below) -->
        <str name="wt">oai</str>

        <!-- the oai_home is a path relative to the solr.home directory.
        The default is [solr_home]/oai
        In this folder the xsl stylesheets, ListMetadataPrefix.xml, Identify.xml and ListSets.xsl must be placed.
        -->
        <str name="oai_home">/oai</str>

        <!-- the base url...  -->
        <str name="proxyurl">http://localhost:8080/oai</str>

        <!-- index name of the oai identifier used for the -identifier parameter
         For example, if your Solr identifier index is "identifier", set it
         to:
         -->
        <str name="field_index_identifier">identifier</str>

        <!-- The prefix will be stripped from the oai identifier value, before it is passed
        to the Solr Lucene query. For example, if your index contains a
         Solr identifiers such as the format:
         id12345
        then an OAI request like GetRecord&identifier=oai:mydomain:id12345
         is rewritten as id12345 and passed on to the Solr query.
        -->
        <str name="prefix">oai:socialhistoryservices:</str>

        <!-- The index name used for the -set parameter -->
        <str name="field_index_set">myIndexForSets</str>

        <!-- the index name that can be used for the -from and -until parameters -->
        <str name="field_index_datestamp">datestamp</str>

        <!-- the index name that can be used to sort datestamps. It is relevant
        to have a sortable field for your datestamp when having paged OAI2
        results. -->
        <str name="field_sort_datestamp">s_datestamp</str>

        <!-- index name for the -set value -->
        <str name="field_index_set">sets</str>

        <!-- Some documents may be large or small, depending on the schema.
        In that case, we set the step of the paging to a lower count. -->
        <lst name="maxrecords">
            <int name="default">200</int>
            <int name="ead">1</int>
            <int name="eci">1</int>
        </lst>

        <!-- The resumption token is build up from string parameters (from,until,set and metadataprefix). The separator
        character is a regular expression that is used to divide them up.
        You don't need to change this.
        -->
        <int name="resumptionTokenExpirationInSeconds">86400</int>
        <str name="resumptionTokenSeparator">,</str>
    </requestHandler>

    <!-- Custom wt that is being used by the OAI handler -->
    <queryResponseWriter name="oai" default="false" class="org.socialhistoryservices.solr.oai.OAIQueryResponseWriter"/>

    </config>

##Mapping tips
To map your Solr documents onto a metadata schema like OAI_DC, Marc, EAD, Mets, MODS, etc., make a corresponding XSLT
 document for each.

###1. From stored Solr index fields
Map stored Solr index fields from your schema.xml. Typically, those fields will show up in a /select XML Solr result set.

For example, if you want to create an oai_dc metadata response with a dc:title and the suitable Solr index field
was defined as 'main_title'

    <field name="main_title" type="string" indexed="true" stored="true" multiValued="false" />

then map it to a dc:title in the oai_dc.xsl document so:

    <dc:title>
        <xsl:value-of select="$doc//str[@name='main_title']"/>
    </dc:title>

See the oai_dc.xsl example in the demo/solr/oai folder of this project for a working example.

###2. Map a single stored XML document
In some cases, you may find it more convenient to - apart from the indexed fields - add the complete document of a
particular metadata schema as an unindexed and compressed resource field and just data dump it.

For example, if you have a MARCXML document:

    <marc:record xmlns:marc="http://www.loc.gov/MARC21/slim">
        <marc:leader>00620nam a22 7i</marc:leader>
        <!-- snip snip -->
        <marc:datafield ind1="1" ind2="0" tag="245">
            <marc:subfield code="a">Artis-gids :</marc:subfield>
            <marc:subfield code="b">125 jaar /</marc:subfield>
            <marc:subfield code="c">samenst. H. v.d. Werken.; tekeningen Ies Spreekmeester.</marc:subfield>
        </marc:datafield>
        <!-- snip snip -->
    </marc:record>

and add in to a Solr index field that is named let's say 'resource':

    <field name="resource" type="string" indexed="false" stored="true" required="true" compressed="true"/>

then the typical Solr XML response would indicate:

        <str name="resource">
            &lt;marc:record xmlns:marc="http://www.loc.gov/MARC21/slim"&gt;
                &lt;marc:leader&gt;00620nam a22 7i&lt;/marc:leader&gt;
                &lt;!-- snip snip --&gt;
                &lt;marc:datafield ind1="1" ind2="0" tag="245"&gt;
                    &lt;marc:subfield code="a"&gt;Artis-gids :&lt;/marc:subfield&gt;
                    &lt;marc:subfield code="b"&gt;125 jaar /&lt;/marc:subfield&gt;
                    &lt;marc:subfield code="c"&gt;samenst. H. v.d. Werken.; tekeningen Ies Spreekmeester.&lt;/marc:subfield&gt;
                &lt;/marc:datafield&gt;
                &lt;!-- snip snip --&gt;
            &lt;/marc:record&gt;
        </str>
        
In your marcxml.xsl you can map this so:

    <xsl:template name="metadata">
        <metadata>
            <xsl:variable name="record" select="saxon:parse($doc//str[@name='resource']/text())/node()"/>
            <xsl:copy-of select="$record"/>
        </metadata>
    </xsl:template>

Note: we used an xslt 2 method here.

See the marcxml.xsl example in the demo/solr/oai folder of this project for a working example.

### XSLT 2
Depending on your approach you may want to use xslt 2. If you do, add an xslt parser like Saxon in your web container's classpath. For example from:

http://repo1.maven.org/maven2/net/sf/saxon/saxon/8.7/saxon-8.7.jar

http://repo1.maven.org/maven2/net/sf/saxon/saxon-dom/8.7/saxon-dom-8.7.jar

Place the libraries in the class path, i.e.

../webapps/solr/WEB-INF/lib

or /tomcat6/lib

or the web-container-classpath-of-your-choice equivalent.

##To build from source
Clone from the git repository and use one the two maven commands:

    $ mvn clean package

The -Dsolr.solr.home VM property may need to be set manually if the unit tests cannot derive it's location. In that case add:

    $ mvn -Dsolr.solr.home=[absolute path to oai4solr/solr] clean package

The end result is a package in ./oai2-plugin/target/oai2-plugin-3.x-1.0.jar ( or your maven local repository if you used 'install').

##Download
You can also download the latest build from https://bamboo.socialhistoryservices.org/browse/OAI4SOLR-OAI4SOLR/latest from the artifacts tab.

##Install
Place oai2-plugin-3.x-1.0.jar in the designated "lib" folder of your Solr application. Or add a symbolic link in the "lib"
that points to the jar.

##Runable demo
Once the project is build, a demo is available. It contains an embedded Solr Jetty server. If you start it, it will load MarcXML test records.

Copy the oai2-plugin-3.x-1.0.jar into the demo/solr/lib folder. Or place a symbolic link to it. The
directory structure should look like this:

    ----
    -demo
        -solr
            -core0
                +conf
                +oai
            +docs
            -lib
                oai2-plugin-4.1.jar

Start the demo with:

    java -jar demo/target/demo-1.0.jar

Then explore the test OAI2 repository with your request to it, e.g.

    http://localhost:8983/solr/core0/oai?verb=Identify