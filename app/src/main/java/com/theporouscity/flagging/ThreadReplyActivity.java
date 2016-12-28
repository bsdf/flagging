package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.theporouscity.flagging.ilx.Thread;

import org.parceler.Parcels;

public class ThreadReplyActivity extends AppCompatActivity {

    public static final String TAG = "ThreadReplyActivity";

    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.boardId";
    public static final String EXTRA_THREAD_ID = "com.theporouscity.android.flagging.threadId";
    public static final String EXTRA_THREAD_NAME = "com.theporouscity.android.flagging.threadName";
    public static final String EXTRA_MESSAGE_COUNT = "com.theporouscity.android.flagging.messageCount";
    public static final String EXTRA_SKEY = "com.theporouscity.android.flagging.skey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ThreadReplyFragment fragment = (ThreadReplyFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = ThreadReplyFragment.newInstance(
                    getIntent().getIntExtra(EXTRA_BOARD_ID, -1),
                    getIntent().getIntExtra(EXTRA_THREAD_ID, -1),
                    getIntent().getStringExtra(EXTRA_THREAD_NAME),
                    getIntent().getStringExtra(EXTRA_SKEY),
                    getIntent().getIntExtra(EXTRA_MESSAGE_COUNT, -1)
            );

            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, int boardId, int threadId, String threadName, String sKey, int messageCount) {
        Intent intent = new Intent(packageContent, ThreadReplyActivity.class);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_THREAD_NAME, threadName);
        intent.putExtra(EXTRA_SKEY, sKey);
        intent.putExtra(EXTRA_MESSAGE_COUNT, messageCount);
        return intent;
    }
}
