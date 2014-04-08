package org.socialhistoryservices.api.oai;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;


/**
 * BatchImport
 * <p/>
 * Stone age import utility. But still good !
 * <p/>
 */
public class BatchImport {

    private Logger log = Logger.getLogger(getClass().getName());

    private EmbeddedSolrServer server;
    private int counter = 0;

    public BatchImport(EmbeddedSolrServer server) {

        this.server = server;
    }

    public void processFiles(File folder) throws IOException, SolrServerException {

        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            process(file);
        }
    }

    private void process(File file) throws IOException, SolrServerException {

        final SolrInputDocument solrInputFields = new SolrInputDocument();
        solrInputFields.addField("identifier", counter++);
        solrInputFields.addField("datestamp", new Date());
        solrInputFields.addField("theme", String.format("setSpec%s", counter % 3));

        final FileInputStream fis = new FileInputStream(file);
        solrInputFields.addField("resource", new String(IOUtils.toByteArray(fis), "utf-8"));
        fis.close();

        log.info("Adding document " + counter);
        server.add(solrInputFields);
    }

}