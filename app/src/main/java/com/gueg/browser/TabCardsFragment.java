package com.gueg.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class TabCardsFragment extends Fragment {

    RecyclerView mRecyclerView;
    TabCardsAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    private static final int VERTICAL_ITEM_SPACE = 25;

    View rootView;
    ArrayList<WebPage> list;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tabcards,container,false);
        list = ((MainActivity)getActivity()).getTabList();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        ImageButton tab_add = (ImageButton) rootView.findViewById(R.id.btn_tab_add);
        final EditText search = (EditText) rootView.findViewById(R.id.fragment_default_search);

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

                }
                return false;
            }
        });

        ImageButton im = (ImageButton) rootView.findViewById(R.id.fragment_default_button);
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

                }
            }
        });
        assert mRecyclerView != null;
        mRecyclerView.setHasFixedSize(true);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swiped

                if (direction == ItemTouchHelper.RIGHT) {
                    ((MainActivity)getActivity()).addTab(((MainActivity)getActivity()).getTabList().get(position).getUrl(),-1);
                }
                if (direction == ItemTouchHelper.LEFT) {
                    ((MainActivity)getActivity()).closeTab(position);
                    mRecyclerView.removeViewAt(position);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, list.size());
                    ((MainActivity)getActivity()).refreshTabs();
                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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


        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        mRecyclerView.setLayoutManager(mLayoutManager);


        mAdapter = new TabCardsAdapter(list);



        mRecyclerView.setAdapter(mAdapter);

        tab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).addTab();
                list = ((MainActivity)getActivity()).getTabList();
                ((MainActivity)getActivity()).refreshCurrentUrls();
            }
        });

        ImageButton tab_rem = (ImageButton) rootView.findViewById(R.id.btn_tab_rem);
        tab_rem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).closeAllTabs();
                list = ((MainActivity)getActivity()).getTabList();
                ((MainActivity)getActivity()).refreshCurrentUrls();
            }
        });

        return rootView;
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    public TabCardsAdapter getAdapter() {
        return mAdapter;
    }
}
