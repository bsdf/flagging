package com.theporouscity.flagging;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.theporouscity.flagging.ilx.ILXAccount;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddEditAccountFragment extends Fragment {

    private static final String TAG = "AddEditAccountFragment";
    private static final String ARG_ACCOUNT = "ILXAccountArg";

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

    public static AddEditAccountFragment newInstance(ILXAccount account) {
        AddEditAccountFragment fragment = new AddEditAccountFragment();

        if (account != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_ACCOUNT, account);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        if (getArguments() != null) {
            mAccount = getArguments().getParcelable(ARG_ACCOUNT);
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

        String newServer = mServerEditText.toString();
        String newInstance = mInstanceEditText.toString();
        String newUsername = mUsernameEditText.toString();
        String newPassword = mPasswordEditText.toString();

        if (mAccount.getDomain() == newServer &&
                mAccount.getInstance() == newInstance &&
                mAccount.getUsername() == newUsername &&
                mAccount.getPassword() == newPassword) {
            getActivity().finish();
        }

        if (newServer.isEmpty() || newInstance.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Fields can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ILXAccount newAccount = new ILXAccount(newServer, newInstance, newUsername, newPassword);
        String error = mILXRequestor.login(newAccount);
        if (error != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
        mILXRequestor.saveAccount(newAccount);
        getActivity().finish();

    }

    private void handleCancel(View v) {
        getActivity().finish();
    }
}
