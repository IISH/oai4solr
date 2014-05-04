#This is a SRU and SRW library plugin for Solr

##What is it ?
A plugin that exposes your Solr 3.x indexes with SRU and SRW SOAP.

##How do I install it ?
I wrote this plugin in 2009 and never made proper documentation.
Looking at it now, there is too much configuration and too little convention.
This has to be addressed first.

Till that time there is a runnable demo.

##To build from source
Clone from the git repository:

    $ mvn clean package

The end result is a package in ./srw-oclc-plugin/target

##Download
You can also download the latest build from https://bamboo.socialhistoryservices.org/browse/OAI4SOLR-OAI4SOLR/latest from the artifacts tab.

##Runable demo
Once the project is build, a demo is available. It contains an embedded Solr Jetty server. If you start it, it will load MarcXML test records.

Copy the oai2-plugin-3.x-1.0.jar into the demo/solr/lib folder. Or place a symbolic link to it. The
directory structure should look like this:

    ----
    -demo
        -solr
            -core0
                +conf
                +srw
            +docs
            -lib
                oai2-plugin-3.1.jar

Start the demo with:

    java -jar demo/target/demo-1.0.jar

Then explore the test SRW repository with your request to it, e.g.

    http://localhost:8983/solr/core0/srw?operation=explain