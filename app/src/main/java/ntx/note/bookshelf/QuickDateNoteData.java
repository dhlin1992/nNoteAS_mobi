package ntx.note.bookshelf;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class QuickDateNoteData {
    @SerializedName("bookTitle")
    private String bookTitle = "";

    @SerializedName("uuid")
    private UUID uuid;

    public void setTitle(String title) {
        this.bookTitle = title;
    }

    public void setId(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return this.bookTitle;
    }

    public UUID getId() {
        return this.uuid;
    }
}
