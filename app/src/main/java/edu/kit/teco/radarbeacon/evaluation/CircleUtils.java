package edu.kit.teco.radarbeacon.evaluation;

import java.util.Collections;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by Iris on 11.08.2015.
 * <p/>
 * Provides static methods to calculate basic operations in the context of a circle, such as
 * distance, average etc.
 * <p/>
 * All methods use a range from minus pi to pi, basically that's the range android uses for
 * magnetic field indication.
 */
public class CircleUtils {

    public static double radiansToDegree(double angle) {
        return (angle + PI) * 180 / PI;
    }

    /**
     * Returns the angle of the point that is opposing/adjacent to the point given.
     */
    public static double getOpposing(double angle) {
        if (angle < 0) {
            return angle + PI;
        } else {
            return angle - PI;
        }
    }

    /**
     * Returns the absolute distance along the circle between the given points; e.g. the distance
     * between 350° and 10° would be 20° (example in degrees for better understanding, values
     * are still in radians).
     */
    static double getCircleDistance(double firstAngle, double secondAngle) {
        return min(abs(firstAngle - secondAngle), 2 * PI - abs(firstAngle - secondAngle));
    }

    /**
     * Averages two angles; e.g. the average of 350° and 10° would be 0° (example in degrees for
     * better understanding, values are still in radians).
     */
    public static double getMeanAngle(double firstAngle, double secondAngle) {
        //averaging over the border of pi/minus pi:
        if (abs(firstAngle - secondAngle) > Math.PI) {
            //average and project to opposite
            return getOpposing(firstAngle + secondAngle) / 2;
        }
        return (firstAngle + secondAngle) / 2;
    }

    /**
     * Averages two or more angles; e.g. the average of 350° and 10° would be 0° (example in degrees
     * for better understanding, values are still in radians).
     * Reference: Jerrold H. Zar: Biostatistical Analysis
     */
    public static double getMeanAngle(List<Double> angles) {
        double X;
        double Y;
        double r;

        double cosSum = 0;
        for (Double a : angles) {
            cosSum += cos(a);
        }
        X = cosSum / angles.size();

        double sinSum = 0;
        for (Double a : angles) {
            sinSum += sin(a);
        }
        Y = sinSum / angles.size();

        r = sqrt(X * X + Y * Y);

        return acos(X / r);
    }

    /**
     * Finds the index of the circle segment, so that angle is between the start and the end of
     * that segment; given the circle gets split in segmentCount segments, starting at minus pi.
     * If angle is on a border, it counts as part of the segment that starts at that angle.
     */
    public static int getCircleSegment(double angle, int segmentCount) {
        double sectionSize = 2 * PI / segmentCount;

        //this works, I promise
        return (int) (floor(angle / sectionSize) + floor(segmentCount / 2));
    }

    public static double getCircleMedian(List<Double> angles) {
        //TODO
        double diameter;
        double diameterAdj;

        Collections.sort(angles);

        //find the diameter so is divides the angles in half
        if (angles.size() % 2 == 0) { //even

        } else { //odd

        }
        return 0;
    }
}
