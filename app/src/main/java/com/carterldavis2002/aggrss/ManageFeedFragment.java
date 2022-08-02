package com.carterldavis2002.aggrss;

import android.app.AlertDialog;
import android.content.Context;
import android.nfc.FormatException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.SAXParserFactory;

public class ManageFeedFragment extends Fragment {
    private ArrayList<Feed> feeds;
    private DatabaseManager dbManager;
    private ManageAdapter adapter;

    private ListView listView;
    private FloatingActionButton fab;

    public ManageFeedFragment() { super(R.layout.fragment_manage_feed); }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        dbManager = new DatabaseManager(context);
    }

    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        listView = v.findViewById(R.id.feed_list);
        fab = v.findViewById(R.id.fab_add);
    }

    @Override
    public void onStart() {
        super.onStart();

        feeds = dbManager.selectAllFeeds();
        adapter = new ManageAdapter(feeds, getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ListItemClickHandler());
        fab.setOnClickListener(new FabClickHandler());
    }

    private class ListItemClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(@NonNull AdapterView parent, View view, int pos, long id) {
            Feed selectedFeed = ((Feed) parent.getAdapter().getItem(pos));

            if(!selectedFeed.isEnabled())
                view.setBackgroundColor(getResources().getColor(R.color.green_500));
            else
                view.setBackgroundColor(getResources().getColor(R.color.transparent));

            selectedFeed.toggleEnabled();
            dbManager.updateFeedById(selectedFeed.getId(), selectedFeed.isEnabled());
        }
    }

    private class FabClickHandler implements View.OnClickListener {
        private EditText titleET;
        private EditText urlET;
        private AlertDialog fetchDialog;

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.add_dialog_title));

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View addInflated = inflater.inflate(R.layout.add_feed_dialog,
                    (ViewGroup) getView(), false);
            titleET = addInflated.findViewById(R.id.title_et);
            urlET = addInflated.findViewById(R.id.url_et);
            builder.setView(addInflated);

            builder.setPositiveButton(getString(R.string.add_dialog_positive_btn), (dialog, i) -> {
                AlertDialog.Builder fetchBuilder = new AlertDialog.Builder(getContext());
                fetchBuilder.setCancelable(false);

                View fetchInflated = inflater.inflate(R.layout.fetch_entries_dialog,
                        (ViewGroup) getView(), false);
                fetchBuilder.setView(fetchInflated);

                fetchDialog = fetchBuilder.show();

                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(new FeedCheckRunnable());
            });

            builder.setNegativeButton(getString(R.string.add_dialog_negative_btn),
                    (dialog, i) -> dialog.cancel());

            builder.show();
        }

        private class FeedCheckRunnable implements Runnable {
            @Override
            public void run() {
                AtomicBoolean atomicError = new AtomicBoolean(false);

                Feed newFeed = new Feed(titleET.getText().toString(),
                        urlET.getText().toString());
                try {
                    SAXHandler saxHandler = new SAXHandler();
                    SAXParserFactory.newInstance().newSAXParser().parse(newFeed.getUrl(),
                            saxHandler);

                    if(!saxHandler.isValidRSS()) throw new FormatException();
                } catch (Exception e) { atomicError.set(true); }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    fetchDialog.dismiss();

                    if(atomicError.get()) {
                        AlertDialog.Builder errorBuilder = new AlertDialog.Builder(getContext());
                        errorBuilder.setTitle(getString(R.string.error_dialog_title));

                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View errorInflated = inflater.inflate(R.layout.fetch_error_dialog,
                                (ViewGroup) getView(), false);
                        errorBuilder.setView(errorInflated);

                        errorBuilder.setPositiveButton(
                                getString(R.string.error_dialog_positive_btn),
                                (innerDialog, innerI) -> innerDialog.cancel());

                        errorBuilder.show();
                    }
                    else {
                        dbManager.insertFeed(newFeed);
                        feeds.add(newFeed);

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
