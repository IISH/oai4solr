package org.socialhistoryservices.api.oai;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
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

    public static String CORE = "core0";

    private EmbeddedServer server;


    protected void setUp() throws Exception {
        super.setUp();
        String solr_home = System.getProperty("solr.solr.home", deriveSolrHome());
        Utils.clearParams();
        server = new EmbeddedServer(new CoreContainer(solr_home, new File(solr_home, "solr.xml")), CORE);
    }

    private String deriveSolrHome() {

        String solr_relative_home = "/oai2-plugin/src/test/solr";

        File file = new File(System.getProperty("user.dir"));
        while (!file.getName().equals("oai4solr")) {
            file = file.getParentFile();
            if (file == null)
                break;
        }

        if (file == null) {
            log.fatal("Cannot find the Solr directory. Please set the VM property with -Dsolr.solr.home=[path?]/oai4solr" + solr_relative_home);
            System.exit(-1);
        }

        String solr_home = new File(file, solr_relative_home).getAbsolutePath();
        System.setProperty("solr.solr.home", solr_home);
        return solr_home;
    }

    protected void tearDown() {
        server.shutdown();
    }

    private void deleteIndex() {
        try {
            server.deleteByQuery("*:*");
            server.optimize();
        } catch (SolrServerException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * testVerbIdentify
     * <p/>
     * See if we receive an Identify response and if it holds against the Identify.xml document.
     *
     * @throws ParseException
     * @throws SolrServerException
     * @throws IOException
     */
    public void testVerbIdentify() throws ParseException, SolrServerException, IOException, XPathExpressionException, JAXBException {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "Identify");
        OAIPMHtype oai2Document = server.sendRequest(params);

        OAIPMHtype oaipmHtype = Utils.loadStaticVerb(VerbType.IDENTIFY);
        IdentifyType identify = oai2Document.getIdentify();
        Assert.assertNotNull(identify);

        DescriptionType description = identify.getDescription().get(0);
        String sampleIdentifierFromResponse = server.GetNode((Node) description.getAny(), "//oai-identifier:sampleIdentifier");
        String sampleIdentifierFromFile = server.GetNode((Node) oaipmHtype.getIdentify().getDescription().get(0).getAny(), "//oai-identifier:sampleIdentifier");
        Assert.assertEquals(sampleIdentifierFromResponse, sampleIdentifierFromFile);
    }

    /**
     * testListSets
     * <p/>
     * See if we receive a ListSet response.
     *
     * @throws ParseException
     * @throws SolrServerException
     * @throws IOException
     */
    public void testListSets() throws ParseException, SolrServerException, IOException, JAXBException {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListSets");
        OAIPMHtype oai2Document = server.sendRequest(params);
        ListSetsType listSets = oai2Document.getListSets();

        OAIPMHtype oaipmHtype = Utils.loadStaticVerb(VerbType.LIST_SETS);

        for (SetType setTypeFromRequest : listSets.getSet()) {
            boolean match = false;
            for (SetType setTypeFromFile : oaipmHtype.getListSets().getSet()) {
                match = setTypeFromFile.getSetName().equals(setTypeFromRequest.getSetName()) &&
                        setTypeFromFile.getSetSpec().equals(setTypeFromRequest.getSetSpec());
                if (match)
                    break;
            }
            Assert.assertTrue(match);
        }
    }


    public void testListSetsNoSetSpec() {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListRecords");
        params.set("metadataPrefix", "oai_dc");
        params.set("set", "some_setSpec");
        OAIPMHtype oai2Document = server.sendRequest(params);
        Assert.assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai2Document.getError().get(0).getCode());

        Utils.setParam("ListSets", null);
        oai2Document = server.sendRequest(params);
        Assert.assertEquals(OAIPMHerrorcodeType.NO_SET_HIERARCHY, oai2Document.getError().get(0).getCode());
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
        Assert.assertNotNull(listMetadataFormats);

        OAIPMHtype oaipmHtype = Utils.loadStaticVerb(VerbType.LIST_METADATA_FORMATS);

        for (MetadataFormatType metadataFormatTypeFromRequest : listMetadataFormats.getMetadataFormat()) {
            boolean match = false;
            for (MetadataFormatType metadataFormatTypeFromFile : oaipmHtype.getListMetadataFormats().getMetadataFormat()) {
                match = metadataFormatTypeFromRequest.getSchema().equals(metadataFormatTypeFromFile.getSchema()) ||
                        metadataFormatTypeFromRequest.getMetadataNamespace().equals(metadataFormatTypeFromFile.getMetadataNamespace()) ||
                        metadataFormatTypeFromRequest.getMetadataPrefix().equals(metadataFormatTypeFromFile.getMetadataPrefix()) ;
                if (match)
                    break;
            }
            Assert.assertTrue(match);
        }
    }

    public void testResumptionToken() throws UnsupportedEncodingException {

        ResumptionToken token = new ResumptionToken();
        token.setVerb(VerbType.LIST_IDENTIFIERS);
        token.setMetadataPrefix("oai_dc");
        ResumptionTokenType resumptionTokenType = ResumptionToken.encodeResumptionToken(token, 0, 200, 1000, (Integer) Utils.getParam("resumptionTokenExpirationInSeconds"));

        String bad_token = resumptionTokenType.getValue().concat("12345");
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "ListRecords");
        params.set("resumptionToken", resumptionTokenType.getValue());
        OAIPMHtype oai2Document = server.sendRequest(params);
        Assert.assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai2Document.getError().get(0).getCode());

        params.set("resumptionToken", bad_token);
        oai2Document = server.sendRequest(params);
        Assert.assertEquals(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, oai2Document.getError().get(0).getCode());

    }

    /**
     * testHarvestFlow
     * <p/>
     * Test a harvesting cycle with resumption tokens
     */
    public void testHarvest() throws IOException, SolrServerException {

        deleteIndex();
        int recordCount = 1000;
        for (int i = 0; i < recordCount; i++) {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("identifier", i);
            document.addField("datestamp", new Date(i));
            document.addField("full_title", "A title " + i);
            server.add(document);
        }
        server.commit();

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
                String expected = Utils.getParam("prefix") + String.valueOf(i);
                String actual = header.getIdentifier();
                Assert.assertEquals(expected, actual);

                getRecord(expected, "A title " + i);
            }
            resumptionToken = oai2Document.getListIdentifiers().getResumptionToken();
        } while (resumptionToken != null);

        Assert.assertEquals(recordCount, count);

        deleteIndex();
    }

    private void getRecord(String identifier, String expected_title) {

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("verb", "GetRecord");
        params.set("metadataPrefix", "oai_dc");
        params.set("identifier", identifier);
        final OAIPMHtype oai2Document = server.sendRequest(params);
        Assert.assertEquals(identifier,
                oai2Document.getGetRecord().getRecord().getHeader().getIdentifier());
        String actual_title = server.GetNode((Node) oai2Document.getGetRecord().getRecord().getMetadata().getAny(), "//dc:title");
        Assert.assertEquals(expected_title, actual_title);
    }

}
