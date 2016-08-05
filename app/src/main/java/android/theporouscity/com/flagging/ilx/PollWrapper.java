package android.theporouscity.com.flagging.ilx;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.theporouscity.com.flagging.ILXTextOutputFormatter;

/**
 * Created by bergstroml on 7/20/16.
 */

public class PollWrapper {

    private Thread mThread;

    public PollWrapper(Thread thread) {
        mThread = thread;
    }

    public boolean isClosed() {
        return mThread.isPollClosed();
    }

    public int getSize() {
        if (mThread.isPollClosed()) {
            return mThread.getPollResults().size();
        } else {
            return mThread.getPollOptions().getPollOptions().size();
        }
    }

    public Spanned getItemTextForDisplay(int position, Drawable youtubePlaceholder, int linkColor, Activity activity) {
        if (mThread.isPollClosed()) {
            return ILXTextOutputFormatter.getILXTextOutputFormatter().getBodyForDisplayShort(
                    mThread.getPollResults().get(position).getOption(), youtubePlaceholder, linkColor, activity);
        } else {
            return ILXTextOutputFormatter.getILXTextOutputFormatter().getBodyForDisplayShort(
                    mThread.getPollOptions().getPollOptions().get(position).getOptionText(), youtubePlaceholder, linkColor, activity);
        }
    }

    public int getVoteCount(int position) {
        return mThread.getPollResults().get(position).getVoteCount();
    }

    public String getVoteCountForDisplay(int position) {
        return String.valueOf(getVoteCount(position));
    }
}
