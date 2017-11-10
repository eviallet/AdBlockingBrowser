package com.gueg.browser.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Explode;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gueg.browser.update.UpdateTask;
import com.gueg.browser.web.AdBlocker;
import com.gueg.browser.web.bookmarks.BookmarkDialog;
import com.gueg.browser.web.bookmarks.BookmarkInterface;
import com.gueg.browser.web.history.HistoryAdapter;
import com.gueg.browser.web.history.sql.SQLUtility;
import com.gueg.browser.thumbnails.Thumbnail;
import com.gueg.browser.thumbnails.ThumbnailsSaver;
import com.gueg.browser.web.history.HistoryItem;
import com.gueg.browser.web.WebFragment;
import com.gueg.browser.rss.RssFragment;
import com.gueg.browser.web.bookmarks.DbBitmapUtility;
import com.gueg.browser.R;
import com.gueg.browser.web.bookmarks.utilities.RecyclerItemClickListener;
import com.gueg.browser.thumbnails.ThumbnailsFragment;
import com.gueg.browser.web.bookmarks.utilities.VerticalSpaceItemDecoration;
import com.gueg.browser.web.bookmarks.Bookmark;
import com.gueg.browser.web.bookmarks.BookmarkSortActivity;
import com.gueg.browser.web.bookmarks.BookmarksCardsAdapter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements OnMainActivityCallListener {


    int ACTIVITY_RESULTS_BTN_FAV_SORT = 1;
    // Fragment managing
    FragmentManager manager = getSupportFragmentManager();
    ThumbnailsFragment tab_manager;
    FrameLayout fragment_container;
    ArrayList<ExtendedFragment> fragments = new ArrayList<>();
    WebFragment currentFragment;
    int currentFragmentPos;
    boolean currentFragmentRss = false;
    // URLS
    String lastClosed;
    boolean opening = false;
    ArrayList<Thumbnail> thumbnails;
    // Bookmarks
    ArrayList<Bookmark> bookmarksList;
    BookmarksCardsAdapter mAdapter;
    RecyclerView bookmarksDrawer;
    SharedPreferences sharedPrefFavs;
    private static final int VERTICAL_ITEM_SPACE = 15;
    // Network info
    boolean isConnected;
    boolean lastState;
    // Launched via shortcut intent
    boolean shortcut = false;
    //
    ImageButton btn_useragent;

    SharedPreferences prefs;

    WebFragment oneTabFrag;

    boolean oneTabMode = false;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("color")) {
                        refreshColor();
                    }
                }
            };

    private final BroadcastReceiver connectivityChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                refreshWebViews();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Inflating layout

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());
        setContentView(R.layout.activity_main);

        AdBlocker.init(this);
        // is connected and receiver

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        lastState = isConnected;


        registerReceiver(connectivityChanged, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        // Fragments

        fragment_container = (FrameLayout) findViewById(R.id.fragment_container);

        Uri startIntentData = getIntent().getData();
        if (startIntentData != null) {
            String intentUrl = startIntentData.toString();
            if (intentUrl.contains("http://") || intentUrl.contains("https://")) {
                oneTabMode = true;
                oneTabFrag = new WebFragment();
                oneTabFrag.setOnStartUrl(intentUrl);
                oneTabFrag.setOneTabMode();
                manager.beginTransaction().add(fragment_container.getId(),oneTabFrag,"onetab").commit();
                ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }


        if(!oneTabMode) {
            // Shared prefs rssClickListener

            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.registerOnSharedPreferenceChangeListener(prefListener);

            tab_manager = new ThumbnailsFragment();
            manager.beginTransaction().add(fragment_container.getId(), tab_manager, Integer.toString(-1)).commit();

            // Bookmarks ---------------------------------


            bookmarksDrawer = (RecyclerView) findViewById(R.id.recycler_view_bookmarks);
            bookmarksDrawer.setHasFixedSize(true);

            RecyclerView.LayoutManager mLayoutManager;
            mLayoutManager = new LinearLayoutManager(getApplicationContext());
            bookmarksDrawer.setLayoutManager(mLayoutManager);
            bookmarksDrawer.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

            bookmarksList = new ArrayList<>();

            sharedPrefFavs = getSharedPreferences(getString(R.string.bookmarks_list_key), Context.MODE_PRIVATE);

            readBookmarks();

            mAdapter = new BookmarksCardsAdapter(bookmarksList);

            bookmarksDrawer.setAdapter(mAdapter);


            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                    final int position = viewHolder.getAdapterPosition(); //get position which is swiped

                    if (direction == ItemTouchHelper.RIGHT) { // edit
                        editBookmark(position);
                    }
                }

            };


            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(bookmarksDrawer);

            bookmarksDrawer.addOnItemTouchListener(
                    new RecyclerItemClickListener(this, bookmarksDrawer, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {    // load in current tab
                            if (fragments.size() != 0 && getCurrentFragment() != null)
                                getCurrentFragment().loadUrl(bookmarksList.get(position).getUrl());
                            else
                                addTab(bookmarksList.get(position).getUrl());
                            closeDrawer();
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {    // new tab
                            addTab(bookmarksList.get(position).getUrl());
                        }
                    })

            );


            // Buttons --------------------------------------


            ImageButton btn_fav = (ImageButton) findViewById(R.id.btn_drawer_favoris);
            btn_fav.setLongClickable(true);
            btn_fav.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(MainActivity.this, BookmarkSortActivity.class);
                    Bundle bundle = new Bundle();
                    ArrayList<String> bookmarksListString = new ArrayList<>(bookmarksList.size());
                    for (int i = 0; i < bookmarksList.size(); i++) {
                        bookmarksListString.add(bookmarksList.get(i).getName());
                    }
                    bundle.putSerializable("BOOKMARKS_LIST", bookmarksListString);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, ACTIVITY_RESULTS_BTN_FAV_SORT);
                    return true;
                }
            });


            btn_useragent = (ImageButton) findViewById(R.id.btn_drawer_useragent);
            if (getIntent().getBooleanExtra("SHORTCUT", false)) {
                shortcut = true;
                btn_useragent.setImageDrawable(getDrawable(R.drawable.checkmark));
                btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            // Drawer search ----------------------

            final EditText search = (EditText) findViewById(R.id.drawer_search);

            search.setImeOptions(EditorInfo.IME_ACTION_DONE);

            search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (search.getText().toString().toLowerCase().contains("http://") || search.getText().toString().toLowerCase().contains("https://")) {
                            addTab(search.getText().toString());
                            search.setText("");
                        } else if (search.getText().toString().toLowerCase().contains("www")) {
                            String display = "http://" + search.getText().toString();
                            search.setText(display);
                            addTab(display);
                            search.setText("");
                        } else if (search.getText().toString().toLowerCase().contains(".fr") || search.getText().toString().toLowerCase().contains(".com")) {
                            String display = "http://www." + search.getText().toString();
                            search.setText(display);
                            addTab(display);
                            search.setText("");
                        } else {
                            addTab("https://www.google.fr/search?q=" + search.getText().toString());
                            search.setText("");
                        }
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        closeDrawer();

                    }
                    return false;
                }
            });

            searchForUpdates();

            if (prefs.getBoolean("prefDarkTheme", false)) {
                LinearLayout d = (LinearLayout) findViewById(R.id.drawer);
                d.setBackgroundColor(0xff696969);
                btn_fav.setBackgroundColor(0xff696969);
                findViewById(R.id.btn_drawer_rss).setBackgroundColor(0xff696969);
                btn_useragent.setBackgroundColor(0xff696969);
                findViewById(R.id.btn_drawer_parametres).setBackgroundColor(0xff696969);
                findViewById(R.id.btn_drawer_history).setBackgroundColor(0xff696969);
                LinearLayout d1 = (LinearLayout) findViewById(R.id.drawer_buttons_1);
                d1.setBackgroundColor(0xff696969);
                LinearLayout d2 = (LinearLayout) findViewById(R.id.drawer_search_layout);
                d2.setBackgroundColor(0xff696969);
                bookmarksDrawer.setBackgroundColor(0xff696969);
                search.setTextColor(0xffC9C0BE);
            }


            // =================================== FRAGMENTS


            if (prefs.getBoolean("prefSaveTabs", true))
                loadCurrentUrls();

            setCurrentFragment(-1);


            initHistory();


            if (prefs.getBoolean("prefHideKeyboard", false))
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            if (getIntent().getExtras() != null) {
                if (getIntent().getStringExtra("LINK") != null)
                    addTab(getIntent().getStringExtra("LINK"));
                else if (getIntent().getStringExtra("CLEAR_HISTORY") != null && getIntent().getStringExtra("CLEAR_HISTORY").equals("1")) {
                    clearHistory();
                }
            }
        }
    }   // onCreate


    @Override
    public void onBackPressed() {
        if(oneTabMode) {
            if(oneTabFrag.canGoBack())
                oneTabFrag.goBack();
            else
                finish();
            return;
        }

        DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (dl.isDrawerOpen(GravityCompat.START))
            dl.closeDrawer(GravityCompat.START);
        else if (fragments.size() > 0) {
            if(currentFragmentRss) {
                currentFragmentRss = false;
                showManager();
            }
            else if (getCurrentFragment()==null) {
                endMainActivity();
            } else if (getCurrentFragment().canGoBack())
                getCurrentFragment().goBack();
            else
                closeTab(getCurrentFragment());
        } else
            endMainActivity();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(oneTabMode) {
                finish();
                return true;
            }
            endMainActivity();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onPause() {
        if(!oneTabMode)
            writeCurrentUrls();
        super.onPause();
    }



    public void endMainActivity() {
        writeCurrentUrls();
        finish();
    }


    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_drawer_parametres:
                closeDrawer();
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_drawer_favoris:
                if (fragments.size() > 0)
                    newBookmark();
                break;
            case R.id.btn_drawer_rss:
                addRssTab();
                break;
            case R.id.btn_drawer_history:
                showHistoryDialog();
                closeDrawer();
                break;
            case R.id.btn_drawer_useragent:
                closeDrawer();
                if (getIntent().getBooleanExtra("SHORTCUT", false)) {
                    if (getCurrentFragment() != null) {
                        if (getCurrentFragment().getWeb().getFavicon() != null) {
                            Bitmap icon = Bitmap.createScaledBitmap(getCurrentFragment().getWeb().getFavicon(), 248, 248, true);
                            new DbBitmapUtility();
                            byte[] pic = DbBitmapUtility.getBytes(icon);

                            Intent result = new Intent();
                            result.putExtra("shortcut_title", getCurrentFragment().getWeb().getTitle());
                            result.putExtra("shortcut_url", getCurrentFragment().getWeb().getUrl());
                            result.putExtra("shortcut_pic", pic);
                            setResult(Activity.RESULT_OK, result);
                            finish();


                            Toast.makeText(MainActivity.this, "Reccourci crée !", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Chargement de l'image du site...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (getCurrentFragment()!=null) {
                        boolean isMobile = getCurrentFragment().toggleUserAgent();
                        if (isMobile)
                            btn_useragent.setImageDrawable(getDrawable(R.drawable.smartphone));
                        else
                            btn_useragent.setImageDrawable(getDrawable(R.drawable.mouse));
                    }
                }
                break;

        }
    }


    // FRAGMENTS

    private WebFragment getCurrentFragment() {
        return currentFragment;
    }


    public void addTab() {
        FragmentTransaction transaction = manager.beginTransaction();

        WebFragment fragment = new WebFragment();
        fragments.add(fragment);
        transaction.add(R.id.fragment_container, fragment, "frag" + Integer.toString(fragments.size()));
        transaction.commit();
        manager.executePendingTransactions();

        fragment.setPos(fragments.indexOf(fragment));

        setCurrentFragment(fragment);
        currentFragment = fragment;

        refreshTabs();
        writeCurrentUrls();
    }

    public void addTab(String url) {
        FragmentTransaction transaction = manager.beginTransaction();

        WebFragment fragment = new WebFragment();
        fragments.add(fragment);
        fragment.setOnStartUrl(url);
        transaction.add(R.id.fragment_container, fragment, "frag" + Integer.toString(fragments.size()));
        transaction.commit();
        manager.executePendingTransactions();
        setCurrentFragment(fragment);

        fragment.setPos(fragments.indexOf(fragment));
        currentFragment = fragment;

        if(!opening) {
            refreshTabs();
            writeCurrentUrls();
        }
    }


    public void addTab(String url, @SuppressWarnings("UnusedParameters") int posCurTab) {
        FragmentTransaction transaction = manager.beginTransaction();

        WebFragment fragment = new WebFragment();
        fragment.setOnStartUrl(url);
        fragments.add(fragment);

        transaction.add(R.id.fragment_container, fragment, "frag" + Integer.toString(fragments.size()));

        transaction.commit();
        manager.executePendingTransactions();


        fragment.setPos(fragments.indexOf(fragment));

        if(currentFragment!=null)
            setCurrentFragment(currentFragment);
        else
            setCurrentFragment(-1);
        refreshTabs();
        writeCurrentUrls();

    }

    private void addRssTab() {

        RssFragment frag = new RssFragment();
        manager.beginTransaction().add(fragment_container.getId(),frag,"RSS").commit();
        manager.executePendingTransactions();

        hideKeyboard();
        fragments.add(frag);
        thumbnails.add(new Thumbnail("RSS","",null));
        currentFragmentPos = fragments.indexOf(frag);
        frag.setPos(fragments.indexOf(frag));
        onRefresh();
        currentFragmentRss = true;
        closeDrawer();
    }


    public void refreshTabs() {
        if(tab_manager.getAdapter()!=null)
            tab_manager.getAdapter().refresh(thumbnails);
    }

    public void notifyItemRemoved(int pos) {
        if(tab_manager.getAdapter()!=null)
            tab_manager.getAdapter().notifyItemRemoved(pos);
    }

    public void undoClose() {
        if (lastClosed != null && lastClosed.length() > 0) {
            addTab(lastClosed);
            lastClosed = "";
        }
    }

    public void closeTab(int pos) {
        if (pos < fragments.size()) {
            FragmentTransaction transaction = manager.beginTransaction();
            if(fragments.get(pos) instanceof WebFragment)
                lastClosed = ((WebFragment)fragments.get(pos)).getWeb().getUrl();
            transaction.remove(fragments.get(pos));

            fragments.remove(pos);
            transaction.commit();
            manager.executePendingTransactions();
            refreshCurrentUrls();

            writeCurrentUrls();
            notifyItemRemoved(pos);

            setCurrentFragment(-1);
        }
    }

    public void closeTab(ExtendedFragment frag) {
        FragmentTransaction transaction = manager.beginTransaction();

        if(frag instanceof WebFragment)
            lastClosed = ((WebFragment)frag).getWeb().getUrl();
        transaction.remove(frag);

        int pos = fragments.indexOf(frag);
        fragments.remove(pos);
        transaction.commit();
        manager.executePendingTransactions();
        refreshCurrentUrls();
        writeCurrentUrls();
        notifyItemRemoved(pos);
        if (fragments.size() > 0)
            setCurrentFragment(fragments.size() - 1);
        else
            setCurrentFragment(-1);
    }

    public void closeAllTabs() {
        FragmentTransaction transaction = manager.beginTransaction();

        for (int i = 0; i < fragments.size(); i++)
            transaction.remove(fragments.get(i));

        fragments.clear();
        transaction.commit();
        manager.executePendingTransactions();
        thumbnails.clear();
        writeCurrentUrls();
        refreshTabs();
    }


    public void setCurrentFragment(int pos) {
        FragmentTransaction transaction = manager.beginTransaction();
        if(pos==-1)
            transaction.setCustomAnimations(R.anim.nothing,R.anim.zoom_out);
        else
            transaction.setCustomAnimations(R.anim.zoom_in,R.anim.nothing);

        hideKeyboard();

        if (fragments.size() == 0 || pos == -1) {
            for (int i = 0; i < fragments.size(); i++)
                transaction.hide(fragments.get(i));
            transaction.show(tab_manager);
            currentFragment = null;
            currentFragmentPos = -1;
        } else {
            for (int i = 0; i < fragments.size(); i++)
                transaction.hide(fragments.get(i));
            transaction.hide(tab_manager);
            transaction.show(fragments.get(pos));

            if(fragments.get(pos) instanceof WebFragment) {
                WebFragment frag = (WebFragment) fragments.get(pos);
                if (frag.getUserAgent()) {
                    btn_useragent.setImageDrawable(getDrawable(R.drawable.smartphone));
                    btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } else {
                    btn_useragent.setImageDrawable(getDrawable(R.drawable.mouse));
                    btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }

                currentFragment = frag;
                frag.onCurrentFragment();
                currentFragmentRss = false;
            }
            else
                currentFragmentRss = true;


            currentFragmentPos = pos;
        }
        transaction.commit();
        manager.executePendingTransactions();
        closeDrawer();

    }

    public void setCurrentFragment(int pos, @SuppressWarnings("UnusedParameters") boolean direction) {
        FragmentTransaction transaction = manager.beginTransaction();
        if(pos==-1)
            transaction.setCustomAnimations(R.anim.nothing,R.anim.zoom_out);
        else if(direction)
            transaction.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right);
        else
            transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left);

        hideKeyboard();

        if (fragments.size() == 0) {
            transaction.show(tab_manager);
            currentFragment = null;
            currentFragmentPos = -1;
        } else if (pos == -1) {
            transaction.show(tab_manager);
            currentFragment = null;
            currentFragmentPos = -1;
        } else {
            for (int i = 0; i < fragments.size(); i++)
                transaction.hide(fragments.get(i));
            transaction.hide(tab_manager);
            transaction.show(fragments.get(pos));

            if(fragments.get(pos) instanceof WebFragment) {
                WebFragment frag = (WebFragment)fragments.get(pos);
                if (frag.getUserAgent()) {
                    btn_useragent.setImageDrawable(getDrawable(R.drawable.smartphone));
                    btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } else {
                    btn_useragent.setImageDrawable(getDrawable(R.drawable.mouse));
                    btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }

                currentFragment = frag;
                frag.onCurrentFragment();
                currentFragmentRss = false;
            } else
                currentFragmentRss = true;

            currentFragmentPos = pos;
        }
        transaction.commit();
        manager.executePendingTransactions();
        closeDrawer();

    }

    public void setCurrentFragment(WebFragment frag) {
        FragmentTransaction transaction = manager.beginTransaction();
        if(frag!=null&&frag.getTag().equals("-1"))
            transaction.setCustomAnimations(R.anim.nothing,R.anim.zoom_out);
        else
            transaction.setCustomAnimations(R.anim.zoom_in,R.anim.nothing);

        hideKeyboard();

        for (int i = 0; i < fragments.size(); i++)
            transaction.hide(fragments.get(i));
        transaction.show(frag);
        //noinspection StatementWithEmptyBody
        if (frag!=null&&frag.getTag().equals("-1")) {
            currentFragment = null;
            currentFragmentPos = -1;
        } else if (frag!=null&&frag.getUserAgent()) {
            currentFragment = frag;
            frag.onCurrentFragment();
            if (!shortcut) {
                btn_useragent.setImageDrawable(getDrawable(R.drawable.smartphone));
                btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
                currentFragmentRss = false;
                currentFragmentPos = fragments.indexOf(frag);
            }
        } else if (frag!=null) {
            currentFragment = frag;
            frag.onCurrentFragment();
            if (!shortcut) {
                btn_useragent.setImageDrawable(getDrawable(R.drawable.mouse));
                btn_useragent.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            currentFragmentRss = false;
            currentFragmentPos = fragments.indexOf(frag);
        }
        transaction.commit();
        manager.executePendingTransactions();
        closeDrawer();
    }

    @Override
    public void onRefresh() {
        refreshCurrentUrls();
        refreshTabs();
    }

    @Override
    public void onSetCurrentFragment(int posFrag, int posLastFrag) {
        setCurrentFragment(posFrag);
        tab_manager.moveToPosition(posLastFrag);
    }

    @Override
    public void showManager() {
        setCurrentFragment(-1);
    }


    private boolean canSwitchTab = true;
    private CountDownTimer timer = new CountDownTimer(250, 250) {

        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            canSwitchTab = true;
        }
    }.start();

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onTabSwipe(boolean direction) {
        if (canSwitchTab) {
            if (!direction) {
                if (currentFragmentPos != -1 && currentFragmentPos + 1 < fragments.size() && fragments.get(currentFragmentPos + 1) != null)
                    setCurrentFragment(currentFragmentPos + 1, direction);
            } else {
                if (currentFragmentPos != -1 && currentFragmentPos - 1 >= 0 && fragments.get(currentFragmentPos - 1) != null)
                    setCurrentFragment(currentFragmentPos - 1, direction);
            }
            canSwitchTab = false;
            timer.start();
        }
    }

    @Override
    public void onNewTab(String url, int pos) {
        if (pos == -1)
            addTab(url);
        else
            addTab(url, pos);
    }




    public void writeCurrentUrls() {
        ThumbnailsSaver.clearStorage(this);
        ThumbnailsSaver.saveToInternalSorage(this, thumbnails);
    }


    public boolean loadCurrentUrls() {
        boolean hasLoaded = false;
        opening = true;
        thumbnails = ThumbnailsSaver.loadFromInternalSorage(this);
        for (int i = 0; i < thumbnails.size(); i++) {
            addTab(thumbnails.get(i).url);
            fragments.get(i).setTempThumbnail(thumbnails.get(i));
        }

        opening = false;
        if (thumbnails.size() != 0)
            hasLoaded = true;
        return hasLoaded;
    }


    public void refreshCurrentUrls() {
        thumbnails.clear();
        for (ExtendedFragment frag : fragments)
            thumbnails.add(frag.getThumbnail());
    }


    private void refreshColor() {
        for (Fragment f : fragments) {
            if(f instanceof WebFragment)
                ((WebFragment)f).refreshColor();
        }
    }


    public void newBookmark() {
        if (getCurrentFragment()!=null) {
            String title = getCurrentFragment().getWeb().getTitle();
            String url = getCurrentFragment().getWeb().getUrl();
            new DbBitmapUtility();
            Bitmap pic = null;
            if (getCurrentFragment().getWeb().getFavicon() != null)
                pic = getCurrentFragment().getWeb().getFavicon();
            if (pic != null) {
                BookmarkInterface listener = new BookmarkInterface() {
                    @Override
                    public void onBookmarkAdded(Bookmark b) {
                        String title = b.getName();
                        String url = b.getUrl();
                        Bitmap pic = b.getPic();

                        bookmarksList.add(new Bookmark(title, url, pic));
                        bookmarksDrawer.getAdapter().notifyDataSetChanged();
                        writeBookmarks();
                    }

                    @Override
                    public void onBookmarkRemoved() {}

                    @Override
                    public void onCancel() {}

                };
                BookmarkDialog dialog = new BookmarkDialog();
                dialog.setBookmark(new Bookmark(title,url,pic),false);
                dialog.setListener(listener);
                dialog.show(manager,"DIALOG");
            } else
                Toast.makeText(this, "Chargement de l'image du site...", Toast.LENGTH_SHORT).show();
        }

        closeDrawer();
    }
    public void editBookmark(final int pos) {
        BookmarkInterface listener = new BookmarkInterface() {
            @Override
            public void onBookmarkAdded(Bookmark b) {
                deleteBookmark(pos,false);

                String title = b.getName();
                String url = b.getUrl();
                Bitmap pic = b.getPic();

                bookmarksList.add(pos,new Bookmark(title, url, pic));
                bookmarksDrawer.getAdapter().notifyDataSetChanged();
                writeBookmarks();
            }
            @Override
            public void onBookmarkRemoved() {
                deleteBookmark(pos,true);
            }
            @Override
            public void onCancel() {
                bookmarksDrawer.getAdapter().notifyDataSetChanged();
            }
        };
        BookmarkDialog dialog = new BookmarkDialog();
        dialog.setBookmark(bookmarksList.get(pos),true);
        dialog.setListener(listener);
        dialog.show(manager,"DIALOG");
    }

    private void closeDrawer() {
        DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert dl != null;
        if (dl.isDrawerOpen(GravityCompat.START)) {
            dl.closeDrawer(GravityCompat.START);
        }
    }

    public void writeBookmarks() {
        SharedPreferences.Editor editor = sharedPrefFavs.edit();
        String key, value;
        editor.putInt(getString(R.string.bookmarks_list_size), bookmarksList.size());
        for (int i = 0; i < bookmarksList.size(); i++) {
            key = getString(R.string.bookmarks_list_item) + Integer.toString(i);
            value = bookmarksList.get(i).getName() + '|' + bookmarksList.get(i).getUrl() + '|';
            editor.putString(key, value);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bookmarksList.get(i).getPic().compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encoded = Base64.encodeToString(b, Base64.DEFAULT);

            editor.putString(key + "_PIC", encoded);
        }

        editor.apply();
    }

    public void readBookmarks() {
        int n = sharedPrefFavs.getInt(getString(R.string.bookmarks_list_size), 0);
        int sepCount;
        char c;
        String str, temp = "", title = "", url = "", key;
        Bitmap pic;

        for (int i = 0; i < n; i++) {
            str = sharedPrefFavs.getString(getString(R.string.bookmarks_list_item) + Integer.toString(i), null);
            sepCount = 0;

            for (int charCount = 0; sepCount < 2; charCount++) {
                assert str != null;
                c = str.charAt(charCount);
                if (c != '|') {
                    temp += c;
                } else {
                    if (sepCount == 0)
                        title = temp;
                    else
                        url = temp;
                    temp = "";
                    sepCount++;
                }
            }

            key = getString(R.string.bookmarks_list_item) + Integer.toString(i) + "_PIC";
            sharedPrefFavs.getString(key, null);
            String toDecode = sharedPrefFavs.getString(key, null);
            assert toDecode != null;
            byte[] imageAsBytes = Base64.decode(toDecode.getBytes(), Base64.DEFAULT);
            pic = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

            bookmarksList.add(new Bookmark(title, url, pic));
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_RESULTS_BTN_FAV_SORT) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle results = data.getExtras();
                ArrayList<String> bookmarkListString = (ArrayList<String>) results.getSerializable("BOOKMARKS_LIST");

                assert bookmarkListString != null;
                for (int i = 0; i < bookmarkListString.size(); i++) {
                    for (int j = 0; j < bookmarksList.size(); j++) {
                        if (bookmarkListString.get(i).equals(bookmarksList.get(j).getName())) {
                            Bookmark temp = bookmarksList.get(j);
                            bookmarksList.remove(j);
                            bookmarksList.add(i, temp);
                        }
                    }
                }

                mAdapter.notifyDataSetChanged();
                writeBookmarks();
            }
            /*if (resultCode == Activity.RESULT_CANCELED) {

            }*/
        }
    }


    @Override
    public void onDestroy() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
        unregisterReceiver(connectivityChanged);
        super.onDestroy();
    }


    private void deleteBookmark(final int position, boolean showConfirmation) {
        if(showConfirmation) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            dialog.dismiss();
                            bookmarksList.remove(position);
                            bookmarksDrawer.removeViewAt(position);
                            mAdapter.notifyItemRemoved(position);
                            mAdapter.notifyItemRangeChanged(position, bookmarksList.size());
                            writeBookmarks();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            mAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Supprimer le favori ?").setPositiveButton("Oui", dialogClickListener)
                    .setNegativeButton("Non", dialogClickListener).show();
        }
        else {
            bookmarksList.remove(position);
            bookmarksDrawer.removeViewAt(position);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemRangeChanged(position, bookmarksList.size());
            writeBookmarks();
        }
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private static final int REQUEST_PERMISSION_RESULT = 0;
    public int checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this,permission);
    }
    public void requestPermission(String permission) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION_RESULT);
    }

    ArrayList<WebFragment> errors = new ArrayList<>();
    @Override
    public void addToErrors(WebFragment frag) {
        errors.add(frag);
    }
    public void refreshWebViews() {
        if(isConnected&&!lastState) {
            for(WebFragment frag : errors)
                frag.getWeb().reload();
            errors.clear();
        }
        lastState = isConnected;
    }


    public ArrayList<Thumbnail> getThumbnails() {
        return thumbnails;
    }



    private ArrayList<HistoryItem> history;
    private SQLUtility sqlHistory;
    private long historyCountdown = 0;
    private void initHistory() {
        sqlHistory = new SQLUtility(this);
        history = new ArrayList<>();
        if(prefs.getString("prefHistory","Désactivé").equals("Permanent"))
            loadNextHistoryElements();
    }


    String previousTitle = "";
    @Override
    public void onPageLoaded(String title, String url) {
        if(!prefs.getString("prefHistory","Désactivé").equals("Désactivé")&&!prefs.getString("prefHistory","Désactivé").equals("0")&&System.currentTimeMillis()-historyCountdown>1000&&title!=null&&!title.isEmpty()&&!title.equals(previousTitle)) {
            history.add(0,new HistoryItem(title, url, System.currentTimeMillis()));
            historyCountdown = System.currentTimeMillis();
            previousTitle = title;
            writeHistory();
        }
    }


    private void writeHistory() {
        if(prefs.getString("prefHistory","Désactivé").equals("Permanent"))
            sqlHistory.write(history);
    }


    RecyclerView historyRecyclerView;
    private void showHistoryDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.fragment_history, null);
        view.findViewById(R.id.fragment_history_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        HistoryAdapter adapter = new HistoryAdapter(history);

        historyRecyclerView = view.findViewById(R.id.fragment_history_recyclerview);
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swiped

                if (direction == ItemTouchHelper.RIGHT) { // delete
                    sqlHistory.removeItem(position);
                    history.remove(position);
                    historyRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        };
        historyRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        historyRecyclerView.setLayoutManager(mLayoutManager);
        historyRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(historyRecyclerView);

        historyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    loadNextHistoryElements();
                }
            }
        });

        historyRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, historyRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {}
                    @Override
                    public void onLongItemClick(View view, int position) {    // new tab
                        addTab(history.get(position).url);
                    }
                })

        );

        historyRecyclerView.setAdapter(adapter);

        dialog.setView(view);
        dialog.setTitle("Historique");
        dialog.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void loadNextHistoryElements() {
        history.addAll(sqlHistory.read20HistoryItems());
        if(historyRecyclerView!=null&&historyRecyclerView.getAdapter()!=null)
            historyRecyclerView.getAdapter().notifyDataSetChanged();
    }
    private void clearHistory() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        sqlHistory.clearHistory();
                        history.clear();
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Effacer l'historique ?").setPositiveButton("Oui", dialogClickListener)
                .setNegativeButton("Non", dialogClickListener).show();
    }

    private void searchForUpdates() {
        if (checkPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if(isConnected) {
                long lastUpdate = prefs.getLong("SEEK_FOR_UPDATES", 0);
                if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastUpdate) >= 24) {
                    UpdateTask update = new UpdateTask(this,UpdateTask.NO_TOAST);
                    update.execute(UpdateTask.UPDATE_LINK);
                    prefs.edit().putLong("SEEK_FOR_UPDATES", System.currentTimeMillis()).apply();
                }
            }
        } else
            requestPermission(WRITE_EXTERNAL_STORAGE);

    }



}
