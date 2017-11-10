package com.gueg.browser.web.bookmarks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;

import com.gueg.browser.R;
import com.gueg.browser.web.bookmarks.utilities.VerticalSpaceItemDecoration;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class BookmarkSortActivity extends Activity {
    private static final int VERTICAL_ITEM_SPACE = 15;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_sort);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final ArrayList<String> bookmarksList = (ArrayList<String>) extras.getSerializable("BOOKMARKS_LIST");



        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_bookmarks_sort);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        final BookmarkSortAdapter mAdapter = new BookmarkSortAdapter(bookmarksList);
        recyclerView.setAdapter(mAdapter);


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.UP| ItemTouchHelper.DOWN) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);



        Button btnValider = (Button) findViewById(R.id.btn_bookmarksortActivity_valider);
        if (btnValider != null) {
            btnValider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent results = new Intent();
                    Bundle extras = new Bundle();
                    extras.putSerializable("BOOKMARKS_LIST",bookmarksList);
                    results.putExtras(extras);
                    setResult(Activity.RESULT_OK,results);
                    finish();
                }
            });
        }
        Button btnAnnuler = (Button) findViewById(R.id.btn_bookmarksortActivity_annuler);
        assert btnAnnuler != null;
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent results = new Intent();
                setResult(Activity.RESULT_CANCELED,results);
                finish();
            }
        });
    }
}
