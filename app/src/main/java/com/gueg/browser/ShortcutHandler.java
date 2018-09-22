package com.gueg.browser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gueg.browser.web.bookmarks.DbBitmapUtility;

public class ShortcutHandler extends FragmentActivity {

    private static final int ACTIVITY_SHORTCUT = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("SHORTCUT",true);
        startActivityForResult(intent,ACTIVITY_SHORTCUT);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_SHORTCUT) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle results = data.getExtras();
                if(results!=null) {
                    String title = results.getString("shortcut_title");
                    String url = results.getString("shortcut_url");
                    byte[] pic = results.getByteArray("shortcut_pic");

                    new DbBitmapUtility();
                    Bitmap icon = DbBitmapUtility.getImage(pic);

                    Intent toLaunchOnShortcut = new Intent(getApplicationContext(), MainActivity.class);
                    toLaunchOnShortcut.putExtra("LINK", url);

                    Intent shortcut = new Intent();
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, toLaunchOnShortcut);
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
                    setResult(Activity.RESULT_OK, shortcut);
                    finishAndRemoveTask();
                }
            }
        }
    }
}
