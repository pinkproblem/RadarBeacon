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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.CircleUtils;
import edu.kit.teco.radarbeacon.evaluation.EvaluationStrategy;
import edu.kit.teco.radarbeacon.evaluation.InsufficientInputException;

public class ResultFragment extends Fragment {

    private HashMap<BluetoothDevice, EvaluationStrategy> results;
    private HashMap<BluetoothDevice, Float> resultBuffer;
    private float smoothAzimuth;

    private ResultCallbackListener callbackListener;

    private RelativeLayout relativeLayout;
    private ArrayList<ImageView> arrows;//for cleaning up
    private ArrayList<TextView> distanceViews;

    public ResultFragment() {
        super();
        resultBuffer = new HashMap<>();
    }

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
        arrows = new ArrayList<>();
        distanceViews = new ArrayList<>();

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

    public void onSmoothAzimuthChange(float newAzimuth) {
        smoothAzimuth = newAzimuth;
        updateView();
    }

//    public void restartMeasure(View view) {
//        callbackListener.restartMeasureRequest();
//    }

    public void updateResults(HashMap<BluetoothDevice, EvaluationStrategy> newResult) {
        results = newResult;
        fillResultBuffer();
    }

    private void fillResultBuffer() {
        resultBuffer.clear();
        for (BluetoothDevice device : results.keySet()) {
            EvaluationStrategy ev = results.get(device);
            float res;
            try {
                res = ev.calculate();
            } catch (InsufficientInputException e) {
                res = 0;
            }
            resultBuffer.put(device, res);
        }
    }

    void updateView() {
        //remove current arrows and texts
        for (ImageView im : arrows) {
            relativeLayout.removeView(im);
        }
        for (TextView t : distanceViews) {
            relativeLayout.removeView(t);
        }

        //add arrow and distance for every device
        for (final BluetoothDevice device : results.keySet()) {
            final ImageView arrow = new ImageView(getActivity());
            arrow.setImageResource(R.drawable.arrow2);
            arrows.add(arrow);

            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceClicked(device);
                }
            });

            int arrowSize = (int) dpToPx(100);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            float radius = screenWidth / 2 - arrowSize / 2;

            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2 - 60;

            //calculate direction
            Float direction = resultBuffer.get(device);
            if (direction == null) {
                fillResultBuffer();
                direction = resultBuffer.get(device);
            }
            direction = (float) (Math.PI / 4);
            float relativeDirection = direction - smoothAzimuth;
            float dirDegree = (float) ((CircleUtils.radiansToDegree(relativeDirection) + 180) % 360);

            //calculate position
            float x = (float) (radius * Math.sin(relativeDirection));
            float y = (float) (radius * Math.cos(relativeDirection));

            //set rotation
            arrow.setRotation(dirDegree);

            //set position
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(arrowSize, arrowSize);
            params.leftMargin = (int) (centerX + x - arrowSize / 2);
            params.topMargin = (int) (centerY - y - arrowSize / 2);
            arrow.setAdjustViewBounds(true);
            relativeLayout.addView(arrow, params);


            //distances
            EvaluationStrategy ev = results.get(device);
            TextView textView = new TextView(getActivity());
            distanceViews.add(textView);

            String text = String.format("%.1f", ev.getDistance()) + "m";
            textView.setText(text);
            textView.setTextSize(25);
            textView.setRotation(dirDegree);

            textView.measure(0, 0);
            float viewWidth = textView.getMeasuredWidth();
            float viewHeight = textView.getMeasuredHeight();

            radius = screenWidth / 2 - arrowSize / 2 - dpToPx(15);
            x = (float) (radius * Math.sin(relativeDirection));
            y = (float) (radius * Math.cos(relativeDirection));
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) (centerX + x - viewWidth / 2);
            params.topMargin = (int) (centerY - y - viewHeight / 2);
            relativeLayout.addView(textView, params);
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
