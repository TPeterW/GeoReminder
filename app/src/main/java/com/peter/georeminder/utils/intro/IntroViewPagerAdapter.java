package com.peter.georeminder.utils.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Random;

/**
 * Created by Peter on 11/3/15.
 *
 */
public class IntroViewPagerAdapter extends FragmentPagerAdapter{

    private static final int PAGE_COUNT = 5;

    Random random = new Random();

    public IntroViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return IntroFragment.newInstance(0xff000000 | random.nextInt(0x00ffffff));

    }
}
