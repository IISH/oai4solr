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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private final Log log = LogFactory.getLog(this.getClass());

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

    @Override
    public String getDescription() {
        return "A SRU\\SRW request handler";
    }

    @Override
    public String getSourceId() {
        return null;
    }

    @Override
    public String getSource() {
        return "$URL: https://github.com/IISH/oai4solr $";
    }

    @Override
    public String getVersion() {
        return "$3.x-1.0 $";
    }

    @Override
    public void inform(SolrCore core) {
        if (db == null) {
            try {
                db = Config.init(core.getSchema(), SolrResourceLoader.locateSolrHome(), initArgs);
            } catch (SAXException e) {
                log.error(e);
            } catch (ParserConfigurationException e) {
                log.error(e);
            } catch (XPathExpressionException e) {
                log.error(e);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}