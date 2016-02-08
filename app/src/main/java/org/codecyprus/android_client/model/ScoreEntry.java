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
 * @author Nearchos Paspallis
 * 31/12/13 / 08:48
 */
public class ScoreEntry implements Serializable
{
    private final String appID;
    private final String playerName;
    private final int score;
    private final long finishTime;

    public ScoreEntry(final String appID, final String playerName, final int score, final long finishTime)
    {
        this.appID = appID;
        this.playerName = playerName;
        this.score = score;
        this.finishTime = finishTime;
    }

    public String getAppID()
    {
        return appID;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public int getScore()
    {
        return score;
    }

    public long getFinishTime()
    {
        return finishTime;
    }
}