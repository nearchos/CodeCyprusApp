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

package org.codecyprus.android_client.ui;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.codecyprus.android_client.BuildConfig;
import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.sync.SyncService;
import org.codecyprus.th.model.Replies;
import org.codecyprus.th.model.Status;

import java.text.DateFormat;
import java.util.*;

/**
 * @author Nearchos Paspallis
 * 27/12/13 / 15:59.
 */
public class ActivityQuestion extends Activity
{
    public static final String TAG = "codecyprus";

    private static final IntentFilter intentFilter = new IntentFilter();
    static
    {
        intentFilter.addAction(SyncService.ACTION_QUESTION_COMPLETED);
        intentFilter.addAction(SyncService.ACTION_ANSWER_COMPLETED);
        intentFilter.addAction(SyncService.ACTION_SKIP_COMPLETED);
        intentFilter.addAction(SyncService.ACTION_SCORE_COMPLETED);
    }
    private ProgressReceiver progressReceiver;

    private ActionBar actionBar;

    private Button buttonA;
    private Button buttonB;
    private Button buttonC;
    private Button buttonD;
    private Button buttonTrue;
    private Button buttonFalse;
    private Button buttonSubmit;

    private TextView feedbackTextView;
    private WebView questionTextView;
    private TextView requiresLocationTextView;
    private TextView cannotBeSkippedTextView;
    private View mcqButtonsContainer;
    private View booleanButtonsContainer;
    private View textButtonsContainer;
    private EditText textAnswerEditText;

    private Replies.QuestionReply question = null;

    private InputMethodManager inputMethodManager;

    private ConnectivityManager connectivityManager = null;

    private Gson gson = new Gson();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_current_question);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        actionBar = getActionBar();

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        feedbackTextView = findViewById(R.id.activity_current_question_feedback);
        feedbackTextView.setVisibility(View.GONE);
        questionTextView = findViewById(R.id.activity_current_question_question);
        requiresLocationTextView = findViewById(R.id.activity_current_question_requires_location);
        requiresLocationTextView.setVisibility(View.GONE);
        cannotBeSkippedTextView = findViewById(R.id.activity_current_question_cannot_be_skipped);
        cannotBeSkippedTextView.setVisibility(View.GONE);
        mcqButtonsContainer = findViewById(R.id.activity_current_question_mcq_buttons);
        booleanButtonsContainer = findViewById(R.id.activity_current_question_boolean_buttons);
        textButtonsContainer = findViewById(R.id.activity_current_question_text_buttons);
        textAnswerEditText = findViewById(R.id.activity_current_question_edit_text);

        buttonA = findViewById(R.id.activity_current_question_button_A);
        buttonA.setOnClickListener(v -> tryToSubmitAnswer("A"));
        buttonB = findViewById(R.id.activity_current_question_button_B);
        buttonB.setOnClickListener(v -> tryToSubmitAnswer("B"));
        buttonC = findViewById(R.id.activity_current_question_button_C);
        buttonC.setOnClickListener(v -> tryToSubmitAnswer("C"));
        buttonD = findViewById(R.id.activity_current_question_button_D);
        buttonD.setOnClickListener(v -> tryToSubmitAnswer("D"));
        buttonTrue = findViewById(R.id.activity_current_question_button_True);
        buttonTrue.setOnClickListener(v -> tryToSubmitAnswer("true"));
        buttonFalse = findViewById(R.id.activity_current_question_button_False);
        buttonFalse.setOnClickListener(v -> tryToSubmitAnswer("false"));
        buttonSubmit = findViewById(R.id.activity_current_question_button_submit);
        buttonSubmit.setOnClickListener(v -> {
            final CharSequence charSequence = textAnswerEditText.getText();
            final String answer = charSequence == null ? "" : charSequence.toString();
            if(answer.isEmpty()) {
                Toast.makeText(ActivityQuestion.this, R.string.Invalid_empty_answer, Toast.LENGTH_SHORT).show();
            } else {
                inputMethodManager.hideSoftInputFromWindow(textAnswerEditText.getWindowToken(), 0);
                tryToSubmitAnswer(answer);
            }
        });

        progressReceiver = new ProgressReceiver();

        // Location-specific code
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
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
            if(question.isCanBeSkipped()) {
                final DialogSkip dialogSkip = new DialogSkip(this);
                dialogSkip.setOnDismissListener(dialog -> {
                    if(dialogSkip.isSkip()) {
                        tryToSkipQuestion();
                    }
                });
                dialogSkip.show();
            } else {
                Toast.makeText(this, R.string.Cannot_be_skipped, Toast.LENGTH_SHORT).show();
            }

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

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;

            default: {
                final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if(intentResult != null) {
                    if(intentResult.getContents() == null) {
                        Toast.makeText(this, R.string.Cancelled, Toast.LENGTH_LONG).show();
                    } else {
                        final String scannedText = intentResult.getContents();
                        handleScannedText(scannedText);
                    }
                } else {
                    // This is important, otherwise the result will not be passed to the fragment
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    private void handleScannedText(final String scannedText)
    {
        final boolean isUrl = URLUtil.isValidUrl(scannedText);
        final String message = getString(R.string.Would_you_like_to_open_this_URL, scannedText);
        if(isUrl) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.URL_detected)
                    .setMessage(message)
                    .setPositiveButton(R.string.Open_URL, (dialog, id) -> {
                        dialog.dismiss();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(scannedText)));
                    })
                    .setNegativeButton(R.string.Use_as_answer, (dialog, id) -> {
                        dialog.dismiss();
                        handleScannedTextAsPlainText(scannedText);
                    }).create().show();
        } else {
            handleScannedTextAsPlainText(scannedText);
        }
    }

    private void handleScannedTextAsPlainText(final String scannedText)
    {
        recoverSession();
        tryToRequestCurrentQuestion();
        textAnswerEditText.setText(scannedText);
        textAnswerEditText.selectAll();
        textAnswerEditText.requestFocus();
        inputMethodManager.showSoftInput(textAnswerEditText, 0);
    }

    private void tryToSkipQuestion() {
        final NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            skipQuestion();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.No_Internet)
                    .setMessage(R.string.It_seems_like_you_are_not_connected_to_Internet)
                    .setPositiveButton(R.string.Retry, (dialog, id) -> {
                        dialog.dismiss();
                        tryToSkipQuestion();
                    })
                    .setNegativeButton(R.string.Cancel, (dialog, id) -> dialog.dismiss()).create().show();
        }
    }

    private void skipQuestion()
    {
        final Intent skipQuestionIntent = new Intent(this, SyncService.class);
        skipQuestionIntent.setAction(SyncService.ACTION_SKIP);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        skipQuestionIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        startService(skipQuestionIntent);
    }

    private String sessionUUID = null;

    private String code = "";

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);

        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        mcqButtonsContainer.setVisibility(View.GONE);
        booleanButtonsContainer.setVisibility(View.GONE);
        textButtonsContainer.setVisibility(View.GONE);

        recoverSession();

        updateUI();
    }

    private void recoverSession() {
        final SerializableSession serializableSession = Preferences.getActiveSession(this);
        if(serializableSession == null)
        {
            Toast.makeText(this, R.string.Invalid_session, Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            sessionUUID = serializableSession.getSessionUUID();
            tryToRequestCurrentQuestion();

            // Within {@code onPause()}, we remove location updates. Here, we resume receiving
            // location updates if the user has requested them.
            if (mRequestingLocationUpdates && checkPermissions()) {
                startLocationUpdates();
            } else if (!checkPermissions()) {
                requestPermissions();
            }
        }

        code = PreferenceManager.getDefaultSharedPreferences(this).getString("code", "");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(progressReceiver);

        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    private void tryToRequestCurrentQuestion()
    {
        final NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            requestCurrentQuestion();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.No_Internet)
                    .setMessage(R.string.It_seems_like_you_are_not_connected_to_Internet)
                    .setPositiveButton(R.string.Retry, (dialog, id) -> {
                        dialog.dismiss();
                        tryToRequestCurrentQuestion();
                    })
                    .setNegativeButton(R.string.Cancel, (dialog, id) -> {
                        dialog.dismiss();
                        finish();
                    }).create().show();
        }
    }

    private void requestCurrentQuestion()
    {
        final Intent currentQuestionIntent = new Intent(this, SyncService.class);
        currentQuestionIntent.setAction(SyncService.ACTION_QUESTION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        parameters.put("code", code);
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

    private void tryToSubmitAnswer(final String answer) {
        final NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            submitAnswer(answer);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.No_Internet)
                    .setMessage(R.string.It_seems_like_you_are_not_connected_to_Internet)
                    .setPositiveButton(R.string.Retry, (dialog, id) -> {
                        dialog.dismiss();
                        tryToSubmitAnswer(answer);
                    })
                    .setNegativeButton(R.string.Cancel, (dialog, id) -> dialog.dismiss()).create().show();
        }
    }

    private void submitAnswer(final String answer)
    {
        // upload answer directly
        buttonA.setEnabled(false);
        buttonB.setEnabled(false);
        buttonC.setEnabled(false);
        buttonD.setEnabled(false);
        buttonTrue.setEnabled(false);
        buttonFalse.setEnabled(false);
        buttonSubmit.setEnabled(false);

        final Intent answerQuestionIntent = new Intent(this, SyncService.class);
        answerQuestionIntent.setAction(SyncService.ACTION_ANSWER);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        if(code != null && code.length() > 0) parameters.put("code", code);
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

            if(payload != null) {
                Log.d(TAG, "intent.getAction() -> " + intent.getAction());

                final Replies.Reply reply = gson.fromJson(payload, Replies.Reply.class);
                if(reply.getStatus() == Status.OK) {
                    if(SyncService.ACTION_QUESTION_COMPLETED.equals(intent.getAction()))
                    {
                        question = gson.fromJson(payload, Replies.QuestionReply.class);
                    }
                    else if(SyncService.ACTION_ANSWER_COMPLETED.equals(intent.getAction()))
                    {
                        feedbackTextView.setVisibility(View.VISIBLE);
                        final Replies.AnswerReply answerReply = gson.fromJson(payload, Replies.AnswerReply.class);

                        if(!answerReply.isCorrect()) // answer == INCORRECT
                        {
                            feedbackTextView.setTextColor(getResources().getColor(R.color.red));
                            feedbackTextView.setText(getString(R.string.Incorrect_msg, answerReply.getMessage()));
                            Toast.makeText(context, R.string.Incorrect, Toast.LENGTH_SHORT).show();
                            requestScore();
                        } else {// answer is correct
                            textAnswerEditText.setText(""); // clear text when correct
                            if(!answerReply.isCompleted()) { // if(answer == CORRECT_UNFINISHED)
                                feedbackTextView.setTextColor(getResources().getColor(R.color.green));
                                feedbackTextView.setText(getString(R.string.Correct));
                                textAnswerEditText.clearComposingText();
                                Toast.makeText(context, R.string.Correct, Toast.LENGTH_SHORT).show();
                                tryToRequestCurrentQuestion(); // will get next one
                            } else { // if(answer == CORRECT_FINISHED)
                                feedbackTextView.setTextColor(getResources().getColor(R.color.green));
                                feedbackTextView.setText(getString(R.string.Correct_finished));
                                textAnswerEditText.clearComposingText();
                                Toast.makeText(context, R.string.Correct_finished, Toast.LENGTH_SHORT).show();
                                // remove from saved sessions in prefs
                                Preferences.clearActiveSession(context);
                                showScoreBoard();
                                // remove this activity from call stack
                                finish();
                            }
                        }
                    }
                    else if(SyncService.ACTION_SKIP_COMPLETED.equals(intent.getAction()))
                    {
                        final Replies.SkipReply skipReply = gson.fromJson(payload, Replies.SkipReply.class);
                        boolean hasMoreQuestions = !skipReply.isCompleted(); // JsonParser.parseSkipQuestion(payload);
                        if(hasMoreQuestions) {
                            Toast.makeText(context, R.string.Skipped_has_more_questions, Toast.LENGTH_SHORT).show();
                            feedbackTextView.setVisibility(View.GONE);
                            tryToRequestCurrentQuestion();
                        } else {
                            Toast.makeText(context, R.string.Skipped_finished, Toast.LENGTH_SHORT).show();
                            // remove from saved sessions in prefs
                            Preferences.clearActiveSession(context);
                            showScoreBoard();
                            // remove this activity from call stack
                            finish();
                        }
                    } else if(SyncService.ACTION_SCORE_COMPLETED.equals(intent.getAction())) {
                        final Replies.ScoreReply scoreReply = gson.fromJson(payload, Replies.ScoreReply.class);
                        final long score = scoreReply.getScore(); // JsonParser.parseScore(payload);
//                        scoreTextView.setText(getString(R.string.Score_is, score));
                        setTitle(getString(R.string.Score, score));
                    } else if(SyncService.ACTION_UPDATE_LOCATION_COMPLETED.equals(intent.getAction())) {
                        // no need to do anything really, except show an error if any
                        final Replies.LocationReply locationReply = gson.fromJson(payload, Replies.LocationReply.class);
                        Log.d(TAG, "locationReply: " + locationReply.getStatus());
                    }
                } else if(reply.getStatus() == Status.ERROR) {
                    final Replies.ErrorReply errorReply = gson.fromJson(payload, Replies.ErrorReply.class);
                    new DialogError(context, errorReply.getErrorMessages()).show();
                }

                // update the UI
                updateUI();
            } else {
                new DialogError(context, "Invalid null response from server").show();
            }
        }
    }

    private void updateUI()
    {
        buttonA.setEnabled(true);
        buttonB.setEnabled(true);
        buttonC.setEnabled(true);
        buttonD.setEnabled(true);
        buttonTrue.setEnabled(true);
        buttonFalse.setEnabled(true);
        buttonSubmit.setEnabled(true);

        if(question != null) {
            final String questionText = question.getQuestionText();
            switch (question.getQuestionType()) {
                case TEXT:
                    textButtonsContainer.setVisibility(View.VISIBLE);
                    textAnswerEditText.selectAll();
                    textAnswerEditText.requestFocus();
                    textAnswerEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    inputMethodManager.showSoftInput(textAnswerEditText, 0);

                    booleanButtonsContainer.setVisibility(View.GONE);
                    mcqButtonsContainer.setVisibility(View.GONE);
                    break;
                case INTEGER:
                    textButtonsContainer.setVisibility(View.VISIBLE);
                    textAnswerEditText.selectAll();
                    textAnswerEditText.requestFocus();
                    textAnswerEditText.setInputType(InputType.TYPE_CLASS_NUMBER); // set keyboard to integer mode
                    inputMethodManager.showSoftInput(textAnswerEditText, 0);

                    booleanButtonsContainer.setVisibility(View.GONE);
                    mcqButtonsContainer.setVisibility(View.GONE);
                    break;
                case NUMERIC:
                    textButtonsContainer.setVisibility(View.VISIBLE);
                    textAnswerEditText.selectAll();
                    textAnswerEditText.requestFocus();
                    textAnswerEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); // set keyboard to real number mode
                    inputMethodManager.showSoftInput(textAnswerEditText, 0);

                    booleanButtonsContainer.setVisibility(View.GONE);
                    mcqButtonsContainer.setVisibility(View.GONE);
                    break;
                case BOOLEAN:
                    textButtonsContainer.setVisibility(View.GONE);
                    inputMethodManager.hideSoftInputFromWindow(textAnswerEditText.getWindowToken(), 0);

                    booleanButtonsContainer.setVisibility(View.VISIBLE);
                    mcqButtonsContainer.setVisibility(View.GONE);
                    break;
                case MCQ:
                    textButtonsContainer.setVisibility(View.GONE);
                    inputMethodManager.hideSoftInputFromWindow(textAnswerEditText.getWindowToken(), 0);

                    booleanButtonsContainer.setVisibility(View.GONE);
                    mcqButtonsContainer.setVisibility(View.VISIBLE);
                    break;
                default:

            }

            questionTextView.loadData(questionText, "text/html", "utf8");
            requiresLocationTextView.setVisibility(question.isRequiresLocation() ? View.VISIBLE : View.GONE);
            cannotBeSkippedTextView.setVisibility(question.isCanBeSkipped() ? View.GONE : View.VISIBLE);
        }
    }

    private void showScoreBoard()
    {
        final Intent startActivityScoreBoardIntent = new Intent(this, ActivityLeaderBoard.class);
        startActivityScoreBoardIntent.putExtra("session", sessionUUID);
        startActivity(startActivityScoreBoardIntent);
    }

    private void updateLocation(final double lat, final double lng)
    {
        final Intent updateLocationIntent = new Intent(ActivityQuestion.this, SyncService.class);
        updateLocationIntent.setAction(SyncService.ACTION_UPDATE_LOCATION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        parameters.put("lat", Double.toString(lat));
        parameters.put("lng", Double.toString(lng));
        updateLocationIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        startService(updateLocationIntent);
    }

    /** Location-specific code (Google Play Services) - see https://github.com/googlesamples/android-play-location **/

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                Snackbar.make(
                        findViewById(android.R.id.content),
                        "Acquired location",
                        Snackbar.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    Log.i(TAG, "All location settings are satisfied.");

                    //noinspection MissingPermission
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        showSnackbar(R.string.permission_rationale,
                                android.R.string.ok, view -> {
                                    // Request permission
                                    ActivityCompat.requestPermissions(ActivityQuestion.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_PERMISSIONS_REQUEST_CODE);
                                });
                    } else {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }

                    updateUI();
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(ActivityQuestion.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(ActivityQuestion.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(ActivityQuestion.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(ActivityQuestion.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.Settings, view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
            }
        }
    }
}