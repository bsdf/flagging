package com.theporouscity.flagging.util;

import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bergstroml on 7/29/16.
 */

public class ILXUrlParser {

    private static Uri.Builder threadUriBuilder
            = Uri.parse("http://ilxor.com/ILX/ThreadSelectedControllerServlet?showall=true").buildUpon();

    public static boolean isThreadUrl(String url) {
        if (url.startsWith("http://www.ilxor.com/ILX/ThreadSelectedControllerServlet") ||
                url.startsWith("http://ilxor.com/ILX/ThreadSelectedControllerServlet")) {
            return true;
        } else {
            return false;
        }
    }

    public static ILXIds getIds(String url) {
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
            return new ILXIds(boardId, threadId, messageId);
        } else {
            return null;
        }
    }

    public static String getMessageUrl(int boardId, int threadId, int messageId) {
        return threadUriBuilder
                .appendQueryParameter("bookmarkedmessageid", Integer.toString(messageId))
                .appendQueryParameter("boardid", Integer.toString(boardId))
                .appendQueryParameter("threadid", Integer.toString(threadId))
                .build().toString();
    }

    public static class ILXIds {
        public int boardId;
        public int threadId;
        public int messageId;

        public ILXIds(int boardId, int threadId, int messageId) {
            this.boardId = boardId;
            this.threadId = threadId;
            this.messageId = messageId;
        }
    }

}
