package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.MovingAverageEvaluation;
import edu.kit.teco.radarbeacon.settings.SettingsFragment;

public class ConnectedMainActivity extends MainBaseActivity implements ConnectionManager.ConnectionListener {

    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        connectionManager = ConnectionManager.getInstance(this);
        connectionManager.setDevices(devices);
        connectionManager.setConnectionListener(this);

        evaluation = new HashMap<>();
        //create a new evaluation instance for each trackable device
        for (BluetoothDevice device : devices) {
            evaluation.put(device, new MovingAverageEvaluation());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionManager.connect();

        SharedPreferences preferences = getSharedPreferences(SettingsFragment
                .PREF_TUT_MEASURE, 0);
        boolean showTutorial = preferences.getBoolean(SettingsFragment.PREF_TUT_MEASURE, true);
        Log.d("iris", "boolean" + showTutorial);

        //if all devices are already connecting, start measuring
        if (connectionManager.getConnectedDevices().size() == devices.size() && !showTutorial) {
            startMeasurement();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMeasurement();
        connectionManager.disconnect();
    }

    @Override
    protected void startMeasurement() {
        connectionManager.startMeasurement();
        Log.d("iris", "start measurement");
    }

    @Override
    protected void stopMeasurement() {
        connectionManager.stopMeasurement();
        Log.d("iris", "stop measurement");
    }

    @Override
    public void onRssiResult(final BluetoothDevice device, final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //add the value for evaluation
                addSample(device, rssi);
                //add for measure fragment
                measureFragment.addSample(getAzimuth(), rssi);
            }
        });
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        if (currentFragment == resultFragment) {
            resultFragment.onDeviceConnected(device);
        }
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        if (currentFragment == resultFragment) {
            resultFragment.onDeviceDisconnected(device);
        }
    }

    @Override
    public void onAllDevicesConnected() {
        connectionManager.startMeasurement();
    }
}
