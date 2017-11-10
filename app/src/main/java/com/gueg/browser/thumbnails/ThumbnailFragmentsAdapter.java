package com.gueg.browser.thumbnails;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import com.gueg.browser.R;
import com.gueg.browser.activities.MainActivity;

import java.util.ArrayList;
import java.util.Collections;

public class ThumbnailFragmentsAdapter extends RecyclerView.Adapter<ThumbnailFragmentsAdapter.ViewHolder> {

    private ArrayList<Thumbnail> mList;
    private boolean theme;


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txt;
        ViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.row_thumbnail_w);
            txt = v.findViewById(R.id.row_thumbnail_t);
        }
    }

    ThumbnailFragmentsAdapter(ArrayList<Thumbnail> list, boolean theme) {
        mList = list;
        this.theme = theme;
    }

    @Override
    public ThumbnailFragmentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_thumbnail, parent, false);
        return new ThumbnailFragmentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ThumbnailFragmentsAdapter.ViewHolder holder, final int position) {
        if(mList.get(position)!=null) {
            holder.img.setImageBitmap(mList.get(position).image);
            holder.txt.setText(mList.get(position).title);
            if(theme)
                holder.txt.setTextColor(0xffC9C0BE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void refresh(ArrayList<Thumbnail> list) {
        mList = list;
        notifyDataSetChanged();
    }


}