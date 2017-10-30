
/*
 * OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
 *
 *     Copyright (c) 2011-2017  International Institute of Social History
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IdentifyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IdentifyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="baseURL" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="protocolVersion" type="{http://www.openarchives.org/OAI/2.0/}protocolVersionType"/>
 *         &lt;element name="adminEmail" type="{http://www.openarchives.org/OAI/2.0/}emailType" maxOccurs="unbounded"/>
 *         &lt;element name="earliestDatestamp" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType"/>
 *         &lt;element name="deletedRecord" type="{http://www.openarchives.org/OAI/2.0/}deletedRecordType"/>
 *         &lt;element name="granularity" type="{http://www.openarchives.org/OAI/2.0/}granularityType"/>
 *         &lt;element name="compression" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.openarchives.org/OAI/2.0/}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifyType", namespace = "http://www.openarchives.org/OAI/2.0/", propOrder = {
    "repositoryName",
    "baseURL",
    "protocolVersion",
    "adminEmail",
    "earliestDatestamp",
    "deletedRecord",
    "granularity",
    "compression",
    "description"
})
public class IdentifyType {

    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected String repositoryName;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String baseURL;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected String protocolVersion;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected List<String> adminEmail;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected String earliestDatestamp;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected DeletedRecordType deletedRecord;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/", required = true)
    protected GranularityType granularity;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/")
    protected List<String> compression;
    @XmlElement(namespace = "http://www.openarchives.org/OAI/2.0/")
    protected List<DescriptionType> description;

    /**
     * Gets the value of the repositoryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the value of the repositoryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryName(String value) {
        this.repositoryName = value;
    }

    /**
     * Gets the value of the baseURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Sets the value of the baseURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseURL(String value) {
        this.baseURL = value;
    }

    /**
     * Gets the value of the protocolVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the value of the protocolVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocolVersion(String value) {
        this.protocolVersion = value;
    }

    /**
     * Gets the value of the adminEmail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adminEmail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdminEmail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAdminEmail() {
        if (adminEmail == null) {
            adminEmail = new ArrayList<String>();
        }
        return this.adminEmail;
    }

    /**
     * Gets the value of the earliestDatestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEarliestDatestamp() {
        return earliestDatestamp;
    }

    /**
     * Sets the value of the earliestDatestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEarliestDatestamp(String value) {
        this.earliestDatestamp = value;
    }

    /**
     * Gets the value of the deletedRecord property.
     * 
     * @return
     *     possible object is
     *     {@link DeletedRecordType }
     *     
     */
    public DeletedRecordType getDeletedRecord() {
        return deletedRecord;
    }

    /**
     * Sets the value of the deletedRecord property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeletedRecordType }
     *     
     */
    public void setDeletedRecord(DeletedRecordType value) {
        this.deletedRecord = value;
    }

    /**
     * Gets the value of the granularity property.
     * 
     * @return
     *     possible object is
     *     {@link GranularityType }
     *     
     */
    public GranularityType getGranularity() {
        return granularity;
    }

    /**
     * Sets the value of the granularity property.
     * 
     * @param value
     *     allowed object is
     *     {@link GranularityType }
     *     
     */
    public void setGranularity(GranularityType value) {
        this.granularity = value;
    }

    /**
     * Gets the value of the compression property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compression property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompression().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCompression() {
        if (compression == null) {
            compression = new ArrayList<String>();
        }
        return this.compression;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DescriptionType }
     * 
     * 
     */
    public List<DescriptionType> getDescription() {
        if (description == null) {
            description = new ArrayList<DescriptionType>();
        }
        return this.description;
    }

}
