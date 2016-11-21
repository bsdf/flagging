package com.theporouscity.flagging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import com.theporouscity.flagging.R;

/**
 * Created by bergstroml on 9/27/16.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        SettingsFragment fragment = (SettingsFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new SettingsFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        setTitle("Settings");
    }

}
