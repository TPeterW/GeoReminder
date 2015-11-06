package com.peter.georeminder.utils.recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peter.georeminder.R;

/**
 * Created by Peter on 11/6/15.
 *
 */
public class LocationRecyclerAdapter extends RecyclerView.Adapter<LocationRecyclerAdapter.RecyclerViewHolder> {


    @Override
    public LocationRecyclerAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    /**
     * ViewHolder
     */
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
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
