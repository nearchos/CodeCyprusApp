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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.sync.SyncService;
import org.codecyprus.th.model.Replies;
import org.codecyprus.th.model.Status;

import java.util.*;

/**
 * @author Nearchos Paspallis
 * 27/12/13 / 15:59.
 */
public class ActivityQuestion extends Activity {
    public static final String TAG = "codecyprus";

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
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
    private View requiresLocationView;
    private CheckBox locationCheckBox;
    private View locationProgressBar;
    private TextView cannotBeSkippedTextView;
    private View mcqButtonsContainer;
    private View booleanButtonsContainer;
    private View textButtonsContainer;
    private EditText textAnswerEditText;

    private Replies.QuestionReply question = null;

    private InputMethodManager inputMethodManager;

    private ConnectivityManager connectivityManager = null;

    private Gson gson = new Gson();

    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_current_question);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        actionBar = getActionBar();

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        feedbackTextView = findViewById(R.id.activity_current_question_feedback);
        feedbackTextView.setVisibility(View.GONE);
        questionTextView = findViewById(R.id.activity_current_question_question);
        requiresLocationView = findViewById(R.id.activity_current_question_requires_location);
        requiresLocationView.setVisibility(View.GONE);
        locationCheckBox = findViewById(R.id.activity_current_question_requires_location_check_box);
        locationCheckBox.setVisibility(View.GONE);
        locationProgressBar = findViewById(R.id.activity_current_question_requires_location_progress_bar);
        locationProgressBar.setVisibility(View.VISIBLE);
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
            if (answer.isEmpty()) {
                Toast.makeText(ActivityQuestion.this, R.string.Invalid_empty_answer, Toast.LENGTH_SHORT).show();
            } else {
                inputMethodManager.hideSoftInputFromWindow(textAnswerEditText.getWindowToken(), 0);
                tryToSubmitAnswer(answer);
            }
        });

        progressReceiver = new ProgressReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (getString(R.string.SKIP).equals(item.getTitle())) {
            if (question.isCanBeSkipped()) {
                final DialogSkip dialogSkip = new DialogSkip(this);
                dialogSkip.setOnDismissListener(dialog -> {
                    if (dialogSkip.isSkip()) {
                        tryToSkipQuestion();
                    }
                });
                dialogSkip.show();
            } else {
                Toast.makeText(this, R.string.Cannot_be_skipped, Toast.LENGTH_SHORT).show();
            }

            return true;
        } else if (getString(R.string.Scan).equals(item.getTitle())) {
            final IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            intentIntegrator.setPrompt(getString(R.string.Scan_a_QR_code));
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.initiateScan();
            return true;
        } else if (getString(R.string.Score_board).equals(item.getTitle())) {
            showScoreBoard();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            // Check for the integer request code originally supplied to startResolutionForResult().
            default: { // only QR code scans are allowed
                final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (intentResult != null) {
                    if (intentResult.getContents() == null) {
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

    private void handleScannedText(final String scannedText) {
        final boolean isUrl = URLUtil.isValidUrl(scannedText);
        final String message = getString(R.string.Would_you_like_to_open_this_URL, scannedText);
        if (isUrl) {
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

    private void handleScannedTextAsPlainText(final String scannedText) {
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

        if (isConnected) {
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

    private void skipQuestion() {
        final Intent skipQuestionIntent = new Intent(this, SyncService.class);
        skipQuestionIntent.setAction(SyncService.ACTION_SKIP);
        final HashMap<String, String> parameters = new HashMap<>();
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

    public static final int PERMISSIONS_REQUEST_CODE = 134;

    private void recoverSession() {
        final SerializableSession serializableSession = Preferences.getActiveSession(this);
        if (serializableSession == null) {
            Toast.makeText(this, R.string.Invalid_session, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            sessionUUID = serializableSession.getSessionUUID();
            tryToRequestCurrentQuestion();

            startListeningForLocationChanges();
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
            requiresLocationView.setVisibility(question.isRequiresLocation() ? View.VISIBLE : View.GONE);
            cannotBeSkippedTextView.setVisibility(question.isCanBeSkipped() ? View.GONE : View.VISIBLE);
        }
    }

    private void showScoreBoard()
    {
        final Intent startActivityScoreBoardIntent = new Intent(this, ActivityLeaderBoard.class);
        startActivityScoreBoardIntent.putExtra("session", sessionUUID);
        startActivity(startActivityScoreBoardIntent);
    }

    // --- code to handle location updates ---

    private void updateLocation(final Location location)
    {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();

        locationCheckBox.setVisibility(View.VISIBLE);
        final long msSinceLastUpdate = System.currentTimeMillis() - location.getTime();
        final int minutesSinceLastUpdate = (int) msSinceLastUpdate / 60000;
        locationCheckBox.setText(getResources().getQuantityString(R.plurals.Acquired_time, minutesSinceLastUpdate, String.format(Locale.ENGLISH, "%d", minutesSinceLastUpdate)));
        locationProgressBar.setVisibility(View.GONE);

        final Intent updateLocationIntent = new Intent(ActivityQuestion.this, SyncService.class);
        updateLocationIntent.setAction(SyncService.ACTION_UPDATE_LOCATION);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("session", sessionUUID);
        parameters.put("latitude", Double.toString(lat));
        parameters.put("longitude", Double.toString(lng));
        updateLocationIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        startService(updateLocationIntent);
    }

    private LocationListener myLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            /* explicitly empty */
        }

        @Override
        public void onProviderEnabled(String s) {
            /* explicitly empty */
        }

        @Override
        public void onProviderDisabled(String s) {
            /* explicitly empty */
        }
    };
    private void startListeningForLocationChanges() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE);
                return;
            }
        }

        final Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        location.getTime();
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10000, 20, myLocationListener);
        updateLocation(location);
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(myLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}