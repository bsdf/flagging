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
import android.theporouscity.com.flagging.ILXTextOutputFormatter;
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
public class Message implements Parcelable {

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

    private int BoardId;
    private int ThreadId;

    private Spanned mDisplayNameForDisplay;
    private Spanned mBodyForDisplayShort;

    public Message(@Element(name="MessageId") int messageId,
                   @Element(name="Deleted") Boolean deleted,
                   @Element(name="Timestamp") Date timestamp,
                   @Element(name="DisplayName") String displayName,
                   @Element(name="Body") String body) {
        MessageId = messageId;
        Deleted = deleted;
        Timestamp = timestamp;
        DisplayName = displayName;
        Body = body;

        mDisplayNameForDisplay = null;
        mBodyForDisplayShort = null;
    }

    public int getMessageId() { return MessageId; }

    public Boolean getDeleted() { return Deleted; }

    public Date getTimestamp() { return Timestamp; }

    public String getDisplayName() { return DisplayName; }

    public Spanned getDisplayNameForDisplay() {
        prepDisplayNameForDisplay();
        return mDisplayNameForDisplay;
    }

    public void prepDisplayNameForDisplay() {
        if (mDisplayNameForDisplay == null) {
            mDisplayNameForDisplay = Html.fromHtml(DisplayName, null, null);
        }
    }

    public String getBody() { return Body; }

    public Spanned getBodyForDisplayShort(Drawable youtubePlaceholderImage, int linkColor, Activity activity) { // TODO probably shouldn't know about formatter
        prepBodyForDisplayShort(youtubePlaceholderImage, linkColor, activity);
        return mBodyForDisplayShort;
    }

    public void prepBodyForDisplayShort(Drawable youtubePlaceholderImage, int linkColor, Activity activity) {
        if (mBodyForDisplayShort == null) {
            mBodyForDisplayShort = ILXTextOutputFormatter.getILXTextOutputFormatter()
                    .getBodyForDisplayShort(Body, youtubePlaceholderImage, linkColor, activity);
        }
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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(MessageId);
        if (Deleted) {
            parcel.writeInt(1);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeLong(Timestamp.getTime());
        parcel.writeString(DisplayName);
        parcel.writeString(Body);
    }

    private Message(Parcel parcel) {
        MessageId = parcel.readInt();
        if (parcel.readInt() == 1) {
            Deleted = true;
        } else {
            Deleted = false;
        }
        Timestamp = new Date(parcel.readLong());
        DisplayName = parcel.readString();
        Body = parcel.readString();
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
