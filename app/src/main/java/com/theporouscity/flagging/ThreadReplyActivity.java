package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class ThreadReplyActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.board_id";
    public static final String EXTRA_THREAD_ID = "com.theporouscity.android.flagging.thread_id";
    public static final String EXTRA_THREAD_NAME = "com.theporouscity.android.flagging.thread_name";
    public static final String TAG = "ThreadReplyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ThreadReplyFragment fragment = (ThreadReplyFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            int boardId = getIntent().getIntExtra(EXTRA_BOARD_ID, -1);
            int threadId = getIntent().getIntExtra(EXTRA_THREAD_ID, -1);
            String threadName = getIntent().getStringExtra(EXTRA_THREAD_NAME);

            fragment = ThreadReplyFragment.newInstance(boardId, threadId, threadName);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, int boardId, int threadId, String threadName) {
        Intent intent = new Intent(packageContent, ThreadReplyActivity.class);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_THREAD_NAME, threadName);
        return intent;
    }
}
