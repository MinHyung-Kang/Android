package hu.ait.android.fo.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import hu.ait.android.fo.MainActivity;
import hu.ait.android.fo.R;

/**
 * Created by user on 2015-12-11.
 * Dialog Fragment used to allow the user to determine which results to filter
 * (Public, Friends Only, Groups, Private)
 */
public class FilterDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final boolean[] checkedItems = ((MainActivity) getActivity()).getFilterPreferences();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.dialog_filter_results)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.eventType_array, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                checkedItems[which] = isChecked;
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Save the options
                        ((MainActivity) getActivity()).savePreference(checkedItems);
                        ((MainActivity) getActivity()).applyPreference();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
