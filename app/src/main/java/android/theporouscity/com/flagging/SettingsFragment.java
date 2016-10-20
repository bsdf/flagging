package android.theporouscity.com.flagging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

/**
 * Created by bergstroml on 9/27/16.
 */

public class SettingsFragment extends Fragment {

    private RadioButton mLoadPicsNever;
    private RadioButton mLoadPicsAlways;
    private RadioButton mLoadPicsWifi;

    private RadioButton mPretendLoggedInNo;
    private RadioButton mPretendLoggedInYes;

    private UserAppSettings mSettings;

    public SettingsFragment() {
        mSettings = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mLoadPicsNever = (RadioButton) view.findViewById(R.id.fragment_settings_load_pics_never_button);
        mLoadPicsAlways = (RadioButton) view.findViewById(R.id.fragment_settings_load_pics_always_button);
        mLoadPicsWifi = (RadioButton) view.findViewById(R.id.fragment_settings_load_pics_wifi_button);

        mPretendLoggedInNo = (RadioButton) view.findViewById(R.id.fragment_settings_pretend_logged_in_no);
        mPretendLoggedInYes = (RadioButton) view.findViewById(R.id.fragment_settings_pretend_logged_in_yes);

        updateSettingsUI();

        return view;

    }

    private UserAppSettings getSettings() {
        if (mSettings == null) {
            mSettings = ILXRequestor.getILXRequestor().getUserAppSettings(getContext());
        }
        return mSettings;
    }

    private void updateSettingsUI() {

        UserAppSettings settings = getSettings();

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

        UserAppSettings settings = getSettings();

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

        UserAppSettings settings = getSettings();

        if (mPretendLoggedInNo.isChecked()) {
            settings.setPretendToBeLoggedInSettingAndPersist(false, getContext());
        } else if (mPretendLoggedInYes.isChecked()) {
            settings.setPretendToBeLoggedInSettingAndPersist(true, getContext());
        }
    }


}
