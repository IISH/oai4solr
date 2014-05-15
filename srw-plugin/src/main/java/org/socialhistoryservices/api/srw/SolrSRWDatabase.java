/*
Copyright 2010 International Institute for Social History, The Netherlands.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.socialhistoryservices.api.srw;

import ORG.oclc.os.SRW.*;
import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.server.AxisServer;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrQueryParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLTermNode;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Lucien van Wouw
 * Date: 15-dec-2009
 * Time: 20:46:56
 * To change this template use File | Settings | File Templates.
 */
public class SolrSRWDatabase extends SRWDatabase {
    // Transform the record from the Solr result set with the specified transformer
    // Add ExtraRecordData and Identifier fields to the record.

    /**
     * @param rec      Our document result:
     *                 <record>
     *                 <recordData>
     *                 the metadata
     *                 </recordData>
     *                 <extraRecordData>
     *                 Optionally <extraRecordData />
     *                 Optionally <Identifier />
     *                 </extraRecordData>
     *                 </record>
     * @param schemaID
     * @return
     * @throws ORG.oclc.os.SRW.SRWDiagnostic
     */
    @Override
    public Record transform(Record rec, String schemaID) throws SRWDiagnostic {
        String recStr = Utilities.hex07Encode(rec.getRecord());

        log.debug("transforming to " + schemaID);
        // They must have specified a transformer
        Transformer t = (Transformer) transformers.get(schemaID);
        if (t == null) {
            log.error("can't transform record in schema " + rec.getRecordSchemaID());
            log.error("record not available in schema " + schemaID);
            log.error("available schemas are:");
            Enumeration enumer = transformers.keys();
            while (enumer.hasMoreElements()) {
                log.error("    " + enumer.nextElement());
            }
            throw new SRWDiagnostic(SRWDiagnostic.RecordNotAvailableInThisSchema, schemaID);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamSource fromRec = new StreamSource(new StringReader(recStr));
        try {
            t.transform(fromRec, new StreamResult(baos));
        } catch (TransformerException e) {
            log.error(e, e);
            throw new SRWDiagnostic(SRWDiagnostic.RecordNotAvailableInThisSchema, schemaID);
        }

        org.w3c.dom.Document document;
        try {
            document = XMLUtils.newDocument(new ByteArrayInputStream(baos.toByteArray()));
        } catch (Exception e) {
            throw new SRWDiagnostic(1, e.getMessage());
        }


        // Collect the recordData, extraRecordData element and Identifier.
        // Bit confusing: extraRecordData is a parameter name... extraRecordData is an SRU element.

        String recordData = GetNodeValue(document, "recordData");
        String extraRecordData = GetNodeValue(document, "extraRecordData");
        String identifier = GetNodeValue(document, "Identifier");

        if (extraRecordData != null)
            rec.setExtraRecordInfo(extraRecordData); // Not record. This method returns a Record, but extraRecordData set in record is not used. So use rec.
        if (identifier != null)
            rec.setIdentifier(identifier); // Does not seem to work.

        return new Record(recordData, schemaID);
    }

    /**
     * A utility method
     *
     * @param document A XML document
     * @param TagName  The Elements to look for in the document
     * @return A string with the XML of the Elementr found
     * @throws ORG.oclc.os.SRW.SRWDiagnostic
     */
    private String GetNodeValue(org.w3c.dom.Document document, String TagName) {
        NodeList list = document.getElementsByTagName(TagName);

        int length = list.getLength();

        if (length == 0)
            return null;

        StringBuilder sb = new StringBuilder();

        //for ( int i = length - 1; i != -1 ; i-- )
        for (int i = 0; i < length; i++) {
            Element element = (Element) list.item(i);
            String xml = XMLUtils.getInnerXMLString(element);
            if (xml != null)
                sb.append(Utilities.hex07Encode(xml));
            //    element.getParentNode().removeChild(element);
        }

        return sb.toString();
    }

    @Override
    public boolean hasaConfigurationFile() {
        log.info("We use the solrconfig and explain documents to set our properties.");
        return false;
    }

    /**
     * Facets are nice here. Remove this addition should OCLC implement SRW version 2.0 with the facetedResults element ?
     * For now we use fields as defined in the solrconfig SRW handler to add to the extra response data.
     * The facets first request may take a while, depending on the size of the index. Better to therefore to add these as autowarming parameters in solr.QuerySenderListener
     *
     * @param result  The resultset for which we have to calculate the facets
     * @param request
     * @return
     */
    @Override
    public String getExtraResponseData(QueryResult result, SearchRetrieveRequestType request) {
        if (facets == null) // No facets were defined in the solrconfig.
            return null;

        SolrQueryResult r = (SolrQueryResult) result;

        if (r.getNumberOfRecords() == 0) // The result set is empty
            return null;

        NamedList list = new NamedList();
        for (int i = 0; i < facets.size(); i++) {
            String key = facets.getName(i);
            Object o = facets.getVal(i);
            if (o instanceof String) {
                String value = (String) o;
                if (key.equals("facet.field")) {
                    List solr_facet_field = facets.getAll(value);
                    if (solr_facet_field.size() == 0)
                        continue;

                    ArrayList solr_facet_fields = (ArrayList) solr_facet_field.get(0);
                    for (Object solr_facet_field1 : solr_facet_fields) {
                        list.add("facet.field", solr_facet_field1);
                    }

                } else
                    list.add(key, value);
            }
        }

        NamedList facets_result = r.getSimpleFacets(SolrParams.toSolrParams(list));
        if (facets_result == null)
            return null;

        try {
            return getFacetData(request, r, facets_result);
        } catch (XMLStreamException e) {
            log.warn(e);
        } catch (UnsupportedEncodingException e) {
            log.warn(e);
        }

        return null;
    }


    /**
     * Create the facet list as described in the loc's SRW 2.0 definition:
     * facetedResults
     * dataSource
     * facets
     * facet
     * displayLabel
     * description
     * index
     * relation
     * terms
     * term
     * actualTerm
     * query
     * requestUrl
     * count
     *
     * @param request       The SRW request that has some parameters we want to use: version and recordSchema. Needed for a follow up query.
     * @param r             The SolrQueryResult resultset. It has the original query. Needed for a follow up query.
     * @param facets_result
     * @return The facets
     * @throws javax.xml.stream.XMLStreamException
     * @throws UnsupportedEncodingException
     */
    private String getFacetData(SearchRetrieveRequestType request, SolrQueryResult r, NamedList facets_result) throws XMLStreamException, UnsupportedEncodingException {
        /*
   facetedResults
       dataSource
           facets
               facet
               displayLabel
               description
               index
               relation
               terms
                   term
                       actualTerm
                       query
                       requestUrl
                       count

        */

        String host = dbProperties.getProperty("serverInfo.host");
        if (host == null)
            host = "/"; // assumption

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(baos, encoding);
        //writer.writeStartDocument();
        writer.writeStartElement("facetedResults");
        writer.writeAttribute("xmlns", "info:srw/xmlns/1/sru-facetedResults");
        writer.writeStartElement("dataSource");
        writer.writeStartElement("facets");

        // So, now we have a list of terms that belong to a cql facet.
        List facet_cql_fields = facets.getAll("facet.field");

        for (Object facet_cql_field : facet_cql_fields) {
            String cql_index_field = (String) facet_cql_field;
            Object o = facets.get(cql_index_field);
            List facet_lucene_fields;
            if (o == null)
                continue;

            facet_lucene_fields = (List) o;

            Map<String, Integer> list_solr_fields_freq = new HashMap();
            for (Object facet_lucene_field : facet_lucene_fields) {
                String index = (String) facet_lucene_field;
                NamedList terms = (NamedList) facets_result.get(index);
                for (int i = 0; i < terms.size(); i++) {
                    String term = terms.getName(i);
                    int freq = (Integer) terms.getVal(i);

                    if (list_solr_fields_freq.containsKey(term))
                        freq += list_solr_fields_freq.get(term);

                    list_solr_fields_freq.put(term, freq);
                }
            }

            if (list_solr_fields_freq.isEmpty())
                continue;

            //Object facetsort = dbProperties.getProperty("facet.sort") ;
            // if ( facetsort != null && String.valueOf(facetsort).equalsIgnoreCase("index"))
//            Collections.sort(list_solr_fields_freq, String.CASE_INSENSITIVE_ORDER);

            // Get the official titel:
            ArrayList list_title = explainMap.get("index.title." + cql_index_field);
            String title = (list_title == null)
                    ? cql_index_field
                    : (String) list_title.get(0);

            // Now produce the results in a facet:
            writer.writeStartElement("facet");
            //sb.append("<facet>");

            writer.writeStartElement("displayLabel");
            writer.writeCharacters(title);
            writer.writeEndElement();

            writer.writeStartElement("index");
            writer.writeCharacters(cql_index_field);
            writer.writeEndElement();

            writer.writeStartElement("relation");
            writer.writeCharacters("=");
            writer.writeEndElement();

            writer.writeStartElement("terms");
            for (Object o1 : list_solr_fields_freq.keySet()) {
                String term = (String) o1;
                int freq = list_solr_fields_freq.get(term);

                String query = cql_index_field + "=" + term;
                String requestUrl = host + "?query=" + r.getOriginalQuery() + " and " + cql_index_field + "=" + term + "&recordSchema=" + request.getRecordSchema() + "&version=" + request.getVersion() + "&startRecord=1&operation=searchRetrieve";

                writer.writeStartElement("term");

                writer.writeStartElement("actualTerm");
                writer.writeCharacters(term);
                writer.writeEndElement();

                writer.writeStartElement("query");
                writer.writeCharacters(query);
                writer.writeEndElement();

                writer.writeStartElement("requestUrl");
                writer.writeCharacters(requestUrl);
                writer.writeEndElement();

                writer.writeStartElement("count");
                writer.writeCharacters(String.valueOf(freq));
                writer.writeEndElement();

                writer.writeEndElement();
            }
            writer.writeEndElement(); // terms
            writer.writeEndElement(); // facets
        }


        writer.writeEndDocument();

        return baos.toString(encoding);
    }

    /**
     * Get the IndexInfo as mentioned in the explain document
     *
     * @return the IndexInfo element
     */
    @Override
    public String getIndexInfo() {

        return getInfo("indexInfo");
    }

    /**
     * Get the SchemaInfo as mentioned in the explain document
     *
     * @return the SchemaInfo element
     */
    @Override
    public String getSchemaInfo() {
        String info = getInfo("schemaInfo");
        if (info == null)
            return super.getSchemaInfo();

        return info;
    }

    /**
     * Get the DatabaseInfo as mentioned in the explain document
     *
     * @return the DatabaseInfo element
     */
    @Override
    public String getDatabaseInfo() {
        String info = getInfo("databaseInfo");
        if (info == null)
            return super.getDatabaseInfo();

        return info;
    }

    /**
     * Get the MetaInfo as mentioned in the explain document
     *
     * @return the MetaInfo element
     */
    @Override
    public String getMetaInfo() {
        String info = getInfo("metaInfo");
        if (info == null)
            return super.getMetaInfo();

        return info;
    }

    /**
     * Get the ConfigInfo as mentioned in the explain document
     *
     * @return the ConfigInfo element
     */
    @Override
    public String getConfigInfo() {
        String info = getInfo("configInfo");
        if (info == null)
            return super.getConfigInfo();

        return info;
    }

    /**
     * Get the element from the explainMap and return the value
     *
     * @return the explain element
     */
    private String getInfo(String key) {
        ArrayList list = explainMap.get("explain." + key);
        if (list == null)
            return null;

        StringWriter writer = new StringWriter();

        writer.write("<");
        writer.write(key);
        writer.write(">");

        for (Object aList : list) {
            writer.write((String) aList);
        }

        writer.write("</");
        writer.write(key);
        writer.write(">");

        return writer.toString();
    }

    @Override
    /**
     * TermList is called to start a scan operation
     *
     * #return the terms
     *
     */
    public TermList getTermList(CQLTermNode term, int position, int maxTerms, ScanRequestType request) {
        // No not allow maxTerms to exceed maximumRecords
        Object o = dbProperties.getProperty("maximumTerms");
        int maximumTerms = (o == null)
                ? maximumRecords
                : new Integer(String.valueOf(o));

        if (maximumTerms < maxTerms) {
            log.warn("maxTerms=" + maxTerms + " in scan request is larger than the allowed setting: " + maximumTerms);
            maxTerms = maximumTerms;
        }

        return new SolrTermList(term, position, maxTerms, explainMap);
    }

    @Override
    public QueryResult getQueryResult(String query, SearchRetrieveRequestType request) throws InstantiationException {

        // Get the sru query string and parse it into a CQL structure
        final CQLParser cqlParser = new CQLParser();
        CQLNode XQuery;
        try {
            XQuery = cqlParser.parse(query);
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }

        // Convert that CQL structure into into a Lucene query format
        String q;
        try {
            q = makeQuery(XQuery);
            log.info("cql2lucene: " + q);
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }

        // And then put it straight into the Solr Parser.
        MessageContext msgContext = MessageContext.getCurrentContext();
        SolrQueryRequest req = (SolrQueryRequest) msgContext.getProperty("SolrQueryRequest");
        SolrQueryParser parser = new SolrQueryParser(req.getSchema(), null);
        Query solr_query;
        try {
            solr_query = parser.parse(q);
        } catch (ParseException e) {
            throw new InstantiationException(e.getMessage());
        }

        // Now sort...
        // http://www.loc.gov/standards/sru/sru1-1archive/search-retrieve-operation.html#sort
        Sort sort = null;
        String sortKeys = request.getSortKeys(); // See if there is a sortkeys argument
        if (sortKeys != null) {
            StringTokenizer st_keys = new StringTokenizer(sortKeys, " "); // Sort fields are separated by a space
            ArrayList<SortField> list = new ArrayList();

            while (st_keys.hasMoreTokens()) {
                String key = st_keys.nextToken();
                StringTokenizer st_params = new StringTokenizer(key, ","); // Sort parameters are separated by a comma

                String fieldname = null;
                boolean top = true; // default asc
                while (st_params.hasMoreTokens()) {
                    String param = st_params.nextToken();

                    if (fieldname == null) // First parameter is always the field to sort.
                        fieldname = param;
                    else if (param.equals("1")) {
                        top = true;
                        break;
                    } else if (param.equals("0")) {
                        top = false;
                        break;
                    }
                }

                // Now we have a fieldname an a sort order.
                // But we still need a map to the actual index
                ArrayList fields = explainMap.get(IndexOptions.sort + "." + fieldname);
                if (fields == null) // No such index to sort on...
                {
                    // just ignore it... probably we should throw an SRW Exception.
                } else
                    for (Object field1 : fields) {
                        SchemaField field = req.getSchema().getField((String) field1);
                        SortField sortField = field.getSortField(top);
                        if (sortField != null)
                            list.add(sortField);
                    }
            }

            if (list.size() != 0) {
                SortField[] sfields = new SortField[list.size()];
                sort = new Sort(list.toArray(sfields));
            }
        }

        // Make the query
        Query filter = null; // not implemented
        DocList docList;
        try {
            docList = req.getSearcher().getDocList(solr_query, filter, sort, 0, Integer.MAX_VALUE);
        } catch (IOException e) {
            throw new InstantiationException(e.getMessage());
        }

        return new SolrQueryResult(request.getQuery(), req, docList, (String) msgContext.getProperty("sru"));
    }

    // Use the xslt stylesheet to convert the CQL query into a Lucene query.

    private String makeQuery(CQLNode xQuery) throws TransformerException, IOException {

        final Transformer t = (Transformer) transformers.get("cql-2-lucene");
        final ByteArrayInputStream bais = mergeCQLandIndices(xQuery);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        t.transform(new StreamSource(bais), new StreamResult(baos));

        return baos.toString(encoding).replace("\r", "").replace("\n", "");
    }

    private ByteArrayInputStream mergeCQLandIndices(CQLNode xQuery) throws IOException {
        // Now create a nice document which we can sent to an XSLT transformer.

        final StringWriter writer = new StringWriter();
        writer.write("<cql2lucene>");
        writer.write("<indices>");

        for (String key : explainMap.keySet()) {
            if (key.startsWith(IndexOptions.search + ".") || key.startsWith(IndexOptions.search_exact + ".") || key.startsWith(IndexOptions.search_range + ".")) {
                ArrayList list = explainMap.get(key);
                for (Object aList : list) {
                    writer.write("<field cql=");
                    writer.write("\"");
                    writer.write(key);
                    writer.write("\">");
                    writer.write((String) aList);
                    writer.write("</field>");
                }
            }
        }


        writer.write("</indices>");

        writer.write(xQuery.toXCQL(0));

        writer.write("</cql2lucene>");
        writer.close();

        return new ByteArrayInputStream(writer.toString().getBytes(encoding));
    }

    @Override
    public boolean supportsSort() {

        // Well... on the one hand: no, sorry. Let's wait and implement CQL sorting arguments.
        // But the sortkey in version 1.1 can to some extend be used rather well. That is what we do.

        return true;
    }

    public void setExplainMap(Map explainMap) {
        this.explainMap = explainMap;
    }

    public void setFacets(NamedList facets) {
        this.facets = facets;
    }

    public void setAxisServer(AxisServer server) {
        axisServer = server;
    }

    public AxisServer getAxisServer() {
        return axisServer;
    }

    public MessageContext setResponseMessage(Message reqMsg, SolrQueryRequest req, Services targetService, RequestTypes requestType, Transport transport) throws IOException {
        MessageContext msgContext = new MessageContext(axisServer);

        msgContext.setTargetService(targetService.toString());
        msgContext.setProperty("db", this);
        msgContext.setProperty("dbname", this.dbname);
        msgContext.setProperty("resultSetIdleTime", 300);
        msgContext.setProperty("SolrQueryRequest", req);
        msgContext.setProperty("RequestTypes", requestType);
        msgContext.setProperty("sru", transport.name());

        String resource;
        switch (requestType) {
            case searchRetrieveRequest:
                resource = this.searchStyleSheet;
                break;

            case scanRequest:
                resource = this.scanStyleSheet;
                break;

            default:
            case explainRequest:
                resource = this.explainStyleSheet;
                break;
        }

        msgContext.setProperty("resource", resource);

        msgContext.setRequestMessage(reqMsg);

        axisServer.invoke(msgContext);

        return msgContext;
    }

    public MessageContext setResponseMessage(Services targetService) throws IOException {
        MessageContext msgContext = new MessageContext(axisServer);
        msgContext.setTargetService(targetService.toString());

        axisServer.generateWSDL(msgContext); // produce an WSDL document
        Document doc = (Document) msgContext.getProperty("WSDL"); // Get the document.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, encoding);
        XMLUtils.DocumentToWriter(doc, writer); // Write the WSDL document into a stream
        writer.close();

        ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
        Message respMsg = new Message(is); // And pass it on to the message
        is.close();

        msgContext.setResponseMessage(respMsg);

        return msgContext;
    }

    public Transformer getTransformers(String schemaID) {
        return (Transformer) transformers.get(schemaID);
    }


    public void init(String dbname, String srwHome,
                     String dbHome, String dbPropertiesFileName,
                     Properties dbProperties,
                     HttpServletRequest request) throws Exception {
        log.info("request=" + request);
        init(dbname, srwHome, dbHome, dbPropertiesFileName, dbProperties);
    }

    @Override
    public void init(String dbname, String srwHome, String dbHome, String dbPropertiesFileName, Properties dbProperties) throws Exception {

        if (dbProperties == null)
            dbProperties = new Properties(srwProperties);

        if (dbProperties.size() == 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            srwProperties.store(baos, null);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            dbProperties.load(bais);
        }

        // Add custom system transformers:
        // It is kind of a hack to put these along the xmlSchema, but it does no harm.
        // Still: ToDo: implement a seperate HashTable for system transformers.
        String xmlSchemas = dbProperties.getProperty("xmlSchemas") + " " + dbProperties.getProperty("system.xmlSchemas");
        dbProperties.setProperty("xmlSchemas", xmlSchemas);

        super.initDB(dbname, srwHome, dbHome, dbPropertiesFileName, dbProperties);

        String temp = dbProperties.getProperty("maximumRecords");
        if (temp == null)
            temp = dbProperties.getProperty("configInfo.maximumRecords");
        if (temp != null)
            setMaximumRecords(Integer.parseInt(temp));
    }

    public enum IndexOptions {
        search,
        search_exact,
        search_range,
        scan,
        scan_exact,
        sort
    }

    public enum RequestTypes {
        explainRequest, scanRequest, searchRetrieveRequest
    }

    public enum Services {
        SRW, ExplainSOAP
    }

    public enum Transport {
        SRU, SRW, JSON
    }  // SRU is a call with the URL. SRW is a SOAP call.

    final private static String encoding = "UTF-8";
    private static AxisServer axisServer;
    private Map<String, ArrayList> explainMap; // typically our normalized Lucene fields for searching
    private NamedList facets;

    private final Log log = LogFactory.getLog(this.getClass());
}
