package com.theporouscity.flagging.util;

import android.os.AsyncTask;
import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.theporouscity.flagging.ilx.ILXAccount;

import java.io.IOException;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bergstroml on 2/26/16.
 */
public class LoggedInRequestTask extends AsyncTask<String, Void, AsyncTaskResult<String>> {

    private OkHttpClient mHttpClient;
    private Callback mCallback;
    private ILXAccount mAccount;
    private final String TAG = "LoggedInRequestTask";

    @Override
    protected AsyncTaskResult<String> doInBackground(String... urls) {
        String resultString;

        if (mAccount.getSessionId() == null) {
            try {
                getSession();
            } catch (Exception e) {
                return new AsyncTaskResult<>(e);
            }
        }

        String url = urls[0];
        Log.d(TAG, "asked for url " + url);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = mHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                return new AsyncTaskResult<>(new ServerInaccessibleException("Server error"));
            }
            resultString = response.body().string();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return new AsyncTaskResult<>(e);
        }

        if (resultString.contains("Login Failed")) {
            ((ClearableCookieJar) mHttpClient.cookieJar()).clear();
            return new AsyncTaskResult<>(new CredentialsFailedException("Login Failed"));
        }

        for (Cookie cookie : mHttpClient.cookieJar().loadForRequest(HttpUrl.parse(url))) {
            if (cookie.name().contentEquals("JSESSIONID")) {
                mAccount.setSessionId(cookie.value());
            }
        }

        return new AsyncTaskResult<>(resultString);
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<String> result) {
        super.onPostExecute(result);

        try {
            mCallback.onComplete(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSession() throws CredentialsFailedException, ServerInaccessibleException {
        try {
            Request request = new Request.Builder().url(mAccount.getIndexUrl()).build();
            Response response = mHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                throw new ServerInaccessibleException("Problem getting login page");
            }
            String responseBody = response.body().string();
            if (responseBody.contains("Login Failed")) {
                throw new CredentialsFailedException("Username + password didn't work");
            }
            for (Cookie cookie : mHttpClient.cookieJar().loadForRequest(HttpUrl.parse(mAccount.getIndexUrl()))) {
                if (cookie.name().contentEquals("JSESSIONID")) {
                    mAccount.setSessionId(cookie.value());
                    return;
                }
            }
            throw new ServerInaccessibleException("Credentials worked but didn't get a session ID");
        } catch (IOException e) {
            throw new ServerInaccessibleException("Problem getting login page: " + e.toString());
        }

    }

    public interface Callback {
        void onComplete(AsyncTaskResult<String> result) throws Exception;
    }

    LoggedInRequestTask(ILXAccount account, OkHttpClient httpClient, Callback callback) {
        mAccount = account;
        mHttpClient = httpClient;
        mCallback = callback;
    }
}
