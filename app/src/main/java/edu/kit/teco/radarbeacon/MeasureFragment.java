package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.kit.teco.radarbeacon.evaluation.CircleUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MeasureFragment extends Fragment {

    private static final String EXTRA_DEVICE_COUNT = "fragment_extra_device_count";

    private static final int NUMBER_OF_SEGMENTS = 6;
    //TODO testing value
    private static final int MIN_VALUES_PER_SEGMENT = 1;//and per device!

    //the activity that gets the call for a completed measurement
    private OnMeasureCompleteListener measureListener;

    private int numberOfDevices;
    private int[] inputCount;
    private boolean measureComplete;

    public static MeasureFragment getInstance(int numberOfDevices) {
        MeasureFragment instance = new MeasureFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRA_DEVICE_COUNT, numberOfDevices);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            measureListener = (OnMeasureCompleteListener) activity;
        } catch (ClassCastException e) {
            //TODO
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputCount = new int[NUMBER_OF_SEGMENTS];
        numberOfDevices = getArguments().getInt(EXTRA_DEVICE_COUNT, 1);
    }

    public void addSample(float azimuth, int rssi) {

        if (measureComplete) {
            return;
        }

        final int segment = CircleUtils.getCircleSegment(azimuth, NUMBER_OF_SEGMENTS);
        inputCount[segment]++;

        //set circle segment as done
        if (inputCount[segment] >= MIN_VALUES_PER_SEGMENT * numberOfDevices) {
        }

        //if all segments are done, call the listener
        if (isMeasureComplete()) {
            measureListener.onMeasureComplete();
            measureComplete = true;
        }
    }

    /**
     * Returns true, if all values in inputCount are equal to or higher than
     * MIN_VALUES_PER_SEGMENT.
     */
    public boolean isMeasureComplete() {
        for (Integer i : inputCount) {
            if (i < MIN_VALUES_PER_SEGMENT * numberOfDevices) {
                return false;
            }
        }
        return true;
    }

    public interface OnMeasureCompleteListener {
        public void onMeasureComplete();
    }
}
