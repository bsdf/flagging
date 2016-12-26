package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.theporouscity.flagging.ilx.Thread;

import org.parceler.Parcels;

public class ThreadReplyActivity extends AppCompatActivity {

    public static final String EXTRA_THREAD = "com.theporouscity.android.flagging.thread";
    public static final String TAG = "ThreadReplyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ThreadReplyFragment fragment = (ThreadReplyFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            Thread thread = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_THREAD));

            fragment = ThreadReplyFragment.newInstance(thread);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, Thread thread) {
        Intent intent = new Intent(packageContent, ThreadReplyActivity.class);
        intent.putExtra(EXTRA_THREAD, Parcels.wrap(thread));
        return intent;
    }
}
