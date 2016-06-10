package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Root;

import java.util.Date;

/**
 * Created by bergstroml on 3/8/16.
 */

@Root
public class RecentlyUpdatedThread extends Thread {

    public RecentlyUpdatedThread(int BoardId, int ThreadId, String Title, Date CreatedOn, Date LastUpdated,
                                 Boolean Deleted, Boolean Worksafe, Boolean Locked, Boolean Poll, Messages Messages) {
        super(BoardId, ThreadId, Title, CreatedOn, LastUpdated, Deleted, Worksafe, Locked, Poll, Messages);
    }
}
