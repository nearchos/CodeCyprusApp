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
 * Time: 13:42
 */
public class Question implements Serializable
{
    private final String question;
    private final boolean isLocationRelevant;

    public Question(final String question,
                    final boolean isLocationRelevant)
    {
        this.question = question;
        this.isLocationRelevant = isLocationRelevant;
    }

    public String getQuestion()
    {
        return question;
    }

    public boolean isLocationRelevant()
    {
        return isLocationRelevant;
    }
}