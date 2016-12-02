package com.theporouscity.flagging.ilx;

import android.util.Log;

import java.security.KeyStore;

/**
 * Created by bergstroml on 11/29/16.
 */

public class ILXAccount {

    private final static String TAG = "ILXAccount";
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
        mDomain = domain;
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
}
