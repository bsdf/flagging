package com.theporouscity.flagging;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddEditAccountFragment extends Fragment {

    @BindView(R.id.fragment_addedit_account_server_edittext)
    EditText mDomainEditText;

    @BindView(R.id.fragment_addedit_account_instance_edittext)
    EditText mInstanceEditText;

    @BindView(R.id.fragment_addedit_account_username_edittext)
    EditText mUsernameEditText;

    @BindView(R.id.fragment_addedit_account_password_edittext)
    EditText mPasswordEditText;

    @BindView(R.id.fragment_addedit_account_save_textview)
    TextView mSaveTextView;

    @BindView(R.id.fragment_addedit_account_cancel_textview)
    TextView mCancelTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addedit_account, container, false);
        ButterKnife.bind(this, view);

        // do stuff

        return view;
    }

}
