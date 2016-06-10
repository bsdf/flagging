package android.theporouscity.com.flagging.ilx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bergstroml on 3/9/16.
 */
@Root
public class Messages {

    @ElementList
    private List<Message> messages;

    public List getMessages() {
        return messages;
    }
}
