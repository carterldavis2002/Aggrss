package edu.niu.z1891607.aggrss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class EntryAdapter extends BaseExpandableListAdapter
{
    private final ArrayList<Entry> entries;
    private final Context context;

    public EntryAdapter(ArrayList<Entry> entries, Context context)
    {
        this.entries = entries;
        this.context = context;
    }

    public int getGroupCount()
    {
        return entries.size();
    }

    public int getChildrenCount(int pos)
    {
        return 1;
    }

    public Object getGroup(int pos) { return entries.get(pos); }

    public Object getChild(int pos, int expandedPos) { return entries.get(pos); }

    public long getGroupId(int pos)
    {
        return 0;
    }

    public long getChildId(int pos, int expandedPos)
    {
         return 0;
    }

    public boolean hasStableIds() { return false; }

    public View getGroupView(int pos, boolean isExpanded, View convert, ViewGroup parent)
    {
        if(convert == null)
            convert = LayoutInflater.from(context).inflate(R.layout.view_feed_list_group, parent,
                    false);

        Entry currentEntry = (Entry) getGroup(pos);

        TextView nameTV = convert.findViewById(R.id.entry_title_tv);
        nameTV.setText(currentEntry.getTitle());

        TextView dateTV = convert.findViewById(R.id.date_tv);
        dateTV.setText(currentEntry.getDate());

        TextView feedTV = convert.findViewById(R.id.feed_tv);
        feedTV.setText(currentEntry.getFeed().getTitle());

        return convert;
    }

    public View getChildView(int pos, int expandedPos, boolean isLastChild, View convert,
                             ViewGroup parent)
    {
        Entry currentEntry = (Entry) getGroup(pos);

        if(convert == null)
            convert = LayoutInflater.from(context).inflate(R.layout.view_feed_list_item, parent,
                    false);

        TextView descriptionTV = convert.findViewById(R.id.description_tv);
        descriptionTV.setText(Html.fromHtml(currentEntry.getDescription()));
        descriptionTV.setMovementMethod(LinkMovementMethod.getInstance());

        Button openBtn = convert.findViewById(R.id.open_btn);
        openBtn.setOnClickListener((v) -> {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(entries.get(pos).getLink())));
            } catch(Exception e) { e.printStackTrace(); }
        });

        return convert;
    }

    public boolean isChildSelectable(int i, int i1) { return false; }
}

