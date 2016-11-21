package android.theporouscity.com.flagging.ilx;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.theporouscity.com.flagging.ILXTextOutputFormatter;
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
    private ILXTextOutputFormatter mILXTextOutputFormatter;
    private static final String TAG = "RichThreadHolder";

    public RichThreadHolder(Thread thread, Context context, ILXTextOutputFormatter mILXTextOutputFormatter) {
        this.mILXTextOutputFormatter = mILXTextOutputFormatter;
        getDrawingResources(context);
        mThread = thread;
        mRichMessageHolders = new ArrayList<RichMessageHolder>();
        for (Message message : mThread.getMessages()) {
            mRichMessageHolders.add(
                    new RichMessageHolder(this.mILXTextOutputFormatter, message, mYoutubePlaceholderImage,
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

        mThread.getMessages().addAll(0, messages);

        RichMessageHolder messageHolder;
        for (int i = 0; i < messages.size(); i++) {
            messageHolder = new RichMessageHolder(this.mILXTextOutputFormatter,
                    messages.get(i),
                    mYoutubePlaceholderImage,
                    mEmptyPlaceholderImage,
                    mLinkColor);
            mRichMessageHolders.add(startPosition + i, messageHolder);
        }
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

    public void prepAllMessagesForDisplay(int startPosition, Activity activity) {
        // first prep visible messages and later, in chronological order
        ArrayList<RichMessageHolder> messagesToPrep = getThisMessageAndLater(startPosition);
        // then prep earlier messages, in reverse chronological order
        messagesToPrep.addAll(getEarlierMessages(startPosition));
        new PrepMessagesTask(activity).execute(messagesToPrep);
    }

    public void prepEarlierMessages(int startPosition, Activity activity) {
        ArrayList<RichMessageHolder> messagesToPrep = getEarlierMessages(startPosition);
        new PrepMessagesTask(activity).execute(messagesToPrep);

    }

    private ArrayList<RichMessageHolder> getThisMessageAndLater(int startPosition) {
        ArrayList<RichMessageHolder> messagesToPrep = new ArrayList<RichMessageHolder>();

        int numTotalMessages = mRichMessageHolders.size();
        if (startPosition < (numTotalMessages - 1)) {
            messagesToPrep.addAll(mRichMessageHolders.subList(startPosition + 1, numTotalMessages));
        }

        return messagesToPrep;
    }

    private ArrayList<RichMessageHolder> getEarlierMessages(int startPosition) {
        ArrayList<RichMessageHolder> messagesToPrep = new ArrayList<RichMessageHolder>();
        if (startPosition > 0) {
            for (int i = startPosition - 1; i > 0; i--) {
                messagesToPrep.add(mRichMessageHolders.get(i));
            }
        }
        return messagesToPrep;
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

            //TODO why aren't we prepping as many messages as we're loading
            Log.d(TAG, "prepping " + Integer.toString(theMessages.size()) + " messages");

            for (RichMessageHolder m : theMessages) {
                m.prepDisplayNameForDisplay();
                m.prepBodyForDisplayShort(mActivity, null);
            }

            return null;
        }
    }
}
