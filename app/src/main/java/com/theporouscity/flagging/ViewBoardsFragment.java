package com.theporouscity.flagging;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;

import com.theporouscity.flagging.ilx.Board;
import com.theporouscity.flagging.ilx.Boards;
import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXRequestor;

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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by bergstroml on 4/1/16.
 */
public class ViewBoardsFragment extends Fragment {

    @BindView(R.id.fragment_view_items_recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_view_items_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.fragment_view_items_loaderrortext)
    TextView mLoadErrorTextView;

    @Inject
    ILXRequestor mILXRequestor;

    private BoardAdapter mBoardAdapter;
    private Boards mBoards;
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

        super.onCreate(savedInstanceState);
        // TODO this is a no-no - should only use retained instance for headless fragments
        setRetainInstance(true);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        mBoards = null;
        mBoardsForDisplay = null;
        mEditing = false;

        updateBoards();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_boards, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration((getActivity())));

        updateUI();
        return view;
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

    private void showError(Exception e)
    {
        Log.d(TAG, e.toString());
        if (mLoadErrorTextView != null) {
            mLoadErrorTextView.setText(e.getMessage());
            mLoadErrorTextView.setVisibility(TextView.VISIBLE);
        } else {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String e)
    {
        Log.d(TAG, e);
        if (mLoadErrorTextView != null) {
            mLoadErrorTextView.setText(e);
            mLoadErrorTextView.setVisibility(TextView.VISIBLE);
        } else {
            Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBoards() {

        mFetching = true;

        mILXRequestor.getBoards((AsyncTaskResult<Boards> result) -> {
            if (result.getError() == null) {
                mFetching = false;
                mBoards = result.getResult();
                if (mBoardsForDisplay == null) {
                    mBoardsForDisplay = new ArrayList<Board>();
                } else {
                    mBoardsForDisplay.clear();
                }
                for (Board board : mBoards.getBoards()) {
                    if (mEditing || board.isEnabled()) {
                        mBoardsForDisplay.add(board);
                    }
                }
                updateUI();
            } else {
                showError(result.getError());
            }
        }, getContext());
    }

    private void updateUI() {

        if (mProgressBar != null) {
            if (mFetching) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (mBoards == null) {
                    showError("Problem loading boards");
                }
            }
        }

        if (mRecyclerView != null && mBoards != null) {
            mBoardAdapter = new BoardAdapter();
            mRecyclerView.setAdapter(mBoardAdapter);
        }
    }

    class BoardHolder extends RecyclerView.ViewHolder
    implements View.OnLongClickListener, View.OnClickListener {

        @BindView(R.id.list_item_board_title_text_view)
        TextView mTitleTextView;

        @BindView(R.id.list_item_board_enabled_switch)
        SwitchCompat mEnabledSwitch;

        private Board mBoard;
        private String mBoardDescription;

        public BoardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void bindBoard(Board board) {
            mBoard = board;
            mTitleTextView.setText(board.getName());
            mBoardDescription = board.getDescription();
            mEnabledSwitch.setOnCheckedChangeListener(null); //TODO there's got to be a better way
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

            mBoard.setEnabled(newEnabled);
            mILXRequestor.persistBoardEnabledState(mBoard, getContext());

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
            holder.bindBoard(mBoardsForDisplay.get(position));
        }

        @Override
        public int getItemCount() {
            return mBoardsForDisplay.size();
        }
    }

}
