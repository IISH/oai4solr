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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrQueryParser;
import org.openarchives.oai2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * OAIRequestHandler
 * <p/>
 * Parse the request as an OAI request; and perform an action.
 * <p/>
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
public class OAIRequestHandler extends RequestHandlerBase {

    private final Log log = LogFactory.getLog(this.getClass());



    @Override
    /**
     * Here we instantiate the oai request and validate the parameters.
     *
     */
    public void handleRequestBody(SolrQueryRequest request, SolrQueryResponse response) throws Exception {

        final NamedList<Object> list = request.getParams().toNamedList();
        list.add("wt", Utils.getParam("wt")); // The request writer
        request.setParams(SolrParams.toSolrParams(list));

        VerbType verb;
        try {
            verb = VerbType.fromValue(request.getParams().get("verb"));
        } catch (Exception e) {
            Utils.error(response, OAIPMHerrorcodeType.BAD_VERB);
            return;
        }
        ResumptionToken oaiRequest = getRequest(request, verb);
        if (oaiRequest == null) {
            Utils.error(response, OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
            return;
        }

        OAIPMHtype oai = initOaiDocument(verb);
        oai.setResponseDate(Utils.getGregorianDate(new Date()));
        oai.setRequest(oaiRequest);
        switch (verb) {
            case GET_RECORD:
            case LIST_IDENTIFIERS:
            case LIST_RECORDS:
                buildQuery(request, response, oaiRequest, verb, oai);
        }
        response.add("oai", oai);
    }

    private ResumptionToken getRequest(SolrQueryRequest request, VerbType verb) {

        final SolrParams params = request.getParams();
        final NamedList<Object> list = params.toNamedList();
        ResumptionToken oaiRequest;
        try {
            oaiRequest = Utils.parseResumptionToken(params.get("resumptionToken"));
        } catch (Exception e) {
            request.setParams(SolrParams.toSolrParams(list));
            return null;
        }
        if (oaiRequest == null) {
            oaiRequest = new ResumptionToken();
            oaiRequest.setVerb(verb);
            oaiRequest.setIdentifier(params.get("identifier"));
            oaiRequest.setMetadataPrefix(params.get("metadataPrefix"));
            oaiRequest.setFrom(params.get("from"));
            oaiRequest.setUntil(params.get("until"));
            oaiRequest.setSet(params.get("set"));
            oaiRequest.setResumptionToken(params.get("resumptionToken"));
            oaiRequest.setValue((String) Utils.getParam("proxyurl"));
        } else {
            oaiRequest.setVerb(verb);
            if (oaiRequest.getFrom() != null)
                list.add("from", oaiRequest.getFrom());
            if (oaiRequest.getUntil() != null)
                list.add("until", oaiRequest.getUntil());
            if (oaiRequest.getSet() != null)
                list.add("set", oaiRequest.getSet());
            if (oaiRequest.getMetadataPrefix() != null)
                list.add("metadataPrefix", oaiRequest.getMetadataPrefix());
        }
        request.setParams(SolrParams.toSolrParams(list));
        return oaiRequest;
    }

    private void buildQuery(SolrQueryRequest request, SolrQueryResponse response, ResumptionToken oaiRequest, VerbType verb, OAIPMHtype oai) throws java.text.ParseException, UnsupportedEncodingException {

        List<String> q = new ArrayList<String>();
        Object maxrecords = Utils.getParam("maxrecords_" + oaiRequest.getMetadataPrefix());
        int len = (maxrecords == null)
                ? (Integer) Utils.getParam("maxrecords_default")
                : Integer.parseInt(String.valueOf(maxrecords));

        DocList docList = null;
        switch (verb) {
            case LIST_IDENTIFIERS:
            case LIST_RECORDS:
                if (!Utils.isValidMetadataPrefix(response, oaiRequest, (OAIPMHtype) Utils.getParam(VerbType.LIST_METADATA_FORMATS.value()))) {
                    return;
                }
                try {
                    addTimestampToQuery(oaiRequest.getFrom(), oaiRequest.getUntil(), q);
                } catch (Exception e) {
                    Utils.error(response, "Bad date format", oai);
                    return;
                }
                if (Utils.isValidSet(oaiRequest.getSet(), response, oaiRequest, oai))
                    addSetToQuery(oaiRequest.getSet(), q);
                else
                    return;

                int cursor = oaiRequest.getCursor();
                int nextCursor = cursor + len;
                docList = runQuery(request, response, oaiRequest, verb, oai, q, cursor, len);
                final ResumptionTokenType rt = (docList.matches() > nextCursor)
                        ? Utils.setResumptionToken(oaiRequest, cursor, nextCursor, docList.matches())
                        : null;
                if (verb == VerbType.LIST_RECORDS)
                    oai.setListRecords(listRecords(rt));
                else
                    oai.setListIdentifiers(listIdentifiers(rt));
                break;

            case GET_RECORD:
                if (!Utils.isValidIdentifier(response, oaiRequest)) {
                    return;
                }
                if (!Utils.isValidMetadataPrefix(response, oaiRequest, (OAIPMHtype) Utils.getParam(VerbType.LIST_METADATA_FORMATS.value()))) {
                    return;
                }
                addToQuery(String.format("%s:\"%s\"", Utils.getParam("field_index_identifier"), Utils.stripOaiPrefix( oaiRequest.getIdentifier() )), q);
                docList = runQuery(request, response, oaiRequest, verb, oai, q, 0, 1);
                oai.setGetRecord(getRecord(response, docList));
                break;
        }

        response.add("docList", docList);
    }

    private void addToQuery(String query, List<String> q) {
        q.add(query);
    }

    /**
     * Add the datestamp and set to the query.
     * The datestamp range is used to define a default query, in case set, from and until parameters were absent.
     *
     * @param q
     */
    private void addTimestampToQuery(String from, String until, List<String> q) throws java.text.ParseException {

        from = Utils.parseDate(from);
        until = Utils.parseDate(until);
        q.add(String.format("%s:[%s TO %s]", Utils.getParam("field_index_datestamp"), from, until));
    }

    private void addSetToQuery(String setParam, List<String> q) {

        if (setParam != null)
            addToQuery(String.format("%s:\"%s\"", Utils.getParam("field_index_set"), setParam), q);
    }

    /**
     * Return an empty OAI main document.
     * If the request was for ListMetadataFormats, ListSets or Identify, we create a cache for this and add it to the
     * response.
     *
     * @param verb
     * @return
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    private OAIPMHtype initOaiDocument(VerbType verb) throws FileNotFoundException, JAXBException {

        final String v = verb.value();
        Object oaipmHtype = Utils.getParam(verb.value());
        if (oaipmHtype != null)
            return (OAIPMHtype) oaipmHtype;

        final String path = (String) Utils.getParam(v.toLowerCase());
        final File f = new File(Utils.getParam("oai_home") + File.separator + path);
        if (!f.exists())
            return new OAIPMHtype();
        final FileInputStream fis = new FileInputStream(f);
        final Source source = new StreamSource(fis);
        final Unmarshaller marshaller = (Unmarshaller) Utils.getParam("unmarshaller");
        JAXBElement<OAIPMHtype> oai = (JAXBElement<OAIPMHtype>) marshaller.unmarshal(source);
        oaipmHtype = oai.getValue();
        Utils.setParam(v, oaipmHtype);
        return (OAIPMHtype) oaipmHtype;
    }

    private DocList runQuery(SolrQueryRequest request, SolrQueryResponse response, RequestType oaiRequest, VerbType verb, OAIPMHtype oai, List<String> q, int cursor, int len) {
        DocList docList;
        try {
            docList = runQuery(request, q, cursor, len);
        } catch (Exception e) {
            Utils.error(response, e.getMessage(), oai);
            return null;
        }

        return docList;
    }

    private DocList runQuery(SolrQueryRequest request, List<String> q, int cursor, int len) throws ParseException, IOException {

        Query filter = null; // not implemented
        final SortField sortField = new SortField((String) Utils.getParam("field_sort_datestamp"), SortField.LONG, false);
        final Sort sort = new Sort(sortField);
        final SolrQueryParser parser = new SolrQueryParser(request.getSchema(), null);
        String[] queryParts = q.toArray(new String[0]);
        final Query query = parser.parse(Utils.join(queryParts, " AND "));
        return request.getSearcher().getDocList(query, filter, sort, cursor, len);
    }

    private ListIdentifiersType listIdentifiers(ResumptionTokenType token) {
        final ListIdentifiersType records = new ListIdentifiersType();
        records.setResumptionToken(token);
        return records;
    }

    private ListRecordsType listRecords(ResumptionTokenType token) {
        final ListRecordsType records = new ListRecordsType();
        records.setResumptionToken(token);
        return records;
    }

    private GetRecordType getRecord(SolrQueryResponse response, DocList docList) {
        if (!Utils.isAvailableIdentifier(response, docList.size()))
            return null;
        if (!Utils.hasMatchingRecords(response, docList.size()))
            return null;
        return new GetRecordType();
    }

    @Override
    public void init(NamedList args) {
        super.init(args);
        Utils.setParam(args, "wt", "oai");
        Utils.setParam(args, "proxyurl", "");
        Utils.setParam(args, "maxrecords", 200);
        Utils.setParam(args, "resumptionTokenExpirationInSeconds", 86400);
        Utils.setParam(args, "separator", ",");
        Utils.setParam(args, "field_index_identifier", "id");
        Utils.setParam(args, "prefix", "");
        Utils.setParam(args, "field_index_datestamp", "datestamp");
        Utils.setParam(args, "field_sort_datestamp", "datestamp");
        Utils.setParam(args, "field_index_set", "set");
        Utils.setParam(args, "identify", "Identify.xml");
        Utils.setParam(args, "listsets", "ListSets.xml");
        Utils.setParam(args, "listmetadataformats", "ListMetadataFormats.xml");
        final List maxrecords = args.getAll("maxrecords");
        if (maxrecords == null)
            Utils.setParam(args, "maxrecords_default", 200);
        else {
            SolrParams p = SolrParams.toSolrParams((NamedList) maxrecords.get(0));
            final Iterator<String> iterator = p.getParameterNamesIterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Utils.setParam(args, "maxrecords_" + key, p.getInt(key));
            }
        }

        // Find the application's solr home folder
        String solr_home = SolrResourceLoader.locateSolrHome();
        if (solr_home == null)
            solr_home = System.getProperty("solr.solr.home");
        String oai_home = (String) args.get("oai_home");
        oai_home = (oai_home == null)
                ? solr_home + File.separatorChar + "oai"
                : solr_home + oai_home;
        args.remove("oai_home");
        Utils.setParam(args, "oai_home", oai_home);

        try {
            final JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            Utils.setParam("marshaller", jc.createMarshaller());
            Utils.setParam("unmarshaller", jc.createUnmarshaller());
        } catch (JAXBException e) {
            log.error(e);
        }

        try {
            initOaiDocument(VerbType.IDENTIFY);
            initOaiDocument(VerbType.LIST_SETS);
            initOaiDocument(VerbType.LIST_METADATA_FORMATS);
        } catch (FileNotFoundException e) {
            log.error(e);
        } catch (JAXBException e) {
            log.error(e);
        }
        addStylesheets(oai_home, (OAIPMHtype) Utils.getParam("ListMetadataFormats"));
    }

    private void addStylesheets(String oai_home, OAIPMHtype oai) {

        final List<MetadataFormatType> schemas = oai.getListMetadataFormats().getMetadataFormat();
        for (MetadataFormatType schema : schemas) {
            final String metadataPrefix = schema.getMetadataPrefix();
            addStylesheet(oai_home, metadataPrefix);
        }
    }

    private void addStylesheet(String oai_home, String metadataPrefix) {

        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final String filename = oai_home + File.separatorChar + metadataPrefix + ".xsl";
            final File file = new File(filename);
            if (!file.exists()) {
                log.warn("Cannot find stylesheet " + filename + " for mapping solr index fields to the " + metadataPrefix + " metadataPrefix.");
                return;
            }
            final Source xslSource = new StreamSource(file);
            xslSource.setSystemId(file.toURI().toURL().toString());
            final Transformer transformer = tf.newTransformer(xslSource);
            Utils.setParam(metadataPrefix, transformer);
            transformer.setParameter("prefix", Utils.getParam("prefix"));
        } catch (TransformerConfigurationException e) {
            log.error(e);
        } catch (MalformedURLException e) {
            log.error(e);
        }
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getSourceId() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}