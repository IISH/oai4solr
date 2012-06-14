/*
 * OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
 *
 *     Copyright (C) 2011  International Institute of Social History
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

package org.socialhistoryservices.api.oai;

import org.openarchives.oai2.RequestType;

/**
 * Class for constructing the resumptionToken
 *
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
public class ResumptionToken extends RequestType {

    private int cursor = 0;

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }
}
