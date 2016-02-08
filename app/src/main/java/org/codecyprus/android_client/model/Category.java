/*
 * This file is part of UCLan-THC server.
 *
 *     UCLan-THC server is free software: you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License as
 *     published by the Free Software Foundation, either version 3 of
 *     the License, or (at your option) any later version.
 *
 *     UCLan-THC server is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.codecyprus.android_client.model;

import java.io.Serializable;

/**
 * User: Nearchos Paspallis
 * Date: 11/09/13
 * Time: 10:06
 */
public class Category implements Serializable, Comparable
{
    private String uuid;
    private String name;
    private String locationUUID;
    private String validFrom;
    private String validUntil;

    public Category(final String uuid, final String name, final String locationUUID, final String validFrom, final String validUntil)
    {
        this.uuid = uuid;
        this.name = name;
        this.locationUUID = locationUUID;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public String getUUID()
    {
        return uuid;
    }

    public String getName()
    {
        return name;
    }

    public String getLocationUUID()
    {
        return locationUUID;
    }

    public String getValidFrom()
    {
        return validFrom;
    }

    public String getValidUntil()
    {
        return validUntil;
    }

    @Override
    public int compareTo(Object another)
    {
        return name.compareTo(((Category) another).getName());
    }

    @Override
    public String toString()
    {
        return getName();
    }
}