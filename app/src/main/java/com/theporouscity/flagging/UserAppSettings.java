package com.theporouscity.flagging;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.theporouscity.flagging.ilx.Board;

/**
 * Created by bergstroml on 9/28/16.
 */

public class UserAppSettings {

    private final static String SETTINGS_TAG = "FlaggingSettings";

    public UserAppSettings(Context context) {

        SharedPreferences preferences = getPreferences(context);

        int loadPrettyPictures = preferences.getInt(UserAppSettings.LoadPrettyPicturesSettingKey, -1);
        if (loadPrettyPictures == 0) {
            setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.NEVER);
        } else if (loadPrettyPictures == 1) {
            setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.ALWAYS);
        } else if (loadPrettyPictures == 2 || loadPrettyPictures == -1) {
            setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.WIFI);
        }

        int pretendToBeLoggedIn = preferences.getInt(UserAppSettings.PretendToBeLoggedInKey, -1);
        if (pretendToBeLoggedIn == 1) {
            setPretendToBeLoggedInSetting(true);
        } else {
            setPretendToBeLoggedInSetting(false);
        }
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(SETTINGS_TAG, Context.MODE_PRIVATE);
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
        persistUserAppSettings(context);
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
        persistUserAppSettings(context);
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

    // TODO remove once we have real bookmarks
    public String getString(String key, Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(key, null);
    }

    public void persistString(String key, String value, Context context) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean getBoardEnabled(Board board, Context context) {
        SharedPreferences preferences = getPreferences(context);
        int enabled = preferences.getInt(getBoardEnabledKey(board), -1);
        if (enabled == -1) {
            return true;
        } else if (enabled == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void persistBoardEnabledState(Board board, Context context) {
        SharedPreferences preferences = getPreferences(context);

        int enabled;

        if (board.isEnabled()) {
            enabled = 1;
        } else {
            enabled = 0;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getBoardEnabledKey(board), enabled);
        editor.apply();
    }

    private String getBoardEnabledKey(Board board) {
        return "board_enabled_" + Integer.toString(board.getId());
    }

    public void persistUserAppSettings(Context context) {

        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        int newLoadPrettyPicturesSetting;
        if (getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.ALWAYS) {
            newLoadPrettyPicturesSetting = 1;
        } else if (getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.WIFI) {
            newLoadPrettyPicturesSetting = 2;
        } else {
            newLoadPrettyPicturesSetting = 0;
        }
        editor.putInt(UserAppSettings.LoadPrettyPicturesSettingKey, newLoadPrettyPicturesSetting);

        int newPretendToBeLoggedInSetting;
        if (getPretendToBeLoggedInSetting()) {
            newPretendToBeLoggedInSetting = 1;
        } else {
            newPretendToBeLoggedInSetting = 0;
        }
        editor.putInt(UserAppSettings.PretendToBeLoggedInKey, newPretendToBeLoggedInSetting);

        editor.apply();

    }
}
