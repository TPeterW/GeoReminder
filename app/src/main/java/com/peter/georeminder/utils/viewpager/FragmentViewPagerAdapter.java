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

    private List<Reminder> reminderList;
    private List<Location> locationList;

    public FragmentViewPagerAdapter(FragmentManager fm, List<Reminder> reminderList, List<Location> locationList) {
        super(fm);
        this.reminderList = reminderList;
        this.locationList = locationList;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return ListReminderFragment.getInstance(reminderList);
        else
            return ListLocationFragment.getInstance(locationList);
    }

    @Override
    public int getCount() {
        return 2;
    }

    // below is obsolete




























//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        Fragment fragment = (Fragment) super.instantiateItem(container, position);
//        registeredFragments.put(position, fragment);
//        return fragment;
//    }
//
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        registeredFragments.remove(position);
//        super.destroyItem(container, position, object);
//    }
//
//    public Fragment getRegisteredFragments(int position) {
//        return registeredFragments.valueAt(position);
//    }
//
//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        currentFragment = (ListReminderFragment) object;
//        super.setPrimaryItem(container, position, object);
//    }
}
