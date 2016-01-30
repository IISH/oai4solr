
/*
 * OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
 *
 *     Copyright (c) 2011-2014  International Institute of Social History
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openarchives.oai2;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for OAI-PMHtype complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OAI-PMHtype">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="responseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="request" type="{http://www.openarchives.org/OAI/2.0/}requestType"/>
 *         &lt;choice>
 *           &lt;element name="error" type="{http://www.openarchives.org/OAI/2.0/}OAI-PMHerrorType" maxOccurs="unbounded"/>
 *           &lt;element name="Identify" type="{http://www.openarchives.org/OAI/2.0/}IdentifyType"/>
 *           &lt;element name="ListMetadataFormats" type="{http://www.openarchives.org/OAI/2.0/}ListMetadataFormatsType"/>
 *           &lt;element name="ListSets" type="{http://www.openarchives.org/OAI/2.0/}ListSetsType"/>
 *           &lt;element name="GetRecord" type="{http://www.openarchives.org/OAI/2.0/}GetRecordType"/>
 *           &lt;element name="ListIdentifiers" type="{http://www.openarchives.org/OAI/2.0/}ListIdentifiersType"/>
 *           &lt;element name="ListRecords" type="{http://www.openarchives.org/OAI/2.0/}ListRecordsType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OAI-PMHtype", namespace = "http://www.openarchives.org/OAI/2.0/", propOrder = {
    "responseDate",
    "request",
    "error",
    "identify",
    "listMetadataFormats",
    "listSets",
    "getRecord",
    "listIdentifiers",
    "listRecords"
})
public class OAIPMHtype {

    // We hardcode the constant xsi namespace and schemaLocation here.
    @XmlAttribute(name = "xmlns:xsi", namespace = "")
    protected String xmlns_xsi = "http://www.w3.org/2001/XMLSchema-instance";
    @XmlAttribute(name = "xsi:schemaLocation", namespace = "")
    protected String xsi_schemaLocation = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";

    // Ensure the responseDate value is parsed correctly with a XmlDatestampAdapter
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    @XmlJavaTypeAdapter(XmlDatestampAdapter.class)
    protected XMLGregorianCalendar responseDate;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected RequestType request;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/")
    protected List<OAIPMHerrorType> error;
    @XmlElement(name = "Identify", namespace = "http://www.openarchives.org/OAI/2.0/")
    protected IdentifyType identify;
    @XmlElement(name = "ListMetadataFormats", namespace = "http://www.openarchives.org/OAI/2.0/")
    protected ListMetadataFormatsType listMetadataFormats;
    @XmlElement(name = "ListSets", namespace = "http://www.openarchives.org/OAI/2.0/")
    protected ListSetsType listSets;
    @XmlElement(name = "GetRecord", namespace = "http://www.openarchives.org/OAI/2.0/")
    protected GetRecordType getRecord;
    @XmlElement(name = "ListIdentifiers", namespace = "http://www.openarchives.org/OAI/2.0/")
    protected ListIdentifiersType listIdentifiers;
    @XmlElement(name = "ListRecords", namespace = "http://www.openarchives.org/OAI/2.0/")
    protected ListRecordsType listRecords;

    /**
     * Gets the value of the responseDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getResponseDate() {
        return responseDate;
    }

    /**
     * Sets the value of the responseDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setResponseDate(XMLGregorianCalendar value) {
        this.responseDate = value;
    }

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link RequestType }
     *     
     */
    public RequestType getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestType }
     *     
     */
    public void setRequest(RequestType value) {
        this.request = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the error property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OAIPMHerrorType }
     * 
     * 
     */
    public List<OAIPMHerrorType> getError() {
        if (error == null) {
            error = new ArrayList<OAIPMHerrorType>();
        }
        return this.error;
    }

    /**
     * Gets the value of the identify property.
     * 
     * @return
     *     possible object is
     *     {@link IdentifyType }
     *     
     */
    public IdentifyType getIdentify() {
        return identify;
    }

    /**
     * Sets the value of the identify property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifyType }
     *     
     */
    public void setIdentify(IdentifyType value) {
        this.identify = value;
    }

    /**
     * Gets the value of the listMetadataFormats property.
     * 
     * @return
     *     possible object is
     *     {@link ListMetadataFormatsType }
     *     
     */
    public ListMetadataFormatsType getListMetadataFormats() {
        return listMetadataFormats;
    }

    /**
     * Sets the value of the listMetadataFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListMetadataFormatsType }
     *     
     */
    public void setListMetadataFormats(ListMetadataFormatsType value) {
        this.listMetadataFormats = value;
    }

    /**
     * Gets the value of the listSets property.
     * 
     * @return
     *     possible object is
     *     {@link ListSetsType }
     *     
     */
    public ListSetsType getListSets() {
        return listSets;
    }

    /**
     * Sets the value of the listSets property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListSetsType }
     *     
     */
    public void setListSets(ListSetsType value) {
        this.listSets = value;
    }

    /**
     * Gets the value of the getRecord property.
     * 
     * @return
     *     possible object is
     *     {@link GetRecordType }
     *     
     */
    public GetRecordType getGetRecord() {
        return getRecord;
    }

    /**
     * Sets the value of the getRecord property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetRecordType }
     *     
     */
    public void setGetRecord(GetRecordType value) {
        this.getRecord = value;
    }

    /**
     * Gets the value of the listIdentifiers property.
     * 
     * @return
     *     possible object is
     *     {@link ListIdentifiersType }
     *     
     */
    public ListIdentifiersType getListIdentifiers() {
        return listIdentifiers;
    }

    /**
     * Sets the value of the listIdentifiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListIdentifiersType }
     *     
     */
    public void setListIdentifiers(ListIdentifiersType value) {
        this.listIdentifiers = value;
    }

    /**
     * Gets the value of the listRecords property.
     * 
     * @return
     *     possible object is
     *     {@link ListRecordsType }
     *     
     */
    public ListRecordsType getListRecords() {
        return listRecords;
    }

    /**
     * Sets the value of the listRecords property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListRecordsType }
     *     
     */
    public void setListRecords(ListRecordsType value) {
        this.listRecords = value;
    }

}
