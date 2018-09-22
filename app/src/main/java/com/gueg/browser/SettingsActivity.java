package com.gueg.browser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.gueg.browser.update.UpdateTask;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


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

        final Preference history = findPreference("prefHistory");
        history.setSummary(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("prefHistory","Désactivé"));
        if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("prefHistory","Désactivé").equals("0"))
            history.setSummary("Désactivé");
        history.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putString("prefHistory",(String)newValue).apply();
                history.setSummary((String)newValue);
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
                if(ContextCompat.checkSelfPermission(getApplicationContext(),WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    UpdateTask update = new UpdateTask(SettingsActivity.this, UpdateTask.SHOW_TOAST);
                    update.execute(UpdateTask.UPDATE_LINK);
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, WRITE_EXTERNAL_STORAGE))
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 0);
                }
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
