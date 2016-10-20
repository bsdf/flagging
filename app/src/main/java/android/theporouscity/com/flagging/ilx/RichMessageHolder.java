package android.theporouscity.com.flagging.ilx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;
import android.theporouscity.com.flagging.ILXTextOutputFormatter;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

/**
 * Created by bergstroml on 10/7/16.
 */

public class RichMessageHolder {

    private static final String TAG = "RichMessageHolder";
    ILXTextOutputFormatter.MessageReadyCallback mCallback;
    private Message mMessage;
    private Spanned mDisplayNameForDisplay;
    private Spanned mBodyForDisplayShort;
    private Drawable mYoutubePlaceholderImage;
    private Drawable mEmptyPlaceholderImage;
    private int mlinkColor;
    //private WeakReference<ILXTextOutputFormatter.ImageGetterAsyncTask> mImageGetterTaskWeakRef;
    //private WeakReference<Bitmap>

    public RichMessageHolder(Message message, Drawable youtubePlaceholderImage,
                             Drawable emptyPlaceholderImage, int linkColor) {
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

        if (mCallback == null && callback != null) {
            mCallback = callback;
        }

        if (mBodyForDisplayShort == null) {
            if (callback != null) {
                Log.d(TAG, "should have a valid callback");
            }
            prepBodyForDisplayShort(activity, mCallback);
        }

        return mBodyForDisplayShort;

    }

    public void prepBodyForDisplayShort(Activity activity,
                                        ILXTextOutputFormatter.MessageReadyCallback callback) {

        if (mCallback == null && callback != null) {
            mCallback = callback;
        }

        if (mBodyForDisplayShort == null) {

            mBodyForDisplayShort = ILXTextOutputFormatter.getILXTextOutputFormatter()
                    .getBodyForDisplayShort(mMessage.getBody(),
                            mYoutubePlaceholderImage,
                            mEmptyPlaceholderImage,
                            mlinkColor,
                            activity,
                            () -> { if (mCallback != null) { mCallback.onComplete(); } });
        }

    }
}
