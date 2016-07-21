package android.theporouscity.com.flagging.ilx;

import android.text.Html;
import android.text.Spanned;

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

    public Spanned getItemTextForDisplay(int position) {
        if (mThread.isPollClosed()) {
            return Html.fromHtml(mThread.getPollResults().get(position).getOption().trim());
        } else {
            return Html.fromHtml(mThread.getPollOptions().getPollOptions().get(position).getOptionText().trim());
        }
    }

    public int getVoteCount(int position) {
        return mThread.getPollResults().get(position).getVoteCount();
    }

    public String getVoteCountForDisplay(int position) {
        return String.valueOf(getVoteCount(position));
    }
}
