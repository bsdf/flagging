package com.theporouscity.flagging.ilx;

/**
 * Created by bergstroml on 12/1/16.
 */

public class ILXServer {

    private String mDomain;
    private String mInstance;
    private String mUsername;
    private String mPassword; // I dunno, is keeping this unencrypted in memory all the time ... bad?

    public ILXServer(String domain, String instance, String username, String password) {
        mDomain = domain.toLowerCase();

        if (mDomain.startsWith("http://")) {
            mDomain = mDomain.substring(7);
        } else if (mDomain.startsWith("https://")) {
            mDomain = mDomain.substring(8);
        }

        if (mDomain.startsWith("www.")) {
            mDomain = mDomain.substring(4);
        }

        mInstance = instance;
        mUsername = username;
        mPassword = password;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getInstance() {
        return mInstance;

    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setPassword(String password) {
        mPassword = password;
    }
}
