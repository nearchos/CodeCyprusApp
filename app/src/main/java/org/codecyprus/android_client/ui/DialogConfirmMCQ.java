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
import android.view.View;
import android.widget.TextView;

import org.codecyprus.android_client.R;

/**
 * Date: 7/9/11
 * Time: 8:58 PM
 */
public class DialogConfirmMCQ extends AlertDialog
{
    public DialogConfirmMCQ(final Context context, final String answer) {
        super(context);

        final View rootView = View.inflate(context, R.layout.fragment_dialog_skip, null);
        setView(rootView);

        setTitle(R.string.Confirm_MCQ);

        ((TextView) rootView.findViewById(R.id.confirm_mcq_text_view)).setText(context.getString(R.string.Are_you_sure_you_want_to_submit_for_the_current_question, answer));

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.Submit), new OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                submit = true;
                dialog.dismiss();
            }
        });

        setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.Cancel), new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                submit = false;
                dialog.dismiss();
            }
        });
    }

    private boolean submit = false;

    public boolean isSubmit()
    {
        return submit;
    }
}