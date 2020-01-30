package name.vbraun.view.write;

import java.util.LinkedList;

import ntx.note.ToolboxConfiguration;

public class TouchHandlerPolygon extends TouchHandlerControlPointABC {
    private static final String TAG = "TouchHandlerPolygon";

    protected TouchHandlerPolygon(HandwriterView view) {
        super(view, view.getOnlyPenInput());
    }

    @Override
    protected LinkedList<? extends GraphicsControlPoint> getGraphicsObjects() {
        return getPage().nooseArt;
    }

    @Override
    protected GraphicsControlPoint newGraphics(float x, float y, float pressure) {
        getPage().nooseArt.clear();
        GraphicsNoose noose = new GraphicsNoose(getPage().getTransform());
        return noose;
    }

    @Override
    protected void destroy() {
    }
}
