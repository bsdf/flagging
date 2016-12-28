package com.theporouscity.flagging.util;

import android.os.AsyncTask;
import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.theporouscity.flagging.ilx.ILXAccount;

import java.io.IOException;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by bergstroml on 2/26/16.
 */
public class LoginTask extends AsyncTask<Void, Void, AsyncTaskResult<Boolean>> {

    private OkHttpClient mHttpClient;
    private Callback mCallback;
    private ILXAccount mAccount;
    private final String TAG = "LoginTask";

    @Override
    protected AsyncTaskResult<Boolean> doInBackground(Void... noArgs) {

        // first url passed should be login page, second should be login controller servlet

        try {
            Request request = new Request.Builder()
                    .url(mAccount.getLoginPageUrl())
                    .build();

            Response response = mHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                return new AsyncTaskResult<>(new ServerInaccessibleException("Problem reaching server"));
            }

            String loginContents = "username=" + mAccount.getUsername() +
                    "&password=" + mAccount.getPassword() + "&rememberMeCheckBox=true";

            MediaType text = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(text, loginContents);
            request = new Request.Builder()
                    .url(mAccount.getLoginControllerUrl())
                    .post(body)
                    .addHeader("Content-Length", Integer.toString(loginContents.length()))
                    .addHeader("Keep-Alive", "300")
                    .addHeader("Referer", mAccount.getLoginControllerUrl())
                    .build();

            response = mHttpClient.newCall(request).execute();

            String responseBody = response.body().string();
            if (responseBody.contains("exceeded")) {
                // TODO retry logging in?
                return new AsyncTaskResult<>(new ServerInaccessibleException("Timed out logging in, try again"));
            }
            if (responseBody.contains("failed")) {
                return new AsyncTaskResult<>(new CredentialsFailedException("Username and password didn't work."));
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return new AsyncTaskResult<>(e);
        }

        return new AsyncTaskResult<>(true);
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<Boolean> result) {
        super.onPostExecute(result);

        try {
            mCallback.onComplete(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onComplete(AsyncTaskResult<Boolean> result) throws Exception;
    }

    LoginTask(ILXAccount account, OkHttpClient httpClient, Callback callback) {
        mAccount = account;
        mHttpClient = httpClient;
        mCallback = callback;
    }
}
