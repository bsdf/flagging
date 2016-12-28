package com.theporouscity.flagging.ilx;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bergstroml on 11/4/16.
 */
@Root(name="Bookmark")
public class Bookmark {

    private final static String TAG = "Bookmark";

    @Element(name="BoardId")
    private int BoardId;

    @Element(name="ThreadId")
    private int ThreadId;

    @Element(name="MessageId")
    private int BookmarkedMessageId;

    @Element(name="Title")
    private String Title;

    private Thread mCachedThread;

    public int getBoardId() {
        return BoardId;
    }

    public int getThreadId() {
        return ThreadId;
    }

    public int getBookmarkedMessageId() {
        return BookmarkedMessageId;
    }

    public void setBookmarkedMessageId(int BookmarkedMessageId) {
        this.BookmarkedMessageId = BookmarkedMessageId;
    }

    public Spanned getTitleForDisplay() { return Html.fromHtml(Title.trim()); }

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