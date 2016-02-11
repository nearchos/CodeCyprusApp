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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.codecyprus.android_client.Installation;
import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.model.Category;
import org.codecyprus.android_client.sync.JsonParseException;
import org.codecyprus.android_client.sync.JsonParser;
import org.codecyprus.android_client.sync.SyncService;
import org.json.JSONException;

import java.util.HashMap;

/**
 * @author Nearchos Paspallis on 23/12/13.
 */
public class ActivityStartQuiz extends Activity
{
    public static final String TAG = "codecyprus";

    public static final String EXTRA_CATEGORY = "extra_category";

    private Category category;

    private EditText teamNameEditText;
    private EditText pirate1NameEditText;
    private EditText pirate1EmailEditText;
    private EditText pirate2NameEditText;
    private EditText pirate2EmailEditText;
    private Button submitButton;

    private boolean hasSecondPirate = false;

    private final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_START_QUIZ_COMPLETED);
    private ProgressReceiver progressReceiver;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_start_quiz);

        category = (Category) getIntent().getSerializableExtra(EXTRA_CATEGORY);

        final TextView bubbleTextView = (TextView) findViewById(R.id.activity_start_quiz_text_view_bubble);
        bubbleTextView.setText(getString(R.string.Team_names_and_emails, category.getName()));

        findViewById(R.id.activity_start_quiz_button_add_second_pirate).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hasSecondPirate = true;
                findViewById(R.id.activity_start_quiz_button_add_second_pirate).setVisibility(View.GONE);
                findViewById(R.id.activity_start_quiz_second_pirate_container).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.activity_start_quiz_button_remove_second_pirate).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hasSecondPirate = false;
                findViewById(R.id.activity_start_quiz_button_add_second_pirate).setVisibility(View.VISIBLE);
                findViewById(R.id.activity_start_quiz_second_pirate_container).setVisibility(View.GONE);
            }
        });

        submitButton = (Button) findViewById(R.id.activity_start_quiz_button_start);
        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitButton.setEnabled(false);
                startQuiz();
            }
        });

        teamNameEditText = (EditText) findViewById(R.id.activity_start_quiz_team_name);
        pirate1NameEditText = (EditText) findViewById(R.id.activity_start_quiz_pirate1_name);
        pirate1EmailEditText = (EditText) findViewById(R.id.activity_start_quiz_pirate1_email);
        pirate2NameEditText = (EditText) findViewById(R.id.activity_start_quiz_pirate2_name);
        pirate2EmailEditText = (EditText) findViewById(R.id.activity_start_quiz_pirate2_email);

        progressReceiver = new ProgressReceiver();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(R.string.ENTER_CODE)
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
        else if(getString(R.string.ENTER_CODE).equals(item.getTitle()))
        {
            enterCode();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startQuiz()
    {
        final String teamName = teamNameEditText.getText() == null ? "" : teamNameEditText.getText().toString().trim();
        final String name1 = pirate1NameEditText.getText() == null ? "" : pirate1NameEditText.getText().toString().trim();
        final String email1 = pirate1EmailEditText.getText() == null ? "" : pirate1EmailEditText.getText().toString().trim();
        final String name2 = pirate2NameEditText.getText() == null ? "" : pirate2NameEditText.getText().toString().trim();
        final String email2 = pirate2EmailEditText.getText() == null ? "" : pirate2EmailEditText.getText().toString().trim();

        final StringBuilder errorMessage = new StringBuilder();
        if(teamName.isEmpty())
        {
            errorMessage.append(getString(R.string.Team_name)).append('\n');
            errorMessage.append(" - ").append(getString(R.string.Empty_name)).append('\n');
        }

        if(name1.isEmpty() || email1.isEmpty() || !isValidEmail(email1))
        {
            errorMessage.append(getString(R.string.First_pirate)).append('\n');
            if(name1.isEmpty()) errorMessage.append(" - ").append(getString(R.string.Empty_name)).append('\n');
            if(email1.isEmpty())
            {
                errorMessage.append(" - ").append(getString(R.string.Empty_email)).append('\n');
            }
            else if(!isValidEmail(email1))
            {
                errorMessage.append(" - ").append(getString(R.string.Invalid_email)).append('\n');
            }
        }

        if(hasSecondPirate)
        {
            if(name2.isEmpty() || email2.isEmpty() || !isValidEmail(email2))
            {
                errorMessage.append(getString(R.string.Second_pirate)).append('\n');
                if(name2.isEmpty()) errorMessage.append(" - ").append(getString(R.string.Empty_name)).append('\n');
                if(email2.isEmpty())
                {
                    errorMessage.append(" - ").append(getString(R.string.Empty_email)).append('\n');
                }
                else if(!isValidEmail(email2))
                {
                    errorMessage.append(" - ").append(getString(R.string.Invalid_email)).append('\n');
                }
            }
        }

        final String message = errorMessage.toString();
        if(message.isEmpty())
        {
            // call 'start quiz' service
            final Intent startQuizIntent = new Intent(this, SyncService.class);
            startQuizIntent.setAction(SyncService.ACTION_START_QUIZ);
            final HashMap<String,String> parameters = new HashMap<String, String>();
            parameters.put("playerName", teamName);
            parameters.put("appID", "codecyprus");
            parameters.put("categoryUUID", category.getUUID());
            if(code != null && code.length() > 0) parameters.put("code", code);
            parameters.put("name1", name1);
            parameters.put("email1", email1);
            parameters.put("name2", name2);
            parameters.put("email2", email2);
            parameters.put("installationID", Installation.id(this));
            startQuizIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
            setProgressBarIndeterminateVisibility(true);
            startService(startQuizIntent);
        }
        else
        {
            submitButton.setEnabled(true);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private class ProgressReceiver extends BroadcastReceiver
    {
        @Override public void onReceive(final Context context, final Intent intent)
        {
            final String payload = (String) intent.getSerializableExtra(SyncService.EXTRA_PAYLOAD);
            setProgressBarIndeterminateVisibility(false);

            if(payload != null)
            {
                try
                {
                    final String sessionUUID = JsonParser.parseStartQuiz(payload);

                    final String teamName = teamNameEditText.getText() == null ? "" : teamNameEditText.getText().toString().trim();
                    final String name1 = pirate1NameEditText.getText() == null ? "" : pirate1NameEditText.getText().toString().trim();
                    final String email1 = pirate1EmailEditText.getText() == null ? "" : pirate1EmailEditText.getText().toString().trim();
                    final String name2 = pirate2NameEditText.getText() == null ? "" : pirate2NameEditText.getText().toString().trim();
                    final String email2 = pirate2EmailEditText.getText() == null ? "" : pirate2EmailEditText.getText().toString().trim();

                    final SerializableSession serializableSession = new SerializableSession(
                            category.getUUID(),
                            category.getName(),
                            category.getLocationUUID(),
                            sessionUUID,
                            teamName,
                            name1,
                            email1,
                            name2,
                            email2);

                    Preferences.addSession(context, serializableSession);
                    Preferences.setActiveSession(context, serializableSession);

                    // start current question activity
                    startActivity(new Intent(context, ActivityCurrentQuestion.class));
                }
                catch (JsonParseException jsonpe)
                {
                    Log.e(TAG, jsonpe.getMessage());
                    submitButton.setEnabled(true);
                    new DialogError(context, jsonpe.getMessage()).show();
                }
                catch (JSONException jsone)
                {
                    Log.e(TAG, jsone.getMessage());
                    submitButton.setEnabled(true);
                    new DialogError(context, jsone.getMessage()).show();
                }
            }
        }
    }

    public static boolean isValidEmail(CharSequence target)
    {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private String code = "";

    private void enterCode()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.Enter_code);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                code = input.getText().toString();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

}