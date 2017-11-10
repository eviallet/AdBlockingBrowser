package com.gueg.browser.rss;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

class FeedParser {



    static ArrayList<RssItem> parse(String feed, String feedUrl, InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            return readFeed(feed, feedUrl, parser);
        } finally {
            in.close();
        }
    }

    private static ArrayList<RssItem> readFeed(String feed, String feedUrl, XmlPullParser parser) throws XmlPullParserException, IOException {

        ArrayList<RssItem> items = new ArrayList<>();


        int eventType;
        String[] item = new String[3];
        String title = "";
        String description = "";
        String link = "";
        long published = -1;
        parser.next();

        while((eventType = parser.getEventType())!=XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG :
                    // <title>
                    if(parser.getName()!=null)
                        item[0]=parser.getName();
                    break;
                case XmlPullParser.TEXT :
                    // Titre
                    if(parser.getText()!=null)
                        item[1]=parser.getText();
                    break;
                case XmlPullParser.END_TAG :
                    // </title>
                    if(parser.getName()!=null)
                        item[2]=parser.getName();
                    break;
            }

            if(item[0]!=null&&item[0].equals("item")) {
                // entering a new item
                title = "";
                description = "";
                link = "";
                published = -1;
                item[0] = "";
                item[1] = "";
                item[2] = "";
            } else if(item[0]!=null&&item[0].length()>0&&item[1]!=null&&item[1].length()>0) {
                // parsing item tag
                // example :
                // item[0] = <title>
                // item[1] = Titre
                switch (item[0]) {
                    case "title" :
                        title+=item[1];
                        break;
                    case "description" :
                        description+=item[1];
                        break;
                    case "pubDate" :
                        published = parseTime(item[1]);
                        break;
                    case "published" :
                        published = parseTime(item[1]);
                        break;
                    case "link" :
                        link+=item[1];
                        break;
                }
                item[0] = "";
                item[1] = "";
            } else if(item[2]!=null&&item[2].equals("item")) {
                // was an item closing tag
                // e.g. </item> : writing parsed item

                items.add(new RssItem(feed, feedUrl, title, link, description, published));
                item[0] = "";
                item[1] = "";
                item[2] = "";
            }
            parser.next();
        }
        return items;
    }

    private static long parseTime(String a) {
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        try {
            return formatter.parse(a).getTime();
        } catch(ParseException e) {
            return -1;
        }
    }



    
}
