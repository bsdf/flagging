package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.theporouscity.com.flagging.ilx.Board;
import android.theporouscity.com.flagging.ilx.Boards;
import android.theporouscity.com.flagging.ilx.Bookmark;
import android.theporouscity.com.flagging.ilx.ServerBookmarks;
import android.theporouscity.com.flagging.ilx.RecentlyUpdatedThread;
import android.theporouscity.com.flagging.ilx.RecentlyUpdatedThreads;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewThreadsFragment extends Fragment {

    private static final String TAG = "ViewThreadsFragment";
    private static final String ARG_BOARD = "board";
    private static final String ARG_MODE = "mode";
    private static final int MODE_BOARD = 0;
    private static final int MODE_SNA = 1;
    private static final int MODE_MARKS = 2;

    @BindView(R.id.fragment_view_threads_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_view_board_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.fragment_view_thread_loaderrortext)
    TextView mLoadErrorTextView;

    @BindView(R.id.fragment_view_board_swipeContainer)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.fragment_view_threads_fab)
    FloatingActionButton mFloatingActionButton;

    @Inject
    ILXRequestor mILXRequestor;

    @Inject
    UserAppSettings mUserAppSettings;

    private ThreadAdapter mThreadAdapter;
    private int mMode;
    private Board mBoard;
    private Boards mBoards;
    private RecentlyUpdatedThreads mThreads;
    private boolean mFetchingThreads;
    private boolean mFetchingBoards;
    private boolean mHasBookmarks;
    private ServerBookmarks mBookmarks;

    public static ViewThreadsFragment newInstance(Board board) {
        ViewThreadsFragment fragment = new ViewThreadsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_BOARD);
        args.putParcelable(ARG_BOARD, board);
        fragment.setArguments(args);
        return fragment;
    }

    public static ViewThreadsFragment newInstance(boolean snaMode) {
        ViewThreadsFragment fragment = new ViewThreadsFragment();
        Bundle args = new Bundle();
        if (snaMode) {
            args.putInt(ARG_MODE, MODE_SNA);
        } else {
            args.putInt(ARG_MODE, MODE_MARKS);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        setRetainInstance(true);
        mBoard = null;
        mFetchingBoards = false;
        if (getArguments() != null) {
            if (getArguments().getInt(ARG_MODE) == MODE_BOARD) {
                mMode = MODE_BOARD;
                mBoard = getArguments().getParcelable(ARG_BOARD);
            } else {
                if (getArguments().getInt(ARG_MODE) == MODE_SNA) {
                    mMode = MODE_SNA;
                } else if (getArguments().getInt(ARG_MODE) == MODE_MARKS) {
                    mMode = MODE_MARKS;
                    getBookmarks();
                }
                getBoards();
            }
            updateThreads();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateThreads();
    }

    private void updateThreads() {
        mFetchingThreads = true;
        if (mMode == MODE_BOARD) {
            mILXRequestor.getRecentlyUpdatedThreads(mBoard.getId(),
                    (RecentlyUpdatedThreads threads) -> {
                        updateThreadsReady(threads);
                    });
        } else if (mMode == MODE_SNA) {
            mILXRequestor.getSiteNewAnswers(
                    (RecentlyUpdatedThreads threads) -> {
                        updateThreadsReady(threads);
                    });
        } else if (mMode == MODE_MARKS) {
            mILXRequestor.getBookmarks(getContext(), (ServerBookmarks bookmarks) -> {
                RecentlyUpdatedThreads threads = new RecentlyUpdatedThreads();
                ArrayList<RecentlyUpdatedThread> bookmarksThreads = new ArrayList<>();
                for (HashMap.Entry<Integer, HashMap<Integer, Bookmark>> boardBookmarks: bookmarks.getBookmarks().entrySet()) {
                    for (HashMap.Entry<Integer, Bookmark> bookmarkEntry : boardBookmarks.getValue().entrySet()) {
                        Bookmark bookmark = bookmarkEntry.getValue();
                        bookmarksThreads.add(new RecentlyUpdatedThread(bookmark.getCachedThread()));
                    }
                }
                threads.setRecentlyUpdatedThreads(bookmarksThreads);
                updateThreadsReady(threads);
            });
        }
    }

    private void getBoards() {
        mFetchingBoards = true;
        if (mMode == MODE_SNA || mMode == MODE_MARKS) {
            mILXRequestor.getBoards(
                    (Boards boards) -> {
                        getBoardsReady(boards);
            }, getContext());
        }
    }

    private void getBoardsReady(Boards boards) {
        mFetchingBoards = false;
        mBoards = boards;
        updateUI();
    }

    private void getBookmarks() {
        mFetchingThreads = true;
        mILXRequestor.getBookmarks(
                getContext(),
                (ServerBookmarks bookmarks) ->
                {
                    mBookmarks = bookmarks;
                    mFetchingThreads = false;
                    updateUI();
                });
    }

    private void updateThreadsReady(RecentlyUpdatedThreads threads) {
        mFetchingThreads = false;
        mThreads = threads;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (mThreads != null) {
            Log.d("got threads", threads.getURI() + " " + threads.getTotalMessages());
        }
        updateUI();
    }

    private void updateUI() {

        updateProgressBar();

        if (!mFetchingThreads && !mFetchingBoards && mLoadErrorTextView != null) {
            if (mThreads == null) {
                mLoadErrorTextView.setVisibility(TextView.VISIBLE);
            } else if (mThreads.getRecentlyUpdatedThreads().size() == 0) {
                mLoadErrorTextView.setText("No recent threads");
                mLoadErrorTextView.setVisibility(TextView.VISIBLE);
            }
        }

        if (mRecyclerView != null && mThreads != null && (mBoards != null || mMode == MODE_BOARD)) {
            mThreadAdapter = new ThreadAdapter();
            mRecyclerView.setAdapter(mThreadAdapter);
        }
    }

    private void updateProgressBar() {
        if (mProgressBar != null) {
            if (mFetchingThreads || mFetchingBoards) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_view_board_menu, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (mMode == MODE_BOARD) {
            getActivity().setTitle(mBoard.getName());
        }

        View view = inflater.inflate(R.layout.fragment_view_threads, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration((getActivity())));

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            updateThreads();
        });

        if (mUserAppSettings.getPretendToBeLoggedInSetting()
                && mMode == MODE_BOARD) {
            mFloatingActionButton.setVisibility(FloatingActionButton.VISIBLE);
            mFloatingActionButton.setOnClickListener((View v) -> {
                Log.d(TAG, "asking a new question");
                Toast.makeText(getContext(), "yup", Toast.LENGTH_LONG).show();
            });
        }

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

    private class ThreadHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private RecentlyUpdatedThread mThread;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private TextView mBoardTitleTextView;

        public ThreadHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            if (mMode == MODE_BOARD) {
                mTitleTextView = (TextView) itemView
                        .findViewById(R.id.list_item_thread_title_text_view);
                mDateTextView = (TextView) itemView
                        .findViewById(R.id.list_item_thread_date_text_view);
            } else if (mMode == MODE_SNA || mMode == MODE_MARKS) {
                mTitleTextView = (TextView) itemView
                        .findViewById(R.id.list_item_snathread_title_text_view);
                mDateTextView = (TextView) itemView
                        .findViewById(R.id.list_item_snathread_date_text_view);
                mBoardTitleTextView = (TextView) itemView
                        .findViewById(R.id.list_item_snathread_board_name_text_view);
            }
        }

        public void bindThread(RecentlyUpdatedThread thread) {
            mThread = thread;
            SpannableStringBuilder ssb = null;
            if (mThread.getPoll()) {
                ssb = new SpannableStringBuilder(mThread.getTitleForDisplay());
                ssb.append(" - poll");
                ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                        mThread.getTitleForDisplay().length()+3,
                        mThread.getTitleForDisplay().length()+7,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                ssb = new SpannableStringBuilder(mThread.getTitleForDisplay());
            }
            mTitleTextView.setText(ssb);
            Date lastUpdated = mThread.getLastUpdated();
            mDateTextView.setText(ILXDateOutputFormatter.formatRelativeDateShort(lastUpdated, false));
            if (mMode == MODE_SNA || mMode == MODE_MARKS) {
                mBoardTitleTextView.setText(mBoards.getBoardById(mThread.getBoardId()).getName());
            }
        }

        @Override
        public void onClick(View view) {
            int bookmarkedMessageId = -1;

            if (mMode == MODE_MARKS) {
                bookmarkedMessageId = mILXRequestor
                        .getCachedBookmarks().getBookmark(mThread.getBoardId(), mThread.getThreadId())
                        .getBookmarkedMessageId();
            }

            Intent intent = ViewThreadActivity
                    .newIntent(getActivity(), mThread.getBoardId(), mThread.getThreadId(), bookmarkedMessageId);
            startActivity(intent);
        }
    }

    private class ThreadAdapter extends RecyclerView.Adapter<ThreadHolder> {

        @Override
        public void onBindViewHolder(ThreadHolder holder, int position) {
            holder.bindThread(
                    (RecentlyUpdatedThread) mThreads.getRecentlyUpdatedThreads().get(position));
        }

        @Override
        public ThreadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = null;
            if (mMode == MODE_BOARD) {
                view = layoutInflater.inflate(R.layout.list_item_thread, parent, false);
            } else if (mMode == MODE_SNA || mMode == MODE_MARKS) {
                view = layoutInflater.inflate(R.layout.list_item_sna_thread, parent, false);
            }
            return new ThreadHolder(view);
        }

        @Override
        public int getItemCount() {
            List<RecentlyUpdatedThread> threads = mThreads.getRecentlyUpdatedThreads();
            if (threads != null) {
                return threads.size();
            } else {
                Log.d("ThreadAdapter", "no threads");
                return 0;
            }

        }
    }

}
