package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bergstroml on 6/28/16.
 */

@Root(name="Result")
public class Result {
    @Element(name="Option")
    private String Option;

    @Element(name="VoteCount")
    private int VoteCount;

    public String getOption() {
        return Option;
    }

    public int getVoteCount() {
        return VoteCount;
    }
}
