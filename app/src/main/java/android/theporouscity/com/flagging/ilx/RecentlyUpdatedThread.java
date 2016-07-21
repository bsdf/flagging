package android.theporouscity.com.flagging.ilx;

import android.text.Html;
import android.text.Spanned;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

/**
 * Created by bergstroml on 3/8/16.
 */

@Root(name="RecentlyUpdatedThread")
public class RecentlyUpdatedThread {

    @Element(name="BoardId")
    private int BoardId;

    @Element(name="ThreadId")
    private int ThreadId;

    @Element(name="Title")
    private String Title;

    @Element(name="CreatedOn")
    private Date CreatedOn;

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

    /*public RecentlyUpdatedThread(int BoardId, int ThreadId, String Title, Date CreatedOn, Date LastUpdated,
                                 Boolean Deleted, Boolean Worksafe, Boolean Locked, Boolean Poll) {
        this.BoardId = BoardId;
        this.ThreadId = ThreadId;
        this.Title = Title;
        this.CreatedOn = CreatedOn;
        this.LastUpdated = LastUpdated;
        this.Deleted = Deleted;
        this.Worksafe = Worksafe;
        this.Locked = Locked;
        this.Poll = Poll;
    }*/

    public int getBoardId() {
        return BoardId;
    }

    public int getThreadId() {
        return ThreadId;
    }

    public String getTitle() {
        return Title;
    }

    public Spanned getTitleForDisplay() { return Html.fromHtml(Title.trim()); }

    public Date getCreatedOn() { return CreatedOn; }

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
}
