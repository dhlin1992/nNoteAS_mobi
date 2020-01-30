package utility;

import android.graphics.RectF;

import java.util.LinkedList;

import name.vbraun.view.write.Graphics;
import name.vbraun.view.write.GraphicsLine;
import name.vbraun.view.write.GraphicsOval;
import name.vbraun.view.write.GraphicsRectangle;
import name.vbraun.view.write.GraphicsTriangle;
import name.vbraun.view.write.Stroke;

public class SelectedObjects {
    public final LinkedList<Stroke> strokes = new LinkedList<>();
    public final LinkedList<GraphicsLine> lineArt = new LinkedList<>();
    public final LinkedList<GraphicsRectangle> rectangleArt = new LinkedList<>();
    public final LinkedList<GraphicsOval> ovalArt = new LinkedList<>();
    public final LinkedList<GraphicsTriangle> triangleArt = new LinkedList<>();
    public final LinkedList<Graphics> allGraphics = new LinkedList<>();
    public final RectF range = new RectF();

    public SelectedObjects() {
    }

    public void addSelectedStroke(Stroke selectedStroke) {
        strokes.add(selectedStroke);
        allGraphics.add(selectedStroke);
    }

    public void addSelectedLine(GraphicsLine selectedLine) {
        lineArt.add(selectedLine);
        allGraphics.add(selectedLine);
    }

    public void addSelectedRectangle(GraphicsRectangle selectedRectangle) {
        rectangleArt.add(selectedRectangle);
        allGraphics.add(selectedRectangle);
    }

    public void addSelectedOval(GraphicsOval selectedOval) {
        ovalArt.add(selectedOval);
        allGraphics.add(selectedOval);
    }

    public void addSelectedTriangle(GraphicsTriangle selectedTriangle) {
        triangleArt.add(selectedTriangle);
        allGraphics.add(selectedTriangle);
    }

    public boolean isEmpty() {
        return strokes.isEmpty() && lineArt.isEmpty() && rectangleArt.isEmpty() && ovalArt.isEmpty() && triangleArt.isEmpty();
    }

    public LinkedList<Graphics> getAllSelectedGraphics() {
        return allGraphics;
    }
}
