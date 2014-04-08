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

import org.apache.commons.io.FilenameUtils;
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
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        list.add("wt", Utils.getParam("wt", "oai")); // The request writer
        request.setParams(SolrParams.toSolrParams(list));

        final OAIPMHtype oai = new OAIPMHtype();
        oai.setResponseDate(Utils.getGregorianDate(new Date()));
        response.add("oai", oai);


        VerbType verb = null;
        try {
            verb = VerbType.fromValue(request.getParams().get("verb"));
        } catch (Exception e) {
            Utils.error(response, String.format("Bad verb. Verb '%s' not implemented.", request.getParams().get("verb")),
                    OAIPMHerrorcodeType.BAD_VERB);
            return;
        }

        ResumptionToken oaiRequest = getRequest(request, verb);
        oai.setRequest(oaiRequest);
        if (!oaiRequest.isGood_resumptionToken()) {
            Utils.error(response, OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
            return;
        }

        switch (verb) {
            case IDENTIFY:
            case LIST_SETS:
            case LIST_METADATA_FORMATS:
                response.getValues().remove("oai");
                response.add("oai", Utils.loadStaticVerb(verb));
                break;
            case GET_RECORD:
            case LIST_IDENTIFIERS:
            case LIST_RECORDS:
                buildQuery(request, response, oaiRequest, verb, oai);
                break;
        }
    }

    private ResumptionToken getRequest(SolrQueryRequest request, VerbType verb) {

        final SolrParams params = request.getParams();
        final NamedList<Object> list = params.toNamedList();
        boolean token_good = true;
        ResumptionToken oaiRequest = null;
        try {
            oaiRequest = ResumptionToken.decodeResumptionToken(params.get("resumptionToken"));
        } catch (Exception e) {
            token_good = false;
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
            oaiRequest.setResumptiontokenHealth(token_good);
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

    private void buildQuery(SolrQueryRequest request, SolrQueryResponse response, ResumptionToken oaiRequest, VerbType verb, OAIPMHtype oai) throws java.text.ParseException, IOException, ParseException {

        List<String> q = new ArrayList<String>();
        Object maxrecords = Utils.getParam("maxrecords_" + oaiRequest.getMetadataPrefix());
        int len = (maxrecords == null)
                ? (Integer) Utils.getParam("maxrecords_default")
                : Integer.parseInt(String.valueOf(maxrecords));

        DocList docList = null;
        switch (verb) {
            case LIST_IDENTIFIERS:
            case LIST_RECORDS:
                if (!Utils.isValidMetadataPrefix(response, oaiRequest)) {
                    return;
                }

                if (!Utils.isValidDatestamp(oaiRequest.getFrom(), "from", response))
                    return;
                String from = Utils.parseRange(oaiRequest.getFrom());

                if (!Utils.isValidDatestamp(oaiRequest.getUntil(), "until", response))
                    return;
                String until = Utils.parseRange(oaiRequest.getUntil());

                q.add(String.format("%s:[%s TO %s]", Utils.getParam("field_index_datestamp"), from, until));

                if (Utils.isValidSet(oaiRequest.getSet(), response))
                    addSetToQuery(oaiRequest.getSet(), q);
                else
                    return;

                int cursor = oaiRequest.getCursor();
                int nextCursor = cursor + len;
                docList = runQuery(request, q, cursor, len);

                if (!Utils.hasMatchingRecords(response, docList.size()))
                    return;

                final ResumptionTokenType rt = (docList.matches() > nextCursor)
                        ? ResumptionToken.encodeResumptionToken(oaiRequest, cursor, nextCursor, docList.matches(), (Integer) Utils.getParam("resumptionTokenExpirationInSeconds"))
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
                if (!Utils.isValidMetadataPrefix(response, oaiRequest)) {
                    return;
                }
                addToQuery(String.format("%s:\"%s\"", Utils.getParam("field_index_identifier"), Utils.stripOaiPrefix(oaiRequest.getIdentifier())), q);
                docList = runQuery(request, q, 0, 1);
                oai.setGetRecord(getRecord(response, docList));
                break;
        }

        response.add("docList", docList);
    }

    private void addToQuery(String query, List<String> q) {
        q.add(query);
    }

    private void addSetToQuery(String setParam, List<String> q) {

        if (setParam != null)
            addToQuery(String.format("%s:\"%s\"", Utils.getParam("field_index_set"), setParam), q);
    }

    private DocList runQuery(SolrQueryRequest request, List<String> q, int cursor, int len) throws ParseException, IOException {

        final SortField sortField = new SortField((String) Utils.getParam("field_sort_datestamp"), SortField.LONG, false);
        final Sort sort = new Sort(sortField);
        final SolrQueryParser parser = new SolrQueryParser(request.getSchema(), null);
        String[] queryParts = q.toArray(new String[0]);
        final Query query = parser.parse(Utils.join(queryParts, " AND "));
        Query filter = null; // not implemented
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
        File file = new File(oai_home);
        if (!file.exists()) {
            log.fatal("Could not locate the oai_directory: " + oai_home + " Set the oai_home property in the solrconfig.xml's OAIRequestHandler section");
            return;
        }
        log.info("oai_home=" + oai_home);
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
            Utils.setParam(args, "ListSets", Utils.loadStaticVerb(VerbType.LIST_SETS));
        } catch (FileNotFoundException e) {
            log.warn(e);
        } catch (JAXBException e) {
            log.error(e);
        }


        addStylesheets(file);
    }

    private void addStylesheets(File oai_home) {

        for (File file : oai_home.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName().toLowerCase();
                return !name.equals("oai.xsl") && (name.endsWith(".xsl") || name.endsWith(".xslt"));
            }
        })) {

            final TransformerFactory tf = TransformerFactory.newInstance();
            try {
                final Source xslSource = new StreamSource(file);
                xslSource.setSystemId(file.toURI().toURL().toString());
                final Templates templates = tf.newTemplates(xslSource);
                String metadataPrefix = FilenameUtils.removeExtension(file.getName());
                Utils.setParam(metadataPrefix, templates);
            } catch (TransformerConfigurationException e) {
                log.error(e);
            } catch (MalformedURLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public String getDescription() {
        return "An OAI2 request handler";
    }

    @Override
    public String getSourceId() {
        return null;
    }

    @Override
    public String getSource() {
        return "https://github.com/IISH/oai4solr";
    }

    @Override
    public String getVersion() {
        return "3.6.2";
    }
}