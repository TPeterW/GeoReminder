package com.peter.georeminder.utils;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;

import java.util.List;

/**
 * Created by Peter on 10/6/15.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    private List<Reminder> reminderList;
    private Context context;
    private LayoutInflater inflater;

    public RecyclerAdapter (Context context, List<Reminder> reminderList){
        this.reminderList = reminderList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.geo_reminder_recycler_item, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.mapScreenshot.setImageResource(R.mipmap.ic_launcher);
        holder.reminderTitle.setText("Title");
        holder.reminderContent.setText("Content Content Content Content Content");

        // TODO: Change this to use information from reminderList
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView mapScreenshot;
        TextView reminderTitle;
        TextView reminderContent;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.geo_holder_cardview);
            mapScreenshot = (ImageView) itemView.findViewById(R.id.recycler_item_map_screenshot);
            reminderTitle = (TextView) itemView.findViewById(R.id.recycler_item_title);
            reminderContent = (TextView) itemView.findViewById(R.id.recycler_item_content);
        }
    }
}