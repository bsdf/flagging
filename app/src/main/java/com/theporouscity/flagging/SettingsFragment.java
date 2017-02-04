package com.theporouscity.flagging;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.theporouscity.flagging.ilx.ILXAccount;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.UserAppSettings;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by bergstroml on 9/27/16.
 */

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    @BindView(R.id.fragment_settings_load_pics_never_button)
    RadioButton mLoadPicsNever;

    @BindView(R.id.fragment_settings_load_pics_always_button)
    RadioButton mLoadPicsAlways;

    @BindView(R.id.fragment_settings_load_pics_wifi_button)
    RadioButton mLoadPicsWifi;

    @BindView(R.id.fragment_settings_accounts_recyclerview)
    RecyclerView mAccountsRecyclerView;

    @Inject
    UserAppSettings settings;

    @Inject
    ILXRequestor ilxRequestor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        mAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateSettingsUI();

        return view;
    }

    private void updateSettingsUI() {
        updateLoadPrettyPicturesButtons(mLoadPicsNever,
                settings.getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.NEVER);
        updateLoadPrettyPicturesButtons(mLoadPicsAlways,
                settings.getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.ALWAYS);
        updateLoadPrettyPicturesButtons(mLoadPicsWifi,
                settings.getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.WIFI);

        mAccountsRecyclerView.setAdapter(new AccountAdapter());
    }

    private void updateLoadPrettyPicturesButtons(RadioButton button, boolean checked) {
        button.setOnCheckedChangeListener(null);
        button.setChecked(checked);
        button.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) -> {
           updateLoadPicsSetting();
        });
    }

    private void updateLoadPicsSetting() {
        if (mLoadPicsNever.isChecked()) {
            settings.setLoadPrettyPicturesSettingAndPersist(UserAppSettings.LoadPrettyPicturesSetting.NEVER, getContext());
        } else if (mLoadPicsAlways.isChecked()) {
            settings.setLoadPrettyPicturesSettingAndPersist(UserAppSettings.LoadPrettyPicturesSetting.ALWAYS, getContext());
        } else if (mLoadPicsWifi.isChecked()) {
            settings.setLoadPrettyPicturesSettingAndPersist(UserAppSettings.LoadPrettyPicturesSetting.WIFI, getContext());
        }

    }

    class AddNewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public AddNewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getContext(), "Add a new account", Toast.LENGTH_SHORT).show();
        }
    }

    class AccountHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.list_item_account_instance_text_view)
        TextView mAccountInstanceTextView;

        private ILXAccount mAccount;

        public AccountHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindAccount(ILXAccount account) {
            mAccount = account;
            mAccountInstanceTextView.setText(mAccount.getInstance());
        }

        @Override
        public void onClick(View view) {
            Intent intent = AddEditAccountActivity.newIntent(getContext(), mAccount);
            startActivity(intent);
        }
    }

    class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_ACCOUNT = 0;
        private final static int TYPE_ADD_BUTTON = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == TYPE_ACCOUNT) {
                View view = layoutInflater.inflate(R.layout.list_item_account, parent, false);
                return new AccountHolder(view);
            }

            Log.d(TAG, "creating add account item");
            View view = layoutInflater.inflate(R.layout.list_item_add_account, parent, false);
            return new AddNewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_ACCOUNT) {
                ((AccountHolder) holder).bindAccount(settings.getAccounts(getContext()).get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == settings.getAccounts(getContext()).size()) {
                return TYPE_ADD_BUTTON;
            }
            return TYPE_ACCOUNT;
        }

        @Override
        public int getItemCount() {
            return settings.getAccounts(getContext()).size() + 1;
        }
    }

}
