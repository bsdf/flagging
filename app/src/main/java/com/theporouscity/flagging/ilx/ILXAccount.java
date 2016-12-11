package com.theporouscity.flagging.ilx;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.security.KeyStore;
import java.util.UUID;

/**
 * Created by bergstroml on 11/29/16.
 */

public class ILXAccount implements Parcelable {

    private final static String TAG = "ILXAccount";
    private String mId;
    private String mDomain;
    private String mInstance;
    private String mUsername;
    private String mPassword;
    private String mLoginCookies;
    private String mSessionId;

    public String getLoginCookies() {
        return mLoginCookies;
    }

    public void setLoginCookies(String loginCookies) {
        mLoginCookies = loginCookies;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String domain) {
        mDomain = domain.toLowerCase();

        if (mDomain.startsWith("http://")) {
            mDomain = mDomain.substring(7);
        } else if (mDomain.startsWith("https://")) {
            mDomain = mDomain.substring(8);
        }

        if (mDomain.startsWith("www.")) {
            mDomain = mDomain.substring(4);
        }
    }

    public String getInstance() {
        return mInstance;
    }

    public void setInstance(String instance) {
        mInstance = instance;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public ILXAccount() { }

    public ILXAccount(String server, String instance, String username, String password) {
        setId(UUID.randomUUID().toString());
        setDomain(server);
        setInstance(instance);
        setUsername(username);
        setPassword(password);
    }

    public ILXAccount(String id, String server, String instance, String username, String password,
                      String loginCookies) {
        setId(id);
        setDomain(server);
        setInstance(instance);
        setUsername(username);
        setPassword(password);
        setLoginCookies(loginCookies);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getId());
        parcel.writeString(getDomain());
        parcel.writeString(getInstance());
        parcel.writeString(getUsername());
        parcel.writeString(getPassword());
        parcel.writeString(getLoginCookies());
        parcel.writeString(getSessionId());
    }

    public static final Parcelable.Creator<ILXAccount> CREATOR = new Parcelable.Creator<ILXAccount>() {
        public ILXAccount createFromParcel(Parcel in) { return new ILXAccount(in); }
        public ILXAccount[] newArray(int size) { return new ILXAccount[size]; }
    }

    private ILXAccount(Parcel parcel) {
        setId(parcel.readString());
        setDomain(parcel.readString());
        setInstance(parcel.readString());
        setUsername(parcel.readString());
        setPassword(parcel.readString());
        setLoginCookies(parcel.readString());
        setSessionId(parcel.readString());
    }
}
