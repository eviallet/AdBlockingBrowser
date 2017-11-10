package com.gueg.browser.web.bookmarks;


import android.graphics.Bitmap;

public class Bookmark implements java.io.Serializable{

    public Bookmark(String name, String url, Bitmap pic) {
        mName = name;
        mUrl = url;
        mPic = pic;
    }


    private String mName;
    private String mUrl;
    private Bitmap mPic;

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public Bitmap getPic() {
        return mPic;
    }

    @Override
    public String toString() {
        return mName;
    }

}
