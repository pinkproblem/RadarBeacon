package edu.kit.teco.radarbeacon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Iris on 19.08.2015.
 */
public class SelectDeviceDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<BluetoothDevice> selectedDevices = new ArrayList();  // Where we track the
        // selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
//        builder.setTitle(R.string.dialog_title_select_devices)
//                // Specify the list array, the items to be selected by default (null for none),
//                // and the listener through which to receive callbacks when items are selected
//                .setMultiChoiceItems(R.array.toppings, null,
//                        new DialogInterface.OnMultiChoiceClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which,
//                                                boolean isChecked) {
//                                if (isChecked) {
//                                    // If the user checked the item, add it to the selected items
//                                    selectedDevices.add(which);
//                                } else if (selectedDevices.contains(which)) {
//                                    // Else, if the item is already in the array, remove it
//                                    selectedDevices.remove(Integer.valueOf(which));
//                                }
//                            }
//                        })
//                        // Set the action buttons
//                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User clicked OK, so save the selectedDevices results somewhere
//                        // or return them to the component that opened the dialog
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });

        return builder.create();
    }
}
