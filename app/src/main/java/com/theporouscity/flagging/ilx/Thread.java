package com.theporouscity.flagging.ilx;

import android.text.Html;
import android.text.Spanned;

import com.theporouscity.flagging.util.PollClosingDate;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bergstroml on 2/29/16.
 */
@Parcel
@Root(name="Thread")
public class Thread {
    @Element(name="URI")
    String URI;

    @Element(name="BoardId")
    int BoardId;

    @Element(name="ThreadId")
    int ThreadId;

    @Element(name="MessageCount")
    int MessageCount;

    @Element(name="Title",data=true)
    String Title;

    @Element(name="CreatorId",data=true)
    String CreatorId;

    @Element(name="CreatedOn")
    Date CreatedOn;

    @Element(name="Key")
    String Key;

    @Element(name="LastUpdated")
    Date LastUpdated;

    @Element(name="Deleted")
    Boolean Deleted;

    @Element(name="Worksafe")
    Boolean Worksafe;

    @Element(name="Locked")
    Boolean Locked;

    @Element(name="Poll")
    Boolean Poll;

    @Element(name="PollClosingDate",required=false)
    PollClosingDate PollClosingDate;

    @ElementList(name="PollResults",required=false)
    List<Result> PollResults;

    @Element(name="PollOptions",required=false)
    PollOptions PollOptions;

    @ElementList(name="Messages")
    List<Message> Messages;

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

    public Spanned getTitleForDisplay() { return Html.fromHtml(Title.trim()); }

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

    public Boolean isPollClosed() {
        if (PollOptions == null) {
            return true;
        } else {
            return false;
        }
    }

    public void updateMetadata(int serverMessageCount, Date lastUpdated) {
        // should be called every time we make a new request for a thread we already have
        MessageCount = serverMessageCount;
        LastUpdated = lastUpdated;
    }

    public List<Message> getMessages() { return Messages; }

    public List<Result> getPollResults() { return PollResults; }

    public PollOptions getPollOptions() { return PollOptions; }

    public PollClosingDate getPollClosingDate() { return PollClosingDate; }

    public void addMessage(Message message) {
        Messages.add(message);
    }

    public int numUnloadedMessages() {
        return getServerMessageCount() - getLocalMessageCount();
    }

    public int getMessagePosition(int messageId) {
        // fuck it
        for (int i = 0; i < Messages.size(); i++) {
            if (Messages.get(i).getMessageId() == messageId) {
                return i;
            }
        }
        return -1;
    }

    public boolean noDuplicates() {

        Set<Integer> set = new HashSet<Integer>();

        for (Message message : Messages) {
            set.add(message.getMessageId());
        }

        if (set.size() < Messages.size()) {
            return false;
        }

        return true;
    }

    public int mostRecentMessageId() {
        return Messages.get(Messages.size()-1).getMessageId();
    }
}
