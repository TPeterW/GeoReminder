package com.peter.georeminder.utils;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.peter.georeminder.models.Reminder;

import java.util.List;

/**
 * Created by Peter on 10/6/15.
 */
public class RecyclerAdapter extends RecyclerView.Adapter {
    private List<Reminder> reminderList;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }
}
