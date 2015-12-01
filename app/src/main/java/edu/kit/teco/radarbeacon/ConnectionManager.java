package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Created by Iris on 01.12.2015.
 */
public class ConnectionManager {

    /**
     * The time in ms after which the rssi scan is repeated if ONE device is scanned. This gets
     * scaled down when more than one device should be scanned.
     */
    public static final int RSSI_DELAY = 50;
    /**
     * Time until a rssi scan is interrupted and the next device is scanned.
     */
    public static final int INTERRUPT_DELAY = 100;

    private static ConnectionManager instance;

    private Context context;

    private ArrayList<BluetoothDevice> devices;
    private ArrayList<BluetoothDevice> connectedDevices;
    private ArrayList<BluetoothGatt> gatts;
    private Handler readRssiHandler;

    private ConnectionListener listener;

    private boolean connectionRunning;
    private boolean measurementRunning;

    private ConnectionManager(Context context) {
        connectedDevices = new ArrayList<>();
        gatts = new ArrayList<>();
        readRssiHandler = new Handler(context.getMainLooper());
    }

    public static synchronized ConnectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionManager(context);
        }
        instance.context = context;
        return instance;
    }

    public void setDevices(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
    }

    public ArrayList<BluetoothDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.listener = listener;
    }

    public void removeConnectionListener() {
        listener = null;
    }

    public void connect() {
        if (connectionRunning) {
            return;
        }
        connectionRunning = true;
        //clear gatts to be sure and start connecting to every passed device
        gatts.clear();
        connectedDevices.clear();
        for (BluetoothDevice device : devices) {
            gatts.add(device.connectGatt(context, true, gattCallback));
        }
    }

    public void disconnect() {
        connectionRunning = false;
        readRssiHandler.removeCallbacksAndMessages(null);
        //close all connections
        for (BluetoothGatt gatt : gatts) {
            gatt.close();
        }
        gatts.clear();
        connectedDevices.clear();
    }

    public void startMeasurement() {
        if (measurementRunning) {
            return;
        }
        measurementRunning = true;

        gatts.get(0).readRemoteRssi();
        //skip after a certain delay time if there was no result, and scan the next device
        Runnable skipRunnable = new SkipRunnable(gatts.get(0));
        readRssiHandler.postDelayed(skipRunnable, INTERRUPT_DELAY);
    }

    public void stopMeasurement() {
        measurementRunning = false;
        //remove all runnables, so it doesnt restart scanning or stuff
        readRssiHandler.removeCallbacksAndMessages(null);
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

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        // Connection state changed.
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                connectedDevices.add(gatt.getDevice());
                if (listener != null) {
                    listener.onDeviceConnected(gatt.getDevice());
                }
                //start scan if all devices connected successfully
                if (connectedDevices.size() == devices.size() && listener != null) {
                    listener.onAllDevicesConnected();
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectedDevices.remove(gatt.getDevice());
                if (listener != null) {
                    listener.onDeviceDisconnected(gatt.getDevice());
                }
                if (connectedDevices.size() == 0) {
                    stopMeasurement();
                }
            }

        }

        // New RSSI received.
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (!connectionRunning || !measurementRunning) {
                return;
            }

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
            Runnable skipRunnable = new SkipRunnable(nextGatt);
            readRssiHandler.postDelayed(skipRunnable, INTERRUPT_DELAY);

            //for some mysterious reasons, the rssi scan sometimes returns 127, which is not a
            // useful result; so skip that
            if (rssi == 127) {
                return;
            }
            //add the value for evaluation
            if (listener != null) {
                listener.onRssiResult(device, rssi);
            }
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

    public interface ConnectionListener {
        public void onRssiResult(BluetoothDevice device, int rssi);

        public void onDeviceConnected(BluetoothDevice device);

        public void onDeviceDisconnected(BluetoothDevice device);

        public void onAllDevicesConnected();
    }
}
