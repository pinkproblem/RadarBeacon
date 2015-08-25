package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class StartMenuActivity extends AppCompatActivity {

    //name of the intent extra of the passed devices
    public static final String EXTRA_DEVICES = "edu.kit.teco.radarbeacon.extra_devices";

    //duration of a le scan in ms
    private static final int SCAN_PERIOD = 3000;
    //duration of the pause between scans in ms
    private static final int SCAN_PAUSE = 5000;

    private BluetoothAdapter btAdapter;
    private Handler scanHandler;
    //all found devices
    private ArrayList<BluetoothDevice> devices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);

        scanHandler = new Handler();
        devices = new ArrayList<>();

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();

        scanHandler.post(startScanRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //remove all actions
        scanHandler.removeCallbacksAndMessages(null);
        //stop le scan
        btAdapter.stopLeScan(scanCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_menu, menu);
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

    public void startConnected(View view) {
        if (!btAdapter.isEnabled()) {
            showEnableBluetoothRequest();
        }

        final ArrayList<BluetoothDevice> selectedDevices = new ArrayList<>();

        //show a new device selection dialog; when it returns save selected devices and pass them
        // to new activity
        SelectDeviceDialog dialog = SelectDeviceDialog.getInstance(new SelectDeviceDialog.OnConfirmSelectionListener() {
            @Override
            public void onConfirmSelection(ArrayList<BluetoothDevice> selection) {
                selectedDevices.addAll(selection);
                Intent intent = new Intent(StartMenuActivity.this, ConnectedMainActivity.class);
                //add selected devices
                intent.putExtra(EXTRA_DEVICES, selectedDevices);
                startActivity(intent);
            }
        }, devices);

        dialog.show(getFragmentManager(), "selectdevice");

    }

    public void startUnconnected(View view) {
        if (!btAdapter.isEnabled()) {
            showEnableBluetoothRequest();
        }
    }

    private void showEnableBluetoothRequest() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //TODO maybe restart scan start
        startActivityForResult(enableBtIntent, 0);
    }

    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!devices.contains(device)) {
                devices.add(device);
            }
        }
    };

    Runnable startScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (btAdapter != null && btAdapter.isEnabled()) {
                btAdapter.startLeScan(scanCallback);
            }
            scanHandler.postDelayed(stopScanRunnable, SCAN_PERIOD);
        }
    };

    Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (btAdapter != null) {
                btAdapter.stopLeScan(scanCallback);
            }
            scanHandler.postDelayed(startScanRunnable, SCAN_PAUSE);
        }
    };
}
