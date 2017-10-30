package org.socialhistoryservices.api.oai;

import junit.framework.TestCase;
import org.apache.solr.response.SolrQueryResponse;
import org.openarchives.oai2.*;

import java.math.BigInteger;
import java.text.ParseException;

/**
 * TestValidation
 * <p/>
 * Assert core logic
 */
public class TestValidation extends TestCase {

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
        assertFalse(Validation.isValidIdentifier(response, oaiRequest));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai");
        assertFalse(Validation.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:");
        assertFalse(Validation.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla");
        assertFalse(Validation.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla:");
        assertFalse(Validation.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla:valid");
        assertTrue(Validation.isValidIdentifier(response, oaiRequest));

        oaiRequest.setIdentifier("oai:bla:valid:also");
        assertTrue(Validation.isValidIdentifier(response, oaiRequest));
    }

    public void testIsAvailableIdentifier() {

        assertFalse(Validation.isAvailableIdentifier(response, 0));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        assertFalse(Validation.isAvailableIdentifier(response, 2));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        assertTrue(Validation.isAvailableIdentifier(response, 1));
    }

    public void testHasMatchingRecords() {

        assertTrue(Validation.hasMatchingRecords(response, 1));
        assertTrue(Validation.hasMatchingRecords(response, 2));

        assertFalse(Validation.hasMatchingRecords(response, 0));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai.getError().get(0).getCode());
        oai.getError().clear();
    }

    public void testIsValidMetadataPrefix() {

        final RequestType oaiRequest = new RequestType();

        oaiRequest.setMetadataPrefix(null);
        assertFalse(Validation.isValidMetadataPrefix(0, response, oaiRequest));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_METADATA_FORMATS, oai.getError().get(0).getCode());
        oai.getError().clear();

        ListMetadataFormatsType listMetadataFormatsType = new ListMetadataFormatsType();
        final MetadataFormatType metadataFormatType = new MetadataFormatType();
        metadataFormatType.setMetadataPrefix("some_schema");
        listMetadataFormatsType.getMetadataFormat().add(metadataFormatType);
        final OAIPMHtype forListlistMetadataFormats = new OAIPMHtype();
        forListlistMetadataFormats.setListMetadataFormats(listMetadataFormatsType);
        Parameters.setParam(VerbType.LIST_METADATA_FORMATS, forListlistMetadataFormats);

        oaiRequest.setMetadataPrefix("some_schema");
        assertTrue(Validation.isValidMetadataPrefix(0, response, oaiRequest));

        oaiRequest.setMetadataPrefix("some_other_schema");
        assertFalse(Validation.isValidMetadataPrefix(0, response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, oai.getError().get(0).getCode());
        oai.getError().clear();

    }

    public void testListSets() {

        assertTrue(Validation.isValidSet(0, null, response));

        assertFalse(Validation.isValidSet(0, "some_set", response));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        //assertEquals(OAIPMHerrorcodeType.NO_SET_HIERARCHY, oai.getError().get(0).getCode());
        oai.getError().clear();

        OAIPMHtype oai_with_set = new OAIPMHtype();
        ListSetsType value = new ListSetsType();
        SetType setType = new SetType();
        setType.setSetSpec("some_other_set");
        value.getSet().add(setType);
        oai_with_set.setListSets(value);
        Parameters.setParam("ListSets", oai_with_set);

        assertFalse(Validation.isValidSet(0, "some_set", response));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai.getError().get(0).getCode());
        oai.getError().clear();

        setType.setSetSpec("some_set");
        assertTrue(Validation.isValidSet(0, "some_set", response));
    }

    public void testFormatDate() {

        OAIPMHtype oaiForIdentify = new OAIPMHtype();
        IdentifyType identify = new IdentifyType();
        oaiForIdentify.setIdentify(identify);
        Parameters.setParam(VerbType.IDENTIFY, oaiForIdentify);

        assertEquals("*", Parsing.parseRange(null, "from"));

        identify.setGranularity(GranularityType.YYYY_MM_DD);
        assertEquals("2012-02-03T00:00:00Z", Parsing.parseRange("2012-02-03", "from"));
        assertEquals("2012-02-03T23:59:59Z", Parsing.parseRange("2012-02-03", "until"));

        identify.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
        assertEquals("2012-02-03T04:05:06Z", Parsing.parseRange("2012-02-03T04:05:06Z", "from"));
        assertEquals("2012-02-03T04:05:06Z", Parsing.parseRange("2012-02-03T04:05:06Z", "until"));
    }

    public void testIsValidDatestampRange() {

        OAIPMHtype oaiForIdentify = new OAIPMHtype();
        IdentifyType identify = new IdentifyType();
        oaiForIdentify.setIdentify(identify);
        Parameters.setParam(VerbType.IDENTIFY, oaiForIdentify);

        identify.setGranularity(GranularityType.YYYY_MM_DD);
        assertFalse(Validation.isValidDatestamp(0, "2012", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-01", "a range", response));
        assertTrue(Validation.isValidDatestamp(0, "2012-02-03", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04:05", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04:05:06", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04:05:06Z", "a range", response));

        identify.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
        assertFalse(Validation.isValidDatestamp(0, "2012", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-01", "a range", response));
        assertTrue(Validation.isValidDatestamp(0, "2012-02-03", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04:05", "a range", response));
        assertFalse(Validation.isValidDatestamp(0, "2012-02-03T04:05:06", "a range", response));
        assertTrue(Validation.isValidDatestamp(0, "2012-02-03T04:05:06Z", "a range", response));

        assertFalse(Validation.isValidDatestamp(0, "nonsense", "a range", response));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.BAD_ARGUMENT, oai.getError().get(0).getCode());
        oai.getError().clear();

        assertFalse(Validation.isValidDatestamp(0, "2012-02-a", "a range", response));
        oai = (OAIPMHtype) response.getValues().get("oai");
        assertEquals(OAIPMHerrorcodeType.BAD_ARGUMENT, oai.getError().get(0).getCode());
        oai.getError().clear();

    }

    public void testFromUntil() throws ParseException {

        OAIPMHtype oaiForIdentify = new OAIPMHtype();
        IdentifyType identify = new IdentifyType();
        oaiForIdentify.setIdentify(identify);
        Parameters.setParam(VerbType.IDENTIFY, oaiForIdentify);
        identify.setGranularity(GranularityType.YYYY_MM_DD);

        assertTrue(Validation.isValidFromUntilCombination(null, null, response));
        assertTrue(Validation.isValidFromUntilCombination("some from date", null, response));
        assertTrue(Validation.isValidFromUntilCombination(null, "some until date", response));
        assertTrue(Validation.isValidFromUntilCombination("2012-02-03", "2012-02-03", response));
        assertFalse(Validation.isValidFromUntilCombination("2012-02-03", "2012-02-02", response));

        identify.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
        assertTrue(Validation.isValidFromUntilCombination("2012-02-03T04:05:06Z", "2012-02-03T04:05:06Z", response));
        assertFalse(Validation.isValidFromUntilCombination("2012-02-03T04:05:06Z", "2012-02-03T04:05:05Z", response));

        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
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
        final ResumptionTokenType resumptionTokenEncoded = ResumptionToken.encodeResumptionToken(0, oaiResumptionToken, 0, nextCursor, 1000, (Integer) Parameters.getParam("resumptionTokenExpirationInSeconds", 86400));

        final ResumptionToken resumptionTokenDecoded = ResumptionToken.decodeResumptionToken(0, resumptionTokenEncoded.getValue());

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
        Parameters.setParam(key, true);
        assertTrue((Boolean) Parameters.getParam(key));

        assertEquals("b", Parameters.getParam("a", "b"));
        assertNull(Parameters.getParam("c"));

        Parameters.setParam(key, null);
        assertNull(Parameters.getParam(key));
    }

    public void testJoin() {

        String[] queryParts = {"a", "b", "c"};
        String join = Parsing.join(queryParts, " AND ");
        assertEquals("a AND b AND c", join);
    }

}
