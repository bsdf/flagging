package com.theporouscity.flagging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.UserAppSettings;

import javax.inject.Inject;

public class StartActivity extends AppCompatActivity {

    @Inject
    ILXRequestor mIlxRequestor;

    @Inject
    UserAppSettings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getApplication()).getILXComponent().inject(this);
        Intent i;
        if (mSettings.getAccounts(this) == null || mSettings.getAccounts(this).isEmpty()) {
            i = AddEditAccountActivity.newIntent(this);
        } else {
            i = new Intent(getApplicationContext(), ActivityMainTabs.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
