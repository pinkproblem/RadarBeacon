package edu.kit.teco.radarbeacon.evaluation;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Iris on 21.07.2015.
 */
public class CircularArrayListTest {

    @Test
    public void listTest() {
        CircularArrayList<Integer> list = new CircularArrayList<>();
        list.add(0, 0);
        list.add(1, 1);
        list.add(2, 2);
        list.add(3, 3);

        assertEquals(list.get(0).intValue(), 0);

        assertEquals(list.get(-1).intValue(), 3);
        assertEquals(list.get(-3).intValue(), 1);
        assertEquals(list.get(-5).intValue(), 3);

        assertEquals(list.get(4).intValue(), 0);
    }

}
