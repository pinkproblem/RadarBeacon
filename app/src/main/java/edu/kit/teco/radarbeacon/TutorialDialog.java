package edu.kit.teco.radarbeacon;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Iris on 01.12.2015.
 */
public class TutorialDialog extends DialogFragment {

    private static final String DIALOG_EXTRA_TEXT = "dialog_extra_text";
    public static final String PREF_TUT_MEASURE = "preftutmeasure";

    private String text;

    private TextView textView;
    private CheckBox checkbox;

    public static TutorialDialog getInstance(String text) {
        TutorialDialog instance = new TutorialDialog();

        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_EXTRA_TEXT, text);
        instance.setArguments(bundle);

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        text = args.getString(DIALOG_EXTRA_TEXT);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //inflate view and retrieve elements
        View view = inflater.inflate(R.layout.dialog_simple_tutorial, null);
        textView = (TextView) view.findViewById(R.id.tutorial_text);
        checkbox = (CheckBox) view.findViewById(R.id.tutorial_checkbox);
        textView.setText(text);

        builder.setTitle(R.string.dialog_title_select_devices)
                .setView(view)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //save the "dont show again" preference
                        if (checkbox.isChecked()) {
                            SharedPreferences pref = getActivity().getSharedPreferences
                                    (PREF_TUT_MEASURE, 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean(PREF_TUT_MEASURE, false);
                            editor.apply();
                        }
                    }
                });

        return builder.create();
    }
}
