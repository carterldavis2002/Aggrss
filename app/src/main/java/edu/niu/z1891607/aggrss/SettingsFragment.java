package edu.niu.z1891607.aggrss;

import android.app.UiModeManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

public class SettingsFragment extends Fragment {
    public SettingsFragment() { super(R.layout.fragment_settings); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner themeDropdown = view.findViewById(R.id.theme_dropdown);
        String[] themes = {"System", "Day", "Night"};

        themeDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item, themes));

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        final String[] currentTheme = {pref.getString("THEME_STRING", themes[0])};
        themeDropdown.setSelection(java.util.Arrays.asList(themes).indexOf(currentTheme[0]));
        themeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = pref.edit();

                int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

                if(themes[i].equals("Day")) mode = AppCompatDelegate.MODE_NIGHT_NO;

                if(themes[i].equals("Night")) mode = AppCompatDelegate.MODE_NIGHT_YES;

                if(!currentTheme[0].equals(themes[i])) {
                    currentTheme[0] = themes[i];

                    AppCompatDelegate.setDefaultNightMode(mode);
                }
                editor.putString("THEME_STRING", themes[i]);
                editor.putInt("THEME_MODE", mode);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner sortDropdown = view.findViewById(R.id.sort_dropdown);
        String[] sortOptions = {"Newest", "Oldest"};

        sortDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                sortOptions));

        final String[] currentSortOption = {pref.getString("SORT_OPTION", sortOptions[0])};
        sortDropdown
                .setSelection(java.util.Arrays.asList(sortOptions).indexOf(currentSortOption[0]));
        sortDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("SORT_OPTION", sortOptions[i]);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}
