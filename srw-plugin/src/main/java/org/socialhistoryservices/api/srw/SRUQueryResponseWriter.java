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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;

import javax.xml.soap.SOAPException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: Lucien van Wouw
 * Date: 15-jul-2009
 * Time: 11:19:06
 */
public class SRUQueryResponseWriter implements QueryResponseWriter {
    private final static String CONTENT_TYPE_JSON_UTF8 = "text/javascript; charset=UTF-8";

    /**
     * cleanup
     * <p/>
     * Remove the SOAP wrapper from the response.
     * <p/>
     * Copied and adapted this method from ORG.oclc.os.SRW.SRWServlet
     * ToDo: apply xslt logic.
     *
     * @param message The soap message
     * @param nameElementClose <element> to be wrapped to </element>
     * @param cleanup True to remove the SOAP wrapper.
     * @return A cleaned up String
     * @throws AxisFault
     */
    private String cleanup(Message message, String nameElementClose, Boolean cleanup) throws AxisFault {

        String soapResponse = message.getSOAPPartAsString();
        int start, stop = soapResponse.indexOf(nameElementClose);
        if (stop == -1) {
            log.warn("Could not find closing element: " + nameElementClose);
            return soapResponse;
        }

        String nameElementOpen = nameElementClose.replace("</", "<").substring(0, nameElementClose.length() - 2);
        start = soapResponse.indexOf(nameElementOpen);
        stop += nameElementClose.length();

        return (cleanup)
                ? cleanup(soapResponse.substring(start, stop).toCharArray())
                : soapResponse.substring(start, stop);
    }

    // Rather perform these with an xslt
    // Copied & pasted this method from ORG.oclc.os.SRW.SRWServlet
    private String cleanup(char[] buf) {
        boolean didOne = false, insideRecordData = false;
        int i, j, len = buf.length;

        for (i = 0; i < len; i++) {
            if (buf[i] == ' ' && len - i > 5) // might be " xsi:"
                if (buf[i + 1] == 'x' && buf[i + 2] == 's' && buf[i + 3] == 'i' &&
                        buf[i + 4] == ':') {
                    if (insideRecordData)
                        if (didOne)
                            continue;
                        else
                            didOne = true;
                    boolean foundQuote = false;
                    for (j = i + 5; j < len; j++)
                        if (buf[j] == '"')
                            if (foundQuote)
                                break;
                            else
                                foundQuote = true;
                    if (j == len) // never found matching quotes, so ignore
                        continue;
                    // remove offending chars
                    //log.info("i="+i+", j="+j+", len="+len);
                    System.arraycopy(buf, j + 1, buf, i, len - j - 1);
                    len -= (j - i) + 1;
                    i--;
                    continue;
                }
            if (buf[i] == '<' && len - 1 > 11) // might be "<RecordData>"
                if (buf[i + 1] == 'r' && buf[i + 2] == 'e' && buf[i + 3] == 'c' &&
                        buf[i + 4] == 'o' && buf[i + 5] == 'r' && buf[i + 6] == 'd' &&
                        buf[i + 7] == 'D' && buf[i + 8] == 'a' && buf[i + 9] == 't' &&
                        buf[i + 10] == 'a') {
                    insideRecordData = true;
                    didOne = false;
                }
            if (buf[i] == '<' && len - 1 > 12) // might be "</RecordData>"
                if (buf[i + 1] == '/' && buf[i + 2] == 'r' && buf[i + 3] == 'e' &&
                        buf[i + 4] == 'c' && buf[i + 5] == 'o' && buf[i + 6] == 'r' &&
                        buf[i + 7] == 'd' && buf[i + 8] == 'D' && buf[i + 9] == 'a' &&
                        buf[i + 10] == 't' && buf[i + 11] == 'a') {
                    insideRecordData = false;
                }
        }
        return new String(buf, 0, len);
    }

    @Override
    public void write(Writer writer, SolrQueryRequest solrQueryRequest, org.apache.solr.response.SolrQueryResponse solrQueryResponse) throws IOException {

        final MessageContext msgContext = (MessageContext) solrQueryResponse.getValues().get("MessageContext");
        final Message message = msgContext.getResponseMessage();

        SolrSRWDatabase.RequestTypes requestType = (SolrSRWDatabase.RequestTypes) msgContext.getProperty(SolrSRWDatabase.RequestTypes.class.getSimpleName());
        String tag = (requestType == null)
                ? null
                : "</" + requestType.name().replace("Request", "Response") + ">";

        final SolrSRWDatabase.Transport transport = (SolrSRWDatabase.Transport) msgContext.getProperty(SolrSRWDatabase.Transport.class.getSimpleName());
        switch (transport) {
            case SRW:
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    message.writeTo(baos);
                } catch (SOAPException e) {
                    log.error(e, e); // Impossible at this point.
                }
                writer.write(baos.toString("UTF-8"));
                break;

            case SRU: // Response which requires removal of SOAP envelope and xsi:type attributes.
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                Object resource = msgContext.getProperty("resource");
                if (resource == null) // No system stylesheet... but sometimes people may add a custom one.
                {
                    resource = msgContext.getProperty("stylesheet");
                    if (resource != null) {
                        writer.write("<?xml-stylesheet title=\"Custom XSL formatting\" type=\"text/xsl\" href=\"");
                        writer.write(String.valueOf(resource));
                        writer.write("\"?>");
                    }
                } else {
                    Object _stylesheet = msgContext.getProperty("stylesheet");
                    String stylesheet = (_stylesheet == null)
                            ? String.valueOf(resource)
                            : String.valueOf(_stylesheet);

                    if (stylesheet.length() != 0) {
                        writer.write("<?xml-stylesheet title=\"OCLC XSL formatting\" type=\"text/xsl\" href=\"");
                        writer.write(String.valueOf(resource));
                        writer.write("\"?>");
                    }
                }

                // We need to change the SOAP response into SRU
                writer.write(cleanup(message, tag, true));
                break;

            case JSON:
                SolrSRWDatabase db = (SolrSRWDatabase) msgContext.getProperty("db");
                String jsonp = (String) msgContext.getProperty("jsonp");

                writer.write(jsonp);
                writer.write("(");
                Transformer t = db.getTransformer("xml-2-json");
                StreamSource source = new StreamSource(new StringReader(cleanup(message, tag, true)));
                try {
                    t.transform(source, new StreamResult(writer));
                } catch (TransformerException e) {
                    log.error(e, e);
                }
                writer.write(")");

                break;
        }
    }

    @Override
    public String getContentType(SolrQueryRequest solrQueryRequest, org.apache.solr.response.SolrQueryResponse solrQueryResponse) {

        final MessageContext msgContext = (MessageContext) solrQueryResponse.getValues().get("MessageContext");
        final SolrSRWDatabase.Transport transport = (SolrSRWDatabase.Transport) msgContext.getProperty(SolrSRWDatabase.Transport.class.getSimpleName());

        switch (transport) {
            default:
                return CONTENT_TYPE_XML_UTF8;

            case JSON:
                return CONTENT_TYPE_JSON_UTF8;
        }
    }

    public void init(NamedList n) {
    }

    private final Log log = LogFactory.getLog(this.getClass());
}
