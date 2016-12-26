package com.theporouscity.flagging.ilx;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

/**
 * Created by bergstroml on 2/29/16.
 */
@Parcel
@Root
public class Message {

    @Element(name = "MessageId")
    int messageId;

    @Element(name = "Deleted")
    Boolean deleted;

    @Element(name = "Timestamp")
    Date timestamp;

    @Element(data = true, name = "DisplayName")
    String displayName;

    @Element(data = true, name = "Body")
    String body;

    @ParcelConstructor
    public Message(@Element(name="MessageId") int messageId,
                   @Element(name="Deleted") Boolean deleted,
                   @Element(name="Timestamp") Date timestamp,
                   @Element(name="DisplayName") String displayName,
                   @Element(name="Body") String body) {
        this.messageId = messageId;
        this.deleted = deleted;
        this.timestamp = timestamp;
        this.displayName = displayName;
        this.body = body;
    }

    public int getMessageId() { return messageId; }

    public Boolean getDeleted() { return deleted; }

    public Date getTimestamp() { return timestamp; }

    public String getDisplayName() { return displayName; }

    public String getBody() { return body; }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
