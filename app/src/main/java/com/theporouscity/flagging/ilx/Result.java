package com.theporouscity.flagging.ilx;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bergstroml on 6/28/16.
 */

@Parcel
@Root(name="Result")
public class Result {
    @Element(name="Option")
    String Option;

    @Element(name="VoteCount")
    int VoteCount;

    public String getOption() {
        return Option;
    }

    public int getVoteCount() {
        return VoteCount;
    }
}
