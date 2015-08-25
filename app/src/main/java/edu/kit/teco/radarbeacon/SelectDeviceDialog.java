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

    /**
     * Returns a new instance of the dialog.
     *
     * @param listener the listener that is called if the user presses the positive button. The
     *                 listener is passed a list of all selected devices.
     * @param devices  a list of all devices that should be selectable in the dialog
     * @return the new instance
     */
    public static SelectDeviceDialog getInstance(OnConfirmSelectionListener listener,
                                                 ArrayList<BluetoothDevice> devices) {
        SelectDeviceDialog instance = new SelectDeviceDialog();

        //pass arguments
        //This is necessary because Fragments only support a default constructur without arguments.
        Bundle args = new Bundle();
        args.putSerializable(DIALOG_EXTRA_LISTENER, listener);
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
                // set the adapter that defines how the list is shown and sets the content
                .setAdapter(new CustomAdapter(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing here, because the checkbox prevents this method from being
                        // called (because it is focusable or something). The click listener is
                        // applied directly to the checkbox, see the adapter below.
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

        //this keeps the dialog open on list item click, else it would dismiss instantly
        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        return dialog;
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
            View view = getActivity().getLayoutInflater().inflate(R.layout
                    .checklist_item_two_lines, null);
            BluetoothDevice device = allDevices.get(position);

            //set device's name and mac to the list item's text fields
            TextView name = (TextView) view.findViewById(R.id.text_device_name);
            TextView mac = (TextView) view.findViewById(R.id.text_device_mac);
            name.setText(device.getName());
            mac.setText(device.getAddress());

            //On item click the following happens: checkbox gets checked (manually, because else
            // it messes up everything, not forwarding the cklick and stuff) if the ceckbox is
            // checked afterwards, the corresponding device is added to the list of selected
            // devices, else it is removed from the list
            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BluetoothDevice clickedDevice = allDevices.get(position);
                    checkbox.toggle();
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
        /**
         * Is called when the user presses the positive button of the dialog.
         *
         * @param selection the selected devices of the dialog
         */
        public void onConfirmSelection(ArrayList<BluetoothDevice> selection);
    }
}
