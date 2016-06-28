package android.theporouscity.com.flagging.ilx;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.xml.sax.XMLReader;

import java.sql.Time;
import java.util.Date;
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

    public String getBody() { return Body; }

    public Spanned getBodyForDisplayShort(Activity activity) {
        Spanned newString = Html.fromHtml(Body, null, new ILXTagHandler(activity));
        return trimSpannable(new SpannableStringBuilder(newString));
    }

    private SpannableStringBuilder trimSpannable(SpannableStringBuilder spannable) {

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

        return spannable.delete(0, trimStart).delete(spannable.length() - trimEnd, spannable.length());
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
