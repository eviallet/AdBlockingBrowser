package com.gueg.browser.web.bookmarks;

public interface BookmarkInterface {
    void onBookmarkAdded(Bookmark b);
    void onCancel();
    void onBookmarkRemoved();
}
