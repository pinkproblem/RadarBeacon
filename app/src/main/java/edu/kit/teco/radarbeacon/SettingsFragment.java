package edu.kit.teco.radarbeacon;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Iris on 15.12.2015.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String PREF_TUT_MEASURE = "pref_tut_measure";
    public static final String PREF_TUT_RESULT = "pref_tut_result";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
