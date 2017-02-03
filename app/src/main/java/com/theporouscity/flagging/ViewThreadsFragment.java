package com.theporouscity.flagging;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.theporouscity.flagging.ilx.Board;
import com.theporouscity.flagging.ilx.Boards;
import com.theporouscity.flagging.ilx.Bookmark;
import com.theporouscity.flagging.ilx.Bookmarks;
import com.theporouscity.flagging.ilx.RecentlyUpdatedThread;
import com.theporouscity.flagging.ilx.RecentlyUpdatedThreads;
import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXDateOutputFormatter;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.UserAppSettings;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

public class ViewThreadsFragment extends Fragment {

    private static final String TAG = "ViewThreadsFragment";
    private static final String ARG_BOARD = "board";
    private static final String ARG_MODE = "mode";
    private static final String BOARDS_FETCH_VAL = "fetchingBoards";
    private static final String THREADS_FETCH_VAL = "fetchingThreads";
    public static final int MODE_BOARD = 0;
    public static final int MODE_SNA = 1;
    public static final int MODE_MARKS = 2;

    @BindView(R.id.fragment_view_threads_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_view_board_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.fragment_view_threads_loaderrortext)
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
    private RecentlyUpdatedThreads mThreads = null;
    private boolean mFetchingThreads;
    private boolean mFetchingBoards;
    private Bookmarks mBookmarks = null;

    public int getMode() {
        return mMode;
    }

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

        mBoard = null;
        mFetchingBoards = false;
        if (getArguments() != null) {
            if (getArguments().getInt(ARG_MODE) == MODE_BOARD) {
                mMode = MODE_BOARD;
                mBoard = getArguments().getParcelable(ARG_BOARD);
            } else {
                if (getArguments().getInt(ARG_MODE) == MODE_SNA) {
                    mMode = MODE_SNA;
                    getBoards();
                } else if (getArguments().getInt(ARG_MODE) == MODE_MARKS) {
                    mMode = MODE_MARKS;
                }
            }
            updateThreads();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateThreads();
    }

    private void showError(Exception e)
    {
        Log.d(TAG, e.toString());
        if (mLoadErrorTextView != null) {
            mLoadErrorTextView.setText(e.getMessage());
            mLoadErrorTextView.setVisibility(TextView.VISIBLE);
            if (mRecyclerView != null) {
                mRecyclerView.setVisibility(RecyclerView.INVISIBLE);
            }
        } else {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideError() {
        if (mLoadErrorTextView != null && mLoadErrorTextView.getVisibility() != TextView.INVISIBLE) {
            mLoadErrorTextView.setVisibility(TextView.INVISIBLE);
        }
        if (mRecyclerView != null && mRecyclerView.getVisibility() != RecyclerView.VISIBLE) {
            mRecyclerView.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private void showError(String error) {
        Log.d(TAG, error);
        if (mLoadErrorTextView != null) {
            mLoadErrorTextView.setText(error);
            mLoadErrorTextView.setVisibility(TextView.VISIBLE);
        } else {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateThreads() {
        mFetchingThreads = true;

        if (mMode == MODE_BOARD) {
            mILXRequestor.getRecentlyUpdatedThreads(getContext(), mBoard.getId(),
                    (AsyncTaskResult<RecentlyUpdatedThreads> result) -> {
                        updateThreadsReady(result);
                    });
        } else if (mMode == MODE_SNA) {
            mILXRequestor.getSiteNewAnswers(getContext(),
                    (AsyncTaskResult<RecentlyUpdatedThreads> result) -> {
                        updateThreadsReady(result);
                    });
        } else if (mMode == MODE_MARKS) {
            mILXRequestor.getBookmarks(getContext(), (AsyncTaskResult<Bookmarks> result) -> {
                updateBookmarksReady(result);
            });
        }
    }

    private void getBoards() {
        mFetchingBoards = true;

        mILXRequestor.getBoards(
                (AsyncTaskResult<Boards> result) -> {
                    getBoardsReady(result);
                }, getContext());
    }

    private void getBoardsReady(AsyncTaskResult<Boards> result) {
        mFetchingBoards = false;
        if (result.getError() == null) {
            mBoards = result.getResult();
            updateUI();
        } else {
            showError(result.getError());
        }
    }

    private void updateThreadsReady(AsyncTaskResult<RecentlyUpdatedThreads> result) {
        mFetchingThreads = false;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (result.getError() == null) {
            mThreads = result.getResult();
            updateUI();
        } else {
            showError(result.getError());
        }
    }

    private void updateBookmarksReady(AsyncTaskResult<Bookmarks> result) {
        mFetchingThreads = false;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (result.getError() == null) {
            mBookmarks = result.getResult();
            updateUI();
        } else {
            showError(result.getError());
        }
    }

    private void updateUI() {

        updateProgressBar();

        if (!mFetchingThreads && !mFetchingBoards) {
            if (mThreads == null && mBookmarks == null) {
                // don't do anything
            } else if (mMode == MODE_SNA && mThreads.getRecentlyUpdatedThreads().size() == 0) {
                showError("No recently updated threads");
            } else if (mMode == MODE_MARKS && mBookmarks.getBookmarks().isEmpty()) {
                showError("No updated bookmarks");
            } else {
                hideError();
                if (mRecyclerView != null
                        && ((mThreads != null && (mBoards != null || mMode == MODE_BOARD))
                        || (mMode == MODE_MARKS && mBookmarks != null))) {
                    mThreadAdapter = new ThreadAdapter();
                    mRecyclerView.setAdapter(mThreadAdapter);
                }
            }
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

        if (mMode == MODE_BOARD) {
            mFloatingActionButton.setVisibility(FloatingActionButton.VISIBLE);
            mFloatingActionButton.setOnClickListener((View v) -> {
                Log.d(TAG, "asking a new question");
                Toast.makeText(getContext(), "yup", Toast.LENGTH_LONG).show();
            });
        }

        updateProgressBar();
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
                mTitleTextView = findById(itemView, R.id.list_item_thread_title_text_view);
                mDateTextView  = findById(itemView, R.id.list_item_thread_date_text_view);
            } else if (mMode == MODE_SNA) {
                mTitleTextView = findById(itemView, R.id.list_item_snathread_title_text_view);
                mDateTextView  = findById(itemView, R.id.list_item_snathread_date_text_view);
                mBoardTitleTextView = findById(itemView, R.id.list_item_snathread_board_name_text_view);
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
            if (mMode == MODE_SNA) {
                mBoardTitleTextView.setText(mBoards.getBoardById(mThread.getBoardId()).getName());
            }
        }

        @Override
        public void onClick(View view) {
            int bookmarkedMessageId = -1;

            Intent intent = ViewThreadActivity
                    .newIntent(getActivity(), mThread.getBoardId(), mThread.getThreadId(), bookmarkedMessageId);
            startActivity(intent);
        }
    }

    private class BookmarkHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Bookmark mBookmark;
        private TextView mTitleTextView;

        public BookmarkHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_bookmark_title_text_view);
        }

        public void bindBookmark(Bookmark bookmark) {
            mBookmark = bookmark;
            mTitleTextView.setText(mBookmark.getTitleForDisplay());
        }

        @Override
        public void onClick(View view) {
            Intent intent = ViewThreadActivity
                    .newIntent(getActivity(), mBookmark.getBoardId(), mBookmark.getThreadId(), mBookmark.getBookmarkedMessageId());
            startActivity(intent);
        }
    }

    private class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof ThreadHolder) {
                ((ThreadHolder) holder).bindThread(
                        mThreads.getRecentlyUpdatedThreads().get(position));
            } else if (mMode == MODE_MARKS) {
                ((BookmarkHolder) holder).bindBookmark(mBookmarks.getBookmarks().get(position));
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view;

            if (mMode == MODE_BOARD) {
                view = layoutInflater.inflate(R.layout.list_item_thread, parent, false);
                return new ThreadHolder(view);
            } else if (mMode == MODE_SNA) {
                view = layoutInflater.inflate(R.layout.list_item_sna_thread, parent, false);
                return new ThreadHolder(view);
            } else if (mMode == MODE_MARKS) {
                view = layoutInflater.inflate(R.layout.list_item_bookmark, parent, false);
                return new BookmarkHolder(view);
            }

            return null;
        }

        @Override
        public int getItemCount() {

            if (mMode == MODE_SNA || mMode == MODE_BOARD) {
                List<RecentlyUpdatedThread> threads = mThreads.getRecentlyUpdatedThreads();
                if (threads != null) {
                    return threads.size();
                } else {
                    Log.d("ThreadAdapter", "no threads");
                    return 0;
                }
            } else if (mMode == MODE_MARKS) {
                return mBookmarks.getBookmarks().size();
            }

            return 0;
        }
    }

}
