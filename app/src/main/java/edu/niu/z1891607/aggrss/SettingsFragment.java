package edu.niu.z1891607.aggrss;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    private static final String PREF_THEME_STRING = "THEME_STRING";
    private String PREF_THEME_INT;
    private String PREF_SORT_OPTION;
    private String PREF_MAX_ENTRIES;

    private SharedPreferences pref;

    private String[] themes;
    private int currentTheme;
    private Spinner themeDropdown;

    private String[] sortOptions;
    private Spinner sortDropdown;

    private EditText maxET;
    private int currentMax;

    public SettingsFragment() { super(R.layout.fragment_settings); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeDropdown = view.findViewById(R.id.theme_dropdown);
        sortDropdown = view.findViewById(R.id.sort_dropdown);
        maxET = view.findViewById(R.id.max_entries_et);
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            PREF_THEME_INT = (String) requireActivity().getClass().getField("PREF_THEME_INT")
                    .get(requireActivity());
        } catch (Exception e) { PREF_THEME_INT = "THEME_MODE"; }

        themes = new String[]{getString(R.string.theme_dropdown_system),
                getString(R.string.theme_dropdown_day), getString(R.string.theme_dropdown_night)};

        themeDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item, themes));

        currentTheme = pref.getInt(PREF_THEME_STRING, R.string.theme_dropdown_system);
        themeDropdown.setSelection(java.util.Arrays.asList(themes).indexOf(
                getString(currentTheme)));
        themeDropdown.setOnItemSelectedListener(new ThemeItemSelectHandler());

        try {
            PREF_SORT_OPTION = (String) requireActivity().getClass()
                    .getField("PREF_SORT_OPTION").get(requireActivity());
        } catch (Exception e) { PREF_SORT_OPTION = "SORT_OPTION"; }

        sortOptions = new String[]{getString(R.string.sort_dropdown_newest),
                getString(R.string.sort_dropdown_oldest)};

        sortDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                sortOptions));

        int currentSortOption = pref.getInt(PREF_SORT_OPTION,
                R.string.sort_dropdown_newest);
        sortDropdown
                .setSelection(java.util.Arrays.asList(sortOptions).indexOf(
                        getString(currentSortOption)));
        sortDropdown.setOnItemSelectedListener(new SortItemSelectHandler());

        try {
            PREF_MAX_ENTRIES = (String) requireActivity().getClass()
                    .getField("PREF_MAX_ENTRIES").get(requireActivity());
        } catch (Exception e) { PREF_MAX_ENTRIES = "MAX_ENTRIES"; }

        int PREF_MAX_ENTRIES_DEFAULT;
        try {
            PREF_MAX_ENTRIES_DEFAULT = requireActivity().getClass()
                    .getField("PREF_MAX_ENTRIES_DEFAULT").getInt(requireActivity());
        } catch (Exception e) { PREF_MAX_ENTRIES_DEFAULT = 50; }

        currentMax = pref.getInt(PREF_MAX_ENTRIES, PREF_MAX_ENTRIES_DEFAULT);
        maxET.setText(String.valueOf(currentMax));
        maxET.setOnFocusChangeListener(new MaxEditTextFocusHandler());
        maxET.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) maxET.clearFocus();
            return false;
        });
    }

    private class ThemeItemSelectHandler implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            SharedPreferences.Editor editor = pref.edit();

            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            int stringRes = R.string.theme_dropdown_system;

            if(themes[i].equals(getString(R.string.theme_dropdown_day))) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                stringRes = R.string.theme_dropdown_day;
            }

            if(themes[i].equals(getString(R.string.theme_dropdown_night))) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                stringRes = R.string.theme_dropdown_night;
            }

            if(!getString(currentTheme).equals(themes[i])) {
                currentTheme = stringRes;

                AppCompatDelegate.setDefaultNightMode(mode);
            }
            editor.putInt(PREF_THEME_STRING, stringRes);
            editor.putInt(PREF_THEME_INT, mode);
            editor.apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    private class SortItemSelectHandler implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            SharedPreferences.Editor editor = pref.edit();

            int stringRes = R.string.sort_dropdown_newest;
            if(sortOptions[i].equals(getString(R.string.sort_dropdown_oldest)))
                stringRes = R.string.sort_dropdown_oldest;

            editor.putInt(PREF_SORT_OPTION, stringRes);
            editor.apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    private class MaxEditTextFocusHandler implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if(hasFocus) return;

            try {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(PREF_MAX_ENTRIES, Integer.parseInt(((EditText) view)
                        .getText().toString()));
                editor.apply();
            } catch (Exception e) {
                ((EditText) view).setText(String.valueOf(currentMax));
            }
        }
    }
}
