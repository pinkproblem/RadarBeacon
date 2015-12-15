package edu.kit.teco.radarbeacon;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Iris on 01.12.2015.
 */
public class TutorialDialog extends DialogFragment {

    private String preference;

    private SpannableStringBuilder text;
    private String title;
    private String buttonText;

    private TextView textView;
    private CheckBox checkbox;

    public static TutorialDialog getInstance(String preference, String text) {
        return getInstance(preference, text, "Okay", null);
    }

    public static TutorialDialog getInstance(String preference, String text, String buttonText,
                                             String title) {
        TutorialDialog instance = new TutorialDialog();

        instance.preference = preference;
        instance.text = new SpannableStringBuilder(text);
        instance.buttonText = buttonText;
        instance.title = title;

        return instance;
    }

    public static TutorialDialog getInstance(String preference, SpannableStringBuilder text, String
            buttonText, String title) {
        TutorialDialog instance = new TutorialDialog();

        instance.preference = preference;
        instance.text = text;
        instance.buttonText = buttonText;
        instance.title = title;

        return instance;
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

        builder.setTitle(title)
                .setView(view)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //save the "dont show again" preference
                        if (checkbox.isChecked()) {
                            SharedPreferences pref = getActivity().getSharedPreferences
                                    (preference, 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean(preference, false);
                            editor.apply();
                        }
                    }
                });

        return builder.create();
    }

    public static SpannableString createIndentedText(String text, int marginFirstLine, int
            marginNextLines) {
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0, text.length(), 0);
        return result;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof OnDismissListener) {
            ((OnDismissListener) getActivity()).onDismissTutorial();
        }
    }

    public interface OnDismissListener {
        public void onDismissTutorial();
    }
}
