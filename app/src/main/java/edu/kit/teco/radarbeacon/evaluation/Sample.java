package edu.kit.teco.radarbeacon.evaluation;

/**
 * Created by Iris on 19.07.2015.
 */
public class Sample {

    private long time;
    private double azimuth;
    private int rssi;

    public Sample(double azimuth, int rssi, long time) {
        this.azimuth = azimuth;
        this.rssi = rssi;
        this.time = time;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public int getRssi() {
        return rssi;
    }

    public long getTime() {
        return time;
    }
}
