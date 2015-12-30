package edu.kit.teco.radarbeacon;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.kit.teco.radarbeacon.settings.SettingsActivity;

public class StartMenuActivity extends AppCompatActivity implements SelectDeviceDialog.OnConfirmSelectionListener {

    //name of the intent extra of the passed devices
    public static final String EXTRA_DEVICES = "edu.kit.teco.radarbeacon.extra_devices";

    //duration of a le scan in ms
    private static final int SCAN_PERIOD = 3000;
    //duration of the pause between scans in ms
    private static final int SCAN_PAUSE = 5000;
    //time until a device is deleted if no new advertisement
    private static final int DEVICE_TIMEOUT = 16000;
    //time until a connection attempt is interrupted for taking to long
    private static final int CONNECT_TIMEOUT = 10000;

    private BluetoothAdapter btAdapter;
    private Handler scanHandler;
    //all found devices and the time of the newest advertisement
    private ArrayList<BluetoothDevice> devices;
    private HashMap<BluetoothDevice, Long> deviceTimeStamp;

    private SelectDeviceDialog selectDialog;
    private Button startButton;
    private Button connectingButton;
    private CheckBox energyCheckbox;
    private ProgressBar connectionProgress;
    private RelativeLayout layout;

    private ConnectionManager connectionManager;
    private Handler connectionTimeoutHandler;
    private boolean intentTriggered; //whether onStop came from my intent or pressing the home
    // button or something


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);

        scanHandler = new Handler();
        devices = new ArrayList<>();
        deviceTimeStamp = new HashMap<>();

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        //ui stuff
        startButton = (Button) findViewById(R.id.button);
        connectingButton = (Button) findViewById(R.id.button_connecting);
        energyCheckbox = (CheckBox) findViewById(R.id.checkbox_save_energy);
        connectionProgress = (ProgressBar) findViewById(R.id.connection_progress);
        layout = (RelativeLayout) findViewById(R.id.start_container);

        connectingButton.setVisibility(View.INVISIBLE);
        connectionProgress.setVisibility(View.INVISIBLE);

        connectionManager = ConnectionManager.getInstance(this);
        connectionTimeoutHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        connectingButton.setVisibility(View.INVISIBLE);
        connectionProgress.setVisibility(View.INVISIBLE);

        scanHandler.post(startScanRunnable);
        scanHandler.post(clearRunnable);

        intentTriggered = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        //remove all actions
        scanHandler.removeCallbacksAndMessages(null);
        //stop le scan
        btAdapter.stopLeScan(scanCallback);

        //stop connection if stop did not come from my intent
        if (!intentTriggered) {
            connectionManager.disconnect();
        }
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showVariantInfo(View view) {
        DialogFragment infoDialog = TextDialog.getInstance(getString(R.string.variant_info));
        infoDialog.show(getFragmentManager(), "variant_info");
    }

    public void startScan(View view) {
        if (!btAdapter.isEnabled()) {
            showEnableBluetoothRequest();
            return;
        }

        //show a new device selection dialog; when it returns save selected devices and pass them
        // to new activity
        selectDialog = SelectDeviceDialog.getInstance(devices);
        selectDialog.show(getFragmentManager(), "selectdevice");

    }

    public void cancelConnection(View view) {
        connectionManager.disconnect();
        connectingButton.setVisibility(View.INVISIBLE);
        connectionProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConfirmSelection(final ArrayList<BluetoothDevice> selection) {

        if (!energyCheckbox.isChecked()) {
            connectionManager.setDevices(selection);
            connectionManager.setConnectionListener(new ConnectionManager.ConnectionListener() {
                @Override
                public void onRssiResult(BluetoothDevice device, int rssi) {

                }

                @Override
                public void onDeviceConnected(BluetoothDevice device) {

                }

                @Override
                public void onDeviceDisconnected(BluetoothDevice device) {

                }

                @Override
                public void onAllDevicesConnected() {
                    connectionTimeoutHandler.removeCallbacksAndMessages(null);

                    Intent intent = new Intent(StartMenuActivity.this, ConnectedMainActivity.class);
                    //add selected devices
                    intent.putExtra(EXTRA_DEVICES, selection);
                    intentTriggered = true;
                    startActivity(intent);
                }
            });
            connectingButton.setVisibility(View.VISIBLE);
            connectionProgress.setVisibility(View.VISIBLE);
            connectionManager.connect();
            connectionTimeoutHandler.postDelayed(interruptConnectRunnable, CONNECT_TIMEOUT);
        } else {
            Intent intent = new Intent(StartMenuActivity.this, UnconnectedMainActivity.class);
            //add selected devices
            intent.putExtra(EXTRA_DEVICES, selection);
            startActivity(intent);
        }
    }

    private void showEnableBluetoothRequest() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //TODO maybe restart scan start
        startActivityForResult(enableBtIntent, 0);
    }

    private void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!devices.contains(device)) {
            devices.add(device);
            if (selectDialog != null) {
                selectDialog.update(devices);
            }
        }
        deviceTimeStamp.put(device, SystemClock.uptimeMillis());
    }

    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[]
                scanRecord) {
            //simply run all from main thread, because else android has weird problems
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StartMenuActivity.this.onLeScan(device, rssi, scanRecord);
                }
            });
        }
    };

    Runnable startScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (btAdapter != null && btAdapter.isEnabled()) {
                btAdapter.startLeScan(scanCallback);
            }
            if (selectDialog != null) {
                selectDialog.setLoadingIcon(true);
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
            if (selectDialog != null) {
                selectDialog.setLoadingIcon(false);
            }
            scanHandler.postDelayed(startScanRunnable, SCAN_PAUSE);
        }
    };

    //runnable that periodically deletes devices which have not sent an advertising for a long time
    Runnable clearRunnable = new Runnable() {
        @Override
        public void run() {
            Iterator<BluetoothDevice> iter = deviceTimeStamp.keySet().iterator();
            BluetoothDevice device;
            while (iter.hasNext()) {
                device = iter.next();
                if (SystemClock.uptimeMillis() - deviceTimeStamp.get(device) > DEVICE_TIMEOUT) {
                    devices.remove(device);
                    iter.remove();
                }
            }
            if (selectDialog != null) {
                selectDialog.update(devices);
            }
            scanHandler.postDelayed(clearRunnable, DEVICE_TIMEOUT);
        }
    };

    Runnable interruptConnectRunnable = new Runnable() {
        @Override
        public void run() {
            connectionManager.disconnect();
            connectingButton.setVisibility(View.INVISIBLE);
            connectionProgress.setVisibility(View.INVISIBLE);
            DialogFragment dialog = TextDialog.getInstance(getString(R.string.connect_timeout));
            dialog.show(getFragmentManager(), "interrupt_connect");
        }
    };
}
