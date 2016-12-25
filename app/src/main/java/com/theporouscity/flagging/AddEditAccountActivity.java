package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.theporouscity.flagging.ilx.ILXAccount;

public class AddEditAccountActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT = "ILXAccountExtra";
    public static final String EXTRA_NOACCOUNTSYET = "NoAccountsYet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        AddEditAccountFragment fragment = (AddEditAccountFragment) fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            ILXAccount account = getIntent().getParcelableExtra(EXTRA_ACCOUNT);
            if (getIntent().getBooleanExtra(EXTRA_NOACCOUNTSYET, false)) {
                fragment = AddEditAccountFragment.newInstance();
            } else {
                fragment = AddEditAccountFragment.newInstance(account);
            }
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

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AddEditAccountActivity.class);
        intent.putExtra(EXTRA_NOACCOUNTSYET, true);
        return intent;
    }
}