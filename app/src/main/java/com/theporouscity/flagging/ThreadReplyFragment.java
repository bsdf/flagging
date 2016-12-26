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

import com.theporouscity.flagging.ilx.Thread;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadReplyFragment extends Fragment {

    private static final String TAG = "ThreadReplyFragment";
    private static final String ARG_THREAD = "thread";

    @BindView(R.id.fragment_thread_reply_title)
    TextView mReplyTitle;

    @BindView(R.id.thread_reply_fab)
    FloatingActionButton mReplyFab;

    @Inject
    ILXRequestor mILXRequestor;

    private Thread mThread;

    public static ThreadReplyFragment newInstance(Thread thread) {
        ThreadReplyFragment fragment = new ThreadReplyFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_THREAD, Parcels.wrap(thread));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        if (getArguments() != null) {
            mThread = Parcels.unwrap(getArguments().getParcelable(ARG_THREAD));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_reply, container, false);
        ButterKnife.bind(this, view);

        mReplyTitle.setText(Html.fromHtml(mThread.getTitle()));
        getActivity().setTitle("Reply to Thread");

        mReplyFab.setOnClickListener(v -> {
            Log.d(TAG, "fab clicked");
            mILXRequestor.postAnswer(mThread, "hello world");
//            mILXRequestor.postAnswer();
        });

        return view;
    }
}
