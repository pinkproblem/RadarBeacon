package edu.kit.teco.radarbeacon.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Math.PI;

/**
 * Evaluation strategy that uses the moving average method to smooth the discrete set of samples.
 * Every original point gets replaced by a smoothed point, whose value is the average of all
 * points within a certain window around the original point. Also, the maximum value is
 * determined by taking the medium of the first x highest values.
 * <p/>
 * All samples are treated in a circular way, means the first value is the successor of the last
 * one and so on.
 */
public class MovingAverageEvaluation implements EvaluationStrategy {

    /*decides how many values should be averaged each time, e.g. 0.5 means with 20 values that
    each smoothed point averages 10 original points.*/
    private final double rangeFactor;
    private CircularArrayList<Sample> samples;
    private Comparator<AvgSample> compareByAzimuth;
    private Comparator<AvgSample> compareByRssi;

    public MovingAverageEvaluation() {
        this(0.3);
    }

    public MovingAverageEvaluation(double rangeFactor) {
        this.rangeFactor = rangeFactor;

        this.samples = new CircularArrayList<>();

        compareByAzimuth = new Comparator<AvgSample>() {
            @Override
            public int compare(AvgSample lhs, AvgSample rhs) {
                if (lhs.azimuth < rhs.azimuth) {
                    return -1;
                } else if (lhs.azimuth > rhs.azimuth) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        compareByRssi = new Comparator<AvgSample>() {
            @Override
            public int compare(AvgSample lhs, AvgSample rhs) {
                if (lhs.avgRssi < rhs.avgRssi) {
                    return -1;
                } else if (lhs.avgRssi > rhs.avgRssi) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }

    @Override
    public void addSample(Sample s) {
        if (s.getAzimuth() < -PI || s.getAzimuth() > PI) {
            throw new IllegalArgumentException();
        }
        if (s.getRssi() > 0) {
            throw new IllegalArgumentException();
        }
        if (s.getTime() < 0) {
            throw new IllegalArgumentException();
        }
        samples.add(s);
    }

    @Override
    public void addSample(float azimuth, int rssi, long time) {
        addSample(new Sample(azimuth, rssi, time));
    }

    @Override
    public float calculate() throws InsufficientInputException {

        //not enough values
        if (samples.size() == 0) {
            throw new InsufficientInputException();
        }

        //sort by azimuth
        Collections.sort(samples, new Comparator<Sample>() {
            @Override
            public int compare(Sample lhs, Sample rhs) {
                if (lhs.getAzimuth() < rhs.getAzimuth()) {
                    return -1;
                } else if (lhs.getAzimuth() > rhs.getAzimuth()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        //smooth with moving average method
        ArrayList<AvgSample> smoothValues = new ArrayList<>();
        //calculate range
        int range = (int) Math.floor(samples.size() * rangeFactor);
        if (range == 0) {
            //avoids division by 0
            range = 1;
        }

        //calculate the average for each value, starting at about range/2 before, ending about
        // range/2 after it
        for (int i = 0; i < samples.size(); i++) {
            float sum = 0;
            for (int j = i - range / 2; j < i - range / 2 + range; j++) {
                sum += samples.get(j).getRssi();
            }
            float avg = sum / range;

            smoothValues.add(i, new AvgSample(avg, samples.get(i).getAzimuth(), samples.get(i).getTime()));
        }

        //get median of first x maximums
        ArrayList<AvgSample> maximums = new ArrayList<>();
        //get first x maximums
        for (int i = 0; i < range; i++) {
            AvgSample max = Collections.max(smoothValues, compareByRssi);
            maximums.add(max);
            smoothValues.remove(max);
        }
        //get median
        //sort again
        Collections.sort(maximums, compareByAzimuth);
        float median;
        if (maximums.size() % 2 == 0) {
            median = (maximums.get(maximums.size() / 2 - 1).azimuth + maximums.get(maximums.size() /
                    2)
                    .azimuth) / 2;
        } else {
            median = maximums.get(maximums.size() / 2).azimuth;
        }

        return median;
    }

    @Override
    public double getDistance() {
        //simple solution: since false high rssi values are unlikely, and the calculation is more
        // exact with higher rssi values, we just take the maximum rssi and transform it to the
        // distance

        //to make it more dynamic, lets say the maximum of the last x values
        int maxRssi = Integer.MIN_VALUE;
        final int lookupWidth = 15;
        for (int i = samples.size() - lookupWidth; i < samples.size(); i++) {
            Sample s = samples.get(i);
            int rssi = s.getRssi();
            if (rssi > maxRssi) {
                maxRssi = rssi;
            }
        }

        return rssiToDistance(maxRssi);
    }

    //aus dem praktikum
    private static final double A = -66.72D; // in dbm
    private static final double K = Math.pow(10D, A / 20D); // in mW

    private static double rssiToDistance(int rssi) {
        return (K / Math.pow(10D, rssi / 20D)); // dBm to mW distance is inversely proportional to power
    }

    /**
     * Same as a normal sample, but with a float rssi, so it can be used for samples that use a
     * combined (e.g. mean) value for rssi.
     */
    class AvgSample {
        float azimuth;
        float avgRssi;
        long time;

        public AvgSample(float avgRssi, float azimuth, long time) {
            this.avgRssi = avgRssi;
            this.azimuth = azimuth;
            this.time = time;
        }
    }
}
