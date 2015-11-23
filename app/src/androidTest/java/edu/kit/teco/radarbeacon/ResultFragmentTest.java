package edu.kit.teco.radarbeacon;

import android.bluetooth.BluetoothDevice;
import android.test.ActivityInstrumentationTestCase2;

import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.EvaluationStrategy;
import edu.kit.teco.radarbeacon.test.FragmentContainer;

/**
 * Created by Iris on 21.11.2015.
 */
public class ResultFragmentTest extends ActivityInstrumentationTestCase2<FragmentContainer> {

    private ResultFragment fragment = new ResultFragment();
    private HashMap<BluetoothDevice, EvaluationStrategy> results;

    public ResultFragmentTest() {
        super(FragmentContainer.class);
    }

    @Override
    public void setUp() {
        results = new HashMap<>();
        getActivity().getFragmentManager().beginTransaction().add(1, fragment, null).commit();
        getInstrumentation().waitForIdleSync();
    }

    public void testA() {
        assertTrue(getActivity().getFragmentManager().findFragmentById(1) != null);
    }

}
