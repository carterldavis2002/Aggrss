package edu.niu.z1891607.aggrss;

import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SAXHandler extends DefaultHandler
{
    private String element = "";
    private Entry currentEntry = null;
    private final ArrayList<Entry> entries = new ArrayList<>();

    private int rssElementCount = 0;
    private int channelElementCount = 0;
    private boolean valid = true;

    StringBuilder titleBuild, linkBuild, dateBuild, descriptionBuild;

    public ArrayList<Entry> getEntries() { return entries; }

    public boolean isValidRSS() { return valid && rssElementCount == 1
            && channelElementCount == 1; }

    public void startElement(String uri, String localName, @NonNull String startElement,
                             Attributes attributes)
    {
        if(startElement.equals("item") && channelElementCount != 1) valid = false;
        if(startElement.equals("item") && currentEntry != null) valid = false;
        if(startElement.equals("channel") && rssElementCount != 1) valid = false;

        titleBuild = new StringBuilder();
        linkBuild = new StringBuilder();
        dateBuild = new StringBuilder();
        descriptionBuild = new StringBuilder();

        element = startElement;
        if (startElement.equals("item")) { currentEntry = new Entry(); }

        if(startElement.equals("rss")) rssElementCount++;
        if(startElement.equals("channel")) channelElementCount++;
    }

    public void endElement(String uri, String localName, @NonNull String endElement)
    {
        if(currentEntry != null)
        {
            switch (endElement)
            {
                case "title":
                    currentEntry.setTitle(Html.fromHtml(titleBuild.toString().trim()).toString());
                    break;
                case "link":
                    currentEntry.setLink(linkBuild.toString().trim());
                    break;
                case "pubDate":
                    currentEntry.setDate(dateBuild.toString().trim());
                    break;
                case "description":
                    currentEntry.setDescription(descriptionBuild.toString().trim());
                    break;
            }
        }

        if (endElement.equals("item")) { entries.add(currentEntry); currentEntry = null; }
    }

    public void characters(char[] ch, int start, int length)
    {
        if(currentEntry != null)
        {
            switch (element)
            {
                case "title":
                    for (int i = start; i < start + length; i++) titleBuild.append(ch[i]);
                    break;
                case "link":
                    for (int i = start; i < start + length; i++) linkBuild.append(ch[i]);
                    break;
                case "pubDate":
                    for (int i = start; i < start + length; i++) dateBuild.append(ch[i]);
                    break;
                case "description":
                    for (int i = start; i < start + length; i++) descriptionBuild.append(ch[i]);
                    break;
            }
        }
    }
}

