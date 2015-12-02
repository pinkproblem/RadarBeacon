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

    ArrayList<ResultBuffer> results;
    ResultBuffer current;
    private float smoothAzimuth;

    private ResultCallbackListener callbackListener;

    private RelativeLayout relativeLayout;
    private RelativeLayout infoRelativeLayout;
    private TextView textName;
    private TextView textMac;
    private TextView textStatus;

    public ResultFragment() {
        super();
        results = new ArrayList<>();
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
        infoRelativeLayout = (RelativeLayout) view.findViewById(R.id.result_info_container);
        textName = (TextView) view.findViewById(R.id.result_name);
        textMac = (TextView) view.findViewById(R.id.result_mac);
        textStatus = (TextView) view.findViewById(R.id.result_status);

        infoRelativeLayout.setVisibility(View.INVISIBLE);

        try {
            updateView();
        } catch (NullPointerException e) {
            //TODO return to main menu or something
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
        fillResultBuffer(newResult);
    }

    private void fillResultBuffer(HashMap<BluetoothDevice, EvaluationStrategy> newResult) {
        results.clear();
        for (final BluetoothDevice device : newResult.keySet()) {
            EvaluationStrategy ev = newResult.get(device);
            float res;
            try {
                res = ev.calculate();
            } catch (InsufficientInputException e) {
                res = 0;
            }

            final ResultBuffer buffer = new ResultBuffer();
            buffer.device = device;
            buffer.direction = res;
            buffer.evaluationStrategy = ev;

            final ImageView arrow = new ImageView(getActivity());
            arrow.setImageResource(R.drawable.arrow2);
            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceClicked(buffer);
                }
            });
            buffer.arrow = arrow;

            TextView textView = new TextView(getActivity());
            String text = String.format("%.1f", ev.getDistance()) + "m";
            textView.setText(text);
            textView.setTextSize(25);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceClicked(buffer);
                }
            });
            buffer.text = textView;

            results.add(buffer);
        }
    }

    void updateView() {

        //add arrow and distance for every device
        for (final ResultBuffer buffer : results) {
            int arrowSize = (int) dpToPx(100);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            float radius = screenWidth / 2 - arrowSize / 2;

            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2 - 60;

            //calculate direction
            float direction = buffer.direction;
//            direction = (float) (Math.PI / 4);
            float relativeDirection = direction - smoothAzimuth;
            float dirDegree = (float) ((CircleUtils.radiansToDegree(relativeDirection) + 180) % 360);

            //calculate position
            float x = (float) (radius * Math.sin(relativeDirection));
            float y = (float) (radius * Math.cos(relativeDirection));

            //set rotation
            buffer.arrow.setRotation(dirDegree);

            //set position
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(arrowSize, arrowSize);
            params.leftMargin = (int) (centerX + x - arrowSize / 2);
            params.topMargin = (int) (centerY - y - arrowSize / 2);
            buffer.arrow.setAdjustViewBounds(true);
            buffer.arrow.setLayoutParams(params);
            if (relativeLayout.indexOfChild(buffer.arrow) == -1) {
                relativeLayout.addView(buffer.arrow);
            }


            //distances
            buffer.text.setRotation(dirDegree);

            buffer.text.measure(0, 0);
            float viewWidth = buffer.text.getMeasuredWidth();
            float viewHeight = buffer.text.getMeasuredHeight();

            radius = screenWidth / 2 - arrowSize / 2 - dpToPx(15);
            x = (float) (radius * Math.sin(relativeDirection));
            y = (float) (radius * Math.cos(relativeDirection));
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) (centerX + x - viewWidth / 2);
            params.topMargin = (int) (centerY - y - viewHeight / 2);
            buffer.text.setLayoutParams(params);
            if (relativeLayout.indexOfChild(buffer.text) == -1) {
                relativeLayout.addView(buffer.text);
            }
        }
    }

    private float dpToPx(float dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics
                ());
    }

    protected void onDeviceClicked(ResultBuffer clicked) {
        if (current != null) {
            current.arrow.setImageResource(R.drawable.arrow2);
        }

        textName.setText(clicked.device.getName());
        textMac.setText(getActivity().getResources().getString(R.string.mac) + ": " + clicked.device
                .getAddress());
        textStatus.setText("ONLINE");//TODO

        infoRelativeLayout.setVisibility(View.VISIBLE);

        clicked.arrow.setImageResource(R.drawable.arrow2_dark);

        current = clicked;
        relativeLayout.bringChildToFront(clicked.arrow);
    }

    public interface ResultCallbackListener {
        /**
         * Is called if the user requested (clicked the button) to restart the measurement process.
         */
//        public void restartMeasureRequest();
    }

    class ResultBuffer {
        BluetoothDevice device;
        float direction;
        EvaluationStrategy evaluationStrategy;
        ImageView arrow;
        TextView text;
    }

}
