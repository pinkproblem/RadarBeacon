package edu.kit.teco.radarbeacon;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
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
import edu.kit.teco.radarbeacon.settings.SettingsActivity;
import edu.kit.teco.radarbeacon.settings.SettingsFragment;

public abstract class MainBaseActivity extends AppCompatActivity implements RotationChangeListener,
        MeasureFragment.OnMeasureCompleteListener,
        TutorialDialog.OnDismissListener {

    private static final int azimuthBufferSize = 10;
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
        fragmentTransaction.replace(R.id.main_container, measureFragment);
        fragmentTransaction.commit();
        currentFragment = measureFragment;
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment == resultFragment) {
            currentFragment = resultFragment;

            //push results to fragment
//            HashMap<BluetoothDevice, EvaluationStrategy> results = new HashMap<>();
//            for (BluetoothDevice device : evaluation.keySet()) {
//                results.put(device, evaluation.get(device));
//            }
            resultFragment.updateResults(evaluation);
        } else if (fragment == measureFragment) {
            currentFragment = measureFragment;

            SharedPreferences preferences = getSharedPreferences(SettingsFragment
                    .PREF_TUT_MEASURE, 0);
            boolean showTutorial = preferences.getBoolean(SettingsFragment.PREF_TUT_MEASURE, true);

            if (showTutorial) {
                stopMeasurement();
                CharSequence text1 = getText(R.string.tutorial_measure_1);
                String text2 = getString(R.string.tutorial_measure_2);
                String text3 = getString(R.string.tutorial_measure_3);
                String text4 = getString(R.string.tutorial_measure_4);

                SpannableString ss2 = TutorialDialog.createIndentedText(text2, 50, 65);
                SpannableString ss3 = TutorialDialog.createIndentedText(text3, 50, 65);
                SpannableString ss4 = TutorialDialog.createIndentedText(text4, 50, 65);

                SpannableStringBuilder text = new SpannableStringBuilder();
                text.append(text1).append(ss2).append(ss3).append(ss4);

                String button = getString(R.string.go);
                String title = getString(R.string.measurement);
                DialogFragment dialog = TutorialDialog.getInstance(SettingsFragment
                                .PREF_TUT_MEASURE, text, button,
                        title);
                dialog.show(getFragmentManager(), "measure_tutorial");
            } else {
                startMeasurement();
            }
        }
    }

    @Override
    public void onDismissTutorial() {
        startMeasurement();
    }

    @Override
    public void onAzimuthChange(float newAzimuth) {
        azimuth = newAzimuth;

        smoothAzimuth = calculateSmoothAzimuth(newAzimuth);

        if (measureFragment != null) {
            measureFragment.rotateView(360 - (float) CircleUtils.radiansToDegree(smoothAzimuth));
        }
        if (currentFragment == resultFragment) {
            resultFragment.onSmoothAzimuthChange(smoothAzimuth);
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
        //currentFragment = resultFragment;
        fragmentTransaction.replace(R.id.main_container, resultFragment);
        fragmentTransaction.commit();

        //dont forget to release the wakelock
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    public void restartMeasure(View view) {
        Log.d("iris", "restart measurement");
        measureFragment = MeasureFragment.getInstance(devices.size());
        //activate the measure fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_container, measureFragment);
        fragmentTransaction.commit();
        //as long as the app is measuring, the screen should not turn off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startMeasurement();
    }
}
