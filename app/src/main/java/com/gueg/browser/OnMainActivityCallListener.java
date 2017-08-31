package com.gueg.browser;

public interface OnMainActivityCallListener {
    void onRefresh();
    void onSetCurrentFragment(int posFrag);
    void onNewTab(String url,int pos);
}
