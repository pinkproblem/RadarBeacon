package edu.kit.teco.radarbeacon.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.kit.teco.radarbeacon.R;

/**
 * Created by Iris on 15.12.2015.
 */
public class ResetPreference extends Preference {

    private Context context;

    public ResetPreference(Context context) {
        super(context);
        this.context = context;
    }

    public ResetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ResetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return li.inflate(R.layout.preference_text_only, parent, false);
    }

    @Override
    protected void onClick() {
        SharedPreferences pref = context.getSharedPreferences(SettingsFragment.PREF_TUT_MEASURE, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(SettingsFragment.PREF_TUT_MEASURE, true);
        editor.apply();

        pref = context.getSharedPreferences(SettingsFragment.PREF_TUT_RESULT, 0);
        editor = pref.edit();
        editor.putBoolean(SettingsFragment.PREF_TUT_RESULT, true);
        editor.apply();

        Toast.makeText(context, R.string.reset_tutorial, Toast.LENGTH_SHORT).show();
    }
}
