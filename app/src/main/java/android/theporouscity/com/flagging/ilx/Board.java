package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bergstroml on 2/29/16.
 */
@Root(name="Board")
public class Board {

    @Element(name="Name")
    private String Name;

    @Element(name="Description")
    private String Description;

    @Element(name="BoardId")
    private int BoardId;

    @Element(name="Private")
    private String Private;

    @Element(name="Popular")
    private String Popular;

    public int getBoardId() {
        return BoardId;
    }

    public String isPrivate() {

        return Private;
    }

    public String isPopular() {
        return Popular;
    }

    public Board(@Element(name="Name") String name,
                 @Element(name="Description") String description,
                 @Element(name="BoardId") int id,
                 @Element(name="Private") String isPrivate,
                 @Element(name="Popular") String popular) {
        this.Name = name;
        this.Description = description;
        this.BoardId = id;
        this.Private = isPrivate;
        this.Popular = popular;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() { return Description; }

    public int getId() {
        return BoardId;
    }
}
