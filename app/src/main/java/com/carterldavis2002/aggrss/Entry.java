package com.carterldavis2002.aggrss;

public class Entry
{
    private String title = "";
    private String link;
    private String date;
    private String description;
    private Feed feed;

    public Entry() {}

    public void setTitle(String newTitle) { title = newTitle; }

    public void setLink(String newLink) { link = newLink; }

    public void setDate(String newDate) { date = newDate; }

    public void setFeed(Feed feed) { this.feed = feed; }

    public void setDescription(String description) { this.description = description; }

    public String getDescription() { return description; }

    public String getTitle() { return title; }

    public String getLink() { return link; }

    public String getDate() { return date; }

    public Feed getFeed() { return feed; }
}

