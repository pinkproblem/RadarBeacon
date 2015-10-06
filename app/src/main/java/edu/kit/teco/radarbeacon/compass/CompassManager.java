package edu.kit.teco.radarbeacon.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Created by Iris on 17.08.2015.
 */
public class CompassManager implements SensorEventListener {

    private Context context;

    //    private float rotation;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    //raw sensor value buffers
    private float[] gravity;
    private float[] magnetic;

    //listeners for rotation change events
    private ArrayList<RotationChangeListener> listeners;

    public CompassManager(Context context) {
        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        listeners = new ArrayList<>();
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magnetic = event.values;
        //calculate orientation from both sensors
        if (gravity != null && magnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, magnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                //extract azimuth
                float newAzimuth = orientation[0];
                notifyListeners(newAzimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //nothing needed here
    }

    public void registerRotationListener(RotationChangeListener listener) {
        listeners.add(listener);
    }

    public void unregisterRotationListener(RotationChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(float newAzimuth) {
        for (RotationChangeListener lst : listeners) {
            lst.onAzimuthChange(newAzimuth);
        }
    }
}
