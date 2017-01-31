package com.theporouscity.flagging.ilx;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.ILXTextOutputFormatter;
import com.theporouscity.flagging.R;
import com.theporouscity.flagging.util.ILXUrlParser;
import com.theporouscity.flagging.util.ServerInaccessibleException;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bergstroml on 10/10/16.
 */

public class RichThreadHolder {

    private static final String TAG = "RichThreadHolder";
    private Thread mThread;
    private Drawable mYoutubePlaceholderImage;
    private Drawable mEmptyPlaceholderImage;
    private int mLinkColor;
    private ILXTextOutputFormatter mILXTextOutputFormatter;
    private String mSid = null;

    public RichThreadHolder(Thread thread, Context context,
                            ILXTextOutputFormatter mILXTextOutputFormatter) {
        this.mILXTextOutputFormatter = mILXTextOutputFormatter;
        getDrawingResources(context);
        mThread = thread;
    }

    public Thread getThread() {
        return mThread;
    }

    public Drawable getYoutubePlaceholderImage() {
        return mYoutubePlaceholderImage;
    }

    public Drawable getEmptyPlaceholderImage() {
        return mEmptyPlaceholderImage;
    }

    public int getLinkColor() {
        return mLinkColor;
    }

    public String getSid() { return mSid; }

    public void setSid(String sid) { mSid = sid; }

    public void prepSid(Context context, ILXRequestor requestor) {
        requestor.getThreadSid(context, mThread.getBoardId(), mThread.getThreadId(),
                (AsyncTaskResult<String> result) -> {
                    if (result.getError() == null) {
                        setSid(result.getResult());
                    } else {
                        Log.e(TAG, result.getError().toString());
                    }
                });
    }

    public RichMessageHolder createRichMessageHolder(int position) {
        return new RichMessageHolder(this.mILXTextOutputFormatter,
                mThread.getMessages().get(position),
                mYoutubePlaceholderImage,
                mEmptyPlaceholderImage,
                mLinkColor);
    }

    public void addMessages(int startPosition, List<Message> messages) {
        mThread.getMessages().addAll(startPosition, messages);
    }

    public void addMessages(List<Message> messages) {
        addMessages(mThread.getMessages().size(), messages);
    }

    public void getDrawingResources(Context context) {
        mYoutubePlaceholderImage = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_slideshow);
        mYoutubePlaceholderImage.setBounds(0, 0, mYoutubePlaceholderImage.getIntrinsicWidth(), mYoutubePlaceholderImage.getIntrinsicHeight());
        mEmptyPlaceholderImage = ContextCompat.getDrawable(context, R.drawable.ic_empty_image);
        mLinkColor = ContextCompat.getColor(context, R.color.colorAccent);
    }

    public void ingestEarlierMessages(Thread thread, Activity activity, int count) {
        addMessages(0, thread.getMessages().subList(0, count));
        getThread().updateMetadata(thread.getServerMessageCount(), thread.getLastUpdated());
    }

    public void ingestLaterMessages(Thread thread, Activity activity, int start, int end) {
        addMessages(thread.getMessages().subList(start, end));
        getThread().updateMetadata(thread.getServerMessageCount(), thread.getLastUpdated());
    }
}
