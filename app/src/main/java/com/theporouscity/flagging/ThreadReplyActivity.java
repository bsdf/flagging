package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadReplyActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.board_id";
    public static final String EXTRA_THREAD_ID = "com.theporouscity.android.flagging.thread_id";
    public static final String EXTRA_THREAD_NAME = "com.theporouscity.android.flagging.thread_name";
    public static final String TAG = "ThreadReplyActivity";

    @BindView(R.id.thread_reply_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.thread_reply_fab)
    FloatingActionButton mReplyFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_reply);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        int boardId = getIntent().getIntExtra(EXTRA_BOARD_ID, -1);
        int threadId = getIntent().getIntExtra(EXTRA_THREAD_ID, -1);
        String threadName = getIntent().getStringExtra(EXTRA_THREAD_NAME);

        mReplyFab.setOnClickListener(view -> {
            Snackbar.make(view, "" + threadName, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    public static Intent newIntent(Context packageContent, int boardId, int threadId, String threadName) {
        Intent intent = new Intent(packageContent, ThreadReplyActivity.class);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_THREAD_NAME, threadName);
        return intent;
    }

}
