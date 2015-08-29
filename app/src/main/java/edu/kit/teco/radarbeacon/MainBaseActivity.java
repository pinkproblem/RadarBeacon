package edu.kit.teco.radarbeacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;

import edu.kit.teco.radarbeacon.compass.CompassManager;
import edu.kit.teco.radarbeacon.compass.RotationChangeListener;

public class MainBaseActivity extends AppCompatActivity implements RotationChangeListener, MeasureFragment.OnMeasureCompleteListener {

    private CompassManager compassManager;
    private float azimuth;

    protected Fragment currentFragment;
    protected MeasureFragment measureFragment;
    protected ResultFragment resultFragment;

    protected ArrayList<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_base);

        //compass stuff
        compassManager = new CompassManager(this);
        //register for rotation updates
        compassManager.registerRotationListener(this);

        devices = (ArrayList<BluetoothDevice>) getIntent().getSerializableExtra(StartMenuActivity.EXTRA_DEVICES);

        measureFragment = MeasureFragment.getInstance(devices.size());
        resultFragment = new ResultFragment();

        //activate the measure fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        currentFragment = measureFragment;
        fragmentTransaction.add(R.id.main_container, currentFragment);
        fragmentTransaction.commit();
        //as long as the app is measuring, the screen should not turn off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();

        compassManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        compassManager.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAzimuthChange(float newAzimuth) {
        azimuth = newAzimuth;
    }

    protected float getAzimuth() {
        return azimuth;
    }

    @Override
    public void onMeasureComplete() {
        //there are enough values for an evaluation, so change to result fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        currentFragment = resultFragment;
        fragmentTransaction.replace(R.id.main_container, currentFragment);
        fragmentTransaction.commit();

        //dont forget to release the wakelock
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }
}