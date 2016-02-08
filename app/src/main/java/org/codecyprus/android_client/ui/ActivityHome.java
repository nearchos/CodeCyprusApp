package org.codecyprus.android_client.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import org.codecyprus.android_client.R;

public class ActivityHome extends Activity
{
    /**
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.activity_home_button_start).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(ActivityHome.this, ActivityCategories.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(R.string.HELP)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        menu.add(R.string.ABOUT)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(getString(R.string.HELP).equals(item.getTitle()))
        {
            startActivity(new Intent(this, ActivityHelp.class));
            return true;
        }
        else if(getString(R.string.ABOUT).equals(item.getTitle()))
        {
            new DialogAbout(this).show();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }
}
