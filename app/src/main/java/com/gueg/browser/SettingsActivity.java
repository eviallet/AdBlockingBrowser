package com.gueg.browser;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference button = findPreference("prefShare");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i=new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Partager");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "https://drive.google.com/file/d/0B0IkweJhZVSfSWM0Z0lVWlNQSms/view?usp=sharing");
                startActivity(Intent.createChooser(i,"Partager avec"));
                return true;
            }
        });

    }

}
