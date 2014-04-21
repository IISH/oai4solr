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

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by IntelliJ IDEA.
 * User: Lucien van Wouw
 * Date: 9-jul-2009
 * Time: 14:11:16
 */

// Process the request's SRU parameters and construct a valid Lucene query.
public class SRURequestHandler extends SRURequestHandlerSOAP implements SolrCoreAware {
    final private static String version = "1.1"; // Our only hardcoded variable, because our OCLC database only supports this version.
    private String wt;

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {

        // Add the response writer parameter to the Solr parameters.
        SolrParams params = req.getParams();
        NamedList<Object> list = params.toNamedList();
        if (params.get("wt") == null)
            list.add("wt", wt);
        if (params.get("version") == null)
            list.add("version", version);
        req.setParams(SolrParams.toSolrParams(list));
        if (db == null)
            lazyInit(req);
        super.handleRequestBody(req, rsp);
    }

    @Override
    public void init(NamedList args) {

        if (this.wt != null)
            return;

        String wt = (String) args.get("wt");
        this.wt = (wt == null)
                ? "srw" // Assumption
                : wt;

        this.initArgs = args;
    }

    private synchronized void lazyInit(SolrQueryRequest req) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        if (db == null) {
            Config config = new Config();
            db = config.init(req.getSchema(), SolrResourceLoader.locateSolrHome(), initArgs);
            xml2json_callback_key = config.getXml2json_callback_key();
        }
    }

    @Override
    public String getVersion() {
        return "$Revision: 1 $";
    }

    @Override
    public String getDescription() {
        return "Solr SRW handler";
    }

    @Override
    public String getSourceId() {
        return "$Id: SRURequestHandler.java 1 2009-07-09 $";
    }

    @Override
    public String getSource() {
        return "$URL: http://api.iisg.nl/srw $";
    }

    @Override
    public URL[] getDocs() {
        try {
            return new URL[]{new URL("http://api.iisg.nl/")};
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public void inform(SolrCore core) {
        core.getDataDir();
    }
}