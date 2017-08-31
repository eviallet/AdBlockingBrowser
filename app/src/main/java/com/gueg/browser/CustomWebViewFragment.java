package com.gueg.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class CustomWebViewFragment extends WebViewFragment {

    private OnPageLoadedListener mPageLoadedListener;
    private OnMainActivityCallListener mMainActivityListener;

    int MENU_CANCEL = 1;
    int MENU_NEWTABLINK = 2;
    int MENU_NEWTABIMAGE = 3;
    int MENU_DOWNLOADIMAGE = 4;
    int MENU_NEWTABLINK_BKG = 5;


    View rootView;
    WebView web;
    EditText text;
    ImageView image;
    ImageButton btn_onglet;
    int posFrag;
    private String toLoad;
    String homepage;
    int colorMain;
    int colorBar;
    boolean blockAds;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMainActivityListener = (OnMainActivityCallListener) context;
        } catch (ClassCastException castException) {
            // The activity does not implement the listener.
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        super.onCreateView(inflater,container,savedInstanceState);
        web = (WebView) rootView.findViewById(R.id.webView);
        RelativeLayout rel = (RelativeLayout) rootView.findViewById(R.id.relatLayoutWeb);
        text = (EditText) rootView.findViewById(R.id.webViewText);
        image = (ImageView) rootView.findViewById(R.id.imageViewWeb);
        btn_onglet = (ImageButton) rootView.findViewById(R.id.cwvTabs);

        AdBlocker.init(getActivity());

        SharedPreferences mainPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        homepage = mainPref.getString("prefHomepage","http://www.google.fr");
        colorMain = mainPref.getInt("prefColorMain",R.color.colorPrimary);
        colorBar =  mainPref.getInt("prefColorBar",R.color.colorTextWebView);

        rel.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        text.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        image.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        btn_onglet.setBackgroundTintList(ColorStateList.valueOf(colorBar));

        blockAds = mainPref.getBoolean("prefAdBlock",true);

        boolean isChecked = mainPref.getBoolean("prefColorBarText",true);

        if(isChecked)
            text.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorTextBlack)));
        else
            text.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorTextWhite)));



        assert btn_onglet != null;
        btn_onglet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityListener.onSetCurrentFragment(-1);
            }
        });



        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText(web.getUrl());
                text.selectAll();
            }
        });

        text.setImeOptions(EditorInfo.IME_ACTION_DONE);

        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(text.getText().toString().toLowerCase().contains("http://")||text.getText().toString().toLowerCase().contains("https://")) {
                        loadUrl(text.getText().toString());
                    }
                    else if(text.getText().toString().toLowerCase().contains("www")) {
                        String display = "http://"+text.getText().toString();
                        text.setText(display);
                        loadUrl(display);
                    }
                    else if(text.getText().toString().toLowerCase().contains(".fr")||text.getText().toString().toLowerCase().contains(".com")) {
                        String display = "http://www."+text.getText().toString();
                        text.setText(display);
                        loadUrl(display);
                    }
                    else
                        loadUrl("https://www.google.fr/search?q="+text.getText().toString());

                }
                return false;
            }
        });


        setOnPageLoadedListener(new OnPageLoadedListener() {
            @Override
            public void onPageLoaded(WebPage page) {
                mMainActivityListener.onRefresh();
            }
        });


        CookieManager.getInstance().setAcceptCookie(true);


        web.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(intent);
                    Toast.makeText(getActivity().getApplicationContext(), "Downloading File",Toast.LENGTH_LONG).show();
            }
        });


        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                result.cancel();
                return true;
            }
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setMax(100);
                progressBar.setProgressTintList(ColorStateList.valueOf(colorMain));
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);

                } else {
                    progressBar.setVisibility(View.VISIBLE);

                }
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                text.setText(title);
                mMainActivityListener.onRefresh();
            }
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                image.setImageBitmap(icon);
                mMainActivityListener.onRefresh();
            }
        });



        if(savedInstanceState==null)
            web.loadUrl(homepage);
        WebSettings webSettings = web.getSettings();
        web.requestFocus();


        // Enable Javascript
        if (webSettings != null) {
            webSettings.setJavaScriptEnabled(true);
            // Enable zoom
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            // Load with overview
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }


        web.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                if(blockAds)
                    return AdBlocker.isAd(url) ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(view, url);
                else
                    return super.shouldInterceptRequest(view,url);

            }

              @Override
              public void onPageStarted(WebView view, String url, Bitmap favIcon) {
                  view.requestFocus();
              }

              @Override
              public void onPageFinished(WebView view, String url) {
                  if (mPageLoadedListener != null)
                      mPageLoadedListener.onPageLoaded(new WebPage(web.getTitle(), web.getUrl(), web.getFavicon()));


                  mMainActivityListener.onRefresh();

              }
        });


        web.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    final WebView.HitTestResult result = ((WebView)v).getHitTestResult();

                    MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId()==MENU_CANCEL)
                                menu.close();
                            else if(item.getItemId()==MENU_NEWTABLINK) {
                                mMainActivityListener.onNewTab(result.getExtra(),-1);
                            }
                            else if(item.getItemId()==MENU_NEWTABLINK_BKG) {
                                mMainActivityListener.onNewTab(result.getExtra(),posFrag);
                            }
                            else if(item.getItemId()==MENU_NEWTABIMAGE) {
                                mMainActivityListener.onNewTab(result.getExtra(),-1);
                            }
                            else if(item.getItemId()==MENU_DOWNLOADIMAGE) {
                                Bitmap image = null;
                                try {
                                    image = urlToPicture(result.getExtra());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                downloadPicture(image);
                            }
                            return true;
                        }
                    };

                    if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                            result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        menu.add(Menu.NONE, MENU_NEWTABIMAGE, Menu.NONE, "Ouvrir l'image dans un nouvel onglet").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_DOWNLOADIMAGE, Menu.NONE, "Télécharger l'image").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, "Annuler").setOnMenuItemClickListener(handler);
                    } else if (result.getType() == WebView.HitTestResult.ANCHOR_TYPE ||
                            result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                        menu.add(Menu.NONE, MENU_NEWTABLINK, Menu.NONE, "Ouvrir dans un nouvel onglet").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_NEWTABLINK_BKG, Menu.NONE, "Ouvrir en arrière plan").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, "Annuler").setOnMenuItemClickListener(handler);
                    }
                }

            });

        return rootView;
    }


    public void setPos(int pos) {
        posFrag=pos;
    }


    public void loadUrl(String url) {
        web.loadUrl(url);
        web.requestFocus();
    }

    public void setOnStartUrl(String url) {
        toLoad = url;
    }




    public boolean canGoBack() {
        return web.canGoBack();
    }

    public void goBack() {
        web.goBack();
    }

    public WebView getWeb() {
        return web;
    }

    @Override
    public void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        web.onResume();
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        web.loadUrl(toLoad);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        web.onPause();
        super.onDestroy();
    }




    public WebPage getWebPage() {
        return new WebPage(web.getTitle(),web.getUrl(),web.getFavicon());
    }

    public void setOnPageLoadedListener(OnPageLoadedListener listener) {
        mPageLoadedListener = listener;
    }


    public void downloadPicture(Bitmap image) {

        // TODO - crash

        if (image != null) {
            ByteArrayOutputStream mByteArrayOS = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 90, mByteArrayOS);
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath());
                File file = new File(dir, "download.jpg");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(mByteArrayOS.toByteArray());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap urlToPicture(String url) throws IOException {
        Bitmap x = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();

            x = BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return x;
    }



}