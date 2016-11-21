package com.theporouscity.flagging;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bergstroml on 2/26/16.
 */
public class GetItemsTask extends AsyncTask<String, Void, String[]> {

    private OkHttpClient mHttpClient;
    private Callback mCallback;
    private final String TAG = "GetItemsTask";

    @Override
    protected String[] doInBackground(String... urls) {
        String[] results = new String[urls.length];
        for (int i=0; i<urls.length; i++){
            String url = urls[i];
            Log.d(TAG, "asked for url " + url);
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = mHttpClient.newCall(request).execute();
                results[i] = response.body().string();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        return results;
    }

    @Override
    protected void onPostExecute(String[] results) {
        super.onPostExecute(results);
        if (results != null) {
            Log.d(TAG, "got some stuff back");
        } else {
            Log.d(TAG, "got nothing back");
        }

        try {
            mCallback.onComplete(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onComplete(String[] results) throws Exception;
    }

    GetItemsTask(OkHttpClient httpClient, Callback callback) {
        mHttpClient = httpClient;
        mCallback = callback;
    }
}
