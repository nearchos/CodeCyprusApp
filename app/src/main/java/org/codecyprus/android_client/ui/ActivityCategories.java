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
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
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
 * @author Nearchos Paspallis
 * Created on 19/12/13.
 */
public class ActivityCategories extends Activity
{
    public static final String TAG = "codecyprus";

    private final IntentFilter intentFilter = new IntentFilter(SyncService.ACTION_CATEGORIES_COMPLETED);
    private ProgressReceiver progressReceiver;

    private ListView listView;

    private Category [] categories;

    private ConnectivityManager connectivityManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_categories);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        listView = (ListView) findViewById(R.id.activity_category_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final Category category = categories[position];
                final SerializableSession serializableSession = Preferences.getSession(ActivityCategories.this, category.getUUID());
                if(serializableSession != null)
                {
                    new DialogResumeOrClear(ActivityCategories.this, serializableSession, category).show();
                }
                else
                {
                    final Intent startQuizIntent = new Intent(ActivityCategories.this, ActivityStartQuiz.class);
                    startQuizIntent.putExtra(ActivityStartQuiz.EXTRA_CATEGORY, category);
                    startActivity(startQuizIntent);
                }
            }
        });

        final ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        progressReceiver = new ProgressReceiver();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(progressReceiver, intentFilter);

        tryToConnectAndRefresh();
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
        menu.add(R.string.REFRESH)
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
        else if(getString(R.string.REFRESH).equals(item.getTitle()))
        {
            refresh();
            return true;
        }
        else
        {
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
                    .setPositiveButton(R.string.Retry, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            tryToConnectAndRefresh();
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

    private void refresh()
    {
        final Intent getCategoriesIntent = new Intent(this, SyncService.class);
        getCategoriesIntent.setAction(SyncService.ACTION_CATEGORIES);
        getCategoriesIntent.putExtra(SyncService.EXTRA_PARAMETERS, new HashMap<String, String>());
        setProgressBarIndeterminateVisibility(true);
        startService(getCategoriesIntent);
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
                    categories = JsonParser.parseGetActiveCategories(payload);

                    // update the UI
                    listView.setAdapter(new CategoriesAdapter(ActivityCategories.this, categories));
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