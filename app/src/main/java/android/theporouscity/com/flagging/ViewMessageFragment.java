package android.theporouscity.com.flagging;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.theporouscity.com.flagging.ilx.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by bergstroml on 7/25/16.
 */

public class ViewMessageFragment extends Fragment implements OnBackPressedListener {
    private static final String TAG = "ViewMessageFragment";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_BOARD_ID = "board_id";
    private static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_THREAD_NAME = "thread_name";
    private Message mMessage;
    private int mBoardId;
    private int mThreadId;
    private String mThreadName;
    private VideoEnabledWebView mWebView;
    private VideoEnabledWebChromeClient mWebChromeClient;
    private TextView mOpenTextView;
    private TextView mSendTextView;

    public ViewMessageFragment() { }

    public static ViewMessageFragment newInstance(Message message, int boardId, int threadId, String threadName) {
        ViewMessageFragment fragment = new ViewMessageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MESSAGE, message);
        args.putInt(ARG_BOARD_ID, boardId);
        args.putInt(ARG_THREAD_ID, threadId);
        args.putString(ARG_THREAD_NAME, threadName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMessage = (Message) getArguments().getParcelable(ARG_MESSAGE);
            mBoardId = getArguments().getInt(ARG_BOARD_ID);
            mThreadId = getArguments().getInt(ARG_THREAD_ID);
            mThreadName = getArguments().getString(ARG_THREAD_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_message, container, false);

        getActivity().setTitle(Html.fromHtml(mThreadName));

        mOpenTextView = (TextView) view.findViewById(R.id.fragment_view_message_view_textview);
        mOpenTextView.setOnClickListener((View v) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(ILXUrlParser.getMessageUrl(mBoardId, mThreadId, mMessage.getMessageId())));
                startActivity(intent);
        });

        mSendTextView = (TextView) view.findViewById(R.id.fragment_view_message_send_textview);
        mSendTextView.setOnClickListener((View v) -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, ILXUrlParser.getMessageUrl(mBoardId, mThreadId, mMessage.getMessageId()));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Send to ..."));
        });

        mWebView = (VideoEnabledWebView) view.findViewById(R.id.fragment_view_message_webview);
        View nonVideoLayout = view.findViewById(R.id.fragment_view_message_nonvideo_relativelayout);
        ViewGroup videoLayout = (ViewGroup) view.findViewById(R.id.fragment_view_message_video_relativelayout);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.fragment_view_message_video_progressbar);
        mWebChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, progressBar, mWebView);

        mWebChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                Window window = getActivity().getWindow();
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = window.getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    window.setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = window.getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    window.setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });

        mWebView.setWebChromeClient(mWebChromeClient);

        mWebView.setWebViewClient(new WebViewClient(){
                                     public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                                         if (url != null && ILXUrlParser.isThreadUrl(url)) {
                                             Activity activity = getActivity();
                                             Intent intent = ViewThreadActivity.newIntent(activity, url);
                                             if (intent != null) {
                                                 activity.startActivity(intent);
                                             }
                                             return true;
                                         } else {
                                             return false;
                                         }
                                     }
                                 });

        mWebView.loadData(ILXTextOutputFormatter.getILXTextOutputFormatter().fixMessageBodyForWebview(mMessage),
                          "text/html", null);

        return view;
    }

    public boolean onBackPressed()
    {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!mWebChromeClient.onBackPressed())
        {
            if (mWebView.canGoBack())
            {
                mWebView.goBack();
                return true;
            }
        }
        return false;
    }
}
