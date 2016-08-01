package android.theporouscity.com.flagging;

import android.theporouscity.com.flagging.ilx.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bergstroml on 7/29/16.
 */

public class ILXUrlParser {

    public static boolean isThreadUrl(String url) {
        if (url.startsWith("http://www.ilxor.com/ILX/ThreadSelectedControllerServlet") ||
                url.startsWith("http://ilxor.com/ILX/ThreadSelectedControllerServlet")) {
            return true;
        } else {
            return false;
        }
    }

    public static ThreadIds getThreadIds(String url) {
        Pattern pattern = Pattern.compile("boardid=([0-9]+)&threadid=([0-9]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            ThreadIds ids = new ILXUrlParser().new ThreadIds(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            return ids;
        } else {
            return null;
        }
    }

    public static String getMessageUrl(int boardId, int threadId, int messageId) {
        String url = "http://ilxor.com/ILX/ThreadSelectedControllerServlet?showall=true";
        url = url + "&bookmarkedmessageid=" + String.valueOf(messageId);
        url = url + "&boardid=" + String.valueOf(boardId);
        url = url + "&threadid=" + String.valueOf(threadId);

        return url;
    }

    public class ThreadIds {
        public int boardId;
        public int threadId;

        public ThreadIds(int boardId, int threadId) {
            this.boardId = boardId;
            this.threadId = threadId;
        }
    }

}
