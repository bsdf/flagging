package android.theporouscity.com.flagging.ilx;

import android.theporouscity.com.flagging.ILXRequestor;
import android.util.Xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

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

    public List getBoards() {
        return mBoards;
    }

}
