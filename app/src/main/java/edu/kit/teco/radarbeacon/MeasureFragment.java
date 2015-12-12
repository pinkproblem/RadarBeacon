package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.kit.teco.radarbeacon.animation.AnimationListenerAdapter;
import edu.kit.teco.radarbeacon.animation.RadarRevealAnimation;
import edu.kit.teco.radarbeacon.evaluation.CircleUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MeasureFragment extends Fragment {

    private static final String EXTRA_DEVICE_COUNT = "fragment_extra_device_count";

    private static final int NUMBER_OF_SEGMENTS = 8;
    //TODO testing value
    private static final int MIN_VALUES_PER_SEGMENT = 1;//and per device!

    //the activity that gets the call for a completed measurement
    private OnMeasureCompleteListener measureListener;

    private int numberOfDevices;
    private int[] inputCount;
    private boolean measureComplete;

    private MeasureDrawable measureDrawable;
    private ImageView icon;
    private TextView textCalculating;
    private RelativeLayout layout;

    public static MeasureFragment getInstance(int numberOfDevices) {
        MeasureFragment instance = new MeasureFragment();
        instance.numberOfDevices = numberOfDevices;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputCount = new int[NUMBER_OF_SEGMENTS];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);
        measureDrawable = (MeasureDrawable) view.findViewById(R.id.measure_view);
        measureDrawable.setSegmentCount(NUMBER_OF_SEGMENTS);

        icon = (ImageView) view.findViewById(R.id.measure_icon);
        textCalculating = (TextView) view.findViewById(R.id.measure_calculating);
        layout = (RelativeLayout) view.findViewById(R.id.measure_container);


        return view;
    }

    public void rotateView(float angle) {
        if (measureDrawable == null || measureComplete) {
            Log.d("radarbeacon", "MeasureFragment: rotation dropped: " + angle);
            return;
        }
        measureDrawable.setRotation(angle);
    }

    public void addSample(float azimuth, int rssi) {

        if (measureComplete || measureDrawable == null || measureListener == null) {
            Log.d("radarbeacon", "Sample dropped: azimuth " + azimuth + " rssi " + rssi);
            return;
        }

        final int segment = CircleUtils.getCircleSegment(azimuth, NUMBER_OF_SEGMENTS);
        inputCount[segment]++;

        //set circle segment as done
        if (inputCount[segment] >= MIN_VALUES_PER_SEGMENT * numberOfDevices) {
            measureDrawable.tag(azimuth);
        }

        //if all segments are done, call the listener
        if (isMeasureComplete()) {

            measureComplete = true;
            displayAnimation(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    measureListener.onMeasureComplete();
                }
            });
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

    private void displayAnimation(Animation.AnimationListener animationListener) {

        Animation animFadeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R
                .anim.fade_out);
        Animation animFadeIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext()
                , android.R
                .anim.fade_in);
        animFadeOut.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                icon.setVisibility(View.INVISIBLE);
            }
        });
        icon.setAnimation(animFadeOut);
        icon.setVisibility(View.GONE);
        animFadeIn.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                textCalculating.setVisibility(View.VISIBLE);
            }
        });
        textCalculating.setAnimation(animFadeIn);

        RadarRevealAnimation ani = new RadarRevealAnimation(getActivity());
        layout.addView(ani, 1);
        ani.getAnimation().setAnimationListener(animationListener);
        ani.getAnimation().startNow();
    }

    public interface OnMeasureCompleteListener {
        public void onMeasureComplete();
    }
}
