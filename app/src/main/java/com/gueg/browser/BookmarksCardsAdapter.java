package com.gueg.browser;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class BookmarksCardsAdapter extends RecyclerView.Adapter<BookmarksCardsAdapter.ViewHolder>{

    private ArrayList<Bookmark> mList;


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mTextView;
        ViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.bookmarkPic);
            mTextView = (TextView) v.findViewById(R.id.bookmarkText);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    BookmarksCardsAdapter(ArrayList<Bookmark> list) {
        mList = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BookmarksCardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmarkcard_row, parent, false);
        return new BookmarksCardsAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final BookmarksCardsAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mList.get(position).getName());
        holder.mImageView.setImageBitmap(mList.get(position).getPic());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }

    void refresh(ArrayList<Bookmark> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }


}