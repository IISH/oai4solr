package org.socialhistoryservices.api.oai;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.core.CoreContainer;

import java.io.File;

/**
 * Demo
 */
public class Demo {

    private final static Log log = LogFactory.getLog("Demo");

    private static String CORE = "core0";

    private static void checkLib(String solr_home) {
        File checkLib = new File(solr_home, "/lib/");
        File[] files = checkLib.listFiles();
        if (files == null || files.length == 0) {
            log.fatal("No library plugins found. Please make sure the /demo/solr/lib folder has the oai2-plugin jar file.");
            System.exit(-1);
        }
    }

    private static String deriveSolrHome() {

        String solr_relative_home = "/demo/solr";

        File file = new File(System.getProperty("user.dir"));
        while (!file.getName().equals("oai4solr")) {
            file = file.getParentFile();
            if (file == null)
                break;
        }

        if (file == null) {
            log.fatal("Cannot find the Solr home directory. Please set the VM property with -Dsolr.solr.home=[path?]/oai4solr" + solr_relative_home);
            System.exit(-1);
        }

        String solr_home = new File(file, solr_relative_home).getAbsolutePath();
        System.setProperty("solr.solr.home", solr_home);
        return solr_home;
    }

    public static void main(String[] args) throws Exception {

        String solr_home = deriveSolrHome();

        checkLib(solr_home);

        final CoreContainer coreContainer = new CoreContainer(solr_home);
        coreContainer.load();
        final EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, CORE);

        server.deleteByQuery("*:*");
        server.optimize();

        final BatchImport batchImport = new BatchImport(server);
        batchImport.processFiles(new File(solr_home + "/docs/"));
        server.commit();
        server.close();

        JettySolrRunner jetty = new JettySolrRunner(solr_home, "/solr", 8983);
        String baseUrl = "http://localhost:8983/solr/core0/oai?";
        System.out.println("\n\nGo ahead and try out the plugin:\n" +
                baseUrl + "verb=Identify\n" +
                baseUrl + "verb=ListSets\n" +
                baseUrl + "verb=ListMetadataFormats\n" +
                baseUrl + "verb=ListRecords&metadataPrefix=oai_dc\n" +
                baseUrl + "verb=ListRecords&metadataPrefix=marcxml\n" +
                baseUrl + "verb=ListRecords&metadataPrefix=solr\n" +
                baseUrl + "verb=GetRecord&identifier=oai:localhost:1&metadataPrefix=oai_dc\n" +
                baseUrl + "verb=GetRecord&identifier=oai:localhost:1&metadataPrefix=marcxml\n" +
                baseUrl + "verb=GetRecord&identifier=oai:localhost:1&metadataPrefix=solr");
        jetty.start();
    }
}
