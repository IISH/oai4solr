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

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * SRURequestHandlerSOAP
 * <p/>
 * We accept the Solr request parameters.
 * Those are either from a GET or a POST.
 * <p/>
 * Next a SOAP envelope is extracted from the POST itself,
 * or if not: a SOAP request is constructed from the Solr parameters.
 * <p/>
 * After that, the request is passed on to the Axis server.
 * The MessageResponse is then added to the Solr response.
 * <p/>
 * And we are done. The QueryResponseWriter will take over the task of producing the response stream.
 */

public abstract class SRURequestHandlerSOAP extends RequestHandlerBase {

    private final static Log log = LogFactory.getLog("SRURequestHandlerSOAP");
    private SolrSRWDatabase db;

    private static String xml2json_callback_key = "callback";

    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        // Collect the sru parameters to construct a SOLR search later on.
        // First we have to determine if we have a SRW\SOAP request or SRU request.
        // A SOAP will always be in an envelope.

        // And we might say when SOLR parameters are present, it must be an SRU call.
        // However, an Axis server is capable to accept a GET with the method specified.
        MessageContext msgContext = DeserializeRequest(req);
        msgContext.setProperty("stylesheet", req.getParams().get("stylesheet"));
        rsp.add("MessageContext", msgContext);
    }

    private MessageContext DeserializeRequest(SolrQueryRequest req) throws Exception {

        Iterable<ContentStream> iterable = req.getContentStreams();
        if (iterable == null)  // We have a GET...
            return Deserialization(req);  // Ok, so let's try to build a SOAP message then based on the given parameters.

        ContentStream stream = iterable.iterator().next();
        return Deserialization(req, stream.getStream());
    }

    // If we failed to get the operation via SOAP, let's try to construct it from the url.
    private MessageContext Deserialization(SolrQueryRequest req) throws Exception, IllegalAccessException {
        // What operation are we talking about ?

        SolrParams params = req.getParams();

        String wsdl = params.get("wsdl");
        if (wsdl != null) // Is it an WSDL request maybe ?
        {
            SolrSRWDatabase.Services targetService;
            try {
                targetService = SolrSRWDatabase.Services.valueOf(wsdl);
            } catch (IllegalArgumentException e) {
                log.warn("wsdl='" + wsdl + "' is not a known service name. Use:");
                for (SolrSRWDatabase.Services service : SolrSRWDatabase.Services.values()) {
                    log.warn(service.name());
                }
                targetService = SolrSRWDatabase.Services.SRW;
            }

            final MessageContext messageContext = db.setResponseMessage(targetService);
            messageContext.setProperty(SolrSRWDatabase.Transport.class.getSimpleName(), SolrSRWDatabase.Transport.SRW);
            return messageContext;
        }

        final SolrSRWDatabase.Transport transport;

        String jsonp = params.get(xml2json_callback_key);
        if (jsonp == null) {
            String targetService = params.get("targetService");
            transport = (targetService == null)
                    ? SolrSRWDatabase.Transport.SRU
                    : SolrSRWDatabase.Transport.SRW;
        } else
            transport = SolrSRWDatabase.Transport.JSON;

        String operation = params.get("operation");
        String request;
        if (operation == null) // Defaults to an explain request
        {
            request = SolrSRWDatabase.RequestTypes.explainRequest.name();
            // Also make sure the request parameters are there...
            NamedList list = params.toNamedList();
            list.add("operation", "explain");

            if (params.get("recordPacking") == null)
                list.add("recordPacking", "xml");

            if (params.get("version") == null)
                list.add("version", "1.1");

            req.setParams(SolrParams.toSolrParams(list));
        } else request = operation + "Request";

        SOAPEnvelope env = new SOAPEnvelope();
        RPCElement body = new RPCElement("http://www.loc.gov/zing/srw/", request, null);

        // Build the SOAP env with the body in it.
        GetSOAPParameters(params, body);
        env.addBodyElement(body);

        byte[] bytes = new byte[env.getLength()];
        bytes = env.getAsString().getBytes("UTF-8");

        InputStream is = new ByteArrayInputStream(bytes);

        MessageContext messageContext = Deserialization(req, is);
        messageContext.setProperty(SolrSRWDatabase.Transport.class.getSimpleName(), transport);
        messageContext.setProperty("jsonp", jsonp);

        return messageContext;
    }

    /**
     * @param body The SOAP body is empty. We try to fill it with Solr Parameters.
     *             The names of the parameters are derived from the DataTypes ( explain or searchRequest ).
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private void GetSOAPParameters(SolrParams params, RPCElement body) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        QName qName = body.getQName();

        Class cls; // The data type
        try {
            cls = GetClassByName(qName);
        } catch (ClassNotFoundException e) {
            // unsupported operation
            body = null;
            return;
        }
        Method class_method = cls.getMethod("getTypeDesc");

        TypeDesc td = (TypeDesc) class_method.invoke(cls);

        FieldDesc[] fields = td.getFields();
        for (FieldDesc field : fields) {
            String item = field.getFieldName();
            String param = params.get(item);
            if (param != null && param.trim().length() != 0) {

                javax.xml.soap.SOAPElement element;
                try {
                    element = body.addChildElement(item, "");
                    element.addTextNode(param);
                } catch (javax.xml.soap.SOAPException e) {
                    log.warn(e);
                }
            }
        }
    }

    // Let's see if we can deserialize a SOAP message.
    private MessageContext Deserialization(SolrQueryRequest req, InputStream is) throws IOException, SOAPException {

        // To find the operation such as scanRequest, all we need to do it:
        // String request = requestMessage.getSOAPEnvelope().getBody().getFirstChild().getLocalName();
        Message aMessage = new Message(is, false);
        String request = aMessage.getSOAPEnvelope().getBody().getFirstChild().getLocalName();
        SolrSRWDatabase.RequestTypes requestType = SolrSRWDatabase.RequestTypes.valueOf(request);
        SolrSRWDatabase.Services targetService = (requestType == SolrSRWDatabase.RequestTypes.explainRequest)
                ? SolrSRWDatabase.Services.ExplainSOAP
                : SolrSRWDatabase.Services.SRW;

        // And yet, this messes up the Axis server processing of this very message, as it sets
        // the internal Envelope property, so again:
        Message requestMessage = new Message(aMessage.getSOAPPartAsString());

        return db.prepairMessage(requestMessage, req, targetService, requestType, SolrSRWDatabase.Transport.SRW);
    }

    private Class GetClassByName(QName qName) throws ClassNotFoundException {

        return Class.forName("gov.loc.www.zing.srw." + qName.getLocalPart().substring(0, 1).toUpperCase() + qName.getLocalPart().substring(1) + "Type");
    }

    public void setDb(SolrSRWDatabase db) {
        this.db = db;
    }
}
