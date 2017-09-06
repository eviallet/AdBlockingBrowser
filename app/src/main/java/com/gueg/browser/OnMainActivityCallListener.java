package com.gueg.browser;

interface OnMainActivityCallListener {
    void onRefresh();
    void onSetCurrentFragment(int posFrag);
    void onNewTab(String url,int pos);
}
