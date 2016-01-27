package com.peter.georeminder.utils.viewpager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.peter.georeminder.EditorScreen;
import com.peter.georeminder.MainScreen;
import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;
import com.peter.georeminder.utils.recyclerview.ReminderRecyclerAdapter;

import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by Peter on 11/5/15.
 *
 */
public class ListReminderFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int CREATE_NEW_GEO_REMINDER_REQUEST_CODE = 0x001;

    private ListReminderListener listener;

    private List<Reminder> reminderList;

    private RecyclerView recyclerView;
    private WaveSwipeRefreshLayout swipeRefreshLayout;

    private ImageView imgNoReminder;
    private TextView txtNoReminder;
    private Button btnNoReminder;


    private ReminderRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;


    public static ListReminderFragment getInstance(){
        return new ListReminderFragment();
    }

    public ListReminderFragment() {
        this.reminderList = MainScreen.getReminderList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_list_reminder_fragment, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.reminder_recycler_layout);
        swipeRefreshLayout = (WaveSwipeRefreshLayout) rootView.findViewById(R.id.reminder_swipe_to_refresh_layout);

        imgNoReminder = (ImageView) rootView.findViewById(R.id.image_no_reminder);
        txtNoReminder = (TextView) rootView.findViewById(R.id.text_no_reminder);
        btnNoReminder = (Button) rootView.findViewById(R.id.borderless_btn_new_reminder);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        
        setUpRecyclerView();

        setUpSwipeRefresh();

        setUpOthers();
        
        return rootView;
    }

    private void setUpRecyclerView() {

        // Main content (RecyclerView)
        recyclerAdapter = new ReminderRecyclerAdapter(getActivity(), reminderList);
        recyclerAdapter.setOnItemClickListener(new ReminderRecyclerAdapter.OnItemClickListener() {              // click event for each reminder item
            @Override
            public void onItemClick(View view, int position) {
                listener.onReminderClicked(view, position);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                listener.onReminderLongClicked(view, position);
                // get the reminder
                String currentTitle = recyclerAdapter.getItem(position).getTitle();

                // to alert the user about deleting by vibrating
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

                // TODO: add code
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(currentTitle)
                        .setItems(new String[]{getString(R.string.dialog_edit_title), getString(R.string.dialog_share_title), getString(R.string.dialog_delete_title)},
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:     // edit button
                                                Toast.makeText(getActivity(), "Edit", Toast.LENGTH_SHORT).show();
                                                break;
                                            case 1:     // share button
                                                showShareDialog(position);
                                                break;
                                            case 2:     // delete button
                                                showDeleteDialog(position);
                                                break;
                                        }
                                    }
                                });
                AlertDialog dialog = builder.create();
                // vibrate, TODO: check disable vibration
                vibrator.vibrate(20);
                dialog.show();

                //TODO: also after adding one, remember to hide these two views
            }
        });


        // use linear layout manager to set Recycler view
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(recyclerAdapter);
        // set hide and show animation when user scrolls
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                listener.onReminderListScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setUpSwipeRefresh() {
        //        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
//        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);        // color scheme
        swipeRefreshLayout.setMaxDropHeight(300);           // TODO: figure out why this doesn't work
        swipeRefreshLayout.setWaveColor(Color.parseColor("#8bc34a"));       // that's colorPrimary
        swipeRefreshLayout.setShadowRadius(7);
        swipeRefreshLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorTransparent));

        swipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {           // this is a very dirty workaround for the build tool support problem
                recyclerView.setNestedScrollingEnabled(false);      // so wouldn't pull AppBar out
                listener.onReminderListRefresh();
            }
        });

//        swipeRefreshLayout.setKeepScreenOn(true);
    }

    private void setUpOthers() {
        // Empty list
        btnNoReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newReminder = new Intent(getActivity(), EditorScreen.class);       // default is a new GeoReminder
                newReminder.putExtra(getString(R.string.bundle_with_map), true);
                //TODO: more specifications

                if (Build.VERSION.SDK_INT >= 21) {
                    getActivity().getWindow().setExitTransition(new Fade());
                    getActivity().getWindow().setReenterTransition(new Fade());
                    getActivity().startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else
                    getActivity().startActivityForResult(newReminder, CREATE_NEW_GEO_REMINDER_REQUEST_CODE);
            }
        });
        if(reminderList.size() != 0) {
            imgNoReminder.setVisibility(View.INVISIBLE);
            txtNoReminder.setVisibility(View.INVISIBLE);
            btnNoReminder.setVisibility(View.INVISIBLE);
            btnNoReminder.setClickable(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        this.listener = (ListReminderListener) context;
        super.onAttach(context);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_is_refreshing))) {
            if(!sharedPreferences.getBoolean(getString(R.string.pref_is_refreshing), false)) {              // is not refreshing any more
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.setNestedScrollingEnabled(true);
                layoutManager.smoothScrollToPosition(recyclerView, null, 0);        // scrolls back up to 0
            }
        }

        if(key.equals(getString(R.string.pref_refresh_enabled))) {
            swipeRefreshLayout.setEnabled(sharedPreferences.getBoolean(getString(R.string.pref_refresh_enabled), true));
        }
    }

    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_delete_title))
                .setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_dialog_warning))
                .setMessage(getString(R.string.dialog_delete_msg))
                .setPositiveButton(getString(R.string.dialog_pos_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recyclerAdapter.deleteReminder(position);
                        if (reminderList.size() == 0) {
                            imgNoReminder.setVisibility(View.VISIBLE);
                            txtNoReminder.setVisibility(View.VISIBLE);
                            btnNoReminder.setVisibility(View.VISIBLE);
                            btnNoReminder.setClickable(true);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_neg_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // let's do nothing
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showShareDialog(final int position) {
        Intent shareWith = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "This is totally temporary").setType("text/plain");
        getActivity().startActivity(Intent.createChooser(shareWith, getString(R.string.dialog_share_msg)));
    }

    @Override
    public void onResume() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public interface ListReminderListener {

        void onReminderClicked(View view, int position);

        void onReminderLongClicked(View view, final int position);

        void onReminderListScrolled(RecyclerView recyclerView, int dx, int dy);

        void onReminderListRefresh();
    }
}
