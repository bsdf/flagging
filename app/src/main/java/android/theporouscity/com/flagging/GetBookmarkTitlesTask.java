package android.theporouscity.com.flagging;

import android.os.AsyncTask;
import android.theporouscity.com.flagging.ilx.Bookmark;
import android.theporouscity.com.flagging.ilx.ServerBookmarks;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bergstroml on 2/26/16.
 */
public class GetBookmarkTitlesTask extends AsyncTask<ServerBookmarks, Void, ArrayList> {

    private OkHttpClient mHttpClient;
    private Callback mCallback;
    private final String TAG = "GetItemsTask";

    @Override
    protected ArrayList<Bookmark> doInBackground(ServerBookmarks... bookmarkses) {
        ServerBookmarks bookmarks = bookmarkses[0];
        ArrayList<Bookmark> titles = new ArrayList<>();

        Pattern pattern = Pattern.compile("<Title><![CDATA[(.*)]]></Title>");

        for (HashMap.Entry<Integer, HashMap<Integer, Bookmark>> boardBookmarks: bookmarks.getBookmarks().entrySet()) {
            for (HashMap.Entry<Integer, Bookmark> bookmarkEntry : boardBookmarks.getValue().entrySet()) {
                Bookmark bookmark = bookmarkEntry.getValue();
                try {
                    String url = "http://ilxor.com/ILX/ThreadSelectedControllerServlet?xml=true&boardid=" +
                            Integer.toString(bookmark.getBoardId()) + "&threadid=" +
                            Integer.toString(bookmark.getThreadId());
                    Log.d(TAG, "asking for url " + url);
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = mHttpClient.newCall(request).execute();
                    Matcher matcher = pattern.matcher(response.body().string());

                    if (matcher.matches()) {
                        Bookmark newBookmark = new Bookmark(bookmark.getBoardId(),
                                bookmark.getThreadId(), bookmark.getBookmarkedMessageId());
                        newBookmark.setBookmarkThreadTitle(matcher.group(1));
                        titles.add(newBookmark);
                    }

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
            }
        }

        return titles;
    }

    @Override
    protected void onPostExecute(ArrayList result) {
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
        void onComplete(ArrayList<Bookmark> result) throws Exception;
    }

    GetBookmarkTitlesTask(OkHttpClient httpClient, Callback callback) {
        mHttpClient = httpClient;
        mCallback = callback;
    }
}
