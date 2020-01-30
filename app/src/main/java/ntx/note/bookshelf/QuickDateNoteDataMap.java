package ntx.note.bookshelf;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickDateNoteDataMap {
    @SerializedName("DateNoteDataMap")
    private HashMap<String, ArrayList<QuickDateNoteData>> DateNoteDataMap = null;

    public void setDateNoteDataMap(HashMap<String, ArrayList<QuickDateNoteData>> DateNoteDataMap) {
        this.DateNoteDataMap = DateNoteDataMap;
    }

    public HashMap<String, ArrayList<QuickDateNoteData>> getDateNoteDataMap() {
        return this.DateNoteDataMap;
    }
}
