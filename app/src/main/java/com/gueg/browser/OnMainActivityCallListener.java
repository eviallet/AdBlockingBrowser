package com.gueg.browser;


import com.gueg.browser.web.WebFragment;

public interface OnMainActivityCallListener {
    void onRefresh();
    void onPageLoaded(String title, String url);
    void onSetCurrentFragment(int posFrag, int posLastFrag);
    void onNewTab(String url,int pos);
    void showManager();
    void onTabSwipe(boolean direction);
    void addToErrors(WebFragment frag);
}
