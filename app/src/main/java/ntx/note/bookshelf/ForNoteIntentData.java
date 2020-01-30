package ntx.note.bookshelf;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class ForNoteIntentData {

	@SerializedName("selectUUID")
	private UUID selectUUID;

	@SerializedName("launcherListType")
	private int launcherListType;
	
	public void setSelectUUID(UUID selectUUID) {
		this.selectUUID = selectUUID;
	}
	
	public UUID getSelectUUID() {
		return this.selectUUID;
	}

	public void setLauncherListType(int launcherListType) {
		this.launcherListType = launcherListType;
	}

	public int getLauncherListType() {
		return this.launcherListType;
	}
}
