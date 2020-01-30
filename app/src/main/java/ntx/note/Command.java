package ntx.note;

import java.util.UUID;

import name.vbraun.view.write.Page;

abstract public class Command {
    private final static String TAG = "Command";

    private Page page;
    private UUID pageUuid;

    protected Command(Page currentPage) {
        page = currentPage;
        pageUuid = currentPage.getUUID();
    }

    public Page getPage() {
        return page;
    }

    public UUID getPageUuid() {
        return pageUuid;
    }

    abstract public void execute();

    abstract public void revert();

    abstract public String toString();
}
