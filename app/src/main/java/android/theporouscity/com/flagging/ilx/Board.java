package android.theporouscity.com.flagging.ilx;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bergstroml on 2/29/16.
 */
@Root(name="Board")
public class Board implements Parcelable{

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Name);
        parcel.writeString(Description);
        parcel.writeInt(BoardId);
        parcel.writeString(Private);
        parcel.writeString(Popular);
    }

    public Board(Parcel parcel) {
        Name = parcel.readString();
        Description = parcel.readString();
        BoardId = parcel.readInt();
        Private = parcel.readString();
        Popular = parcel.readString();
    }

    public static final Parcelable.Creator<Board> CREATOR = new Parcelable.Creator<Board>() {

        public Board createFromParcel(Parcel parcel) {
            return new Board(parcel);
        }

        public Board[] newArray(int size) {
            return new Board[size];
        }
    };
}
