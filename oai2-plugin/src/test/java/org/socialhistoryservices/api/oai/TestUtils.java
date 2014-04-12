package org.socialhistoryservices.api.oai;

import junit.framework.TestCase;
import org.apache.solr.response.SolrQueryResponse;
import org.openarchives.oai2.*;

import java.math.BigInteger;

/**
 * TestUtils
 * <p/>
 * Assert core logic
 */
public class TestUtils extends TestCase {

    SolrQueryResponse response;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        response = new SolrQueryResponse();
        response.add("oai", new OAIPMHtype());
    }

    @Override
    protected void tearDown() throws Exception {
        response.getValues().remove("oai");
    }

    public void testIsValidIdentifier() {

        final RequestType oaiRequest = new RequestType();

        oaiRequest.setIdentifier(null);
        assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai");
        assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:");
        assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla");
        assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla:");
        assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla:valid");
        assertTrue(Utils.isValidIdentifier(response, oaiRequest));

        oaiRequest.setIdentifier("oai:bla:valid:also");
        assertTrue(Utils.isValidIdentifier(response, oaiRequest));
    }

    public void testIsAvailableIdentifier() {

        assertFalse(Utils.isAvailableIdentifier(response, 0));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        assertFalse(Utils.isAvailableIdentifier(response, 2));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        assertTrue(Utils.isAvailableIdentifier(response, 1));
    }

    public void testHasMatchingRecords() {

        assertTrue(Utils.hasMatchingRecords(response, 1));
        assertTrue(Utils.hasMatchingRecords(response, 2));

        assertFalse(Utils.hasMatchingRecords(response, 0));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai.getError().get(0).getCode());
        oai.getError().clear();
    }

    public void testIsValidMetadataPrefix() {

        final RequestType oaiRequest = new RequestType();

        oaiRequest.setMetadataPrefix(null);
        assertFalse(Utils.isValidMetadataPrefix(response, oaiRequest));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_METADATA_FORMATS, oai.getError().get(0).getCode());
        oai.getError().clear();

        ListMetadataFormatsType listMetadataFormatsType = new ListMetadataFormatsType();
        final MetadataFormatType metadataFormatType = new MetadataFormatType();
        metadataFormatType.setMetadataPrefix("some_schema");
        listMetadataFormatsType.getMetadataFormat().add(metadataFormatType);
        final OAIPMHtype forListlistMetadataFormats = new OAIPMHtype();
        forListlistMetadataFormats.setListMetadataFormats(listMetadataFormatsType);
        Utils.setParam(VerbType.LIST_METADATA_FORMATS, forListlistMetadataFormats);

        oaiRequest.setMetadataPrefix("some_schema");
        assertTrue(Utils.isValidMetadataPrefix(response, oaiRequest));

        oaiRequest.setMetadataPrefix("some_other_schema");
        assertFalse(Utils.isValidMetadataPrefix(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, oai.getError().get(0).getCode());
        oai.getError().clear();

    }

    public void testListSets() {

        assertTrue(Utils.isValidSet(null, response));

        assertFalse(Utils.isValidSet("some_set", response));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_SET_HIERARCHY, oai.getError().get(0).getCode());
        oai.getError().clear();

        OAIPMHtype oai_with_set = new OAIPMHtype();
        ListSetsType value = new ListSetsType();
        SetType setType = new SetType();
        setType.setSetSpec("some_other_set");
        value.getSet().add(setType);
        oai_with_set.setListSets(value);
        Utils.setParam("ListSets", oai_with_set);

        assertFalse(Utils.isValidSet("some_set", response));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai.getError().get(0).getCode());
        oai.getError().clear();

        setType.setSetSpec("some_set");
        assertTrue(Utils.isValidSet("some_set", response));
    }

    public void testFormatDate() {

        assertEquals("*", Utils.parseRange(null));
        assertEquals("2012-01-01T00:00:00Z", Utils.parseRange("2012"));
        assertEquals("2012-02-01T00:00:00Z", Utils.parseRange("2012-02"));
        assertEquals("2012-02-03T00:00:00Z", Utils.parseRange("2012-02-03"));
        assertEquals("2012-02-03T04:00:00Z", Utils.parseRange("2012-02-03T04"));
        assertEquals("2012-02-03T04:05:00Z", Utils.parseRange("2012-02-03T04:05"));
        assertEquals("2012-02-03T04:05:06Z", Utils.parseRange("2012-02-03T04:05:06"));
        assertEquals("2012-02-03T04:05:06Z", Utils.parseRange("2012-02-03T04:05:06Z"));
        assertEquals("2012-02-03T04:05:06Z", Utils.parseRange("2012-02-03T04:05:06.789sZ"));
    }

    public void testIsValidDatestampRange() {

        OAIPMHtype oaiForIdentify = new OAIPMHtype();
        IdentifyType identify = new IdentifyType();
        oaiForIdentify.setIdentify(identify);
        Utils.setParam(VerbType.IDENTIFY, oaiForIdentify);

        identify.setGranularity(GranularityType.YYYY_MM_DD);
        assertTrue(Utils.isValidDatestamp("2012", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-01", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-02-03", "a range", response));
        assertFalse(Utils.isValidDatestamp("2012-02-03T04", "a range", response));
        assertFalse(Utils.isValidDatestamp("2012-02-03T04:05", "a range", response));
        assertFalse(Utils.isValidDatestamp("2012-02-03T04:05:06", "a range", response));
        assertFalse(Utils.isValidDatestamp("2012-02-03T04:05:06Z", "a range", response));

        identify.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
        assertTrue(Utils.isValidDatestamp("2012", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-01", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-02-03", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-02-03T04", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-02-03T04:05", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-02-03T04:05:06", "a range", response));
        assertTrue(Utils.isValidDatestamp("2012-02-03T04:05:06Z", "a range", response));

        assertFalse(Utils.isValidDatestamp(Utils.parseRange(""), "a range", response));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.BAD_ARGUMENT, oai.getError().get(0).getCode());
        oai.getError().clear();

        assertFalse(Utils.isValidDatestamp(Utils.parseRange("2012-02-a"), "a range", response));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.BAD_ARGUMENT, oai.getError().get(0).getCode());
        oai.getError().clear();

    }

    public void testResumptionToken() throws Exception {

        final ResumptionToken oaiResumptionToken = new ResumptionToken();
        oaiResumptionToken.setVerb(VerbType.GET_RECORD);
        oaiResumptionToken.setFrom("from");
        oaiResumptionToken.setUntil("until");
        oaiResumptionToken.setMetadataPrefix("prefix");

        int nextCursor = 200;
        final ResumptionTokenType resumptionTokenEncoded = ResumptionToken.encodeResumptionToken(oaiResumptionToken, 0, nextCursor, 1000, (Integer) Utils.getParam("resumptionTokenExpirationInSeconds", 86400));

        final ResumptionToken resumptionTokenDecoded = ResumptionToken.decodeResumptionToken(resumptionTokenEncoded.getValue());

        BigInteger add = resumptionTokenEncoded.getCursor().add(BigInteger.valueOf(nextCursor));
        BigInteger cursor = BigInteger.valueOf(resumptionTokenDecoded.getCursor());
        assertEquals(add, cursor);
        assertEquals(oaiResumptionToken.getVerb(), resumptionTokenDecoded.getVerb());
        assertNull(resumptionTokenDecoded.getIdentifier());
        assertEquals(oaiResumptionToken.getFrom(), resumptionTokenDecoded.getFrom());
        assertEquals(oaiResumptionToken.getUntil(), resumptionTokenDecoded.getUntil());
        assertEquals(oaiResumptionToken.getMetadataPrefix(), resumptionTokenDecoded.getMetadataPrefix());
    }

    public void testParams() {

        String key = "Now it is there";
        Utils.setParam(key, true);
        assertTrue((Boolean) Utils.getParam(key));

        assertEquals("b", Utils.getParam("a", "b"));
        assertNull(Utils.getParam("c"));

        Utils.setParam(key, null);
        assertNull(Utils.getParam(key));
    }

    public void testJoin() {

        String[] queryParts = {"a", "b", "c"};
        String join = Utils.join(queryParts, " AND ");
        assertEquals("a AND b AND c", join);
    }

}
