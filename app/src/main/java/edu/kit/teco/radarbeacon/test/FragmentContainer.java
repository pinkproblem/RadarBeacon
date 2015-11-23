package edu.kit.teco.radarbeacon.test;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.LinearLayout;

/**
 * Created by Iris on 22.11.2015.
 */
public class FragmentContainer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view = new LinearLayout(this);
        view.setId(1);

        setContentView(view);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        //ResultFragmentTest.fragment.updateResults(results);
    }
}
