package com.theporouscity.flagging;

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

    public static ilxIds getIds(String url) {
        int boardId, threadId;
        int messageId = -1;

        Pattern pattern = Pattern.compile("(bookmarkedmessageid=([0-9]+)&){0,1}boardid=([0-9]+)&threadid=([0-9]+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            boardId = Integer.parseInt(matcher.group(3));
            threadId = Integer.parseInt(matcher.group(4));
            if (matcher.group(2) != null) {
                messageId = Integer.parseInt(matcher.group(2));
            }
            ilxIds ids = new ILXUrlParser().new ilxIds(boardId, threadId, messageId);
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

    public class ilxIds {
        public int boardId;
        public int threadId;
        public int messageId;

        public ilxIds(int boardId, int threadId, int messageId) {
            this.boardId = boardId;
            this.threadId = threadId;
            this.messageId = messageId;
        }
    }

}
