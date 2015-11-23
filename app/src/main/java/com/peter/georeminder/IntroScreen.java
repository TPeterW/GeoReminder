package com.peter.georeminder;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.peter.georeminder.utils.intro.IntroViewPagerAdapter;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by Peter on 11/3/15.
 *
 */
public class IntroScreen extends SwipeBackActivity {

    private IntroViewPagerAdapter adapter;

    private Button buttonSkip;

    private SwipeBackLayout swipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        initView();
    }

    private void initView() {

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_RIGHT);         // initialise swipe back layout
        swipeBackLayout.setEnableGesture(false);                                    // this is assuming I'm gonna have multiple intro pages, which I am

        ViewPager viewPager = (ViewPager) findViewById(R.id.intro_view_pager);
        viewPager.setPageTransformer(true, new CubeOutTransformer());

        adapter = new IntroViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.circular_indicator);
        circleIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getCount() - 1) {
                    // TODO: enable swipe back to main screen here
                    swipeBackLayout.setEnableGesture(true);           // only in the last fragment can you swipe to main screen
                    buttonSkip.setText(getString(R.string.proceed));
                } else {
                    swipeBackLayout.setEnableGesture(false);          // others can't
                    buttonSkip.setText(getString(R.string.skip));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        buttonSkip = (Button) findViewById(R.id.skip_intro_btn);
        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeBackLayout.setEnableGesture(true);
                swipeBackLayout.scrollToFinishActivity();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;                // you can't exit by pressing back key
        }
        return super.onKeyDown(keyCode, event);
    }
}
