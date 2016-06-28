package android.theporouscity.com.flagging;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.theporouscity.com.flagging.ilx.Boards;
import android.theporouscity.com.flagging.ilx.Thread;
import android.theporouscity.com.flagging.ilx.RecentlyUpdatedThreads;
import android.util.Log;
import android.widget.TextView;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

/**
 * Created by bergstroml on 2/26/16.
 */
public class ILXRequestor {

    private static final String boardsListUrl = "http://ilxor.com/ILX/BoardsXmlControllerServlet";
    private static final String updatedThreadsUrl = "http://ilxor.com/ILX/NewAnswersControllerServlet?xml=true&boardid=";
    private static final String threadUrl = "http://ilxor.com/ILX/ThreadSelectedControllerServlet?xml=true&boardid=";

    private static ILXRequestor mILXRequestor;
    private static OkHttpClient mHttpClient;
    private static Serializer mSerializer;
    private static volatile Boards mBoards;

    public interface BoardsCallback {
        void onComplete(Boards boards);
    }

    public interface RecentlyUpdatedThreadsCallback {
        void onComplete(RecentlyUpdatedThreads threads);
    }

    public interface ThreadCallback {
        void onComplete(Thread thread);
    }

    private ILXRequestor() {}

    public static ILXRequestor getILXRequestor() {

        if (mILXRequestor == null) {
            mILXRequestor = new ILXRequestor();
            mHttpClient = new OkHttpClient();
            mSerializer = newPersister();
            mBoards = null;
        }

        return mILXRequestor;
    }

    public void getBoards(BoardsCallback boardsCallback) {
        if (mBoards == null) {
            Log.d("ILXRequestor", "passing on request for boards xml");
            new GetItemsTask(mHttpClient, (String result) -> {
                if (result != null) {
                    Log.d("get boards", result);
                    mBoards = mSerializer.read(Boards.class, result, false);
                }
                boardsCallback.onComplete(mBoards);
            }).execute(boardsListUrl);
        } else {
            Log.d("ILXRequestor", "returning cached boards");
            boardsCallback.onComplete(mBoards);
        }
    }

    public void getRecentlyUpdatedThreads(int boardId, RecentlyUpdatedThreadsCallback threadsCallback) {
        String url = updatedThreadsUrl + boardId;
        Log.d("ILXRequestor", "passing on request for recent threads");
        new GetItemsTask(mHttpClient, (String result) -> {
            if (result != null) {
                Log.d("get threads", result);
                RecentlyUpdatedThreads threads =
                        mSerializer.read(RecentlyUpdatedThreads.class, result, false);
                threadsCallback.onComplete(threads);
            }
        }).execute(url);
    }

    public void getThread(int boardId, int threadId, int count, ThreadCallback threadCallback) {
        String url = threadUrl + boardId + "&threadid=" + threadId;
        if (count > 0) {
            url = url + "&showlastmessages=" + count;
        }
        Log.d("ILXRequestor", "getting a thread");
        new GetItemsTask(mHttpClient, (String result) -> {
            if (result != null) {
                Log.d("get messages", result);
                Thread thread = mSerializer.read(Thread.class, result, false);
                threadCallback.onComplete(thread);
            }
        }).execute(url);
    }

    private static Persister newPersister() {
        ILXDateTransform transform = new ILXDateTransform();
        return new Persister(new Matcher() {
            @Override
            public Transform match(Class cls) throws Exception {
                if (cls == Date.class) return transform;
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

}
