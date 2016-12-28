package com.theporouscity.flagging;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;

import com.theporouscity.flagging.ilx.Message;
import com.theporouscity.flagging.util.AsyncTaskResult;
import com.theporouscity.flagging.util.ILXRequestor;
import com.theporouscity.flagging.util.ILXTextOutputFormatter;
import com.theporouscity.flagging.util.ILXUrlParser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by bergstroml on 7/25/16.
 */

public class ViewMessageFragment extends Fragment {
    private static final String TAG = "ViewMessageFragment";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_THREAD_NAME = "thread_name";
    private static final String ARG_THREAD_SID = "thread_sid";

    @BindView(R.id.fragment_view_message_webview)
    WebView mWebView;

    @BindView(R.id.fragment_view_message_view_textview)
    TextView mOpenTextView;

    @BindView(R.id.fragment_view_message_send_textview)
    TextView mSendTextView;

    @BindView(R.id.fragment_view_message_bookmark_textview)
    TextView mBookmarkTextView;

    @BindView(R.id.fragment_view_message_button_bar)
    LinearLayout mButtonBar;

    @BindView(R.id.fragment_view_message_relativelayout)
    RelativeLayout mRelativeLayout;

    @Inject
    ILXRequestor mILXRequestor;

    @Inject
    ILXTextOutputFormatter mILXTextOutputFormatter;

    private Message mMessage;
    private int mBoardId;
    private int mThreadId;
    private String mThreadName;
    private String mThreadSid;

    public static ViewMessageFragment newInstance(Message message, int boardId, int threadId,
                                                  String threadName, String threadSid) {
        ViewMessageFragment fragment = new ViewMessageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MESSAGE, message);
        args.putInt(ARG_BOARD_ID, boardId);
        args.putInt(ARG_THREAD_ID, threadId);
        args.putString(ARG_THREAD_NAME, threadName);
        args.putString(ARG_THREAD_SID, threadSid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FlaggingApplication) getActivity().getApplication()).getILXComponent().inject(this);

        if (getArguments() != null) {
            mMessage = (Message) getArguments().getParcelable(ARG_MESSAGE);
            mBoardId = getArguments().getInt(ARG_BOARD_ID);
            mThreadId = getArguments().getInt(ARG_THREAD_ID);
            mThreadName = getArguments().getString(ARG_THREAD_NAME);
            mThreadSid = getArguments().getString(ARG_THREAD_SID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_message, container, false);
        ButterKnife.bind(this, view);

        getActivity().setTitle(Html.fromHtml(mThreadName));

        mBookmarkTextView.setOnClickListener((View v) -> {
            mILXRequestor.addBookmark(mBoardId, mThreadId, mMessage.getMessageId(), mThreadSid,
                    getContext(), (AsyncTaskResult<Boolean> result) -> {
                        if (result.getError() == null) {
                            Toast.makeText(getContext(), "Bookmark set", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Problem setting bookmark: " + result.getError().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        mOpenTextView.setOnClickListener((View v) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ILXUrlParser.getMessageUrl(mBoardId, mThreadId, mMessage.getMessageId())));
            startActivity(intent);
        });

        mSendTextView.setOnClickListener((View v) -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, ILXUrlParser.getMessageUrl(mBoardId, mThreadId, mMessage.getMessageId()));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Send to ..."));
        });

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient(){
                                     public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                                         if (url != null) {
                                             if(ILXUrlParser.isThreadUrl(url)) {
                                                 Activity activity = getActivity();
                                                 Intent intent = ViewThreadActivity.newIntent(activity, url);
                                                 if (intent != null) {
                                                     activity.startActivity(intent);
                                                 }
                                             }
                                             else {
                                                 Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                 startActivity(i);
                                             }
                                             return true;
                                         } else {
                                             return false;
                                         }
                                     }
                                 });

        if (((ViewMessageActivity) getActivity()).deviceIsRotated()) {

            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            ((ViewMessageActivity) getActivity()).getSupportActionBar().hide();

            mRelativeLayout.removeView(mButtonBar);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mWebView.getLayoutParams();
            layoutParams.setMarginStart(0);
            layoutParams.setMarginStart(0);
            mWebView.setLayoutParams(layoutParams);

        }

        mWebView.loadData(mILXTextOutputFormatter.fixMessageBodyForWebview(mMessage),
                "text/html; charset=utf-8", null);

        return view;
    }
}
