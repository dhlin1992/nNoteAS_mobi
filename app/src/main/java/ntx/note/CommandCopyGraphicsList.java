package ntx.note;


import java.util.LinkedList;

import name.vbraun.view.write.Graphics;
import name.vbraun.view.write.Page;
import ntx.note2.R;

public class CommandCopyGraphicsList extends Command {

    protected final LinkedList<Graphics> graphics;

    public CommandCopyGraphicsList(Page page, LinkedList<Graphics> toCopy) {
        super(page);
        graphics = toCopy;
    }

    @Override
    public void execute() {
        UndoManager.getApplication().add_for_erase_revert(getPageUuid(), graphics);
    }

    @Override
    public void revert() {
        UndoManager.getApplication().remove_for_erase(getPageUuid(), graphics);
    }

    @Override
    public String toString() {
        int n = Global.getCurrentBook().getPageNumber(getPage());
        NoteWriterActivity app = UndoManager.getApplication();
        return app.getString(R.string.command_erase_graphics, n);
    }

}
