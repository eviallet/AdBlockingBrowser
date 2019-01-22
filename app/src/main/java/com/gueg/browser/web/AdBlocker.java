package com.gueg.browser.web;


import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {
    private static final String AD_HOSTS_FILE = "ad_hosts.txt";
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
        return !TextUtils.isEmpty(url) && isAdHost(url);
    }

    private static boolean isAdHost(String url) {
        boolean domainFound = false;
        int i=0;
        StringBuilder domain = new StringBuilder();
        char c;
        while(!domainFound&&i<url.length()) {
            c = url.charAt(i);
            domain.append(c);
            if(domain.toString().contains("http://")|| domain.toString().contains("https://")|| domain.toString().contains("www.")|| domain.toString().matches("ww[0-9]."))
                domain = new StringBuilder();
            if(domain.toString().contains(".fr")|| domain.toString().contains(".com")|| domain.toString().contains(".net")|| domain.toString().contains(".co.uk")|| domain.toString().contains(".de"))
                domainFound=true;

            i++;
        }
        return AD_HOSTS.contains(domain.toString());
    }

    static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
