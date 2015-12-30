package edu.kit.teco.radarbeacon.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Iris on 15.12.2015.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
