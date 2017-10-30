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
class Parameters {

    final private static HashMap<String, Object> store = new HashMap<String, Object>();


    static OAIPMHtype getParam(int prefix, VerbType verb) {
        return (OAIPMHtype) store.get(prefix + "$" + verb.value());
    }

    static void setParam(VerbType verb, Object def) {
        setParam(0, verb.value(), def);
    }

    static void setParam(int prefix, VerbType verb, Object def) {
        setParam(prefix, verb.value(), def);
    }

    static Object getParam(String key) {
        return getParam(key, null);
    }

    static Object getParam(String key, Object def) {
        return getParam(0, key, def);
    }

    static Object getParam(int prefix, String key) {
        return getParam(prefix, key, null);
    }

    static Object getParam(int prefix, String key, Object def) {
        Object o = store.get(prefix + "$" + key);
        return (o == null) ? def : o;
    }

    static Boolean getBool(int prefix, String key, Boolean def) {
        Object o = store.get(prefix + "$" + key);
        return (o == null) ? def : (Boolean) o;
    }

    static void setParam(int prefix, NamedList args, String key, Object def) {
        Object value = args.get(key);
        if (value == null)
            value = def;
        setParam(prefix, key, value);
    }

    static void setParam(String key, Object o) {
        setParam(0, key, o);
    }

    static void setParam(int prefix, String key, Object o) {
        if (o == null)
            store.remove(prefix + "$" + key);
        else
            store.put(prefix + "$" + key, o);
    }

    static void clearParams() {
        store.clear();
    }
}
