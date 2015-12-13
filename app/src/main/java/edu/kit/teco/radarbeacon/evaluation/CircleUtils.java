package edu.kit.teco.radarbeacon.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
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
        ArrayList<Double> list = new ArrayList<>();
        list.add(firstAngle);
        list.add(secondAngle);
        return getMeanAngle(list);
    }

    /**
     * Averages two or more angles; e.g. the average of 350° and 10° would be 0° (example in degrees
     * for better understanding, values are still in radians).
     * Reference: Jerrold H. Zar: Biostatistical Analysis
     */
    public static double getMeanAngle2(List<Double> angles) {
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

        //dont forget to shift back
        return acos(X / r);
    }

    public static double getMeanAngle(List<Double> angles) {

        double X;
        double Y;

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

        //dont forget to shift back
        return atan2(Y, X);
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
        double diameter = getMeanAngle(angles);
        double diameterAdj = getOpposing(diameter);

        Collections.sort(angles);

        //find the diameter so is divides the angles in half
        if (angles.size() % 2 == 0) { //even
            //iterate over midpoints of two points each, then test if exactly half of them are on each side
            for (int i = 0; i < angles.size(); i++) {
                double angle1 = angles.get(i);
                double angle2 = angles.get((i + 1) % angles.size());
                double a = getMeanAngle(angle1, angle2);

                int count = 0;
                for (Double b : angles) {
                    if (isToRight(a, b)) {
                        count++;
                    }
                }
                if (count == angles.size() / 2) {
                    diameter = a;
                    diameterAdj = getOpposing(a);
                    break;
                }
            }
        } else { //odd
            //iterate over angles, then test if exactly half of them are on each side
            for (Double a : angles) {
                int count = 0;
                for (Double b : angles) {
                    if (a == b) {
                        continue;
                    }
                    if (isToRight(a, b)) {
                        count++;
                    }
                }
                if (count == angles.size() / 2) {
                    diameter = a;
                    diameterAdj = getOpposing(a);
                    break;
                }
            }
        }
        //check if diameter or diameterAdj is closer to mean angle
        double mean = getMeanAngle(angles);
        if (getCircleDistance(mean, diameter) <= getCircleDistance(mean, diameterAdj)) {
            return diameter;
        } else {
            return diameterAdj;
        }
    }

    /**
     * Returns true, iff angle b is in the right semicircle defined by the diameter through angle
     * a, false otherwise. Angle a and the one opposing to a do not belong to the semisphere.
     *
     * @param a the angle spanning the diameter
     * @param b the angle to test
     */
    public static boolean isToRight(double a, double b) {
        //transform so a is at zero
        double normalB = b - a;
        if (normalB < -PI) {
            normalB += 2 * PI;
        }

        return normalB > 0 && normalB < PI;
    }
}
