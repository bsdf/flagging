package com.theporouscity.flagging.ilx;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXTextOutputFormatter;
import com.theporouscity.flagging.R;
import com.theporouscity.flagging.util.ServerInaccessibleException;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by bergstroml on 10/10/16.
 */

public class RichThreadHolder {

    private static final String TAG = "RichThreadHolder";
    private Thread mThread;
    private ILXAccount mAccount;
    private OkHttpClient mAccountClient;
    private ArrayList<RichMessageHolder> mRichMessageHolders;
    private Drawable mYoutubePlaceholderImage;
    private Drawable mEmptyPlaceholderImage;
    private int mLinkColor;
    private ILXTextOutputFormatter mILXTextOutputFormatter;
    private String mHtmlThread = null;

    public RichThreadHolder(Thread thread, ILXAccount account, OkHttpClient sharedClient,
                            Context context, ILXTextOutputFormatter mILXTextOutputFormatter) {
        this.mILXTextOutputFormatter = mILXTextOutputFormatter;
        getDrawingResources(context);
        mThread = thread;
        mAccount = account;
        mAccountClient = mAccount.getHttpClient(context, sharedClient);
        mRichMessageHolders = new ArrayList<>();
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
        new PrepMessagesTask(activity, false).execute(messagesToPrep);
    }

    public void ingestEarlierMessages(Thread thread, Activity activity, int count) {
        addMessages(0, thread.getMessages().subList(0, count));
        getThread().updateMetadata(thread.getServerMessageCount(), thread.getLastUpdated());
        prepEarlierMessages(count, activity);
    }

    public void prepEarlierMessages(int startPosition, Activity activity) {
        ArrayList<RichMessageHolder> messagesToPrep = getEarlierMessages(startPosition);
        new PrepMessagesTask(activity, false).execute(messagesToPrep);

    }

    public void ingestLaterMessages(Thread thread, Activity activity, int start, int end) {
        addMessages(thread.getMessages().subList(start, end));
        getThread().updateMetadata(thread.getServerMessageCount(), thread.getLastUpdated());
        prepLaterMessages(end - start, activity);
    }

    public void prepLaterMessages(int numMessages, Activity activity) {
        //ArrayList<RichMessageHolder messagesToPrep = >
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
        private boolean mLoadedLaterMessages;

        public PrepMessagesTask(Activity activity, boolean loadedLaterMessages) {
            super();
            mActivity = activity;
            mLoadedLaterMessages = loadedLaterMessages;
        }

        @Override
        protected Void doInBackground(ArrayList<RichMessageHolder>... messages) {

            ArrayList<RichMessageHolder> theMessages = messages[0];

            //TODO why aren't we prepping as many messages as we're loading
            Log.d(TAG, "prepping " + Integer.toString(theMessages.size()) + " messages");

            // in order to bookmark messages we need an id for each message (not the message id)
            // unfortunately it's only available in the html for the thread
            //
            // also unfortunate, seems the only way to guarantee we get all the messages we
            // need in html is to load the entire thread

            if (mHtmlThread == null || mLoadedLaterMessages == true) {
                String url = mAccount.getThreadHtmlUrl(mThread.getBoardId(), mThread.getThreadId());
                url = url + "&showall=true";

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response response = mAccountClient.newCall(request).execute();
                    if (response.code() != 200) {
                        Log.d(TAG, "Couldn't get thread html");
                    }
                    mHtmlThread = response.body().string();
                } catch (Exception e) {
                    Log.d(TAG, "Error getting thread html: " + e.toString());
                }
            }

            for (RichMessageHolder m : theMessages) {
                m.prepDisplayNameForDisplay();
                m.prepBodyForDisplayShort(mActivity, null);
            }

            return null;
        }
    }
}
