package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.theporouscity.com.flagging.ilx.Message;
import android.theporouscity.com.flagging.ilx.PollWrapper;
import android.theporouscity.com.flagging.ilx.Thread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bergstroml on 6/15/16.
 */

public class ViewThreadFragment extends Fragment {

    private static final String TAG = "ViewThreadFragment";
    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_MESSAGE_ID = "message_id";
    private static final int mDefaultMessagesChunk = 100;
    private Thread mThread;
    private PollWrapper mPollWrapper;
    private RecyclerView mRecyclerView;
    private ThreadAdapter mThreadAdapter;
    private ProgressBar mProgressBar;
    private TextView mLoadErrorTextView;
    private boolean mFetching;
    private int mBoardId;
    private int mThreadId;
    private int mInitialMessageId;
    private SwipeRefreshLayoutBottom mSwipeRefreshLayoutBottom;
    private Drawable mYoutubePlaceholderImage;
    private int mLinkColor;

    public ViewThreadFragment() { }

    public static ViewThreadFragment newInstance(int boardId, int threadId, int messageId) {
        ViewThreadFragment fragment = new ViewThreadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOARD_ID, boardId);
        args.putInt(ARG_THREAD_ID, threadId);
        args.putInt(ARG_MESSAGE_ID, messageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBoardId = getArguments().getInt(ARG_BOARD_ID);
            mThreadId = getArguments().getInt(ARG_THREAD_ID);
            mInitialMessageId = getArguments().getInt(ARG_MESSAGE_ID);
            mYoutubePlaceholderImage = getActivity().getDrawable(android.R.drawable.ic_menu_slideshow);
            mYoutubePlaceholderImage.setBounds(0, 0, mYoutubePlaceholderImage.getIntrinsicWidth(), mYoutubePlaceholderImage.getIntrinsicHeight());
            mLinkColor = ContextCompat.getColor(getActivity(), R.color.colorAccent);
            loadThread();
        }
    }

    private void loadThread() {
        mFetching = true;
        if (mInitialMessageId == -1) {
            ILXRequestor.getILXRequestor().getThread(mBoardId, mThreadId, -1, mDefaultMessagesChunk,
                    (Thread thread) -> {
                        mFetching = false;
                        mThread = thread;
                        mPollWrapper = new PollWrapper(mThread);
                        updateUI();
                        prepMessagesForDisplay(mThread.getMessages().size() - 1);
                    });
        } else {
            ILXRequestor.getILXRequestor().getThread(mBoardId, mThreadId, mInitialMessageId, -1,
                    (Thread thread) -> {
                        mFetching = false;
                        mThread = thread;
                        mPollWrapper = new PollWrapper(mThread);
                        updateUI();
                        prepMessagesForDisplay(mThread.getMessagePosition(mInitialMessageId));
                    });
        }
    }

    private void prepMessagesForDisplay(int startPosition) {
        ArrayList<Message> messagestoPrep = new ArrayList<Message>();

        int numTotalMessages = mThread.getMessages().size();
        if (startPosition < (numTotalMessages - 1)) {
            messagestoPrep.addAll(mThread.getMessages().subList(startPosition + 1, numTotalMessages));
        }
        if (startPosition > 0) {
            for (int i = startPosition - 1; i > 0; i--) {
                messagestoPrep.add(mThread.getMessages().get(i));
            }
        }

        new PrepMessagesTask(mYoutubePlaceholderImage, mLinkColor, getActivity()).execute(messagestoPrep);
    }

    private int numHeaderRows() {
        int numRows = 1;
        if (mThread.numUnloadedMessages() > 0) {
            numRows = numRows + 1;
        }
        if (mThread.isPoll()) {
            numRows = numRows + mPollWrapper.getSize();
        }
        return numRows;
    }

    private void loadEarlierMessages(int numEarlierMessagesToPrepend) {
        // TODO see if there's a way to grab messages X-X'
        int numToRequest = mThread.getLocalMessageCount() + numEarlierMessagesToPrepend;
        ILXRequestor.getILXRequestor().getThread(mBoardId, mThreadId, -1,
                numToRequest, (Thread thread) -> {

                    int numHeaderRows = numHeaderRows();

                    mThread.getMessages().addAll(0,
                            thread.getMessages().subList(0, numEarlierMessagesToPrepend));

                    mThreadAdapter.notifyItemRangeInserted(numHeaderRows, numEarlierMessagesToPrepend);
                    mThreadAdapter.updateLoader();
                    mRecyclerView.scrollToPosition(numHeaderRows + numEarlierMessagesToPrepend);

        });
    }

    private void loadLaterMessages(int numToRequest) {

        ILXRequestor.getILXRequestor().getThread(mBoardId, mThreadId, -1, numToRequest,
                (Thread thread) -> {

                    int numNewMessages = thread.getServerMessageCount() - mThread.getServerMessageCount();
                    if (numNewMessages <= numToRequest) {

                        if (mSwipeRefreshLayoutBottom != null) {
                            mSwipeRefreshLayoutBottom.setRefreshing(false);
                        }

                        if (numNewMessages > 0) {

                            int numOldMessages = mThread.getLocalMessageCount();
                            mThread.getMessages().addAll(
                                    thread.getMessages().subList(numToRequest - numNewMessages, numToRequest));
                            mThread.updateMetadata(thread.getServerMessageCount(), thread.getLastUpdated());

                            mThreadAdapter.notifyItemRangeInserted(numHeaderRows() + numOldMessages, numNewMessages);
                            mRecyclerView.scrollToPosition(numHeaderRows() + numOldMessages);

                        }

                    } else {
                        loadLaterMessages(numNewMessages + 10); // try to make sure this is our last request
                    }
                });
    }

    private void updateUI() {

        if (mProgressBar != null) {
            if (mFetching) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (mThread == null) {
                    mLoadErrorTextView.setVisibility(TextView.VISIBLE);
                }
            }
        }

        if (mRecyclerView != null && mThread != null) {

            getActivity().setTitle(mThread.getTitleForDisplay());
            if (android.os.Build.VERSION.SDK_INT > 21) {
                ((AppBarLayout) getActivity().findViewById(R.id.fragment_view_thread_appbarlayout)).setElevation(0);
            }

            if (mInitialMessageId != -1) {
                int scrollPosition = numHeaderRows() + mThread.getMessagePosition(mInitialMessageId);
                mRecyclerView.scrollToPosition(scrollPosition);
            }

            mThreadAdapter = new ThreadAdapter();
            mRecyclerView.setAdapter(mThreadAdapter);

            Log.d(TAG, "Updated UI");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //if (((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() > 0) {
            ((AppBarLayout) getActivity().findViewById(R.id.fragment_view_thread_appbarlayout)).setExpanded(false, true);
        //}

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_thread, container, false);

        ((ViewThreadActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.fragment_view_thread_toolbar));

        final LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_thread_recyclerview);
        mRecyclerView.setLayoutManager(layoutManager);

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_view_thread_progressbar);
        mLoadErrorTextView = (TextView) view.findViewById(R.id.fragment_view_thread_loaderrortext);

        mSwipeRefreshLayoutBottom = (SwipeRefreshLayoutBottom) view.findViewById(R.id.fragment_view_thread_swipeContainer);
        mSwipeRefreshLayoutBottom.setOnRefreshListener(() -> {
            loadLaterMessages(25);
        });

        updateUI();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class MessageHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        private Message mMessage;
        private TextView mBodyTextView;
        private TextView mDateTextView;
        private TextView mDisplayNameTextView;

        public MessageHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mBodyTextView = (TextView) itemView
                    .findViewById(R.id.list_item_message_body_text_view);
            mDateTextView = (TextView) itemView
                    .findViewById(R.id.list_item_message_date_text_view);
            mDisplayNameTextView = (TextView) itemView
                    .findViewById(R.id.list_item_message_display_name_text_view);

            mBodyTextView.setOnClickListener((View view) -> {
                if (mBodyTextView.getSelectionStart() == -1 &&
                        mBodyTextView.getSelectionEnd() == -1) {
                    openMessage();
                }
            });
            mBodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        public void bindMessage(Message message) {
            mMessage = message;

            mBodyTextView.setText(mMessage.getBodyForDisplayShort(mYoutubePlaceholderImage, mLinkColor, getActivity()));
            mDateTextView.setText(ILXDateOutputFormatter.formatRelativeDateShort(mMessage.getTimestamp(), false));
            mDisplayNameTextView.setText(mMessage.getDisplayNameForDisplay());
        }

        @Override
        public void onClick(View view) {
            openMessage();
        }

        private void openMessage() {
            Intent intent = ViewMessageActivity.newIntent(getActivity(), mMessage, mBoardId, mThreadId, mThread.getTitle());
            startActivity(intent);
        }
    }

    private class PollItemHolder extends RecyclerView.ViewHolder {

        private TextView mPollItemTextTextView;
        private TextView mPollItemVotesTextView;

        public PollItemHolder(View itemView) {
            super(itemView);

            if (mThread.isPollClosed()) {
                mPollItemTextTextView = (TextView) itemView
                        .findViewById(R.id.list_item_poll_option_closed_text);
                mPollItemVotesTextView = (TextView) itemView
                        .findViewById(R.id.list_item_poll_option_closed_vote_count);
            } else {
                mPollItemTextTextView = (TextView) itemView
                        .findViewById(R.id.list_item_poll_option_open_text);
            }

        }

        public void bindPollItem(int position) {
            if (mPollWrapper.isClosed()) {
                mPollItemTextTextView.setText(mPollWrapper.getItemTextForDisplay(position, mYoutubePlaceholderImage, mLinkColor, getActivity()));
                mPollItemVotesTextView.setText(mPollWrapper.getVoteCountForDisplay(position));
            } else {
                mPollItemTextTextView.setText(mPollWrapper.getItemTextForDisplay(position, mYoutubePlaceholderImage, mLinkColor, getActivity()));
            }
        }
    }

    private class LoaderHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        private TextView mLoadMoreTextView;
        private int mNumToLoad;

        public LoaderHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mLoadMoreTextView = (TextView) itemView
                    .findViewById(R.id.list_item_loadmore_loadtext);
        }

        public void bindLoader() {
            mNumToLoad = Math.min(mThread.numUnloadedMessages(), mDefaultMessagesChunk);
            mLoadMoreTextView.setText("Load " + String.valueOf(mNumToLoad) + " earlier messages");
        }

        @Override
        public void onClick(View view) {
            loadEarlierMessages(mNumToLoad);
        }
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView mHeaderTextView;

        public HeaderHolder(View itemView) {
            super(itemView);
            mHeaderTextView = (TextView) itemView
                    .findViewById(R.id.list_item_thread_header_text_view);
        }

        public void bindHeader() {
            mHeaderTextView.setText(mThread.getTitleForDisplay());
        }
    }

    private class PollHeaderHolder extends RecyclerView.ViewHolder {
        private TextView mPollHeaderTextView;

        public PollHeaderHolder(View itemView) {
            super(itemView);
            mPollHeaderTextView = (TextView) itemView
                    .findViewById(R.id.list_item_poll_header_text_view);
        }

        public void bindPollHeader() {
            if (mPollWrapper.isClosed()) {
                mPollHeaderTextView.setText("Poll results");
            } else {
                String closing = ILXDateOutputFormatter.formatRelativeDateShort(mThread.getPollClosingDate().getDate(), true);
                mPollHeaderTextView.setText("Poll closes in " + closing);
            }
        }
    }


    private class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_POLL_HEADER = 1;
        private static final int TYPE_POLL_OPEN = 2;
        private static final int TYPE_POLL_CLOSED = 3;
        private static final int TYPE_LOADMORE = 4;
        private static final int TYPE_MESSAGE = 5;
        private int mHeaderPosition;
        private int mPollHeaderPosition;
        private int mLoaderPosition;
        private int mPollStartPosition;
        private int mPollEndPosition;

        public ThreadAdapter() {
            super();

            mHeaderPosition = 0;

            if (mThread.isPoll()) {
                mPollHeaderPosition = 1;
                mPollStartPosition = 2;
                mPollEndPosition = mPollStartPosition + mPollWrapper.getSize() - 1;
            } else {
                mPollHeaderPosition = mPollStartPosition = mPollEndPosition = -1;
            }

            if (mThread.numUnloadedMessages() > 0) {
                mLoaderPosition = 1;
                if (mThread.isPoll()) {
                    mPollHeaderPosition++;
                    mPollStartPosition++;
                    mPollEndPosition++;
                }
            } else {
                mLoaderPosition = -1;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MessageHolder) {
                MessageHolder messageHolder = (MessageHolder) holder;
                int realPosition = position - numHeaderRows();
                if (realPosition < 0) {
                    Log.d(TAG, "zero-indexed message with a header row YO WTFUUUUUUUUUUUCK");
                    realPosition = 0;
                }
                messageHolder.bindMessage(
                    (Message) mThread.getMessages().get(realPosition));
            } else if (holder instanceof LoaderHolder) {
                LoaderHolder loaderHolder = (LoaderHolder) holder;
                loaderHolder.bindLoader();
            } else if (holder instanceof PollItemHolder) {
                int realPosition = position - 2;
                if (mLoaderPosition != -1) {
                    realPosition--;
                }
                ((PollItemHolder) holder).bindPollItem(realPosition);
            } else if (holder instanceof HeaderHolder) {
                HeaderHolder headerHolder = (HeaderHolder) holder;
                headerHolder.bindHeader();
            } else if (holder instanceof PollHeaderHolder) {
                PollHeaderHolder pollHeaderHolder = (PollHeaderHolder) holder;
                pollHeaderHolder.bindPollHeader();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = null;

            switch (viewType) {

                case TYPE_POLL_OPEN:
                case TYPE_POLL_CLOSED:
                    if (mThread.isPollClosed()) {
                        view = layoutInflater.inflate(R.layout.list_item_poll_option_closed, parent, false);
                    } else {
                        view = layoutInflater.inflate(R.layout.list_item_poll_option_open, parent, false);
                    }
                    return new PollItemHolder(view);

                case TYPE_LOADMORE:
                    view = layoutInflater.inflate(R.layout.list_item_loadmore, parent, false);
                    return new LoaderHolder(view);

                case TYPE_HEADER:
                    view = layoutInflater.inflate(R.layout.list_item_thread_header, parent, false);
                    return new HeaderHolder(view);

                case TYPE_POLL_HEADER:
                    view = layoutInflater.inflate(R.layout.list_item_poll_header, parent, false);
                    return new PollHeaderHolder(view);
            }

            view = layoutInflater.inflate(R.layout.list_item_message, parent, false);
            return new MessageHolder(view);
        }

        @Override
        public int getItemCount() {
            List<Message> messages = mThread.getMessages();
            if (messages != null) {
                return messages.size() + numHeaderRows();
            } else {
                Log.d("ThreadAdapter", "no messages");
                return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {

            if (position == mLoaderPosition) {
                return TYPE_LOADMORE;
            } else if (position >= mPollStartPosition && position <= mPollEndPosition) {
                if (mThread.isPollClosed()) {
                    return TYPE_POLL_CLOSED;
                } else {
                    return TYPE_POLL_OPEN;
                }
            } else if (position == mHeaderPosition) {
                return TYPE_HEADER;
            } else if (position == mPollHeaderPosition) {
                return TYPE_POLL_HEADER;
            }
            return TYPE_MESSAGE;
        }

        public void updateLoader() {

            // assume that we'll never have fewer messages than we had before ...

            if (mLoaderPosition != -1) {
                if (mThread.numUnloadedMessages() > 0) {
                    notifyItemChanged(mLoaderPosition);
                } else {
                    int oldPosition = mLoaderPosition;
                    mLoaderPosition = -1;
                    if (mPollStartPosition != -1) {
                        mPollStartPosition--;
                        mPollEndPosition--;
                    }
                    notifyItemRemoved(oldPosition);
                }
            }
        }
    }
}
