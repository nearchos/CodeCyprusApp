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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.codecyprus.android_client.R;
import org.codecyprus.th.model.Replies;

import java.util.Locale;
import java.util.Vector;

/**
 * @author Nearchos Paspallis
 * 24/12/13
 */
public class LeaderboardEntriesAdapter extends ArrayAdapter<Replies.LeaderboardEntry>
{
    public static final String TAG = "codecyprus";

    private final LayoutInflater layoutInflater;

    public LeaderboardEntriesAdapter(final Context context, final Vector<Replies.LeaderboardEntry> scoreEntries)
    {
        super(context, R.layout.list_item_score_entry, scoreEntries);

        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = convertView != null ? convertView : layoutInflater.inflate(R.layout.list_item_score_entry, null); // todo fix suing standard pattern for recycler view

        final Replies.LeaderboardEntry leaderboardEntry = getItem(position);

        // Creates a ViewHolder and store references to the two children views we want to bind data to.
        assert view != null;
        final TextView playerName = view.findViewById(R.id.list_item_score_entry_player_name);
        final TextView score = view.findViewById(R.id.list_item_score_entry_score);
        final TextView finishTimeTextView = view.findViewById(R.id.list_item_score_entry_finish_time);

        assert leaderboardEntry != null;

        // Bind the data efficiently with the holder.
        playerName.setText(leaderboardEntry.getPlayer());
        score.setText(String.format(Locale.US, "%d", leaderboardEntry.getScore()));
        final long completionTime = leaderboardEntry.getCompletionTime();
        if(completionTime > 0) {
            finishTimeTextView.setText(getContext().getString(R.string.Finished_in, timeInText(completionTime)));
        } else {
            finishTimeTextView.setText(getContext().getString(R.string.Not_finished_yet));
        }

        return view;
    }

    private static String timeInText(final long duration)
    {
        long SECOND = 1000L;
        long MINUTE = 60 * SECOND;
        long HOUR = 60 * MINUTE;

        if(duration < SECOND)
        {
            return duration + "ms";
        }
        else if(duration < MINUTE)
        {
            long ms = duration % SECOND;
            return duration / SECOND + "s:"
                    + (ms < 100 ? "0" : "") + (ms < 10 ? "0" : "") + ms;
        }
        else if(duration < HOUR)
        {
            long ms = duration % SECOND;
            long s = duration / MINUTE;
            return duration / MINUTE + "m:"
                    + (s < 10 ? "0" : "") + s + "s:"
                    + (ms < 100 ? "0" : "") + (ms < 10 ? "0" : "") + ms + "ms";
        }
        else
        {
            long ms = duration % SECOND;
            long s = (duration % MINUTE) / SECOND;
            long m = (duration % HOUR) / MINUTE;
            return duration / HOUR + "h:"
                    + (m < 10 ? "0" : "") + m + "m:"
                    + (s < 10 ? "0" : "") + s + "s:"
                    + (ms < 100 ? "0" : "") + (ms < 10 ? "0" : "") + ms + "ms";
        }
    }

    public static void main(String[] args) {
        final long [] times = { 100123L, 1100123L, 6100123, 3600123, 3660123, 3661123, 3662123 };
        for(final long t : times) {
            System.out.println("timeInText(" + t + " --timeInText-->" + timeInText(t));
        }
    }
}