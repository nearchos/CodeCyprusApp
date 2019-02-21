package org.codecyprus.android_client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.codecyprus.android_client.BuildConfig;
import org.codecyprus.android_client.R;

import java.util.List;

public class ActivityAbout extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new AboutFragment())
                .commit();

        getListView().setBackgroundResource(R.drawable.app_background);
    }

    public static class AboutFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.about);

            {
                final Preference rateUsPreference = findPreference("rateUs");
                rateUsPreference.setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=" + BuildConfig.APPLICATION_ID)));
                    return false;
                });
            }
            {
                final Preference sharePreference = findPreference("share");
                sharePreference.setOnPreferenceClickListener(preference -> {
                    final String title = getString(R.string.app_name);
                    final String text = getString(R.string.Install);
                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    final String shareText = getString(R.string.Share);
                    if(isIntentAvailable(getActivity(), shareIntent)) {
                        startActivity(Intent.createChooser(shareIntent, shareText));
                    } else {
                        Toast.makeText(getActivity(), "No apps available for sharing", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                });
            }
            {
                final Preference applicationVersionPreference = findPreference("applicationVersion");
                applicationVersionPreference.setSummary(BuildConfig.VERSION_NAME);
            }
            {
                final Preference applicationCodePreference = findPreference("applicationCode");
                applicationCodePreference.setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nearchos/CodeCyprusOrg")));
                    return false;
                });
            }
        }
    }

    private static boolean isIntentAvailable(final Context context, final Intent intent) {
        if(context == null) return false;
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}