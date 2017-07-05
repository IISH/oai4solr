package org.socialhistoryservices.api.oai;

import org.apache.solr.common.util.NamedList;
import org.openarchives.oai2.OAIPMHtype;
import org.openarchives.oai2.VerbType;

import java.util.HashMap;

/**
 * Parameters
 * <p/>
 * Im memory datastore for system parameters.
 */
public class Parameters {

    final private static HashMap<String, Object> store = new HashMap<String, Object>();


    public static OAIPMHtype getParam(VerbType verb) {

        return (OAIPMHtype) store.get(verb.value());
    }

    public static void setParam(VerbType verb, Object def) {

        setParam(verb.value(), def);
    }

    public static Object getParam(String key) {

        return store.get(key);
    }

    public static Object getParam(String key, Object def) {

        Object o = store.get(key);
        return (o == null) ? def : o;
    }

    static Boolean getBool(String key, Boolean def) {

        Object o = store.get(key);
        return (o == null) ? def : (Boolean) o;
    }

    static void setParam(NamedList args, String key, Object def) {

        Object value = args.get(key);
        if (value == null)
            value = def;
        setParam(key, value);
    }

    public static void setParam(String key, Object o) {

        if (o == null)
            store.remove(key);
        else
            store.put(key, o);
    }

    public static void clearParams() {
        store.clear();
    }
}
