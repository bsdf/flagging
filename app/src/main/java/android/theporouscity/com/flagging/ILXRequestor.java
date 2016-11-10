package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.SharedPreferences;
import android.theporouscity.com.flagging.ilx.Board;
import android.theporouscity.com.flagging.ilx.Boards;
import android.theporouscity.com.flagging.ilx.Bookmark;
import android.theporouscity.com.flagging.ilx.ServerBookmarks;
import android.theporouscity.com.flagging.ilx.Message;
import android.theporouscity.com.flagging.ilx.Thread;
import android.theporouscity.com.flagging.ilx.RecentlyUpdatedThreads;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import okhttp3.OkHttpClient;

/**
 * Created by bergstroml on 2/26/16.
 */
public class ILXRequestor {

    private static final String TAG = "ILXRequestor";
    private static final String boardsListUrl = "http://ilxor.com/ILX/BoardsXmlControllerServlet";
    private static final String updatedThreadsUrl = "http://ilxor.com/ILX/NewAnswersControllerServlet?xml=true&boardid=";
    private static final String threadUrl = "http://ilxor.com/ILX/ThreadSelectedControllerServlet?xml=true&boardid=";
    private static final String snaUrl = "http://ilxor.com/ILX/SiteNewAnswersControllerServlet?xml=true";
    private static final String ilxServerTag = "ILX";
    private static final String serializedBookmarksPrefix = "serializedBookmarks";

    private static ILXRequestor mILXRequestor;
    private static OkHttpClient mHttpClient;
    private static Serializer mSerializer;
    private static volatile Boards mBoards;
    private static SharedPreferences mPreferences;
    private static HashMap<String, ServerBookmarks> mServersBookmarks;


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

    private ILXRequestor() {}

    public static ILXRequestor getILXRequestor() {

        if (mILXRequestor == null) {
            mILXRequestor = new ILXRequestor();
            mHttpClient = new OkHttpClient();
            mSerializer = newPersister();
            mBoards = null;
            mPreferences = null;
            mServersBookmarks = null;
        }

        return mILXRequestor;
    }

    private String getServerTag() {
        return ilxServerTag;
    }

    public void getBookmarks(Context context, BookmarksCallback bookmarksCallback) {

        if (mServersBookmarks == null) {
            mServersBookmarks = new HashMap<String, ServerBookmarks>();
        } else if (mServersBookmarks.get(getServerTag()) != null) {
            bookmarksCallback.onComplete(mServersBookmarks.get(getServerTag()));
        }

        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences(ilxServerTag, Context.MODE_PRIVATE);
        }

        ServerBookmarks bookmarks = new ServerBookmarks();
        mServersBookmarks.put(getServerTag(), bookmarks);
        String serializedBookmarks = mPreferences.getString(serializedBookmarksPrefix + getServerTag(), null);
        if (serializedBookmarks != null) {
            String[] bookmarkValTriplets = serializedBookmarks.split("-");
            for (String bookmarkValTriplet : bookmarkValTriplets) {
                String[] bookmarkVals = bookmarkValTriplet.split(".");
                bookmarks.addBookmark(Integer.valueOf(bookmarkVals[0]), Integer.valueOf(bookmarkVals[1]), Integer.valueOf(bookmarkVals[2]));
            }
            new GetBookmarkTitlesTask(mHttpClient, (ArrayList<Bookmark> result) -> {
                for (Bookmark bookmarkWithTitle : result) {
                    Bookmark bookmark = bookmarks.getBookmark(bookmarkWithTitle.getBoardId(), bookmarkWithTitle.getThreadId());
                    if (bookmark != null && bookmarkWithTitle != null) {
                        bookmark.setBookmarkThreadTitle(bookmarkWithTitle.getBookmarkThreadTitle());
                    }
                }
            }).execute(bookmarks);
        } else {
            bookmarksCallback.onComplete(bookmarks);
        }
    }

    public void haveBookmarks(Context context, HaveBookmarksCallback callback) {
        getBookmarks(context, (ServerBookmarks bookmarks) -> {
            if (bookmarks.getBookmarks().entrySet().isEmpty()) {
                callback.onComplete(false);
            } else{
                callback.onComplete(true);
            }
        });
    }

    public void serializeBoardBookmarks(Context context) {
        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences(ilxServerTag, Context.MODE_PRIVATE);
        }

        if (mServersBookmarks == null || mServersBookmarks.get(getServerTag()) == null) {
            return;
        }

        ServerBookmarks bookmarks = mServersBookmarks.get(getServerTag());
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
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(serializedBookmarksPrefix + getServerTag(), serializedBookmarks);
        editor.apply();
    }

    private SharedPreferences getPreferences(Context context) {

        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences(ilxServerTag, Context.MODE_PRIVATE);
        }

        return mPreferences;
    }

    public UserAppSettings getUserAppSettings(Context context) {

        UserAppSettings settings = new UserAppSettings();

        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences(ilxServerTag, Context.MODE_PRIVATE);
        }

        int loadPrettyPictures = mPreferences.getInt(UserAppSettings.LoadPrettyPicturesSettingKey, -1);
        if (loadPrettyPictures == -1 || loadPrettyPictures == 0) {
            settings.setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.NEVER);
        } else if (loadPrettyPictures == 1) {
            settings.setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.ALWAYS);
        } else if (loadPrettyPictures == 2) {
            settings.setLoadPrettyPicturesSetting(UserAppSettings.LoadPrettyPicturesSetting.WIFI);
        }

        int pretendToBeLoggedIn = mPreferences.getInt(UserAppSettings.PretendToBeLoggedInKey, -1);
        if (pretendToBeLoggedIn == 1) {
            settings.setPretendToBeLoggedInSetting(true);
        } else {
            settings.setPretendToBeLoggedInSetting(false);
        }

        return settings;
    }

    public void persistUserAppSettings(UserAppSettings settings, Context context) {

        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences(ilxServerTag, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor editor = mPreferences.edit();

        int newLoadPrettyPicturesSetting;
        if (settings.getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.ALWAYS) {
            newLoadPrettyPicturesSetting = 1;
        } else if (settings.getLoadPrettyPicturesSetting() == UserAppSettings.LoadPrettyPicturesSetting.WIFI) {
            newLoadPrettyPicturesSetting = 2;
        } else {
            newLoadPrettyPicturesSetting = 0;
        }
        editor.putInt(UserAppSettings.LoadPrettyPicturesSettingKey, newLoadPrettyPicturesSetting);

        int newPretendToBeLoggedInSetting;
        if (settings.getPretendToBeLoggedInSetting()) {
            newPretendToBeLoggedInSetting = 1;
        } else {
            newPretendToBeLoggedInSetting = 0;
        }
        editor.putInt(UserAppSettings.PretendToBeLoggedInKey, newPretendToBeLoggedInSetting);

        editor.apply();

    }

    public void getBoards(BoardsCallback boardsCallback, Context context) {
        if (mBoards == null) {
            Log.d(TAG, "passing on request for boards xml");
            new GetItemsTask(mHttpClient, (String result) -> {
                if (result != null) {
                    mBoards = mSerializer.read(Boards.class, result, false);
                }

                if (mPreferences == null) {
                    mPreferences = context.getSharedPreferences(ilxServerTag, Context.MODE_PRIVATE);
                }

                for (Board board : mBoards.getBoards()) {
                    int enabled = mPreferences.getInt(getBoardEnabledKey(board), -1);
                    if (enabled == -1) {
                        board.setEnabled(true);
                    } else if (enabled == 0) {
                        board.setEnabled(false);
                    } else {
                        board.setEnabled(true);
                    }
                }

                boardsCallback.onComplete(mBoards);
            }).execute(boardsListUrl);
        } else {
            Log.d(TAG, "returning cached boards");
            boardsCallback.onComplete(mBoards);
        }
    }

    private String getBoardEnabledKey(Board board) {
        return "board_enabled_" + Integer.toString(board.getId());
    }

    public void persistBoardEnabledState(Board board) {
        if (mPreferences != null) {

            int enabled;

            if (board.isEnabled()) {
                enabled = 1;
            } else {
                enabled = 0;
            }

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(getBoardEnabledKey(board), enabled);
            editor.apply();
        }
    }

    public void getRecentlyUpdatedThreads(int boardId, RecentlyUpdatedThreadsCallback threadsCallback) {
        String url = updatedThreadsUrl + boardId;
        Log.d(TAG, "passing on request for recent threads");
        new GetItemsTask(mHttpClient, (String result) -> {
            if (result != null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, result, false);
                threadsCallback.onComplete(threads);
            }
        }).execute(url);
    }

    public void getSiteNewAnswers(RecentlyUpdatedThreadsCallback threadsCallback) {
        Log.d(TAG, "getting site new answers");
        new GetItemsTask(mHttpClient, (String result) -> {
            if (result != null) {
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, result, false);
                threadsCallback.onComplete(threads);
            }
        }).execute(snaUrl);
    }

    public void getThread(int boardId, int threadId, int initialMessageId, int count, ThreadCallback threadCallback) {
        String url = threadUrl + boardId + "&threadid=" + threadId;
        if (initialMessageId != -1) {
            url = url + "&bookmarkedmessageid=" + initialMessageId;
            Log.d(TAG, "requesting a message in a thread");
        } else if (count > 0) {
            url = url + "&showlastmessages=" + count;
            Log.d(TAG, "requesting " + String.valueOf(count) + " messages in a thread");
        } else {
            Log.d(TAG, "getting a thread");
        }
        new GetItemsTask(mHttpClient, (String result) -> {
            Thread thread = null;
            if (result != null) {
                thread = mSerializer.read(Thread.class, result, false);
            }

            if (thread != null) {
                processMessagesForDisplay(thread.getMessages());
            }

            threadCallback.onComplete(thread);
        }).execute(url);
    }

    private void processMessagesForDisplay(List<Message> messages) {

    }

    private static Persister newPersister() {
        ILXDateTransform transform1 = new ILXDateTransform();
        ILXPollDateTransform transform2 = new ILXPollDateTransform();

        return new Persister(new Matcher() {
            @Override
            public Transform match(Class cls) throws Exception {
                if (cls == Date.class) {
                    return transform1;
                } else if (cls == PollClosingDate.class) {
                    return transform2;
                }
                return null;
            }
        });
    }

    private static final class ILXDateTransform implements Transform<Date> {
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

    private static final class ILXPollDateTransform implements Transform<PollClosingDate> {
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
