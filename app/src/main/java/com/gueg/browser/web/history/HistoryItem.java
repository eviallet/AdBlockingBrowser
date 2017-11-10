package com.gueg.browser.web.history;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistoryItem {

    public String title;
    public String url;
    public Long date;


    public HistoryItem(String t, String u, Long d) {
        title = t;
        url = u;
        date = d;
    }




    String getFormattedDate() {
        String prox = getProximity(date);
        SimpleDateFormat formatter;
        if(prox==null) {
            formatter = new SimpleDateFormat("dd/MM'-'HH:mm", Locale.FRANCE);
            return formatter.format(date);
        }
        else {
            formatter = new SimpleDateFormat("HH:mm", Locale.FRANCE);
            return prox+'-'+formatter.format(date);
        }
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

}
