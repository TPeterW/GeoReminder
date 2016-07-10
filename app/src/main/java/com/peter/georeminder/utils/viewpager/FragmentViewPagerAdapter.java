package com.peter.georeminder.utils.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.peter.georeminder.models.Location;
import com.peter.georeminder.models.Reminder;

import java.util.List;

/**
 * Created by Peter on 11/5/15.
 *
 */
public class FragmentViewPagerAdapter extends FragmentPagerAdapter {

    private final static int VIEW_PAGER_NUM_PAGES = 2;

    public FragmentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return ListReminderFragment.getInstance();
        else
            return ListLocationFragment.getInstance();
    }

    @Override
    public int getCount() {
        return VIEW_PAGER_NUM_PAGES;
    }
}
