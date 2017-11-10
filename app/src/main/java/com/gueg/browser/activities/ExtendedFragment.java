package com.gueg.browser.activities;

import android.support.v4.app.Fragment;

import com.gueg.browser.thumbnails.Thumbnail;


public abstract class ExtendedFragment extends Fragment {
    public abstract Thumbnail getThumbnail();
    public abstract void setTempThumbnail(Thumbnail thumbnail);
}
