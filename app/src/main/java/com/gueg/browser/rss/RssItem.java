package com.gueg.browser.rss;

public class RssItem {
    public final String feed;
    public final String feedUrl;
    public final String title;
    public final String link;
    public final String description;
    public final long published;

    public RssItem(String feed, String feedUrl, String title, String link, String description, long published) {
        this.feed = feed;
        this.feedUrl = feedUrl;
        this.title = title;
        this.link = link;
        this.published = published;
        this.description = description;
    }
}
