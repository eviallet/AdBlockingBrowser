package com.gueg.browser.thumbnails;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.gueg.browser.R;
import com.gueg.browser.web.bookmarks.utilities.RecyclerItemClickListener;
import com.gueg.browser.web.bookmarks.utilities.VerticalSpaceItemDecoration;
import com.gueg.browser.activities.MainActivity;
import java.util.ArrayList;

public class ThumbnailsFragment extends Fragment implements View.OnFocusChangeListener {

    RecyclerView mRecyclerView;
    ThumbnailFragmentsAdapter mAdapter;

    private static final int VERTICAL_ITEM_SPACE = 25;

    View rootView;
    ArrayList<Thumbnail> list;
    EditText search;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tabcards,container,false);

        list = ((MainActivity)getActivity()).getThumbnails();
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        ImageButton tab_add = rootView.findViewById(R.id.btn_tab_add);
        ImageButton tab_undo = rootView.findViewById(R.id.btn_tab_undo);
        search = rootView.findViewById(R.id.fragment_default_search);

        boolean theme = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("prefDarkTheme",false);
        if(theme) {
            rootView.setBackgroundColor(0xff313335);
            search.setBackgroundDrawable(getActivity().getDrawable(R.drawable.search_bar_dark));
        }
        ImageView logo = rootView.findViewById(R.id.fragment_default_logo);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.animate().rotation(360).setDuration(5000).start();
            }
        });

        search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(search.getText().toString().toLowerCase().contains("http://")||search.getText().toString().toLowerCase().contains("https://")) {
                        ((MainActivity)getActivity()).addTab(search.getText().toString());
                        search.setText("");
                    }
                    else if(search.getText().toString().toLowerCase().contains("www")) {
                        String display = "http://"+search.getText().toString();
                        search.setText(display);
                        ((MainActivity)getActivity()).addTab(display);
                        search.setText("");
                    }
                    else if(search.getText().toString().toLowerCase().contains(".fr")||search.getText().toString().toLowerCase().contains(".com")) {
                        String display = "http://www."+search.getText().toString();
                        search.setText(display);
                        ((MainActivity)getActivity()).addTab(display);
                        search.setText("");
                    }
                    else {
                        ((MainActivity) getActivity()).addTab("https://www.google.fr/search?q=" + search.getText().toString());
                        search.setText("");
                    }

                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        ImageButton im = rootView.findViewById(R.id.fragment_default_button);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!search.getText().toString().isEmpty())
                {
                    if(search.getText().toString().toLowerCase().contains("http://")||search.getText().toString().toLowerCase().contains("https://")) {
                        ((MainActivity)getActivity()).addTab(search.getText().toString());
                        search.setText("");
                    }
                    else if(search.getText().toString().toLowerCase().contains("www")) {
                        String display = "http://"+search.getText().toString();
                        search.setText(display);
                        ((MainActivity)getActivity()).addTab(display);
                        search.setText("");
                    }
                    else if(search.getText().toString().toLowerCase().contains(".fr")||search.getText().toString().toLowerCase().contains(".com")) {
                        String display = "http://www."+search.getText().toString();
                        search.setText(display);
                        ((MainActivity)getActivity()).addTab(display);
                        search.setText("");
                    }
                    else {
                        ((MainActivity) getActivity()).addTab("https://www.google.fr/search?q=" + search.getText().toString());
                        search.setText("");
                    }
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                }
            }
        });


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.UP) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swiped

                if (direction == ItemTouchHelper.UP) {
                    ((MainActivity)getActivity()).closeTab(position);
                    mRecyclerView.removeViewAt(position);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, list.size());
                    mAdapter.notifyDataSetChanged();
                    ((MainActivity)getActivity()).refreshTabs();
                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        mAdapter = new ThumbnailFragmentsAdapter(list,theme);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new CenterScrollListener());


        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        ((MainActivity)getActivity()).setCurrentFragment(position);
                        ((MainActivity)getActivity()).refreshTabs();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        //((MainActivity)getActivity()).newBookmark(position);
                    }
                })

        );

        tab_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).undoClose();
                ((MainActivity)getActivity()).setCurrentFragment(-1);
                list = ((MainActivity)getActivity()).getThumbnails();
            }
        });




        tab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).addTab();
                ((MainActivity)getActivity()).refreshCurrentUrls();
            }
        });

        ImageButton tab_rem = rootView.findViewById(R.id.btn_tab_rem);
        tab_rem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).closeAllTabs();
                list = ((MainActivity)getActivity()).getThumbnails();
                ((MainActivity)getActivity()).refreshCurrentUrls();
            }
        });


        return rootView;
    }

    public void moveToPosition(int pos) {
        mRecyclerView.scrollToPosition(pos);
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            if(!search.hasFocus())
                search.requestFocus();
        } else {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public ThumbnailFragmentsAdapter getAdapter() {
        return mAdapter;
    }
}
