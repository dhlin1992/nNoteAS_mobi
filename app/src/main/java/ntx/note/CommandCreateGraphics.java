package ntx.note;


import name.vbraun.view.write.Graphics;
import name.vbraun.view.write.Page;
import ntx.note2.R;

public class CommandCreateGraphics extends Command {

    protected final Graphics graphics;

    public CommandCreateGraphics(Page page, Graphics toAdd) {
        super(page);
        graphics = toAdd;
    }

    @Override
    public void execute() {
        UndoManager.getApplication().add(getPageUuid(), graphics);
    }

    @Override
    public void revert() {
        UndoManager.getApplication().remove(getPageUuid(), graphics);
    }

    @Override
    public String toString() {
        int n = Global.getCurrentBook().getPageNumber(getPage());
        NoteWriterActivity app = UndoManager.getApplication();
        return app.getString(R.string.command_create_graphics, n);
    }
}
