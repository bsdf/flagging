package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.theporouscity.com.flagging.ilx.Board;

/**
 * Created by bergstroml on 6/15/16.
 */

public class ViewThreadActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.board_id";
    public static final String EXTRA_THREAD_ID = "com.theporouscity.android.flagging.thread_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ViewThreadFragment fragment = (ViewThreadFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            int boardId = getIntent().getIntExtra(EXTRA_BOARD_ID, -1);
            int threadId = getIntent().getIntExtra(EXTRA_THREAD_ID, -1);
            fragment = ViewThreadFragment.newInstance(boardId, threadId);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, int boardId, int threadId) {
        Intent intent = new Intent(packageContent, ViewThreadActivity.class);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        return intent;
    }
}
