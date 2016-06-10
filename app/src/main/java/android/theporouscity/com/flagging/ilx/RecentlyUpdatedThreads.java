package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bergstroml on 3/8/16.
 */

@Root
public class RecentlyUpdatedThreads {

    @ElementList(inline=true)
    private List<RecentlyUpdatedThread> mRecentlyUpdatedThreads;

    @Element
    private String URI;

    @Element
    private int TotalMessages;

    public RecentlyUpdatedThreads(@Element(name="RecentlyUpdatedThreads") List<RecentlyUpdatedThread> recentlyUpdatedThreads,
                                  @Element(name="URI") String URI,
                                  @Element(name="TotalMessages") int totalMessages) {
        mRecentlyUpdatedThreads = recentlyUpdatedThreads;
        this.URI = URI;
        TotalMessages = totalMessages;
    }

    public List<RecentlyUpdatedThread> getRecentlyUpdatedThreads() {
        return mRecentlyUpdatedThreads;
    }

    public String getURI() {
        return URI;
    }

    public int getTotalMessages() {
        return TotalMessages;
    }
}
