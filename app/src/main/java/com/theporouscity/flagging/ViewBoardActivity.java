package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import com.theporouscity.flagging.R;

import com.theporouscity.flagging.ilx.Board;

import org.parceler.Parcels;

/**
 * Created by bergstroml on 6/6/16.
 */

public class ViewBoardActivity extends AppCompatActivity {

    public static final String EXTRA_BOARD = "com.theporouscity.android.flagging.board";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        ViewThreadsFragment fragment = (ViewThreadsFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            Board board = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_BOARD));
            fragment = ViewThreadsFragment.newInstance(board);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public static Intent newIntent(Context packageContent, Board board) {
        Intent intent = new Intent(packageContent, ViewBoardActivity.class);
        intent.putExtra(EXTRA_BOARD, Parcels.wrap(board));
        return intent;
    }

}
