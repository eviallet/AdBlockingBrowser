package com.gueg.browser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

class BookmarkSortAdapter extends RecyclerView.Adapter<BookmarkSortAdapter.ViewHolder> implements ItemTouchHelperListener{

    private ArrayList<String> mList;


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.bookmarkSortText);
        }
    }


    BookmarkSortAdapter(ArrayList<String> list) {
        mList = list;
    }

    @Override
    public BookmarkSortAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmarkcardsort_row, parent, false);
        return new BookmarkSortAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BookmarkSortAdapter.ViewHolder holder, final int position) {
        holder.mTextView.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }


}