package edu.kit.teco.radarbeacon.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Iris on 20.07.2015.
 */
public class MovingAverageEvaluation implements EvaluationStrategy {

    /*decides how many values should be averaged each time, e.g. 0.5 means with 20 values that
    each smoothed point averages 10 original points.*/
    private final double rangeFactor;
    private CircularArrayList<Sample> samples;
    private Comparator<AvgSample> compAzimuth;
    private Comparator<AvgSample> compRssi;

    public MovingAverageEvaluation() {
        this(0.3);
    }

    public MovingAverageEvaluation(double rangeFactor) {
        this.rangeFactor = rangeFactor;

        this.samples = new CircularArrayList<>();

        compAzimuth = new Comparator<AvgSample>() {
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
        compRssi = new Comparator<AvgSample>() {
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
        samples.add(s);
    }

    @Override
    public void addSample(double azimuth, int rssi, long time) {
        addSample(new Sample(azimuth, rssi, time));
    }

    @Override
    public double calculate() {

        if (samples.size() == 0) {
            return 42;//lolz
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

        for (int i = 0; i < samples.size(); i++) {
            double sum = 0;
            for (int j = i - range / 2; j < i - range / 2 + range; j++) {
                sum += samples.get(j).getRssi();
            }
            double avg = sum / range;

            smoothValues.add(i, new AvgSample(avg, samples.get(i).getAzimuth(), samples.get(i).getTime()));
        }

        //get median of first x maximums
        ArrayList<AvgSample> maximums = new ArrayList<>();
        //get first x maximums
        for (int i = 0; i < range; i++) {
            AvgSample max = Collections.max(smoothValues, compRssi);
            maximums.add(max);
            smoothValues.remove(max);
        }
        //get median
        //sort again
        Collections.sort(maximums, compAzimuth);
        double median;
        if (maximums.size() % 2 == 0) {
            median = (maximums.get(maximums.size() / 2 - 1).azimuth + maximums.get(maximums.size() /
                    2)
                    .azimuth) / 2;
        } else {
            median = maximums.get(maximums.size() / 2).azimuth;
        }

        return median;
    }

    class AvgSample {
        double azimuth;
        double avgRssi;
        long time;

        public AvgSample(double avgRssi, double azimuth, long time) {
            this.avgRssi = avgRssi;
            this.azimuth = azimuth;
            this.time = time;
        }
    }
}
