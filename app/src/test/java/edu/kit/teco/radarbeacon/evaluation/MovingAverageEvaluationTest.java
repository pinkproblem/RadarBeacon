package edu.kit.teco.radarbeacon.evaluation;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Iris on 22.07.2015.
 * <p>
 * All test cases assume a rangeFactor of 0.3.
 */
public class MovingAverageEvaluationTest {

    final double delta = 0.001;

    @Test
    public void movingAverageTest1() {
        MovingAverageEvaluation ev = new MovingAverageEvaluation();

        ev.addSample(1.0f, -60, 0);

        try {
            assertEquals(ev.calculate(), 1.0, delta);
        } catch (InsufficientInputException e) {
            assertTrue(false);
        }
    }

    @Test
    public void movingAverageTest2() {
        MovingAverageEvaluation ev = new MovingAverageEvaluation();

        ev.addSample(0.6f, -60, 0);
        ev.addSample(0.7f, -60, 0);
        ev.addSample(0.8f, -60, 0);
        ev.addSample(0.9f, -50, 0);
        ev.addSample(1.0f, -50, 0);
        ev.addSample(1.1f, -50, 0);
        ev.addSample(1.2f, -60, 0);
        ev.addSample(1.3f, -60, 0);
        ev.addSample(1.4f, -60, 0);
        ev.addSample(1.5f, -60, 0);

        try {
            assertEquals(ev.calculate(), 1.0, delta);
        } catch (InsufficientInputException e) {
            assertTrue(false);
        }
    }

    @Test
    public void movingAverageTest3() {
        MovingAverageEvaluation ev = new MovingAverageEvaluation();

        ev.addSample(-3.0f, -80, 0);
        ev.addSample(-2.5f, -100, 0);
        ev.addSample(-2.0f, -90, 0);
        ev.addSample(-1.5f, -80, 0);
        ev.addSample(-1.1f, -60, 0);
        ev.addSample(-1.0f, -70, 0);
        ev.addSample(-0.9f, -60, 0);
        ev.addSample(0.0f, -80, 0);
        ev.addSample(0.5f, -80, 0);
        ev.addSample(1.0f, -90, 0);
        ev.addSample(1.5f, -80, 0);
        ev.addSample(2.0f, -100, 0);
        ev.addSample(3.0f, -100, 0);

        try {
            assertEquals(ev.calculate(), -1.0, delta);
        } catch (InsufficientInputException e) {
            assertTrue(false);
        }
    }

    @Test(expected = InsufficientInputException.class)
    public void exceptionTest() throws InsufficientInputException {
        MovingAverageEvaluation ev = new MovingAverageEvaluation();

        ev.calculate();
    }
}
