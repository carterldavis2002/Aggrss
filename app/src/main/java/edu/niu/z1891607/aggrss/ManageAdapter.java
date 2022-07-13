package edu.niu.z1891607.aggrss;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ManageAdapter extends BaseAdapter
{
    private final ArrayList<Feed> feeds;
    private final Context context;
    private final DatabaseManager dbManager;

    public ManageAdapter(ArrayList<Feed> feeds, Context context)
    {
        this.dbManager = new DatabaseManager(context);
        this.feeds = feeds;
        this.context = context;
    }

    public int getCount()
    {
        return feeds.size();
    }

    public Object getItem(int pos)
    {
        return feeds.get(pos);
    }

    public long getItemId(int pos)
    {
        return 0;
    }

    public View getView(int pos, View convert, ViewGroup parent)
    {
        if(convert == null)
            convert = LayoutInflater.from(context).inflate(R.layout.manage_feed_list, parent,
                    false);

        Feed currentFeed = (Feed) getItem(pos);

        if(currentFeed.isEnabled())
            convert.setBackgroundColor(context.getResources().getColor(R.color.green));
        else
            convert.setBackgroundColor(context.getResources().getColor(R.color.transparent));

        TextView titleTV = convert.findViewById(R.id.title_tv);
        titleTV.setText(currentFeed.getTitle());

        TextView urlTV = convert.findViewById(R.id.url_tv);
        urlTV.setText(currentFeed.getUrl());

        ImageButton deleteBtn = convert.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(v -> {
            dbManager.deleteFeedById(feeds.get(pos).getId());
            feeds.remove(pos);
            notifyDataSetChanged();
        });

        return convert;
    }
}
