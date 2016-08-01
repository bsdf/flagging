package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
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

    public String getURI() {
        return URI;
    }

    public List<Board> getBoards() {
        return mBoards;
    }

}
