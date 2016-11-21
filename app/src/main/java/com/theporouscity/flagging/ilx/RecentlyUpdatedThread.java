package com.theporouscity.flagging.ilx;

import android.text.Html;
import android.text.Spanned;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.lang.*;
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

    public RecentlyUpdatedThread(@Element(name="BoardId") int BoardId,
                                 @Element(name="ThreadId") int ThreadId,
                                 @Element(name="Title") String Title,
                                 @Element(name="CreatedOn") Date CreatedOn,
                                 @Element(name="LastUpdated") Date LastUpdated,
                                 @Element(name="Deleted") Boolean Deleted,
                                 @Element(name="Worksafe") Boolean Worksafe,
                                 @Element(name="Locked") Boolean Locked,
                                 @Element(name="Poll") Boolean Poll) {
        this.BoardId = BoardId;
        this.ThreadId = ThreadId;
        this.Title = Title;
        this.CreatedOn = CreatedOn;
        this.LastUpdated = LastUpdated;
        this.Deleted = Deleted;
        this.Worksafe = Worksafe;
        this.Locked = Locked;
        this.Poll = Poll;
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

    public RecentlyUpdatedThread(Thread thread) {
        BoardId = thread.getBoardId();
        ThreadId = thread.getThreadId();
        Title = thread.getTitle();
        CreatedOn = thread.getCreatedOn();
        LastUpdated = thread.getLastUpdated();
        Deleted = thread.getDeleted();
        Worksafe = thread.getWorksafe();
        Locked = thread.getLocked();
        Poll = thread.isPoll();
    }
}
