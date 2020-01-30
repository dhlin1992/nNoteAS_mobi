package name.vbraun.view.write;

import java.util.LinkedList;

public class TouchHandlerImage extends TouchHandlerControlPointABC {
    private static final String TAG = "TouchHandlerImage";

    protected TouchHandlerImage(HandwriterView view) {
        super(view, view.getOnlyPenInput());
    }

    @Override
    protected LinkedList<? extends GraphicsControlPoint> getGraphicsObjects() {
        return getPage().images;
    }

    protected float maxDistanceControlPointScreen() {
        return 25f;
    }

    @Override
    protected GraphicsControlPoint newGraphics(float x, float y, float pressure) {
        GraphicsImage image = new GraphicsImage(
                getPage().getTransform(), x, y);
        return image;
    }

    @Override
    protected void destroy() {
    }

}
