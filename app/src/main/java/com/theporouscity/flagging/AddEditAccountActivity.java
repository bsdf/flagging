package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.theporouscity.flagging.ilx.ILXAccount;

public class AddEditAccountActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT = "ILXAccountExtra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        AddEditAccountFragment fragment = (AddEditAccountFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            ILXAccount account = getIntent().getParcelableExtra(EXTRA_ACCOUNT);
            fragment = AddEditAccountFragment.newInstance(account);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        setTitle("Edit Account");

    }

    public static Intent newIntent(Context packageContent, ILXAccount account) {
        Intent intent = new Intent(packageContent, AddEditAccountActivity.class);
        if (account != null) {
            intent.putExtra(EXTRA_ACCOUNT, account);
        }
        return intent;
    }
}
