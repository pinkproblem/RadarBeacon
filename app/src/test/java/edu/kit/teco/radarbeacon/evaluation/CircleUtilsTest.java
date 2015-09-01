package edu.kit.teco.radarbeacon.evaluation;

import org.junit.Test;

import static org.junit.Assert.*;
import static java.lang.Math.*;
import static edu.kit.teco.radarbeacon.evaluation.CircleUtils.*;

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

    }

    @Test
    public void testGetCircleSegment() throws Exception {

    }
}