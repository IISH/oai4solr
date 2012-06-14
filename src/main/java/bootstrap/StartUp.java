package bootstrap;

import org.eclipse.jdt.internal.core.Assert;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.socialhistoryservices.api.oai.OAIRequestHandler;

/**
 * Startup
 * <p/>
 * For testing.
 * <p/>
 * Startup our Jetty server and load the Solr application with these to VM parameters:
 * -Dport=server port ( like 8080 )
 * -DwebApp=/path/to/solr.war (for example from a maven dependency)
 * -DcontextPath= ( your context path. For example /solr/
 * <p/>
 * <p/>
 * The solr.war file must be placed in the application path.
 */

public class StartUp {

    private void start() throws Exception {

        int port = Integer.parseInt(System.getProperty("port"));
        Server server = new Server(port);
        String webApp = System.getProperty("webApp");
        String contextPath = System.getProperty("contextPath");
        if (contextPath == null) contextPath = "/";
        if (webApp == null) webApp = "solr.war"; // Just guessing
        server.addHandler(new WebAppContext(webApp, contextPath));
        server.start();
    }

    public static void main(String... args) throws Exception {

        final StartUp server = new StartUp();
        server.start();
    }
}