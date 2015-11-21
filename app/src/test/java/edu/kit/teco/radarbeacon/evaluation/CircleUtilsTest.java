package edu.kit.teco.radarbeacon.evaluation;

import org.junit.Test;

import java.util.ArrayList;

import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getCircleDistance;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getMeanAngle;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getOpposing;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;

/**
 * Created by Iris on 29.08.2015.
 */
public class CircleUtilsTest {

    final double delta = 0.001;

    @Test
    public void testGetOpposing() throws Exception {
        assertEquals(-PI / 2, getOpposing(PI / 2), delta);
        assertEquals(0, getOpposing(PI), delta);
        assertEquals(0, getOpposing(-PI), delta);
        assertEquals(-3 * PI / 4, getOpposing(PI / 4), delta);
    }

    @Test
    public void testGetCircleDistance() throws Exception {
        assertEquals(PI, getCircleDistance(0, PI), delta);
        assertEquals(PI, getCircleDistance(0, -PI), delta);
        assertEquals(0.3, getCircleDistance(0, 0.3), delta);
        assertEquals(PI / 2, getCircleDistance(-3 * PI / 4, 3 * PI / 4), delta);
    }

    @Test
    public void testGetMeanAngle() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(-PI / 4);
        list.add(PI / 4);
        assertEquals(0, getMeanAngle(list), delta);
    }

    @Test
    public void testGetMeanAngle2() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(-3 * PI / 4);
        list.add(3 * PI / 4);
        assertEquals(PI, abs(getMeanAngle(list)), delta);
    }

    @Test
    public void testGetMeanAngle3() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(-120 * PI / 180);
        list.add(-170 * PI / 180);
        list.add(90 * PI / 180);
        assertEquals(173.33333333 * PI / 180, abs(getMeanAngle(list)), 0.1);
    }

    @Test
    public void testGetMeanAnglePositive() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(PI * 1 / 4);
        list.add(PI * 3 / 4);
        assertEquals(PI / 2, getMeanAngle(list), 0.1);
    }

    //tests for correct handling in negative area
    @Test
    public void testGetMeanAngleNegative() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(-PI * 1 / 4);
        list.add(-PI * 3 / 4);
        assertEquals(-PI / 2, getMeanAngle(list), 0.1);
    }

    @Test
    public void testGetCircleSegment() throws Exception {

    }
}