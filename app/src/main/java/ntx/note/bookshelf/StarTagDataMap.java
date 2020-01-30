package ntx.note.bookshelf;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class StarTagDataMap {
    @SerializedName("StarTagDataMap")
    private HashMap<String, ArrayList<StarTagData>> StartTagDataMap = null;

    public void setDateNoteDataMap(HashMap<String, ArrayList<StarTagData>> StartTagDataMap) {
        this.StartTagDataMap = StartTagDataMap;
    }

    public HashMap<String, ArrayList<StarTagData>> getDateNoteDataMap() {
        return this.StartTagDataMap;
    }
}
