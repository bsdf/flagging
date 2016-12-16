package com.theporouscity.flagging.ilx;

import android.util.Log;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.lang.*;

/**
 * Created by bergstroml on 11/4/16.
 */
@Root(name="Bookmark")
public class Bookmark {

    private final static String TAG = "Bookmark";

    @Element(name="BoardId")
    private int boardId;

    @Element(name="ThreadId")
    private int threadId;

    @Element(name="MessageId")
    private int bookmarkedMessageId;

    @Element(name="Title")
    private String bookmarkThreadTitle;

    private Thread mCachedThread;

    public Bookmark(int boardId, int threadId, int bookmarkedMessageId) {
        this.boardId = boardId;
        this.threadId = threadId;
        this.bookmarkedMessageId = bookmarkedMessageId;
    }

    public int getBoardId() {
        return boardId;
    }

    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getBookmarkedMessageId() {
        return bookmarkedMessageId;
    }

    public void setBookmarkedMessageId(int bookmarkedMessageId) {
        this.bookmarkedMessageId = bookmarkedMessageId;
    }

    public void setCachedThread(Thread thread) {
        mCachedThread = thread;
        if (mCachedThread == null) {
            Log.d(TAG, System.identityHashCode(this) + "set cached thread but it was null");
        } else {
            Log.d(TAG, System.identityHashCode(this) + "set non-null cached thread");
        }
    }

    public Thread getCachedThread() {
        if (mCachedThread == null) {
            Log.d(TAG, System.identityHashCode(this) + "returning null cached thread");
        } else {
            Log.d(TAG, System.identityHashCode(this) + "returning non-null cached thread");
        }
        return mCachedThread;
    }
}