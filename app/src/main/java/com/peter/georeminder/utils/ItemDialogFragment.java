package com.peter.georeminder.utils;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peter.georeminder.R;

/**
 * Created by peter on 2015-10-22.
 */
public class ItemDialogFragment extends DialogFragment {

    public static ItemDialogFragment newInstance(){
        ItemDialogFragment dialogFragment = new ItemDialogFragment();

        // specifications if needed
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_op_dialog, container, false);

        // TODO: add click event

        return view;
    }
}
