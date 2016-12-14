package com.theporouscity.flagging.ilx;

import android.app.Activity;
import android.text.Spanned;
import com.theporouscity.flagging.util.ILXTextOutputFormatter;

import java.util.ArrayList;

/**
 * Created by bergstroml on 7/20/16.
 */

public class PollWrapper {

    private RichThreadHolder mThreadHolder;
    private ArrayList<RichPollItem> mRichPollItems;
    private ILXTextOutputFormatter mILXTextOutputFormatter;

    public PollWrapper(RichThreadHolder threadHolder, ILXTextOutputFormatter mILXTextOutputFormatter) {
        mThreadHolder = threadHolder;
        this.mILXTextOutputFormatter = mILXTextOutputFormatter;
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

    public void prepPollItems(Activity activity) {
        mRichPollItems = new ArrayList<RichPollItem>();
        RichPollItem item = null;
        if (mThreadHolder != null && mThreadHolder.getThread() != null) {
            if (mThreadHolder.getThread().isPollClosed() && mThreadHolder.getThread().getPollResults() != null) {
                for (Result result : mThreadHolder.getThread().getPollResults()) {
                    item = new RichPollItem(true, result, null);
                    item.prepItemTextForDisplay(activity, null);
                    mRichPollItems.add(item);
                }
            } else if (mThreadHolder.getThread().getPollOptions() != null && mThreadHolder.getThread().getPollOptions().getPollOptions() != null) {
                for (PollOptions.PollOption option : mThreadHolder.getThread().getPollOptions().getPollOptions()) {
                    item = new RichPollItem(false, null, option);
                    item.prepItemTextForDisplay(activity, null);
                    mRichPollItems.add(item);
                }
            }
        }
    }

    public Spanned getItemTextForDisplay(int position, Activity activity,
                                         ILXTextOutputFormatter.MessageReadyCallback callback) {
        if (mRichPollItems == null) {
            prepPollItems(activity);
        }

        return mRichPollItems.get(position).getItemTextForDisplay(activity, callback);
    }

    public int getVoteCount(int position) {
        return mThreadHolder.getThread().getPollResults().get(position).getVoteCount();
    }

    public String getVoteCountForDisplay(int position) {
        return String.valueOf(getVoteCount(position));
    }

    private class RichPollItem {
        boolean mIsResult;
        private Result mResult;
        private PollOptions.PollOption mOption;
        private ILXTextOutputFormatter.MessageReadyCallback mCallback;
        private Spanned mItemTextForDisplay;

        public RichPollItem(boolean isResult, Result result, PollOptions.PollOption option) {
            mIsResult = isResult;
            mResult = result;
            mOption = option;
        }

        public Spanned getItemTextForDisplay(Activity activity, ILXTextOutputFormatter.MessageReadyCallback callback) {
            if (mCallback == null && callback != null) {
                mCallback = callback;
            }

            if (mItemTextForDisplay == null) {
                prepItemTextForDisplay(activity, mCallback);
            }

            return mItemTextForDisplay;
        }

        public void prepItemTextForDisplay(Activity activity, ILXTextOutputFormatter.MessageReadyCallback callback) {
            if (mCallback == null && callback != null) {
                mCallback = callback;
            }

            if (mItemTextForDisplay == null) {
                if (mIsResult) {
                    mItemTextForDisplay = mILXTextOutputFormatter.getBodyForDisplayShort(
                            mResult.getOption(),
                            mThreadHolder.getYoutubePlaceholderImage(),
                            mThreadHolder.getEmptyPlaceholderImage(),
                            mThreadHolder.getLinkColor(),
                            activity,
                            () -> { if (mCallback != null) { mCallback.onComplete(); } });
                } else {
                    mItemTextForDisplay = mILXTextOutputFormatter.getBodyForDisplayShort(
                            mOption.getOptionText(),
                            mThreadHolder.getYoutubePlaceholderImage(),
                            mThreadHolder.getEmptyPlaceholderImage(),
                            mThreadHolder.getLinkColor(),
                            activity,
                            () -> { if (mCallback != null) { mCallback.onComplete(); } });
                }
            }
        }
    }

}
