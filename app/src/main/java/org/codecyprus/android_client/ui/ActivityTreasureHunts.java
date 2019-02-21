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

import com.google.gson.Gson;

import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.sync.SyncService;
import org.codecyprus.th.model.Replies;
import org.codecyprus.th.model.TreasureHunt;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author Nearchos Paspallis
 * Created on 19/12/13.
 */
public class ActivityTreasureHunts extends Activity
{
    public static final String TAG = "codecyprus";

    private final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_LIST_COMPLETED);
    private ProgressReceiver progressReceiver;

    private ListView listView;

    private Vector<TreasureHunt> treasureHunts = new Vector<>();

    private ConnectivityManager connectivityManager = null;

    private Gson gson = new Gson();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_categories);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        listView = findViewById(R.id.activity_category_list_view);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final TreasureHunt treasureHunt = treasureHunts.elementAt(position);
            final SerializableSession serializableSession = Preferences.getSession(ActivityTreasureHunts.this, treasureHunt.getUuid());
            if(serializableSession != null) {
                new DialogResumeOrClear(ActivityTreasureHunts.this, serializableSession, treasureHunt).show();
            } else {
                final Intent startQuizIntent = new Intent(ActivityTreasureHunts.this, ActivityStartQuiz.class);
                startQuizIntent.putExtra(ActivityStartQuiz.EXTRA_TREASURE_HUNT, treasureHunt);
                startActivity(startQuizIntent);
            }
        });

        final ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        progressReceiver = new ProgressReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);

        tryToConnectAndRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(R.string.REFRESH)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if(getString(R.string.REFRESH).equals(item.getTitle())) {
            refresh();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void tryToConnectAndRefresh() {
        final NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            refresh();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.No_Internet)
                    .setMessage(R.string.It_seems_like_you_are_not_connected_to_Internet)
                    .setPositiveButton(R.string.Retry, (dialog, id) -> {
                        dialog.dismiss();
                        tryToConnectAndRefresh();
                    })
                    .setNegativeButton(R.string.Cancel, (dialog, id) -> {
                        dialog.dismiss();
                        finish();
                    }).create().show();
        }
    }

    private void refresh() {
        final Intent getTreasureHuntsIntent = new Intent(this, SyncService.class);
        getTreasureHuntsIntent.setAction(SyncService.ACTION_LIST);
        getTreasureHuntsIntent.putExtra(SyncService.EXTRA_PARAMETERS, new HashMap<String, String>());
        setProgressBarIndeterminateVisibility(true);
        startService(getTreasureHuntsIntent);
    }

    private class ProgressReceiver extends BroadcastReceiver
    {
        @Override public void onReceive(final Context context, final Intent intent)
        {
            final String payload = (String) intent.getSerializableExtra(SyncService.EXTRA_PAYLOAD);
            setProgressBarIndeterminateVisibility(false);

            if(payload != null) {
                final Replies.ListReply listReply = gson.fromJson(payload, Replies.ListReply.class);
                if(listReply.getStatus().isOk()) {
                    treasureHunts.clear();
                    treasureHunts = listReply.getSelectedTreasureHunts();
                }

                // update the UI
                Log.d(TAG, "Treasure hunts: " + treasureHunts);
                listView.setAdapter(new TreasureHuntsAdapter(ActivityTreasureHunts.this, treasureHunts));
            }
        }
    }
}