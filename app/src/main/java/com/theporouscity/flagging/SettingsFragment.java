package com.theporouscity.flagging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.theporouscity.flagging.R;
import com.theporouscity.flagging.ilx.ILXServer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by bergstroml on 9/27/16.
 */

public class SettingsFragment extends Fragment {

    @BindView(R.id.fragment_settings_load_pics_never_button)
    RadioButton mLoadPicsNever;

    @BindView(R.id.fragment_settings_load_pics_always_button)
    RadioButton mLoadPicsAlways;

    @BindView(R.id.fragment_settings_load_pics_wifi_button)
    RadioButton mLoadPicsWifi;

    @BindView(R.id.fragment_settings_pretend_logged_in_no)
    RadioButton mPretendLoggedInNo;

    @BindView(R.id.fragment_settings_pretend_logged_in_yes)
    RadioButton mPretendLoggedInYes;

    @BindView(R.id.fragment_settings_accounts_recyclerview)
    RecyclerView mAccountsRecyclerView;

    @Inject
    UserAppSettings settings;

    ArrayList<ILXServer> mServers;

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

        mServers = settings.getServers(getContext());
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

        }
    }

    class AccountHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @BindView(R.id.list_item_account_instance_text_view)
        TextView mAccountInstanceTextView;

        private ILXServer mServer;

        public AccountHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindServer(ILXServer server) {
            mServer = server;
            mAccountInstanceTextView.setText(mServer.getInstance());
        }

        @Override
        public void onClick(View view) {
            //TODO
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

            View view = layoutInflater.inflate(R.layout.list_item_add_account, parent, false);
            return new AddNewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_ACCOUNT) {
                ((AccountHolder) holder).bindServer(mServers.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mServers.size()) {
                return TYPE_ADD_BUTTON;
            }
            return TYPE_ACCOUNT;
        }

        @Override
        public int getItemCount() {
            return mServers.size() + 1;
        }
    }

}
