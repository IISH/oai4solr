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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.openarchives.oai2.OAIPMHtype;
import org.openarchives.oai2.RequestType;
import org.openarchives.oai2.ResumptionTokenType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Date;

/**
 * OAIQueryResponseWriter
 * <p/>
 * Writes out the resultset in an OAI2 response.
 * <p/>
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
public class OAIQueryResponseWriter implements org.apache.solr.response.QueryResponseWriter {

    private final static QName qname = new QName("http://www.openarchives.org/OAI/2.0/", "OAI-PMH");

    private final Log log = LogFactory.getLog(this.getClass());

    public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {

        final int caller_id = (int) response.getValues().get("id");
        final OAIPMHtype oai = (OAIPMHtype) response.getValues().get("oai");
        oai.setResponseDate(Parsing.getGregorianDate(new Date()));

        Object tmp = response.getValues().get("docList");
        boolean hasRecord = (tmp != null) && ((DocList) tmp).size() != 0;

        if (hasRecord) {
            writer.write("<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" \n" +
                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
                    "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">");
            addResponseDate(writer, oai.getResponseDate());
            addRequest(writer, oai.getRequest());
            openXmlElement(writer, oai.getRequest().getVerb().value());
            ResumptionTokenType resumptionToken = null;
            switch (oai.getRequest().getVerb()) {
                case GET_RECORD:
                    result(caller_id, writer, request, response);
                    break;
                case LIST_RECORDS:
                    result(caller_id, writer, request, response);
                    resumptionToken = oai.getListRecords().getResumptionToken();
                    break;
                case LIST_IDENTIFIERS:
                    result(caller_id, writer, request, response);
                    resumptionToken = oai.getListIdentifiers().getResumptionToken();
                    break;
            }

            addResumptionToken(writer, resumptionToken);

            closeXmlElement(writer, oai.getRequest().getVerb().value());
            writer.write("</OAI-PMH>");
        } else {
            norecords(caller_id, writer, oai);
        }
    }

    private void addResponseDate(Writer writer, XMLGregorianCalendar calendar) throws IOException {

        openXmlElement(writer, "responseDate");
        writer.write(Parsing.parseGregorianDate(calendar));
        closeXmlElement(writer, "responseDate");
    }

    private void addRequest(Writer writer, RequestType request) throws IOException {

        writer.write("<request");
        writeAttribute(writer, "verb", request.getVerb().value());
        if (request.getResumptionToken() == null) {
            writeAttribute(writer, "from", request.getFrom());
            writeAttribute(writer, "until", request.getUntil());
            writeAttribute(writer, "set", request.getSet());
            writeAttribute(writer, "metadataPrefix", request.getMetadataPrefix());
            writeAttribute(writer, "identifier", request.getIdentifier());
        } else {
            writeAttribute(writer, "resumptionToken", request.getResumptionToken());
        }
        writer.write("/>");
    }

    private void addResumptionToken(Writer writer, ResumptionTokenType resumptionToken) throws IOException {

        if (resumptionToken != null) {
            writer.write("<resumptionToken");
            writeAttribute(writer, "cursor", String.valueOf(resumptionToken.getCursor()));
            writeAttribute(writer, "completeListSize", String.valueOf(resumptionToken.getCompleteListSize()));
            writeAttribute(writer, "expirationDate", Parsing.parseGregorianDate(resumptionToken.getExpirationDate()));
            writer.write(">" + resumptionToken.getValue());
            closeXmlElement(writer, "resumptionToken");
        }
    }

    private void writeAttribute(Writer writer, String name, String value) throws IOException {

        if (value != null && !value.isEmpty())
            writer.write(" " + name + "=\"" + value + "\"");
    }

    private void openXmlElement(Writer writer, String name) throws IOException {
        writer.write("<" + name + ">");
    }

    private void closeXmlElement(Writer writer, String name) throws IOException {
        writer.write("</" + name + ">");
    }

    /**
     * norecords
     * <p/>
     * Marshalls the Identifier, ListSets, ListMetadataFormats and errors.
     */
    @SuppressWarnings("unchecked")
    private void norecords(int caller_id, Writer writer, OAIPMHtype oai) throws IOException {

        final Marshaller marshaller = (Marshaller) Parameters.getParam("marshaller");
        final JAXBElement element = new JAXBElement(qname, OAIPMHtype.class, oai);
        try {
            marshaller.marshal(element, writer);
        } catch (JAXBException e) {
            error(writer, e.getMessage());
        }
    }

    private void error(Writer writer, String message) throws IOException {

        writer.write("<error code=\"0\">" + message + "</error>");
        log.error("System error: " + message);
    }

    /**
     * Iterate over each individual Solr record and do the transformation and marshalling into an object.
     * We could transform the entire resultset, but this way we may spare some memory.
     */
    private void result(int caller_id, Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {

        DocList docList = (DocList) response.getValues().get("docList");
        final DocIterator iterator = docList.iterator();
        while (iterator.hasNext()) {
            int docId = iterator.next();
            try {
                writeRecord(caller_id, writer, request, docId);
            } catch (TransformerException e) {
                error(writer, e.getMessage());
            }
        }
    }

    /**
     * Get the Lucene document from the index and add it to a temporary response.
     */
    private void writeRecord(int caller_id, Writer writer, SolrQueryRequest request, int docId) throws IOException, TransformerException {

        SolrParams params = request.getParams();
        final SolrQueryResponse dummy = new SolrQueryResponse();
        dummy.add("solrparams", params.toNamedList()); // May be used in the xslt.
        dummy.add("result", request.getSearcher().doc(docId));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw;
        try {
            osw = new OutputStreamWriter(baos, "UTF-8");
            XMLWriter.writeResponse(osw, request, dummy);
            osw.close();
        } catch (IOException e) {
            error(writer, e.getMessage());
        }

        transform(caller_id, writer, baos, params.get("metadataPrefix"));
    }

    /**
     * Transform the Solr document into a schema using the associate transformer.
     */
    private void transform(int caller_id, Writer writer, ByteArrayOutputStream baos, String metadataPrefix) throws TransformerException {

        StreamSource source = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
        Result result = new StreamResult(writer);
        Templates t = (Templates) Parameters.getParam(caller_id, metadataPrefix);
        Transformer transformer = t.newTransformer();
        transformer.setParameter("prefix", Parameters.getParam(caller_id, "prefix"));
        transformer.transform(source, result);
    }

    public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
        return CONTENT_TYPE_XML_UTF8;
    }

    public void init(NamedList namedList) {
        // Not implemented.
    }
}
