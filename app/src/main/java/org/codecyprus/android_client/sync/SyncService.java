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

package org.codecyprus.android_client.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * @author Nearchos Paspallis on 22/12/13.
 */
public class SyncService extends IntentService
{
    public static final String TAG = "uclan-thc";
    public static final String NAME = "SyncService";

    public static final String ACTION_LIST = "ListTreasureHunts";
    public static final String ACTION_LIST_COMPLETED = "ListTreasureHuntsCompleted";

    public static final String ACTION_START = "StartTreasureHunt";
    public static final String ACTION_START_COMPLETED = "StartTreasureHuntCompleted";

    public static final String ACTION_QUESTION = "CurrentQuestion";
    public static final String ACTION_QUESTION_COMPLETED = "CurrentQuestionCompleted";

    public static final String ACTION_ANSWER = "AnswerQuestion";
    public static final String ACTION_ANSWER_COMPLETED = "AnswerQuestionCompleted";

    public static final String ACTION_SKIP = "SkipQuestion";
    public static final String ACTION_SKIP_COMPLETED = "SkipQuestionCompleted";

    public static final String ACTION_SCORE = "Score";
    public static final String ACTION_SCORE_COMPLETED = "ScoreCompleted";

    public static final String ACTION_LEADER_BOARD = "LeaderBoard";
    public static final String ACTION_LEADER_BOARD_COMPLETED = "LeaderBoardCompleted";

    public static final String ACTION_UPDATE_LOCATION = "UpdateLocation";
    public static final String ACTION_UPDATE_LOCATION_COMPLETED = "UpdateLocationCompleted";

    public static final String EXTRA_PARAMETERS     = "parameters";
    public static final String EXTRA_PAYLOAD        = "payload";
    public static final String EXTRA_ERROR_MESSAGE  = "error-message";

    public static final String BASE_URL         = "https://codecyprus.org/th/api";
    public static final String LIST_URL         = BASE_URL + "/list";
    public static final String START_URL        = BASE_URL + "/start";
    public static final String QUESTION_URL     = BASE_URL + "/question";
    public static final String ANSWER_URL       = BASE_URL + "/answer";
    public static final String SKIP_URL         = BASE_URL + "/skip";
    public static final String SCORE_URL        = BASE_URL + "/score";
    public static final String LEADER_BOARD_URL = BASE_URL + "/leaderboard";
    public static final String LOCATION_URL     = BASE_URL + "/location";

    public SyncService()
    {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        final Map<String,String> parameters = (Map<String, String>) intent.getSerializableExtra(EXTRA_PARAMETERS);
        final String reply;

        try
        {
            if(ACTION_LIST.equals(intent.getAction())) { // todo transform action to enum
                Log.d(TAG, "SyncService: " + ACTION_LIST);
                reply = get(LIST_URL, parameters);
                notify(ACTION_LIST_COMPLETED, reply);
            } else if(ACTION_START.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_START);
                reply = get(START_URL, parameters);
                notify(ACTION_START_COMPLETED, reply);
            } else if(ACTION_QUESTION.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_QUESTION);
                reply = get(QUESTION_URL, parameters);
                notify(ACTION_QUESTION_COMPLETED, reply);
            } else if(ACTION_ANSWER.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_ANSWER);
                reply = get(ANSWER_URL, parameters);
                notify(ACTION_ANSWER_COMPLETED, reply);
            } else if(ACTION_SKIP.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_SKIP);
                reply = get(SKIP_URL, parameters);
                notify(ACTION_SKIP_COMPLETED, reply);
            } else if(ACTION_SCORE.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_SCORE);
                reply = get(SCORE_URL, parameters);
                notify(ACTION_SCORE_COMPLETED, reply);
            } else if(ACTION_LEADER_BOARD.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_LEADER_BOARD);
                reply = get(LEADER_BOARD_URL, parameters);
                notify(ACTION_LEADER_BOARD_COMPLETED, reply);
            } else if(ACTION_UPDATE_LOCATION.equals(intent.getAction())) {
                Log.d(TAG, "SyncService: " + ACTION_UPDATE_LOCATION);
                reply = get(LOCATION_URL, parameters);
                notify(ACTION_UPDATE_LOCATION_COMPLETED, reply);
            }
        }
        catch (IOException ioe)
        {
            Log.e(TAG, ioe.getMessage());
        }
    }

    private String get(final String baseUrl, final Map<String,String> parameters)
            throws IOException
    {
        final String url = getUrlWithParameters(baseUrl, parameters);

        // send get
        final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
        httpURLConnection.setRequestProperty("Content-Type", "text/json;charset=UTF-8");

        final int responseCode = httpURLConnection.getResponseCode();
        if(responseCode != 200)
            throw new IOException("The service returned error code: " + responseCode);

        final InputStream inputStream = httpURLConnection.getInputStream();

        // handle httpResponse
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), 8);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(bufferedReader.readLine()).append("\n");
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(line).append("\n");
        }

        inputStream.close();
        return stringBuilder.toString();
    }

    private String getUrlWithParameters(final String baseUrl, final Map<String,String> parameters)
            throws UnsupportedEncodingException
    {
        if(parameters == null || parameters.isEmpty()) {
            return baseUrl;
        } else {
            final int numOfParameters = parameters.size();
            int count = 0;

            final StringBuilder url = new StringBuilder(baseUrl);
            url.append("?");
            final Set<String> keys = parameters.keySet();
            for(final String key : keys) {
                url.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8"));
                if(count++ < numOfParameters - 1) url.append("&");
            }

            return url.toString();
        }
    }

    private void notify(final String action, final Serializable payload) {
        final Intent intent = new Intent(action);
        if(payload != null) intent.putExtra(EXTRA_PAYLOAD, payload);
        sendBroadcast(intent);
    }
}