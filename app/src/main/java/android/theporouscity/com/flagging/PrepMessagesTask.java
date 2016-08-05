package android.theporouscity.com.flagging;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.theporouscity.com.flagging.ilx.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bergstroml on 8/4/16.
 */

public class PrepMessagesTask extends AsyncTask<ArrayList<Message>, Void, Void> {

    private Drawable mYoutubePlaceholderImage;
    private int mLinkColor;
    private Activity mActivity;

    public PrepMessagesTask(Drawable youtubePlaceholderImage, int linkColor, Activity activity) {
        super();
        mYoutubePlaceholderImage = youtubePlaceholderImage;
        mLinkColor = linkColor;
        mActivity = activity;
    }

    @Override
    protected Void doInBackground(ArrayList<Message>... messages) {

        ArrayList<Message> theMessages = messages[0];

        for (Message m : theMessages) {
            m.prepDisplayNameForDisplay();
            m.prepBodyForDisplayShort(mYoutubePlaceholderImage, mLinkColor, mActivity);
        }

        return null;
    }
}
