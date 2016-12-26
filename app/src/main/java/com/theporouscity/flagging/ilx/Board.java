package com.theporouscity.flagging.ilx;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bergstroml on 2/29/16.
 */
@Parcel
@Root(name="Board")
public class Board {

    @Element(name="Name")
    String name;

    @Element(name="Description")
    String description;

    @Element(name="BoardId")
    int boardId;

    @Element(name="Private")
    String isPrivate;

    @Element(name="Popular")
    String popular;

    boolean enabled;

    public String isPrivate() {
        return isPrivate;
    }

    public String isPopular() {
        return popular;
    }

    @ParcelConstructor
    public Board(@Element(name="Name") String name,
                 @Element(name="Description") String description,
                 @Element(name="BoardId") int boardId,
                 @Element(name="Private") String isPrivate,
                 @Element(name="Popular") String popular) {
        this.name = name;
        this.description = description;
        this.boardId = boardId;
        this.isPrivate = isPrivate;
        this.popular = popular;
    }

    public String getName() {
        return name;
    }

    public String getDescription() { return description; }

    public int getId() {
        return boardId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
