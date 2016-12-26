package com.theporouscity.flagging;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.theporouscity.flagging.ilx.Board;
import com.theporouscity.flagging.ilx.Boards;
import com.theporouscity.flagging.ilx.Bookmark;
import com.theporouscity.flagging.ilx.Message;
import com.theporouscity.flagging.ilx.RecentlyUpdatedThreads;
import com.theporouscity.flagging.ilx.ServerBookmarks;
import com.theporouscity.flagging.ilx.Thread;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.transform.Transform;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by bergstroml on 2/26/16.
 */
public class ILXRequestor {

    public static final String ILX_SERVER_TAG = "ILX";

    private static final String TAG = "ILXRequestor";
    private static final String boardsListUrl = "http://ilxor.com/ILX/BoardsXmlControllerServlet";
    private static final String updatedThreadsUrlBase = "http://ilxor.com/ILX/NewAnswersControllerServlet?xml=true";
    private static final String threadUrlBase = "http://ilxor.com/ILX/ThreadSelectedControllerServlet?xml=true";
    private static final String snaUrl = "http://ilxor.com/ILX/SiteNewAnswersControllerServlet?xml=true";
    private static final String newAnswerUrl = "http://ilxor.com/ILX/NewAnswerControllerServlet";
    private static final String serializedBookmarksPrefix = "serializedBookmarks";

    private OkHttpClient mHttpClient;
    private Serializer mSerializer;
    private volatile Boards mBoards;
    private SharedPreferences mPreferences;
    private Map<String, ServerBookmarks> mServersBookmarks;
    private List<BookmarksCallback> mBookmarksCallbacks = new ArrayList<>();

    public ILXRequestor(OkHttpClient mHttpClient, Serializer mSerializer, SharedPreferences mPreferences) {
        this.mHttpClient = mHttpClient;
        this.mSerializer = mSerializer;
        this.mPreferences = mPreferences;
    }

    private String getServerTag() {
        return ILX_SERVER_TAG;
    }

    public boolean getBookmarks(Context context, BookmarksCallback bookmarksCallback) {

        mBookmarksCallbacks.add(bookmarksCallback);
        if (mBookmarksCallbacks.size() > 1) {
            return true;
        }

        if (mServersBookmarks == null) {
            mServersBookmarks = new HashMap<>();
        }

        if (mServersBookmarks.get(getServerTag()) != null) {
            ArrayList<String> bookmarkThreadUrls = new ArrayList<>();
            ArrayList<Bookmark> bookmarksForThreads = new ArrayList<>();
            ServerBookmarks bookmarks = mServersBookmarks.get(getServerTag());
            boolean allBookmarksHaveThreads = true;
            for (HashMap.Entry<Integer, HashMap<Integer, Bookmark>> boardBookmarks: bookmarks.getBookmarks().entrySet()) {
                for (HashMap.Entry<Integer, Bookmark> bookmarkEntry : boardBookmarks.getValue().entrySet()) {
                    Bookmark bookmark = bookmarkEntry.getValue();
                    if (bookmark.getCachedThread() == null) {
                        allBookmarksHaveThreads = false;
                        bookmarksForThreads.add(bookmark);

                        String url = Uri.parse(threadUrlBase).buildUpon()
                                .appendQueryParameter("boardid", Integer.toString(bookmark.getBoardId()))
                                .appendQueryParameter("threadid", Integer.toString(bookmark.getThreadId()))
                                .appendQueryParameter("bookmarkedmessageid", Integer.toString(bookmark.getBookmarkedMessageId()))
                                .build().toString();
                        bookmarkThreadUrls.add(url);
                    }
                }
            }
            if (allBookmarksHaveThreads) {
                processBookmarkCallbacks();
            } else {
                new GetItemsTask(mHttpClient, (String[] results) -> {
                    for (int i = 0; i < results.length; i++) {
                        Bookmark bookmark = bookmarksForThreads.get(i);
                        Thread bookmarkThread = mSerializer.read(Thread.class, results[i], false);
                        bookmark.setCachedThread(bookmarkThread);
                    }
                    processBookmarkCallbacks();
                }).execute(bookmarkThreadUrls.toArray(new String[0]));
            }
        } else {
            ServerBookmarks bookmarks = new ServerBookmarks();
            mServersBookmarks.put(getServerTag(), bookmarks);
            String serializedBookmarks = mPreferences.getString(serializedBookmarksPrefix + getServerTag(), null);
            if (serializedBookmarks != null) {
                String[] bookmarkValTriplets = serializedBookmarks.split("-");
                String[] bookmarkThreadUrls = new String[bookmarkValTriplets.length];
                for (int i=0; i<bookmarkValTriplets.length; i++) {
                    String bookmarkValTriplet = bookmarkValTriplets[i];
                    String[] bookmarkVals = bookmarkValTriplet.split("\\.");
                    bookmarks.addBookmark(Integer.valueOf(bookmarkVals[0]),
                            Integer.valueOf(bookmarkVals[1]), Integer.valueOf(bookmarkVals[2]));

                    String url = Uri.parse(threadUrlBase).buildUpon()
                            .appendQueryParameter("boardid", bookmarkVals[0])
                            .appendQueryParameter("threadid", bookmarkVals[1])
                            .appendQueryParameter("bookmarkedmessageid", bookmarkVals[2])
                            .build().toString();

                    bookmarkThreadUrls[i] = url;
                }
                new GetItemsTask(mHttpClient, (String[] results) -> {
                    for (int i=0; i<results.length; i++) {
                        String bookmarkValTriplet = bookmarkValTriplets[i];
                        String[] bookmarkVals = bookmarkValTriplet.split("\\.");
                        Bookmark bookmark = bookmarks.getBookmark(
                                Integer.valueOf(bookmarkVals[0]), Integer.valueOf(bookmarkVals[1]));
                        Thread bookmarkThread = mSerializer.read(Thread.class, results[i], false);
                        bookmark.setCachedThread(bookmarkThread);
                    }
                    processBookmarkCallbacks();
                }).execute(bookmarkThreadUrls);
            } else {
                processBookmarkCallbacks();
                return false;
            }
        }
        return true;
    }

    private void processBookmarkCallbacks() {
        while (!mBookmarksCallbacks.isEmpty()) {
            BookmarksCallback callback = mBookmarksCallbacks.get(0);
            mBookmarksCallbacks.remove(callback);
            callback.onComplete(mServersBookmarks.get(getServerTag()));
        }
    }

    public ServerBookmarks getCachedBookmarks() {
        if (mServersBookmarks != null && mServersBookmarks.get(getServerTag()) != null) {
            return mServersBookmarks.get(getServerTag());
        }
        return null;
    }

    public void serializeBoardBookmarks(Context context) {
        if (mServersBookmarks == null || mServersBookmarks.get(getServerTag()) == null) {
            return;
        }

        ServerBookmarks bookmarks = mServersBookmarks.get(getServerTag());
        String serializedBookmarks = "";
        for (HashMap.Entry<Integer, HashMap<Integer, Bookmark>> boardBookmarks: bookmarks.getBookmarks().entrySet()) {
            for (HashMap.Entry<Integer, Bookmark> bookmarkEntry : boardBookmarks.getValue().entrySet()) {
                Bookmark bookmark = bookmarkEntry.getValue();

                serializedBookmarks
                        += bookmark.getBoardId()
                        + "."
                        + bookmark.getThreadId()
                        + "."
                        + bookmark.getBookmarkedMessageId()
                        + "-";
            }
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(serializedBookmarksPrefix + getServerTag(), serializedBookmarks);
        editor.apply();
    }

    private SharedPreferences getPreferences(Context context) {
        return mPreferences;
    }

    public void persistUserAppSettings(UserAppSettings settings, Context context) {
        SharedPreferences.Editor editor = mPreferences.edit();

        editor.putInt(UserAppSettings.LoadPrettyPicturesSettingKey, settings.getLoadPrettyPicturesSetting().toInt());

        int newPretendToBeLoggedInSetting = settings.getPretendToBeLoggedInSetting() ? 1 : 0;
        editor.putInt(UserAppSettings.PretendToBeLoggedInKey, newPretendToBeLoggedInSetting);

        editor.apply();
    }

    public void getBoards(BoardsCallback boardsCallback, Context context) {
        if (mBoards == null) {
            Log.d(TAG, "passing on request for boards xml");
            new GetItemsTask(mHttpClient, (String[] results) -> {
                if (results != null && results[0] != null) {
                    mBoards = mSerializer.read(Boards.class, results[0], false);
                }

                for (Board board : mBoards.getBoards()) {
                    int enabled = mPreferences.getInt(getBoardEnabledKey(board), -1);
                    board.setEnabled(enabled != 0);
                }

                boardsCallback.onComplete(mBoards);
            }).execute(boardsListUrl);
        } else {
            Log.d(TAG, "returning cached boards");
            boardsCallback.onComplete(mBoards);
        }
    }

    private String getBoardEnabledKey(Board board) {
        return String.format(Locale.getDefault(), "board_enabled_%d", board.getId());
    }

    public void persistBoardEnabledState(Board board) {
        if (mPreferences != null) {
            SharedPreferences.Editor editor = mPreferences.edit();

            int enabled = board.isEnabled() ? 1 : 0;
            editor.putInt(getBoardEnabledKey(board), enabled);

            editor.apply();
        }
    }

    public void getRecentlyUpdatedThreads(int boardId, RecentlyUpdatedThreadsCallback threadsCallback) {
        String url = Uri.parse(updatedThreadsUrlBase).buildUpon()
                .appendQueryParameter("boardid", Integer.toString(boardId))
                .build().toString();

        Log.d(TAG, "passing on request for recent threads");
        new GetItemsTask(mHttpClient, (String[] results) -> {
            if (results != null && results[0] != null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, results[0], false);
                threadsCallback.onComplete(threads);
            } else {
                threadsCallback.onComplete(null);
            }
        }).execute(url);
    }

    public void getSiteNewAnswers(RecentlyUpdatedThreadsCallback threadsCallback) {
        Log.d(TAG, "getting site new answers");
        new GetItemsTask(mHttpClient, (String[] results) -> {
            if (results != null && results[0] != null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, results[0], false);
                threadsCallback.onComplete(threads);
            } else {
                threadsCallback.onComplete(null);
            }
        }).execute(snaUrl);
    }

    public void getThread(int boardId, int threadId, int initialMessageId, int count, ThreadCallback threadCallback) {
        Uri.Builder builder = Uri.parse(threadUrlBase).buildUpon()
                .appendQueryParameter("boardid", Integer.toString(boardId))
                .appendQueryParameter("threadid", Integer.toString(threadId));

        if (initialMessageId != -1) {
            Log.d(TAG, "requesting a message in a thread");
            builder.appendQueryParameter("bookmarkedmessageid", Integer.toString(initialMessageId));
        } else if (count > 0) {
            Log.d(TAG, "requesting " + String.valueOf(count) + " messages in a thread");
            builder.appendQueryParameter("showlastmessages", Integer.toString(count));
        } else {
            Log.d(TAG, "getting a thread");
        }
        new GetItemsTask(mHttpClient, (String[] results) -> {
            Thread thread = null;
            if (results != null && results[0] != null) {
                thread = mSerializer.read(Thread.class, results[0], false);
            }

            if (thread != null) {
                processMessagesForDisplay(thread.getMessages());
            }

            threadCallback.onComplete(thread);
        }).execute(builder.build().toString());
    }

    public void postAnswer(Thread thread, String message) {
        RequestBody body = new FormBody.Builder()
                .add("boardId", String.valueOf(thread.getBoardId()))
                .add("threadId", String.valueOf(thread.getThreadId()))
                .add("messageCount", String.valueOf(thread.getMessages().size()))
                .add("sKey", thread.getKey())
//                .add("sKey", "C79071A24C1647BC224DF2C718A54015")
                .add("text", message)
                .build();

        Request req = new Request.Builder()
                .url(newAnswerUrl)
//                .header("Cookie", "StyleSheet=newold.css; username=\"aGiDYXzdnWd8lWK/Y5psYJkimZJr\"; password=\"QlrFZzKgpnVDkzW0MJJXbA==\"; JSESSIONID=C79071A24C1647BC224DF2C718A54015; __utma=43332989.1687555477.1479489587.1479489587.1479489587.1; __utmc=43332989; __utmz=43332989.1479489587.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)")
                .post(body)
                .build();

        mHttpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, response.toString());
            }
        });
    }

    private void processMessagesForDisplay(List<Message> messages) {

    }

    public static final class ILXDateTransform implements Transform<Date> {
        ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
            protected SimpleDateFormat initialValue()
            {
                SimpleDateFormat r = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                r.setTimeZone(TimeZone.getTimeZone("GMT"));
                return r;
            }
        };

        public Date read(String source) throws Exception
        {
            return sdf.get().parse(source);
        }

        public String write(Date source) throws Exception
        {
            return sdf.get().format(source);
        }
    }

    public static final class ILXPollDateTransform implements Transform<PollClosingDate> {
        ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
            protected SimpleDateFormat initialValue()
            {
                SimpleDateFormat r = new SimpleDateFormat("yyyy-MM-dd");
                r.setTimeZone(TimeZone.getTimeZone("GMT"));
                return r;
            }
        };

        public PollClosingDate read(String source) throws Exception
        {
            Date date = sdf.get().parse(source);
            return new PollClosingDate(date);
        }

        public String write(PollClosingDate source) throws Exception
        {
            return  sdf.get().format(source);
        }
    }

    public interface BoardsCallback {
        void onComplete(Boards boards);
    }

    public interface RecentlyUpdatedThreadsCallback {
        void onComplete(RecentlyUpdatedThreads threads);
    }

    public interface ThreadCallback {
        void onComplete(Thread thread);
    }

    public interface BookmarksCallback {
        void onComplete(ServerBookmarks bookmarks);
    }

    public interface HaveBookmarksCallback {
        void onComplete(boolean result);
    }
}
