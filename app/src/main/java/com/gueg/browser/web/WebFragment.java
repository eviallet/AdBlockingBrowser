package com.gueg.browser.web;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListPopupWindow;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gueg.browser.activities.ExtendedFragment;
import com.gueg.browser.R;
import com.gueg.browser.activities.MainActivity;
import com.gueg.browser.activities.OnMainActivityCallListener;
import com.gueg.browser.thumbnails.Thumbnail;

import java.io.File;
import java.net.URISyntaxException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.DOWNLOAD_SERVICE;


@SuppressWarnings("deprecation")
public class WebFragment extends ExtendedFragment implements AdapterView.OnItemClickListener {

    private OnMainActivityCallListener mMainActivityListener;

    int MENU_CANCEL = 1;
    int MENU_NEWTABLINK = 2;
    int MENU_NEWTABIMAGE = 3;
    int MENU_DOWNLOADIMAGE = 4;
    int MENU_NEWTABLINK_BKG = 5;
    int MENU_COPY_LINK = 6;

    public boolean oneTabMode = false;
    String mainUrl = "";

    boolean userAgentMobile = true;
    String userAgentDefault;

    ListPopupWindow menu;
    String[] menuChoices={"Rafraichir la page", "Suivant", "Rechercher sur la page", "Voir le code source", "Dupliquer la page", "Copier le lien", "Partager le lien"};
    String[] menuChoicesOneTabMode={"Rafraichir la page", "Suivant", "Rechercher sur la page", "Voir le code source", "Ouvrir l'application complète", "Copier le lien", "Partager le lien"};

    //"(Ne pas bloquer les pubs ici)"
    //"(Rechercher un flux rss)"

    // Find on page
    String searching;
    RelativeLayout findOnPageLayout;
    EditText search;
    ImageButton findOnPagePrev;
    ImageButton findOnPageNext;

    boolean isBarHidden = false;

    Thumbnail tempThumbnail = null;

    WebSettings webSettings;
    SharedPreferences mainPref;
    View rootView;
    WebView web;
    EditText text;
    ImageView image;
    ImageButton btn_onglet;
    RelativeLayout rel;
    int posFrag;
    private String toLoad;
    String homepage;
    ProgressBar anim;
    ProgressBar progressBar;
    int colorMain;
    int colorBar;
    boolean textUrlShown = false;
    boolean loading = false;

    float bkpProgressBarPos = 0;

    static final int DOWN = 0;
    static final int UP = 0;
    int currentStatus;

    int margin;
    int pos;
    boolean canHideBar = false;
    Handler scrollHandler = new Handler();

    // TODO NEW TAB FROM IMG

    boolean fragHasBeenLoaded = false;

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
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int cx = getActivity().getWindow().getDecorView().getWidth()/2;
                int cy = getActivity().getWindow().getDecorView().getHeight()/3;
                int width = getActivity().getWindow().getDecorView().getWidth();
                int height = getActivity().getWindow().getDecorView().getHeight();

                float finalRadius = Math.max(width, height) / 2 + Math.max(width - cx, height - cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                anim.setDuration(1000);
                anim.start();


                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)web.getLayoutParams();
                params.setMargins(0,rel.getHeight(),0,0);
                web.setLayoutParams(params);

                fragHasBeenLoaded = true;
            }
        });
        web = rootView.findViewById(R.id.webView);
        rel = rootView.findViewById(R.id.relatLayoutWeb);
        text = rootView.findViewById(R.id.webViewText);
        image = rootView.findViewById(R.id.imageViewWeb);
        btn_onglet = rootView.findViewById(R.id.cwvTabs);
        anim = rootView.findViewById(R.id.progressAnim);

        mainPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        refreshColor();

        anim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.stopLoading();
            }
        });


        assert btn_onglet != null;
        btn_onglet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!oneTabMode) {
                mMainActivityListener.onRefresh();
                mMainActivityListener.onSetCurrentFragment(-1, posFrag);
            }
            }
        });
        btn_onglet.setLongClickable(true);
        menu = new ListPopupWindow(getContext());
        menu.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,menuChoices));
        menu.setAnchorView(btn_onglet);
        menu.setWidth(600);
        menu.setOnItemClickListener(this);
        btn_onglet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                menu.show();
                return true;
            }
        });



        web.setOnTouchListener(new View.OnTouchListener() {
            private float posY=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        posY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(mainPref.getBoolean("prefHideBar",true)&&event.getY()-posY<-300&&!isBarHidden&&canHideBar) {
                            if(currentStatus!=DOWN)
                                scrollHandler.removeCallbacks(null);
                            currentStatus=DOWN;
                            margin=rel.getHeight();
                            final float percentage = margin/10;
                            rel.animate().translationYBy(-margin).setDuration(200).start();
                            bkpProgressBarPos = progressBar.getY();
                            progressBar.setY(-margin);
                            scrollHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)web.getLayoutParams();
                                    params.setMargins(0,margin-=percentage,0,0);
                                    web.setLayoutParams(params);
                                    if(margin>0)
                                        scrollHandler.postDelayed(this,2);
                                    else
                                        rel.setVisibility(View.INVISIBLE);
                                }
                            });
                            if(!isBarHidden)
                                isBarHidden = true;
                        } else if(event.getY()>posY+200&&isBarHidden){
                            if(currentStatus!=UP)
                                scrollHandler.removeCallbacks(null);
                            currentStatus=UP;
                            rel.setY(-rel.getHeight());
                            rel.setVisibility(View.VISIBLE);
                            margin=rel.getHeight();
                            progressBar.setY(bkpProgressBarPos);
                            isBarHidden = false;
                            final float percentage = margin/10;
                            pos=0;
                            scrollHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)web.getLayoutParams();
                                    params.setMargins(0,pos+=percentage,0,0);
                                    web.setLayoutParams(params);
                                    if(pos<margin)
                                        scrollHandler.postDelayed(this,2);
                                    else
                                        params.setMargins(0, margin, 0, 0);
                                }
                            });
                            rel.animate().translationY(0).setDuration(200).start();
                        }
                        break;
                }
                return false;
            }
        });

        findOnPageLayout = rootView.findViewById(R.id.webfindonpagelayout);
        findOnPageNext = rootView.findViewById(R.id.webfindonpagenext);
        findOnPagePrev = rootView.findViewById(R.id.webfindonpageprev);
        search = rootView.findViewById(R.id.webfindonpage);


        ImageButton fopCancel = rootView.findViewById(R.id.webfindonpagecancel);
        fopCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findOnPageLayout.setVisibility(View.GONE);
                web.clearMatches();
            }
        });
        findOnPageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search.getText().length() > 0 && searching == null) {
                    searching = search.getText().toString();
                    web.findAllAsync(searching);
                } else if (search.getText().length() == 0) {
                    web.clearMatches();
                    searching = null;
                } else if (search.getText().toString().equals(searching)) {
                    web.findNext(true);
                } else if (!search.getText().toString().equals(searching)) {
                    searching = search.getText().toString();
                    web.findAllAsync(searching);
                }
            }
        });
        findOnPagePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search.getText().length() > 0 && searching == null) {
                    searching = search.getText().toString();
                    web.findAllAsync(searching);
                } else if (search.getText().length() == 0) {
                    web.clearMatches();
                    searching = null;
                } else if (search.getText().toString().equals(searching)) {
                    web.findNext(false);
                } else if (!search.getText().toString().equals(searching)) {
                    searching = search.getText().toString();
                    web.findAllAsync(searching);
                }
            }
        });


        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textUrlShown){
                    ((EditText) v).setText(web.getUrl());
                    ((EditText) v).selectAll();
                    textUrlShown = true;
                }
            }
        });

        text.setOnTouchListener(new View.OnTouchListener() {
            float mLastMotionX;
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                final int action = ev.getAction();
                final float x = ev.getX();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if(((EditText)v).getInputType()==InputType.TYPE_NULL)
                            ((EditText)v).setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        mLastMotionX = x;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        final int deltaX = (int) (x - mLastMotionX);
                        ((EditText) v).setInputType(InputType.TYPE_NULL);

                        if (Math.abs(deltaX) >= 80) {
                            mMainActivityListener.onTabSwipe(deltaX >= 0);
                            mLastMotionX = x;
                        }

                        return true;
                }
                return false;
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


        CookieManager.getInstance().setAcceptCookie(true);


        progressBar = rootView.findViewById(R.id.progressBar);
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
                    progressBar.bringToFront();
                }

            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                text.setText(title);
                if(!oneTabMode)
                    mMainActivityListener.onRefresh();
            }
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                image.setImageBitmap(icon);
            }
        });

        web.setAnimationCacheEnabled(false);
        web.setDrawingCacheEnabled(true);
        web.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (((MainActivity)getActivity()).checkPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    DownloadManager dm = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getActivity(), "Téléchargement en cours...", Toast.LENGTH_LONG).show();
                } else {
                    ((MainActivity)getActivity()).requestPermission(WRITE_EXTERNAL_STORAGE);
                }

            }
        });


        if(savedInstanceState==null)
            web.loadUrl(homepage);
        webSettings = web.getSettings();
        web.requestFocus();


        // Enable Javascript
        if (webSettings != null) {
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setSupportZoom(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }


        web.setWebViewClient(new SSLTolerentWebViewClient());


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
                                downloadPicture(result.getExtra());
                            }
                            else if(item.getItemId()==MENU_COPY_LINK) {
                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Lien", result.getExtra());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "Lien copié", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    };

                    if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                            result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        if(!oneTabMode) {
                            menu.add(Menu.NONE, MENU_NEWTABLINK, Menu.NONE, "Ouvrir dans un nouvel onglet").setOnMenuItemClickListener(handler);
                            menu.add(Menu.NONE, MENU_NEWTABLINK_BKG, Menu.NONE, "Ouvrir en arrière plan").setOnMenuItemClickListener(handler);
                            menu.add(Menu.NONE, MENU_NEWTABIMAGE, Menu.NONE, "Ouvrir l'image dans un nouvel onglet").setOnMenuItemClickListener(handler);
                        }
                        menu.add(Menu.NONE, MENU_DOWNLOADIMAGE, Menu.NONE, "Télécharger l'image").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_COPY_LINK, Menu.NONE, "Copier le lien").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, "Annuler").setOnMenuItemClickListener(handler);
                    } else if (result.getType() == WebView.HitTestResult.ANCHOR_TYPE ||
                            result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                        if(!oneTabMode) {
                            menu.add(Menu.NONE, MENU_NEWTABLINK, Menu.NONE, "Ouvrir dans un nouvel onglet").setOnMenuItemClickListener(handler);
                            menu.add(Menu.NONE, MENU_NEWTABLINK_BKG, Menu.NONE, "Ouvrir en arrière plan").setOnMenuItemClickListener(handler);
                        }
                        menu.add(Menu.NONE, MENU_COPY_LINK, Menu.NONE, "Copier le lien").setOnMenuItemClickListener(handler);
                        menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, "Annuler").setOnMenuItemClickListener(handler);
                    }
                }

            });

        userAgentDefault = web.getSettings().getUserAgentString();

        return rootView;
    }

    public void setOneTabMode() {
        oneTabMode = true;
        menuChoices = menuChoicesOneTabMode;
        if(menu!=null)
            menu.setAdapter(new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,menuChoices));
    }


    public void onCurrentFragment() {
        if(rel!=null)
            rel.animate().translationZ(25).setDuration(500).start();
        if(btn_onglet!=null)
            btn_onglet.animate().translationZ(20).setDuration(500).start();
    }

    public boolean toggleUserAgent() {
        if (userAgentMobile) {
            web.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            userAgentMobile = false;
            webSettings.setUseWideViewPort(true);
            web.reload();
        } else {
            web.getSettings().setUserAgentString(userAgentDefault);
            userAgentMobile = true;
            webSettings.setUseWideViewPort(false);
            web.reload();
        }
        return userAgentMobile;
    }

    public boolean getUserAgent() {
        return userAgentMobile;
    }



    public void refreshColor() {
        homepage = mainPref.getString("prefHomepage","http://www.google.fr");
        colorMain = mainPref.getInt("prefColorMain",0xffffffff);
        colorBar =  mainPref.getInt("prefColorBar",0xffffffff);


        rel.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        text.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        image.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        btn_onglet.setBackgroundTintList(ColorStateList.valueOf(colorBar));


        boolean isChecked = mainPref.getBoolean("prefColorBarText",true);

        if(isChecked)
            text.setTextColor(ColorStateList.valueOf(0xff000000));
        else
            text.setTextColor(ColorStateList.valueOf(0xffffffff));
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


    public void downloadPicture(String url) {
        if (((MainActivity)getActivity()).checkPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            DownloadManager mdDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            int indexOxHttp = url.indexOf("://");
            File destinationFile = new File(Environment.getExternalStorageDirectory(), url.substring(indexOxHttp + 2, indexOxHttp + 22));
            request.setDescription("Downloading ...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(destinationFile));
            mdDownloadManager.enqueue(request);
        } else {
            ((MainActivity)getActivity()).requestPermission(WRITE_EXTERNAL_STORAGE);
        }
    }




    private class SSLTolerentWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(!url.startsWith("http")) {
                Uri parsedUri = Uri.parse(url);
                PackageManager packageManager = getActivity().getPackageManager();
                Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
                if (browseIntent.resolveActivity(packageManager) != null) {
                    getActivity().startActivity(browseIntent);
                }
                // if no activity found, try to parse intent://
                else {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            getActivity().startActivity(intent);
                        }
                        //try to find fallback url
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null) {
                            web.loadUrl(fallbackUrl);
                        }
                        //invite to install
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                                Uri.parse("market://details?id=" + intent.getPackage()));
                        if (marketIntent.resolveActivity(packageManager) != null) {
                            getActivity().startActivity(marketIntent);
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            web.loadUrl(url);
            return true;
        }



        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,  WebResourceRequest request) {
            return mainPref.getBoolean("prefAdBlock",true)&&
                    !request.getUrl().toString().equals(mainUrl)&&
                    AdBlocker.isAd(request.getUrl().toString()) ?
                    AdBlocker.createEmptyResource() :
                    super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favIcon) {
            mainUrl = url;
            if(textUrlShown)
                textUrlShown = false;
            anim.setVisibility(View.VISIBLE);
            image.setVisibility(View.INVISIBLE);
            loading = true;
            if(isBarHidden) {
                isBarHidden = false;
                rel.animate().translationY(0).setDuration(100).start();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)web.getLayoutParams();
                params.setMargins(0,rel.getHeight(),0,0);
                web.setLayoutParams(params);
                rel.setVisibility(View.VISIBLE);
                progressBar.setY(bkpProgressBarPos);
                rel.animate().translationY(0).setDuration(100).start();
            }
            //view.requestFocus();
            scrollHandler.removeCallbacks(null);
            canHideBar = false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loading = false;
            anim.setVisibility(View.INVISIBLE);
            image.setVisibility(View.VISIBLE);
            final int animDuration = 150;
            image.setImageBitmap(web.getFavicon());
            image.animate().scaleX(1.25f).scaleY(1.25f).setDuration(animDuration).start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    image.animate().scaleX(1).scaleY(1).setDuration(animDuration).start();
                }
            },animDuration+20);
            view.loadUrl("javascript:document.getElementsByName('viewport')[0].setAttribute('content', 'initial-scale=1.0,maximum-scale=10.0');");
            canHideBar = true;
            if(!oneTabMode) {
                web.buildDrawingCache();
                mMainActivityListener.onRefresh();
                mMainActivityListener.onPageLoaded(view.getTitle(), view.getUrl());
            }
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            String message = "Erreur de certificat SSL.";
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = "Le certificat est hors de confiance.";
                    break;
                case SslError.SSL_EXPIRED:
                    message = "Le certificat a expiré.";
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = "Le certificat ne correspond pas à l'hôte.";
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = "Le certificat n'est pas encore valide.";
                    break;
            }
            message += " Continuer quand même?";

            builder.setTitle("Erreur de certificat SSL");
            builder.setMessage(message);
            builder.setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }


        @Override
        public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
            if(!oneTabMode)
                mMainActivityListener.addToErrors(WebFragment.this);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(menuChoices[position]) {
            case "Rafraichir la page" :
                web.reload();
                break;
            case "Suivant":
                if(web.canGoForward())
                    web.goForward();
                break;
            case "Ne pas bloquer les pubs ici" :
                break;
            case "Rechercher sur la page" :
                if(findOnPageLayout.getVisibility()==View.GONE) {
                    findOnPageLayout.setVisibility(View.VISIBLE);
                    findOnPageLayout.bringToFront();
                } else {
                    findOnPageLayout.setVisibility(View.GONE);
                    web.clearMatches();
                }
                break;
            case "Rechercher un flux rss" :
                break;
            case "Voir le code source" :
                web.loadUrl("view-source:"+web.getUrl());
                break;
			case "Dupliquer la page":
				mMainActivityListener.onNewTab(web.getUrl(),posFrag);
				break;
            case "Copier le lien" :
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Lien", web.getUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Lien copié", Toast.LENGTH_SHORT).show();
                break;
            case "Partager le lien" :
                Intent i=new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                //i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Partager");
                i.putExtra(android.content.Intent.EXTRA_TEXT, web.getUrl());
                startActivity(Intent.createChooser(i,"Partager avec"));
                break;
            case "Ouvrir l'application complète" :
                Intent intent = getActivity().getPackageManager()
                        .getLaunchIntentForPackage( getActivity().getPackageName() );
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("LINK",web.getUrl());
                startActivity(intent);
                break;
        }
        menu.dismiss();
    }

    @Override
    public Thumbnail getThumbnail() {
        if(fragHasBeenLoaded) {
            web.buildDrawingCache();
            return new Thumbnail(web.getTitle(), web.getUrl(), web.getDrawingCache());
        } else
            return tempThumbnail;
    }

    @Override
    public void setTempThumbnail(Thumbnail t) {
        tempThumbnail = t;
    }




}