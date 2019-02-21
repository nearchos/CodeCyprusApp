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
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.codecyprus.android_client.R;

import java.util.ArrayList;

/**
 * Date: 7/9/11
 * Time: 8:58 PM
 */
public class DialogError extends AlertDialog
{
    private final ListView errorsListView;

    DialogError(final Context context)
    {
        super(context);

        final View rootView = View.inflate(context, R.layout.fragment_dialog_error, null);
        setView(rootView);

        setTitle(R.string.Error);
        errorsListView = rootView.findViewById(R.id.dialog_error_messages);

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.Dismiss), (dialog, which) -> dialog.dismiss());
    }

    DialogError(final Context context, final String message)
    {
        this(context);
        setMessages(new String [] { message });
    }

    DialogError(final Context context, final ArrayList<String> messages) {
        this(context);
        setMessages(messages.toArray(new String[messages.size()]));
    }

    private void setMessages(final String [] messages)
    {
        final ArrayAdapter<String> messagesArrayAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, messages);
        errorsListView.setAdapter(messagesArrayAdapter);
    }
}