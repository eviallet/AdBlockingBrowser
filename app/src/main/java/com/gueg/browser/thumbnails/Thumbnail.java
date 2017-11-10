package com.gueg.browser.thumbnails;

import android.graphics.Bitmap;

public class Thumbnail {
    public String title;
    public String url;
    public Bitmap image;

    public Thumbnail(String t, String u, Bitmap b) {
        title = t;
        url = u;
        image = b;
    }

}