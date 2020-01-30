package utility;

import java.util.UUID;

import ntx.note.bookshelf.NtxLauncherActivity;

public class StarTagHeaderModel implements NtxLauncherActivity.ListItem {

    String header;

    public void setheader(String header) {
        this.header = header;
    }

    @Override
    public boolean isHeader() {
        return true;
    }

    @Override
    public String getName() {
        return header;
    }
}
