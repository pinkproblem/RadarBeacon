package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.kit.teco.radarbeacon.evaluation.CircleUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MeasureFragment extends Fragment {

    private static final int NUMBER_OF_SEGMENTS = 6;
    private static final int MIN_VALUES_PER_SEGMENT = 5;

    //the activity that gets the call for a completed measurement
    private OnMeasureCompleteListener measureListener;

    private int[] segmentCount;
    private boolean measureComplete;

    ArrayList<TextView> texts;

    public MeasureFragment() {
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

        texts = new ArrayList<>();
        texts.add((TextView) view.findViewById(R.id.s1));
        texts.add((TextView) view.findViewById(R.id.s2));
        texts.add((TextView) view.findViewById(R.id.s3));
        texts.add((TextView) view.findViewById(R.id.s4));
        texts.add((TextView) view.findViewById(R.id.s5));
        texts.add((TextView) view.findViewById(R.id.s6));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        segmentCount = new int[NUMBER_OF_SEGMENTS];
    }

    public void addSample(float azimuth, int rssi) {

        if (measureComplete) {
            return;
        }

        final int segment = CircleUtils.getCircleSegment(azimuth, NUMBER_OF_SEGMENTS);
        segmentCount[segment]++;

        //set circle segment as done
        if (segmentCount[segment] >= MIN_VALUES_PER_SEGMENT) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    texts.get(segment).setText("yes");

                }
            });
        }

        //if all segments are done, call the listener
        if (isMeasureComplete()) {
            measureListener.onMeasureComplete();
            measureComplete = true;
        }
    }

    /**
     * Returns true, if all values in segmentCount are equal to or higher than
     * MIN_VALUES_PER_SEGMENT.
     */
    public boolean isMeasureComplete() {
        for (Integer i : segmentCount) {
            if (i < MIN_VALUES_PER_SEGMENT) {
                return false;
            }
        }
        return true;
    }

    public interface OnMeasureCompleteListener {
        public void onMeasureComplete();
    }
}
