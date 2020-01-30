package ntx.note.bookshelf;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class RecentlyNoteData {
    @SerializedName("recentIndex")
    private int recentIndex = 0;

    @SerializedName("noteId")
    private UUID noteId;

    @SerializedName("isLandscape")
    private boolean isLandscape;

    @SerializedName("noteTilte")
    private String noteTilte = "";

    @SerializedName("notePath")
    private String notePath = "";

    @SerializedName("noteHasCover")
    private boolean noteHasCover = false;

    public RecentlyNoteData(int index) {
        this.recentIndex = index;
    }

    public void setId(UUID id) {
        this.noteId = id;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
    }

    public void setTitle(String title) {
        this.noteTilte = title;
    }

    public void setPath(String path) {
        this.notePath = path;
    }

    public void setCover(boolean hasCover) {
        this.noteHasCover = hasCover;
    }

    public int getIndex() {
        return this.recentIndex;
    }

    public UUID getId() {
        return this.noteId;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public String getTitle() {
        return this.noteTilte;
    }

    public String getPath() {
        return this.notePath;
    }

    public boolean hasCover() {
        return this.noteHasCover;
    }
}
