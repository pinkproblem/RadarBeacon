package edu.kit.teco.radarbeacon.evaluation;

import java.util.ArrayList;

/**
 * Created by Iris on 20.07.2015.
 * <p>
 * An array list that also takes negative indices and handles them as if the list was repeated periodically, e.g. get(-1) equals a call to get(size-2).
 */
public class CircularArrayList<E> extends ArrayList<E> {

    @Override
    public E get(int index) {
        int newIndex = index % size();
        if (newIndex < 0) {
            newIndex += size();
        }

        return super.get(newIndex);
    }
}
