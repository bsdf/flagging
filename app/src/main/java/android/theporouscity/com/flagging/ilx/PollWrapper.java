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

    private RichThreadHolder mThreadHolder;

    public PollWrapper(RichThreadHolder threadHolder) {
        mThreadHolder = threadHolder;
    }

    public boolean isClosed() {
        return mThreadHolder.getThread().isPollClosed();
    }

    public int getSize() {
        if (mThreadHolder.getThread().isPollClosed()) {
            return mThreadHolder.getThread().getPollResults().size();
        } else {
            return mThreadHolder.getThread().getPollOptions().getPollOptions().size();
        }
    }

    public Spanned getItemTextForDisplay(int position, Activity activity,
                                         ILXTextOutputFormatter.MessageReadyCallback callback) {
        if (mThreadHolder.getThread().isPollClosed()) {
            return ILXTextOutputFormatter.getILXTextOutputFormatter().getBodyForDisplayShort(
                    mThreadHolder.getThread().getPollResults().get(position).getOption(),
                    mThreadHolder.getYoutubePlaceholderImage(),
                    mThreadHolder.getEmptyPlaceholderImage(),
                    mThreadHolder.getLinkColor(),
                    activity,
                    callback);
        } else {
            return ILXTextOutputFormatter.getILXTextOutputFormatter().getBodyForDisplayShort(
                    mThreadHolder.getThread().getPollOptions().getPollOptions().get(position).getOptionText(),
                    mThreadHolder.getYoutubePlaceholderImage(),
                    mThreadHolder.getEmptyPlaceholderImage(),
                    mThreadHolder.getLinkColor(),
                    activity,
                    callback);
        }
    }

    public int getVoteCount(int position) {
        return mThreadHolder.getThread().getPollResults().get(position).getVoteCount();
    }

    public String getVoteCountForDisplay(int position) {
        return String.valueOf(getVoteCount(position));
    }
}
