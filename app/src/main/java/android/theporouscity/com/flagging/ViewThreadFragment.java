package android.theporouscity.com.flagging;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.theporouscity.com.flagging.ilx.Message;
import android.theporouscity.com.flagging.ilx.Thread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by bergstroml on 6/15/16.
 */

public class ViewThreadFragment extends Fragment {

    private static final String TAG = "ViewThreadFragment";
    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final int mDefaultMessagesChunk = 100;
    private Thread mThread;
    private RecyclerView mRecyclerView;
    private ThreadAdapter mThreadAdapter;
    private ProgressBar mProgressBar;
    private TextView mLoadErrorTextView;
    private boolean mFetching;
    private int mBoardId;
    private int mThreadId;

    public ViewThreadFragment() { }

    public static ViewThreadFragment newInstance(int boardId, int threadId) {
        ViewThreadFragment fragment = new ViewThreadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOARD_ID, boardId);
        args.putInt(ARG_THREAD_ID, threadId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mBoardId = getArguments().getInt(ARG_BOARD_ID);
            mThreadId = getArguments().getInt(ARG_THREAD_ID);
            loadThread();
        }
    }

    private void loadThread() {
        mFetching = true;
        ILXRequestor.getILXRequestor().getThread(mBoardId, mThreadId, mDefaultMessagesChunk,
                (Thread thread) -> {
                    mFetching = false;
                    mThread = thread;
                    updateUI();
                });
    }

    private void loadMoreThread(int numEarlierMessagesToPrepend) {
        // TODO this is wasteful, see if there's a way to grab messages X-X'
        int numToRequest = mThread.getLocalMessageCount() + numEarlierMessagesToPrepend;
        ILXRequestor.getILXRequestor().getThread(mBoardId, mThreadId,
                numToRequest, (Thread thread) -> {

                    int numHeaderRows = 0;

                    mThread.getMessages().addAll(0,
                            thread.getMessages().subList(0, numEarlierMessagesToPrepend));

                    if (mThread.getLocalMessageCount() < mThread.getServerMessageCount()) {
                        numHeaderRows = numHeaderRows + 1;
                    }
                    if (mThread.isPoll()) {
                        numHeaderRows = numHeaderRows + 1;
                    }

                    mThreadAdapter.notifyItemRangeInserted(numHeaderRows, numEarlierMessagesToPrepend);
                    mThreadAdapter.updateLoader();
                    mRecyclerView.scrollToPosition(numEarlierMessagesToPrepend + 1);

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
            mRecyclerView.scrollToPosition(mThread.getMessages().size() - 1); // TODO bookmarks
            mThreadAdapter = new ThreadAdapter();
            mRecyclerView.setAdapter(mThreadAdapter);
            getActivity().setTitle(mThread.getTitle());

            Log.d(TAG, "Updated UI");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_thread, container, false);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_thread_recyclerview);
        mRecyclerView.setLayoutManager(layoutManager);

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_view_thread_progressbar);
        mLoadErrorTextView = (TextView) view.findViewById(R.id.fragment_view_thread_loaderrortext);

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
        }

        public void bindMessage(Message message) {
            mMessage = message;
            mBodyTextView.setText(mMessage.getBodyForDisplayShort(getActivity()));
            mDateTextView.setText(ILXDateOutputFormat.formatRelativeDateShort(mMessage.getTimestamp()));
            mDisplayNameTextView.setText(mMessage.getDisplayNameForDisplay());
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(), "open sesame", Toast.LENGTH_SHORT).show();
        }
    }

    private class PollHolder extends RecyclerView.ViewHolder {

        public PollHolder(View itemView) {
            super(itemView);
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
            mNumToLoad = Math.min(mThread.getServerMessageCount() - mThread.getLocalMessageCount(), 50);
            mLoadMoreTextView.setText("Load " + String.valueOf(mNumToLoad) + " earlier messages");
        }

        @Override
        public void onClick(View view) {
            loadMoreThread(mNumToLoad);
        }
    }

    private class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_POLL_OPEN = 0;
        private static final int TYPE_POLL_CLOSED = 1;
        private static final int TYPE_LOADMORE = 2;
        private static final int TYPE_MESSAGE = 3;
        private int mLoaderPosition;
        private int mPollPosition;

        public ThreadAdapter() {
            super();

            if (mThread.getServerMessageCount() > mThread.getLocalMessageCount()) {
                mLoaderPosition = 0;
                if (mThread.isPoll()) {
                    mPollPosition = 1;
                } else {
                    mPollPosition = -1;
                }
            } else {
                mLoaderPosition = -1;
                if (mThread.isPoll()) {
                    mPollPosition = 0;
                } else {
                    mPollPosition = -1;
                }
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MessageHolder) {
                MessageHolder messageHolder = (MessageHolder) holder;
                int realPosition = position - getNumHeaderRows();
                if (realPosition < 0) {
                    Log.d(TAG, "zero-indexed message with a header row YO WTFUUUUUUUUUUUCK");
                    realPosition = 0;
                }
                messageHolder.bindMessage(
                    (Message) mThread.getMessages().get(realPosition));
            } else if (holder instanceof LoaderHolder) {
                LoaderHolder loaderHolder = (LoaderHolder) holder;
                loaderHolder.bindLoader();
            } else if (holder instanceof PollHolder) {
                // don't need to do anything
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
                        view = layoutInflater.inflate(R.layout.list_item_poll_closed, parent, false);
                    } else {
                        view = layoutInflater.inflate(R.layout.list_item_poll_open, parent, false);
                    }
                    return new PollHolder(view);

                case TYPE_LOADMORE:
                    view = layoutInflater.inflate(R.layout.list_item_loadmore, parent, false);
                    return new LoaderHolder(view);
            }

            view = layoutInflater.inflate(R.layout.list_item_message, parent, false);
            return new MessageHolder(view);
        }

        @Override
        public int getItemCount() {
            List<Message> messages = mThread.getMessages();
            if (messages != null) {
                return messages.size() + getNumHeaderRows();
            } else {
                Log.d("ThreadAdapter", "no messages");
                return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {

            if (position == mLoaderPosition) {
                return TYPE_LOADMORE;
            } else if (position == mPollPosition) {
                if (mThread.isPollClosed()) {
                    return TYPE_POLL_CLOSED;
                } else {
                    return TYPE_POLL_OPEN;
                }
            }
            return TYPE_MESSAGE;
        }

        public void updateLoader() {
            // assume that we'll never have fewer messages than we had before ...

            if (mLoaderPosition != -1) {
                if (mThread.getLocalMessageCount() < mThread.getServerMessageCount()) {
                    notifyItemChanged(mLoaderPosition);
                } else {
                    int oldPosition = mLoaderPosition;
                    mLoaderPosition = -1;
                    notifyItemRemoved(oldPosition);
                }
            }
        }

        public int getNumHeaderRows() {
            int count = 0;
            if (mLoaderPosition != -1) {
                count = count + 1;
            }
            if (mPollPosition != -1) {
                count = count + 1;
            }
            return count;
        }
    }
}
