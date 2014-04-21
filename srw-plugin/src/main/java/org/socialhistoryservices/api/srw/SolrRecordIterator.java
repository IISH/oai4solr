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

import ORG.oclc.os.SRW.Record;
import ORG.oclc.os.SRW.RecordIterator;
import ORG.oclc.os.SRW.SRWDiagnostic;
import gov.loc.www.zing.srw.ExtraDataType;
import org.apache.lucene.document.Document;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 Iterator to loop through the result's subset.
 */
class SolrRecordIterator implements RecordIterator
{
    private final SolrQueryRequest req;
    private final String transport;
    private ArrayList<Integer> docList;
    private final String schemaId;
    private final ExtraDataType edt;
    private final Iterator iterator;

    public SolrRecordIterator(SolrQueryRequest req, String transport, ArrayList<Integer> docList, String schemaId, ExtraDataType edt)
    {
        this.req = req;
        this.transport = transport ;
        this.docList = docList;
        this.iterator = docList.iterator();
        this.schemaId = schemaId ;
        this.edt = edt;
    }

    @Override
    public void close()
    {
        docList.clear();
    }

    @Override
    public Record nextRecord() throws SRWDiagnostic
    {
        int docId = (Integer)next();
        Document doc = null;
        try {
            doc = req.getSearcher().doc(docId);
        } catch (IOException e) {
            // Impossible at this point
        }

        // Add the data to the response.
        SolrQueryResponse rsp = new SolrQueryResponse();
        rsp.add("transport", transport); // additional information that can be used for the xslt
        rsp.add("result", doc);

        // Include the Solr parameters, if any;
        SolrParams params = req.getParams();
        if ( params != null )
            // We add the request parameters so the xslt stylesheets can do something with them.
            // This does not work for parameters in the query string with repeatable key names: a=a&a=b&a=c
            rsp.add("solrparams", params.toNamedList());

        //rsp.add("SearchRetrieveRequest", solrQueryResult.xsltSearchRetrieveRequest);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer ;
        try {
            writer = new OutputStreamWriter(baos, "UTF-8");
            XMLWriter.writeResponse(writer, req, rsp);
            writer.close();
        } catch (IOException e) {
            throw new SRWDiagnostic(1, e.getMessage());
        }

        Record record;
        try {
            record = new Record(baos.toString("UTF-8"), schemaId );
        } catch (UnsupportedEncodingException e) {
            throw new SRWDiagnostic(1, e.getMessage());
        }

        return record;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Object next() {
        return iterator.next();
    }

    @Override
    public void remove()
    {
    }
}
