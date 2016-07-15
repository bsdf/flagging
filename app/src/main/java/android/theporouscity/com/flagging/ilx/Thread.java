package android.theporouscity.com.flagging.ilx;

import android.theporouscity.com.flagging.ILXRequestor;
import android.theporouscity.com.flagging.PollClosingDate;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

/**
 * Created by bergstroml on 2/29/16.
 */
@Root(name="Thread")
public class Thread {
    @Element(name="URI")
    private String URI;

    @Element(name="BoardId")
    private int BoardId;

    @Element(name="ThreadId")
    private int ThreadId;

    @Element(name="MessageCount")
    private int MessageCount;

    @Element(name="Title",data=true)
    private String Title;

    @Element(name="CreatorId",data=true)
    private String CreatorId;

    @Element(name="CreatedOn")
    private Date CreatedOn;

    @Element(name="Key")
    private String Key;

    @Element(name="LastUpdated")
    private Date LastUpdated;

    @Element(name="Deleted")
    private Boolean Deleted;

    @Element(name="Worksafe")
    private Boolean Worksafe;

    @Element(name="Locked")
    private Boolean Locked;

    @Element(name="Poll")
    private Boolean Poll;

    @Element(name="PollClosingDate",required=false)
    private PollClosingDate PollClosingDate;

    @ElementList(name="PollResults",required=false)
    private List<Result> PollResults;

    @Element(name="PollOptions",required=false)
    private PollOptions PollOptions;

    @ElementList(name="Messages")
    private List<Message> Messages;

    /*public Thread(int boardId, int threadId, String title, Date createdOn, Date lastUpdated, Boolean deleted, Boolean worksafe, Boolean locked, Boolean poll, List<Message> messages) {
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
    }*/

    public String getURI() { return URI; }

    public int getBoardId() { return BoardId; }

    public int getThreadId() {
        return ThreadId;
    }

    public int getServerMessageCount() { return MessageCount; }

    public int getLocalMessageCount() { return Messages.size(); }

    public String getTitle() {
        return Title;
    }

    public String getCreatorId() { return CreatorId; }

    public Date getCreatedOn() {
        return CreatedOn;
    }

    public String getKey() { return Key; }

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

    public Boolean isPoll() {
        return Poll;
    }

    public Boolean isPollClosed() { return false; } // TODO: do stuff

    public List<Message> getMessages() { return Messages; }

    public List<Result> getPollResults() { return PollResults; }

    public PollOptions getPollOptions() { return PollOptions; }

    public void addMessage(Message message) {
        Messages.add(message);
    }
}
