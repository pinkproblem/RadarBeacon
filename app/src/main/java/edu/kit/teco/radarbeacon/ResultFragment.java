package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

public class ResultFragment extends Fragment {

    private HashMap<BluetoothDevice, Float> results;

    private OnFragmentInteractionListener mListener;

    LinearLayout container;

    public ResultFragment() {
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

        this.container = (LinearLayout) view.findViewById(R.id.linear_layout);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateResults(HashMap<BluetoothDevice, Float> newResult) {
        results = newResult;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();

            }
        });
    }

    void updateView() {
        container.removeAllViews();
        for (BluetoothDevice device : results.keySet()) {
            TextView text = new TextView(getActivity());
            text.setText(device.getName() + ": " + results.get(device));
            container.addView(text);
        }
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction();
    }

}
