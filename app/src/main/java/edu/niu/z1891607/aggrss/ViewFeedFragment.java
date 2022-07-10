package edu.niu.z1891607.aggrss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.parsers.SAXParserFactory;

public class ViewFeedFragment extends Fragment {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FeedCommunication mCallback;
    private ArrayList<Entry> entries;
    private final ArrayList<Entry> removedEntries = new ArrayList<>();
    private EntryAdapter adapter;

    public ViewFeedFragment() {}

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mCallback = (FeedCommunication) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement FeedCommunication");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_view_feed, container, false);
    }

    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        SwipeRefreshLayout refreshLayout = v.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            getAndDisplayEntries(v);
            removedEntries.clear();
            refreshLayout.setRefreshing(false);
        });

        getAndDisplayEntries(v);
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.search_item) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Search feed entries");

            View inflated = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_entries_dialog, (ViewGroup) getView(),
                            false);
            EditText titleET = inflated.findViewById(R.id.entry_title_et);
            builder.setView(inflated);

            builder.setPositiveButton("Search", (dialog, i) -> {
                String searchTerm = titleET.getText().toString().toLowerCase().trim();

                Iterator<Entry> it = entries.iterator();
                while(it.hasNext())
                {
                    Entry e = it.next();
                    if(!e.getTitle().toLowerCase().contains(searchTerm)) {
                        removedEntries.add(e);
                        it.remove();
                        adapter.notifyDataSetChanged();
                    }
                }

                it = removedEntries.iterator();
                while(it.hasNext())
                {
                    Entry e = it.next();
                    if(e.getTitle().toLowerCase().contains(searchTerm))
                    {
                        entries.add(e);
                        it.remove();
                        adapter.notifyDataSetChanged();
                    }
                }
                Collections.sort(entries, (o1, o2) -> {
                    DateTimeFormatter formatter = DateTimeFormatter
                            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
                    LocalDateTime ldt1;
                    if(!o1.getDate().equals(""))
                        ldt1 = LocalDateTime.parse(o1.getDate(), formatter);
                    else
                        ldt1 = LocalDateTime.now();


                    LocalDateTime ldt2;
                    if(!o2.getDate().equals(""))
                        ldt2 = LocalDateTime.parse(o2.getDate(), formatter);
                    else
                        ldt2 = LocalDateTime.now();

                    return ldt2.compareTo(ldt1);
                });
            });

            builder.setNegativeButton("Cancel", (dialog, i) -> dialog.cancel());

            builder.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAndDisplayEntries(View v) {
        executor.execute(() -> {
            entries = new ArrayList<>();
            for(Feed feed : mCallback.getFeedsArray())
            {
                if(feed.isEnabled()) {
                    try {
                        SAXHandler saxHandler = new SAXHandler();
                        SAXParserFactory.newInstance().newSAXParser().parse(feed.getUrl(),
                                saxHandler);
                        entries.addAll(saxHandler.getEntries());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            Collections.sort(entries, (o1, o2) -> {
                ZonedDateTime zdt1;
                if(o1.getDate() != null)
                    zdt1 = ZonedDateTime.parse(o1.getDate(), formatter);
                else
                    zdt1 = ZonedDateTime.now();
                ZonedDateTime instantInUTC1 = zdt1.withZoneSameInstant(ZoneId.of("UTC"));

                ZonedDateTime zdt2;
                if(o2.getDate() != null)
                    zdt2 = ZonedDateTime.parse(o2.getDate(), formatter);
                else
                    zdt2 = ZonedDateTime.now();
                ZonedDateTime instantInUTC2 = zdt2.withZoneSameInstant(ZoneId.of("UTC"));

                return instantInUTC2.compareTo(instantInUTC1);
            });

            for(Entry e : entries)
            {
                if(e.getDate() != null) {
                    ZonedDateTime zdt1 = ZonedDateTime.parse(e.getDate(), formatter);
                    ZonedDateTime local = zdt1.withZoneSameInstant(ZoneId.systemDefault());
                    e.setDate(local.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,
                            FormatStyle.SHORT)));
                }
                else
                    e.setDate("");
            }

            handler.post(() -> {
                ListView listView = v.findViewById(R.id.feed_entries_list);
                adapter = new EntryAdapter(entries, getContext());
                listView.setAdapter(adapter);
                listView.setOnItemClickListener((adapterView, view, i, l) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(entries.get(i).getLink())));
                });
            });
        });
    }
}