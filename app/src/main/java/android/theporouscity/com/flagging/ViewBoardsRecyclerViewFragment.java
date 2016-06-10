package android.theporouscity.com.flagging;

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
import android.widget.TextView;

import java.util.List;

/**
 * Created by bergstroml on 4/1/16.
 */
public class ViewBoardsRecyclerViewFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BoardAdapter mBoardAdapter;
    private Boards mBoards;

    public static ViewBoardsRecyclerViewFragment newInstance() {

        Bundle args = new Bundle();

        ViewBoardsRecyclerViewFragment fragment = new ViewBoardsRecyclerViewFragment();
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

        View view = inflater.inflate(R.layout.fragment_view_items_recyclerview, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_items_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        return view;
    }

    private void updateBoards() {
        ILXRequestor.getILXRequestor().getBoards((Boards boards) -> { mBoards = boards; updateUI(); });
    }

    private void updateUI() {
        if (mRecyclerView != null && mBoards != null) {
            mBoardAdapter = new BoardAdapter();
            mRecyclerView.setAdapter(mBoardAdapter);
        }
    }

    private class BoardHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener {

        private Board mBoard;
        private TextView mTitleTextView;
        //private TextView mDescriptionTextView;

        public BoardHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView
                    .findViewById(R.id.list_item_board_title_text_view);
            //mDescriptionTextView = (TextView) itemView
            //        .findViewById(R.id.list_item_board_description_text_view);
        }

        public void bindBoard(Board board) {
            mBoard = board;
            mTitleTextView.setText(board.getName());
            //mDescriptionTextView.setText(board.getDescription());
        }

        @Override
        public void onClick(View v) { /* start view board activity */ }
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