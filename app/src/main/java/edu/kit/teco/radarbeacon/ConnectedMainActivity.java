package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.EvaluationStrategy;
import edu.kit.teco.radarbeacon.evaluation.MovingAverageEvaluation;

public class ConnectedMainActivity extends MainBaseActivity {

    /**
     * The time in ms after which the rssi scan is repeated if ONE device is scanned. This gets
     * scaled down when more than one device should be scanned.
     */
    public static final int RSSI_DELAY = 50;
    /**
     * Time until a rssi scan is interrupted and the next device is scanned.
     */
    public static final int INTERRUPT_DELAY = 100;

    private ArrayList<BluetoothDevice> devices;
    private ArrayList<BluetoothGatt> gatts;
    private Handler readRssiHandler;
    private HashMap<BluetoothDevice, EvaluationStrategy> evaluation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        devices = (ArrayList<BluetoothDevice>) getIntent().getSerializableExtra(StartMenuActivity.EXTRA_DEVICES);
        gatts = new ArrayList<>();
        readRssiHandler = new Handler();

        evaluation = new HashMap<>();
        //create a new evaluation instance for each trackable device
        for (BluetoothDevice device : devices) {
            evaluation.put(device, new MovingAverageEvaluation());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //clear gatts to be sure and start connecting to every passed device
        gatts.clear();
        for (BluetoothDevice device : devices) {
            gatts.add(device.connectGatt(this, true, gattCallback));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //remove all runnables, so it doesnt restart scanning or stuff
        readRssiHandler.removeCallbacksAndMessages(null);

        //close all connections
        for (BluetoothGatt gatt : gatts) {
            gatt.close();
        }
        gatts.clear();
    }

    private void startScan() {

        //TODO this will fail hard when both first calls fail
        gatts.get(0).readRemoteRssi();
        //skip after a certain delay time if there was no result, and scan the next device
        Runnable skipRunnable = new Runnable() {
            @Override
            public void run() {
                //Start next device in queue
                BluetoothGatt sndnextGatt = getNextGatt(gatts.get(0));
                sndnextGatt.readRemoteRssi();
            }
        };
        readRssiHandler.postDelayed(skipRunnable, INTERRUPT_DELAY);
    }

    private void stopScan() {
        readRssiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onMeasureComplete() {
        stopScan();

        super.onMeasureComplete();

        //push results to fragment
        HashMap<BluetoothDevice, Float> results = new HashMap<>();
        for (BluetoothDevice device : evaluation.keySet()) {
            double azimuthRes = evaluation.get(device).calculate();
            results.put(device, (float) azimuthRes);
        }
        resultFragment.updateResults(results);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connected_main, menu);
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

    private BluetoothGatt getNextGatt(BluetoothGatt gatt) {
        int current = gatts.indexOf(gatt);
        final int next;
        if (current + 1 < gatts.size()) {
            next = current + 1;
        } else {
            next = 0;//start first if end of queue
        }
        return gatts.get(next);
    }

    private void addSample(BluetoothDevice device, int rssi) {
        EvaluationStrategy ev = evaluation.get(device);
        float azimuth = getAzimuth();
        long time = SystemClock.uptimeMillis();

        ev.addSample(azimuth, rssi, time);
    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        // Connection state changed.
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.readRemoteRssi();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            }

        }

        // New RSSI received.
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            //mainly removes the skipRunnable
            readRssiHandler.removeCallbacksAndMessages(null);

            BluetoothDevice device = gatt.getDevice();

            //The activity polls rssi values in a circular way from all passed devices. Once one
            // value is received, the scan for the next device is started.
            final BluetoothGatt nextGatt = getNextGatt(gatt);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    nextGatt.readRemoteRssi();
                }
            };
            readRssiHandler.postDelayed(runnable, RSSI_DELAY / devices.size());

            //skip after a certain delay time if there was no result, and scan the next device
            Runnable skipRunnable = new Runnable() {
                @Override
                public void run() {
                    //Start next device in queue
                    BluetoothGatt sndnextGatt = getNextGatt(nextGatt);
                    sndnextGatt.readRemoteRssi();
                }
            };
            readRssiHandler.postDelayed(skipRunnable, INTERRUPT_DELAY);

            //for some mysterious reasons, the rssi scan sometimes returns 127, which is not a
            // useful result; so skip that
            if (rssi == 127) {
                return;
            }
            //add the value for evaluation
            addSample(device, rssi);

            measureFragment.addSample(getAzimuth(), rssi);
        }

    };
}
