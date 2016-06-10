package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

/**
 * Created by bergstroml on 2/29/16.
 */
@Root
public class Thread {
    @Element
    private int BoardId;

    @Element
    private int ThreadId;

    @Element
    private String Title;

    @Element
    private Date CreatedOn;

    @Element
    private Date LastUpdated;

    @Element
    private Boolean Deleted;

    @Element
    private Boolean Worksafe;

    @Element
    private Boolean Locked;

    @Element
    private Boolean Poll;

    @Element
    private Messages Messages;

    public Thread(int boardId, int threadId, String title, Date createdOn, Date lastUpdated, Boolean deleted, Boolean worksafe, Boolean locked, Boolean poll, Messages messages) {
        BoardId = boardId;
        ThreadId = threadId;
        Title = title;
        CreatedOn = createdOn;
        LastUpdated = lastUpdated;
        Deleted = deleted;
        Worksafe = worksafe;
        Locked = locked;
        Poll = poll;
        Messages = messages;
    }

    public int getBoardId() {
        return BoardId;
    }

    public int getThreadId() {
        return ThreadId;
    }

    public String getTitle() {
        return Title;
    }

    public Date getCreatedOn() {
        return CreatedOn;
    }

    public Date getLastUpdated() {
        return LastUpdated;
    }

    public Boolean getDeleted() {
        return Deleted;
    }

    public Boolean getWorksafe() {
        return Worksafe;
    }

    public Boolean getLocked() {
        return Locked;
    }

    public Boolean getPoll() {
        return Poll;
    }

    public Messages getMessages() { return Messages; }
}
