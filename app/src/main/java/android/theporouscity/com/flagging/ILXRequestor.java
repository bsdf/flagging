package android.theporouscity.com.flagging;

import android.theporouscity.com.flagging.ilx.Boards;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by bergstroml on 2/26/16.
 */
public class ILXRequestor {

    private static final String boardsListUrl = "http://ilxor.com/ILX/BoardsXmlControllerServlet";
    private static ILXRequestor mILXRequestor;
    private static OkHttpClient mHttpClient;
    private static Serializer mSerializer;
    private static volatile Boards mBoards;

    public interface Callback {
        void onComplete(Boards boards);
    }

    private ILXRequestor() {}

    public static ILXRequestor getILXRequestor() {

        if (mILXRequestor == null) {
            mILXRequestor = new ILXRequestor();
            mHttpClient = new OkHttpClient();
            mSerializer = new Persister();
            mBoards = null;
        }

        return mILXRequestor;
    }

    public void getBoards(Callback callback) {
        if (mBoards == null) {
            Log.d("ILXRequestor", "passing on request for boards xml");
            new GetItemsTask(mHttpClient, (String result) -> {
                if (result != null) {
                    mBoards = mSerializer.read(Boards.class, result, false);
                }
                callback.onComplete(mBoards);
            }).execute(boardsListUrl);
        } else {
            Log.d("ILXRequestor", "returning cached boards");
            callback.onComplete(mBoards);
        }
    }
}
