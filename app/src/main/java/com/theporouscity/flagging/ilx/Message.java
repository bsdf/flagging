package com.theporouscity.flagging.ilx;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

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
    }

    public int getMessageId() { return MessageId; }

    public Boolean getDeleted() { return Deleted; }

    public Date getTimestamp() { return Timestamp; }

    public String getDisplayName() { return DisplayName; }

    public String getBody() { return Body; }

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
