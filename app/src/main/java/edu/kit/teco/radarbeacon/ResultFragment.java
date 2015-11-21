package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;

import edu.kit.teco.radarbeacon.evaluation.CircleUtils;

public class ResultFragment extends Fragment {

    private HashMap<BluetoothDevice, Float> results;

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
        updateView();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbackListener = (ResultCallbackListener) activity;
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

    public void updateResults(HashMap<BluetoothDevice, Float> newResult) {
        results = newResult;
    }

    void updateView() {
        for (final BluetoothDevice device : results.keySet()) {
            final ImageView arrow = new ImageView(getActivity());
            arrow.setImageResource(R.drawable.arrow);

            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceClicked(device);
                }
            });


            arrow.setAdjustViewBounds(true);
//            arrow.setMaxWidth(100);

            float direction = results.get(device);
            double dirDegree = (CircleUtils.radiansToDegree(direction) + 180) % 360;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(200, 200, 400, 400);
            arrow.setLayoutParams(params);

//            arrow.layout(100, 100, 110, 110);
            relativeLayout.addView(arrow);
        }
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
