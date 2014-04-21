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

import ORG.oclc.os.SRW.RecordIterator;
import gov.loc.www.zing.srw.ExtraDataType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Lucien van Wouw
 * Date: 15-dec-2009
 * Time: 20:57:19
 *
 * Store the query: results ( integers ) and prepare the iterators
 */
class SolrQueryResult extends ORG.oclc.os.SRW.QueryResult {

    //private final ArrayList<Integer> docIds;
    private final SolrQueryRequest req;
    private final String originalQuery ;
    private final String transport;
    private final DocList docList;

    public SolrQueryResult(String query, SolrQueryRequest req, DocList docList, String transport)
    {
        this.originalQuery = query ;
        this.req = req ;
        this.transport = transport ;
        this.docList = docList;
    }

    @Override
    public long getNumberOfRecords() {
        return docList.size();
    }

    public String getOriginalQuery()
    {
        return originalQuery;
    }

    /**
     * Creates an iterator for an array of Lucene DocIds in the specified range and from the starting point.
     * <p>
     * Solr has it's own iterator to loop through a DocList, yet we cannot use that one.
     * That is because the OCLC caches the resultset ( and the Solr iterator that goes with it ). Other
     * request must have their own instance of an iterator and this avoid concurrency problems.
     * <p>
     * The Cache is managed by the HouseKeeping class
     * @param index    the starting record of the returning resultlist
     * @param numRecs  the maximum number of records to return
     * @param schemaId the recordSchema
     * @param edt      ExtraDataType that is used with the query.
     * @return         A record iterator.
     * @throws InstantiationException
     */
    @Override
    public RecordIterator newRecordIterator(long index, int numRecs, String schemaId, ExtraDataType edt) throws InstantiationException
    {
        DocList subset = docList.subset((int)index - 1, numRecs);
        DocIterator iterator = subset.iterator();
        ArrayList<Integer> docIds = new ArrayList(numRecs);
        while (iterator.hasNext())
        {
            int docId = iterator.next();
            docIds.add(docId);
        }

        return new SolrRecordIterator(req, transport, docIds, "default", edt);
    }

     @Override
    public void close()
     {
         super.close();

         req.close();
     }

    public NamedList getSimpleFacets(SolrParams solrParams)
    {
        SimpleFacets simple = new SimpleFacets(req, docList, solrParams);
        NamedList facets_result ;


        try {
            facets_result = simple.getFacetFieldCounts();
        } catch (IOException e) {
            log.warn(e);
            return null ;
        } catch (ParseException e) {
            log.warn(e);
            return null ;
        }

        return facets_result;
    }

    private final Log log = LogFactory.getLog(this.getClass());
}
