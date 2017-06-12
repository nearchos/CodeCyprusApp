/*
 * Copyright (c) 2016.
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

package org.codecyprus.android_client.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.model.Answer;
import org.codecyprus.android_client.model.Question;
import org.codecyprus.android_client.sync.JsonParseException;
import org.codecyprus.android_client.sync.JsonParser;
import org.codecyprus.android_client.sync.SyncService;
import org.json.JSONException;

import java.util.*;

import static org.codecyprus.android_client.model.Answer.*;

/**
 * @author Nearchos Paspallis
 * 27/12/13 / 15:59.
 */
public class ActivityCurrentQuestion extends Activity
{
    public static final String TAG = "codecyprus";

    private static final IntentFilter intentFilter = new IntentFilter();
    static
    {
        intentFilter.addAction(SyncService.ACTION_CURRENT_QUESTION_COMPLETED);
        intentFilter.addAction(SyncService.ACTION_ANSWER_QUESTION_COMPLETED);
        intentFilter.addAction(SyncService.ACTION_SKIP_QUESTION_COMPLETED);
        intentFilter.addAction(SyncService.ACTION_SCORE_COMPLETED);
    }
    private ProgressReceiver progressReceiver;

    private ActionBar actionBar;

    private Button buttonA;
    private Button buttonB;
    private Button buttonC;
    private Button buttonD;
    private Button buttonSubmit;

    private TextView feedbackTextView;
    private TextView questionTextView;
    private TextView requiresLocationTextView;
    private View mcqButtonsContainer;
    private View textButtonsContainer;
    private EditText textAnswerEditText;

    private Question question = null;

    private InputMethodManager inputMethodManager;

    private LocationManager locationManager;

    private LocationUpdater locationUpdater;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_current_question);

        actionBar = getActionBar();

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationUpdater = new LocationUpdater();

//        scoreTextView = (TextView) findViewById(R.id.activity_current_question_score);
        feedbackTextView = (TextView) findViewById(R.id.activity_current_question_feedback);
        feedbackTextView.setVisibility(View.GONE);
        questionTextView = (TextView) findViewById(R.id.activity_current_question_question);
        requiresLocationTextView = (TextView) findViewById(R.id.activity_current_question_requires_location);
        requiresLocationTextView.setVisibility(View.GONE);
        mcqButtonsContainer = findViewById(R.id.activity_current_question_mcq_buttons);
        textButtonsContainer = findViewById(R.id.activity_current_question_text_buttons);
        textAnswerEditText = (EditText) findViewById(R.id.activity_current_question_edit_text);

        buttonA =  (Button) findViewById(R.id.activity_current_question_button_A);
        buttonA.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitAnswer("A");
            }
        });
        buttonB =  (Button) findViewById(R.id.activity_current_question_button_B);
        buttonB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitAnswer("B");
            }
        });
        buttonC = (Button) findViewById(R.id.activity_current_question_button_C);
        buttonC.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitAnswer("C");
            }
        });
        buttonD = (Button) findViewById(R.id.activity_current_question_button_D);
        buttonD.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitAnswer("D");
            }
        });
        buttonSubmit = (Button) findViewById(R.id.activity_current_question_button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final CharSequence charSequence = textAnswerEditText.getText();
                final String answer = charSequence == null ? "" : charSequence.toString();
                if(answer.isEmpty())
                {
                    Toast.makeText(ActivityCurrentQuestion.this, R.string.Invalid_empty_answer, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    inputMethodManager.hideSoftInputFromWindow(textAnswerEditText.getWindowToken(), 0);
                    submitAnswer(answer);
                }
            }
        });

        progressReceiver = new ProgressReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(R.string.SKIP)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        menu.add(R.string.Scan)
                .setIcon(R.drawable.ic_qrcode_black_48dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(R.string.Score_board)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        else if(getString(R.string.SKIP).equals(item.getTitle()))
        {
            final DialogSkip dialogSkip = new DialogSkip(this);
            dialogSkip.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    if(dialogSkip.isSkip())
                    {
                        skipQuestion();
                    }
                }
            });
            dialogSkip.show();

            return true;
        }
        else if(getString(R.string.Scan).equals(item.getTitle()))
        {
            final IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            intentIntegrator.setPrompt(getString(R.string.Scan_a_QR_code));
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.initiateScan();
            return true;
        }
        else if(getString(R.string.Score_board).equals(item.getTitle()))
        {
            showScoreBoard();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(intentResult != null) {
            if(intentResult.getContents() == null) {
                Log.d("ActivityCurrentQuestion", "Cancelled scan");
                Toast.makeText(this, R.string.Cancelled, Toast.LENGTH_LONG).show();
            } else {
                Log.d("ActivityCurrentQuestion", "Scanned");
                final String scannedText = intentResult.getContents();
                if(question.isMCQ()) {
                    if("A".equalsIgnoreCase(scannedText) ||
                            "B".equalsIgnoreCase(scannedText) ||
                            "C".equalsIgnoreCase(scannedText) ||
                            "D".equalsIgnoreCase(scannedText)) {
                        // todo show dialog?
                        final DialogConfirmMCQ dialogConfirmMCQ = new DialogConfirmMCQ(this, scannedText);
                        dialogConfirmMCQ.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override public void onDismiss(DialogInterface dialog) {
                                if(dialogConfirmMCQ.isSubmit()) {
                                    submitAnswer(scannedText);
                                }
                            }
                        });
                        dialogConfirmMCQ.show();
                    } else {
                        Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    textAnswerEditText.setText(scannedText);
                    textAnswerEditText.selectAll();
                    textAnswerEditText.requestFocus();
                    inputMethodManager.showSoftInput(textAnswerEditText, 0);
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void skipQuestion()
    {
        final Intent skipQuestionIntent = new Intent(this, SyncService.class);
        skipQuestionIntent.setAction(SyncService.ACTION_SKIP_QUESTION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        skipQuestionIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        startService(skipQuestionIntent);
    }

    private String sessionUUID = null;
//    private String locationUUID = null;

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);

        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        mcqButtonsContainer.setVisibility(View.GONE);
        textButtonsContainer.setVisibility(View.GONE);

        final SerializableSession serializableSession = Preferences.getActiveSession(this);
        if(serializableSession == null)
        {
            Toast.makeText(this, R.string.Invalid_session, Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            sessionUUID = serializableSession.getSessionUUID();
            requestCurrentQuestion();

//            locationUUID = serializableSession.getLocationUUID();
            // request normal (i.e. android-based) location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locationUpdater);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locationUpdater);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(progressReceiver);

        locationManager.removeUpdates(locationUpdater);
    }

    private void requestCurrentQuestion()
    {
        final Intent currentQuestionIntent = new Intent(this, SyncService.class);
        currentQuestionIntent.setAction(SyncService.ACTION_CURRENT_QUESTION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        // todo add 'code'?
        currentQuestionIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        startService(currentQuestionIntent);

        requestScore();
    }

    private void requestScore()
    {
        final Intent scoreIntent = new Intent(this, SyncService.class);
        scoreIntent.setAction(SyncService.ACTION_SCORE);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        scoreIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        startService(scoreIntent);
    }

    private void submitAnswer(final String answer)
    {
        // upload answer directly
        uploadAnswer(answer);
    }

    private void uploadAnswer(final String answer)
    {
        buttonA.setEnabled(false);
        buttonB.setEnabled(false);
        buttonC.setEnabled(false);
        buttonD.setEnabled(false);
        buttonSubmit.setEnabled(false);

        final Intent answerQuestionIntent = new Intent(this, SyncService.class);
        answerQuestionIntent.setAction(SyncService.ACTION_ANSWER_QUESTION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        parameters.put("answer", answer.trim());
        answerQuestionIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        startService(answerQuestionIntent);
    }

    private class ProgressReceiver extends BroadcastReceiver
    {
        @Override public void onReceive(final Context context, final Intent intent)
        {
            final String payload = (String) intent.getSerializableExtra(SyncService.EXTRA_PAYLOAD);
            setProgressBarIndeterminateVisibility(false);

            if(payload != null)
            {
                Log.d(TAG, "intent.getAction() -> " + intent.getAction());
                try
                {
                    if(SyncService.ACTION_CURRENT_QUESTION_COMPLETED.equals(intent.getAction()))
                    {
                        question = JsonParser.parseCurrentQuestion(payload);
                    }
                    else if(SyncService.ACTION_ANSWER_QUESTION_COMPLETED.equals(intent.getAction()))
                    {
                        feedbackTextView.setVisibility(View.VISIBLE);
                        final Answer answer = JsonParser.parseAnswerQuestion(payload);
                        if(answer == INCORRECT)
                        {
                            feedbackTextView.setTextColor(getResources().getColor(R.color.red));
                            feedbackTextView.setText(getString(R.string.Incorrect));
                            Toast.makeText(context, R.string.Incorrect, Toast.LENGTH_SHORT).show();
                            requestScore();
                        }
                        else if(answer == UNKNOWN_OR_INCORRECT_LOCATION)
                        {
                            feedbackTextView.setTextColor(getResources().getColor(R.color.red));
                            feedbackTextView.setText(getString(R.string.Unknown_or_incorrect_location));
                            Toast.makeText(context, R.string.Unknown_or_incorrect_location, Toast.LENGTH_SHORT).show();
                            requestScore();
                        }
                        else if(answer == CORRECT_UNFINISHED)
                        {
                            feedbackTextView.setTextColor(getResources().getColor(R.color.green));
                            feedbackTextView.setText(getString(R.string.Correct));
                            Toast.makeText(context, R.string.Correct, Toast.LENGTH_SHORT).show();
                            requestCurrentQuestion();
                        }
                        else if(answer == CORRECT_FINISHED)
                        {
                            feedbackTextView.setTextColor(getResources().getColor(R.color.green));
                            feedbackTextView.setText(getString(R.string.Correct_finished));
                            Toast.makeText(context, R.string.Correct_finished, Toast.LENGTH_SHORT).show();
                            // remove from saved sessions in prefs
                            Preferences.clearActiveSession(context);
                            showScoreBoard();
                            // remove this activity from call stack
                            finish();
                        }
                    }
                    else if(SyncService.ACTION_SKIP_QUESTION_COMPLETED.equals(intent.getAction()))
                    {
                        boolean hasMoreQuestions = JsonParser.parseSkipQuestion(payload);
                        if(hasMoreQuestions)
                        {
                            Toast.makeText(context, R.string.Skipped_has_more_questions, Toast.LENGTH_SHORT).show();
                            feedbackTextView.setVisibility(View.GONE);
                            requestCurrentQuestion();
                        }
                        else
                        {
                            Toast.makeText(context, R.string.Skipped_finished, Toast.LENGTH_SHORT).show();
                            // remove from saved sessions in prefs
                            Preferences.clearActiveSession(context);
                            showScoreBoard();
                            // remove this activity from call stack
                            finish();
                        }
                    }
                    else if(SyncService.ACTION_SCORE_COMPLETED.equals(intent.getAction()))
                    {
                        final int score = JsonParser.parseScore(payload);
//                        scoreTextView.setText(getString(R.string.Score_is, score));
                        setTitle(getString(R.string.Score, score));
                    }
                    else if(SyncService.ACTION_UPDATE_LOCATION_COMPLETED.equals(intent.getAction()))
                    {
                        // no need to do anything really, except show an error if any (will be handled via the try-catch block)
                        JsonParser.parseUpdateLocation(payload);
                    }

                    // update the UI
                    updateUI();
                }
                catch (JsonParseException jsonpe)
                {
                    Log.e(TAG, jsonpe.getMessage());
                    new DialogError(context, jsonpe.getMessage()).show();
                }
                catch (JSONException jsone)
                {
                    Log.e(TAG, jsone.getMessage());
                }
            }
        }
    }

    private void updateUI()
    {
        buttonA.setEnabled(true);
        buttonB.setEnabled(true);
        buttonC.setEnabled(true);
        buttonD.setEnabled(true);
        buttonSubmit.setEnabled(true);

        assert question != null;
        String questionText = question.getQuestion();
        if(question.isMCQ()) {
            questionText = questionText.substring(4).trim();
            mcqButtonsContainer.setVisibility(View.VISIBLE);
            textButtonsContainer.setVisibility(View.GONE);
            inputMethodManager.hideSoftInputFromWindow(textAnswerEditText.getWindowToken(), 0);
        } else {
            mcqButtonsContainer.setVisibility(View.GONE);
            textButtonsContainer.setVisibility(View.VISIBLE);
            textAnswerEditText.selectAll();
            textAnswerEditText.requestFocus();
            inputMethodManager.showSoftInput(textAnswerEditText, 0);
        }
        questionTextView.setText(Html.fromHtml(questionText));
        requiresLocationTextView.setVisibility(question.isLocationRelevant() ? View.VISIBLE : View.GONE);
    }

    private void showScoreBoard()
    {
        final Intent startActivityScoreBoardIntent = new Intent(this, ActivityScoreBoard.class);
        startActivityScoreBoardIntent.putExtra("session", sessionUUID);
        startActivity(startActivityScoreBoardIntent);
    }

    private void updateLocation(final double lat, final double lng)
    {
        final Intent updateLocationIntent = new Intent(ActivityCurrentQuestion.this, SyncService.class);
        updateLocationIntent.setAction(SyncService.ACTION_UPDATE_LOCATION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        parameters.put("lat", Double.toString(lat));
        parameters.put("lng", Double.toString(lng));
        updateLocationIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        startService(updateLocationIntent);
    }

    private class LocationUpdater implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location)
        {
            final double lat = location.getLatitude();
            final double lng = location.getLongitude();
            Log.d(TAG, "location: " + lat + ", " + lng);

            // call sync service to update location
            updateLocation(lat, lng);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d(TAG, "onStatusChanged: " + provider + ", status: " + status);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }
    }
}