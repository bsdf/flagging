package com.theporouscity.flagging;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.theporouscity.flagging.R;

import com.theporouscity.flagging.ilx.ServerBookmarks;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by bergstroml on 8/8/16.
 */

public class ActivityMainTabs extends AppCompatActivity {

    private static final String TAG = "ActivityMainTabs";

    @BindView(R.id.activity_main_tabs_tabs)
    TabLayout mTabLayout;

    @BindView(R.id.activity_main_tabs_viewpager)
    ViewPager mViewPager;

    @BindView(R.id.activity_main_tabs_fab)
    FloatingActionButton mFloatingActionButton;

    @BindView(R.id.activity_main_tabs_toolbar)
    Toolbar mToolbar;

    @Inject
    ILXRequestor mILXRequestor;

    private int mShortAnimationDuration;
    private boolean mFetchedBookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, System.identityHashCode(this) + "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_tabs);
        ((FlaggingApplication) getApplication()).getILXComponent().inject(this);
        ButterKnife.bind(this);

        mFetchedBookmarks = false;
        mILXRequestor.getBookmarks(this, (ServerBookmarks b) ->
        {
            mFetchedBookmarks = true;
            // can only modify the # of tabs on the main thread :(
            // although we can do a one-time setup of the adapter in this callback
            setupViewPager(mViewPager, savedInstanceState);
        });

        mTabLayout.setupWithViewPager(mViewPager);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mToolbar.setTitle("ILX");
        mToolbar.setOnMenuItemClickListener((MenuItem item) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        });
        mToolbar.inflateMenu(R.menu.activity_main_tabs_menu);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, System.identityHashCode(this) + " resuming");
        //TODO check to see if we need to get rid of the bookmarks tab
        super.onResume();
    }

    private void setupViewPager(ViewPager viewPager, Bundle savedInstanceState) {

        Log.d(TAG, System.identityHashCode(this) + "setupViewPager");

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), mFloatingActionButton);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageSelected(int position) {
                if (position == boardsPosition()) {
                    setFabVisibility(true);
                } else {
                    setFabVisibility(false);
                }
            }

        });

        viewPager.setAdapter(adapter);
    }

    void setFabVisibility(boolean visible) {
        if (visible) {
            mFloatingActionButton.setAlpha(0f);
            mFloatingActionButton.setVisibility(FloatingActionButton.VISIBLE);
            mFloatingActionButton.animate().alpha(1f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(null);
        } else {
            mFloatingActionButton.setVisibility(FloatingActionButton.GONE);
        }
    }

    private boolean haveBookmarks() {

        if (!mFetchedBookmarks) {
            Log.d(TAG, "should never call haveBookmarks before finished fetching");
            return false;
        }

        if (mILXRequestor.getCachedBookmarks() != null) {
            return !mILXRequestor.getCachedBookmarks().getBookmarks().isEmpty();
        } else {
            return false;
        }
    }

    private int bookmarksPosition() {
        if (haveBookmarks()) {
            return 1;
        } else {
            return -1;
        }
    }

    private int snaPosition() {
        return 0;
    }

    private int boardsPosition() {
        if (haveBookmarks()) {
            return 2;
        } else {
            return 1;
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private static final String TAG = "ViewPagerAdapter";
        private FloatingActionButton mFloatingActionButton;

        public ViewPagerAdapter(FragmentManager manager, FloatingActionButton floatingActionButton) {
            super(manager);
            mFloatingActionButton = floatingActionButton;
        }

        @Override
        public Fragment getItem(int position) {

            if (position == bookmarksPosition()) {
                mFragmentList.add(ViewThreadsFragment.newInstance(false));
            } else if (position == snaPosition()) {
                mFragmentList.add(ViewThreadsFragment.newInstance(true));
            } else if (position == boardsPosition()) {
                mFragmentList.add(new ViewBoardsFragment());
            }

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            if (haveBookmarks()) {
                return 3;
            } else {
                return 2;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position == bookmarksPosition()) {
                return "Bookmarks";
            } else if (position == snaPosition()) {
                return "New Answers";
            } else {
                return "Boards";
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            if (position == boardsPosition()) { // TODO yuck
                mFloatingActionButton.setOnClickListener((View v) -> {
                    ((ViewBoardsFragment) fragment).toggleEditMode();
                });
            }

            return fragment;
        }
    }

}
