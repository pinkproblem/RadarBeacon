package edu.kit.teco.radarbeacon.evaluation;

/**
 * Strategy to calculate a directional result our of samples.
 */
public interface EvaluationStrategy {

    /**
     * Add a sample to the already existing set.
     *
     * @param s the sample
     * @throws IllegalArgumentException @see addSample(double azimuth, int rssi, long time)
     */
    public void addSample(Sample s);

    /**
     * Add a sample to the already existing set.
     *
     * @param azimuth rotation angle in rad; must be between -Pi and Pi
     * @param rssi    Received signal strength indicator; must be <=0
     * @param time    time stamp of receival; must be >= 0
     * @throws IllegalArgumentException if the conditions above aren't met
     */
    public void addSample(float azimuth, int rssi, long time);

    /**
     * Calculates the resulting azimuth value out of all added samples.
     *
     * @throws InsufficientInputException if there were not enough values to calculate, or
     *                                    something else went really really wrong
     */
    public float calculate() throws InsufficientInputException;

    /**
     * Returns an estimated distance to the remote device based on collected sample information.
     *
     * @return distance to remote device
     */
    public double getDistance();

    /**
     * Returns a smooth distance value to the remote device based on the last few values recorded
     * . Is not exact but pretty to show and lokks stable.
     */
    double getSmoothDistance();
}
