package edu.kit.teco.radarbeacon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;

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

    private ArrayList<BluetoothGatt> gatts;
    private Handler readRssiHandler;


    //connection counter
    private int connectedDevices;

    private Dialog connectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

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
        connectedDevices = 0;
        for (BluetoothDevice device : devices) {
            gatts.add(device.connectGatt(this, true, gattCallback));
        }

        showConnectingDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopMeasurement();

        //close all connections
        for (BluetoothGatt gatt : gatts) {
            gatt.close();
        }
        gatts.clear();
    }

    @Override
    protected void startMeasurement() {

        gatts.get(0).readRemoteRssi();
        //skip after a certain delay time if there was no result, and scan the next device
        Runnable skipRunnable = new SkipRunnable(gatts.get(0));
        readRssiHandler.postDelayed(skipRunnable, INTERRUPT_DELAY);
    }

    @Override
    protected void stopMeasurement() {
        //remove all runnables, so it doesnt restart scanning or stuff
        readRssiHandler.removeCallbacksAndMessages(null);
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

    private void showConnectingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_connect, null));
        connectingDialog = builder.create();
        connectingDialog.setCanceledOnTouchOutside(false);
        // handle back button
        connectingDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    connectingDialog.dismiss();
                    Intent intent = new Intent(ConnectedMainActivity.this,
                            StartMenuActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });

        connectingDialog.show();
    }

    private void hideConnectionDialog() {
        if (connectingDialog != null) {
            connectingDialog.hide();
        }
    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        // Connection state changed.
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("IRIS", "on connection state changed: status = " + status + " newState = " + newState);

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                connectedDevices++;
                //start scan if all devices connected successfully
                if (connectedDevices == devices.size()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideConnectionDialog();
                        }
                    });
                    startMeasurement();
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectedDevices--;
            }

        }

        // New RSSI received.
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            //mainly removes the skipRunnable
            readRssiHandler.removeCallbacksAndMessages(null);

            if (currentFragment != measureFragment) {
                //obviously this call somehow sneaked through even though it shouldnt, so we can
                // skip right away
                return;
            }

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
            Runnable skipRunnable = new SkipRunnable(nextGatt);
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

    /**
     * A runnable responsible for starting a rssi scan for the next device in the queue, in case
     * the current scan doesnt return after a certain time.
     */
    class SkipRunnable implements Runnable {

        //the gatt that is running the scan, which should be interrupted in case of no response
        private BluetoothGatt gatt;

        SkipRunnable(BluetoothGatt gatt) {
            this.gatt = gatt;
        }

        @Override
        public void run() {
            //Start next device in queue
            BluetoothGatt nextGatt = getNextGatt(gatt);
            nextGatt.readRemoteRssi();
            //also add a skip runnable for the next scan, in case it doesnt answer too
            SkipRunnable nextSkip = new SkipRunnable(nextGatt);
            readRssiHandler.postDelayed(nextSkip, INTERRUPT_DELAY);
        }
    }
}
