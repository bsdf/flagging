package android.theporouscity.com.flagging;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.theporouscity.com.flagging.ilx.Board;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewBoardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewBoardFragment extends Fragment {

    private static final String ARG_BOARD = "board";
    private Board mBoard;

    private OnFragmentInteractionListener mListener;

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
        if (getArguments() != null) {
            mBoard = getArguments().getParcelable(ARG_BOARD);
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
        return inflater.inflate(R.layout.fragment_view_board, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
