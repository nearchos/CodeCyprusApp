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

package org.codecyprus.android_client.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * @author Nearchos Paspallis on 22/12/13.
 */
public class SyncService extends IntentService
{
    public static final String TAG = "uclan-thc";
    public static final String NAME = "SyncService";

    public static final String ACTION_CATEGORIES = "Categories";
    public static final String ACTION_CATEGORIES_COMPLETED = "CategoriesCompleted";

    public static final String ACTION_START_QUIZ = "StartQuiz";
    public static final String ACTION_START_QUIZ_COMPLETED = "StartQuizCompleted";

    public static final String ACTION_CURRENT_QUESTION = "CurrentQuestion";
    public static final String ACTION_CURRENT_QUESTION_COMPLETED = "CurrentQuestionCompleted";

    public static final String ACTION_ANSWER_QUESTION = "AnswerQuestion";
    public static final String ACTION_ANSWER_QUESTION_COMPLETED = "AnswerQuestionCompleted";

    public static final String ACTION_SKIP_QUESTION = "SkipQuestion";
    public static final String ACTION_SKIP_QUESTION_COMPLETED = "SkipQuestionCompleted";

    public static final String ACTION_SCORE = "Score";
    public static final String ACTION_SCORE_COMPLETED = "ScoreCompleted";

    public static final String ACTION_SCORE_BOARD = "ScoreBoard";
    public static final String ACTION_SCORE_BOARD_COMPLETED = "ScoreBoardCompleted";

    public static final String ACTION_UPDATE_LOCATION = "UpdateLocation";
    public static final String ACTION_UPDATE_LOCATION_COMPLETED = "UpdateLocationCompleted";

    public static final String EXTRA_PARAMETERS     = "parameters";
    public static final String EXTRA_PAYLOAD        = "payload";
    public static final String EXTRA_ERROR_MESSAGE  = "error-message";

    public static final String BASE_JSON_URL                    = "http://uclan-thc.appspot.com/api/json";
    public static final String CATEGORIES_JSON_URL              = BASE_JSON_URL + "/categories";
    public static final String START_QUIZ_JSON_URL              = BASE_JSON_URL + "/startQuiz";
    public static final String CURRENT_QUESTION_JSON_URL        = BASE_JSON_URL + "/currentQuestion";
    public static final String ANSWER_QUESTION_JSON_URL         = BASE_JSON_URL + "/answerQuestion";
    public static final String SKIP_QUESTION_JSON_URL           = BASE_JSON_URL + "/skipQuestion";
    public static final String SCORE_JSON_URL                   = BASE_JSON_URL + "/score";
    public static final String SCORE_BOARD_JSON_URL             = BASE_JSON_URL + "/scoreBoard";
    public static final String UPDATE_LOCATION_JSON_URL         = BASE_JSON_URL + "/updateLocation";

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
            if(ACTION_CATEGORIES.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_CATEGORIES);
                reply = get(CATEGORIES_JSON_URL, parameters);
                notify(ACTION_CATEGORIES_COMPLETED, reply);
            }
            else if(ACTION_START_QUIZ.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_START_QUIZ);
                reply = get(START_QUIZ_JSON_URL, parameters);
                notify(ACTION_START_QUIZ_COMPLETED, reply);
            }
            else if(ACTION_CURRENT_QUESTION.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_CURRENT_QUESTION);
                reply = get(CURRENT_QUESTION_JSON_URL, parameters);
                notify(ACTION_CURRENT_QUESTION_COMPLETED, reply);
            }
            else if(ACTION_ANSWER_QUESTION.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_ANSWER_QUESTION);
                reply = get(ANSWER_QUESTION_JSON_URL, parameters);
                notify(ACTION_ANSWER_QUESTION_COMPLETED, reply);
            }
            else if(ACTION_SKIP_QUESTION.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_SKIP_QUESTION);
                reply = get(SKIP_QUESTION_JSON_URL, parameters);
                notify(ACTION_SKIP_QUESTION_COMPLETED, reply);
            }
            else if(ACTION_SCORE.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_SCORE);
                reply = get(SCORE_JSON_URL, parameters);
                notify(ACTION_SCORE_COMPLETED, reply);
            }
            else if(ACTION_SCORE_BOARD.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_SCORE_BOARD);
                reply = get(SCORE_BOARD_JSON_URL, parameters);
                notify(ACTION_SCORE_BOARD_COMPLETED, reply);
            }
            else if(ACTION_UPDATE_LOCATION.equals(intent.getAction()))
            {
                Log.d(TAG, "SyncService: " + ACTION_UPDATE_LOCATION);
                reply = get(UPDATE_LOCATION_JSON_URL, parameters);
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
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
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
        if(parameters == null || parameters.isEmpty())
        {
            return baseUrl;
        }
        else
        {
            final int numOfParameters = parameters.size();
            int count = 0;

            final StringBuilder url = new StringBuilder(baseUrl);
            url.append("?");
            final Set<String> keys = parameters.keySet();
            for(final String key : keys)
            {
                url.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8"));
                if(count++ < numOfParameters - 1) url.append("&");
            }

            return url.toString();
        }
    }

    private void notify(final String action, final Serializable payload)
    {
        final Intent intent = new Intent(action);
        if(payload != null) intent.putExtra(EXTRA_PAYLOAD, payload);
        sendBroadcast(intent);
    }
}