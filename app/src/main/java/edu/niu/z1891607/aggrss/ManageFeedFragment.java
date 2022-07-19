package edu.niu.z1891607.aggrss;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.nfc.FormatException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.SAXParserFactory;

public class ManageFeedFragment extends Fragment {
    private ArrayList<Feed> feeds;
    private DatabaseManager dbManager;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ManageFeedFragment() { super(R.layout.fragment_manage_feed); }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        dbManager = new DatabaseManager(getContext());
    }

    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        feeds = dbManager.selectAllFeeds();

        ListView listView = v.findViewById(R.id.feed_list);
        ManageAdapter adapter = new ManageAdapter(feeds, getContext());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, pos, id) -> {
            ColorDrawable currentColor = (ColorDrawable) view.getBackground();
            int currentId = currentColor.getColor();

            if(Integer.toHexString(ContextCompat.getColor(requireContext(), R.color.transparent))
                    .equals(Integer.toHexString(currentId))) {
                view.setBackgroundColor(getResources().getColor(R.color.green_500));
            }
            else
                view.setBackgroundColor(getResources().getColor(R.color.transparent));

            Feed selectedFeed = ((Feed) parent.getAdapter().getItem(pos));
            selectedFeed.toggleEnabled();
            dbManager.updateFeedById(selectedFeed.getId(), selectedFeed.isEnabled());
        });

        FloatingActionButton fab = v.findViewById(R.id.fab_add);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add a new RSS feed");

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View addInflated = inflater.inflate(R.layout.add_feed_dialog,
                    (ViewGroup) getView(), false);
            EditText titleET = addInflated.findViewById(R.id.title_et);
            EditText urlET = addInflated.findViewById(R.id.url_et);
            builder.setView(addInflated);

            builder.setPositiveButton("Add", (dialog, i) -> {
                AlertDialog.Builder fetchBuilder = new AlertDialog.Builder(getContext());
                fetchBuilder.setCancelable(false);

                View fetchInflated = inflater.inflate(R.layout.fetch_entries_dialog,
                        (ViewGroup) getView(), false);
                fetchBuilder.setView(fetchInflated);

                AlertDialog fetchDialog = fetchBuilder.show();

                Feed newFeed = new Feed(titleET.getText().toString(),
                        urlET.getText().toString());
                executor.execute(() -> {
                    final AtomicBoolean atomicError = new AtomicBoolean(false);

                    try {
                        SAXHandler saxHandler = new SAXHandler();
                        SAXParserFactory.newInstance().newSAXParser().parse(newFeed.getUrl(),
                                saxHandler);

                        if(!saxHandler.isValidRSS()) throw new FormatException();
                    } catch (Exception e) { atomicError.set(true); }

                    handler.post(() -> {
                        fetchDialog.dismiss();

                        if(atomicError.get()) {
                            AlertDialog.Builder errorBuilder = new AlertDialog.Builder(getContext());
                            errorBuilder.setTitle("Fetch error");

                            View errorInflated = inflater.inflate(R.layout.fetch_error_dialog,
                                    (ViewGroup) getView(), false);
                            errorBuilder.setView(errorInflated);

                            errorBuilder.setPositiveButton("Ok",
                                    (innerDialog, innerI) -> innerDialog.cancel());

                            errorBuilder.show();
                        }
                        else {
                            dbManager.insertFeed(newFeed);
                            feeds.add(newFeed);

                            adapter.notifyDataSetChanged();
                        }
                    });
                });
            });

            builder.setNegativeButton("Cancel", (dialog, i) -> dialog.cancel());

            builder.show();
        });
    }
}
