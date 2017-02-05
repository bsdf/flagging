package com.theporouscity.flagging;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.theporouscity.flagging.util.ILXRequestor;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadReplyFragment extends Fragment {

    private static final String TAG = "ThreadReplyFragment";

    private static final String ARG_BOARD_ID = "boardId";
    private static final String ARG_THREAD_ID = "threadId";
    private static final String ARG_THREAD_NAME = "threadName";
    private static final String ARG_MESSAGE_COUNT = "messageCount";
    private static final String ARG_SKEY = "sKey";

    @BindView(R.id.fragment_thread_reply_title)
    TextView mReplyTitle;

    @BindView(R.id.thread_reply_fab)
    FloatingActionButton mReplyFab;

    @BindView(R.id.reply_text)
    EditText replyText;

    @Inject
    ILXRequestor mILXRequestor;

    private int boardId;
    private int threadId;
    private String threadName;
    private int messageCount;
    private String sKey;

    public static ThreadReplyFragment newInstance(int boardId, int threadId, String threadName, String sKey, int messageCount) {
        ThreadReplyFragment fragment = new ThreadReplyFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOARD_ID, boardId);
        args.putInt(ARG_THREAD_ID, threadId);
        args.putString(ARG_THREAD_NAME, threadName);
        args.putInt(ARG_MESSAGE_COUNT, messageCount);
        args.putString(ARG_SKEY, sKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        if (getArguments() != null) {
            boardId = getArguments().getInt(ARG_BOARD_ID);
            threadId = getArguments().getInt(ARG_THREAD_ID);
            threadName = getArguments().getString(ARG_THREAD_NAME);
            messageCount = getArguments().getInt(ARG_MESSAGE_COUNT);
            sKey = getArguments().getString(ARG_SKEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_reply, container, false);
        ButterKnife.bind(this, view);

        mReplyTitle.setText(Html.fromHtml(threadName));
        getActivity().setTitle("Reply to Thread");

        mReplyFab.setOnClickListener(v -> {
            String reply = replyText.getText().toString();
            if (reply != null && !reply.trim().equals("")) {
                Log.d(TAG, "posting reply..");
                mILXRequestor.postAnswer(getContext(), reply, boardId, threadId, sKey, messageCount,
                        result -> {
                            if (result.getError() == null) {
                                Log.d(TAG, "reply posted.");
                                Toast.makeText(getContext(), "Reply posted", Toast.LENGTH_SHORT).show();
                                getActivity().onBackPressed();
                            } else {
                                Log.d(TAG, "error posting reply: " + result.getError().toString());
                                Toast.makeText(getContext(), "Error posting reply", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        return view;
    }
}
