package edu.niu.z1891607.aggrss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

public class ManageFeedFragment extends Fragment {
    private ArrayList<Feed> feeds;
    private FeedCommunication mCallback;

    public ManageFeedFragment() { super(R.layout.fragment_manage_feed); }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mCallback = (FeedCommunication) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement FeedCommunication");
        }
    }

    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        feeds = mCallback.getFeedsArray();

        ListView listView = v.findViewById(R.id.feed_list);
        ManageAdapter adapter = new ManageAdapter(feeds, getContext(), this::saveFeeds);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, pos, id) -> {
            ColorDrawable currentColor = (ColorDrawable) view.getBackground();
            int currentId = currentColor.getColor();

            if(Integer.toHexString(ContextCompat.getColor(requireContext(), R.color.transparent))
                    .equals(Integer.toHexString(currentId))) {
                view.setBackgroundColor(getResources().getColor(R.color.green));
            }
            else
                view.setBackgroundColor(getResources().getColor(R.color.transparent));

            ((Feed) parent.getAdapter().getItem(pos)).toggleEnabled();
            saveFeeds();
        });

        FloatingActionButton fab = v.findViewById(R.id.fab_add);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add a new RSS feed");

            View inflated = LayoutInflater.from(getContext()).inflate(R.layout.add_feed_dialog,
                    (ViewGroup) getView(), false);
            EditText titleET = inflated.findViewById(R.id.title_et);
            EditText urlET = inflated.findViewById(R.id.url_et);
            builder.setView(inflated);

            builder.setPositiveButton("Add", (dialog, i) -> {
                Feed newFeed = new Feed(titleET.getText().toString(), urlET.getText().toString());
                feeds.add(newFeed);
                saveFeeds();

                adapter.notifyDataSetChanged();
            });

            builder.setNegativeButton("Cancel", (dialog, i) -> dialog.cancel());

            builder.show();
        });
    }

    public interface FeedSave { void saveFeeds(); }

    public void saveFeeds() {
        mCallback.setFeedsArray(feeds);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        editor.putString("FEEDS", gson.toJson(feeds));
        editor.apply();
    }
}
