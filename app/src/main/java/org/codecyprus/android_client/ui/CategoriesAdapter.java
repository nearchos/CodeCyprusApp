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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.codecyprus.android_client.Preferences;
import org.codecyprus.android_client.R;
import org.codecyprus.android_client.SerializableSession;
import org.codecyprus.android_client.model.Category;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Nearchos Paspallis on 24/12/13.
 */
public class CategoriesAdapter extends ArrayAdapter<Category>
{
    public static final String TAG = "codecyprus";

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    static
    {
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
//    public static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("dd-MMM HH:mm");

    private final LayoutInflater layoutInflater;

    public CategoriesAdapter(final Context context, final Category [] categories)
    {
        super(context, R.layout.list_item_category, categories);

        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override public View getView(final int position, final View convertView, final ViewGroup parent)
    {
        final View view = layoutInflater.inflate(R.layout.list_item_category, null);

        final Category category = getItem(position);
        final SerializableSession serializableSession = Preferences.getSession(getContext(), category.getUUID());

        // Creates a ViewHolder and store references to the two children views we want to bind data to.
        assert view != null;
        view.findViewById(R.id.list_item_category_active).setVisibility(serializableSession == null ? View.GONE : View.VISIBLE);
        final TextView categoryName = (TextView) view.findViewById(R.id.list_item_category_name);
        final TextView categoryValidDateAndTime = (TextView) view.findViewById(R.id.list_item_category_valid_date_and_time);

        // Bind the data efficiently with the holder.
        categoryName.setText(category.getName());

        final long now = System.currentTimeMillis();
        try
        {
            final Date validFrom = SIMPLE_DATE_FORMAT.parse(category.getValidFrom());
            final Date validUntil = SIMPLE_DATE_FORMAT.parse(category.getValidUntil());
            if(now < validFrom.getTime())
            {
                categoryValidDateAndTime.setText(getContext().getString(R.string.Going_live_in, timeInText(validFrom.getTime() - now)));
                view.setAlpha(0.4f);
            }
            else if(validFrom.getTime() <= now && now < validUntil.getTime())
            {
                categoryValidDateAndTime.setText(getContext().getString(R.string.Ends_in, timeInText(validUntil.getTime() - now)));
            }
            else // assert now > validUntil.getTime()
            {
                categoryValidDateAndTime.setText(getContext().getString(R.string.Finished_ago, timeInText(now - validUntil.getTime())));
                view.setAlpha(0.4f);
            }
        }
        catch (ParseException pe)
        {
            Log.e(TAG, pe.getMessage());
        }

        return view;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
//        final Category category = getItem(position);
//        final long now = System.currentTimeMillis();
//
//        try
//        {
//            final Date validFrom = SIMPLE_DATE_FORMAT.parse(category.getValidFrom());
//            final Date validUntil = SIMPLE_DATE_FORMAT.parse(category.getValidUntil());
//            return validFrom.getTime() <= now && now <= validUntil.getTime();
//        }
//        catch (ParseException pe)
//        {
//            Log.e(TAG, pe.getMessage());
//            return true;
//        }
    }

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60L * SECOND;
    public static final long HOUR = 60L * MINUTE;
    public static final long DAY = 24L * HOUR;
    public static final long WEEK = 7L * DAY;

    private String timeInText(final long duration)
    {
        if(duration < SECOND)
        {
            return getContext().getString(R.string.just_now);
        }
        else if(duration < MINUTE)
        {
            final String dt = Long.toString(duration / SECOND);
            return getContext().getString(R.string.seconds, dt);
        }
        else if(duration < HOUR)
        {
            final String dt = Long.toString(duration / MINUTE);
            return getContext().getString(R.string.minutes, dt);
        }
        else if(duration < DAY)
        {
            final String dt = Long.toString(duration / HOUR);
            return getContext().getString(R.string.hours, dt);
        }
        else if(duration < 2 * WEEK)
        {
            final String dt = Long.toString(duration / DAY);
            return getContext().getString(R.string.days, dt);
        }
        else
        {
            final String dt = Long.toString(duration / WEEK);
            return getContext().getString(R.string.weeks, dt);
        }
    }
}