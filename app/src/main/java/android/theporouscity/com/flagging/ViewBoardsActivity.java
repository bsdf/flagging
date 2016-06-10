package android.theporouscity.com.flagging;

import android.support.v4.app.Fragment;

public class ViewBoardsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return ViewBoardsRecyclerViewFragment.newInstance();
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items);

        ILXRequestor ilxRequestor = new ILXRequestor();

        Log.d("ViewBoardsActivity", "about to ask for xml");
        TextView onlyView = (TextView) findViewById(R.id.onlyView);
        ilxRequestor.getBoards((Boards boards) -> onlyView.setText(((Board) boards.getBoards().get(0)).getName()));
    }*/
}
