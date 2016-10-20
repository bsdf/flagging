package android.theporouscity.com.flagging.ilx;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.theporouscity.com.flagging.R;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bergstroml on 10/10/16.
 */

public class RichThreadHolder {

    private Thread mThread;
    private ArrayList<RichMessageHolder> mRichMessageHolders;
    private Drawable mYoutubePlaceholderImage;
    private Drawable mEmptyPlaceholderImage;
    private int mLinkColor;
    private static final String TAG = "RichThreadHolder";

    public RichThreadHolder(Thread thread, Context context) {
        getDrawingResources(context);
        mThread = thread;
        mRichMessageHolders = new ArrayList<RichMessageHolder>();
        for (Message message: mThread.getMessages()) {
            mRichMessageHolders.add(
                    new RichMessageHolder(message, mYoutubePlaceholderImage,
                                            mEmptyPlaceholderImage, mLinkColor));
        }
    }

    public Thread getThread() {
        return mThread;
    }

    public ArrayList<RichMessageHolder> getRichMessageHolders() {
        return mRichMessageHolders;
    }

    public Drawable getYoutubePlaceholderImage() {
        return mYoutubePlaceholderImage;
    }

    public Drawable getEmptyPlaceholderImage() {
        return mEmptyPlaceholderImage;
    }

    public int getLinkColor() {
        return mLinkColor;
    }

    public void addMessages(int startPosition, List<Message> messages) {
        Log.d(TAG, "size before " + Integer.toString(mRichMessageHolders.size()) + " messages");

        mThread.getMessages().addAll(0, messages);

        RichMessageHolder messageHolder;
        for (int i = 0; i < messages.size(); i++) {
            messageHolder = new RichMessageHolder(messages.get(i),
                                                    mYoutubePlaceholderImage,
                                                    mEmptyPlaceholderImage,
                                                    mLinkColor);
            mRichMessageHolders.add(startPosition + i, messageHolder);
        }
        Log.d(TAG, "size after " + Integer.toString(mRichMessageHolders.size()) + " messages");
    }

    public void addMessages(List<Message> messages) {
        addMessages(mRichMessageHolders.size(), messages);
    }

    public void getDrawingResources(Context context) {
        mYoutubePlaceholderImage = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_slideshow);
        mYoutubePlaceholderImage.setBounds(0, 0, mYoutubePlaceholderImage.getIntrinsicWidth(), mYoutubePlaceholderImage.getIntrinsicHeight());
        mEmptyPlaceholderImage = ContextCompat.getDrawable(context, R.drawable.ic_empty_image);
        mLinkColor = ContextCompat.getColor(context, R.color.colorAccent);
    }

    public void prepMessagesForDisplay(int startPosition, Activity activity) {
        ArrayList<RichMessageHolder> messagesToPrep = new ArrayList<RichMessageHolder>();

        // first prep visible messages and later, in chronological order
        int numTotalMessages = mRichMessageHolders.size();
        if (startPosition < (numTotalMessages - 1)) {
            messagesToPrep.addAll(mRichMessageHolders.subList(startPosition + 1, numTotalMessages));
        }

        // then prep earlier messages, in reverse chronological order
        if (startPosition > 0) {
            for (int i = startPosition - 1; i > 0; i--) {
                messagesToPrep.add(mRichMessageHolders.get(i));
            }
        }

        new PrepMessagesTask(activity).execute(messagesToPrep);

    }

    class PrepMessagesTask extends AsyncTask<ArrayList<RichMessageHolder>, Void, Void>
    {
        private Activity mActivity;

        public PrepMessagesTask(Activity activity) {
            super();
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(ArrayList<RichMessageHolder>... messages) {

            ArrayList<RichMessageHolder> theMessages = messages[0];

            Log.d(TAG, "prepping " + Integer.toString(theMessages.size()) + " messages");

            for (RichMessageHolder m : theMessages) {
                m.prepDisplayNameForDisplay();
                m.prepBodyForDisplayShort(mActivity, null);
            }

            return null;
        }
    }
}
