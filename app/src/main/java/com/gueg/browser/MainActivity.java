package com.gueg.browser;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements  OnMainActivityCallListener {


    int ACTIVITY_RESULTS_BTN_FAV_NEW = 1;
    int ACTIVITY_RESULTS_BTN_FAV_SORT = 2;
    TabCardsFragment tab_manager;
    FrameLayout fragment_container;
    ArrayList<CustomWebViewFragment> fragments = new ArrayList<>();
    ArrayList<String> currentUrls = new ArrayList<>();
    ArrayList<Bookmark> bookmarksList;
    BookmarksCardsAdapter mAdapter;
    RecyclerView bookmarksDrawer;
    SharedPreferences sharedPrefFavs;
    SharedPreferences sharedPrefUrls;
    private static final int VERTICAL_ITEM_SPACE = 15;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("color")) {
                        refreshColor();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Inflating layout

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);



        // Shared prefs listener

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        // Fragments


        tab_manager = new TabCardsFragment();

        fragment_container = (FrameLayout) findViewById(R.id.fragment_container);

        android.app.FragmentManager manager = getFragmentManager();
        final android.app.FragmentTransaction transaction = manager.beginTransaction();

        transaction.add(fragment_container.getId(),tab_manager,Integer.toString(-1));

        transaction.commit();
        manager.executePendingTransactions();



        // DRAWER  ====================================================

        // ListView ---------------------------------



        bookmarksDrawer = (RecyclerView) findViewById(R.id.recycler_view_bookmarks);
        bookmarksDrawer.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        bookmarksDrawer.setLayoutManager(mLayoutManager);
        bookmarksDrawer.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        bookmarksList = new ArrayList<>();

        sharedPrefFavs = getSharedPreferences(getString(R.string.bookmarks_list_key),Context.MODE_PRIVATE);

        readBookmarks();

        mAdapter = new BookmarksCardsAdapter(bookmarksList);

        bookmarksDrawer.setAdapter(mAdapter);


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swiped

                if (direction == ItemTouchHelper.RIGHT) { // delete
                    bookmarksList.remove(position);
                    bookmarksDrawer.removeViewAt(position);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, bookmarksList.size());
                    writeBookmarks();
                }
            }

        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(bookmarksDrawer);

        bookmarksDrawer.addOnItemTouchListener(
                new RecyclerItemClickListener(this, bookmarksDrawer ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {    // load in current tab
                        if(fragments.size()!=0)
                            getCurrentFragment().loadUrl(bookmarksList.get(position).getUrl());
                        else
                            addTab(bookmarksList.get(position).getUrl());
                        DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                        assert dl != null;
                        dl.closeDrawer(GravityCompat.START);
                    }

                    @Override public void onLongItemClick(View view, int position) {    // new tab
                        addTab(bookmarksList.get(position).getUrl());
                    }
                })

        );


        // Buttons --------------------------------------


        ImageButton btn_fav = (ImageButton) findViewById(R.id.btn_drawer_favoris);
        ImageButton btn_next = (ImageButton) findViewById(R.id.btn_drawer_suivant);
        ImageButton btn_tabs = (ImageButton) findViewById(R.id.btn_drawer_onglets);
        ImageButton btn_settings = (ImageButton) findViewById(R.id.btn_drawer_parametres);
        assert btn_fav != null;
        btn_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fragments.size()>0)
                    newBookmark();
            }
        });
        btn_fav.setLongClickable(true);
        btn_fav.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(fragments.size()!=0) {
                    Intent intent = new Intent(MainActivity.this, BookmarkSortActivity.class);
                    Bundle bundle = new Bundle();
                    ArrayList<String> bookmarksListString = new ArrayList<>(bookmarksList.size());
                    for (int i = 0; i < bookmarksList.size(); i++) {
                        bookmarksListString.add(bookmarksList.get(i).getName());
                    }
                    bundle.putSerializable("BOOKMARKS_LIST", bookmarksListString);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, ACTIVITY_RESULTS_BTN_FAV_SORT);
                }
                return true;
            }
        });

        assert btn_next != null;
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                if(fragments.size()>0&&!getCurrentFragment().getTag().equals("-1")) {
                    if (getCurrentFragment().getWeb().canGoForward()) {
                        getCurrentFragment().getWeb().goForward();
                        assert dl != null;
                        if (!getCurrentFragment().getWebView().canGoForward() && dl.isDrawerOpen(GravityCompat.START))
                            dl.closeDrawer(GravityCompat.START);
                    }
                }
                else {
                    assert dl != null;
                    dl.closeDrawer(GravityCompat.START);
                }
            }
        });

        assert btn_settings != null;
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                assert dl != null;
                dl.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });

        assert btn_tabs != null;
        btn_tabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                assert dl != null;
                dl.closeDrawer(GravityCompat.START);
                if(getIntent().getBooleanExtra("SHORTCUT",false)) {
                    if (!getCurrentFragment().getTag().equals("-1")) {
                        WebPage page = getCurrentFragment().getWebPage();
                        if (page.getPic() != null) {
                            Bitmap icon = Bitmap.createScaledBitmap(page.getPic(), 128, 128, true);
                            new DbBitmapUtility();
                            byte[] pic = DbBitmapUtility.getBytes(icon);

                            Intent result = new Intent();
                            result.putExtra("shortcut_title",page.getTitle());
                            result.putExtra("shortcut_url",page.getUrl());
                            result.putExtra("shortcut_pic",pic);
                            setResult(Activity.RESULT_OK, result);


                            Toast.makeText(MainActivity.this, "Reccourci crée !", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Chargement de l'image du site...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        final EditText search = (EditText) findViewById(R.id.drawer_search);

        search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(search.getText().toString().toLowerCase().contains("http://")||search.getText().toString().toLowerCase().contains("https://")) {
                        addTab(search.getText().toString());
                        search.setText("");
                    }
                    else if(search.getText().toString().toLowerCase().contains("www")) {
                        String display = "http://"+search.getText().toString();
                        search.setText(display);
                        addTab(display);
                        search.setText("");
                    }
                    else if(search.getText().toString().toLowerCase().contains(".fr")||search.getText().toString().toLowerCase().contains(".com")) {
                        String display = "http://www."+search.getText().toString();
                        search.setText(display);
                        addTab(display);
                        search.setText("");
                    }
                    else {
                        addTab("https://www.google.fr/search?q=" + search.getText().toString());
                        search.setText("");
                    }
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
                    dl.closeDrawer(GravityCompat.START);

                }
                return false;
            }
        });


        // =================================== FRAGMENTS



        sharedPrefUrls = getSharedPreferences(getString(R.string.urls_list_key),Context.MODE_PRIVATE);

        loadCurrentUrls();

        setCurrentFragment(-1);

        Uri startIntentData = getIntent().getData();
        if(startIntentData!=null) {
            String intentUrl = startIntentData.toString();
            if(intentUrl.contains("http://")||intentUrl.contains("https://")) {
                addTab(intentUrl);
            }
        }

        if (getIntent().getExtras() != null) {
            if (getIntent().getStringExtra("LINK")!=null) {
                addTab(getIntent().getStringExtra("LINK"));
            }}


    }   // onCreate










    @Override
    public void onBackPressed() {
        DrawerLayout dl = (DrawerLayout)findViewById(R.id.drawer_layout);
        assert dl != null;
        if(dl.isDrawerOpen(GravityCompat.START))
            dl.closeDrawer(GravityCompat.START);
        else if(fragments.size()>0) {
            if (getCurrentFragment().canGoBack())
                getCurrentFragment().goBack();
            else
                closeTab(getCurrentFragment());
        }
        else
            endMainActivity();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            endMainActivity();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onPause() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                writeCurrentUrls();
            }
        });
        super.onPause();
    }

    public void endMainActivity() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                writeCurrentUrls();
            }
        });
        finishAndRemoveTask();
    }









    // FRAGMENTS

    private CustomWebViewFragment getCurrentFragment() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
        return (CustomWebViewFragment) f;
    }


    public void addTab() {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        CustomWebViewFragment fragment = new CustomWebViewFragment();
        fragments.add(fragment);
        transaction.add(R.id.fragment_container,fragment,"frag"+Integer.toString(fragments.size()));
        transaction.commit();
        manager.executePendingTransactions();

        setCurrentFragment(fragment);

        writeCurrentUrls();
    }

    public void addTab(String url) {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        CustomWebViewFragment fragment = new CustomWebViewFragment();
        fragments.add(fragment);
        fragment.setOnStartUrl(url);
        transaction.add(R.id.fragment_container,fragment,"frag"+Integer.toString(fragments.size()));
        transaction.commit();
        manager.executePendingTransactions();
        setCurrentFragment(fragment);

        fragment.setPos(fragments.size());

        writeCurrentUrls();
    }


    public void addTab(String url, @SuppressWarnings("UnusedParameters") int posCurTab) {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        CustomWebViewFragment fragment = new CustomWebViewFragment();
        fragment.setOnStartUrl(url);
        fragments.add(fragment);

        transaction.add(R.id.fragment_container,fragment,"frag"+Integer.toString(fragments.size()));

        transaction.commit();
        manager.executePendingTransactions();


        fragment.setPos(fragments.size());

        setCurrentFragment(getCurrentFragment());
        writeCurrentUrls();

    }


    public void refreshTabs() {
        tab_manager.getAdapter().refresh(getTabList());
    }


    public void closeTab(int pos) {
        if(pos<fragments.size()) {
            android.app.FragmentManager manager = getFragmentManager();
            android.app.FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragments.get(pos));

            fragments.remove(pos);
            transaction.commit();
            manager.executePendingTransactions();
            refreshCurrentUrls();
            writeCurrentUrls();
            tab_manager.getAdapter().refresh(getTabList());

        }
    }
    public void closeTab(CustomWebViewFragment frag) {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        transaction.remove(frag);

        fragments.remove(fragments.indexOf(frag));
        transaction.commit();
        manager.executePendingTransactions();
        refreshCurrentUrls();
        writeCurrentUrls();
        tab_manager.getAdapter().refresh(getTabList());
    }

    public void closeAllTabs() {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        for(int i=0;i<fragments.size();i++)
            transaction.remove(fragments.get(i));

        fragments.clear();
        //addTab();
        transaction.commit();
        manager.executePendingTransactions();
        currentUrls.clear();
        writeCurrentUrls();
        tab_manager.getAdapter().refresh(null);
    }

    public void setCurrentFragment(int pos) {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        if(fragments.size()==0) {
            transaction.show(manager.findFragmentByTag("-1"));
        }
        else if (pos==-1) {
            for(int i=0; i<fragments.size();i++)
                transaction.hide(fragments.get(i));
            transaction.show(manager.findFragmentByTag("-1"));
        }
        else {
            for(int i=0; i<fragments.size();i++)
                transaction.hide(fragments.get(i));
            transaction.hide(manager.findFragmentByTag("-1"));
            transaction.show(fragments.get(pos));
        }
        transaction.commit();
        manager.executePendingTransactions();
    }

    public void setCurrentFragment(CustomWebViewFragment frag) {
        android.app.FragmentManager manager = getFragmentManager();
        android.app.FragmentTransaction transaction = manager.beginTransaction();

        for(int i=0; i<fragments.size(); i++)
            transaction.hide(fragments.get(i));
        transaction.show(frag);
        transaction.commit();
        manager.executePendingTransactions();
    }

    @Override
    public void onRefresh() {
        refreshTabs();
        refreshCurrentUrls();
    }

    @Override
    public void onSetCurrentFragment(int posFrag) {
        setCurrentFragment(posFrag);
    }

    @Override
    public void onNewTab(String url, int pos) {
        if(pos==-1)
            addTab(url);
        else
            addTab(url,pos);
    }

    public ArrayList<WebPage> getTabList() {

        ArrayList<WebPage> list = new ArrayList<>();
        for (int i = 0; i < fragments.size(); i++) {
            WebView web = fragments.get(i).getWeb();
            WebPage page;
            if (web != null) {
                page = new WebPage(web.getTitle(), web.getUrl(), web.getFavicon());
            } else {
                page = new WebPage("Google", "http://www.google.fr", null);
            }
            list.add(page);
        }
        return list;
    }









    public void writeCurrentUrls() {
        SharedPreferences.Editor editor = sharedPrefUrls.edit();
        String key,value;
        editor.putInt(getString(R.string.urls_list_size),currentUrls.size());
        for(int i=0; i<currentUrls.size();i++) {
            key = getString(R.string.urls_list_item)+Integer.toString(i);
            value = currentUrls.get(i);
            editor.putString(key,value);
        }

        editor.apply();
    }

    public void readCurrentUrls() {
        int n = sharedPrefUrls.getInt(getString(R.string.urls_list_size),0);
        String key, temp;
        for(int i=0; i<n; i++) {
            key = getString(R.string.urls_list_item)+Integer.toString(i);
            temp = sharedPrefUrls.getString(key,null);
            currentUrls.add(temp);
        }
    }


    public boolean loadCurrentUrls() {
        boolean hasLoaded = false;
        readCurrentUrls();
        for(int i=0; i<currentUrls.size(); i++) {
            addTab(currentUrls.get(i));
        }
        if(currentUrls.size()!=0)
            hasLoaded = true;
        return hasLoaded;
    }

    public void refreshCurrentUrls() {
        currentUrls.clear();
        for(int i=0; i<fragments.size(); i++) {
            currentUrls.add(fragments.get(i).getWebPage().getUrl());
        }
    }





    @SuppressWarnings("deprecation")
    private void refreshColor() {
        for(CustomWebViewFragment f : fragments) {
            f.refreshColor();
        }
    }
















    public void newBookmark() {
        if(!getCurrentFragment().getTag().equals("-1")) {
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            WebPage page = getCurrentFragment().getWebPage();
            String title = page.getTitle();
            String url = page.getUrl();
            new DbBitmapUtility();
            byte[] pic = null;
            if(page.getPic()!=null)
                pic = DbBitmapUtility.getBytes(page.getPic());
            intent.putExtra("com.gueg.browser.TITLE", title);
            intent.putExtra("com.gueg.browser.URL", url);
            if(pic!=null) {
                intent.putExtra("com.gueg.browser.PIC", pic);
                startActivityForResult(intent, ACTIVITY_RESULTS_BTN_FAV_NEW);
            }
            else
                Toast.makeText(this,"Chargement de l'image du site...", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert dl != null;
        if (dl.isDrawerOpen(GravityCompat.START)) {
            dl.closeDrawer(GravityCompat.START);
        }
    }

    /*
    public void newBookmark(int position) {

        Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
        String title = getTabList().get(position).getTitle();
        String url = getTabList().get(position).getUrl();
        new DbBitmapUtility();
        byte[] pic = null;
        if(getTabList().get(position).getPic()!=null)
            pic = DbBitmapUtility.getBytes(getTabList().get(position).getPic());
        intent.putExtra("com.gueg.browser.TITLE", title);
        intent.putExtra("com.gueg.browser.URL", url);
        if(pic!=null) {
            intent.putExtra("com.gueg.browser.PIC", pic);
            startActivityForResult(intent, ACTIVITY_RESULTS_BTN_FAV);
            bookmarkAdapter.notifyDataSetChanged();
        }
        else
            Toast.makeText(this,"Chargement de l'image du site...", Toast.LENGTH_SHORT).show();
    }
    */

    public void writeBookmarks() {
        SharedPreferences.Editor editor = sharedPrefFavs.edit();
        String key,value;
        editor.putInt(getString(R.string.bookmarks_list_size),bookmarksList.size());
        for(int i=0; i<bookmarksList.size();i++) {
            key = getString(R.string.bookmarks_list_item)+Integer.toString(i);
            value = bookmarksList.get(i).getName()+'|'+bookmarksList.get(i).getUrl()+'|';
            editor.putString(key,value);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bookmarksList.get(i).getPic().compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encoded = Base64.encodeToString(b, Base64.DEFAULT);

            editor.putString(key+"_PIC",encoded);
        }

        editor.apply();
    }

    public void readBookmarks() {
        int n = sharedPrefFavs.getInt(getString(R.string.bookmarks_list_size),0);
        int sepCount;
        char c;
        String str, temp= "", title = "", url = "", key;
        Bitmap pic;

        for(int i=0; i<n; i++) {
            str = sharedPrefFavs.getString(getString(R.string.bookmarks_list_item)+Integer.toString(i),null);
            sepCount = 0;

            for(int charCount=0; sepCount < 2; charCount++) {
                assert str != null;
                c = str.charAt(charCount);
                if (c != '|') {
                    temp += c;
                }
                else {
                    if(sepCount==0)
                        title = temp;
                    else
                        url = temp;
                    temp = "";
                    sepCount++;
                }
            }

            key = getString(R.string.bookmarks_list_item)+Integer.toString(i)+"_PIC";
            sharedPrefFavs.getString(key,null);
            String toDecode = sharedPrefFavs.getString(key,null);
            assert toDecode != null;
            byte[] imageAsBytes = Base64.decode(toDecode.getBytes(),Base64.DEFAULT);
            pic = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

            bookmarksList.add(new Bookmark(title,url,pic));
        }
    }




    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_RESULTS_BTN_FAV_NEW) {
            if(resultCode == Activity.RESULT_OK){
                Bundle results = data.getExtras();
                final String title = results.getString("com.gueg.browser.NEWTITLE");
                final String url = results.getString("com.gueg.browser.NEWURL");
                final byte[] pic = results.getByteArray("com.gueg.browser.NEWPIC");

                new DbBitmapUtility();
                bookmarksList.add(new Bookmark(title,url,DbBitmapUtility.getImage(pic)));
                writeBookmarks();
            }
            /*if (resultCode == Activity.RESULT_CANCELED) {

            }*/
        }
        else if (requestCode == ACTIVITY_RESULTS_BTN_FAV_SORT) {
            if(resultCode == Activity.RESULT_OK){
                Bundle results = data.getExtras();
                ArrayList<String> bookmarkListString = (ArrayList<String>) results.getSerializable("BOOKMARKS_LIST");

                assert bookmarkListString != null;
                for(int i = 0; i<bookmarkListString.size(); i++) {
                    for(int j=0; j<bookmarksList.size(); j++) {
                        if(bookmarkListString.get(i).equals(bookmarksList.get(j).getName())) {
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
        super.onDestroy();
    }



}
