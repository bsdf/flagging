package com.theporouscity.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.HashMap;
import java.util.List;

/**
 * Created by bergstroml on 2/29/16.
 */
@Root(name="Boards")
public class Boards {

    @ElementList(name="Board",inline=true)
    private List<Board> mBoards;

    @Element(name="URI")
    private String URI;

    private HashMap<Integer, Board> mBoardIdHashMap = null;

    public String getURI() {
        return URI;
    }

    public List<Board> getBoards() {
        return mBoards;
    }

    public Board getBoardById(int boardId) {

        if (mBoards == null) {
            return null;
        }

        if (mBoardIdHashMap == null) {
            mBoardIdHashMap = new HashMap<>(mBoards.size());
            for (Board board : mBoards) {
                mBoardIdHashMap.put(board.getId(), board);
            }
        }

        return mBoardIdHashMap.get(boardId);
    }

}
