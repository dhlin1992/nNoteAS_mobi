package ntx.note;

import java.util.LinkedList;

import name.vbraun.view.write.Graphics;
import name.vbraun.view.write.Page;

public class CommandModifyGraphicsList extends Command {

    protected final LinkedList<Graphics> graphicsList;
    protected final LinkedList<Graphics> backupDataGraphicsList;
    protected final LinkedList<Graphics> newDataGraphicsList;

    public CommandModifyGraphicsList(Page page, LinkedList<Graphics> modifiedGraphicsList) {
        super(page);
        graphicsList = new LinkedList<>();
        graphicsList.addAll(modifiedGraphicsList);

        backupDataGraphicsList = new LinkedList<>();
        newDataGraphicsList = new LinkedList<>();
        for (Graphics graphics : modifiedGraphicsList) {
            backupDataGraphicsList.add(graphics.getBackupGraphics());
            newDataGraphicsList.add(graphics.getCloneGraphics());
        }
    }

    @Override
    public void execute() {
        UndoManager.getApplication().modify_graphicsList(getPageUuid(), graphicsList, newDataGraphicsList);
    }

    @Override
    public void revert() {
        UndoManager.getApplication().modify_graphicsList(getPageUuid(), graphicsList, backupDataGraphicsList);

    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

}
