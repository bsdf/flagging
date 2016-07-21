package android.theporouscity.com.flagging.ilx;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.URLSpan;
import android.theporouscity.com.flagging.R;
import android.theporouscity.com.flagging.ViewThreadActivity;
import android.util.Log;
import android.view.View;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.xml.sax.XMLReader;

import java.sql.Time;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bergstroml on 2/29/16.
 */
@Root
public class Message {

    @Element
    private int MessageId;

    @Element
    private Boolean Deleted;

    @Element
    private Date Timestamp;

    @Element(data=true)
    private String DisplayName;

    @Element(data=true)
    private String Body;

    public int getMessageId() { return MessageId; }

    public Boolean getDeleted() { return Deleted; }

    public Date getTimestamp() { return Timestamp; }

    public String getDisplayName() { return DisplayName; }

    public Spanned getDisplayNameForDisplay() {
        return Html.fromHtml(DisplayName, null, null);
    }

    public String getBody() { return Body; }

    public Spanned getBodyForDisplayShort(Activity activity) {
        Spanned newString = Html.fromHtml(Body, null, new ILXTagHandler(activity));
        return fixSpannable(new SpannableStringBuilder(newString), activity);
    }

    public void setMessageId(int messageId) {
        MessageId = messageId;
    }

    public void setDeleted(Boolean deleted) {
        Deleted = deleted;
    }

    public void setTimestamp(Date timestamp) {
        Timestamp = timestamp;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public void setBody(String body) {
        Body = body;
    }

    private SpannableStringBuilder fixSpannable(SpannableStringBuilder spannable, Activity activity) {

        if (spannable == null) {
            return null;
        }

        // trim it

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
            if (span.getURL().startsWith("http://www.ilxor.com/ILX/ThreadSelectedControllerServlet")) {
                int start = spannable.getSpanStart(span);
                int end = spannable.getSpanEnd(span);
                String url = span.getURL();
                spannable.removeSpan(span);
                spannable.setSpan(new ILXURLSpan(url, activity), start, end, 0);
            }
        }

        return spannable;
    }

    private static class ILXURLSpan extends ClickableSpan {

        private int mBoardId;
        private int mThreadId;

        private Activity mActivity;

        public ILXURLSpan(String url, Activity activity) {

            mActivity = activity;

            Pattern pattern = Pattern.compile("boardid=([0-9]+)&threadid=([0-9]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                mBoardId = Integer.parseInt(matcher.group(1));
                mThreadId = Integer.parseInt(matcher.group(2));
            } else {
                mBoardId = -1;
                mThreadId = -1;
            }

            TextPaint textPaint = new TextPaint();
            textPaint.linkColor = ContextCompat.getColor(activity, R.color.colorAccent);
            updateDrawState(textPaint);
        }

        @Override
        public void onClick(View widget) {
            Log.d("ILXURLSpan", "ilx link clicked");
            if (mBoardId != -1 && mThreadId != -1) {
                Intent intent = ViewThreadActivity.newIntent(mActivity, mBoardId, mThreadId);
                mActivity.startActivity(intent);
            }
        }
    }

    private class ILXTagHandler implements Html.TagHandler {

        private Activity mActivity;

        public ILXTagHandler(Activity activity) {
            mActivity = activity;
        }

        public void handleTag(boolean opening, String tag, Editable output,
                              XMLReader xmlReader) {
            if (tag.equalsIgnoreCase("object")) {
                if (opening) {
                    processObject(output);
                }
            }
        }

        private void processObject(Editable output) {
            Drawable d = mActivity.getDrawable(android.R.drawable.ic_menu_slideshow);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);

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
}
