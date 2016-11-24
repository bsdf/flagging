package com.theporouscity.flagging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.theporouscity.flagging.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

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

    @Inject
    UserAppSettings settings;

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

        boolean notLoggedIn = !settings.getPretendToBeLoggedInSetting();

        updatePretendLoggedInButtons(mPretendLoggedInNo, notLoggedIn);
        updatePretendLoggedInButtons(mPretendLoggedInYes, settings.getPretendToBeLoggedInSetting());
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

    private void updatePretendLoggedInButtons(RadioButton button, boolean checked) {
        button.setOnCheckedChangeListener(null);
        button.setChecked(checked);
        button.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) -> {
            updatePretendLoggedInSetting();
        });
    }

    private void updatePretendLoggedInSetting() {
        if (mPretendLoggedInNo.isChecked()) {
            settings.setPretendToBeLoggedInSettingAndPersist(false, getContext());
        } else if (mPretendLoggedInYes.isChecked()) {
            settings.setPretendToBeLoggedInSettingAndPersist(true, getContext());
        }
    }


}
