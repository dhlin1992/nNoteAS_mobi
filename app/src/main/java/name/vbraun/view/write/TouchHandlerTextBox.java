package name.vbraun.view.write;

import java.util.LinkedList;

import ntx.note.ToolboxConfiguration;

public class TouchHandlerTextBox extends TouchHandlerControlPointABC {
    private static final String TAG = "TouchHandlerNoose";

    protected TouchHandlerTextBox(HandwriterView view) {
        super(view, view.getOnlyPenInput());
    }

    @Override
    protected LinkedList<? extends GraphicsControlPoint> getGraphicsObjects() {
        return getPage().textBoxes;
    }

    @Override
    protected GraphicsControlPoint newGraphics(float x, float y, float pressure) {
        TextBox textBox = new TextBox(getPage().getTransform(), x, y);
        return textBox;
    }

    @Override
    protected void destroy() {
    }

    @Override
    protected float maxDistanceControlPointScreen() {
        return 25.0f;
    }
}
