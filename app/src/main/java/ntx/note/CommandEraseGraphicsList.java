package ntx.note;


import java.util.LinkedList;

import name.vbraun.view.write.Graphics;
import name.vbraun.view.write.Page;
import ntx.note2.R;

public class CommandEraseGraphicsList extends Command {

    protected final LinkedList<Graphics> graphics;

    public CommandEraseGraphicsList(Page page, LinkedList<Graphics> toErase) {
        super(page);
        graphics = toErase;
    }

    @Override
    public void execute() {
        UndoManager.getApplication().remove_for_erase(getPageUuid(), graphics);
    }

    @Override
    public void revert() {
        UndoManager.getApplication().add_for_erase_revert(getPageUuid(), graphics);
    }

    @Override
    public String toString() {
        int n = Global.getCurrentBook().getPageNumber(getPage());
        NoteWriterActivity app = UndoManager.getApplication();
        return app.getString(R.string.command_erase_graphics, n);
    }

}
