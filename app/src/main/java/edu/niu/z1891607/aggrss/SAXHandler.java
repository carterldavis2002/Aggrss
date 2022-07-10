package edu.niu.z1891607.aggrss;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SAXHandler extends DefaultHandler
{
    private String element = "";
    private Entry currentEntry;
    private final ArrayList<Entry> entries = new ArrayList<>();

    StringBuilder titleBuild, linkBuild, dateBuild;

    public ArrayList<Entry> getEntries() { return entries; }

    public void startElement(String uri, String localName, @NonNull String startElement,
                             Attributes attributes)
    {
        titleBuild = new StringBuilder();
        linkBuild = new StringBuilder();
        dateBuild = new StringBuilder();

        element = startElement;
        if (startElement.equals("item")) { currentEntry = new Entry(); }
    }

    public void endElement(String uri, String localName, @NonNull String endElement)
    {
        if(currentEntry != null)
        {
            switch (endElement)
            {
                case "title":
                    currentEntry.setTitle(titleBuild.toString().trim());
                    break;
                case "link":
                    currentEntry.setLink(linkBuild.toString().trim());
                    break;
                case "pubDate":
                    currentEntry.setDate(dateBuild.toString().trim());
                    break;
            }
        }

        if (endElement.equals("item")) { entries.add(currentEntry); }
    }

    public void characters(char[] ch, int start, int length)
    {
        if(currentEntry != null)
        {
            switch (element)
            {
                case "title":
                    for (int i = start; i < start + length; i++)
                    {
                        titleBuild.append(ch[i]);
                    }
                    break;
                case "link":
                    for (int i = start; i < start + length; i++)
                    {
                        linkBuild.append(ch[i]);
                    }
                    break;
                case "pubDate":
                    for (int i = start; i < start + length; i++)
                    {
                        dateBuild.append(ch[i]);
                    }
                    break;
            }
        }
    }
}

