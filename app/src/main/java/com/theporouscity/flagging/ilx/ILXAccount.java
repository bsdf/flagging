package com.theporouscity.flagging.ilx;

import android.app.Application;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.theporouscity.flagging.FlaggingApplication;
import com.theporouscity.flagging.di.ILXComponent;
import com.theporouscity.flagging.util.AccountCookiePersistor;

import java.util.UUID;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

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
    private String mSessionId = null;

    private OkHttpClient mAccountHttpClient = null;

    public OkHttpClient getHttpClient(Context context, OkHttpClient sharedClient) {
        if (mAccountHttpClient == null) {

            ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new AccountCookiePersistor(context, this));

            mAccountHttpClient = sharedClient.newBuilder()
                    .cookieJar(cookieJar)
                    .build();

            return mAccountHttpClient;
        }

        return mAccountHttpClient;
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

    public ILXAccount(String id, String server, String instance, String username, String password) {
        setId(id);
        setDomain(server);
        setInstance(instance);
        setUsername(username);
        setPassword(password);
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
        parcel.writeString(getSessionId());
    }

    public static final Parcelable.Creator<ILXAccount> CREATOR = new Parcelable.Creator<ILXAccount>() {
        public ILXAccount createFromParcel(Parcel in) { return new ILXAccount(in); }
        public ILXAccount[] newArray(int size) { return new ILXAccount[size]; }
    };

    private ILXAccount(Parcel parcel) {
        setId(parcel.readString());
        setDomain(parcel.readString());
        setInstance(parcel.readString());
        setUsername(parcel.readString());
        setPassword(parcel.readString());
        setSessionId(parcel.readString());
    }

    private String getBaseUrlPath() {
        return "http://" + getDomain() + "/" + getInstance();
    }

    public String getBoardsListUrl() {
        return getUrlHelper("/BoardsXmlControllerServlet");
    }

    public String getUpdatedThreadsUrl(int boardId) {
        return getUrlHelper("/NewAnswersControllerServlet?xml=true&boardid=" + Integer.toString(boardId));
    }

    public String getThreadUrl(int boardId, int threadId) {
        return getUrlHelper("/ThreadSelectedControllerServlet?xml=true&boardid="
            + Integer.toString(boardId) + "&threadid=" + Integer.toString(threadId));
    }

    public String getThreadHtmlUrl(int boardId, int threadId) {
        return getUrlHelper("/ThreadSelectedControllerServlet?boardid="
                + Integer.toString(boardId) + "&threadid=" + Integer.toString(threadId));
    }

    public String getSnaUrl() {
        return getUrlHelper("/SiteNewAnswersControllerServlet?xml=true");
    }

    public String getLoginPageUrl() {
        return getUrlHelper("/Pages/login.jsp");
    }

    public String getLoginControllerUrl() {
        return getUrlHelper("/LoginControllerServlet");
    }

    public String getIndexUrl() {
        return getUrlHelper("/index.jsp");
    }

    public String getBookmarksUrl() {
        return getUrlHelper("/BookmarksControllerServlet?xml=true");
    }

    private String getUrlHelper(String path) {
        return getBaseUrlPath() + path;
    }
}
