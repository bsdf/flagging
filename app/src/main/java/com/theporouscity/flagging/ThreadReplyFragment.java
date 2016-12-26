package com.theporouscity.flagging;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadReplyFragment extends Fragment {

    private static final String TAG = "ThreadReplyFragment";

    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_THREAD_NAME = "thread_name";

    @BindView(R.id.fragment_thread_reply_title)
    TextView mReplyTitle;

    @BindView(R.id.thread_reply_fab)
    FloatingActionButton mReplyFab;

    @Inject
    ILXRequestor mILXRequestor;

    private int mBoardId;
    private int mThreadId;
    private String mThreadName;

    public static ThreadReplyFragment newInstance(int boardId, int threadId, String threadName) {
        ThreadReplyFragment fragment = new ThreadReplyFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOARD_ID, boardId);
        args.putInt(ARG_THREAD_ID, threadId);
        args.putString(ARG_THREAD_NAME, threadName);
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
            mThreadName = getArguments().getString(ARG_THREAD_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_reply, container, false);
        ButterKnife.bind(this, view);

        mReplyTitle.setText(Html.fromHtml(mThreadName));
        getActivity().setTitle("Reply to Thread");

        mReplyFab.setOnClickListener(v -> {
            Log.d(TAG, "ASDASDASDASD CLICKED!!!");
        });

        return view;
    }
}
