package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.CircleUtils;
import edu.kit.teco.radarbeacon.evaluation.EvaluationStrategy;
import edu.kit.teco.radarbeacon.evaluation.InsufficientInputException;

public class ResultFragment extends Fragment {

    private HashMap<BluetoothDevice, EvaluationStrategy> results;

    private ResultCallbackListener callbackListener;

    private RelativeLayout relativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        relativeLayout = (RelativeLayout) view.findViewById(R.id.result_relative_layout);

        try {
            updateView();
        } catch (NullPointerException e) {
            //TODO return to main menu or something
            results = new HashMap<>();
            updateView();
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
//            callbackListener = (ResultCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ResultCallbackListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackListener = null;
    }

//    public void restartMeasure(View view) {
//        callbackListener.restartMeasureRequest();
//    }

    public void updateResults(HashMap<BluetoothDevice, EvaluationStrategy> newResult) {
        results = newResult;
    }

    void updateView() {
        for (final BluetoothDevice device : results.keySet()) {
            final ImageView arrow = new ImageView(getActivity());
            arrow.setImageResource(R.drawable.arrow2);

            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceClicked(device);
                }
            });

            int arrowSize = (int) dpToPx(100);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int radius = screenWidth / 2 - arrowSize / 2;

            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2 - 70;

            //calculate direction
            float direction;
            try {
                direction = results.get(device).calculate();
            } catch (InsufficientInputException e) {
                direction = 0;
            }
            //direction = (float) (0);
            double dirDegree = (CircleUtils.radiansToDegree(direction) + 180) % 360;

            //calculate position
            float x = (float) (radius * Math.sin(direction));
            float y = (float) (radius * Math.cos(direction));

            //set rotation
            arrow.setRotation((float) dirDegree);

            //set position
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(arrowSize, arrowSize);
            params.leftMargin = (int) (centerX + x - arrowSize / 2);
            params.topMargin = (int) (centerY - y - arrowSize / 2);
            arrow.setAdjustViewBounds(true);
            relativeLayout.addView(arrow, params);
        }
    }

    private float dpToPx(float dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics
                ());
    }

    protected void onDeviceClicked(BluetoothDevice device) {

    }

    public interface ResultCallbackListener {
        /**
         * Is called if the user requested (clicked the button) to restart the measurement process.
         */
//        public void restartMeasureRequest();
    }

}
