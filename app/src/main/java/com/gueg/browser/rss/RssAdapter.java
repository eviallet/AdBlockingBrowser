package com.gueg.browser.rss;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.gueg.browser.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


class RssAdapter extends RecyclerView.Adapter<RssAdapter.ViewHolder> {

    private ArrayList<RssItem> _rss;
    private OnRSSItemClick _listener;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView description;
        TextView date;
        ImageView expand;

        ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.row_rss_image);
            title = (TextView) v.findViewById(R.id.row_rss_text_title);
            description = (TextView) v.findViewById(R.id.row_rss_text_description);
            date = (TextView) v.findViewById(R.id.row_rss_text_date);
            expand = (ImageButton) v.findViewById(R.id.row_rss_expand);
        }
    }

    RssAdapter(ArrayList<RssItem> items, OnRSSItemClick listener, Context context) {
        _rss = items;
        _listener = listener;
        this.context = context;
    }

    @Override
    public RssAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_rss, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RssItem item = _rss.get(position);

        holder.expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.description.getVisibility()==View.GONE) {
                    holder.description.setVisibility(View.VISIBLE);
                    holder.expand.animate().rotation(0).setDuration(300).start();
                }
                else {
                    holder.description.setVisibility(View.GONE);
                    holder.expand.animate().rotation(90).setDuration(300).start();
                }
            }
        });

        holder.title.setText(item.title);
        if(item.description!=null&&item.description.length()>0)
            holder.description.setText(item.description);
        else
            holder.description.setText("");

        if(item.link!=null&&item.link.length()>0)
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _listener.onRssClick(item);
                }
            });

        holder.date.setText(getFormattedDate(item.published));


        /*
        TextCrawler crawler = new TextCrawler();
        LinkPreviewCallback callback = new LinkPreviewCallback() {
            @Override
            public void onPre() {}
            @Override
            public void onPos(SourceContent sourceContent, boolean b) {
                if(sourceContent.getImages()!=null&&sourceContent.getImages().get(0)!=null)
                    UrlImageViewHelper.setUrlDrawable(holder.image, sourceContent.getImages().get(0), new UrlImageViewCallback() {
                        @Override
                        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                            imageView.setImageBitmap(loadedBitmap);
                        }
                    });
            }
        };
        crawler.makePreview(callback,item.link);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                ImageView img = new ImageView(context);
                img.setMinimumHeight(300);
                img.setMinimumWidth(300);
                img.setImageDrawable(holder.image.getDrawable());
                builder.setView(img);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        */
    }

    private String getProximity(long date) {
        int p = Math.round(TimeUnit.MILLISECONDS.toDays(date)-TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()));
        switch(p) {
            case 0 :
                return "Auj";
            case -1 :
                return "Hier";
            case -2 :
                return "Av-hier";
            default :
                return null;
        }
    }

    private String getFormattedDate(long date) {
        String prox = getProximity(date);
        SimpleDateFormat formatter;
        if(prox==null) {
            formatter = new SimpleDateFormat("dd/MM'\n'HH:mm", Locale.FRANCE);
            return formatter.format(date);
        }
        else {
            formatter = new SimpleDateFormat("HH:mm", Locale.FRANCE);
            return prox+'\n'+formatter.format(date);
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _rss.size();
    }
}
