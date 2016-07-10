package com.peter.georeminder.utils.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.peter.georeminder.R;
import com.peter.georeminder.models.Location;

import java.util.List;

/**
 * Created by Peter on 11/6/15.
 * Adapter for the list of locations on the second tap of ViewPager
 */
public class LocationRecyclerAdapter extends RecyclerView.Adapter<LocationRecyclerAdapter.RecyclerViewHolder> {
    private List<Location> locationList;
    private Context context;
    private LayoutInflater inflater;

    /**
     * Implement OnClick and OnLongClick
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    // implement onItemClick and onItemLongClick
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }





    public LocationRecyclerAdapter(Context context, List<Location> locationList){
        this.locationList = locationList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public LocationRecyclerAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_location_recycler_item, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        //TODO: do what? figure out later
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        holder.mapScreenshot.setImageResource(R.drawable.location_default_icon);
        holder.locationTitle.setText(getItem(position).getName());
        // TODO: initialise the list view of all reminders
        // TODO: Change this to use information from locationList

        // set OnItemClick/LongClick listener
        // implement in calling activity (in this case, MainScreen)

        holder.cardView.setLongClickable(true);
        if (listener != null){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder.cardView, position);
                }
            });

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemLongClick(holder.cardView, position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;    //TODO: might want to change this later
    }

    public Location getItem(int position){
        return locationList.get(position);
    }

    public void addLocation(int position, Location addLocation){
        locationList.add(position, addLocation);
        notifyItemInserted(position);
    }

    public void deleteLocation(int position){
        locationList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged((int) getItemId(position), getItemCount() + 1);
    }

    /**
     * ViewHolder
     */
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView mapScreenshot;
        TextView locationTitle;
        ListView listReminders;             // list of reminders at this location
        //TODO: inplement this listReminders later (Maybe in version 2)

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.location_holder_cardview);
            mapScreenshot = (ImageView) itemView.findViewById(R.id.location_item_map_screenshot);
            locationTitle = (TextView) itemView.findViewById(R.id.location_recycler_item_title);
        }
    }
}
