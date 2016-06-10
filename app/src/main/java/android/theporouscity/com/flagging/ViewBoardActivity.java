package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by bergstroml on 6/6/16.
 */

public class ViewBoardActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD_ID = "com.theporouscity.android.flagging.boardId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ViewBoardFragment fragment = (ViewBoardFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            int boardId = (int) getIntent().getSerializableExtra(EXTRA_BOARD_ID);
            fragment = ViewBoardFragment.newInstance(boardId);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, int boardId) {
        Intent intent = new Intent(packageContent, ViewBoardActivity.class);
        intent.putExtra(EXTRA_BOARD_ID, boardId);
        return intent;
    }

}
