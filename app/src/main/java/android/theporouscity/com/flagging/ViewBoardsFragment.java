package android.theporouscity.com.flagging;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.theporouscity.com.flagging.ilx.Board;
import android.theporouscity.com.flagging.ilx.Boards;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by bergstroml on 4/1/16.
 */
public class ViewBoardsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BoardAdapter mBoardAdapter;
    private Boards mBoards;
    private ProgressBar mProgressBar;
    private TextView mLoadErrorTextView;
    private boolean mFetching;
    private boolean mScrolling;
    private String TAG = "ViewBoardsFragment";

    public static ViewBoardsFragment newInstance() {

        Bundle args = new Bundle();

        ViewBoardsFragment fragment = new ViewBoardsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mBoards = null;
        updateBoards();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_boards, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_items_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration((getActivity())));

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_view_items_progressbar);
        mLoadErrorTextView = (TextView) view.findViewById(R.id.fragment_view_thread_loaderrortext);

        updateUI();
        return view;
    }

    private void updateBoards() {

        mFetching = true;
        ILXRequestor.getILXRequestor().getBoards((Boards boards) -> {
            mFetching = false;
            mBoards = boards;
            updateUI();
        });

    }

    private void updateUI() {

        if (mProgressBar != null) {
            if (mFetching) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (mBoards == null) {
                    mLoadErrorTextView.setVisibility(TextView.VISIBLE);
                }
            }
        }

        if (mRecyclerView != null && mBoards != null) {
            mBoardAdapter = new BoardAdapter();
            mRecyclerView.setAdapter(mBoardAdapter);
        }
    }

    private class BoardHolder extends RecyclerView.ViewHolder
    implements View.OnLongClickListener, View.OnClickListener {

        private Board mBoard;
        private TextView mTitleTextView;
        private String mBoardDescription;

        //private TextView mDescriptionTextView;

        public BoardHolder(View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView
                    .findViewById(R.id.list_item_board_title_text_view);
            //mDescriptionTextView = (TextView) itemView
            //        .findViewById(R.id.list_item_board_description_text_view);
        }

        public void bindBoard(Board board) {
            mBoard = board;
            mTitleTextView.setText(board.getName());
            mBoardDescription = board.getDescription();
            //mDescriptionTextView.setText(board.getDescription());
        }

        /*
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (motionEvent.getDownTime() > 5000) {
                        Toast.makeText(getActivity(), mBoardDescription,
                                Toast.LENGTH_SHORT).show();
                    }
                    view.setPressed(false);
                    return false;

                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    Intent intent = ViewBoardActivity.newIntent(getActivity(), mBoard);
                    startActivity(intent);
                    return false;

                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    return false;
            }

            return false;
        }
        */

        @Override
        public void onClick(View view) {
            Intent intent = ViewBoardActivity.newIntent(getActivity(), mBoard);
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(getActivity(), mBoardDescription, Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private class BoardAdapter extends RecyclerView.Adapter<BoardHolder> {
        @Override
        public BoardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_board, parent, false);
            return new BoardHolder(view);
        }

        @Override
        public void onBindViewHolder(BoardHolder holder, int position) {
            holder.bindBoard((Board) mBoards.getBoards().get(position));
        }

        @Override
        public int getItemCount() {
            return mBoards.getBoards().size();
        }
    }

}
