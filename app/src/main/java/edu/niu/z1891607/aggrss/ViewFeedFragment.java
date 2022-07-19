package edu.niu.z1891607.aggrss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.SAXParserFactory;

public class ViewFeedFragment extends Fragment {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private DatabaseManager dbManager;
    private ArrayList<Entry> entries;
    private final ArrayList<Entry> removedEntries = new ArrayList<>();

    private ExpandableListView listView;
    private EntryAdapter adapter;

    public ViewFeedFragment() {}

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        dbManager = new DatabaseManager(getContext());
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

            AtomicReference<LocalDateTime> startDate = new AtomicReference<>(LocalDateTime.MIN);
            AtomicReference<LocalDateTime> endDate = new AtomicReference<>(LocalDateTime.MAX);
            AtomicBoolean chosenDates = new AtomicBoolean(false);

            Button rangeBtn = inflated.findViewById(R.id.date_range_btn);
            rangeBtn.setOnClickListener(v -> {
                MaterialDatePicker<Pair<Long, Long>> rangePicker =
                        MaterialDatePicker.Builder.dateRangePicker()
                                .setTitleText("Select range of dates").build();
                rangePicker.addOnPositiveButtonClickListener(selection -> {
                    chosenDates.set(true);

                    startDate.set(LocalDateTime.ofInstant(Instant.ofEpochMilli(selection.first),
                                ZoneId.of("UTC")));

                    endDate.set(LocalDateTime.ofInstant(Instant.ofEpochMilli(selection.second),
                                ZoneId.of("UTC")).plusDays(1));

                    TextView selected = inflated.findViewById(R.id.chosen_dates_tv);
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
                    selected.setText("Chosen Dates: " + formatter.format(startDate.get()) + " - "
                    + formatter.format(endDate.get().minusDays(1)));
                });

                rangePicker.show(requireActivity().getSupportFragmentManager(),
                        "FILTER_PICKER");
            });

            builder.setView(inflated);

            builder.setPositiveButton("Search", (dialog, i) -> {
                for(int j = 0;j < adapter.getGroupCount();j++) listView.collapseGroup(j);

                String searchTerm = titleET.getText().toString().toLowerCase().trim();

                DateTimeFormatter formatter = DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

                Iterator<Entry> it = entries.iterator();
                while(it.hasNext())
                {
                    Entry e = it.next();

                    LocalDateTime ldt;
                    if(!e.getDate().equals(""))
                        ldt = LocalDateTime.parse(e.getDate(), formatter);
                    else
                        ldt = LocalDateTime.MAX;

                    if(!e.getTitle().toLowerCase().contains(searchTerm)
                            || (!(ldt.compareTo(startDate.get()) >= 0) && chosenDates.get())
                            || (!(ldt.compareTo(endDate.get()) < 0) && chosenDates.get())) {
                        removedEntries.add(e);
                        it.remove();
                        adapter.notifyDataSetChanged();
                    }
                }

                it = removedEntries.iterator();
                while(it.hasNext())
                {
                    Entry e = it.next();

                    LocalDateTime ldt;
                    if(!e.getDate().equals(""))
                        ldt = LocalDateTime.parse(e.getDate(), formatter);
                    else
                        ldt = LocalDateTime.MAX;

                    if(e.getTitle().toLowerCase().contains(searchTerm)
                            && ((ldt.compareTo(startDate.get()) >= 0)
                            && (ldt.compareTo(endDate.get()) < 0) || !chosenDates.get())) {
                        entries.add(e);
                        it.remove();
                        adapter.notifyDataSetChanged();
                    }
                }

                sortEntriesByDateTime(formatter);
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
            for(Feed feed : dbManager.selectAllFeeds())
            {
                if(feed.isEnabled()) {
                    try {
                        SAXHandler saxHandler = new SAXHandler();
                        SAXParserFactory.newInstance().newSAXParser().parse(feed.getUrl(),
                                saxHandler);

                        if(saxHandler.isValidRSS()) {
                            for(Entry e : saxHandler.getEntries()) e.setFeed(feed);
                            entries.addAll(saxHandler.getEntries());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            DateTimeFormatter rfc1123Formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            DateTimeFormatter localizedFormatter = DateTimeFormatter.ofLocalizedDateTime(
                    FormatStyle.MEDIUM, FormatStyle.SHORT);
            for(Entry e : entries)
            {
                try {
                    ZonedDateTime zdt = ZonedDateTime.parse(e.getDate(), rfc1123Formatter);
                    ZonedDateTime local = zdt.withZoneSameInstant(ZoneId.systemDefault());
                    e.setDate(local.format(localizedFormatter));
                }
                catch(Exception exception) { e.setDate(""); }
            }


            sortEntriesByDateTime(localizedFormatter);

            handler.post(() -> {
                listView = v.findViewById(R.id.feed_entries_list);
                adapter = new EntryAdapter(entries, getContext());
                listView.setAdapter(adapter);
            });
        });
    }

    private void sortEntriesByDateTime(DateTimeFormatter formatter) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean descending = pref.getString("SORT_OPTION", "Newest").equals("Newest");

        Collections.sort(entries, (o1, o2) -> {
            LocalDateTime ldt1;
            if(!o1.getDate().equals(""))
                ldt1 = LocalDateTime.parse(o1.getDate(), formatter);
            else
                ldt1 = LocalDateTime.MAX;

            LocalDateTime ldt2;
            if(!o2.getDate().equals(""))
                ldt2 = LocalDateTime.parse(o2.getDate(), formatter);
            else
                ldt2 = LocalDateTime.MAX;

            return descending ? ldt2.compareTo(ldt1) : ldt1.compareTo(ldt2);
        });
    }
}