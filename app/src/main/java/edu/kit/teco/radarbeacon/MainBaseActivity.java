package edu.kit.teco.radarbeacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.kit.teco.radarbeacon.compass.CompassManager;
import edu.kit.teco.radarbeacon.compass.RotationChangeListener;
import edu.kit.teco.radarbeacon.evaluation.CircleUtils;
import edu.kit.teco.radarbeacon.evaluation.EvaluationStrategy;

public abstract class MainBaseActivity extends AppCompatActivity implements RotationChangeListener,
        MeasureFragment.OnMeasureCompleteListener, ResultFragment.ResultCallbackListener {

    private static final int azimuthBufferSize = 5;
    private CompassManager compassManager;
    private List<Double> azimuthBuffer;
    private int bufferCounter;
    private float azimuth;
    protected float smoothAzimuth;

    protected Fragment currentFragment;
    protected MeasureFragment measureFragment;
    protected ResultFragment resultFragment;

    protected ArrayList<BluetoothDevice> devices;
    protected HashMap<BluetoothDevice, EvaluationStrategy> evaluation;


    protected abstract void startMeasurement();

    protected abstract void stopMeasurement();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_base);

        //compass stuff
        compassManager = new CompassManager(this);
        //register for rotation updates
        compassManager.registerRotationListener(this);
        azimuthBuffer = new ArrayList<>();

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
    protected void onResume() {
        super.onResume();
        compassManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassManager.stop();
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
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment == resultFragment) {
            //push results to fragment
            HashMap<BluetoothDevice, EvaluationStrategy> results = new HashMap<>();
            for (BluetoothDevice device : evaluation.keySet()) {
                results.put(device, evaluation.get(device));
            }
            resultFragment.updateResults(results);
        }
    }

    @Override
    public void onAzimuthChange(float newAzimuth) {
        azimuth = newAzimuth;

        smoothAzimuth = calculateSmoothAzimuth(newAzimuth);

        if (measureFragment != null) {
            measureFragment.rotateView(360 - (float) CircleUtils.radiansToDegree(smoothAzimuth));
        }

    }

    private float calculateSmoothAzimuth(float newAzimuth) {
        //hacky version of a ringbuffer
        if (azimuthBuffer.size() <= bufferCounter) {
            azimuthBuffer.add((double) newAzimuth);
        } else {
            azimuthBuffer.set(bufferCounter, (double) newAzimuth);
        }
        bufferCounter = (bufferCounter + 1) % azimuthBufferSize;

        return (float) CircleUtils.getMeanAngle(azimuthBuffer);
    }

    protected float getAzimuth() {
        return azimuth;
    }

    protected void addSample(BluetoothDevice device, int rssi) {
        EvaluationStrategy ev = evaluation.get(device);
        float azimuth = getAzimuth();
        long time = SystemClock.uptimeMillis();

        ev.addSample(azimuth, rssi, time);
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

        //stop scanning
        stopMeasurement();

        //push results to fragment
//        HashMap<BluetoothDevice, Float> results = new HashMap<>();
//        for (BluetoothDevice device : evaluation.keySet()) {
//            try {
//                float azimuthRes = evaluation.get(device).calculate();
//                results.put(device, azimuthRes);
//            } catch (InsufficientInputException e) {
//                e.printStackTrace();
//            }
//        }
//        resultFragment.updateResults(results);
    }

    public void restartMeasure(View view) {
        measureFragment = MeasureFragment.getInstance(devices.size());
        //activate the measure fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        currentFragment = measureFragment;
        fragmentTransaction.replace(R.id.main_container, currentFragment);
        fragmentTransaction.commit();
        //as long as the app is measuring, the screen should not turn off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startMeasurement();
    }
}
