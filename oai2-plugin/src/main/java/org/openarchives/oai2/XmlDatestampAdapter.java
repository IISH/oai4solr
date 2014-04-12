package org.openarchives.oai2;

import org.socialhistoryservices.api.oai.Utils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * XmlDatestampAdapter
 */
public class XmlDatestampAdapter extends XmlAdapter<String, XMLGregorianCalendar> {

    @Override
    public XMLGregorianCalendar unmarshal(String v) throws Exception {
        return Utils.getGregorianDate(v)  ;
    }

    @Override
    public String marshal(XMLGregorianCalendar date) throws Exception {
       return Utils.parseGregorianDate(date)   ;
    }

}
