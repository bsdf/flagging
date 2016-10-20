package android.theporouscity.com.flagging;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.theporouscity.com.flagging.ilx.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;
import org.xml.sax.XMLReader;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bergstroml on 7/21/16.
 */

public class ILXTextOutputFormatter {

    private static ILXTextOutputFormatter mILXTextOutputFormatter;

    private ILXTextOutputFormatter() {}

    public static ILXTextOutputFormatter getILXTextOutputFormatter() {

        if (mILXTextOutputFormatter == null) {
            mILXTextOutputFormatter = new ILXTextOutputFormatter();
        }
        return mILXTextOutputFormatter;
    }

    public String fixMessageBodyForWebview(Message message) {

        // everything that happens in this method is very good and cool

        String newBody = "<style>img{display: inline; height: auto; max-width: 100%;}</style>";
        newBody = newBody + "<p>" + message.getDisplayName() + " at " + ILXDateOutputFormatter.formatAbsoluteDateLong(message.getTimestamp()) + "</p>";
        newBody = newBody + message.getBody();
        newBody = newBody.replaceAll("src=\"//", "src=\"http://");

        Pattern pattern = Pattern.compile("<object(?:(?!<object).)*<embed src=\"http://www.youtube.com/v/((?:(?!<object).)*)&fs=1\"(?:(?!<object).)*</object>");
        Matcher matcher = pattern.matcher(newBody);

        String iframeSrc = "<div style=\"position:relative; padding-bottom:56.25%; height:0\">";
        iframeSrc = iframeSrc + "<iframe style=\"position:absolute;top:0;left:0;width:100%;height:100%\" ";
        iframeSrc = iframeSrc + "src=\"http://www.youtube.com/embed/$1?html5=1&fs=1&controls=2&modestbranding=1\" frameborder=0 allowfullscreen></iframe></div>";
        newBody = matcher.replaceAll(iframeSrc);

        // theoretically I guess I could look at the posting date to decide which youtube code to look for, eh

        pattern = Pattern.compile("<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/([0-9a-zA-Z_-]+)\" frameborder=\"0\" allowfullscreen></iframe>");
        matcher = pattern.matcher(newBody);
        newBody = matcher.replaceAll(iframeSrc);

        return newBody;
    }

    public Spanned getBodyForDisplayShort(String text, Drawable youtubePlaceholderImage,
                                          Drawable emptyPlaceholderImage, int linkColor,
                                          Activity activity, MessageReadyCallback callback) {

        // TODO maybe refactor things so we're not checking the network status all the time

        ILXImgHandler imgHandler = null;
        ILXTagHandler tagHandler = new ILXTagHandler(youtubePlaceholderImage);

        if (ILXRequestor.getILXRequestor().getUserAppSettings(activity).shouldLoadPictures(activity)) {
            imgHandler = new ILXImgHandler(emptyPlaceholderImage, activity, callback);
        }

        Spanned newString = Html.fromHtml(text, imgHandler, tagHandler);
        return fixSpannable(new SpannableStringBuilder(newString), linkColor, activity);

    }

    private SpannableStringBuilder fixSpannable(SpannableStringBuilder spannable, int linkColor, Activity activity) {

        if (spannable == null) {
            return null;
        }

        int trimStart = 0;
        int trimEnd = 0;

        String text = spannable.toString();

        while (text.length() > 0 && text.startsWith("\n")) {
            text = text.substring(1);
            trimStart += 1;
        }

        while (text.length() > 0 && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
            trimEnd += 1;
        }

        spannable.delete(0, trimStart).delete(spannable.length() - trimEnd, spannable.length());

        // handle ILX links

        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan span : spans) {
            if (ILXUrlParser.isThreadUrl(span.getURL())) {
                int start = spannable.getSpanStart(span);
                int end = spannable.getSpanEnd(span);
                String url = span.getURL();
                spannable.removeSpan(span);
                spannable.setSpan(new ILXURLSpan(url, linkColor, activity), start, end, 0);
            }
        }

        return spannable;
    }

    private static class ILXURLSpan extends ClickableSpan {

        private String mThreadUrl;
        private Activity mActivity;

        public ILXURLSpan(String url, int linkColor, Activity activity) {

            mThreadUrl = url;
            mActivity = activity;

            TextPaint textPaint = new TextPaint();
            updateDrawState(textPaint);
        }

        @Override
        public void onClick(View widget) {
            Log.d("ILXURLSpan", "ilx link clicked");
            if (mThreadUrl != null) {
                Intent intent = ViewThreadActivity.newIntent(mActivity, mThreadUrl);
                if (intent != null) {
                    mActivity.startActivity(intent);
                }
            }
        }
    }

    private class ILXTagHandler implements Html.TagHandler {

        private Drawable mYoutubePlaceholderImage;

        public ILXTagHandler(Drawable youtubePlaceholderImage) {
            mYoutubePlaceholderImage = youtubePlaceholderImage;
        }

        public void handleTag(boolean opening, String tag, Editable output,
                              XMLReader xmlReader) {
            if (tag.equalsIgnoreCase("object") || tag.equalsIgnoreCase("iframe")) {
                if (opening) {
                    processYoutubeEmbed(output);
                }
            }
        }

        private void processYoutubeEmbed(Editable output) {

            ImageSpan span = new ImageSpan(mYoutubePlaceholderImage, ImageSpan.ALIGN_BASELINE);

            // hmmm
            if (output.toString().endsWith("\n")) {
                output.append("x");
            } else {
                output.append("\nx");
            }

            int len = output.length();
            output.setSpan(span, len-1, len, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

    }

    private class ILXImgHandler implements Html.ImageGetter {

        private Drawable mEmptyImagePlaceholder;
        private Activity mActivity;
        private MessageReadyCallback mCallback;

        public ILXImgHandler(Drawable emptyImagePlaceholder, Activity activity, MessageReadyCallback callback) {
            mEmptyImagePlaceholder = emptyImagePlaceholder;
            mActivity = activity;
            mCallback = callback;
        }

        @Override
        public Drawable getDrawable(String source) {
            LevelListDrawable d = new LevelListDrawable();
            d.addLevel(0, 0, mEmptyImagePlaceholder);
            d.setBounds(0, 0, mEmptyImagePlaceholder.getIntrinsicWidth(), mEmptyImagePlaceholder.getIntrinsicHeight());
            new ImageGetterAsyncTask(mActivity, d, mCallback).execute(source);

            return d;
        }

    }

    private class ImageGetterAsyncTask extends AsyncTask<String, Void, Bitmap> {


        public static final String TAG = "ImageGetterAsyncTask";
        private LevelListDrawable levelListDrawable;
        private final WeakReference<Activity> mActivity;
        private String mSource;
        private MessageReadyCallback mCallback;

        public ImageGetterAsyncTask(Activity activity, LevelListDrawable levelListDrawable,
                                    MessageReadyCallback callback) {
            mActivity = new WeakReference<Activity>(activity);
            this.levelListDrawable = levelListDrawable;
            mCallback = callback;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            mSource = params[0];
            try {
                Log.d(TAG, "Downloading the image from: " + mSource);
                try {

                    // TODO hopefully we get an onStop or something and we can stop loading images when we navigate away

                    final Activity activity = mActivity.get();

                    /*DisplayMetrics metrics = new DisplayMetrics();
                    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int resizeWidth, resizeHeight;
                    resizeWidth = resizeHeight = 0;
                    if (metrics.widthPixels < metrics.heightPixels) {
                        resizeWidth = metrics.widthPixels;
                    } else {
                        resizeHeight = metrics.heightPixels;
                    }*/

                    // TODO bring this back when we can re-resize to original size if original is smaller

                    return Picasso
                            .with(activity)
                            .load(mSource)
                            //.resize(resizeWidth, resizeHeight)
                            //.centerInside()
                            .get();

                } catch (OutOfMemoryError outOfMemoryError) {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            final Activity activity = mActivity.get();
            if (activity != null && bitmap != null) {
                try {
                    Drawable d = new BitmapDrawable(activity.getResources(), bitmap);
                    Point size = new Point();
                    activity.getWindowManager().getDefaultDisplay().getSize(size);
                    // Lets calculate the ratio according to the screen width in px
                    int multiplier = size.x / bitmap.getWidth();
                    Log.d(TAG, "multiplier: " + multiplier);
                    levelListDrawable.addLevel(1, 1, d);
                    // Set bounds width  and height according to the bitmap resized size
                    levelListDrawable.setBounds(0, 0, bitmap.getWidth() * multiplier, bitmap.getHeight() * multiplier);
                    levelListDrawable.setLevel(1);
                    if (mCallback != null) {
                        Log.d(TAG, "got image, calling back");
                        mCallback.onComplete();
                    } else {
                        Log.d(TAG, "got image BUT callback was null");
                    }
                } catch (Exception e) { /* Like a null bitmap, etc. */ }
            }
        }
    }

    public interface MessageReadyCallback {
        public void onComplete();
    }

}
