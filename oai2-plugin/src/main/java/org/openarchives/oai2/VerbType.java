
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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for verbType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="verbType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Identify"/>
 *     &lt;enumeration value="ListMetadataFormats"/>
 *     &lt;enumeration value="ListSets"/>
 *     &lt;enumeration value="GetRecord"/>
 *     &lt;enumeration value="ListIdentifiers"/>
 *     &lt;enumeration value="ListRecords"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "verbType", namespace = "http://www.openarchives.org/OAI/2.0/")
@XmlEnum
public enum VerbType {

    @XmlEnumValue("Identify")
    IDENTIFY("Identify"),
    @XmlEnumValue("ListMetadataFormats")
    LIST_METADATA_FORMATS("ListMetadataFormats"),
    @XmlEnumValue("ListSets")
    LIST_SETS("ListSets"),
    @XmlEnumValue("GetRecord")
    GET_RECORD("GetRecord"),
    @XmlEnumValue("ListIdentifiers")
    LIST_IDENTIFIERS("ListIdentifiers"),
    @XmlEnumValue("ListRecords")
    LIST_RECORDS("ListRecords");
    private final String value;

    VerbType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VerbType fromValue(String v) {
        for (VerbType c: VerbType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
