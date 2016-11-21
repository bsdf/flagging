package com.theporouscity.flagging;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by bergstroml on 9/28/16.
 */

public class UserAppSettings {

    private ILXRequestor mILXRequestor;

    public UserAppSettings(ILXRequestor mILXRequestor) {
        this.mILXRequestor = mILXRequestor;
    }

    public static final String LoadPrettyPicturesSettingKey = "load pretty pictures";
    public enum LoadPrettyPicturesSetting {
        NEVER,
        ALWAYS,
        WIFI
    }
    private LoadPrettyPicturesSetting mLoadPrettyPicturesSetting;

    public static final String PretendToBeLoggedInKey = "pretend to be logged in";
    private boolean mPretendToBeLoggedInSetting;

    public LoadPrettyPicturesSetting getLoadPrettyPicturesSetting() {
        return mLoadPrettyPicturesSetting;
    }

    public void setLoadPrettyPicturesSettingAndPersist(LoadPrettyPicturesSetting loadPrettyPicturesSetting, Context context) {
        mLoadPrettyPicturesSetting = loadPrettyPicturesSetting;
        mILXRequestor.persistUserAppSettings(this, context);
    }

    public void setLoadPrettyPicturesSetting(LoadPrettyPicturesSetting loadPrettyPicturesSetting) {
        mLoadPrettyPicturesSetting = loadPrettyPicturesSetting;
    }

    public boolean getPretendToBeLoggedInSetting() {
        return mPretendToBeLoggedInSetting;
    }

    public void setPretendToBeLoggedInSetting(boolean pretendToBeLoggedInSetting) {
        mPretendToBeLoggedInSetting = pretendToBeLoggedInSetting;
    }

    public void setPretendToBeLoggedInSettingAndPersist(boolean pretendToBeLoggedInSetting, Context context) {
        mPretendToBeLoggedInSetting = pretendToBeLoggedInSetting;
        mILXRequestor.persistUserAppSettings(this, context);
    }

    public boolean shouldLoadPictures(Context context) {
        if (mLoadPrettyPicturesSetting == LoadPrettyPicturesSetting.ALWAYS) {
            return true;
        } else if (mLoadPrettyPicturesSetting == LoadPrettyPicturesSetting.WIFI) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }
}
