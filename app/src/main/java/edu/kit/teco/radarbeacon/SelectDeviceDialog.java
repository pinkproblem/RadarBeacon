package edu.kit.teco.radarbeacon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;


public class SelectDeviceDialog extends DialogFragment {

    public static final String DIALOG_EXTRA_LISTENER = "dialog_extra_listener";
    public static final String DIALOG_EXTRA_DEVICES = "dialog_extra_devices";

    private OnConfirmSelectionListener confirmListener;
    private ArrayList<BluetoothDevice> allDevices;

    private ArrayList<BluetoothDevice> selectedDevices;

    public SelectDeviceDialog() {
    }

    public static SelectDeviceDialog getInstance(OnConfirmSelectionListener listener,
                                                 ArrayList<BluetoothDevice> devices) {
        SelectDeviceDialog instance = new SelectDeviceDialog();

        Bundle args = new Bundle();
        args.putSerializable(DIALOG_EXTRA_LISTENER, listener);
        args.putSerializable(DIALOG_EXTRA_DEVICES, devices);

        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        confirmListener = (OnConfirmSelectionListener) args.get(DIALOG_EXTRA_LISTENER);
        allDevices = (ArrayList<BluetoothDevice>) args.get(DIALOG_EXTRA_DEVICES);

        selectedDevices = new ArrayList<>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        //TODO select at least one
        builder.setTitle(R.string.dialog_title_select_devices)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setAdapter(new CustomAdapter(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        selectedDevices.add(allDevices.get(which));
//                        Log.d("", "add device:" + which);
                    }
                })
                .setPositiveButton(R.string.start_scan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        confirmListener.onConfirmSelection(selectedDevices);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        return dialog;
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return allDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = getActivity().getLayoutInflater().inflate(R.layout
                    .checklist_item_two_lines, null);
            BluetoothDevice device = allDevices.get(position);

            TextView name = (TextView) view.findViewById(R.id.text_device_name);
            TextView mac = (TextView) view.findViewById(R.id.text_device_mac);
            name.setText(device.getName());
            mac.setText(device.getAddress());

            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BluetoothDevice clickedDevice = allDevices.get(position);
                    Log.d("", "clicked: " + clickedDevice.getAddress());
                    if (checkbox.isChecked()) {
                        selectedDevices.add(clickedDevice);
                    } else {
                        selectedDevices.remove(clickedDevice);
                    }
                }
            });

            return view;
        }
    }

    public interface OnConfirmSelectionListener extends Serializable {
        public void onConfirmSelection(ArrayList<BluetoothDevice> selection);
    }
}
