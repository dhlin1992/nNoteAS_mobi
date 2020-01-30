package ntx.note.bookshelf;

import android.text.format.Time;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class DateNoteData {
    @SerializedName("bookTitle")
    private String bookTitle = "";

    @SerializedName("uuid")
    private UUID uuid;

    @SerializedName("create Time")
    private String createTime;

    @SerializedName("modify Time")
    private String modifyTime;

    @SerializedName("version")
    private int version;

    @SerializedName("pageNumber")
    private int pageNumber;

    @SerializedName("currentPage")
    private int currentPage;

    @SerializedName("currentPageUuid")
    private UUID currentPageUuid;

    @SerializedName("cTime")
    private Time cTime;

    @SerializedName("mTime")
    private Time mTime;

    @SerializedName("isLandscape")
    private boolean isLandscape;


    public void setTitle(String title) {
        this.bookTitle = title;
    }

    public void setId(UUID uuid) {
        this.uuid = uuid;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setCurrentPageUuid(UUID currentPageUuid) {
        this.currentPageUuid = currentPageUuid;
    }

    public void setCTime(Time cTime) {
        this.cTime = cTime;
    }

    public void setMTime(Time mTime) {
        this.mTime = mTime;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
    }

    public String getTitle() {
        return this.bookTitle;
    }

    public UUID getId() {
        return this.uuid;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public String getModifyTime() {
        return this.modifyTime;
    }

    public int getVersion() {
        return this.version;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public UUID getCurrentPageUuid() {
        return currentPageUuid;
    }

    public Time getCTime() {
        return this.cTime;
    }

    public Time getMTime() {
        return this.mTime;
    }

    public boolean isLandscape() {
        return isLandscape;
    }
}
