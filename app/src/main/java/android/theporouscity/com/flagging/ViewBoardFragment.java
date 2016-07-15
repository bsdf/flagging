package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.theporouscity.com.flagging.ilx.Board;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ViewBoardFragment extends Fragment {

    private static final String ARG_BOARD = "board";
    private Board mBoard;
    private RecentlyUpdatedThreads mThreads;
    private RecyclerView mRecyclerView;
    private ThreadAdapter mThreadAdapter;
    private ProgressBar mProgressBar;
    private TextView mLoadErrorTextView;
    private boolean mFetching;

    public ViewBoardFragment() {
        // Required empty public constructor
    }

    public static ViewBoardFragment newInstance(Board board) {
        ViewBoardFragment fragment = new ViewBoardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOARD, board);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mBoard = getArguments().getParcelable(ARG_BOARD);
            updateThreads();
        } else {
            mBoard = null;
        }
    }

    private void updateThreads() {
        mFetching = true;
        ILXRequestor.getILXRequestor().getRecentlyUpdatedThreads(mBoard.getBoardId(),
                (RecentlyUpdatedThreads threads) -> {
                    mFetching = false;
                    mThreads = threads;
                    if (mThreads != null) {
                        Log.d("got threads", threads.getURI() + " " + threads.getTotalMessages());
                    }
                    updateUI();
        });
    }

    private void updateUI() {

        if (mProgressBar != null) {
            if (mFetching) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (mThreads == null) {
                    mLoadErrorTextView.setVisibility(TextView.VISIBLE);
                } else if (mThreads.getRecentlyUpdatedThreads().size() == 0) {
                    mLoadErrorTextView.setText("No recent threads");
                    mLoadErrorTextView.setVisibility(TextView.VISIBLE);
                }
            }
        }

        if (mRecyclerView != null && mThreads != null) {
            mThreadAdapter = new ThreadAdapter();
            mRecyclerView.setAdapter(mThreadAdapter);
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
        getActivity().setTitle(mBoard.getName());
        View view = inflater.inflate(R.layout.fragment_view_board, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_threads_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration((getActivity())));

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_view_board_progressbar);
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

    private class ThreadHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private RecentlyUpdatedThread mThread;
        private TextView mTitleTextView;
        private TextView mDateTextView;

        public ThreadHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView
                    .findViewById(R.id.list_item_thread_title_text_view);
            mDateTextView = (TextView) itemView
                    .findViewById(R.id.list_item_thread_date_text_view);
        }

        public void bindThread(RecentlyUpdatedThread thread) {
            mThread = thread;
            mTitleTextView.setText(mThread.getTitle());
            Date lastUpdated = mThread.getLastUpdated();
            mDateTextView.setText(ILXDateOutputFormat.formatRelativeDateShort(lastUpdated));
        }

        @Override
        public void onClick(View view) {
            Intent intent = ViewThreadActivity
                    .newIntent(getActivity(), mThread.getBoardId(), mThread.getThreadId());
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
            View view = layoutInflater.inflate(R.layout.list_item_thread, parent, false);
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
