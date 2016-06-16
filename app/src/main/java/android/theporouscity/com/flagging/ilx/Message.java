package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.sql.Time;
import java.util.Date;

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
}
