package com.carterldavis2002.aggrss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
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
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.SAXParserFactory;

public class ViewFeedFragment extends Fragment {
    private DatabaseManager dbManager;
    private ArrayList<Entry> entries;
    private final ArrayList<Entry> removedEntries = new ArrayList<>();
    private boolean descending;

    private ExpandableListView listView;
    private EntryAdapter adapter;

    private SharedPreferences pref;
    private String PREF_MAX_ENTRIES;
    private int PREF_MAX_ENTRIES_DEFAULT;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean chosenDates;

    private View inflatedDialog;

    private TextView noResultsTV;
    private TextView fetchingTV;

    private SwipeRefreshLayout refreshLayout;
    private boolean refreshing = true;

    public ViewFeedFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dbManager = new DatabaseManager(context);
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_view_feed, container, false);
    }

    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        refreshLayout = v.findViewById(R.id.refresh_layout);
        listView = v.findViewById(R.id.feed_entries_list);
        noResultsTV = v.findViewById(R.id.no_results_tv);
        fetchingTV = v.findViewById(R.id.fetching_tv);

    }

    @Override
    public void onStart() {
        super.onStart();

        String PREF_SORT_OPTION;
        try {
            PREF_SORT_OPTION = (String) requireActivity().getClass()
                    .getField("PREF_SORT_OPTION").get(requireActivity());
        } catch (Exception e) { PREF_SORT_OPTION = "SORT_OPTION"; }

        descending = pref.getInt(PREF_SORT_OPTION, R.string.sort_dropdown_newest)
                == R.string.sort_dropdown_newest;

        refreshLayout.setOnRefreshListener(() -> {
            if(!refreshing) {
                refreshing = true;
                getAndDisplayEntries();
                removedEntries.clear();
            }
            refreshLayout.setRefreshing(false);
        });

        listView.setOnGroupExpandListener(i -> {
            if(adapter == null) return;

            for(int j = 0;j < adapter.getGroupCount();j++) {
                if(j != i) listView.collapseGroup(j);
            }
        });

        getAndDisplayEntries();
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.search_item) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.search_dialog_title));

            inflatedDialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.search_entries_dialog, (ViewGroup) getView(),
                            false);
            EditText titleET = inflatedDialog.findViewById(R.id.entry_title_et);

            startDate = LocalDateTime.MIN;
            endDate = LocalDateTime.MAX;
            chosenDates = false;

            Button rangeBtn = inflatedDialog.findViewById(R.id.date_range_btn);
            rangeBtn.setOnClickListener(new DateRangeClickHandler());

            builder.setView(inflatedDialog);

            builder.setPositiveButton(getString(R.string.search_dialog_positive_button),
                    (dialog, i) -> {
                for(int j = 0;j < adapter.getGroupCount();j++) listView.collapseGroup(j);

                String searchTerm = titleET.getText().toString().toLowerCase().trim();

                DateTimeFormatter formatter = DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

                filterEntriesByKeywordAndDate(searchTerm, formatter, false);
                filterEntriesByKeywordAndDate(searchTerm, formatter, true);

                sortEntriesByDateTime(formatter, entries, descending);

                if(entries.size() == 0) noResultsTV.setVisibility(View.VISIBLE);
                else noResultsTV.setVisibility(View.GONE);
            });

            builder.setNegativeButton(getString(R.string.search_dialog_negative_button),
                    (dialog, i) -> dialog.cancel());

            builder.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAndDisplayEntries() {
        noResultsTV.setVisibility(View.GONE);
        fetchingTV.setVisibility(View.VISIBLE);

        entries = new ArrayList<>();
        adapter = new EntryAdapter(entries, getContext());
        listView.setAdapter(adapter);

        if(dbManager.selectAllFeeds().size() == 0) {
            fetchingTV.setVisibility(View.GONE);
            noResultsTV.setVisibility(View.VISIBLE);
        }

        AtomicInteger feedsParsed = new AtomicInteger();
        for(Feed feed : dbManager.selectAllFeeds()) {
            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                SAXHandler saxHandler = new SAXHandler();
                if(feed.isEnabled()) {
                    try {
                        SAXParserFactory.newInstance().newSAXParser().parse(feed.getUrl(),
                                saxHandler);
                    } catch (Exception e) { e.printStackTrace(); }
                }

                handler.post(() -> {
                    feedsParsed.getAndIncrement();

                    DateTimeFormatter rfc1123Formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
                    DateTimeFormatter localizedFormatter = DateTimeFormatter.ofLocalizedDateTime(
                            FormatStyle.MEDIUM, FormatStyle.SHORT);

                    try {
                        PREF_MAX_ENTRIES = (String) requireActivity().getClass()
                                .getField("PREF_MAX_ENTRIES").get(requireActivity());
                    } catch (Exception e) { PREF_MAX_ENTRIES = "MAX_ENTRIES"; }

                    try {
                        PREF_MAX_ENTRIES_DEFAULT = requireActivity().getClass()
                                .getField("PREF_MAX_ENTRIES_DEFAULT")
                                .getInt(requireActivity());
                    } catch (Exception e) { PREF_MAX_ENTRIES_DEFAULT = 50; }

                    if(saxHandler.isValidRSS()) {
                        ArrayList<Entry> retrievedEntries = saxHandler.getEntries();
                        for(Entry e : retrievedEntries) {
                            e.setFeed(feed);

                            try {
                                ZonedDateTime zdt = ZonedDateTime.parse(e.getDate(),
                                        rfc1123Formatter);
                                ZonedDateTime local = zdt.withZoneSameInstant(
                                        ZoneId.systemDefault());
                                e.setDate(local.format(localizedFormatter));
                            }
                            catch(Exception exception) { e.setDate(""); }
                        }

                        sortEntriesByDateTime(localizedFormatter, retrievedEntries,
                                true);

                        int maxEntries = pref.getInt(PREF_MAX_ENTRIES,
                                PREF_MAX_ENTRIES_DEFAULT);
                        while(retrievedEntries.size() > maxEntries)
                            retrievedEntries.remove(retrievedEntries.size() - 1);

                        entries.addAll(retrievedEntries);
                        sortEntriesByDateTime(localizedFormatter, entries, descending);
                    }

                    if(feedsParsed.get() == dbManager.selectAllFeeds().size()) {
                        fetchingTV.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        refreshing = false;

                        if(entries.size() == 0)
                            noResultsTV.setVisibility(View.VISIBLE);
                    }
                });
            });
        }
    }

    private void sortEntriesByDateTime(DateTimeFormatter formatter, ArrayList<Entry> entryArr,
                                       boolean descending) {
        Collections.sort(entryArr, (o1, o2) -> {
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

    private void filterEntriesByKeywordAndDate(String searchTerm, DateTimeFormatter formatter,
                                               boolean removed) {
        Iterator<Entry> it;
        if(!removed)
            it = entries.iterator();
        else
            it = removedEntries.iterator();

        while(it.hasNext()) {
            Entry e = it.next();

            LocalDateTime ldt;
            if(!e.getDate().equals(""))
                ldt = LocalDateTime.parse(e.getDate(), formatter);
            else
                ldt = LocalDateTime.MAX;

            if(!removed && (!e.getTitle().toLowerCase().contains(searchTerm)
                    || (!(ldt.compareTo(startDate) >= 0) && chosenDates)
                    || (!(ldt.compareTo(endDate) < 0) && chosenDates))) {
                removedEntries.add(e);

                it.remove();
                adapter.notifyDataSetChanged();
            }
            else if(removed && (e.getTitle().toLowerCase().contains(searchTerm)
                    && ((ldt.compareTo(startDate) >= 0)
                    && (ldt.compareTo(endDate) < 0) || !chosenDates))) {
                entries.add(e);

                it.remove();
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class DateRangeClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            MaterialDatePicker<Pair<Long, Long>> rangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText(getString(R.string.date_picker_title)).build();
            rangePicker.addOnPositiveButtonClickListener(selection -> {
                chosenDates = true;

                startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(selection.first),
                        ZoneId.of("UTC"));

                endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(selection.second),
                        ZoneId.of("UTC")).plusDays(1);

                TextView selected = inflatedDialog.findViewById(R.id.chosen_dates_tv);
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
                selected.setText(getString(R.string.search_dialog_chosen_dates,
                        formatter.format(startDate), formatter.format(endDate.minusDays(1))));
            });

            rangePicker.show(requireActivity().getSupportFragmentManager(),
                    "FILTER_PICKER");
        }
    }
}