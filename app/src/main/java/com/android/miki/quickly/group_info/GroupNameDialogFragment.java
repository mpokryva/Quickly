package com.android.miki.quickly.group_info;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.miki.quickly.R;

/**
 * Created by mpokr on 6/25/2017.
 */

public class GroupNameDialogFragment extends DialogFragment {

    public static final String TAG = GroupNameDialogFragment.class.getName();
    private GroupNameDialogListener dialogListener;
    private EditText editText;
    private String editTextContent;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        dialogListener = (GroupNameDialogListener) args.getSerializable(GroupNameDialogListener.TAG);
        editTextContent = args.getString(getString(R.string.dialog_text));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_change_group_name, null));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Overwritten in onStart()
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Just closes dialog
            }
        });
        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();
        Button okButton = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setTextColor(ContextCompat.getColor(getContext(), R.color.LightBlue));
        Button cancelButton = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
        cancelButton.setTextColor(ContextCompat.getColor(getContext(), R.color.LightBlue));
        editText = (EditText) getDialog().findViewById(R.id.edittext_group_name);
        if (editTextContent != null) {
            editText.setText(editTextContent);
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = GroupNameDialogFragment.this.getArguments();
                dialogListener.groupNameChanged(editText.getText().toString());
                dialogListener.dialogClosed();
                dismiss();
            }
        });
    }

    protected String getText() {
        return editText.getText().toString();
    }




    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        dialogListener.dialogClosed();
    }
}
