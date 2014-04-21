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

import ORG.oclc.os.SRW.TermList;
import gov.loc.www.zing.srw.TermType;
import org.apache.axis.MessageContext;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrQueryParser;
import org.z3950.zing.cql.CQLTermNode;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Lucien van Wouw
 * Date: 7-jan-2010
 * Time: 16:30:50
 */
class SolrTermList extends TermList
{
    // facetquery could have been an alternative to enumTerm...
    public SolrTermList(CQLTermNode term, int position, int maxTerms, Map<String,ArrayList> getExplainMap){
        MessageContext msgContext = MessageContext.getCurrentContext();
        SolrQueryRequest req = (SolrQueryRequest)msgContext.getProperty("SolrQueryRequest");
        SolrQueryParser parser = new SolrQueryParser(req.getSchema(), null);

        ArrayList indices = ( term.getRelation().getBase().equals("exact") ) // scans are exact or any\all\=
            ? getExplainMap.get(SolrSRWDatabase.IndexOptions.scan_exact + "." + term.getQualifier())
            : getExplainMap.get(SolrSRWDatabase.IndexOptions.scan + "." + term.getQualifier()) ;

        if ( indices == null ) // no such context index
            return ;

        if ( position > maxTerms)
            maxTerms = position ;

        SolrIndexReader reader = req.getSearcher().getReader() ;
        Map<String,Integer> list = new HashMap();
        org.apache.lucene.index.Term marker_term = null;
        for (Object indice : indices) {
            String field_name = (String) indice;
            try {
                // Term_to is at best the actual term from the search exactly matching a term in the index.
                // ... of at least the one near it...
                // This may not be exact, because the stored terms went through the analyzer(s).
                // So we shall try an approximation.
                String text_to = field_name + ":" + term.getTerm();
                String parsed_text_to = parser.parse(text_to).toString(field_name);
                Term term_to;
                boolean empty = (term.getTerm().equals("$") || term.getTerm().length() == 0);
                term_to = (empty) // If the search term was empty, we should begin at the start of the first term in the index.
                        ? LowestTerm(reader, field_name)
                        : new Term(field_name, parsed_text_to);

                // These may be the terms at the beginning of the enumeration
                Term term_from = LowestTerm(reader, field_name);

                if (empty) {
                    HigherTerm(reader, list, term_to, maxTerms);
                    if (marker_term == null)
                        marker_term = new Term(field_name, parsed_text_to);
                } else {
                    // This is a little more tricky...
                    // iterate again
                    String marker = HigherMiddleTerm(maxTerms, reader, list, field_name, term_to, term_from);
                    if (marker_term == null)
                        marker_term = (marker == null)
                                ? new Term(field_name, parsed_text_to)
                                : new Term(field_name, marker);
                }
            }
            catch (Exception e) {
                log.warn(e);
            }
        }

        // Do some sorting here... Let's return the list ascending
        TermTypeRecord marker = null ;
        List<TermTypeRecord> termTypeRecords = new ArrayList();
        for (Object o : list.keySet()) {
            String key = (String) o;
            NonNegativeInteger numberOfRecords = new NonNegativeInteger(String.valueOf(list.get(key)));
            TermType tt = new TermType(key, numberOfRecords, null, null, null);
            TermTypeRecord ttr = new TermTypeRecord(tt);
            termTypeRecords.add(ttr);

            if (key.equals(marker_term.text()) || key.equals(term.getTerm()))
                marker = ttr;
        }
        Collections.sort(termTypeRecords);

        // Next go back and forth the index.......
        // Position determines the position of the requested term in the list.
        ArrayList terms_in_response = new ArrayList(maxTerms);
        int index = termTypeRecords.indexOf(marker);

        // First get all records before the requested term:
        // We include the actual term
        int start = index - position + 1;
        if ( start < 0 )
            start = 0 ;
        while ( maxTerms > 0 && start < termTypeRecords.size() )
        {
            TermTypeRecord record = termTypeRecords.get(start++) ;
            if ( record == null )
                break ;

            terms_in_response.add(record.term) ;
            maxTerms--;
        }

        // Finaly, add the resultset of terms to the response.
        TermType[] terms = new TermType[terms_in_response.size()];
        setTerms((TermType[])terms_in_response.toArray(terms)) ;
    }

    // Get a list of terms, with the requested term somewhere in the middle of it.
    // Not really reliable, but adequate.
    private String HigherMiddleTerm(int maxTerms, SolrIndexReader reader, Map<String, Integer> list, String field_name, Term term_to, Term term_from) throws IOException {
        int count = maxTerms + 10; // Just an offset.
        String marker = null ;
        TermEnum termEnum = reader.terms(term_from);
        do
        {
            if ( field_name.equalsIgnoreCase( termEnum.term().field() ) )
            {

            String text = termEnum.term().text();
            int freq = termEnum.docFreq() ;
            AddToList(list, text, freq);

            int compare = term_to.compareTo(termEnum.term()) ;
            if ( compare <= 0 )
            {
                if ( marker == null )
                    marker = text ;

                count--;
            }
            }
        }
        while ( termEnum.next() && count > 0 ) ;

        return marker ;
    }


    // Enumererate from a term upwards ( ascending that is ) with the maximum number of terms that ought to
    // be in the list.
    private void HigherTerm(SolrIndexReader reader, Map<String,Integer> list, Term term_to, int maxTerms) throws IOException {
        TermEnum enumerator = reader.terms(term_to);
        while ( enumerator.next() && maxTerms > 0)
        {
            Term test = enumerator.term();
            if ( test.field().equals(term_to.field()))
            {
                AddToList(list, test.text(), enumerator.docFreq()) ;
                maxTerms-- ;
            }
        }
    }

    private void AddToList(Map<String, Integer> list, String text, int freq) {
        if ( list.containsKey(text))
            list.put(text, freq + list.get(text)) ;
        else
            list.put(text, freq) ;
    }

    // Lets get the very first term in the index
    private Term LowestTerm(SolrIndexReader reader, String fld) throws IOException {

        TermEnum enumerator = reader.terms() ;
        while (enumerator.next())
        {
            if ( fld.equals( enumerator.term().field() ) )
                return enumerator.term() ;
            
        }

        return enumerator.term() ;
    }

    /*
    @Override
    public TermType[] getTerms()
    {
        return super.getTerms() ;
    }

    @Override
    public void setTerms(TermType[] terms)
    {
        super.setTerms(terms);
    }
    */

    public class TermTypeRecord implements Comparable<TermTypeRecord>
    {
        final TermType term;

        public TermTypeRecord( TermType term )
        {
                this.term = term ;
        }

        @Override
        public int compareTo(TermTypeRecord o)
        {
            int compare = term.getValue().compareTo(o.term.getValue()) ;

            return compare ;
        }
    }

    private final Log log = LogFactory.getLog(this.getClass());

}
