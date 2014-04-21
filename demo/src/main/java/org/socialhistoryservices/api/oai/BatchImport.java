package org.socialhistoryservices.api.oai;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Date;


/**
 * BatchImport
 * <p/>
 * Stone age import utility. But still good !
 *
 * Imports sample marcxml records
 * <p/>
 */
public class BatchImport {

    private Logger log = Logger.getLogger(getClass().getName());
    private EmbeddedSolrServer server;
    private int counter = 0;
    private Transformer transformer;

    public BatchImport(EmbeddedSolrServer server) throws ParserConfigurationException, TransformerConfigurationException {

        this.server = server;
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/marc2solrfields.xsl");
        transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(resourceAsStream));
    }

    public void processFiles(File folder) throws IOException, SolrServerException, ParserConfigurationException, SAXException, TransformerException {

        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            process(file);
        }
    }

    private void process(File file) throws IOException, SolrServerException, ParserConfigurationException, SAXException, TransformerException {

        final SolrInputDocument solrInputFields = new SolrInputDocument();
        solrInputFields.addField("identifier", counter++);
        solrInputFields.addField("datestamp", new Date());
        solrInputFields.addField("theme", String.format("setSpec%s", counter % 3));

        final StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(file),new StreamResult(writer));
        final String[] fields = writer.toString().split("_Separator_");
        for ( String field : fields) {
            String[] split = field.split(":", 2);
            if ( split.length == 2)
                solrInputFields.addField(split[0].trim(), split[1].trim());
        }

        final FileInputStream fis = new FileInputStream(file);
        solrInputFields.addField("resource", new String(IOUtils.toByteArray(fis), "utf-8"));
        fis.close();

        log.info("Adding document " + counter);
        server.add(solrInputFields);
    }

}