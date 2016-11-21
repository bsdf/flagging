package com.theporouscity.flagging;

import android.support.v4.app.Fragment;

public class ViewBoardsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return ViewBoardsFragment.newInstance();
    }

}
