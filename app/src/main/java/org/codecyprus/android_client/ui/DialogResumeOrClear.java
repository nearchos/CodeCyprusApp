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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.model.Category;

/**
 * Date: 7/9/11
 * Time: 8:58 PM
 */
public class DialogResumeOrClear extends AlertDialog
{
    public static final String TAG = "codecyprus";

    public DialogResumeOrClear(final Context context, final SerializableSession serializableSession, final Category category)
    {
        super(context);

        final View rootView = View.inflate(context, R.layout.fragment_dialog_resume_or_clear, null);
        setView(rootView);

        setTitle(R.string.Resume_or_clear);

        final TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_resume_or_clear_message);
        messageTextView.setText(context.getString(R.string.Resume_or_clear_message, serializableSession.getCategoryName()));

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.Resume), new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // resume
                Preferences.setActiveSession(context, serializableSession);
                final Intent continueQuizIntent = new Intent(context, ActivityCurrentQuestion.class);
                context.startActivity(continueQuizIntent);
                dismiss();
            }
        });

        setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.Start_new), new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // start new
                Preferences.clearSession(context, serializableSession);
                final Intent startQuizIntent = new Intent(context, ActivityStartQuiz.class);
                startQuizIntent.putExtra(ActivityStartQuiz.EXTRA_CATEGORY, category);
                context.startActivity(startQuizIntent);
                dismiss();
            }
        });

        setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.Cancel), new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
    }
}