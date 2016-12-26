package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.theporouscity.flagging.ilx.Message;
import com.theporouscity.flagging.ilx.PollWrapper;
import com.theporouscity.flagging.ilx.RichMessageHolder;
import com.theporouscity.flagging.ilx.RichThreadHolder;
import com.theporouscity.flagging.ilx.Thread;
import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXDateOutputFormatter;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.ILXTextOutputFormatter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static butterknife.ButterKnife.findById;

/**
 * Created by bergstroml on 6/15/16.
 */

public class ViewThreadFragment extends Fragment {

    private static final String TAG = "ViewThreadFragment";
    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_MESSAGE_ID = "message_id";
    private static final String ARG_MESSAGES_LOADED_COUNT = "messages_loaded_count";
    private static final int mDefaultMessagesChunk = 100;

    @BindView(R.id.fragment_view_thread_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_view_thread_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.fragment_view_thread_loaderrortext)
    TextView mLoadErrorTextView;

    @BindView(R.id.fragment_view_thread_swipeContainer)
    SwipeRefreshLayoutBottom mSwipeRefreshLayoutBottom;

    @BindView(R.id.fragment_view_thread_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fragment_view_thread_appbarlayout)
    AppBarLayout mAppBarLayout;

    @Inject
    ILXRequestor mILXRequestor;

    @Inject
    ILXTextOutputFormatter mILXTextOutputFormatter;

    private RichThreadHolder mThreadHolder;
    private PollWrapper mPollWrapper;
    private ThreadAdapter mThreadAdapter;

    private boolean mFetching;
    private int mBoardId;
    private int mThreadId;
    private int mInitialMessageId;
    private int mMessagesLoadedCount;

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
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        if (getArguments() != null) {
            mBoardId = getArguments().getInt(ARG_BOARD_ID);
            mThreadId = getArguments().getInt(ARG_THREAD_ID);
            mInitialMessageId = getArguments().getInt(ARG_MESSAGE_ID);
            mMessagesLoadedCount = getArguments().getInt(ARG_MESSAGES_LOADED_COUNT);
            Log.d(TAG, "messages loaded - onCreate - " + Integer.toString(mMessagesLoadedCount));
            //loadThread();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_BOARD_ID, mBoardId);
        outState.putSerializable(ARG_THREAD_ID, mThreadId);
        outState.putSerializable(ARG_MESSAGE_ID, mInitialMessageId);
        Log.d(TAG, "messages loaded - onSaveInstanceState - " + Integer.toString(mMessagesLoadedCount));
        outState.putSerializable(ARG_MESSAGES_LOADED_COUNT, mMessagesLoadedCount);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mBoardId = savedInstanceState.getInt(ARG_BOARD_ID);
            mThreadId = savedInstanceState.getInt(ARG_THREAD_ID);
            mInitialMessageId = savedInstanceState.getInt(ARG_MESSAGE_ID);
            Log.d(TAG, "messages loaded - onActivityCreated, before update - " + Integer.toString(mMessagesLoadedCount));
            mMessagesLoadedCount = savedInstanceState.getInt(ARG_MESSAGES_LOADED_COUNT);
            Log.d(TAG, "messages loaded - onActivityCreated, after update - " + Integer.toString(mMessagesLoadedCount));
        }

        // only here do we seem to have access to the Fragment's saved instance state
        // it's available in onCreate too but for some reason onCreate passes in the Activity's saved instance state
        // (which contains the Fragment's instance state, but would have to extract, so wtf)

        loadThread();
        updateUI();
    }

    private void showError(Exception e)
    {
        Log.d(TAG, e.toString());
        if (mLoadErrorTextView != null) {
            mLoadErrorTextView.setText(e.getMessage());
            mLoadErrorTextView.setVisibility(TextView.VISIBLE);
        } else {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String e)
    {
        Log.d(TAG, e);
        if (mLoadErrorTextView != null) {
            mLoadErrorTextView.setText(e);
            mLoadErrorTextView.setVisibility(TextView.VISIBLE);
        } else {
            Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadThread() {
        mFetching = true;

        int numMessagesToLoad;

        Log.d(TAG, "messages loaded - loadThread, before load - " + Integer.toString(mMessagesLoadedCount));


        if (mInitialMessageId == -1) {

            if (mMessagesLoadedCount == 0) {
                numMessagesToLoad = mDefaultMessagesChunk;
            } else {
                numMessagesToLoad = mMessagesLoadedCount;
            }

            mILXRequestor.getThread(getContext(), mBoardId, mThreadId, -1, numMessagesToLoad,
                    (AsyncTaskResult<Thread> result) -> {
                        mFetching = false;
                        if (result.getError() == null) {
                            mThreadHolder = new RichThreadHolder(result.getResult(),
                                    mILXRequestor.getCurrentAccount(),
                                    mILXRequestor.getCurrentClient(getContext()),
                                    getContext(), mILXTextOutputFormatter);
                            mPollWrapper = new PollWrapper(mThreadHolder, mILXTextOutputFormatter);
                            updateUI();
                            mThreadHolder.prepAllMessagesForDisplay(mThreadHolder.getRichMessageHolders().size() - 1, getActivity());
                            mPollWrapper.prepPollItems(getActivity());
                            mMessagesLoadedCount = mThreadHolder.getThread().getLocalMessageCount();
                            Log.d(TAG, "messages loaded - loadThread, no initial, after load - " + Integer.toString(mMessagesLoadedCount));
                        } else {
                            showError(result.getError());
                        }
                    });
        } else {

            if (mMessagesLoadedCount == 0) {
                numMessagesToLoad = -1;
            } else {
                numMessagesToLoad = mMessagesLoadedCount;
            }

            mILXRequestor.getThread(getContext(), mBoardId, mThreadId, mInitialMessageId, numMessagesToLoad,
                    (AsyncTaskResult<Thread> result) -> {
                        if (result.getError() == null) {
                            mFetching = false;
                            mThreadHolder = new RichThreadHolder(result.getResult(),
                                    mILXRequestor.getCurrentAccount(),
                                    mILXRequestor.getCurrentClient(getContext()),
                                    getContext(), mILXTextOutputFormatter);
                            mThreadHolder.getDrawingResources(getContext());
                            mPollWrapper = new PollWrapper(mThreadHolder, mILXTextOutputFormatter);
                            updateUI();
                            mThreadHolder.prepAllMessagesForDisplay(mThreadHolder.getThread().getMessagePosition(mInitialMessageId), getActivity());
                            mPollWrapper.prepPollItems(getActivity());
                            mMessagesLoadedCount = mThreadHolder.getThread().getLocalMessageCount();
                            Log.d(TAG, "messages loaded - loadThread, with initial, after load - " + Integer.toString(mMessagesLoadedCount));
                        } else {
                            showError(result.getError());
                        }
                    });
        }
    }

    private int numHeaderRows() {
        int numRows = 1;
        if (mThreadHolder.getThread().numUnloadedMessages() > 0) {
            numRows = numRows + 1;
        }
        if (mThreadHolder.getThread().isPoll()) {
            numRows = numRows + mPollWrapper.getSize();
        }
        return numRows;
    }

    private void loadEarlierMessages(int numEarlierMessagesToPrepend) {
        // TODO see if there's a way to grab messages X-X'
        int numToRequest = mThreadHolder.getThread().getLocalMessageCount() + numEarlierMessagesToPrepend;

        mILXRequestor.getThread(getContext(), mBoardId, mThreadId, -1,
                numToRequest, (AsyncTaskResult<Thread> result) -> {

                    if (result.getError() == null) {

                        Thread thread = result.getResult();
                        mMessagesLoadedCount = numToRequest;

                        mThreadHolder.ingestEarlierMessages(thread, getActivity(), numEarlierMessagesToPrepend);

                        int numHeaderRows = numHeaderRows();
                        mThreadAdapter.notifyItemRangeInserted(numHeaderRows, numEarlierMessagesToPrepend);
                        mThreadAdapter.updateLoader();
                        mRecyclerView.scrollToPosition(numHeaderRows + numEarlierMessagesToPrepend);
                    } else {
                        showError(result.getError());
                    }

                });
    }

    private void loadLaterMessages(int numAdditionalToRequest) {

        mILXRequestor.getThread(getContext(), mBoardId, mThreadId, -1, numAdditionalToRequest,
                (AsyncTaskResult<Thread> result) -> {

                    if (result.getError() == null) {
                        Thread thread = result.getResult();
                        int numNewMessages = thread.getServerMessageCount() - mThreadHolder.getThread().getServerMessageCount();
                        if (numNewMessages <= numAdditionalToRequest) {

                            if (mSwipeRefreshLayoutBottom != null) {
                                mSwipeRefreshLayoutBottom.setRefreshing(false);
                            }

                            if (numNewMessages > 0) {

                                mMessagesLoadedCount += numNewMessages;
                                int numOldMessages = mThreadHolder.getThread().getLocalMessageCount();

                                mThreadHolder.ingestLaterMessages(thread, getActivity(), numAdditionalToRequest - numNewMessages, numAdditionalToRequest);

                                mThreadAdapter.notifyItemRangeInserted(numHeaderRows() + numOldMessages, numNewMessages);
                                mRecyclerView.scrollToPosition(numHeaderRows() + numOldMessages);

                            }

                        } else {
                            loadLaterMessages(numNewMessages + 10); // try to make sure this is our last request
                        }
                    } else {
                        showError(result.getError());
                    }

                });

    }

    private void registerFabListeners(View view) {
        FabSpeedDial fabSpeedDial = (FabSpeedDial) view.findViewById(R.id.fragment_view_thread_fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.thread_actions_menu_bookmark:
                        fabBookmarkHandler();
                        break;
                    case R.id.thread_actions_menu_reply:
                        fabReplyHandler();
                        break;
                }
                return false;
            }
        });
    }

    private void fabBookmarkHandler() {
        List<Message> messages = mThreadHolder.getThread().getMessages();
        Message lastMessage = messages.get(messages.size() - 1);

        mILXRequestor.getCachedBookmarks()
                .addBookmark(mBoardId, mThreadId, lastMessage.getMessageId());
        mILXRequestor.serializeBoardBookmarks(getContext());
        Toast.makeText(getContext(), "Bookmark set", Toast.LENGTH_SHORT).show();
    }

    private void fabReplyHandler() {
        Intent intent = ThreadReplyActivity.newIntent(getActivity(), mThreadHolder.getThread());
        startActivity(intent);

        Log.d(TAG, "inside fabReplyHandler");
    }

    private void updateUI() {

        if (mProgressBar != null) {
            if (mFetching) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mLoadErrorTextView.setVisibility(TextView.INVISIBLE);
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (mThreadHolder == null || mThreadHolder.getThread() == null) {
                    showError("Problem loading thread");
                } else {
                    mLoadErrorTextView.setVisibility(TextView.INVISIBLE);
                }
            }
        }

        if (mRecyclerView != null && mThreadHolder != null && mThreadHolder.getThread() != null) {

            getActivity().setTitle(mThreadHolder.getThread().getTitleForDisplay());
            if (android.os.Build.VERSION.SDK_INT > 21) {
                mAppBarLayout.setElevation(0);
            }

            if (mInitialMessageId != -1) {
                int scrollPosition = numHeaderRows() + mThreadHolder.getThread().getMessagePosition(mInitialMessageId);
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
        mAppBarLayout.setExpanded(false, true);
        //}

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_thread, container, false);
        ButterKnife.bind(this, view);

        ((ViewThreadActivity) getActivity()).setSupportActionBar(mToolbar);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(layoutManager);

        mSwipeRefreshLayoutBottom.setOnRefreshListener(() -> {
            loadLaterMessages(25);
        });

        registerFabListeners(view);
        updateUI();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "pausing thread fragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "stopping thread fragment");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroying thread fragment");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class MessageHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        @BindView(R.id.list_item_message_body_text_view)
        TextView mBodyTextView;

        @BindView(R.id.list_item_message_date_text_view)
        TextView mDateTextView;

        @BindView(R.id.list_item_message_display_name_text_view)
        TextView mDisplayNameTextView;

        private RichMessageHolder mRichMessageHolder;

        public MessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            mBodyTextView.setOnClickListener((View view) -> {
                if (mBodyTextView.getSelectionStart() == -1 &&
                        mBodyTextView.getSelectionEnd() == -1) {
                    openMessage();
                }
            });
            mBodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        public void bindMessage(RichMessageHolder richMessageHolder) {
            mRichMessageHolder = richMessageHolder;
            ILXTextOutputFormatter.MessageReadyCallback callback = () -> {
                mThreadAdapter.notifyItemChanged(getAdapterPosition());
            };

            mBodyTextView.setText(mRichMessageHolder.getBodyForDisplayShort(getActivity(), callback));

            mDateTextView.setText(ILXDateOutputFormatter.formatRelativeDateShort(
                    mRichMessageHolder.getMessage().getTimestamp(), false));
            mDisplayNameTextView.setText(mRichMessageHolder.getDisplayNameForDisplay());
        }

        @Override
        public void onClick(View view) {
            openMessage();
        }

        private void openMessage() {
            Intent intent = ViewMessageActivity.newIntent(getActivity(),
                    mRichMessageHolder.getMessage(), mBoardId, mThreadId,
                    mThreadHolder.getThread().getTitle(), mThreadHolder.getSid());
            startActivity(intent);
        }
    }

    private class PollItemHolder extends RecyclerView.ViewHolder {

        private TextView mPollItemTextTextView;
        private TextView mPollItemVotesTextView;

        public PollItemHolder(View itemView) {
            super(itemView);

            if (mThreadHolder.getThread().isPollClosed()) {
                mPollItemTextTextView  = findById(itemView, R.id.list_item_poll_option_closed_text);
                mPollItemVotesTextView = findById(itemView, R.id.list_item_poll_option_closed_vote_count);
            } else {
                mPollItemTextTextView  = findById(itemView, R.id.list_item_poll_option_open_text);
            }
        }

        public void bindPollItem(int position) {
            mPollItemTextTextView.setText(mPollWrapper.getItemTextForDisplay(
                    position, getActivity(),
                    () -> { mThreadAdapter.notifyItemChanged(getAdapterPosition()); }));
            if (mPollWrapper.isClosed()) {
                mPollItemVotesTextView.setText(mPollWrapper.getVoteCountForDisplay(position));
            }
        }
    }

    class LoaderHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        @BindView(R.id.list_item_loadmore_loadtext)
        TextView mLoadMoreTextView;

        private int mNumToLoad;
        private boolean mLoading;

        public LoaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void bindLoader() {
            mLoading = false;
            mNumToLoad = Math.min(mThreadHolder.getThread().numUnloadedMessages(), mDefaultMessagesChunk);
            mLoadMoreTextView.setText("Load " + String.valueOf(mNumToLoad) + " earlier messages");
        }

        @Override
        public void onClick(View view) {
            if (!mLoading) {
                mLoading = true;
                loadEarlierMessages(mNumToLoad);
            }
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_thread_header_text_view)
        TextView mHeaderTextView;

        public HeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindHeader() {
            mHeaderTextView.setText(mThreadHolder.getThread().getTitleForDisplay());
        }
    }

    class PollHeaderHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_poll_header_text_view)
        TextView mPollHeaderTextView;

        public PollHeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindPollHeader() {
            if (mPollWrapper.isClosed()) {
                mPollHeaderTextView.setText("Poll results");
            } else {
                String closing = ILXDateOutputFormatter.formatRelativeDateShort(
                        mThreadHolder.getThread().getPollClosingDate().getDate(), true);
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

            if (mThreadHolder.getThread().isPoll()) {
                mPollHeaderPosition = 1;
                mPollStartPosition = 2;
                mPollEndPosition = mPollStartPosition + mPollWrapper.getSize() - 1;
            } else {
                mPollHeaderPosition = mPollStartPosition = mPollEndPosition = -1;
            }

            if (mThreadHolder.getThread().numUnloadedMessages() > 0) {
                mLoaderPosition = 1;
                if (mThreadHolder.getThread().isPoll()) {
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
                    realPosition = 0;
                }
                messageHolder.bindMessage(mThreadHolder.getRichMessageHolders().get(realPosition));
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
            View view;

            switch (viewType) {

                case TYPE_POLL_OPEN:
                case TYPE_POLL_CLOSED:
                    if (mThreadHolder.getThread().isPollClosed()) {
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
            List<Message> messages = mThreadHolder.getThread().getMessages();
            if (messages != null) {
                return messages.size() + numHeaderRows();
            } else {
                return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {

            if (position == mLoaderPosition) {
                return TYPE_LOADMORE;
            } else if (position >= mPollStartPosition && position <= mPollEndPosition) {
                if (mThreadHolder.getThread().isPollClosed()) {
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
                if (mThreadHolder.getThread().numUnloadedMessages() > 0) {
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
