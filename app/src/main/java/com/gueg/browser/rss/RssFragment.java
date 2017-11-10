package com.gueg.browser.rss;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gueg.browser.activities.ExtendedFragment;
import com.gueg.browser.R;
import com.gueg.browser.web.bookmarks.utilities.VerticalSpaceItemDecoration;
import com.gueg.browser.activities.OnMainActivityCallListener;
import com.gueg.browser.rss.sql.SQLUtility;
import com.gueg.browser.thumbnails.Thumbnail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RssFragment extends ExtendedFragment {

    Thumbnail tempThumbnail;
    View rootView;
    RecyclerView recycler;
    ArrayList<RssFeed> feeds = new ArrayList<>();
    ArrayList<RssItem> items = new ArrayList<>();
    ArrayList<RssItem> parsedItems = new ArrayList<>();
    SwipeRefreshLayout swiperefresh;
    OnMainActivityCallListener mMainActivityListener;
    SharedPreferences mainPref;
    RelativeLayout rel;
    TextView text;
    ImageView image;
    ExecutorService threadQueue;
    SQLUtility sql;
    ImageButton btn_tabs;
    int posFrag;

    OnRSSItemClick rssClickListener = new OnRSSItemClick() {
        @Override
        public void onRssClick(RssItem item) {
            if(mMainActivityListener!=null)
                mMainActivityListener.onNewTab(item.link,-1);
        }
    };

    View.OnClickListener addFeed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            LinearLayout root = new LinearLayout(getContext());
            root.setOrientation(LinearLayout.VERTICAL);
            final EditText textTitle = new EditText(getContext());
            final EditText textUrl = new EditText(getContext());
            root.addView(textTitle);
            root.addView(textUrl);
            alert.setTitle("Nouveau flux rss");
            alert.setMessage("Entrer le titre et l'url du flux : ");

            alert.setView(root);

            alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    addFeed(textTitle.getText().toString(), textUrl.getText().toString());
                }
            });

            alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }
    };

    View.OnClickListener editFeed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Editer les flux rss");
            alert.setMessage("Cocher les flux à supprimer : ");
            final ListView list = new ListView(getContext());
            list.setAdapter(new FeedAdapter(getContext(),feeds));
            alert.setView(list);

            alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    feeds.clear();
                    feeds.addAll(((FeedAdapter)list.getAdapter()).getNewList());
                    recycler.getAdapter().notifyDataSetChanged();
                    refresh();
                }
            });

            alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }
    };


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
        rootView = inflater.inflate(R.layout.fragment_rss, container, false);
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
            }
        });
        swiperefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.rss_refreshlayout);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swiperefresh.setDrawingCacheEnabled(true);
        swiperefresh.buildDrawingCache();


        threadQueue = Executors.newSingleThreadExecutor();

        sql = new SQLUtility(getContext());

        rel = (RelativeLayout) rootView.findViewById(R.id.relatLayoutRss);
        image = (ImageView) rootView.findViewById(R.id.rss_pic);
        text = (TextView) rootView.findViewById(R.id.rss_text);

        text.setOnTouchListener(new View.OnTouchListener() {
            float mLastMotionX;
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                final int action = ev.getAction();
                final float x = ev.getX();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionX = x;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        final int deltaX = (int) (x - mLastMotionX);

                        if( Math.abs(deltaX) >= 80) {
                            mMainActivityListener.onTabSwipe(deltaX>=0);
                            mLastMotionX = x;
                        }

                        return true;

                }
                return false;
            }
        });

        swiperefresh.animate().translationZ(25).setDuration(500).start();
        btn_tabs = (ImageButton) rootView.findViewById(R.id.rss_tab);
        btn_tabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityListener.onSetCurrentFragment(-1,posFrag);
            }
        });
        btn_tabs.animate().translationZ(20).setDuration(500).start();

        mainPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        refreshColor();

        recycler = (RecyclerView) rootView.findViewById(R.id.rss_recyclerview);
        recycler.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(mLayoutManager);
        recycler.addItemDecoration(new VerticalSpaceItemDecoration(15));
        RssAdapter mAdapter = new RssAdapter(items, rssClickListener, getContext());
        recycler.setAdapter(mAdapter);



        ImageButton btn_add = (ImageButton) rootView.findViewById(R.id.activity_rss_add);
        btn_add.setOnClickListener(addFeed);

        ImageButton btn_edit = (ImageButton) rootView.findViewById(R.id.activity_rss_edit);
        btn_edit.setOnClickListener(editFeed);


        getLocalItems();
        getFeeds();
        refresh();

        ImageButton btn_settings = (ImageButton) rootView.findViewById(R.id.activity_rss_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });


        return rootView;
    }

    private void getFeeds() {
        feeds.clear();
        for(RssItem item : items) {
            RssFeed feed = new RssFeed(item.feed,item.feedUrl);
            boolean found = false;
            for(RssFeed f : feeds)
                if(f.title!=null&&f.url!=null&&f.title.equals(feed.title)&&feed.url.equals(f.url))
                    found=true;
            if(!found)
                feeds.add(feed);
        }
    }

    private void clear() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        sql.clear();
                        items.clear();
                        recycler.getAdapter().notifyDataSetChanged();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Supprimer les flux ?").setPositiveButton("Oui", dialogClickListener)
                .setNegativeButton("Non", dialogClickListener).show();
    }

    private void getLocalItems() {
        items.addAll(sql.readAllRssItems());
        recycler.getAdapter().notifyDataSetChanged();
    }

    public void setPos(int pos) {
        posFrag=pos;
    }

    public void refreshColor() {
        int colorBar =  mainPref.getInt("prefColorBar",0xffffffff);

        rel.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        text.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        image.setBackgroundTintList(ColorStateList.valueOf(colorBar));
        btn_tabs.setBackgroundTintList(ColorStateList.valueOf(colorBar));

        boolean isChecked = mainPref.getBoolean("prefColorBarText",true);

        if(isChecked)
            text.setTextColor(ColorStateList.valueOf(0xff000000));
        else
            text.setTextColor(ColorStateList.valueOf(0xffffffff));
    }

    private void refresh() {
        for(final RssFeed feed : feeds) {
            threadQueue.submit(new Runnable() {
                @Override
                public void run() {
                    InputStream input;
                    try {
                        input = new URL(feed.url).openStream();
                        try {
                            parsedItems.addAll(FeedParser.parse(feed.title, feed.url, input));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (MalformedURLException e) {
                        Toast.makeText(getContext(), "Adresse URL incorrecte.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Erreur lors de la récupération..", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        threadQueue.submit(new Runnable() {
            @Override
            public void run() {
                for (RssItem item : parsedItems) {
                    boolean found = false;
                    for (RssItem t : items) {
                        if (t.title.equals(item.title) && t.link.equals(item.link))
                            found = true;
                    }
                    if (!found) {
                        items.add(item);
                    }
                }
                // to avoid ViewRoot$CalledFromWrongThreadException :
                // Only the original thread that created a view hierarchy can touch its views.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (swiperefresh.isRefreshing())
                            swiperefresh.setRefreshing(false);
                        recycler.getAdapter().notifyDataSetChanged();
                    }
                });

                Collections.sort(items, new Comparator<RssItem>() {
                    @Override
                    public int compare(RssItem i1, RssItem i2) {
                        return new Date(i2.published).compareTo(new Date(i1.published));
                    }
                });
                parsedItems.clear();
            }
        });
    }



    public void addFeed(String title, String url) {
        feeds.add(new RssFeed(title,url));
        refresh();
    }


    @Override
    public void onPause() {
        sql.write(items);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        sql.onDestroy();
        super.onDestroy();
    }



    private class FeedAdapter extends ArrayAdapter<RssFeed> {

        ArrayList<RssFeed> rssFeed;
        ArrayList<CheckedTextView> checked = new ArrayList<>();

        FeedAdapter(Context context, ArrayList<RssFeed> rssFeed) {
            super(context, 0, rssFeed);
            this.rssFeed = rssFeed;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            RssFeed feed = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_rssfeed, parent, false);
            }
            // Lookup view for data population
            CheckedTextView title = (CheckedTextView) convertView.findViewById(R.id.row_rssfeed_title);
            TextView url = (TextView) convertView.findViewById(R.id.row_rssfeed_url);
            // Populate the data into the template view using the data object
            assert feed != null;
            title.setText(feed.title);
            url.setText(feed.url);
            checked.add(title);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CheckedTextView)v).setChecked(!((CheckedTextView)v).isChecked());
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }

        ArrayList<RssFeed> getNewList() {
            for(int i=0; i<rssFeed.size(); i++) {
                if(checked.get(i).isChecked()) {
                    sql.removeFeed(rssFeed.get(i).title);
                    rssFeed.remove(i);
                }
            }
            return rssFeed;
        }
    }

    @Override
    public Thumbnail getThumbnail() {
        swiperefresh.buildDrawingCache();
        return new Thumbnail("Rss","",swiperefresh.getDrawingCache());
    }

    @Override
    public void setTempThumbnail(Thumbnail t) {
        tempThumbnail = t;
    }

}
