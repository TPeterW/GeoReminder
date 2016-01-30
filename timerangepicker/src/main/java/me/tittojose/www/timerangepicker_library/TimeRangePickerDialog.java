package me.tittojose.www.timerangepicker_library;


import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TimePicker;

/**
 * Created by Jose on 24/05/15.
 * Modified by Peter on 30/01/16
 */
public class TimeRangePickerDialog extends DialogFragment implements View.OnClickListener {
    TabHost tabs;
    Button setTimeRange;
    TimePicker startTimePicker, endTimePicker;
    OnTimeRangeSelectedListener onTimeRangeSelectedListener;
    @ColorInt int primaryColor = R.color.colorPrimary;
    boolean is24HourMode;

    public static TimeRangePickerDialog newInstance(OnTimeRangeSelectedListener callback, boolean is24HourMode) {
        TimeRangePickerDialog ret = new TimeRangePickerDialog();
        ret.initialize(callback, is24HourMode);
        return ret;
    }

    public void initialize(OnTimeRangeSelectedListener callback,
                           boolean is24HourMode) {
        onTimeRangeSelectedListener = callback;
        this.is24HourMode = is24HourMode;
    }

    public interface OnTimeRangeSelectedListener {
        void onTimeRangeSelected(int startHour, int startMin, int endHour, int endMin);
    }

    public void setOnTimeRangeSetListener(OnTimeRangeSelectedListener callback) {
        onTimeRangeSelectedListener = callback;
    }

    public void setPrimaryColor(@ColorInt int primaryColor) {
        this.primaryColor = primaryColor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.timerange_picker_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        tabs = (TabHost) root.findViewById(R.id.tabHost);
        setTimeRange = (Button) root.findViewById(R.id.bSetTimeRange);
        startTimePicker = (TimePicker) root.findViewById(R.id.startTimePicker);
        endTimePicker = (TimePicker) root.findViewById(R.id.endTimePicker);
        setTimeRange.setOnClickListener(this);
        tabs.findViewById(R.id.tabHost);
        tabs.setup();
        TabHost.TabSpec tapPageStart = tabs.newTabSpec("one");
        tapPageStart.setContent(R.id.startTimeGroup);
        tapPageStart.setIndicator(getString(R.string.tab_start_time));

        TabHost.TabSpec tagPageEnd = tabs.newTabSpec("two");
        tagPageEnd.setContent(R.id.endTimeGroup);
        tagPageEnd.setIndicator(getString(R.string.tab_end_time));

        tabs.addTab(tapPageStart);
        tabs.addTab(tagPageEnd);

        setTimeRange.setTextColor(primaryColor);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null)
            return;
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bSetTimeRange) {
            dismiss();
            int startHour = startTimePicker.getCurrentHour();
            int startMin = startTimePicker.getCurrentMinute();
            int endHour = endTimePicker.getCurrentHour();
            int endMin = endTimePicker.getCurrentMinute();
            onTimeRangeSelectedListener.onTimeRangeSelected(startHour, startMin, endHour, endMin);
        }
    }
}
