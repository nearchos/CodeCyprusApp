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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.codecyprus.android_client.Installation;
import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.sync.SyncService;
import org.codecyprus.th.model.Replies;
import org.codecyprus.th.model.TreasureHunt;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Nearchos Paspallis on 23/12/13.
 */
public class ActivityStartQuiz extends Activity {

    public static final String TAG = "codecyprus";

    public static final String EXTRA_TREASURE_HUNT = "extra_treasure_hunt";

    private TreasureHunt treasureHunt;

    private EditText teamNameEditText;
    private EditText teamEmailEditText;
    private EditText pirate1NameEditText;
    private EditText pirate2NameEditText;
    private Button startQuizButton;

    private boolean hasFirstPirate = false;
    private boolean hasSecondPirate = false;

    private final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_START_COMPLETED);
    private ProgressReceiver progressReceiver;

    private ConnectivityManager connectivityManager = null;

    private Gson gson = new Gson();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_start_quiz);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        treasureHunt = (TreasureHunt) getIntent().getSerializableExtra(EXTRA_TREASURE_HUNT);

        final TextView bubbleTextView = findViewById(R.id.activity_start_quiz_text_view_bubble);
        bubbleTextView.setText(getString(R.string.Team_names_and_emails, treasureHunt.getName()));

        findViewById(R.id.activity_start_quiz_button_add_second_pirate).setOnClickListener(v -> {
            hasSecondPirate = true;
            findViewById(R.id.activity_start_quiz_button_add_second_pirate).setVisibility(View.GONE);
            findViewById(R.id.activity_start_quiz_second_pirate_container).setVisibility(View.VISIBLE);
        });

        findViewById(R.id.activity_start_quiz_button_remove_second_pirate).setOnClickListener(v -> {
            hasSecondPirate = false;
            findViewById(R.id.activity_start_quiz_button_add_second_pirate).setVisibility(View.VISIBLE);
            findViewById(R.id.activity_start_quiz_second_pirate_container).setVisibility(View.GONE);
        });

        startQuizButton = findViewById(R.id.activity_start_quiz_button_start);
        startQuizButton.setOnClickListener(v -> tryToStartQuiz());

        teamNameEditText = findViewById(R.id.activity_start_quiz_team_name);
        teamEmailEditText = findViewById(R.id.activity_start_quiz_team_email);
        pirate1NameEditText = findViewById(R.id.activity_start_quiz_pirate1_name);
        // when pirate1 edittext receives focus, then if empty set to the id of the email
        pirate1NameEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus && pirate1NameEditText.getText().toString().isEmpty()) {
                final String teamEmail = teamEmailEditText.getText().toString().trim();
                if(teamEmail.contains("@")) {
                    pirate1NameEditText.setText(teamEmail.substring(0, teamEmail.indexOf('@')));
                    pirate1NameEditText.selectAll();
                }
            }
        });
        pirate2NameEditText = findViewById(R.id.activity_start_quiz_pirate2_name);

        progressReceiver = new ProgressReceiver();
    }

    private String code = "";

    public static final int PERMISSIONS_REQUEST_GET_ACCOUNT = 43;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);
        code = PreferenceManager.getDefaultSharedPreferences(this).getString("code", "");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[] { Manifest.permission.GET_ACCOUNTS }, PERMISSIONS_REQUEST_GET_ACCOUNT);
        } else {
            selectAccount();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_REQUEST_GET_ACCOUNT) {
            selectAccount();
        }
        // do nothing if denied
    }

    private void selectAccount() {
        final AccountManager accountManager = AccountManager.get(this);
        final Account[] accounts = accountManager.getAccountsByType("com.google");

        if(accounts.length == 0) {
            // do nothing
        } else { // accounts.length >= 1
            // select the single email
            teamEmailEditText.setText(accounts[0].name);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }

    private void enterCode() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.Enter_code);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.OK, (dialog, whichButton) -> {
            code = input.getText().toString();
            PreferenceManager.getDefaultSharedPreferences(ActivityStartQuiz.this).edit().putString("code", code).apply();
            // try to connect bypassing non-started categories
            final String errorMessage = validateUserInput();
            if(!errorMessage.isEmpty()) {
                startQuizButton.setEnabled(true);
                Toast.makeText(ActivityStartQuiz.this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                uploadStartQuizRequest();
            }
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });

        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.ENTER_CODE)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        } else if(getString(R.string.ENTER_CODE).equals(item.getTitle())) {
            enterCode();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void tryToStartQuiz() {
        final String errorMessage = validateUserInput();
        if(!errorMessage.isEmpty()) {
            startQuizButton.setEnabled(true);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            final NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if(isConnected) {
                startQuiz();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.No_Internet)
                        .setMessage(R.string.It_seems_like_you_are_not_connected_to_Internet)
                        .setPositiveButton(R.string.Retry, (dialog, id) -> {
                            dialog.dismiss();
                            tryToStartQuiz();
                        })
                        .setNegativeButton(R.string.Cancel, (dialog, id) -> dialog.dismiss()).create().show();
            }
        }
    }

    private String validateUserInput()
    {
        final String teamName = teamNameEditText.getText() == null ? "" : teamNameEditText.getText().toString().trim();
        final String teamEmail = teamEmailEditText.getText() == null ? "unknown@somewhere.com" : teamEmailEditText.getText().toString().trim();
        final String name1 = pirate1NameEditText.getText() == null ? "" : pirate1NameEditText.getText().toString().trim();
        final String name2 = pirate2NameEditText.getText() == null ? "" : pirate2NameEditText.getText().toString().trim();

        final StringBuilder errorMessage = new StringBuilder();
        if(teamName.isEmpty()) {
            errorMessage.append(getString(R.string.Team_name)).append('\n');
            errorMessage.append(" - ").append(getString(R.string.Empty_name)).append('\n');
        }

        if(teamEmail.isEmpty()) {
            errorMessage.append(getString(R.string.Email)).append('\n');
            errorMessage.append(" - ").append(getString(R.string.Empty_email)).append('\n');
        } else if(!isValidEmail(teamEmail)) {
            errorMessage.append(" - ").append(getString(R.string.Invalid_email)).append('\n');
        }

        if(hasFirstPirate) {
            if(name1.isEmpty()) {
                errorMessage.append(getString(R.string.First_pirate)).append(" - ").append(getString(R.string.Empty_name)).append('\n');
            }
        }

        if(hasSecondPirate) {
            if(name2.isEmpty()) {
                errorMessage.append(getString(R.string.Second_pirate)).append(" - ").append(getString(R.string.Empty_name)).append('\n');
            }
        }

        return errorMessage.toString();
    }

    private void startQuiz() {
            final Date validFrom = new Date(treasureHunt.getStartsOn());
            if(validFrom.after(new Date())) { // category not active yet
                final String startsInTime = TreasureHuntsAdapter.timeInText(this, validFrom.getTime() - System.currentTimeMillis());
                new AlertDialog.Builder(this)
                        .setTitle(R.string.Inactive_category)
                        .setMessage(getString(R.string.The_selected_category_is_not_active_yet, startsInTime))
                        .setPositiveButton(R.string.Try_again, (dialog, id) -> {
                            dialog.dismiss();
                            tryToStartQuiz();
                        })
                        .setNegativeButton(R.string.Wait, (dialog, id) -> dialog.dismiss()).create().show();
            } else {
                uploadStartQuizRequest();
            }
    }

    private void uploadStartQuizRequest() {
        startQuizButton.setEnabled(false);

        final String teamName = teamNameEditText.getText() == null ? "" : teamNameEditText.getText().toString().trim();
        final String teamEmail = teamEmailEditText.getText() == null ? "unknown@somewhere.com" : teamEmailEditText.getText().toString().trim();
        final Intent startQuizIntent = new Intent(this, SyncService.class);
        startQuizIntent.setAction(SyncService.ACTION_START);
        final HashMap<String,String> parameters = new HashMap<>();
        parameters.put("player", teamName);
        parameters.put("app", getApplicationContext().getPackageName());
        parameters.put("treasure-hunt-id", treasureHunt.getUuid());
        if(code != null && code.length() > 0) parameters.put("code", code);
        parameters.put("email", teamEmail);
        parameters.put("installationID", Installation.id(this));
        startQuizIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        // call 'start quiz' service
        startService(startQuizIntent);
    }

    private class ProgressReceiver extends BroadcastReceiver {
        @Override public void onReceive(final Context context, final Intent intent) {
            final String payload = (String) intent.getSerializableExtra(SyncService.EXTRA_PAYLOAD);
            setProgressBarIndeterminateVisibility(false);

            if(payload != null) {
                final Replies.Reply reply = gson.fromJson(payload, Replies.Reply.class);
                if(reply.getStatus().isOk()) {
                    final Replies.StartReply startReply = gson.fromJson(payload, Replies.StartReply.class);
                    final String sessionUUID = startReply.getSessionId();

                    final String teamName = teamNameEditText.getText() == null ? "" : teamNameEditText.getText().toString().trim();
                    final String teamEmail = teamEmailEditText.getText() == null ? "" : teamEmailEditText.getText().toString().trim(); // todo not needed
                    final String name1 = pirate1NameEditText.getText() == null ? "" : pirate1NameEditText.getText().toString().trim();
                    final String name2 = pirate2NameEditText.getText() == null ? "" : pirate2NameEditText.getText().toString().trim();

                    final SerializableSession serializableSession = new SerializableSession(
                            treasureHunt.getUuid(),
                            treasureHunt.getName(),
                            null,
                            sessionUUID,
                            teamName,
                            teamEmail,
                            name1,
                            name2);

                    Preferences.addSession(context, serializableSession);
                    Preferences.setActiveSession(context, serializableSession);

                    // start current question activity
                    startActivity(new Intent(context, ActivityQuestion.class));

                } else {
                    final Replies.ErrorReply errorReply = gson.fromJson(payload, Replies.ErrorReply.class);
                    startQuizButton.setEnabled(true);
                    new DialogError(context, errorReply.getErrorMessages()).show();
                }
            }
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}