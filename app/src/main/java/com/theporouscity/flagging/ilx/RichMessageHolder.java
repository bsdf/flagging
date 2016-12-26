package com.theporouscity.flagging.ilx;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import com.theporouscity.flagging.util.ILXTextOutputFormatter;

/**
 * Created by bergstroml on 10/7/16.
 */

public class RichMessageHolder {

    private static final String TAG = "RichMessageHolder";
    private static final String ITAG = "imglog ";
    ILXTextOutputFormatter.MessageReadyCallback mCallback;
    private Message mMessage;
    private Spanned mDisplayNameForDisplay;
    private Spanned mBodyForDisplayShort;
    private Drawable mYoutubePlaceholderImage;
    private Drawable mEmptyPlaceholderImage;
    private int mlinkColor;
    private ILXTextOutputFormatter mILXTextOutputFormatter;

    public RichMessageHolder(ILXTextOutputFormatter mILXTextOutputFormatter, Message message,
                             Drawable youtubePlaceholderImage, Drawable emptyPlaceholderImage, int linkColor) {
        this.mILXTextOutputFormatter = mILXTextOutputFormatter;
        mMessage = message;
        mYoutubePlaceholderImage = youtubePlaceholderImage;
        mEmptyPlaceholderImage = emptyPlaceholderImage;
        mlinkColor = linkColor;
        mCallback = null;
    }

    public Message getMessage() {
        return mMessage;
    }

    public Spanned getDisplayNameForDisplay() {
        prepDisplayNameForDisplay();
        return mDisplayNameForDisplay;
    }

    public void prepDisplayNameForDisplay() {
        if (mDisplayNameForDisplay == null) {
            mDisplayNameForDisplay = Html.fromHtml(mMessage.getDisplayName(), null, null);
        }
    }

    public Spanned getBodyForDisplayShort(Activity activity,
                                          ILXTextOutputFormatter.MessageReadyCallback callback) {

        if (callback == null) {
            Log.d(TAG, ITAG + "get msg with null callback");
        }

        if (mCallback == null) {
            mCallback = callback;
        }

        if (mBodyForDisplayShort == null) {
            prepBodyForDisplayShort(activity, mCallback);
        }

        return mBodyForDisplayShort;

    }

    public void prepBodyForDisplayShort(Activity activity,
                                        ILXTextOutputFormatter.MessageReadyCallback callback) {

        if (mCallback == null) {
            mCallback = callback;
        }

        if (mBodyForDisplayShort == null) {

            mBodyForDisplayShort = mILXTextOutputFormatter
                    .getBodyForDisplayShort(mMessage.getBody(),
                            mYoutubePlaceholderImage,
                            mEmptyPlaceholderImage,
                            mlinkColor,
                            activity,
                            () -> {
                                if (mCallback != null) {
                                    mCallback.onComplete();
                                } else {
                                    Log.d(TAG, ITAG + "got img, null callback " + Integer.toString(mMessage.getMessageId()));
                                }
                            });
        }

    }
}
