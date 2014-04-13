/*
 * OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
 *
 *     Copyright (c) 2011-2014  International Institute of Social History
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
import org.openarchives.oai2.RequestType;
import org.openarchives.oai2.ResumptionTokenType;
import org.openarchives.oai2.VerbType;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Date;

/**
 * Class for constructing the resumptionToken
 *
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
public class ResumptionToken extends RequestType {

   private boolean good_resumptionToken = true;
    private int cursor = 0;

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public boolean isGood_resumptionToken() {
        return good_resumptionToken;
    }

    public void setResumptiontokenHealth(boolean good_resumptionToken) {
        this.good_resumptionToken = good_resumptionToken;
    }

    /**
     * encodeResumptionToken
     * <p/>
     * Parses a ResumptionTokenType instance into a base 64 encoded string. This string will serve as the
     * resumption token.
     *
     * @throws java.io.UnsupportedEncodingException
     */
    public static ResumptionTokenType encodeResumptionToken(ResumptionToken oaiResumptionToken, int cursor, int nextCursor, int matches, int resumptionTokenExpirationInSeconds) throws UnsupportedEncodingException {

        final ResumptionTokenType resumptionToken = new ResumptionTokenType();
        resumptionToken.setCursor(BigInteger.valueOf(cursor));
        final long t = new Date().getTime() + 1000L * resumptionTokenExpirationInSeconds;
        resumptionToken.setExpirationDate(Parsing.getGregorianDate(new Date(t)));
        resumptionToken.setCompleteListSize(BigInteger.valueOf(matches));

        // Parameters are in fixed order: "verb", "from", "until","set","metadataPrefix","cursor"
        final String s = (String) Parameters.getParam("separator", ",");
        String token = oaiResumptionToken.getVerb().value() + s + getValue(oaiResumptionToken.getFrom()) + s + getValue(oaiResumptionToken.getUntil()) + s + getValue(oaiResumptionToken.getSet()) + s + getValue(oaiResumptionToken.getMetadataPrefix()) + s + nextCursor;
        final byte[] bytes = token.getBytes("utf-8");
        resumptionToken.setValue(Base64.byteArrayToBase64(bytes, 0, bytes.length));
        return resumptionToken;
    }

    private static String getValue(String text) {
        return (text == null) ? "" : text.trim();
    }

    /**
     * decodeResumptionToken
     * <p/>
     * Parse a base 64 encoded value into a resumption token.
     * The string is separated by values. Turned into an array by the separator, the
     * OAI2 parameters are in fixed order: "from", "until","set","metadataPrefix","cursor"
     *
     * @param token The resumption token as base 64 encoded String
     * @return The resumption token instance
     * @throws Exception
     */
    public static ResumptionToken decodeResumptionToken(String token) throws Exception {

        if (token == null)
            return null;
        final byte[] bytes = Base64.base64ToByteArray(token);
        String pt = new String(bytes, "utf-8");
        final String separator = (String) Parameters.getParam("separator", ",");
        final String[] split = pt.split(separator, 6);
        ResumptionToken re = new ResumptionToken();

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

}
