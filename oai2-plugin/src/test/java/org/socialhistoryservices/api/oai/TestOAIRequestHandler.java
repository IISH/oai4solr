package org.socialhistoryservices.api.oai;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.openarchives.oai2.*;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * TestOAIRequestHandler
 * <p/>
 * Test the OAI2 request and responses
 */
public class TestOAIRequestHandler extends TestCase {

    private final Log log = LogFactory.getLog(this.getClass());

    static String CORE = "core0";
    private static EmbeddedServer server = null;

    private static int testRecordCount = 1000;
    private static String datestamp_from = "1980-01-01T00:00:00Z";
    private static String datestamp_until = "1989-12-31T23:59:59Z";
    private static long marker_from;
    private static long marker_until;
    private static int expectedFromUntilRange;
    private static String marked_setSpec = "setSpec1";
    private static int expectedSetSpec;


    /**
     * Start the embedded server and add [testRecordCount] Lucene documents.
     * The setSpec will alternative between setSpec1, setSpec2 and setSpec3
     * And the datestamp will be between day 1 (1970 ) and the next 1000 months or so.
     */
    @Override
    protected void setUp() throws Exception {

        if (server == null) {
            super.setUp();
            String solr_home = System.getProperty("solr.solr.home");
            if (solr_home == null)
                solr_home = deriveSolrHome();

            FileUtils.deleteDirectory(new File(solr_home + "/" + CORE + "/data/index/"));
            FileUtils.deleteDirectory(new File(solr_home + "/" + CORE + "/data/tlog/"));

            Parameters.clearParams();
            final CoreContainer coreContainer = new CoreContainer(solr_home);
            coreContainer.load();
            server = new EmbeddedServer(coreContainer, CORE);
            marker_from = Parsing.parseDatestamp(datestamp_from).getTime();
            marker_until = Parsing.parseDatestamp(datestamp_until).getTime();

            for (int i = 0; i < testRecordCount; i++) {
                SolrInputDocument document = new SolrInputDocument();
                document.addField("identifier", i);
                String datestamp = String.valueOf(1000 + i) + "-06-21T12:00:00Z";
                Date date = Parsing.parseDatestamp(datestamp);
                document.addField("datestamp", date);
                document.addField("resource", "A title " + i);
                String theme = "setSpec" + i % 3;
                document.addField("theme", theme);
                server.add(document);

                if (date.getTime() >= marker_from && date.getTime() <= marker_until)
                    expectedFromUntilRange++;

                if (theme.equals(marked_setSpec))
                    expectedSetSpec++;

            }
            server.commit();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Parameters.setParam("enable_filter_query", false);
        Parameters.setParam("static_query", "");
    }

    private String deriveSolrHome() {

        File file = new File(System.getProperty("user.dir"), "/oai2-plugin/src/test/solr");
        if (!file.exists()) {
            file = new File(System.getProperty("user.dir"), "/src/test/solr");
            if (!file.exists()) {
                log.fatal("PWD=" + System.getProperty("user.dir") + "\nCannot find the Solr directory. Please set the VM property with -Dsolr.solr.home=[path?]/oai4solr/oai2-plugin/src/test/solr");
                System.exit(-1);
            }
        }

        System.setProperty("solr.solr.home", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    /**
     * testVerbIdentify
     * <p/>
     * See if we receive an Identify response and if it holds against the Identify.xml document.
     */
    public void testVerbIdentify() throws SolrServerException, IOException, XPathExpressionException, JAXBException {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "Identify");
        OAIPMHtype oai2Document = server.sendRequest(params);

        OAIPMHtype oaipmHtype = Parsing.loadStaticVerb(VerbType.IDENTIFY);
        IdentifyType identify = oai2Document.getIdentify();
        assertNotNull(identify);

        DescriptionType description = identify.getDescription().get(0);
        String sampleIdentifierFromResponse = server.GetNode((Node) description.getAny(), "//oai-identifier:sampleIdentifier");
        String sampleIdentifierFromFile = server.GetNode((Node) oaipmHtype.getIdentify().getDescription().get(0).getAny(), "//oai-identifier:sampleIdentifier");
        assertEquals(sampleIdentifierFromResponse, sampleIdentifierFromFile);
    }

    /**
     * testListSets
     * <p/>
     * See if we receive a ListSet response.
     */
    public void testListSets() throws SolrServerException, IOException, JAXBException {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListSets");
        OAIPMHtype oai2Document = server.sendRequest(params);
        ListSetsType listSets = oai2Document.getListSets();

        OAIPMHtype oaipmHtype = Parsing.loadStaticVerb(VerbType.LIST_SETS);

        for (SetType setTypeFromRequest : listSets.getSet()) {
            boolean match = false;
            for (SetType setTypeFromFile : oaipmHtype.getListSets().getSet()) {
                match = setTypeFromFile.getSetName().equals(setTypeFromRequest.getSetName()) &&
                        setTypeFromFile.getSetSpec().equals(setTypeFromRequest.getSetSpec());
                if (match)
                    break;
            }
            assertTrue(match);
        }
    }

    public void testSetSpecCount() {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("set", marked_setSpec);
        params.set("metadataPrefix", "oai_dc");

        int count = 0;
        ResumptionTokenType resumptionToken = null;
        do {
            if (resumptionToken != null)
                params.set("resumptionToken", resumptionToken.getValue());

            final OAIPMHtype oai2Document = server.sendRequest(params);
            final ListIdentifiersType listIdentifiers = oai2Document.getListIdentifiers();
            for (HeaderType header : listIdentifiers.getHeader()) {
                count++;
                assertTrue(header.getSetSpec().contains(marked_setSpec));
            }
            resumptionToken = oai2Document.getListIdentifiers().getResumptionToken();
        } while (resumptionToken != null);

        assertEquals(expectedSetSpec, count);
    }


    public void testListSetsNoSetSpecNoSetHierarchy() throws FileNotFoundException, JAXBException {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListRecords");
        params.set("metadataPrefix", "oai_dc");
        params.set("set", "some_setSpec");
        OAIPMHtype oai2Document = server.sendRequest(params);
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai2Document.getError().get(0).getCode());

        Parameters.setParam(VerbType.LIST_SETS, null);
        oai2Document = server.sendRequest(params);
        Parameters.setParam(VerbType.LIST_SETS, Parsing.loadStaticVerb(VerbType.LIST_SETS));
        assertEquals(OAIPMHerrorcodeType.NO_SET_HIERARCHY, oai2Document.getError().get(0).getCode());
    }

    /**
     * testListMetadataFormats
     * <p/>
     * See if we receive a ListMetadataFormats response.
     */
    public void testListMetadataFormats() throws FileNotFoundException, JAXBException {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListMetadataFormats");
        OAIPMHtype oai2Document = server.sendRequest(params);
        ListMetadataFormatsType listMetadataFormats = oai2Document.getListMetadataFormats();
        assertNotNull(listMetadataFormats);

        OAIPMHtype oaipmHtype = Parsing.loadStaticVerb(VerbType.LIST_METADATA_FORMATS);

        for (MetadataFormatType metadataFormatTypeFromRequest : listMetadataFormats.getMetadataFormat()) {
            boolean match = false;
            for (MetadataFormatType metadataFormatTypeFromFile : oaipmHtype.getListMetadataFormats().getMetadataFormat()) {
                match = metadataFormatTypeFromRequest.getSchema().equals(metadataFormatTypeFromFile.getSchema()) ||
                        metadataFormatTypeFromRequest.getMetadataNamespace().equals(metadataFormatTypeFromFile.getMetadataNamespace()) ||
                        metadataFormatTypeFromRequest.getMetadataPrefix().equals(metadataFormatTypeFromFile.getMetadataPrefix());
                if (match)
                    break;
            }
            assertTrue(match);
        }
    }

    public void testNoSuchMetadataFormat() {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListRecords");
        params.set("metadataPrefix", "unregistered_prefix");
        OAIPMHtype oai2Document = server.sendRequest(params);
        assertEquals(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, oai2Document.getError().get(0).getCode());
    }

    public void testResumptionToken() throws UnsupportedEncodingException {

        ResumptionToken token = new ResumptionToken();
        token.setVerb(VerbType.LIST_IDENTIFIERS);
        token.setMetadataPrefix("oai_dc");
        token.setFrom("2001-01-01T00:00:00Z");
        ResumptionTokenType resumptionTokenType = ResumptionToken.encodeResumptionToken(token, 0, 200, 1000, (Integer) Parameters.getParam("resumptionTokenExpirationInSeconds"));

        String bad_token = resumptionTokenType.getValue().concat("12345");
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListRecords");
        params.set("resumptionToken", resumptionTokenType.getValue());
        OAIPMHtype oai2Document = server.sendRequest(params);
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai2Document.getError().get(0).getCode());

        params.set("resumptionToken", bad_token);
        oai2Document = server.sendRequest(params);
        assertEquals(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, oai2Document.getError().get(0).getCode());

    }

    /**
     * testHarvestFlow
     * <p/>
     * Test a harvesting cycle with resumption tokens.
     * Check a mapping while we are there.
     */
    public void testHarvest() throws IOException, SolrServerException {

        // Start a harvest
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("metadataPrefix", "oai_dc");

        int count = 0;
        ResumptionTokenType resumptionToken = null;
        do {
            if (resumptionToken != null)
                params.set("resumptionToken", resumptionToken.getValue());

            final OAIPMHtype oai2Document = server.sendRequest(params);
            final ListIdentifiersType listIdentifiers = oai2Document.getListIdentifiers();
            for (HeaderType header : listIdentifiers.getHeader()) {
                int i = count++;
                String expected = Parameters.getParam("prefix") + String.valueOf(i);
                String actual = header.getIdentifier();
                assertEquals(expected, actual);

                getRecord(expected, "A title " + i);
            }
            resumptionToken = oai2Document.getListIdentifiers().getResumptionToken();
        } while (resumptionToken != null);

        assertEquals(testRecordCount, count);

    }

    private void getRecord(String identifier, String expected_title) {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "GetRecord");
        params.set("metadataPrefix", "oai_dc");
        params.set("identifier", identifier);
        final OAIPMHtype oai2Document = server.sendRequest(params);
        assertEquals(identifier,
                oai2Document.getGetRecord().getRecord().getHeader().getIdentifier());
        String actual_title = server.GetNode((Node) oai2Document.getGetRecord().getRecord().getMetadata().getAny(), "//dc:title");
        assertEquals(expected_title, actual_title);
    }

    /**
     * testFromUntil
     * <p/>
     * See if the from and until parameters give the expected recordCount and check if the header/datestamp is as it
     * should be.
     */
    public void testFromUntil() throws java.text.ParseException {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("from", datestamp_from);
        params.set("until", datestamp_until);
        params.set("metadataPrefix", "oai_dc");

        int count = 0;
        ResumptionTokenType resumptionToken = null;
        do {
            if (resumptionToken != null)
                params.set("resumptionToken", resumptionToken.getValue());

            final OAIPMHtype oai2Document = server.sendRequest(params);
            final ListIdentifiersType listIdentifiers = oai2Document.getListIdentifiers();
            for (HeaderType header : listIdentifiers.getHeader()) {
                count++;
                long datestamp = Parsing.parseDatestamp(header.getDatestamp()).getTime();
                assertTrue("datestamp < from : " + header.getDatestamp() + " < " + datestamp_from, datestamp >= marker_from);
                assertTrue("datestamp > until : " + header.getDatestamp() + " > " + datestamp_until, datestamp <= marker_until);
            }
            resumptionToken = oai2Document.getListIdentifiers().getResumptionToken();
        } while (resumptionToken != null);

        assertEquals(expectedFromUntilRange, count);
    }

    public void testFilterQueryOff() {
        // Start a harvest
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("metadataPrefix", "oai_dc");
        params.set("fq", "theme:i_do_not_exist");

        int count = 0;
        ResumptionTokenType resumptionToken = null;
        do {
            if (resumptionToken != null)
                params.set("resumptionToken", resumptionToken.getValue());

            final OAIPMHtype oai2Document = server.sendRequest(params);
            final ListIdentifiersType listIdentifiers = oai2Document.getListIdentifiers();
            for (HeaderType header : listIdentifiers.getHeader()) {
                int i = count++;
                String expected = Parameters.getParam("prefix") + String.valueOf(i);
                String actual = header.getIdentifier();
                assertEquals(expected, actual);
            }
            resumptionToken = oai2Document.getListIdentifiers().getResumptionToken();
        } while (resumptionToken != null);
        assertEquals(testRecordCount, count);
    }

    public void testFilterQueryOn() {
        // Start a harvest
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("metadataPrefix", "oai_dc");
        params.set("fq", "theme:setSpec1");
        Parameters.setParam("enable_filter_query", true);

        int count = getCount(params);
        assertEquals(expectedSetSpec, count);
    }

    public void testStaticQuery() {
        // Start a harvest
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("metadataPrefix", "oai_dc");
        params.set("fq", "theme:i_do_not_exist");
        Parameters.setParam("static_query", "theme:setSpec1");

        int count = getCount(params);
        assertEquals(expectedSetSpec, count);
    }

    private int getCount(ModifiableSolrParams params) {
        int count = 0;
        ResumptionTokenType resumptionToken = null;
        do {
            if (resumptionToken != null)
                params.set("resumptionToken", resumptionToken.getValue());

            final OAIPMHtype oai2Document = server.sendRequest(params);
            final ListIdentifiersType listIdentifiers = oai2Document.getListIdentifiers();
            count += listIdentifiers.getHeader().size();
            resumptionToken = oai2Document.getListIdentifiers().getResumptionToken();
        } while (resumptionToken != null);
        return count;
    }

    public void testStaticQueryAndFilteredQuery() {
        // Start a harvest
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("metadataPrefix", "oai_dc");
        params.set("fq", "theme:setSpec1");
        Parameters.setParam("enable_filter_query", true);
        Parameters.setParam("static_query", "theme:setSpec1");

        int count = getCount(params);
        assertEquals(expectedSetSpec, count);
    }

    public void testStaticQueryAndFilteredQueryNoResults() {
        // Start a harvest
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListIdentifiers");
        params.set("metadataPrefix", "oai_dc");
        params.set("fq", "theme:i_do_not_exist");
        Parameters.setParam("enable_filter_query", true);
        Parameters.setParam("static_query", "theme:setSpec1");

        final OAIPMHtype oai2Document = server.sendRequest(params);
        assertEquals(1, oai2Document.getError().size());
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai2Document.getError().get(0).getCode());
    }

}
