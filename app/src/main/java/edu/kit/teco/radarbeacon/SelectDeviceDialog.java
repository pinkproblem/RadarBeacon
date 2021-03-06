package edu.kit.teco.radarbeacon;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class SelectDeviceDialog extends DialogFragment {

    public static final String DIALOG_EXTRA_DEVICES = "dialog_extra_devices";

    private OnConfirmSelectionListener confirmListener;
    private ArrayList<BluetoothDevice> allDevices;

    private ArrayList<BluetoothDevice> selectedDevices;
    private BaseAdapter adapter;

    private ProgressBar loadingIcon;

    /**
     * Returns a new instance of the dialog.
     *
     * @param devices a list of all devices that should be selectable in the dialog
     * @return the new instance
     */
    public static SelectDeviceDialog getInstance(
            ArrayList<BluetoothDevice> devices) {
        SelectDeviceDialog instance = new SelectDeviceDialog();

        //pass arguments
        //This is necessary because Fragments only support a default constructor without arguments.
        Bundle args = new Bundle();
        args.putSerializable(DIALOG_EXTRA_DEVICES, devices);

        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //unpack arguments
        //This is necessary because Fragments only support a default constructur without arguments.
        Bundle args = getArguments();
        allDevices = (ArrayList<BluetoothDevice>) args.get(DIALOG_EXTRA_DEVICES);

        selectedDevices = new ArrayList<>();
    }

    public void update(ArrayList<BluetoothDevice> newDevices) {
//        allDevices=newDevices;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setLoadingIcon(boolean show) {
        if (show) {
            loadingIcon.setVisibility(View.VISIBLE);
        } else {
            loadingIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            confirmListener = (OnConfirmSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmSelectionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        adapter = new CustomAdapter();

        //inflate view and retrieve elements
        View view = inflater.inflate(R.layout.dialog_select_device, null);
        ListView list = (ListView) view.findViewById(R.id.list_select);
        TextView emptyView = (TextView) view.findViewById(R.id.empty_view);
        loadingIcon = (ProgressBar) view.findViewById(R.id.loading_select);

        list.setEmptyView(emptyView);
        list.setAdapter(adapter);

        //TODO select at least one
        builder.setTitle(R.string.dialog_title_select_devices)
                .setView(view)
                .setPositiveButton(R.string.start_scan, null)//OnClickListener is defined in
                        // onStart(), else the dialog dismisses always
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedDevices.size() < 1) {
                        Toast toast = Toast.makeText(getActivity(), R.string.toast_select_one,
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        dialog.dismiss();
                        confirmListener.onConfirmSelection(selectedDevices);
                    }
                }
            });
        }
    }

    //adapter for the list inside the dialog
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
            View view = View.inflate(getActivity(), R.layout
                    .checklist_item_two_lines, null);
            BluetoothDevice device = allDevices.get(position);

            //set device's name and mac to the list item's text fields
            TextView name = (TextView) view.findViewById(R.id.text_device_name);
            TextView mac = (TextView) view.findViewById(R.id.text_device_mac);
            name.setText(device.getName());
            mac.setText(device.getAddress());

            //On item click the following happens: checkbox is toggled (manually, because else
            // it messes up everything, not forwarding the click and stuff) if the checkbox is
            // checked afterwards, the corresponding device is added to the list of selected
            // devices, else it is removed from the list
            // Please notice: Checkbox is disabled via layout definition
            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BluetoothDevice clickedDevice = allDevices.get(position);
                    checkbox.toggle();

                    if (checkbox.isChecked()) {
                        selectedDevices.add(clickedDevice);
                    } else {
                        selectedDevices.remove(clickedDevice);
                    }
                }
            });

            //set the checkbox as checked if the device is in the selection list. This is
            // necessary because else the checked-state might get lost on view refresh.
            if (selectedDevices.contains(device)) {
                checkbox.setChecked(true);
            }

            return view;
        }
    }

    interface OnConfirmSelectionListener {
        /**
         * Is called when the user presses the positive button of the dialog.
         *
         * @param selection the selected devices of the dialog
         */
        public void onConfirmSelection(ArrayList<BluetoothDevice> selection);
    }
}
