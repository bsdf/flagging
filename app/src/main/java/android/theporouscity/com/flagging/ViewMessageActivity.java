package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.theporouscity.com.flagging.ilx.Message;

import java.util.List;

/**
 * Created by bergstroml on 7/21/16.
 */

public class ViewMessageActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.theporouscity.android.flagging.message";
    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.board_id";
    public static final String EXTRA_THREAD_ID = "com.theporouscity.android.flagging.thread_id";
    public static final String EXTRA_THREAD_NAME = "com.theporouscity.android.flagging.thread_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ViewMessageFragment fragment = (ViewMessageFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            Message message = getIntent().getParcelableExtra(EXTRA_MESSAGE);
            int boardId = getIntent().getIntExtra(EXTRA_BOARD_ID, -1);
            int threadId = getIntent().getIntExtra(EXTRA_THREAD_ID, -1);
            String threadName = getIntent().getStringExtra(EXTRA_THREAD_NAME);
            fragment = ViewMessageFragment.newInstance(message, boardId, threadId, threadName);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, Message message, int boardId, int threadId, String threadName) {
        Intent intent = new Intent(packageContent, ViewMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_THREAD_NAME, threadName);
        return intent;
    }

    @Override
    public void onBackPressed() {
        boolean handled = false;
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            //TODO: Perform your logic to pass back press here
            for(Fragment fragment : fragmentList){
                if(fragment instanceof OnBackPressedListener){
                    if(((OnBackPressedListener)fragment).onBackPressed()){
                        handled = true;
                    }
                }
            }
        }
        if (!handled) {
            super.onBackPressed();
        }
    }
}
