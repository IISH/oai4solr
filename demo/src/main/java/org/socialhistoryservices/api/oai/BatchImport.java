package org.socialhistoryservices.api.oai;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;


/**
 * BatchImport
 * <p/>
 * Stone age import utility. But still use full for demos.
 * <p/>
 * args[0]: SOLR update url with core, like http://localhost:8080/solr/core0/update
 */
public class BatchImport {

    private EmbeddedSolrServer server;
    private Transformer transformer;
    private int counter = 0;

    public BatchImport(EmbeddedSolrServer server) throws TransformerConfigurationException, FileNotFoundException, MalformedURLException {

        this.server = server;
        transformer= TransformerFactory.newInstance().newTransformer()  ;
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    }

    public void process(File file) throws IOException, XMLStreamException, ParserConfigurationException, SAXException, TransformerException, SolrServerException {

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = builderFactory.newDocumentBuilder();
        final Document doc = builder.parse(file);
        final NodeList childNodes = doc.getDocumentElement().getElementsByTagName("record");

        for ( int i = 0 ; i < childNodes.getLength();i++) {

            ByteArrayOutputStream resource = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(childNodes.item(i));
            Result outputTarget = new StreamResult(resource);
            transformer.transform(xmlSource, outputTarget);
            resource.close();

            final SolrInputDocument solrInputFields = new SolrInputDocument();
            solrInputFields.addField("identifier", counter++);
            solrInputFields.addField("datestamp", new Date());
            solrInputFields.addField("theme", String.format("setSpec1", counter % 3));
            solrInputFields.addField("resource", new String(resource.toByteArray(), "utf-8"));
            log.info("Adding document " + counter);
            server.add(solrInputFields);
        }
    }

    private Logger log = Logger.getLogger(getClass().getName());
}