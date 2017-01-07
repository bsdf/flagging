package com.theporouscity.flagging;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.theporouscity.flagging.ilx.Bookmarks;
import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.UserAppSettings;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    private static final String MARKS_VAL = "haveBookmarks";

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

    @Inject
    UserAppSettings mSettings;

    private int mShortAnimationDuration;
    private boolean mHaveBookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mHaveBookmarks = savedInstanceState.getBoolean(MARKS_VAL);
        }

        setContentView(R.layout.activity_main_tabs);
        ((FlaggingApplication) getApplication()).getILXComponent().inject(this);
        ButterKnife.bind(this);

        mILXRequestor.getBookmarks(this, (AsyncTaskResult<Bookmarks> result) ->
        {
            if (result.getError() == null) {
                if (!result.getResult().getBookmarks().isEmpty()) {
                    mHaveBookmarks = true;
                } else {
                    mHaveBookmarks = false;
                }
            } else {
                Log.d(TAG, result.getError().toString());
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }

            setupViewPager(mViewPager);
            mTabLayout.setupWithViewPager(mViewPager);
        });

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mToolbar.setTitle(mILXRequestor.getCurrentInstanceName());
        mToolbar.setOnMenuItemClickListener((MenuItem item) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        });
        mToolbar.inflateMenu(R.menu.activity_main_tabs_menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((mViewPager.getChildCount() == 2 && mHaveBookmarks) ||
                mViewPager.getChildCount() == 3 && !mHaveBookmarks) {

            if (mViewPager.getAdapter() == null) {
                setupViewPager(mViewPager);
            } else {
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(MARKS_VAL, mHaveBookmarks);
    }

    // can only modify # of tabs on UI thread
    private void setupViewPager(ViewPager viewPager) {

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

    private int bookmarksPosition() {
        if (mHaveBookmarks) {
            return 1;
        } else {
            return -1;
        }
    }

    private int snaPosition() {
        return 0;
    }

    private int boardsPosition() {
        if (mHaveBookmarks) {
            return 2;
        } else {
            return 1;
        }
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragmentList = new ArrayList<>();
        private static final String TAG = "ViewPagerAdapter";
        private FloatingActionButton mFloatingActionButton;

        public ViewPagerAdapter(FragmentManager manager, FloatingActionButton floatingActionButton) {
            super(manager);
            mFloatingActionButton = floatingActionButton;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment theFragment = null;

            if (position == bookmarksPosition()) {
                theFragment = ViewThreadsFragment.newInstance(false);
                mFragmentList.add(theFragment);
            } else if (position == snaPosition()) {
                theFragment = ViewThreadsFragment.newInstance(true);
                mFragmentList.add(theFragment);
            } else if (position == boardsPosition()) {
                theFragment = new ViewBoardsFragment();
                mFragmentList.add(theFragment);
            }

            return theFragment;
        }

        @Override
        public int getCount() {
            if (mHaveBookmarks) {
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

        @Override
        public int getItemPosition(Object object) {

            // we only get here after notifyDatasetChanged so we know tabs need to change
            // inefficient but this (having bookmarks <-> not having them) is rare

            mFragmentList = new ArrayList<>();
            return POSITION_NONE;
        }
    }

}
