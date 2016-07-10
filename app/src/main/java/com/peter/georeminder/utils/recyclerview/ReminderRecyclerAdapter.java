package com.peter.georeminder.utils.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blunderer.materialdesignlibrary.views.CardView;

import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Peter on 10/6/15.
 * Adapter for the list of reminders on the first tap of ViewPager
 */
public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderRecyclerAdapter.RecyclerViewHolder> {

    private OnBadgeUpdateListener badgeUpdateListener;

    private List<Reminder> reminderList;
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
    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }


    public ReminderRecyclerAdapter(Context context, List<Reminder> reminderList){
        this.reminderList = reminderList;
        this.context = context;
        badgeUpdateListener = (OnBadgeUpdateListener) context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ReminderRecyclerAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_reminder_recycler_item, parent, false);

        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        // TODO: do what? figure out later
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        // set OnItemClick/LongClick itemClickListener
        // implement in calling activity (in this case, MainScreen)

        Reminder current = reminderList.get(holder.getAdapterPosition());
        holder.cardView.setTitle(current.getTitle());
        holder.cardView.setImageResource(R.drawable.reminder_default_icon);
        holder.cardView.setNormalButtonText(context.getString(R.string.card_view_btn_edit));

        if (current.isWithLocation()) {         // has location
            holder.cardView.setDescription(context.getString(R.string.card_view_txt_location_name) + current.getRemindLocation().getName());
            holder.cardView.setHighlightButtonText(context.getString(R.string.card_view_btn_map));
        } else {                                // has a time
            if (current.isWithTime()) {
//                if (current.getEndTime() != null)
//                    holder.cardView.setDescription(context.getString(R.string.card_view_txt_date) + " " + current.getEndDate());
//                else
//                    holder.cardView.setDescription(context.getString(R.string.card_view_txt_date) + " " + current.getStartDate());
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTimeInMillis(current.getStartTime());
                holder.cardView.setDescription(context.getString(R.string.card_view_txt_date) + " " + startCalendar.getTime());
            } else {
//                holder.cardView.setDescription(context.getString(R.string.card_view_txt_no_hurry));
                holder.cardView.setDescription(reminderList.get(position).getDescription());
            }

            holder.cardView.setHighlightButtonText(null);
        }

        holder.cardView.setRadius(8);

//        Log.d("CardView", "Height: " + holder.itemView.getHeight() + " Width: " + holder.itemView.getWidth());

        if(itemClickListener != null){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(holder.cardView, holder.getAdapterPosition());
                }
            });

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemClickListener.onItemLongClick(holder.cardView, holder.getAdapterPosition());
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Reminder getItem(int position){
        return reminderList.get(position);
    }

    public void addReminder(int position, Reminder addedReminder){
        // TODO: what kind of reminder and where to add them
        reminderList.add(position, addedReminder);
        notifyItemInserted(position);
        badgeUpdateListener.onBadgeUpdate();
    }

    public void deleteReminder(int position){
        reminderList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged((int) getItemId(position), getItemCount() + 1);
        badgeUpdateListener.onBadgeUpdate();
    }



    /**
     * ViewHolder
     */
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        // TODO: add other attributes

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.reminder_holder_card_view);
        }
    }

    public interface OnBadgeUpdateListener {

        void onBadgeUpdate();

    }
}