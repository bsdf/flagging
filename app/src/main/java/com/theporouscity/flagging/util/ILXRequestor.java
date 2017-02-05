package com.theporouscity.flagging.util;

import android.content.Context;

import com.theporouscity.flagging.ilx.Board;
import com.theporouscity.flagging.ilx.Boards;
import com.theporouscity.flagging.ilx.Bookmarks;
import com.theporouscity.flagging.ilx.ILXAccount;
import com.theporouscity.flagging.ilx.Thread;
import com.theporouscity.flagging.ilx.RecentlyUpdatedThreads;

import android.os.AsyncTask;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.transform.Transform;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Serializer mSerializer;
    private volatile Boards mBoards;
    private List<BookmarksCallback> mBookmarksCallbacks = new ArrayList<>();
    private UserAppSettings mUserAppSettings;
    private OkHttpClient mSharedHttpClient;

    public interface BoardsCallback {
        void onComplete(AsyncTaskResult<Boards> result);
    }

    public interface RecentlyUpdatedThreadsCallback {
        void onComplete(AsyncTaskResult<RecentlyUpdatedThreads> result);
    }

    public interface ThreadCallback {
        void onComplete(AsyncTaskResult<Thread> result);
    }

    public interface BookmarksCallback {
        void onComplete(AsyncTaskResult<Bookmarks> result);
    }

    public interface LoginCallback {
        void onComplete(AsyncTaskResult<Boolean> result);
    }

    public interface AddBookmarkCallback {
        void onComplete(AsyncTaskResult<Boolean> result);
    }

    public interface RemoveBookmarkCallback {
        void onComplete(AsyncTaskResult<Boolean> result);
    }

    public interface GetThreadSidCallback {
        void onComplete(AsyncTaskResult<String> result);
    }

    public interface NewAnswerCallback {
        void onComplete(AsyncTaskResult<String> result);
    }

    public ILXRequestor(Serializer mSerializer, OkHttpClient sharedClient) {
        this.mSerializer = mSerializer;
        this.mSharedHttpClient = sharedClient;
    }

    public ILXAccount getCurrentAccount() {
        return mUserAppSettings.getCurrentAccount();
    }

    public OkHttpClient getCurrentClient(Context context) {
        return getCurrentAccount().getHttpClient(context, mSharedHttpClient);
    }

    public String getCurrentInstanceName() {
        if (getCurrentAccount() == null) {
            return null;
        }
        return getCurrentAccount().getInstance();
    }

    public void login(Context context, ILXAccount account, LoginCallback callback) {

        mUserAppSettings.setCurrentAccount(account);
        OkHttpClient client = getCurrentAccount().getHttpClient(context, mSharedHttpClient);

        new LoginTask(getCurrentAccount(), client, (AsyncTaskResult<Boolean> result) -> {
            callback.onComplete(result);
        }).execute();
    }

    public void saveAccount(Context context, ILXAccount account) {
        mUserAppSettings.saveAccountAndPersist(context, account);
    }

    public void getBookmarks(Context context, BookmarksCallback bookmarksCallback) {

        mBookmarksCallbacks.add(bookmarksCallback);
        if (mBookmarksCallbacks.size() > 1) {
            return;
        }

        new LoggedInRequestTask(getCurrentAccount(),
                getCurrentAccount().getHttpClient(context, mSharedHttpClient), (AsyncTaskResult<String> result) -> {
            if (result.getError() == null) {
                Bookmarks bookmarks = mSerializer.read(Bookmarks.class, result.getResult(), false);
                processBookmarkCallbacks(new AsyncTaskResult<>(bookmarks));
            } else {
                processBookmarkCallbacks(new AsyncTaskResult<>(result.getError()));
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getCurrentAccount().getBookmarksUrl());
    }

    private void processBookmarkCallbacks(AsyncTaskResult<Bookmarks> result) {
        while (!mBookmarksCallbacks.isEmpty()) {
            BookmarksCallback callback = mBookmarksCallbacks.get(0);
            mBookmarksCallbacks.remove(callback);
            callback.onComplete(result);
        }
    }

    public void addBookmark(int boardId, int threadId, int messageId, String threadSid,
        Context context, AddBookmarkCallback callback) {

        String url = getCurrentAccount().getAddBookmarkUrl(boardId, threadId, messageId, threadSid);

        new LoggedInRequestTask(getCurrentAccount(),
                getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
                    if (result.getError() == null) {
                        callback.onComplete(new AsyncTaskResult<>(true));
                    } else {
                        callback.onComplete(new AsyncTaskResult<>(result.getError()));
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public void removeBookmark(int boardId, int threadId, String threadSid,
                               Context context, RemoveBookmarkCallback callback) {

        String url = getCurrentAccount().getRemoveBookmarkUrl(boardId, threadId, threadSid);

        new LoggedInRequestTask(getCurrentAccount(),
                getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
                    if (result.getError() == null) {
                        callback.onComplete(new AsyncTaskResult<>(true));
                    } else {
                        callback.onComplete(new AsyncTaskResult<>(result.getError()));
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

    }

    public UserAppSettings getUserAppSettings(Context context) {
        if (mUserAppSettings == null) {
            mUserAppSettings = new UserAppSettings(context);
        }
        return mUserAppSettings;
    }

    public void getBoards(BoardsCallback boardsCallback, Context context) {

        if (mBoards == null && getUserAppSettings(context) != null) {
            Log.d(TAG, "passing on request for boards xml");
            new LoggedInRequestTask(getCurrentAccount(), getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                    (AsyncTaskResult<String> result) -> {
                if (result.getError() == null) {
                    mBoards = mSerializer.read(Boards.class, result.getResult(), false);
                } else {
                    boardsCallback.onComplete(new AsyncTaskResult<>(result.getError()));
                }

                for (Board board : mBoards.getBoards()) {
                    board.setEnabled(mUserAppSettings.getBoardEnabled(board, context));
                }

                boardsCallback.onComplete(new AsyncTaskResult<>(mBoards));
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getCurrentAccount().getBoardsListUrl());
        } else {
            Log.d(TAG, "returning cached boards");
            boardsCallback.onComplete(new AsyncTaskResult<>(mBoards));
        }
    }

    public void persistBoardEnabledState(Board board, Context context) {
        if (getUserAppSettings(context) != null) {
            mUserAppSettings.persistBoardEnabledState(board, context);
        }
    }

    public void getRecentlyUpdatedThreads(Context context, int boardId, RecentlyUpdatedThreadsCallback threadsCallback) {
        new LoggedInRequestTask(getCurrentAccount(), getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
            if (result.getError() == null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, result.getResult(), false);
                threadsCallback.onComplete(new AsyncTaskResult<>(threads));
            } else {
                threadsCallback.onComplete(new AsyncTaskResult<>(result.getError()));
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getCurrentAccount().getUpdatedThreadsUrl(boardId));
    }

    public void getSiteNewAnswers(Context context, RecentlyUpdatedThreadsCallback threadsCallback) {

        Log.d(TAG, "getting site new answers");
        new LoggedInRequestTask(getCurrentAccount(), getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
            if (result.getError() == null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, result.getResult(), false);
                threadsCallback.onComplete(new AsyncTaskResult<>(threads));
            } else {
                threadsCallback.onComplete(new AsyncTaskResult<>(result.getError()));
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getCurrentAccount().getSnaUrl());
    }

    public void getThread(Context context, int boardId, int threadId, int initialMessageId, int count, ThreadCallback threadCallback) {

        String url = getCurrentAccount().getThreadUrl(boardId, threadId);
        if (initialMessageId != -1) {
            url = url + "&bookmarkedmessageid=" + initialMessageId;
            Log.d(TAG, "requesting a message in a thread");
        } else if (count > 0) {
            url = url + "&showlastmessages=" + count;
            Log.d(TAG, "requesting " + String.valueOf(count) + " messages in a thread");
        } else {
            Log.d(TAG, "getting a thread");
        }
        new LoggedInRequestTask(getCurrentAccount(), getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
            Thread thread = null;
            if (result.getError() == null) {
                thread = mSerializer.read(Thread.class, result.getResult(), false);
                threadCallback.onComplete(new AsyncTaskResult<>(thread));
            } else {
                threadCallback.onComplete(new AsyncTaskResult<>(result.getError()));
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public void getThreadSid(Context context, int boardId, int threadId,
                             GetThreadSidCallback callback) {
        String url = getCurrentAccount().getThreadHtmlUrl(boardId, threadId);
        new LoggedInRequestTask(getCurrentAccount(), getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
                    if (result.getError() == null) {
                        Pattern pattern = Pattern.compile("\\?boardid=.+?;sid=([^']+)");
                        Matcher matcher = pattern.matcher(result.getResult());

                        if (matcher.find() && matcher.group(1) != null) {
                            callback.onComplete(new AsyncTaskResult<>(matcher.group(1)));
                        } else {
                            callback.onComplete(new AsyncTaskResult<>(new Exception("can't find thread sid")));
                        }
                    } else {
                        callback.onComplete(new AsyncTaskResult<>(result.getError()));
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
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

    private static final String newAnswerUrl = "http://ilxor.com/ILX/NewAnswerControllerServlet";
    public void postAnswer(Context context, String message, int boardId, int threadId, String sKey, int messageCount, NewAnswerCallback callback) {
        RequestBody body = new FormBody.Builder()
                .add("boardId", String.valueOf(boardId))
                .add("threadId", String.valueOf(threadId))
                .add("messageCount", String.valueOf(messageCount))
                .add("sKey", sKey)
                .add("text", message)
                .build();

        new LoggedInRequestTask(getCurrentAccount(), "POST", body, getCurrentAccount().getHttpClient(context, mSharedHttpClient),
                (AsyncTaskResult<String> result) -> {
                    if (result.getError() == null) {
                        String responseBody = result.getResult();
                        callback.onComplete(new AsyncTaskResult<>(responseBody));
                    } else {
                        callback.onComplete(new AsyncTaskResult<>(result.getError()));
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, newAnswerUrl);
    }
}
