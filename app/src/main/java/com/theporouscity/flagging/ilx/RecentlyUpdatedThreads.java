package com.theporouscity.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bergstroml on 3/8/16.
 */

@Root(name="RecentlyUpdatedThreads")
public class RecentlyUpdatedThreads {

    @ElementList(name="RecentlyUpdatedThread",inline=true,required=false)
    private List<RecentlyUpdatedThread> mRecentlyUpdatedThreads;

    @Element(name="URI")
    private String URI;

    @Element(name="TotalMessages")
    private int TotalMessages;

    /*public RecentlyUpdatedThreads(@ElementList(name="RecentlyUpdatedThread") List<RecentlyUpdatedThread> recentlyUpdatedThreads,
                                  @Element(name="URI") String URI,
                                  @Element(name="TotalMessages") int totalMessages) {
        mRecentlyUpdatedThreads = recentlyUpdatedThreads;
        this.URI = URI;
        TotalMessages = totalMessages;
    }*/

    public List<RecentlyUpdatedThread> getRecentlyUpdatedThreads() {
        return mRecentlyUpdatedThreads;
    }

    public String getURI() {
        return URI;
    }

    public int getTotalMessages() {
        return TotalMessages;
    }

    public void setRecentlyUpdatedThreads(List<RecentlyUpdatedThread> recentlyUpdatedThreads) {
        mRecentlyUpdatedThreads = recentlyUpdatedThreads;
    }
}
