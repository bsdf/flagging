package android.theporouscity.com.flagging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.theporouscity.com.flagging.ilx.Board;
import android.theporouscity.com.flagging.ilx.Boards;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
    private boolean mEditing;
    private List<Board> mBoardsForDisplay;
    private String TAG = "ViewBoardsFragment";

    public static ViewBoardsFragment newInstance() {

        Bundle args = new Bundle();

        ViewBoardsFragment fragment = new ViewBoardsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, System.identityHashCode(this) + "OnCreate");

        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mBoards = null;
        mBoardsForDisplay = null;
        mEditing = false;

        updateBoards();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, System.identityHashCode(this) + "OnCreateView");

        View view = inflater.inflate(R.layout.fragment_view_boards, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_view_items_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration((getActivity())));

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_view_items_progressbar);
        mLoadErrorTextView = (TextView) view.findViewById(R.id.fragment_view_thread_loaderrortext);

        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, System.identityHashCode(this) + "OnDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, System.identityHashCode(this) + "OnDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "OnDetach");
    }

    public void toggleEditMode() {
        if (mEditing) {
            mEditing = false;
        } else {
            mEditing = true;
        }
        updateBoards();
        if (mBoardAdapter != null) {
            mBoardAdapter.notifyDataSetChanged();
        }
    }

    private void updateBoards() {

        mFetching = true;
        ILXRequestor.getILXRequestor().getBoards((Boards boards) -> {
            mFetching = false;
            mBoards = boards;
            if (mBoardsForDisplay == null) {
                mBoardsForDisplay = new ArrayList<Board>();
            } else {
                mBoardsForDisplay.clear();
            }
            for (Board board : boards.getBoards()) {
                if (mEditing || board.isEnabled()) {
                    mBoardsForDisplay.add(board);
                }
            }
            updateUI();
        }, getContext());

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
        private SwitchCompat mEnabledSwitch;

        public BoardHolder(View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView
                    .findViewById(R.id.list_item_board_title_text_view);
            mEnabledSwitch = (SwitchCompat) itemView
                    .findViewById(R.id.list_item_board_enabled_switch);
        }

        public void bindBoard(Board board) {
            mBoard = board;
            mTitleTextView.setText(board.getName());
            mBoardDescription = board.getDescription();
            mEnabledSwitch.setOnCheckedChangeListener(null);
            mEnabledSwitch.setChecked(board.isEnabled());
            if (mEditing) {
                mEnabledSwitch.setOnCheckedChangeListener((CompoundButton button, boolean isChecked) -> {
                    toggleBoardChecked();
                });
                mEnabledSwitch.setVisibility(View.VISIBLE);
            } else {
                mEnabledSwitch.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            if (mEditing) {
                toggleBoardChecked();
                mEnabledSwitch.setChecked(mBoard.isEnabled());
            } else {
                Intent intent = ViewBoardActivity.newIntent(getActivity(), mBoard);
                startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(getActivity(), mBoardDescription, Toast.LENGTH_SHORT).show();
            return true;
        }

        private void toggleBoardChecked() {
            boolean newEnabled = mBoard.isEnabled() ? false : true;
            mBoard.setEnabledAndPersist(newEnabled);
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
            holder.bindBoard((Board) mBoardsForDisplay.get(position));
        }

        @Override
        public int getItemCount() {
            return mBoardsForDisplay.size();
        }
    }

}
