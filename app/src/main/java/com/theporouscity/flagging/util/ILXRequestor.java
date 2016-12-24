package com.theporouscity.flagging.util;

import android.content.Context;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.theporouscity.flagging.ilx.Board;
import com.theporouscity.flagging.ilx.Boards;
import com.theporouscity.flagging.ilx.Bookmark;
import com.theporouscity.flagging.ilx.Bookmarks;
import com.theporouscity.flagging.ilx.ILXAccount;
import com.theporouscity.flagging.ilx.Message;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
public class ILXRequestor {

    public static final String ILX_SERVER_TAG = "ILX";

    private static final String TAG = "ILXRequestor";

    private OkHttpClient mHttpClient;
    private Serializer mSerializer;
    private volatile Boards mBoards;
    private List<BookmarksCallback> mBookmarksCallbacks = new ArrayList<>();
    private ILXAccount mCurrentAccount;
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
        void onComplete(Bookmarks bookmarks);
    }

    public interface HaveBookmarksCallback {
        void onComplete(boolean result);
    }

    public ILXRequestor(OkHttpClient mHttpClient, Serializer mSerializer) {
        this.mHttpClient = mHttpClient;
        this.mSerializer = mSerializer;
    }

    private String getBaseUrlPath(ILXAccount account) {
        ILXAccount theAccount;
        if (account == null) {
            if (mCurrentAccount == null) {
                return null;
            } else {
                theAccount = mCurrentAccount;
            }
        } else {
            theAccount = account;
        }
        return "https://" + theAccount.getDomain() + "/" + theAccount.getInstance();
    }

    private String getCurrentInstanceName() {
        if (mCurrentAccount == null) {
            return null;
        }
        return mCurrentAccount.getInstance();
    }

    private String getBoardsListUrl(ILXAccount account) {
        return getUrlHelper(account, "/BoardsXmlControllerServlet");
    }
    private String getBoardsListUrl() { return getBoardsListUrl(null); }

    private String getUpdatedThreadsUrl(ILXAccount account) {
        return getUrlHelper(account, "/NewAnswersControllerServlet?xml=true&boardid=");
    }
    private String getUpdatedThreadsUrl() { return getUpdatedThreadsUrl(null); }

    private String getThreadUrl(ILXAccount account) {
        return getUrlHelper(account, "/ThreadSelectedControllerServlet?xml=true&boardid=");
    }
    private String getThreadUrl() { return getThreadUrl(null); }

    private String getSnaUrl(ILXAccount account) {
        return getUrlHelper(account, "/SiteNewAnswersControllerServlet?xml=true");
    }
    private String getSnaUrl() { return getSnaUrl(null); }

    private String getLoginPageUrl(ILXAccount account) {
        return getUrlHelper(account, "/Pages/login.jsp");
    }
    private String getLoginPageUrl() { return getLoginPageUrl(null); }

    private String getLoginControllerUrl(ILXAccount account) {
        return getUrlHelper(account, "/LoginControllerServlet");
    }
    private String getLoginControllerUrl() { return getLoginControllerUrl(null); }

    private String getIndexUrl(ILXAccount account) {
        return getUrlHelper(account, "/index.jsp");
    }
    private String getIndexUrl() { return getIndexUrl(null); }

    private String getBookmarksUrl(ILXAccount account) { return getUrlHelper(account, "/BookmarksControllerServlet?xml=true"); }
    private String getBookmarksUrl() { return getBookmarksUrl(null); }

    private String getUrlHelper(ILXAccount account, String path) {
        if (account == null && mCurrentAccount == null) {
            return null;
        }
        return getBaseUrlPath(account) + path;
    }

    public void login(ILXAccount account) throws CredentialsFailedException, ServerInaccessibleException {
        OkHttpClient client = account.getHttpClient();
        try {
            Request request = new Request.Builder().url(getLoginPageUrl(account)).build();
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                throw new ServerInaccessibleException("Problem getting login page");
            }
        } catch (IOException e) {
            throw new ServerInaccessibleException("Problem getting login page: " + e.toString());
        }

        String loginContents = "username=" + account.getUsername() +
                "password=" + account.getPassword() + "rememberMeCheckBox=true";

        MediaType text = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(text, loginContents);
        Request request = new Request.Builder()
                .url(getLoginControllerUrl(account))
                .post(body)
                .addHeader("Content-Length", Integer.toString(loginContents.length()))
                .addHeader("Keep-Alive", "300")
                .addHeader("Referer", getLoginControllerUrl(account))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (responseBody.contains("exceeded")) {
                // TODO retry logging in?
                throw new ServerInaccessibleException("Timed out logging in, try again.");
            }
            if (responseBody.contains("failed")) {
                throw new CredentialsFailedException("Username and password didn't work.");
            }

        } catch (IOException e) {
            throw new ServerInaccessibleException("Problem logging in: " + e.toString());
        }

        getSession(account);

    }

    public void getSession(ILXAccount account) throws CredentialsFailedException, ServerInaccessibleException {
        OkHttpClient client = account.getHttpClient();
        try {
            Request request = new Request.Builder().url(getIndexUrl(account)).build();
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                throw new ServerInaccessibleException("Problem getting login page");
            }
            String responseBody = response.body().string();
            if (responseBody.contains("Login Failed")) {
                throw new CredentialsFailedException("Username + password didn't work");
            }
            for (Cookie cookie : account.getHttpClient().cookieJar().loadForRequest(HttpUrl.parse(getIndexUrl(account)))) {
                if (cookie.name().contentEquals("JSESSIONID")) {
                    account.setSessionId(cookie.value());
                    return;
                }
            }
            throw new ServerInaccessibleException("Credentials worked but didn't get a session ID");
        } catch (IOException e) {
            throw new ServerInaccessibleException("Problem getting login page: " + e.toString());
        }

    }

    public void saveAccount(Context context, ILXAccount account) {
        mUserAppSettings.addAccountAndPersist(context, account);
    }

    private void validateSession(Context context) throws CredentialsFailedException, ServerInaccessibleException {

        if (mCurrentAccount == null) {

            if (mUserAppSettings == null || mUserAppSettings.getAccounts(context).isEmpty()) {
                throw new CredentialsFailedException("no accounts");
            }

            mCurrentAccount = mUserAppSettings.getAccounts(context).get(0);
        }

        if (mCurrentAccount.getHttpClient().cookieJar()
                .loadForRequest(HttpUrl.parse(getLoginPageUrl(mCurrentAccount))) == null) {

            login(mCurrentAccount);
        }

        if (mCurrentAccount.getSessionId() == null) {
            getSession(mCurrentAccount);
        }
    }

    public void getBookmarks(Context context, BookmarksCallback bookmarksCallback) throws CredentialsFailedException, ServerInaccessibleException {

        validateSession(context);

        mBookmarksCallbacks.add(bookmarksCallback);
        if (mBookmarksCallbacks.size() > 1) {
            return;
        }

        new GetItemsTask(mCurrentAccount.getHttpClient(), (String[] results) -> {
            if (results != null && results[0] != null) {
                Bookmarks bookmarks =
                        mSerializer.read(Bookmarks.class, results[0], false);
                processBookmarkCallbacks(bookmarks);
            } else {
                processBookmarkCallbacks(null);
            }
        }).execute(getBookmarksUrl());
    }

    private void processBookmarkCallbacks(Bookmarks bookmarks) {
        while (!mBookmarksCallbacks.isEmpty()) {
            BookmarksCallback callback = mBookmarksCallbacks.get(0);
            mBookmarksCallbacks.remove(callback);
            callback.onComplete(bookmarks);
        }
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

    public void getBoards(BoardsCallback boardsCallback, Context context) throws CredentialsFailedException, ServerInaccessibleException {

        validateSession(context);

        if (mBoards == null && getUserAppSettings(context) != null) {
            Log.d(TAG, "passing on request for boards xml");
            new GetItemsTask(mCurrentAccount.getHttpClient(), (String[] results) -> {
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

    public void getRecentlyUpdatedThreads(Context context, int boardId, RecentlyUpdatedThreadsCallback threadsCallback)
            throws CredentialsFailedException, ServerInaccessibleException
    {

        validateSession(context);

        String url = getUpdatedThreadsUrl() + boardId;
        Log.d(TAG, "passing on request for recent threads");
        new GetItemsTask(mCurrentAccount.getHttpClient(), (String[] results) -> {
            if (results != null && results[0] != null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, results[0], false);
                threadsCallback.onComplete(threads);
            } else {
                threadsCallback.onComplete(null);
            }
        }).execute(url);
    }

    public void getSiteNewAnswers(Context context, RecentlyUpdatedThreadsCallback threadsCallback)
            throws CredentialsFailedException, ServerInaccessibleException {

        validateSession(context);

        Log.d(TAG, "getting site new answers");
        new GetItemsTask(mCurrentAccount.getHttpClient(), (String[] results) -> {
            if (results != null && results[0] != null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, results[0], false);
                threadsCallback.onComplete(threads);
            } else {
                threadsCallback.onComplete(null);
            }
        }).execute(getSnaUrl());
    }

    public void getThread(Context context, int boardId, int threadId, int initialMessageId, int count, ThreadCallback threadCallback)
            throws CredentialsFailedException, ServerInaccessibleException
    {

        validateSession(context);

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
        new GetItemsTask(mCurrentAccount.getHttpClient(), (String[] results) -> {
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
