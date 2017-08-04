package com.gueg.browser;

import android.graphics.Bitmap;

class WebPage {
    private String title;
    private String url;
    private Bitmap pic;
    WebPage(String newTitle, String newUrl, Bitmap newPic) {
        title=newTitle;
        url=newUrl;
        pic=newPic;
    }
    String getTitle() {
        return title;
    }
    String getUrl() {
        return url;
    }
    Bitmap getPic() {
        return pic;
    }
}