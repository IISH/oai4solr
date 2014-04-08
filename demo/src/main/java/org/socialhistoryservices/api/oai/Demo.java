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

    public static String CORE = "core0";

    public static void main(String[] args) throws Exception {

        String solr_home = deriveSolrHome();
        CoreContainer coreContainer = new CoreContainer(solr_home, new File(solr_home, "solr.xml"));
        EmbeddedSolrServer server = null ;
        try {
            new EmbeddedSolrServer(coreContainer, CORE);
        }   catch (org.apache.solr.common.SolrException e) {
            log.fatal(e);
            log.fatal("Make sure you have the oai2-plugin jar placed in the Solr ./demo/solr/lib folder, or a symbolic link pointing to it");
            System.exit(-1);
        }

        server.deleteByQuery("*:*");
        server.optimize();

        final BatchImport batchImport = new BatchImport(server);
        batchImport.process(new File(solr_home + "/docs/irsh.xml"));
        server.commit();
        server.shutdown();

        String baseUrl = "http://localhost:8983/solr/core0/oai?";
        JettySolrRunner solrRunner = new JettySolrRunner("/solr", 8983);
        solrRunner.start(false);
        System.out.println("Now try out the plugin. Make sure it is on the classpath when you build this demo:\n" +
                baseUrl + "verb=Identify\n" +
                baseUrl + "verb=ListSets\n" +
                baseUrl + "verb=ListMetadataFormats\n" +
                baseUrl + "verb=ListRecords&metaPrefix=oai_dc\n" +
                baseUrl + "verb=GetRecord&identifier=oai:localhost:1&metaPrefix=oai_dc\n" +
                baseUrl + "verb=GetRecord&identifier=oai:localhost:1&metaPrefix=solr\n" +
                baseUrl + "verb=GetRecord&identifier=oai:localhost:1&metaPrefix=marcxml");
        solrRunner.waitForSolr("/solr");
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
}
