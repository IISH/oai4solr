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

import org.openarchives.oai2.GranularityType;
import org.openarchives.oai2.OAIPMHtype;
import org.openarchives.oai2.VerbType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Parsing
 *
 * Utility class to parse request values to Solr Lucene queries and format dates into the two OAI2 UTCdatetime datestamps.
 */
public class Parsing {


    /**
     * getGregorianDate
     * <p/>
     * Convert a Java date into a GregorianCalendar date in the Zulu timezone.
     */
    static XMLGregorianCalendar getGregorianDate(Date date) {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            date2.setTimezone(0); // Zulu
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return date2;
    }

    /**
     * parseDatestamp
     *
     * Parse a UTCdatetime datestamp String into a Date
     */
    static Date parseDatestamp(String datestamp) throws ParseException {

        final SimpleDateFormat dateFormat = (datestamp.length() == GranularityType.YYYY_MM_DD_THH_MM_SS_Z.value().length()) ?
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'") :
                new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(datestamp);
    }

    /**
     * getGregorianDate
     * <p/>
     * Convert a String into a GregorianCalendar date in the Zulu timezone.
     */
    public static XMLGregorianCalendar getGregorianDate(String datestamp) {

        XMLGregorianCalendar gregorianDate = null;
        try {
            gregorianDate = getGregorianDate(parseDatestamp(datestamp));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return gregorianDate;
    }

    /**
     * parseGregorianDate
     * <p/>
     * returns a datestamp in the allowed OAI2 datestamp format:
     *
     * @param date A XMLGregorianCalendar date
     * @return A datestamp in the OAI2 proscribed format
     */
    public static String parseGregorianDate(XMLGregorianCalendar date) {

        date.setTimezone(0); // Zulu
        int l = date.toString().length();
        return (l < 19) ? date.toString() : date.toString().substring(0, 19) + "Z";   // length of YYYY-MM-DDThh:mm:ssZ
    }

    static String join(String[] s, String glue) {

        int k = s.length;
        if (k == 0) return null;
        StringBuilder out = new StringBuilder();
        out.append(s[0]);
        for (int x = 1; x < k; ++x)
            out.append(glue).append(s[x]);
        return out.toString();
    }



    /**
     * parseRange
     * <p/>
     * The range will determine to pad to the present or future:
     * range(from)  => 2001-02-03 => 2001-02-03T00:00:00Z
     * range(until) => 2001-02-03 => 2001-02-03T23:59:59Z
     * <p/>
     *
     * @return An ISO 8601 formatted date or an infinite range Lucene character when the date is null
     */
    static String parseRange(String datestamp, String range) {

        if (datestamp == null) return "*";

        return (datestamp.length() == GranularityType.YYYY_MM_DD_THH_MM_SS_Z.value().length()) ? datestamp
                : (range.equalsIgnoreCase("from")) ? datestamp.concat("T00:00:00Z") : datestamp.concat("T23:59:59Z");
    }



    /**
     * stripOaiPrefix
     * <p/>
     * Remove the oai prefix and return the identifier.
     * oai:domain:identifier => identifier
     *
     * @param identifier the oai identifier
     * @return A local identifier
     */
    static String stripOaiPrefix(String identifier) {

        final String prefix = (String) Parameters.getParam("prefix");
        final int length_prefix = prefix.length();
        if (identifier.length() < length_prefix) return "unparsable";
        return identifier.substring(length_prefix);
    }

    /**
     * loadStaticVerb
     * <p/>
     * Loads an Identity.xml, ListSets.xml or ListMetadataPrefix.xml from file and unmarshalls it.
     *
     * @param verb The OAI2 verb
     * @return The OAIPMHtype instance
     */
    @SuppressWarnings("unchecked")
    static OAIPMHtype loadStaticVerb(VerbType verb) throws FileNotFoundException, JAXBException {

        final File f = new File(Parameters.getParam("oai_home") + File.separator + verb.value() + ".xml");
        final FileInputStream fis = new FileInputStream(f);
        final Source source = new StreamSource(fis);
        final Unmarshaller marshaller = (Unmarshaller) Parameters.getParam("unmarshaller");
        return ((JAXBElement<OAIPMHtype>) marshaller.unmarshal(source)).getValue();
    }

}