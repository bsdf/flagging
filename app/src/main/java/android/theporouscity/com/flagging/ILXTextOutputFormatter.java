package android.theporouscity.com.flagging;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.theporouscity.com.flagging.ilx.Message;
import android.util.Log;
import android.view.View;

import org.xml.sax.XMLReader;

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

    public Spanned getBodyForDisplayShort(String text, Drawable youtubePlaceholderImage, int linkColor, Activity activity) {
        Spanned newString = Html.fromHtml(text, null, new ILXTagHandler(youtubePlaceholderImage));
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
                    processObject(output);
                }
            }
        }

        private void processObject(Editable output) {

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

}
