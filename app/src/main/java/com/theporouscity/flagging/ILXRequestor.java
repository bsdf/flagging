package com.theporouscity.flagging;

import android.content.Context;
import android.content.SharedPreferences;
import com.theporouscity.flagging.ilx.Board;
import com.theporouscity.flagging.ilx.Boards;
import com.theporouscity.flagging.ilx.Bookmark;
import com.theporouscity.flagging.ilx.ServerBookmarks;
import com.theporouscity.flagging.ilx.Message;
import com.theporouscity.flagging.ilx.Thread;
import com.theporouscity.flagging.ilx.RecentlyUpdatedThreads;

import android.support.v4.util.Pair;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.transform.Transform;

import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.OkHttpClient;

/**
 * Created by bergstroml on 2/26/16.
 */
public class ILXRequestor {

    public static final String ILX_SERVER_TAG = "ILX";

    private static final String TAG = "ILXRequestor";
    private static final String serializedBookmarksPrefix = "serializedBookmarks";

    private OkHttpClient mHttpClient;
    private Serializer mSerializer;
    private volatile Boards mBoards;
    private Map<String, ServerBookmarks> mServersBookmarks;
    private List<BookmarksCallback> mBookmarksCallbacks = new ArrayList<>();
    private ArrayList<Pair<String, String>> mServers = null;
    private Pair<String,String> mCurrentServer;
    private UserAppSettings mUserAppSettings;

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

    public ILXRequestor(OkHttpClient mHttpClient, Serializer mSerializer) {
        this.mHttpClient = mHttpClient;
        this.mSerializer = mSerializer;
        mCurrentServer = new Pair<String, String>("ilxor.com", "ILX");
    }

    private String getBaseUrlPath() {
        return "http://" + mCurrentServer.first + "/" + mCurrentServer.second;
    }

    private String getBoardsListUrl() {
        String boardsListPath = "/BoardsXmlControllerServlet";
        return getBaseUrlPath() + boardsListPath;
    }

    private String getUpdatedThreadsUrl() {
        String updatedThreadsPath = "/NewAnswersControllerServlet?xml=true&boardid=";
        return getBaseUrlPath() + updatedThreadsPath;
    }

    private String getThreadUrl() {
        String threadPath = "/ThreadSelectedControllerServlet?xml=true&boardid=";
        return getBaseUrlPath() + threadPath;
    }

    private String getSnaUrl() {
        String snaPath = "/SiteNewAnswersControllerServlet?xml=true";
        return getBaseUrlPath() + snaPath;
    }

    private String getCurrentInstanceName() {
        return ILX_SERVER_TAG;
    }

    public boolean getBookmarks(Context context, BookmarksCallback bookmarksCallback) {

        mBookmarksCallbacks.add(bookmarksCallback);
        if (mBookmarksCallbacks.size() > 1) {
            return true;
        }

        if (mServersBookmarks == null) {
            mServersBookmarks = new HashMap<String, ServerBookmarks>();
        }

        if (getUserAppSettings(context) == null) {
            return false;
        }

        if (mServersBookmarks.get(getCurrentInstanceName()) != null) {
            ArrayList<String> bookmarkThreadUrls = new ArrayList<>();
            ArrayList<Bookmark> bookmarksForThreads = new ArrayList<>();
            ServerBookmarks bookmarks = mServersBookmarks.get(getCurrentInstanceName());
            boolean allBookmarksHaveThreads = true;
            for (HashMap.Entry<Integer, HashMap<Integer, Bookmark>> boardBookmarks: bookmarks.getBookmarks().entrySet()) {
                for (HashMap.Entry<Integer, Bookmark> bookmarkEntry : boardBookmarks.getValue().entrySet()) {
                    Bookmark bookmark = bookmarkEntry.getValue();
                    if (bookmark.getCachedThread() == null) {
                        allBookmarksHaveThreads = false;
                        bookmarksForThreads.add(bookmark);
                        bookmarkThreadUrls.add(getThreadUrl() + Integer.toString(bookmark.getBoardId())
                                + "&threadid=" + Integer.toString(bookmark.getThreadId())
                                + "&bookmarkedmessageid=" + Integer.toString(bookmark.getBookmarkedMessageId()));
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
            mServersBookmarks.put(getCurrentInstanceName(), bookmarks);
            String serializedBookmarks = mUserAppSettings.getString(serializedBookmarksPrefix + getCurrentInstanceName(), context);
            if (serializedBookmarks != null) {
                String[] bookmarkValTriplets = serializedBookmarks.split("-");
                String[] bookmarkThreadUrls = new String[bookmarkValTriplets.length];
                for (int i=0; i<bookmarkValTriplets.length; i++) {
                    String bookmarkValTriplet = bookmarkValTriplets[i];
                    String[] bookmarkVals = bookmarkValTriplet.split("\\.");
                    bookmarks.addBookmark(Integer.valueOf(bookmarkVals[0]),
                            Integer.valueOf(bookmarkVals[1]), Integer.valueOf(bookmarkVals[2]));
                    bookmarkThreadUrls[i] = getThreadUrl() + bookmarkVals[0] +
                            "&threadid=" + bookmarkVals[1] +
                            "&bookmarkedmessageid=" + bookmarkVals[2];
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
            callback.onComplete(mServersBookmarks.get(getCurrentInstanceName()));
        }
    }

    public ServerBookmarks getCachedBookmarks() {
        if (mServersBookmarks != null && mServersBookmarks.get(getCurrentInstanceName()) != null) {
            return mServersBookmarks.get(getCurrentInstanceName());
        }
        return null;
    }

    public void serializeBoardBookmarks(Context context) {
        if (getUserAppSettings(context) == null) {
            return;
        }

        if (mServersBookmarks == null || mServersBookmarks.get(getCurrentInstanceName()) == null) {
            return;
        }

        ServerBookmarks bookmarks = mServersBookmarks.get(getCurrentInstanceName());
        String serializedBookmarks = "";
        for (HashMap.Entry<Integer, HashMap<Integer, Bookmark>> boardBookmarks: bookmarks.getBookmarks().entrySet()) {
            for (HashMap.Entry<Integer, Bookmark> bookmarkEntry : boardBookmarks.getValue().entrySet()) {
                Bookmark bookmark = bookmarkEntry.getValue();

                serializedBookmarks
                        += Integer.toString(bookmark.getBoardId())
                        + "."
                        + Integer.toString(bookmark.getThreadId())
                        + "."
                        + Integer.toString(bookmark.getBookmarkedMessageId())
                        + "-";
            }
        }
        mUserAppSettings.persistString(serializedBookmarksPrefix + getCurrentInstanceName(), serializedBookmarks, context);
    }

    public UserAppSettings getUserAppSettings(Context context) {
        if (mUserAppSettings == null) {
            mUserAppSettings = new UserAppSettings(context);
        }
        return mUserAppSettings;
    }

    public boolean persistUserAppSettings(Context context) {
        if (getUserAppSettings(context) == null) {
            return false;
        }
        mUserAppSettings.persistUserAppSettings(context);
        return true;
    }

    public void getBoards(BoardsCallback boardsCallback, Context context) {
        if (mBoards == null && getUserAppSettings(context) != null) {
            Log.d(TAG, "passing on request for boards xml");
            new GetItemsTask(mHttpClient, (String[] results) -> {
                if (results != null && results[0] != null) {
                    mBoards = mSerializer.read(Boards.class, results[0], false);
                }

                for (Board board : mBoards.getBoards()) {
                    board.setEnabled(mUserAppSettings.getBoardEnabled(board, context));
                }

                boardsCallback.onComplete(mBoards);
            }).execute(getBoardsListUrl());
        } else {
            Log.d(TAG, "returning cached boards");
            boardsCallback.onComplete(mBoards);
        }
    }

    public void persistBoardEnabledState(Board board, Context context) {
        if (getUserAppSettings(context) != null) {
            mUserAppSettings.persistBoardEnabledState(board, context);
        }
    }

    public void getRecentlyUpdatedThreads(int boardId, RecentlyUpdatedThreadsCallback threadsCallback) {
        String url = getUpdatedThreadsUrl() + boardId;
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
        }).execute(getSnaUrl());
    }

    public void getThread(int boardId, int threadId, int initialMessageId, int count, ThreadCallback threadCallback) {
        String url = getThreadUrl() + boardId + "&threadid=" + threadId;
        if (initialMessageId != -1) {
            url = url + "&bookmarkedmessageid=" + initialMessageId;
            Log.d(TAG, "requesting a message in a thread");
        } else if (count > 0) {
            url = url + "&showlastmessages=" + count;
            Log.d(TAG, "requesting " + String.valueOf(count) + " messages in a thread");
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
        }).execute(url);
    }

    private void processMessagesForDisplay(List<Message> messages) {

    }

    public static final class ILXDateTransform implements Transform<Date> {
        ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat> () {
            protected SimpleDateFormat initialValue ()
            {
                SimpleDateFormat r = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
                r.setTimeZone (TimeZone.getTimeZone ("GMT"));
                return r;
            }
        };

        public Date read (String source) throws Exception
        {
            return sdf.get ().parse (source);
        }
        public String write (Date source) throws Exception
        {
            return sdf.get ().format (source);
        }
    }

    public static final class ILXPollDateTransform implements Transform<PollClosingDate> {
        ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat> () {
            protected SimpleDateFormat initialValue ()
            {
                SimpleDateFormat r = new SimpleDateFormat ("yyyy-MM-dd");
                r.setTimeZone (TimeZone.getTimeZone ("GMT"));
                return r;
            }
        };

        public PollClosingDate read (String source) throws Exception
        {
            Date date = sdf.get ().parse (source);
            return new PollClosingDate(date);
        }
        public String write (PollClosingDate source) throws Exception
        {
            return  sdf.get ().format (source);
        }
    }
}
