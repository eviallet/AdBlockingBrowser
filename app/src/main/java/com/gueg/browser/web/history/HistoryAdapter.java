package com.gueg.browser.web.history;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gueg.browser.R;

import java.util.ArrayList;


public class HistoryAdapter extends RecyclerView.Adapter<com.gueg.browser.web.history.HistoryAdapter.ViewHolder> {

    private ArrayList<HistoryItem> _history;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView url;
        TextView date;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.row_history_title);
            url = v.findViewById(R.id.row_history_url);
            date = v.findViewById(R.id.row_history_date);
        }
    }

    public HistoryAdapter(ArrayList<HistoryItem> items) {
        _history = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_historyitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final HistoryItem item = _history.get(position);

        holder.title.setText(item.title);
        holder.url.setText(item.url);
        holder.date.setText(item.getFormattedDate());

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _history.size();
    }
}
