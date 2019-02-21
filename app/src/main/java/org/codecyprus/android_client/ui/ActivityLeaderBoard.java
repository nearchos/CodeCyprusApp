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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.codecyprus.android_client.R;
import org.codecyprus.android_client.sync.SyncService;
import org.codecyprus.th.model.Replies;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author Nearchos Paspallis
 * 31/12/13 / 08:53.
 */
public class ActivityLeaderBoard extends Activity
{
    public static final String TAG = "codecyprus";

    private final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_LEADER_BOARD_COMPLETED);
    private ProgressReceiver progressReceiver;

    private TextView leaderBoardName;
    private Button leaderboardPrize;
    private ListView listView;

    private ConnectivityManager connectivityManager = null;

    private Gson gson = new Gson();

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_leader_board);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        leaderBoardName = findViewById(R.id.activity_leader_board_name);
        leaderboardPrize = findViewById(R.id.activity_leader_board_prize);
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void showQRCode(View view) {
        final String sessionUUID = getIntent().getStringExtra("session");
        final Intent intent = new Intent(this, ActivityGenerateQRCode.class);
        intent.putExtra("session", sessionUUID);
        startActivity(intent);
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
                    .setPositiveButton(R.string.Retry, (dialog, id) -> {
                        dialog.dismiss();
                        tryToRefreshScoreBoard();
                    })
                    .setNegativeButton(R.string.Cancel, (dialog, id) -> {
                        dialog.dismiss();
                        finish();
                    }).create().show();
        }
    }

    private void refreshScoreBoard()
    {
        final Intent getScoreBoardIntent = new Intent(this, SyncService.class);
        getScoreBoardIntent.setAction(SyncService.ACTION_LEADER_BOARD);
        final HashMap<String,String> parameters = new HashMap<>();
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

            if(payload != null) {
                final Replies.Reply reply = gson.fromJson(payload, Replies.Reply.class);
                if(reply.getStatus().isOk()) {
                    final Replies.LeaderboardReply leaderboardReply = gson.fromJson(payload, Replies.LeaderboardReply.class);
                    final Vector<Replies.LeaderboardEntry> leaderboardEntries = leaderboardReply.getLeaderboard();
                    // update the UI
                    leaderBoardName.setText(leaderboardReply.getTreasureHuntName());
                    listView.setAdapter(new LeaderboardEntriesAdapter(context, leaderboardEntries));
                } else {
                    final Replies.ErrorReply errorReply = gson.fromJson(payload, Replies.ErrorReply.class);
                    new DialogError(context, errorReply.getErrorMessages()).show();
                }
            }
        }
    }
}