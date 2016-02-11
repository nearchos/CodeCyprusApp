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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import org.codecyprus.android_client.BuildConfig;
import org.codecyprus.android_client.R;

/**
 * Date: 7/9/11
 * Time: 8:58 PM
 */
public class DialogAbout extends AlertDialog
{
    public DialogAbout(final Context context)
    {
        super(context);

        final View view = View.inflate(context, R.layout.fragment_dialog_about, null);
        setView(view);

        setTitle(R.string.About);

        ((TextView) view.findViewById(R.id.dialog_about_build_name)).setText(BuildConfig.VERSION_NAME);

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.Dismiss), new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.Rate_us), new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                final String packageName = context.getPackageName();
                final PackageManager packageManager = context.getPackageManager();
                final Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                if(packageManager.queryIntentActivities(marketIntent, PackageManager.GET_ACTIVITIES).isEmpty())
                {
                    // start browser
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=" + packageName)));
                }
                else
                {
                    // start market
                    context.startActivity(marketIntent);
                }
            }
        });
    }
}