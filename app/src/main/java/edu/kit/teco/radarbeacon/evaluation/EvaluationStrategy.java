package edu.kit.teco.radarbeacon.evaluation;

/**
 * Created by Iris on 19.07.2015.
 */
public interface EvaluationStrategy {

    public void addSample(Sample s);

    public void addSample(double azimuth, int rssi, long time);

    public double calculate();
}
