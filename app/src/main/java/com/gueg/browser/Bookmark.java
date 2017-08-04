package com.gueg.browser;


import android.graphics.Bitmap;

class Bookmark implements java.io.Serializable{

    Bookmark(String name, String url, Bitmap pic) {
        mName = name;
        mUrl = url;
        mPic = pic;
    }


    private String mName;
    private String mUrl;
    private Bitmap mPic;

    String getName() {
        return mName;
    }

    String getUrl() {
        return mUrl;
    }

    Bitmap getPic() {
        return mPic;
    }

    @Override
    public String toString() {
        return mName;
    }

}
