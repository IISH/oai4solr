package org.socialhistoryservices.api.oai;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.openarchives.oai2.OAIPMHtype;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * EmbeddedServer
 * <p/>
 * Because the Solr EmbeddedSolrServer returns a parsed, normalized response, it is not
 * useful to evaluate the flow that otherwise would have begun with the input to the
 * RequestHandler and ended with the output of the OAIQueryResponseWriter.
 * <p/>
 * Therefore we override that normalization step here and chain the two class instances.
 *
 * Furthermore we add some helper methods for unmarchall and lookup actions.
 */
public class EmbeddedServer extends EmbeddedSolrServer2 {

    private final Log log = LogFactory.getLog(this.getClass());

    EmbeddedServer(CoreContainer coreContainer, String coreName) {
        super(coreContainer, coreName);
    }

    /**
     * getParsedResponse
     * <p/>
     * Use t
     *
     * @param request  The request set by the unit test and received by the OAIRequestHandler
     * @param response The response set by the OAIRequestHandler
     * @return The request and response objects.
     */
    public NamedList<Object> getParsedResponse(SolrQueryRequest request, SolrQueryResponse response) {
        final NamedList<Object> list = new NamedList<>();
        list.add("request", request);
        list.add("response", response);
        return list;
    }

    /**
     * sendRequest
     * <p/>
     * Send the request from the unit test method to the OAI handler.
     * Then let the result be unmarshalled into a OAIPMHtype instance and returned
     *
     * @return The OAI2 response.
     */
    @SuppressWarnings("unchecked")
    OAIPMHtype sendRequest(SolrParams oai_params) {

        // Create a request with the desired oai parameters and set the OAI handler
        SolrRequest request = new QueryRequest(oai_params);
        request.setPath("/oai");
        NamedList<Object> list = null;

        // make the request
        try {
            list = request(request, TestOAIRequestHandler.CORE);
        } catch (SolrServerException | IOException e) {
            log.error(e);
        }
        assert list != null;

        // pass on the result to the  response writer and capture the response.
        QueryResponseWriter queryResponseWriter = getCoreContainer().getCore(TestOAIRequestHandler.CORE).getQueryResponseWriter("oai");
        ByteArrayOutputStream is = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(is);
        try {
            queryResponseWriter.write(writer, (SolrQueryRequest) list.get("request"), (SolrQueryResponse) list.get("response"));
            writer.close();
        } catch (IOException e) {
            log.error(e);
        }

        // unmarchall (is that a verb ?) the response into the main OAIPMHtype instance
        byte[] bytes = is.toByteArray();
        final Source source = new StreamSource(new ByteArrayInputStream(bytes));
        final Unmarshaller marshaller = (Unmarshaller) Parameters.getParam("unmarshaller");
        JAXBElement<OAIPMHtype> oai = null;
        try {
            oai = (JAXBElement<OAIPMHtype>) marshaller.unmarshal(source);
        } catch (JAXBException e) {
            log.error(e);
        }
        assert oai != null;
        return oai.getValue();
    }

    /**
     * GetNode
     *
     * A way to evaluate the XmlAny content buried deep within an OAIPMHtype XmlAnyElement instance.
     *
     * @param node An OAIPMHtype instance's node
     * @param xquery The node to look for
     * @return The value of the node
     */
    String GetNode(Node node, String xquery) {

        XPathExpression expr;
        Object evaluate = null;

        try {
            expr = getXPathExpression(xquery);
            evaluate = expr.evaluate(node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return (evaluate == null) ? null : ((Node) evaluate).getTextContent();
    }

    private XPathExpression getXPathExpression(String xquery) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();


        final NamespaceContext ns = new NamespaceContext() {

            @Override
            public String getPrefix(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getNamespaceURI(String prefix) {

                Map<String, String> namespaces = new HashMap<>();
                namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
                namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
                namespaces.put("oai", "http://www.openarchives.org/OAI/2.0/");
                namespaces.put("oai-identifier", "http://www.openarchives.org/OAI/2.0/oai-identifier");

                if (namespaces.containsKey(prefix))
                    return namespaces.get(prefix);

                return XMLConstants.XML_NS_URI;
            }
        };

        xpath.setNamespaceContext(ns);

        return xpath.compile(xquery);
    }

}
