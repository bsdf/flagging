package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by bergstroml on 6/15/16.
 */

public class ViewThreadActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.board_id";
    public static final String EXTRA_THREAD_ID = "com.theporouscity.android.flagging.thread_id";
    public static final String EXTRA_MESSAGE_ID = "com.theporouscity.android.flagging.message_id";
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ViewThreadFragment fragment = (ViewThreadFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            int boardId = getIntent().getIntExtra(EXTRA_BOARD_ID, -1);
            int threadId = getIntent().getIntExtra(EXTRA_THREAD_ID, -1);
            int messageId = getIntent().getIntExtra(EXTRA_MESSAGE_ID, -1);
            fragment = ViewThreadFragment.newInstance(boardId, threadId, messageId);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, int boardId, int threadId, int messageId) {
        Intent intent = new Intent(packageContent, ViewThreadActivity.class);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        return intent;
    }

    public static Intent newIntent(Context packageContent, String threadUrl) {
        Intent intent = new Intent(packageContent, ViewThreadActivity.class);

        ILXUrlParser.ilxIds ids = ILXUrlParser.getIds(threadUrl);

        if (ids != null) {
            intent.putExtra(EXTRA_BOARD_ID, ids.boardId);
            intent.putExtra(EXTRA_THREAD_ID, ids.threadId);
            intent.putExtra(EXTRA_MESSAGE_ID, ids.messageId);
            return intent;
        } else {
            return null;
        }
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (toolbar != null) {
            mToolbar = toolbar;
            super.setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setElevation(0);
        }
    }

    @Override
    public void setTitle(CharSequence title) {

        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.fragment_view_thread_toolbar);
            setSupportActionBar(mToolbar);
        }

        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }
}
