package org.socialhistoryservices.api.oai;

import junit.framework.Assert;
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
        response.getValues().remove("oai")  ;
    }

    public void testIsValidIdentifier() {

        final RequestType oaiRequest = new RequestType();

        oaiRequest.setIdentifier(null);
        Assert.assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai");
        Assert.assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:");
        Assert.assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla");
        Assert.assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla:");
        Assert.assertFalse(Utils.isValidIdentifier(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        oaiRequest.setIdentifier("oai:bla:valid");
        Assert.assertTrue(Utils.isValidIdentifier(response, oaiRequest));

        oaiRequest.setIdentifier("oai:bla:valid:also");
        Assert.assertTrue(Utils.isValidIdentifier(response, oaiRequest));
    }

    public void testIsAvailableIdentifier() {

        Assert.assertFalse(Utils.isAvailableIdentifier(response, 0));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        Assert.assertFalse(Utils.isAvailableIdentifier(response, 2));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, oai.getError().get(0).getCode());
        oai.getError().clear();

        Assert.assertTrue(Utils.isAvailableIdentifier(response, 1));
    }

    public void testHasMatchingRecords() {

        Assert.assertTrue(Utils.hasMatchingRecords(response, 1));
        Assert.assertTrue(Utils.hasMatchingRecords(response, 2));

        Assert.assertFalse(Utils.hasMatchingRecords(response, 0));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai.getError().get(0).getCode());
        oai.getError().clear();
    }

    public void testIsValidMetadataPrefix() {

        final RequestType oaiRequest = new RequestType();

        oaiRequest.setMetadataPrefix(null);
        Assert.assertFalse(Utils.isValidMetadataPrefix(response, oaiRequest));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.NO_METADATA_FORMATS, oai.getError().get(0).getCode());
        oai.getError().clear();

        Utils.setParam("some_schema", true);
        oaiRequest.setMetadataPrefix("some_schema");
        Assert.assertTrue(Utils.isValidMetadataPrefix(response, oaiRequest));

        Utils.setParam("some_schema", null);
        Assert.assertFalse(Utils.isValidMetadataPrefix(response, oaiRequest));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, oai.getError().get(0).getCode());
        oai.getError().clear();

    }

    public void testListSets() {

        Assert.assertTrue(Utils.isValidSet(null, response));

        Assert.assertFalse(Utils.isValidSet("some_set", response));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.NO_SET_HIERARCHY, oai.getError().get(0).getCode());
        oai.getError().clear();

        OAIPMHtype oai_with_set = new OAIPMHtype();
        ListSetsType value = new ListSetsType();
        SetType setType = new SetType();
        setType.setSetSpec("some_other_set");
        value.getSet().add(setType);
        oai_with_set.setListSets(value);
        Utils.setParam("ListSets", oai_with_set);

        Assert.assertFalse(Utils.isValidSet("some_set", response));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.NO_RECORDS_MATCH, oai.getError().get(0).getCode());
        oai.getError().clear();

        setType.setSetSpec("some_set");
        Assert.assertTrue(Utils.isValidSet("some_set", response));
    }

    public void testFormatDate() {

        Assert.assertEquals("*", Utils.parseRange(null));
        Assert.assertEquals("2012-01-01T00:00:00Z", Utils.parseRange("2012"));
        Assert.assertEquals("2012-02-01T00:00:00Z", Utils.parseRange("2012-02"));
        Assert.assertEquals("2012-02-03T00:00:00Z", Utils.parseRange("2012-02-03"));
        Assert.assertEquals("2012-02-03T04:00:00Z", Utils.parseRange("2012-02-03T04"));
        Assert.assertEquals("2012-02-03T04:05:00Z", Utils.parseRange("2012-02-03T04:05"));
        Assert.assertEquals("2012-02-03T04:05:06Z", Utils.parseRange("2012-02-03T04:05:06"));
        Assert.assertEquals("2012-02-03T04:05:06Z", Utils.parseRange("2012-02-03T04:05:06Z"));
        Assert.assertEquals("2012-02-03T04:05:06Z", Utils.parseRange("2012-02-03T04:05:06.789s"));
    }

    public void testIsValidDatestampRange() {

        Assert.assertTrue(Utils.isValidDatestamp("2012", "a range", response));
        Assert.assertTrue(Utils.isValidDatestamp("2012-01", "a range", response));
        Assert.assertTrue(Utils.isValidDatestamp("2012-02-03", "a range", response));
        Assert.assertTrue(Utils.isValidDatestamp("2012-02-03T04", "a range", response));
        Assert.assertTrue(Utils.isValidDatestamp("2012-02-03T04:05", "a range", response));
        Assert.assertTrue(Utils.isValidDatestamp("2012-02-03T04:05:06", "a range", response));
        Assert.assertTrue(Utils.isValidDatestamp("2012-02-03T04:05:06Z", "a range", response));

        Assert.assertFalse(Utils.isValidDatestamp(Utils.parseRange(""), "a range", response));
        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.BAD_ARGUMENT, oai.getError().get(0).getCode());
        oai.getError().clear();

        Assert.assertFalse(Utils.isValidDatestamp(Utils.parseRange("2012-02-a"), "a range", response));
        oai = (OAIPMHtype) response.getValues().get("oai");
        Assert.assertEquals(OAIPMHerrorcodeType.BAD_ARGUMENT, oai.getError().get(0).getCode());
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
        Assert.assertEquals(add, cursor);
        Assert.assertEquals(oaiResumptionToken.getVerb(), resumptionTokenDecoded.getVerb());
        Assert.assertNull(resumptionTokenDecoded.getIdentifier());
        Assert.assertEquals(oaiResumptionToken.getFrom(), resumptionTokenDecoded.getFrom());
        Assert.assertEquals(oaiResumptionToken.getUntil(), resumptionTokenDecoded.getUntil());
        Assert.assertEquals(oaiResumptionToken.getMetadataPrefix(), resumptionTokenDecoded.getMetadataPrefix());
    }

    public void testParams(){

        String key = "Now it is there";
        Utils.setParam(key, true);
        Assert.assertTrue((Boolean) Utils.getParam(key));

        Assert.assertEquals("b", Utils.getParam("a", "b"));
        Assert.assertNull(Utils.getParam("c"));

        Utils.setParam(key, null);
        Assert.assertNull(Utils.getParam(key));
    }

    public void testJoin(){

        String[] queryParts = {"a", "b", "c"};
        String join = Utils.join(queryParts, " AND ");
        Assert.assertEquals("a AND b AND c", join);
    }

}
