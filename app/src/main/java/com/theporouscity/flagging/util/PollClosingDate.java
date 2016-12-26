package com.theporouscity.flagging.util;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.Date;

/**
 * Created by bergstroml on 6/29/16.
 */

@Parcel
public class PollClosingDate {
    Date date;

    @ParcelConstructor
    public PollClosingDate(Date date) {
        this.date = date;
    }

    public Date getDate() { return date; }
}
