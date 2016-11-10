package android.theporouscity.com.flagging.ilx;

/**
 * Created by bergstroml on 11/4/16.
 */

public class Bookmark {

    private int boardId;
    private int threadId;
    private int bookmarkedMessageId;
    private String bookmarkThreadTitle;

    public Bookmark(int boardId, int threadId, int bookmarkedMessageId) {
        this.boardId = boardId;
        this.threadId = threadId;
        this.bookmarkedMessageId = bookmarkedMessageId;
    }

    public int getBoardId() {
        return boardId;
    }

    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getBookmarkedMessageId() {
        return bookmarkedMessageId;
    }

    public void setBookmarkedMessageId(int bookmarkedMessageId) {
        this.bookmarkedMessageId = bookmarkedMessageId;
    }

    public String getBookmarkThreadTitle() {
        return bookmarkThreadTitle;
    }

    public void setBookmarkThreadTitle(String bookmarkThreadTitle) {
        this.bookmarkThreadTitle = bookmarkThreadTitle;
    }
}