package edu.kit.teco.radarbeacon;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import edu.kit.teco.radarbeacon.animation.AnimationListenerAdapter;
import edu.kit.teco.radarbeacon.animation.RadarRevealAnimation;
import edu.kit.teco.radarbeacon.evaluation.CircleUtils;
import edu.kit.teco.radarbeacon.evaluation.EvaluationStrategy;
import edu.kit.teco.radarbeacon.evaluation.InsufficientInputException;

public class ResultFragment extends Fragment {

    ArrayList<ResultBuffer> results;
    ResultBuffer current;
    private float smoothAzimuth;

    private RelativeLayout relativeLayout;
    private RelativeLayout infoRelativeLayout;
    private TextView textName;
    private TextView textMac;
    private TextView textStatus;

    private boolean showResults;

    public enum DeviceState {ONLINE, OFFLINE, ERROR}

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        RadarRevealAnimation ani = new RadarRevealAnimation(getActivity());
        ani.setRevealDirection(RadarRevealAnimation.RevealDirection.OPEN);
        relativeLayout.addView(ani);
        relativeLayout.bringChildToFront(ani);
        ani.getAnimation().setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    showResults = true;
                    updateView();
                } catch (NullPointerException e) {
                    //TODO return to main menu or something
                }
            }
        });
        ani.getAnimation().startNow();
    }

    public void onSmoothAzimuthChange(float newAzimuth) {
        smoothAzimuth = newAzimuth;
        if (showResults) {
            updateView();
        }
    }

    public void updateResults(HashMap<BluetoothDevice, EvaluationStrategy> newResult) {
        fillResultBuffer(newResult);
    }

    private void fillResultBuffer(HashMap<BluetoothDevice, EvaluationStrategy> newResult) {
        results = new ArrayList<>();
        for (final BluetoothDevice device : newResult.keySet()) {

            final ResultBuffer buffer = new ResultBuffer();

            EvaluationStrategy ev = newResult.get(device);
            float res;
            try {
                res = ev.calculate();
                buffer.state = DeviceState.ONLINE;
            } catch (InsufficientInputException e) {
                res = (float) (Math.random() * (Math.PI - 0.1) - Math.PI / 2);
                buffer.state = DeviceState.ERROR;
            }

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

    private void updateDeviceInfo() {
        textStatus.setText(getStateString(current.state));
        textStatus.setTextColor(getStateColor(current.state));
        textName.setText(current.device.getName());
        textMac.setText(getString(R.string.mac) + ": " + current.device
                .getAddress());
    }

    private String getStateString(DeviceState state) {
        String text;
        switch (state) {
            case ONLINE:
                text = getString(R.string.online);
                break;
            case OFFLINE:
                text = getString(R.string.offline);
                break;
            case ERROR:
                text = getString(R.string.error);
                break;
            default:
                text = getString(R.string.error);
                break;
        }
        return text;
    }

    private int getStateColor(DeviceState state) {
        int color;
        switch (state) {
            case ONLINE:
                color = getResources().getColor(R.color.lightgreen);
                break;
            case OFFLINE:
                color = getResources().getColor(R.color.lightred);
                break;
            case ERROR:
                color = getResources().getColor(R.color.lightred);
                break;
            default:
                color = getResources().getColor(R.color.lightred);
                break;
        }
        return color;
    }

    private float dpToPx(float dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics
                ());
    }

    protected void onDeviceClicked(ResultBuffer clicked) {
        //reset previous arrow
        if (current != null) {
            current.arrow.setImageResource(R.drawable.arrow2);
        }

        current = clicked;

        updateDeviceInfo();
        infoRelativeLayout.setVisibility(View.VISIBLE);

        clicked.arrow.setImageResource(R.drawable.arrow2_dark);
        relativeLayout.bringChildToFront(clicked.arrow);
    }

    public void onDeviceConnected(BluetoothDevice device) {
        final ResultBuffer buffer = getBuffer(device);
        if (buffer == null) return;

        buffer.state = DeviceState.ONLINE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDeviceInfo();
            }
        });
    }

    public void onDeviceDisconnected(BluetoothDevice device) {
        final ResultBuffer buffer = getBuffer(device);
        if (buffer == null) return;

        buffer.state = DeviceState.OFFLINE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDeviceInfo();
            }
        });
    }

    private ResultBuffer getBuffer(BluetoothDevice device) {
        for (ResultBuffer res : results) {
            if (res.device.equals(device)) {
                return res;
            }
        }
        return null;
    }

    class ResultBuffer {
        BluetoothDevice device;
        DeviceState state;
        float direction;
        EvaluationStrategy evaluationStrategy;
        ImageView arrow;
        TextView text;
    }

}
