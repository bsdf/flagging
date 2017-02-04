package com.theporouscity.flagging;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.theporouscity.flagging.ilx.ILXAccount;
import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.UserAppSettings;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddEditAccountFragment extends Fragment {

    private static final String TAG = "AddEditAccountFragment";
    private static final String ARG_ACCOUNT = "ILXAccountArg";
    private static final String ARG_NOACCOUNTSYET = "ArgNoAccountsYet";

    @BindView(R.id.fragment_addedit_account_server_edittext)
    EditText mServerEditText;

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

    @Inject
    UserAppSettings mSettings;

    @Inject
    ILXRequestor mILXRequestor;

    private ILXAccount mAccount;
    private boolean mNoAccountsYet;

    public static AddEditAccountFragment newInstance(ILXAccount account) {
        AddEditAccountFragment fragment = new AddEditAccountFragment();

        Bundle args = new Bundle();
        if (account != null) {
            args.putParcelable(ARG_ACCOUNT, account);
        }
        args.putBoolean(ARG_NOACCOUNTSYET, false);
        fragment.setArguments(args);

        return fragment;
    }

    public static AddEditAccountFragment newInstance() {
        AddEditAccountFragment fragment = new AddEditAccountFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NOACCOUNTSYET, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        if (getArguments() != null) {
            mAccount = getArguments().getParcelable(ARG_ACCOUNT);
            mNoAccountsYet = getArguments().getBoolean(ARG_NOACCOUNTSYET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addedit_account, container, false);
        ButterKnife.bind(this, view);

        if (mAccount != null) {
            mServerEditText.setText(mAccount.getDomain());
            mInstanceEditText.setText(mAccount.getInstance());
            mUsernameEditText.setText(mAccount.getUsername());
            mPasswordEditText.setText(mAccount.getPassword());
        }

        mSaveTextView.setOnClickListener(this::handleSave);
        mCancelTextView.setOnClickListener(this::handleCancel);

        return view;
    }

    private void handleSave(View v) {

        String newServer = mServerEditText.getText().toString();
        String newInstance = mInstanceEditText.getText().toString();
        String newUsername = mUsernameEditText.getText().toString();
        String newPassword = mPasswordEditText.getText().toString();

        if (newServer.isEmpty() || newInstance.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Fields can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAccount != null) {
            if (mAccount.getDomain() == newServer &&
                    mAccount.getInstance() == newInstance &&
                    mAccount.getUsername() == newUsername &&
                    mAccount.getPassword() == newPassword) {
                getActivity().finish();
            }
        }

        final ILXAccount editedAccount = new ILXAccount(newServer, newInstance, newUsername, newPassword);

        mILXRequestor.login(getContext(), editedAccount, (AsyncTaskResult<Boolean> result) -> {
            if (result.getError() == null) {

                Toast.makeText(getContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();

                if (mAccount == null) {
                    mAccount = editedAccount;
                } else {
                    mAccount.setDomain(editedAccount.getDomain());
                    mAccount.setInstance(editedAccount.getInstance());
                    mAccount.setUsername(editedAccount.getUsername());
                    mAccount.setPassword(editedAccount.getPassword());
                }

                mILXRequestor.saveAccount(getContext(), mAccount);

                if (mNoAccountsYet) {
                    Intent i = new Intent(getContext(), ActivityMainTabs.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }

                getActivity().finish();

            } else {
                Log.d(TAG, result.getError().toString());
                Toast.makeText(getContext(), result.getError().getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void handleCancel(View v) {
        if (!mNoAccountsYet) {
            getActivity().finish();
        } else {
            Toast.makeText(getContext(), "Need an account to continue", Toast.LENGTH_SHORT).show();
        }
    }
}
