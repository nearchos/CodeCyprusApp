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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.model.ScoreEntry;
import org.codecyprus.android_client.sync.JsonParseException;
import org.codecyprus.android_client.sync.JsonParser;
import org.codecyprus.android_client.sync.SyncService;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author Nearchos Paspallis
 * 31/12/13 / 08:53.
 */
public class ActivityScoreBoard extends Activity
{
    public static final String TAG = "codecyprus";

    private final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_SCORE_BOARD_COMPLETED);
    private ProgressReceiver progressReceiver;

    private TextView scoreBoardTitle;
    private ListView listView;

    private ConnectivityManager connectivityManager = null;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_score_board);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        scoreBoardTitle = findViewById(R.id.activity_score_board_title);
        listView = findViewById(R.id.activity_score_board_list_view);

        final ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        progressReceiver = new ProgressReceiver();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);
        tryToRefreshScoreBoard();
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
        menu.add(R.string.Scan)
                .setIcon(R.drawable.ic_qrcode_black_48dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

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
        else if(getString(R.string.Scan).equals(item.getTitle()))
        {
            final String sessionUUID = getIntent().getStringExtra("session");
            final Intent intent = new Intent(this, ActivityGenerateQRCode.class);
            intent.putExtra("session", sessionUUID);
            startActivity(intent);
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    private void tryToRefreshScoreBoard() {
        final NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            refreshScoreBoard();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.No_Internet)
                    .setMessage(R.string.It_seems_like_you_are_not_connected_to_Internet)
                    .setPositiveButton(R.string.Retry, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            tryToRefreshScoreBoard();
                        }
                    })
                    .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create().show();
        }
    }

    private void refreshScoreBoard()
    {
        final Intent getScoreBoardIntent = new Intent(this, SyncService.class);
        getScoreBoardIntent.setAction(SyncService.ACTION_SCORE_BOARD);
        final HashMap<String,String> parameters = new HashMap<String, String>();
        final String sessionUUID = getIntent().getStringExtra("session");
        parameters.put("session", sessionUUID);
        parameters.put("sorted", "true");
        getScoreBoardIntent.putExtra(SyncService.EXTRA_PARAMETERS, parameters);
        setProgressBarIndeterminateVisibility(true);
        startService(getScoreBoardIntent);
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
                    final Vector<ScoreEntry> scoreEntries = JsonParser.parseScoreBoard(payload);
                    // update the UI
                    listView.setAdapter(new ScoreEntriesAdapter(context, scoreEntries));
                }
                catch (JsonParseException jsonpe)
                {
                    Log.e(TAG, jsonpe.getMessage());
                    new DialogError(context, jsonpe.getMessage()).show();
                }
                catch (JSONException jsone)
                {
                    Log.e(TAG, jsone.getMessage());
                    new DialogError(context, jsone.getMessage()).show();
                }
            }
        }
    }
}