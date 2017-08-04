package com.gueg.browser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import java.util.ArrayList;

public class TabCardsFragment extends Fragment {

    RecyclerView mRecyclerView;
    TabCardsAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    private static final int VERTICAL_ITEM_SPACE = 25;

    View rootView;
    ArrayList<WebPage> list;

    String TAG = "TabCardsFragments";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        rootView = inflater.inflate(R.layout.fragment_tabcards,container,false);
        Log.d(TAG,"rootView inflated");
        list = ((MainActivity)getActivity()).getTabList();
        Log.d(TAG,"list getted from mainactivity");
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        Log.d(TAG,"mRecyclerView inflated");
        ImageButton tab_add = (ImageButton) rootView.findViewById(R.id.btn_tab_add);
        assert mRecyclerView != null;
        mRecyclerView.setHasFixedSize(true);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                /*final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                ArrayList<CustomWebViewFragment> list = ((MainActivity)getActivity()).getRawList();
                CustomWebViewFragment temp = list.get(toPos);
                list.set(toPos,list.get(fromPos));
                list.set(fromPos,temp);
                ((MainActivity)getActivity()).setTabList(list);
                mAdapter.notifyItemMoved(fromPos,toPos);
                return true; // true if moved
                */
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

        Log.d(TAG,"setting layoutmanager");

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        mRecyclerView.setLayoutManager(mLayoutManager);

        Log.d(TAG,"Calling TabCardsAdapter");

        mAdapter = new TabCardsAdapter(list);


        Log.d(TAG,"adapter and interface created");
        mRecyclerView.setAdapter(mAdapter);
        Log.d(TAG,"adapter setted");

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
