/*
 * Copyright (c) 2017.
 *
 * This file is part of the Code Cyprus App.
 *
 * The Code Cyprus App is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 *  Code Cyprus App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Code Cyprus App. If not, see <http://www.gnu.org/licenses/>.
 */

package org.codecyprus.android_client;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * @author Nearchos Paspallis
 * 27/12/13
 */
public class SerializableSession implements Serializable
{
    public static final String TAG = "codecyprus";

    private final String categoryUUID;
    private final String categoryName;
    private final String locationUUID;
    private final String sessionUUID;
    private final String teamName;
    private final String name1;
    private final String email1;
    private final String name2;
    private final String email2;

    public SerializableSession(String categoryUUID,
                               String categoryName,
                               String locationUUID,
                               String sessionUUID,
                               final String teamName,
                               String name1,
                               String email1,
                               String name2,
                               String email2)
    {
        this.categoryUUID = categoryUUID;
        this.categoryName = categoryName;
        this.locationUUID = locationUUID;
        this.sessionUUID = sessionUUID;
        this.teamName = teamName;
        this.name1 = name1;
        this.email1 = email1;
        this.name2 = name2;
        this.email2 = email2;
    }

    public String getCategoryUUID()
    {
        return categoryUUID;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public String getLocationUUID()
    {
        return locationUUID;
    }

    public String getSessionUUID()
    {
        return sessionUUID;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public String getName1()
    {
        return name1;
    }

    public String getEmail1()
    {
        return email1;
    }

    public String getName2()
    {
        return name2;
    }

    public String getEmail2()
    {
        return email2;
    }

    static public String toJSON(final SerializableSession session)
    {
        return new StringBuilder("{")
                .append("\"categoryUUID\": \"").append(session.categoryUUID).append("\",")
                .append("\"categoryName\": \"").append(session.categoryName).append("\",")
                .append("\"locationUUID\": \"").append(session.locationUUID).append("\",")
                .append("\"sessionUUID\": \"").append(session.sessionUUID).append("\",")
                .append("\"teamName\": \"").append(session.teamName).append("\",")
                .append("\"name1\": \"").append(session.name1).append("\",")
                .append("\"email1\": \"").append(session.email1).append("\",")
                .append("\"name2\": \"").append(session.name2).append("\",")
                .append("\"email2\": \"").append(session.email2).append("\" }")
                .toString();
    }

    static public SerializableSession fromJSON(final String json)
    {
        try
        {
            final JSONObject jsonObject = new JSONObject(json);
            return new SerializableSession(jsonObject.getString("categoryUUID"),
                    jsonObject.getString("categoryName"),
                    jsonObject.getString("locationUUID"),
                    jsonObject.getString("sessionUUID"),
                    jsonObject.getString("teamName"),
                    jsonObject.getString("name1"),
                    jsonObject.getString("email1"),
                    jsonObject.getString("name2"),
                    jsonObject.getString("email2"));
        }
        catch (JSONException jsone)
        {
            Log.e(TAG, jsone.getMessage());
            return null;
        }
    }
}