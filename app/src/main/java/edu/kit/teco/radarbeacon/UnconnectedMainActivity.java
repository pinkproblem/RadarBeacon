package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.MovingAverageEvaluation;

public class UnconnectedMainActivity extends MainBaseActivity {

    /**
     * The rssi scan is restarted periodically after this interval.
     * That's because some devices only trigger the callback of the scan on the first received
     * package of a certain beacon, and drop all others.
     */
    public static final int RESTART_INTERVAL = 1200;

    private Handler scanHandler;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_unconnected_main);

        scanHandler = new Handler();
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);
        bluetoothAdapter = btManager.getAdapter();

        evaluation = new HashMap<>();
        //create a new evaluation instance for each trackable device
        for (BluetoothDevice device : devices) {
            evaluation.put(device, new MovingAverageEvaluation());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences(TutorialDialog
                .PREF_TUT_MEASURE, 0);
        boolean showTutorial = preferences.getBoolean(TutorialDialog.PREF_TUT_MEASURE, true);

        if (!showTutorial) {
            startMeasurement();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopMeasurement();
    }

    @Override
    protected void startMeasurement() {
        //start le scan
        scanHandler.post(scanRunnable);
    }

    @Override
    protected void stopMeasurement() {
        bluetoothAdapter.stopLeScan(leScanCallback);
        scanHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unconnected_main, menu);
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

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (rssi > 0) {
                return;
            }

            addSample(device, rssi);
            if (currentFragment == measureFragment) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        measureFragment.addSample(getAzimuth(), rssi);
                    }
                });
            }
        }
    };

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            //yes, this is deprecated, but the replacement is API lvl 21 or so, so above my min sdk
            bluetoothAdapter.stopLeScan(leScanCallback);
            bluetoothAdapter.startLeScan(leScanCallback);
            scanHandler.postDelayed(scanRunnable, RESTART_INTERVAL);
        }
    };
}
