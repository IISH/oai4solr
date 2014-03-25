#This is a OAI2 library plugin for Solr

##What is it ?
A plugin that exposes your Solr 3.x indexes with the OAI2 protocol.

##How it works
You can use simple xslt and a few mappings to expose your index, regardless of your
solr schema. It can be used for single and multicore instances.

##The metadataSchema
The ListMetadataPrefix.xml contains your schema definitions. By
convention, at startup the oai4Solr plugin  will look for a corresponding
 XSLT document in the solr/oai folder and load it.

##ListSets
ListSets are not constructed dynamically from facets. Rather, they are
declared in the file ListSets.xml. For example like:

<ListSets>
        <set>
            <setSpec>iisg_marcxml</setSpec>
            <setName>Catalog</setName>
        </set>
<ListSets>

You specify the solr index field for sets in the solrconfig.xml document with the "field_index_set" field.

##Datestamps
The -from and -until OAI2 parameters need to be mapped also in the solrconfig.xml document. Make sure the solr fields that contain the indexed datestamps are of type 'date'. For example:

<fieldType name="date" class="solr.DateField" sortMissingLast="true" omitNorms="true"/>
<field name="iisg_datestamp" type="date" indexed="true" stored="true" required="true" default="NOW"/>

##Mapping your schema
To map your Solr
 documents onto a metadata schema like OAI_DC, Marc, EAD, Mets, MODS, etc,
 you need to make a corresponding XSLT document.

In some cases, you can add the schema as a document in a resource field...
and just datadump it ( see the marc.xsl as an example ). In other you need
 to map individual stored Solr fields to the schema you want to expose ( see
 oai_dc.xml where such a mapping takes place).

## XSLT 2
Depending on your approach you may want to use xslt 2. If you do, add an xslt parser like Saxon in your web container's classpath. For example from:

http://repo1.maven.org/maven2/net/sf/saxon/saxon/8.7/saxon-8.7.jar
http://repo1.maven.org/maven2/net/sf/saxon/saxon-dom/8.7/saxon-dom-8.7.jar

Place the libraries in the class path:

I.e.

.../webapps/solr/WEB-INF/lib

or /tomcat6/lib

##Configuration
In the solrconfig.xml add a requestHandler. In that section you map
OAI parameters to your schema's identifier and datestamps index fields.
Further more you can add the metadataPrefici you support and set paging.

##The oai folder
For each metadataPrefix you need an associated xslt document that must be
placed in a "oai" folder. There are sample xslt documents already
in the folder of this distribution.

##Non dynamic documents
In this release the Identify, ListSets and ListMetadataPrefix verbs are
 xml documents you need to set manually.

##Solrconfig.xml configuration
Set the following in the solrconfig.xml document:

<config>

    ....

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

        <!-- index of the oai identifier used for the -identifier parameter
         For example, if your Solr identifier index is "identifier", you set it
         to... ques what...
         -->
        <str name="field_index_identifier">identifier</str>

        <!-- The index used for the -set parameter -->
        <str name="field_index_set">myIndexForSets</str>

        <!-- The prefix will be stripped from the oai identifier value, before it is passed
        to the Solr Lucene query. For example, if your index contains a
         Solr identifiers such as the format:
         id12345
        then an OAI request like GetRecord&identifier=oai:mydomain:id12345
         is rewritten as id12345 and passed on to the Solr query.
        -->
        <str name="prefix">oai:socialhistoryservices:</str>

        <!-- the field that can be used for the -from and -until parameters -->
        <str name="field_index_datestamp">datestamp</str>

        <!-- the field that can be used to sort datestamps. It is relevant
        to have a sortable field for your datestamp when having paged OAI2
        results. -->
        <str name="field_sort_datestamp">s_datestamp</str>

        <!-- index for the -set value -->
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

##Download
You can download the latest build from https://bamboo.socialhistoryservices.org/browse/OAI4SOLR-OAI4SOLR/

##To build from source
Download from the repository and use the maven command:

<code>$ mvn clean package<code>

##Install
To install place the oai4solr.jar in the designated "lib" folder of your Solr application.
