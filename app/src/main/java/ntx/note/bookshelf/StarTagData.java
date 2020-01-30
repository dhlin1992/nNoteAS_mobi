package ntx.note.bookshelf;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class StarTagData {
    @SerializedName("PageUUID")
    private UUID PageUUID;

    @SerializedName("Page")
    private int Page;

    @SerializedName("BookName")
    private String BookName;

    @SerializedName("BookUUID")
    private UUID BookUUID;

    public void setPageUUID(UUID PageUUID) {
        this.PageUUID = PageUUID;
    }

    public UUID getPageUUID() {
        return this.PageUUID;
    }

    public void setPage(int Page) {
        this.Page = Page;
    }

    public int getPage() {
        return this.Page;
    }

    public void setBookName(String BookName) {
        this.BookName = BookName;
    }

    public String getBookName() {
        return this.BookName;
    }

    public void setBookUUID(UUID BookUUID) {
        this.BookUUID = BookUUID;
    }

    public UUID getBookUUID() {
        return this.BookUUID;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StarTagData)) return false;
        StarTagData o = (StarTagData) obj;
        return o.getPageUUID().equals(this.getPageUUID());
    }
}
