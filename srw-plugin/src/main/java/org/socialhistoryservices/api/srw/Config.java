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

/*
    Config.java
    Load and initialize the properties such as the OCLC database, Explain settings and mappings, etc.
*/


package org.socialhistoryservices.api.srw;


import org.apache.axis.configuration.FileProvider;
import org.apache.axis.server.AxisServer;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.util.DOMUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.schema.DateField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;


/**
 * Config.java
 * <p/>
 * Initializes all common resources: the axis server and loads the explain XML document into the CQL-2-Lucene list
 */
class Config {

    private static int c;
    private static String setSolr;
    private static final Log log = LogFactory.getLog(Config.class);

    /**
     * init
     * <p/>
     * Reads all configuration parameters and loads the CQL-2-Lucene list into memory.
     *
     * @param schema            Solr schema instance
     * @param srw_absolute_home full path of the srw configuration files
     * @param args              Solr namedlist parameters as they were set in the solrconfig core document.
     * @return An instance of a SolrSRWDatabase.
     */
    public static SolrSRWDatabase init(IndexSchema schema, String srw_absolute_home, NamedList args) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {

        NamedList srw_properties = (NamedList) args.get("srw_properties");

        Properties properties = new Properties();
        for (int i = 0; i < srw_properties.size(); i++) {
            String field_name = srw_properties.getName(i);
            properties.setProperty(field_name, String.valueOf(srw_properties.getVal(i)));
        }

        if (!properties.containsKey("SRW.Context")) {
            properties.setProperty("SRW.Context", "/");
        }
        properties.setProperty("SRW.Home", properties.getProperty("SRW.Context"));
        properties.setProperty("dbHome", srw_absolute_home);
        getExplainVariables(properties);

        String host = properties.getProperty("serverInfo.host", "http://localhost/solr/srw");
        properties.setProperty("serverInfo.host", host);  // Assumption
        String port = properties.getProperty("serverInfo.port", "8983");
        properties.setProperty("serverInfo.port", port); // Assumption

        if (!properties.containsKey("serverInfo.port"))
            properties.setProperty("serverInfo.port", "");

        setSolr = properties.getProperty("solr.identifier");
        if (setSolr == null)
            setSolr = "info:srw/cql-context-set/2/solr";  // Assumption

        String dbkey = properties.getProperty("databaseInfo.title", properties.getProperty("serverInfo.database", properties.getProperty("dbname", SolrSRWDatabase.class.getSimpleName())));
        String dbname = (SolrSRWDatabase.dbs.containsKey(dbkey))
                ? dbkey + ".instance" + String.valueOf(++c)
                : dbkey;

        properties.setProperty("db." + dbname + ".class", SolrSRWDatabase.class.getName());
        properties.setProperty("db." + dbname + ".home", srw_absolute_home);
        SolrSRWDatabase db = (SolrSRWDatabase) SolrSRWDatabase.getDB(dbname, properties);
        if (db.getAxisServer() == null) {
            // As it is a static we only need to initialize the axis server field once.
            InputStream is = new FileInputStream(srw_absolute_home + "/deploy.wsdd");
            FileProvider provider = new FileProvider(is);
            db.setAxisServer(new AxisServer(provider));
        }

        Map<String, ArrayList> explainMap = loadExplainMap(schema, properties);
        db.setExplainMap(explainMap);
        db.setFacets(getFacets(args.get("facets"), explainMap));

        return db;
    }

    /**
     * getExplainVariables
     * <p/>
     * Take the information from the explain record and add them to the server's properties as cached data.
     *
     * @param properties
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    private static void getExplainVariables(Properties properties) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        final File f = new File(properties.getProperty("dbHome"), "explain.xml");
        if (!f.exists())
            throw new IOException("Explain document not found: " + f.getAbsolutePath());

        final Document doc = GetDOMDocument(f);

        final NodeList infos = GetNodes(doc, "zr:explain/zr:serverInfo | zr:explain/zr:databaseInfo | zr:explain/zr:metaInfo");
        for (int i = 0; i < infos.getLength(); i++) {
            final Element info = (Element) infos.item(i);
            final NodeList c = info.getChildNodes();
            for (int j = 0; j < c.getLength(); j++) {
                final Node node = c.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final String text = node.getTextContent();
                    if (text != null)
                        properties.setProperty(info.getLocalName() + "." + node.getLocalName(), text);
                }
            }
        }
    }

    private static NamedList getFacets(Object o, Map<String, ArrayList> indexFields) {
        if (o == null)
            return null;

        boolean added = false;

        NamedList facets = (NamedList) o;
        List facet_fields = facets.getAll("facet.field");

        for (Object facet_field : facet_fields) {
            String cql_facet_field = (String) facet_field;
            List list = indexFields.get(SolrSRWDatabase.IndexOptions.scan_exact + "." + cql_facet_field);
            if (list == null)
                list = indexFields.get(SolrSRWDatabase.IndexOptions.scan + "." + cql_facet_field);
            if (list == null) {
                log.warn("The facet.field=" + cql_facet_field + " does not map to a know Lucene index field and will be ignored.");
                continue;
            }

            // This list is used for the getExtraResponseData.
            // We want to know the mapping from CQL to Lucene fields.
            facets.add(cql_facet_field, list);
            added = true;
        }

        if (added)
            return facets;

        return null;
    }


    /**
     * loadExplainMap
     * <p/>
     * Iterates through the explain.xml document and locates all indexInfo set elements.
     * For each set, determine the mapped Solr indexFields and their type ( scan or searchable ).
     *
     * @return The result is a map with set names as keys, and a list of solr fields as values:
     * map<String set as key, List solrFields>
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    private static Map<String, ArrayList> loadExplainMap(IndexSchema indexSchema, Properties properties) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        final File f = new File(properties.getProperty("dbHome"), "explain.xml");
        final Document explainDocument = GetDOMDocument(f);
        Map<String, ArrayList> explainMap = new HashMap();

        log.info("Added " + cacheIndexFields(indexSchema, explainMap, explainDocument) + " index fields to the cache.");
        log.info("Removed " + removeNonIndexedMaps(explainDocument) + " maps because they referred to non indexed Solr fields.");
        log.info("Removed " + remoteEmptyIndexes(explainDocument) + " empty index elements from the explain document.");
        log.info("Removed " + removeSolrMaps(explainDocument) + " solr maps from the explain document");
        log.info("Removed " + removeAllSolrSets(explainDocument) + " solr maps from the explain document");
        log.info("Added " + addSolrFieldsToExplainDocument(indexSchema, explainMap, explainDocument) + " Solr field definitions to the explain document");
        log.info("Added " + cacheExplainElements(explainMap, explainDocument) + " to the cache.");

        return explainMap;
    }

    private static int cacheIndexFields(IndexSchema indexSchema, Map<String, ArrayList> explainMap, Document explainDocument) throws XPathExpressionException {
        // Locate the indexInfo sets

        SolrSRWDatabase.IndexOptions[] options = SolrSRWDatabase.IndexOptions.values();

        final NodeList schemaInfos = GetNodes(explainDocument, "zr:explain/zr:indexInfo/zr:set[not(@identifier='" + setSolr + "')]");
        for (int i = 0; i < schemaInfos.getLength(); i++) {
            Element schema = (Element) schemaInfos.item(i);
            String identifier = DOMUtil.getAttr(schema, "identifier");
            log.info("Adding set " + identifier);

            String name = DOMUtil.getAttr(schema, "name");
            String xquery = "zr:map/zr:name[@set='" + name + "']";

            NodeList indexInfos = GetNodes(explainDocument, "zr:explain/zr:indexInfo/zr:index");
            for (int l = 0; l < indexInfos.getLength(); l++) {
                Element indexInfo = (Element) indexInfos.item(l);

                NodeList indices = GetNodes(indexInfo, xquery);
                for (int j = 0; j < indices.getLength(); j++) {
                    String key = indices.item(j).getTextContent();
                    Element element_name = (Element) indices.item(j);

                    for (SolrSRWDatabase.IndexOptions option1 : options) {
                        String option = option1.name();
                        ArrayList list = GetSolrIndices(indexSchema, element_name, option);
                        int index = option.indexOf("_");
                        String alt_option = (index == -1)
                                ? option
                                : option.substring(0, index);

                        Attr attr;
                        if (indexInfo.hasAttribute(alt_option))
                            attr = indexInfo.getAttributeNode(alt_option);
                        else {
                            attr = explainDocument.createAttribute(alt_option);
                            indexInfo.setAttributeNode(attr);
                        }

                        if (list.size() == 0 && !attr.getValue().equals("true"))
                            attr.setValue("false");
                        else {
                            attr.setValue("true");
                            String index_name = name + "." + key;
                            explainMap.put(option + "." + index_name, list);

                            Node title = GetNode(element_name.getParentNode().getParentNode(), "title");
                            if (title != null) {
                                ArrayList title_list = new ArrayList();
                                title_list.add(title.getTextContent());

                                explainMap.put("index.title." + index_name, title_list);
                            }
                        }
                    }
                }
            }
        }
        return explainMap.size();
    }

    /**
     * cacheExplainElements
     *
     * @param explainMap The cache.
     * @param doc        The The explain document.
     * @throws XPathExpressionException
     */
    private static int cacheExplainElements(Map<String, ArrayList> explainMap, Document doc) throws XPathExpressionException {

        final NodeList info = GetNodes(doc, "zr:explain/zr:*");
        for (int i = 0; i < info.getLength(); i++) {
            Element element = (Element) info.item(i);

            NodeList children = element.getChildNodes();
            ArrayList list = new ArrayList(children.getLength());
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element child;
                    try {
                        child = (Element) children.item(j);
                    } catch (Exception e) {
                        log.warn(e);
                        continue;
                    }

                    list.add(XMLUtils.ElementToString(child));
                }
            }
            explainMap.put("explain." + element.getLocalName(), list);
        }
        return info.getLength();
    }

    /**
     * addSolrFieldsToExplainDocument
     * <p/>
     * Whenever there is an element:
     * <set name="solr" identifier="info:srw/cql-context-set/2/solr"/>
     * we add add indexed solr fields to the explain document.
     *
     * @param indexSchema
     * @param explainMap
     * @param doc         The explain document.
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private static int addSolrFieldsToExplainDocument(IndexSchema indexSchema, Map<String, ArrayList> explainMap, Document doc) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

        int count = 0;
        final Node indexInfoSolr = GetNode(doc, "zr:explain/zr:indexInfo/zr:set[@identifier='" + setSolr + "']");
        if (indexInfoSolr != null) {
            Node schemaInfoSolr = GetNode(doc, "zr:explain/zr:schemaInfo/zr:schema[@identifier='" + setSolr + "']");
            if (schemaInfoSolr == null)
                log.info("The explain/indexInfo/set[@identifier='" + setSolr + "'] was declared, but no corresponding output format was defined in the explain/schemaInfo element.");

            Node title = (schemaInfoSolr == null)
                    ? null
                    : GetNode(schemaInfoSolr, "zr:title");

            String Title = (title == null)
                    ? setSolr
                    : title.getTextContent();

            boolean scan = false;
            StringBuilder sb = new StringBuilder();
            for (String fieldName : indexSchema.getFields().keySet()) {
                count++;
                final SchemaField field = indexSchema.getField(fieldName);
                if (field.indexed()) {
                    ArrayList list = new ArrayList(1);
                    list.add(fieldName);
                    explainMap.put("search.solr." + fieldName, list);

                    scan = scan || (field.getType() instanceof StrField || field.getType() instanceof DateField);
                    sb.append("<map><name set='solr'>").append(fieldName).append("</name></map>");
                }
            }
            sb.append("</index>");
            sb.insert(0, "<index search='true' scan='" + scan + "' sort='" + scan + "'><title>" + Title + "</title>");

            // Cast our string to a document
            final ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
            DocumentBuilder db = XMLUtils.getDocumentBuilder();
            Document doc_solr = db.parse(bais);
            XMLUtils.releaseDocumentBuilder(db);

            // Add the document to the explain document.
            Node child = doc.importNode(doc_solr.getDocumentElement(), true);
            Node indexInfo = GetNode(doc, "zr:explain/zr:indexInfo");
            indexInfo.appendChild(child);
        }
        return count;
    }

    /**
     * removeAllSolrSets
     *
     * @param doc The explain document.
     * @return The number of deleted Solr map declarations.
     * @throws XPathExpressionException
     */
    private static int removeAllSolrSets(Document doc) throws XPathExpressionException {

        final NodeList nodelist = GetNodes(doc, "//zr:name[@set='solr']"); // All remaining solr names.
        for (int i = nodelist.getLength() - 1; i != -1; i--) {
            Node child = nodelist.item(i);
            child.getParentNode().removeChild(child);
        }
        return nodelist.getLength();
    }

    /**
     * removeSolrMaps
     * <p/>
     * Remove all solr maps.
     *
     * @param doc The explain document.
     * @return The number of deleted maps.
     * @throws XPathExpressionException
     */
    private static int removeSolrMaps(Document doc) throws XPathExpressionException {

        final NodeList nodelist = GetNodes(doc, "//zr:map[not(zr:name/@set!='solr')]"); // All solr maps, without other schema.
        for (int i = nodelist.getLength() - 1; i != -1; i--) {
            Node child = nodelist.item(i);
            child.getParentNode().removeChild(child);
        }
        return nodelist.getLength();
    }

    /**
     * remoteEmptyIndexes
     * <p/>
     * Removed any empty index elements from the explain document.
     *
     * @param doc The explain document.
     * @return he number of deleted index definitions.
     */
    private static int remoteEmptyIndexes(Document doc) throws XPathExpressionException {
        final NodeList nodelist = GetNodes(doc, "//zr:index[not(zr:map)]");
        for (int i = nodelist.getLength() - 1; i != -1; i--) {
            Node child = nodelist.item(i);
            child.getParentNode().removeChild(child);
        }
        return nodelist.getLength();
    }

    /**
     * removeNonIndexedMaps
     * <p/>
     * Remove all declared map declarations that are in fact not searchable.
     *
     * @param doc The explain document.
     * @throws XPathExpressionException
     */
    private static int removeNonIndexedMaps(Document doc) throws XPathExpressionException {

        final NodeList nodelist = GetNodes(doc, "//zr:index[not(zr:map/zr:name/@set='solr')]/zr:map");
        for (int i = nodelist.getLength() - 1; i != -1; i--) {
            Node child = nodelist.item(i);
            child.getParentNode().removeChild(child);
        }
        return nodelist.getLength();
    }

    private static ArrayList GetSolrIndices(IndexSchema indexSchema, Element name, String option) throws XPathExpressionException {
        ArrayList list = new ArrayList();

        /* Sibling map elements that only relate to a specific index
       <index search="true" scan="true">
           <title>Title</title>
           <map>
               <name set="dc">title</name>
               <name set="solr">marc_245</name> ==> Maps to dc.title
               <name set="solr">marc_246</name> ==> Maps to dc.title aswell
               <name set="solr" search="marc_246" /> ==> Maps to dc.title too
           </map>
        */

        ArrayList<Element> removal = new ArrayList();

        Element map_name = (Element) name.getParentNode();
        NodeList solr_indices = GetNodes(map_name, "zr:name[@set='solr' and @" + option + "]");
        for (int i = 0; i < solr_indices.getLength(); i++) {
            Element map = (Element) solr_indices.item(i);
            boolean match = AddToList(indexSchema, list, map.getAttribute(option));
            if (!match)
                removal.add(map);
        }

        /* Parent map elements that cover all indices mentioned.
            Example:
            <index search="true" scan="false">
                <title>Identifier</title>
                <map><name set="dc">identifier</name></map>
                <map><name set="marc">controlfield.001</name></map>
                <map><name set="solr">marc_controlfield_001</name></map> ==> Maps to dc.identifier and marc.controlfield.001
            </index>
        */

        Element index = (Element) name.getParentNode().getParentNode();
        solr_indices = GetNodes(index, "zr:map[not(zr:name/@set!='solr')]/zr:name[@set='solr' and @" + option + "]");
        for (int i = 0; i < solr_indices.getLength(); i++) {
            Element map = (Element) solr_indices.item(i);
            boolean match = AddToList(indexSchema, list, map.getAttribute(option));
            if (!match)
                removal.add(map);
        }

        for (int i = removal.size() - 1; i != -1; i--) {
            Element element = removal.get(i);
            element.getParentNode().removeChild(element);
        }

        return list;
    }

    private static boolean AddToList(IndexSchema schema, ArrayList list, String indexname) {

        if (indexname == null || indexname.isEmpty()) return false;

        if (list.contains(indexname)) return true;

        if (!schema.hasExplicitField(indexname)) {
            log.warn("The Lucene index field '" + indexname + "' is mentioned in the crosswalk explain.xml document, but it is not declared in the core's schema. It will not show up in the explain record and cannot be used for searching.");
            return false;
        }

        if (!schema.getField(indexname).indexed()) {
            log.warn("The Lucene index field '" + indexname + "' is mentioned in the crosswalk but is not indexed. It will not show up in the explain record and cannot be used for searching.");
            return false;
        }

        log.info("Add Lucene index field '" + indexname + "'.");
        return list.add(indexname);
    }

    private static Document GetDOMDocument(File file) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilder db = XMLUtils.getDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        XMLUtils.releaseDocumentBuilder(db);

        return doc;
    }

    private static Node GetNode(Object item, String xquery) throws XPathExpressionException {

        XPathExpression expr = getXPathExpression(xquery);
        return (Node) expr.evaluate(item, XPathConstants.NODE);
    }

    private static NodeList GetNodes(Object item, String xquery) throws XPathExpressionException {

        XPathExpression expr = getXPathExpression(xquery);
        return (NodeList) expr.evaluate(item, XPathConstants.NODESET);
    }

    private static XPathExpression getXPathExpression(String xquery) throws XPathExpressionException {

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        // http://www.ibm.com/developerworks/library/x-javaxpathapi.html
        NamespaceContext ns = new NamespaceContext() {

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

                if (prefix == null)
                    throw new NullPointerException("Null prefix");
                if (prefix.equalsIgnoreCase("zr"))
                    return "http://explain.z3950.org/dtd/2.0/";
                if (prefix.equalsIgnoreCase("srw"))
                    return "http://www.loc.gov/zing/srw/";
                if (prefix.equalsIgnoreCase("xml"))
                    return XMLConstants.XML_NS_URI;
                return XMLConstants.NULL_NS_URI;
            }
        };

        xpath.setNamespaceContext(ns);
        return xpath.compile(xquery);
    }

}