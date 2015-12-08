package edu.kit.teco.radarbeacon;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by Iris on 08.12.2015.
 */
public class TextDialog extends DialogFragment {

    private String text;
    private String buttonText;
    private String title;

    public static TextDialog getInstance(String text, String buttonText, String title) {
        TextDialog instance = new TextDialog();
        instance.text = text;
        instance.buttonText = buttonText;
        instance.title = title;
        return instance;
    }

    public static TextDialog getInstance(String text, String buttonText) {
        return getInstance(text, buttonText, null);
    }

    public static TextDialog getInstance(String text) {
        return getInstance(text, "Okay");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);

        builder.setMessage(text)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        if (title != null) {
            builder.setTitle(title);
        }

        return builder.create();
    }
}
