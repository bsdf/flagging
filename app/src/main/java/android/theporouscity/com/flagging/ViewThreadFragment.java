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
import android.widget.TextView;

import java.util.List;

/**
 * Created by bergstroml on 6/15/16.
 */

public class ViewThreadFragment extends Fragment {

    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final int mDefaultMessagesChunk = 50;
    private Thread mThread;
    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;

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
            updateThread(
                    getArguments().getInt(ARG_BOARD_ID),
                    getArguments().getInt(ARG_THREAD_ID)
            );
        }
    }

    private void updateThread(int boardId, int threadId) {
        ILXRequestor.getILXRequestor().getThread(boardId, threadId, mDefaultMessagesChunk,
                (Thread thread) -> {
                    mThread = thread;
                    updateUI();
                });
    }

    private void updateUI() {
        if (mRecyclerView != null && mThread != null) {
            mMessageAdapter = new MessageAdapter();
            mRecyclerView.setAdapter(mMessageAdapter);
            getActivity().setTitle(mThread.getTitle());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_thread, container, false);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false) {

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                Log.d("ViewThreadFragment", "laying out children");
                super.onLayoutChildren(recycler, state);
                scrollToPosition(mRecyclerView.getAdapter().getItemCount()-1);
            }

        };

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_thread_recyclerview);
        mRecyclerView.setLayoutManager(layoutManager);
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
            mDisplayNameTextView.setText(mMessage.getDisplayName());
        }

        @Override
        public void onClick(View view) {
            mRecyclerView.getLayoutManager().scrollToPosition(
                    mRecyclerView.getAdapter().getItemCount()-1);
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            holder.bindMessage(
                    (Message) mThread.getMessages().get(position));
            if (position == mThread.getMessages().size() - 1) {
                mRecyclerView.getLayoutManager().scrollToPosition(position);
            }
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_message, parent, false);
            return new MessageHolder(view);
        }

        @Override
        public int getItemCount() {
            List<Message> messages = mThread.getMessages();
            if (messages != null) {
                return messages.size();
            } else {
                Log.d("MessageAdapter", "no messages");
                return 0;
            }
        }
    }

}
