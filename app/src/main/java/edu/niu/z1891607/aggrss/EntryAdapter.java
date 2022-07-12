package edu.niu.z1891607.aggrss;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntryAdapter extends BaseAdapter
{
    private final ArrayList<Entry> entries;
    private final Context context;

    public EntryAdapter(ArrayList<Entry> entries, Context context)
    {
        this.entries = entries;
        this.context = context;
    }

    public int getCount()
    {
        return entries.size();
    }

    public Object getItem(int pos)
    {
        return entries.get(pos);
    }

    public long getItemId(int pos)
    {
        return 0;
    }

    public View getView(int pos, View convert, ViewGroup parent)
    {
        if(convert == null)
            convert = LayoutInflater.from(context).inflate(R.layout.view_feed_list, parent,
                    false);

        Entry currentEntry = (Entry) getItem(pos);

        TextView nameTV = convert.findViewById(R.id.entry_title_tv);
        nameTV.setText(currentEntry.getTitle());

        TextView dateTV = convert.findViewById(R.id.date_tv);
        dateTV.setText(currentEntry.getDate());

        TextView feedTV = convert.findViewById(R.id.feed_tv);
        feedTV.setText(currentEntry.getFeed().getTitle());

        return convert;
    }
}

