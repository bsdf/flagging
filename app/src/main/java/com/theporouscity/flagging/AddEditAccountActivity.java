package com.theporouscity.flagging;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AddEditAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        AddEditAccountFragment fragment = (AddEditAccountFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new AddEditAccountFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        setTitle("Add or Edit Account");
    }
}
