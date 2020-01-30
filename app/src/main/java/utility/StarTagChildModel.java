package utility;

import java.util.UUID;

import ntx.note.bookshelf.NtxLauncherActivity;

public class StarTagChildModel implements NtxLauncherActivity.ListItem {

    private String title;

    private UUID BookUUID;

    private int PageNumber;

    private UUID PageUuid;

    public void setName(String title) {
        this.title = title;
    }

    public void setBookUUID(UUID BookUUID) {
        this.BookUUID = BookUUID;
    }

    public void setPageNumber(int PageNumber) {
        this.PageNumber = PageNumber;
    }

    public void setPageUuid(UUID pageUuid) {
        PageUuid = pageUuid;
    }

    public UUID getBookUUID() {
        return this.BookUUID;
    }

    public int getPageNumber() {
        return this.PageNumber;
    }

    public UUID getPageUuid() {
        return PageUuid;
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @Override
    public String getName() {
        return title;
    }
}
