/*
 * OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
 *
 *     Copyright (c) 2011-2017  International Institute of Social History
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

import org.apache.solr.response.SolrQueryResponse;
import org.openarchives.oai2.*;

import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation
 *
 * Utility class to check the validity of OAI2 requests
 */
class Validation {

    final private static Pattern datestampSLong = Pattern.compile("^\\d{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9]Z$|^\\d{4}-[0-1][0-9]-[0-3][0-9]$");
    final private static Pattern datestampShort = Pattern.compile("^\\d{4}-[0-1][0-9]-[0-3][0-9]");

    static boolean error(SolrQueryResponse response, OAIPMHerrorcodeType code) {

        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        OAIPMHerrorType error = new OAIPMHerrorType();
        error.setCode(code);
        oai.getError().add(error);
        return false;
    }

    static boolean error(SolrQueryResponse response, String value, OAIPMHerrorcodeType code) {

        OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        OAIPMHerrorType error = new OAIPMHerrorType();
        error.setValue(value);
        error.setCode(code);
        oai.getError().add(error);
        return false;
    }

    /**
     * isValidIdentifier
     * <p/>
     * See if the identifier exists or is invalid.
     */
    static boolean isValidIdentifier(SolrQueryResponse response, RequestType oaiRequest) {

        final String identifier = oaiRequest.getIdentifier();
        if (identifier == null)
            return error(response, "Missing identifier!", OAIPMHerrorcodeType.BAD_ARGUMENT);

        String[] split = identifier.split(":", 3);
        boolean isValid = split.length == 3 && split[0].equals("oai") && split[2].length() != 0;

        return isValid || error(response, "Invalid identifier format!", OAIPMHerrorcodeType.BAD_ARGUMENT);
    }

    /**
     * isAvailableIdentifier
     * <p/>
     * Use to check if a GetRecord verb produced a single record.
     *
     * @param recordCount Number of records
     * @return true if we have the expected number
     */
    static boolean isAvailableIdentifier(SolrQueryResponse response, int recordCount) {

        return recordCount == 1 || error(response, OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);
    }

    /**
     * hasMatchingRecords
     * <p/>
     * Use to check if the resultset has records.
     *
     * @param recordCount Number of records
     * @return true if we have more than zero records
     */
    static boolean hasMatchingRecords(SolrQueryResponse response, int recordCount) {

        return recordCount != 0 || error(response, OAIPMHerrorcodeType.NO_RECORDS_MATCH);
    }

    /**
     * isValidMetadataPrefix
     * <p/>
     * See if the requested metadataPrefix is supported.
     *
     * @param prefix the id of the caller
     * @return True if supported
     */
    static boolean isValidMetadataPrefix(int prefix, SolrQueryResponse response, RequestType oaiRequest) {

        final String metadataPrefix = oaiRequest.getMetadataPrefix();
        if (metadataPrefix == null) {
            return error(response, "Missing metadata prefix!", OAIPMHerrorcodeType.BAD_ARGUMENT);
        }

        final OAIPMHtype oaipmHtype = Parameters.getParam(prefix, VerbType.LIST_METADATA_FORMATS);
        for (MetadataFormatType metadataFormatType : oaipmHtype.getListMetadataFormats().getMetadataFormat()) {
            if (metadataFormatType.getMetadataPrefix().equals(metadataPrefix)) return true;
        }
        return error(response, OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT);
    }

    /**
     * isValidSet
     * <p/>
     * See if we support ListSets. If so, check if the setSpec exists.
     *
     * @param prefix the id of the caller
     * @param setSpec The requested set
     * @return True if we have a declared setSpec matching the request
     */
    static boolean isValidSet(int prefix, String setSpec, SolrQueryResponse response) {

        if (setSpec == null || setSpec.isEmpty())
            return true;
        final OAIPMHtype oaipmHtype = Parameters.getParam(prefix, VerbType.LIST_SETS);
        if (oaipmHtype == null) {
            return error(response, OAIPMHerrorcodeType.NO_SET_HIERARCHY);
        }
        final List<SetType> sets = oaipmHtype.getListSets().getSet();
        for (SetType setType : sets) {
            if (setType.getSetSpec().equals(setSpec))
                return true;
        }
        return error(response, String.format("Set argument doesn't match any sets. The setSpec was '%s'", setSpec), OAIPMHerrorcodeType.NO_RECORDS_MATCH);
    }

    static boolean isValidDatestamp(int prefix, String datestamp, String range, SolrQueryResponse response) {

        if (datestamp == null) return true;

        final GranularityType granularity = Parameters.getParam(prefix, VerbType.IDENTIFY).getIdentify().getGranularity();
        Pattern pattern = (granularity == GranularityType.YYYY_MM_DD_THH_MM_SS_Z) ? datestampSLong : datestampShort;

        if (!pattern.matcher(datestamp).matches()) return error(response,
                String.format("The '%s' argument '%s' is not a valid UTCdatetime.", range, datestamp),
                OAIPMHerrorcodeType.BAD_ARGUMENT);

        return (granularity == GranularityType.YYYY_MM_DD_THH_MM_SS_Z ||
                datestamp.length() == GranularityType.YYYY_MM_DD.value().length())
                || error(response,
                String.format("The '%s' argument '%s' is outside this repository's granularity '%s'.", range, datestamp, granularity.value()),
                OAIPMHerrorcodeType.BAD_ARGUMENT);
    }

    /**
     * isValidFromUntilCombination
     * <p/>
     * Check if the from parameter is not behind the until.
     *
     * @param from  The from datestamp
     * @param until The until datestamp
     * @return True if from <= until
     */
    static boolean isValidFromUntilCombination(String from, String until, SolrQueryResponse response) throws ParseException {

        return (from == null || until == null || Parsing.parseDatestamp(from).getTime() <= Parsing.parseDatestamp(until).getTime()) || error(response, "Bad date values, must have from<=until", OAIPMHerrorcodeType.BAD_ARGUMENT);
    }

}