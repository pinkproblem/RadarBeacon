package edu.kit.teco.radarbeacon.evaluation;

import org.junit.Test;

import java.util.ArrayList;

import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getCircleDistance;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getCircleMedian;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getMeanAngle;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.getOpposing;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.isToRight;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testIsToRight() throws Exception {
        assertTrue(isToRight(0.0, 1.0));
        assertTrue(isToRight(1.0, 1.1));
        assertTrue(isToRight(0.0, 3.0 * PI / 4.0));
        assertTrue(isToRight(3.0 * PI / 4.0, -3.0 * PI / 4.0));
        assertTrue(isToRight(-3.0 * PI / 4.0, 0.0));
        assertTrue(isToRight(-1.0 * PI / 4.0, 1.0 * PI / 4.0));
        assertTrue(isToRight(-1.0 * PI / 4.0, PI / 2.0));

        assertFalse(isToRight(0.0, -1.0));
        assertFalse(isToRight(0.0, -PI));
        assertFalse(isToRight(-3.0 * PI / 4.0, 3.0 * PI / 4.0));
        assertFalse(isToRight(PI / 2.0, 0.0));
    }

    @Test
    public void testGetCircleMedian() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(0.0);
        list.add(1.0);
        list.add(-1.0);
        assertEquals(0.0, getCircleMedian(list), delta);

        list.add(-3.0);
        assertEquals(-0.5, getCircleMedian(list), delta);

        list.add(-2.0);
        assertEquals(-1.0, getCircleMedian(list), delta);
    }

    //test over definition border
    @Test
    public void testGetCircleMedian2() throws Exception {
        ArrayList<Double> list = new ArrayList<>();
        list.add(-3.0);
        list.add(-3.1);
        list.add(2.7);
        assertEquals(-3.1, getCircleMedian(list), delta);

        list.add(-2.9);
        assertEquals(-3.05, getCircleMedian(list), delta);

        list.add(2.0);
        assertEquals(-3.1, getCircleMedian(list), delta);

        list.add(2.1);
        assertEquals(PI - 0.2, getCircleMedian(list), delta);
    }
}