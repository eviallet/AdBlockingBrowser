package com.gueg.browser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Preference btn_share = findPreference("prefShare");
        btn_share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i=new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Partager");
                i.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_link));
                startActivity(Intent.createChooser(i,"Partager avec"));
                return true;
            }
        });

        Preference homepage = findPreference("prefHomepage");
        SharedPreferences mainPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        homepage.setSummary(mainPref.getString("prefHomepage","http://www.google.fr"));

        final Preference btn_update = findPreference("prefUpdate");

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            btn_update.setSummary(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        btn_update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.share_link)));
                startActivity(browserIntent);
                return true;
            }
        });

        Preference colorMain = findPreference("prefColorMain");
        Preference colorBarText = findPreference("prefColorBarText");
        Preference colorBar = findPreference("prefColorBar");


        colorMain.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putInt("prefColorMain",(int)newValue).apply();
                return true;
            }
        });

        colorBarText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putBoolean("prefColorBarText",(boolean)newValue).apply();
                return true;
            }
        });

        colorBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putInt("prefColorBar",(int)newValue).apply();
                return true;
            }
        });



    }

}
