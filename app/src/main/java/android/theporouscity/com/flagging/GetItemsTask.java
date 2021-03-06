package android.theporouscity.com.flagging;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bergstroml on 2/26/16.
 */
public class GetItemsTask extends AsyncTask<String, Void, String> {

    private OkHttpClient mHttpClient;
    private Callback mCallback;
    private final String TAG = "GetItemsTask";

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        Log.d(TAG, "asked for url " + url);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = mHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            Log.d(TAG, "got some stuff back");
        } else {
            Log.d(TAG, "got nothing back");
        }

        try {
            mCallback.onComplete(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onComplete(String result) throws Exception;
    }

    GetItemsTask(OkHttpClient httpClient, Callback callback) {
        mHttpClient = httpClient;
        mCallback = callback;
    }
}
