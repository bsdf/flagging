package com.theporouscity.flagging.ilx;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by lukas on 12/23/16.
 */

@Root(name="Bookmarks")
public class Bookmarks {

    @ElementList(name="Bookmark", inline=true)
    private List<Bookmark> mBookmarks;

    public List<Bookmark> getBookmarks() {
        return mBookmarks;
    }
}
