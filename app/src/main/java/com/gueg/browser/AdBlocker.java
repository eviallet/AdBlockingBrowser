package com.gueg.browser;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {
    private static final String AD_HOSTS_FILE = "pgl.yoyo.org.txt";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void init(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    loadFromAssets(context);
                } catch (IOException e) {
                    // noop
                }
                return null;
            }
        }.execute();
    }

    @WorkerThread
    private static void loadFromAssets(Context context) throws IOException {
        BufferedReader reader;

        InputStream file = context.getAssets().open(AD_HOSTS_FILE);
        reader = new BufferedReader(new InputStreamReader(file));
        String line;
        while((line = reader.readLine()) != null) {
            AD_HOSTS.add(line);
        }
    }
    static boolean isAd(String url) {
        if(TextUtils.isEmpty(url)) {
            return false;
        }
        return isAdHost(url);
    }

    private static boolean isAdHost(String url) {
        boolean domainFound = false;
        int i=0;
        String domain = "";
        char c;
        while(!domainFound&&i<url.length()) {
            c = url.charAt(i);
            domain+=c;
            if(domain.contains("http://")||domain.contains("https://")||domain.contains("www.")||domain.matches("ww[0-9]."))
                domain="";
            if(domain.contains(".fr")||domain.contains(".com")||domain.contains(".net"))
                domainFound=true;

            i++;
        }
        return AD_HOSTS.contains(domain);
    }

    static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
