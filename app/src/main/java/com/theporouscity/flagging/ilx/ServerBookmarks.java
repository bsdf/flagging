package com.theporouscity.flagging.ilx;

import java.util.HashMap;

/**
 * Created by bergstroml on 11/3/16.
 */

public class ServerBookmarks {

    private HashMap<Integer, HashMap<Integer, Bookmark>> mBookmarks;
    private String mSerializedBookmarksKeyPrefix = "serializedBookmarks";

    public ServerBookmarks() {
        mBookmarks = new HashMap<Integer, HashMap<Integer, Bookmark>>();
    }

    public HashMap<Integer, HashMap<Integer, Bookmark>> getBookmarks() {
        return mBookmarks;
    }

    public void addBookmark(int boardId, int threadId, int bookmarkedMessageId) {

        HashMap<Integer, Bookmark> boardBookmarks = mBookmarks.get(boardId);
        if (boardBookmarks == null) {
            boardBookmarks = new HashMap<Integer, Bookmark>();
            mBookmarks.put(boardId, boardBookmarks);
        }

        Bookmark bookmark = boardBookmarks.get(threadId);
        if (bookmark == null) {
            bookmark = new Bookmark(boardId, threadId, bookmarkedMessageId);
            boardBookmarks.put(threadId, bookmark);
        } else {
            bookmark.setBookmarkedMessageId(bookmarkedMessageId);
        }
    }

    public Bookmark getBookmark(int boardId, int threadId) {
        HashMap boardBookmarks = mBookmarks.get(boardId);
        if (boardBookmarks != null) {
            return (Bookmark) boardBookmarks.get(threadId);
        }

        return null;
    }

}
