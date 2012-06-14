/*
 * OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
 *
 *     Copyright (C) 2011  International Institute of Social History
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.socialhistoryservices.api.oai;

import org.apache.solr.common.util.Base64;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.openarchives.oai2.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * Utility class
 */
public class Utils {

    final private static HashMap<String, Object> parameters = new HashMap<String, Object>();

    public static boolean error(SolrQueryResponse response, OAIPMHerrorcodeType code) {

        Object oai = response.getValues().get("oai");
        if (oai == null)
            oai = new OAIPMHtype();
        OAIPMHerrorType error = new OAIPMHerrorType();
        error.setCode(code);
        ((OAIPMHtype) oai).getError().add(error);
        response.add("oai", oai);
        return false;
    }

    public static boolean error(SolrQueryResponse response, String value, OAIPMHtype oai) {

        OAIPMHerrorType error = new OAIPMHerrorType();
        error.setValue(value);
        oai.getError().add(error);
        response.add("oai", oai);
        return false;
    }

    public static RecordType error(Exception e) {

        RecordType rt = new RecordType();
        MetadataType md = new MetadataType();
        md.setAny(e.getMessage());
        rt.setMetadata(md);
        return rt;
    }


    public static boolean isValidIdentifier(SolrQueryResponse response, RequestType oaiRequest) {

        final String identifier = oaiRequest.getIdentifier();
        if (identifier == null) {
            return error(response, OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);
        }

        if (true) // ToDo: remove later. The index only contains local identifiers.
            return true;

        // Is the identifier valid ?
        // We only check if identifier consist of three parts, starting with oai
        String[] split = identifier.split(":", 3);
        if (split.length != 3 || !split[0].equals("oai")) {
            return error(response, OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);
        }
        return true;
    }

    public static boolean isAvailableIdentifier(SolrQueryResponse response, int m) {

        if (m == 1)
            return true;
        return error(response, OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);
    }

    public static boolean isValidMetadataPrefix(SolrQueryResponse response, RequestType oaiRequest, OAIPMHtype oai) {

        final String metadataPrefix = oaiRequest.getMetadataPrefix();
        if (metadataPrefix == null) {
            return error(response, OAIPMHerrorcodeType.NO_METADATA_FORMATS);
        }
        if (parameters.containsKey(metadataPrefix))
            return true;
        return error(response, OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT);
    }

    public static boolean isValidSet(String setParam, SolrQueryResponse response, RequestType oaiRequest, OAIPMHtype oai) {

        if (setParam == null || setParam.isEmpty())
            return true;
        final Object o = getParam("ListSets");
        if (o == null) {
            return error(response, OAIPMHerrorcodeType.NO_SET_HIERARCHY);
        }
        OAIPMHtype oaipmHtype = (OAIPMHtype) o;
        final List<SetType> sets = oaipmHtype.getListSets().getSet();
        for (SetType setType : sets) {
            if (setType.getSetSpec().equals(setParam))
                return true;
        }
        return error(response, "No such set.", oai);
    }

    public static boolean hasMatchingRecords(SolrQueryResponse response, int size) {

        if (size == 0) {
            return error(response, OAIPMHerrorcodeType.NO_RECORDS_MATCH);
        }
        return true;
    }

    public static XMLGregorianCalendar getGregorianDate(Date date) {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return date2;
    }

    public static String join(String[] s, String glue) {

        int k = s.length;
        if (k == 0) return null;
        StringBuilder out = new StringBuilder();
        out.append(s[0]);
        for (int x = 1; x < k; ++x)
            out.append(glue).append(s[x]);
        return out.toString();
    }

    public static Object getParam(String key) {

        return parameters.get(key);
    }

    public static Object getParam(String key, Object def) {

        Object o = parameters.get(key);
        if (o == null)
            o = def;
        return def;
    }

    public static void setParam(NamedList args, String key, Object def) {

        Object value = args.get(key);
        if (value == null)
            value = def;
        Utils.setParam(key, value);
    }

    public static void setParam(String key, Object o) {

        parameters.put(key, o);
    }

    public static String formatDate(String date) {

        final String template = "2010-01-01T00:00:00Z";
        final int tl = template.length();
        final int dl = date.length();
        if (dl > tl)
            return date.substring(0, tl - 1) + "Z"; // Not entirely correct... GMT correction needed.
        return date + template.substring(tl);
    }

    public static String parseDate(String date) {

        return (date == null || date.isEmpty())
                ? "*"
                : Utils.formatDate(date);
    }

    public static ResumptionToken parseResumptionToken(String token) throws Exception {

        if (token == null)
            return null;
        final byte[] bytes = Base64.base64ToByteArray(token);
        String pt = new String(bytes, "utf-8");
        final String separator = (String) Utils.getParam("separator", ",");
        final String[] split = pt.split(separator, 6);
        ResumptionToken re = new ResumptionToken();
        // Parameters are in fixed order: "from", "until","set","metadataPrefix","cursor"
        re.setVerb(VerbType.fromValue(split[0]));
        if (!split[1].isEmpty())
            re.setFrom(split[1]);
        if (!split[2].isEmpty())
            re.setUntil(split[2]);
        if (!split[3].isEmpty())
            re.setSet(split[3]);
        if (!split[4].isEmpty())
            re.setMetadataPrefix(split[4]);
        int cursor = (split[5].isEmpty())
                ? 0
                : Integer.parseInt(split[5]);
        re.setCursor(cursor);
        re.setResumptionToken(token);
        return re;
    }

    public static ResumptionTokenType setResumptionToken(ResumptionToken oaiRequest, int cursor, int nextCursor, int matches) throws UnsupportedEncodingException {

        final ResumptionTokenType resumptionToken = new ResumptionTokenType();
        resumptionToken.setCursor(BigInteger.valueOf(cursor));
        final int resumptionTokenExpirationInSeconds = (Integer) getParam("resumptionTokenExpirationInSeconds");
        final long t = new Date().getTime() + 1000L * resumptionTokenExpirationInSeconds;
        resumptionToken.setExpirationDate(Utils.getGregorianDate(new Date(t)));
        resumptionToken.setCompleteListSize(BigInteger.valueOf(matches));

        // Parameters are in fixed order: "verb", "from", "until","set","metadataPrefix","cursor"
        final String s = (String) Utils.getParam("separator", ",");
        String token = oaiRequest.getVerb().value() + s + getValue(oaiRequest.getFrom()) + s + getValue(oaiRequest.getUntil()) + s + getValue(oaiRequest.getSet()) + s + getValue(oaiRequest.getMetadataPrefix()) + s + nextCursor;
        final byte[] bytes = token.getBytes("utf-8");
        resumptionToken.setValue(Base64.byteArrayToBase64(bytes, 0, bytes.length));
        return resumptionToken;
    }

    private static String getValue(String text) {
        if (text == null)
            return "";
        return text.trim();
    }

    public static String stripOaiPrefix(String identifier) {

        final String prefix = (String) Utils.getParam("prefix");
        final String id = identifier.substring(prefix.length());
        System.out.println("Prefix is " + prefix);
        System.out.println("Identifier is " + identifier);
        System.out.println("Looking for " + id);
        return id;
    }
}