package name.vbraun.view.write;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import junit.framework.Assert;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedList;

import name.vbraun.lib.pen.PenEventNTX;
import name.vbraun.view.write.Graphics.Tool;
import name.vbraun.view.write.GraphicsControlPoint.ControlPoint;
import ntx.draw.nDrawHelper;
import ntx.note.CallbackEvent;
import ntx.note.Global;
import ntx.note.ToolboxConfiguration;

import static ntx.note.Global.PLUS_REDRAW_RANGE;

/**
 * Base class for touch handles than manipulate control points
 *
 * @author vbraun
 */
public abstract class TouchHandlerControlPointABC extends TouchHandlerABC {
    private final static String TAG = "TouchHandlerControlPointABC";
    private final static int MOVE_BOUNDING_GAP = 1;

    private final boolean activePen;

    private int penID = -1;
    private int fingerId1 = -1;
    private int fingerId2 = -1;
    private float oldPressure, newPressure;
    private float oldX, oldY, newX, newY; // main pointer (usually pen)
    private float oldX1, oldY1, newX1, newY1; // for 1st finger
    private float oldX2, oldY2, newX2, newY2; // for 2nd finger
    private long oldT, newT;

    private GraphicsControlPoint.ControlPoint activeControlPoint = null;

    public int down_sample_counter = 0;
    private float penDownX, penDownY, penUpX, penUpY;
    private float lastCenterScreenX, lastCenterScreenY;

    private GraphicsControlPoint mActionDownInBoundGraphics;
    private GraphicsControlPoint mActionUpInBoundGraphics;
    private static boolean nDrawIsOpen = false;
    private static boolean isChangeMode = false;
    protected ArrayList<Point> polygon = new ArrayList<>();
    protected Point oldPoint;

    protected TouchHandlerControlPointABC(HandwriterView view, boolean activePen) {
        super(view);
        this.activePen = activePen;
        view.invalidate(); // make control points appear
    }

    @Override
    protected void interrupt() {
        abortMotion();
        super.interrupt();
    }

    @Override
    protected boolean onTouchEvent(MotionEvent event) {
        if (activePen)
            return onTouchEventActivePen(event);
        else
            return onTouchEventPassivePen(event);
    }

    protected boolean onTouchEventPassivePen(MotionEvent event) {
        // TODO
        return onTouchEventActivePen(event);
    }

    protected GraphicsControlPoint newGraphicsObject = null;

    protected boolean onTouchEventActivePen(MotionEvent event) {

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {

            if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.POLYGON_NOOSE) {
                if (polygon.size() == 0) {
                    oldPoint = new Point(Math.round(event.getX()), Math.round(event.getY()));
                    polygon.add(oldPoint);
                } else if (oldPoint != null && Math.sqrt(Math.pow(oldPoint.x - event.getX(), 2) + Math.pow(oldPoint.y - event.getY(), 2)) > 50) {
                    oldPoint = new Point(Math.round(event.getX()), Math.round(event.getY()));
                    polygon.add(oldPoint);
                }
            }

            if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.POLYGON_NOOSE
                    && (activeControlPoint != null && activeControlPoint.getGraphics().isCenterControlPoint(activeControlPoint))) {

                if (!isChangeMode) {
                    Intent aw = new Intent("ntx.eink_control.GLOBAL_REFRESH");
                    aw.putExtra("updatemode", PenEventNTX.UPDATE_MODE_GLOBAL_PARTIAL_A2_WITH_DITHER_WITH_WAIT);
                    aw.putExtra("commandFromNtxApp", true);
                    getContext().sendBroadcast(aw);

                    Intent quickIntent = new Intent("ntx.eink_control.QUICK_REFRESH");
                    quickIntent.putExtra("updatemode", PenEventNTX.UPDATE_MODE_FULL_DU_WITH_DITHER);
                    quickIntent.putExtra("commandFromNtxApp", true);
                    getContext().sendBroadcast(quickIntent);
                    isChangeMode = true;
                }

                if (nDrawIsOpen) {
                    nDrawHelper.NDrawSwitch(false);
                    nDrawIsOpen = false;
                }
            }

            //if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.LINE) {
            // Dango 20180921 : down sample for Tool.LINE
            down_sample_counter++;
            if (down_sample_counter % 15 != 0) {
                return false;
            } else {
                down_sample_counter = 0;
            }
            //}


            if (getMoveGestureWhileWriting() && fingerId1 != -1 && fingerId2 == -1) {
                int idx1 = event.findPointerIndex(fingerId1);
                if (idx1 != -1) {
                    oldX1 = newX1 = event.getX(idx1);
                    oldY1 = newY1 = event.getY(idx1);
                }
            }
            if (getMoveGestureWhileWriting() && fingerId2 != -1) {
                Assert.assertTrue(fingerId1 != -1);
                int idx1 = event.findPointerIndex(fingerId1);
                int idx2 = event.findPointerIndex(fingerId2);
                if (idx1 == -1 || idx2 == -1)
                    return true;
                newX1 = event.getX(idx1);
                newY1 = event.getY(idx1);
                newX2 = event.getX(idx2);
                newY2 = event.getY(idx2);
                view.invalidate();
                return true;
            }
            if (penID == -1)
                return true;
            int penIdx = event.findPointerIndex(penID);
            if (penIdx == -1)
                return true;

            oldT = newT;
            newT = System.currentTimeMillis();
            // Log.v(TAG, "ACTION_MOVE index="+pen+" pointerID="+penID);
            oldX = newX;
            oldY = newY;
            oldPressure = newPressure;
            newX = event.getX(penIdx);
            newY = event.getY(penIdx);
            newPressure = event.getPressure(penIdx);
            drawOutline(newX, newY);
            return true;
        } else if (action == MotionEvent.ACTION_DOWN) {
            penDownX = event.getX();
            penDownY = event.getY();
            Assert.assertTrue(event.getPointerCount() == 1);
            newT = System.currentTimeMillis();
            if (useForTouch(event) && getDoubleTapWhileWriting() && Math.abs(newT - oldT) < 250) {
                // double-tap
                // view.centerAndFillScreen(event.getX(), event.getY());
                view.zoomOutAndFillScreen();
                abortMotion();
                return true;
            }
            oldT = newT;
            if (useForTouch(event) && getMoveGestureWhileWriting() && event.getPointerCount() == 1) {
                fingerId1 = event.getPointerId(0);
                fingerId2 = -1;
                newX1 = oldX1 = event.getX();
                newY1 = oldY1 = event.getY();
            }
            if (penID != -1) {
                abortMotion();
                return true;
            }
            // Log.v(TAG, "ACTION_DOWN");
            if (!useForWriting(event))
                return true; // eat non-pen events
            penID = event.getPointerId(0);
            activeControlPoint = findControlPoint(event.getX(), event.getY());

            if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.POLYGON_NOOSE) {
                polygon.clear();
                oldPoint = null;
                isChangeMode = false;
            }

            mActionDownInBoundGraphics = isPointInBound(penDownX, penDownY);

            if (activeControlPoint == null) {
                // none within range, create new graphics
                getPage().clearSelectedObjects();
                if (!getPage().nooseArt.isEmpty()) {
                    RectF box = getPage().nooseArt.get(0).getBoundingBox();
                    box.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                    Rect boxRoundOut = new Rect();
                    box.roundOut(boxRoundOut);
                    getPage().nooseArt.clear();
                    getPage().draw(view.canvas, box);
                    CallbackEvent callbackEvent = new CallbackEvent();
                    callbackEvent.setMessage(CallbackEvent.NOOSE_ALL_BTN_GONE);
                    EventBus.getDefault().post(callbackEvent);
                    view.invalidate(boxRoundOut);
                }
                newGraphicsObject = newGraphics(event.getX(), event.getY(), event.getPressure());
                activeControlPoint = newGraphicsObject.initialControlPoint();
                bBox.setEmpty();
            } else {
                mActionDownInBoundGraphics = null;

                if (activeControlPoint.getGraphics() instanceof GraphicsNoose) {
                    lastCenterScreenX = activeControlPoint.screenX();
                    lastCenterScreenY = activeControlPoint.screenY();
                }
                if (activeControlPoint.getGraphics() instanceof TextBox)
                    view.setNowEditedGraphics(activeControlPoint.getGraphics());
                activeControlPoint.getGraphics().backup();
                bBox.set(activeControlPoint.getGraphics().getBoundingBox());
            }

            if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.POLYGON_NOOSE
                    && (activeControlPoint == null || !activeControlPoint.getGraphics().isCenterControlPoint(activeControlPoint))) {
                nDrawHelper.NDrawSetPenType(Global.NDRAW_PEN_TYPE_PENCIL);
                nDrawHelper.NDrawSetMaxStrokeWidthWhenUsingPressure(-1);
                nDrawHelper.NDrawSetStrokeWidth(6);
                nDrawHelper.NDrawSetGreyPaint(8);
                nDrawHelper.NDrawSwitch(true);
                nDrawIsOpen = true;
            }

            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            penUpX = event.getX();
            penUpY = event.getY();
            mActionUpInBoundGraphics = isPointInBound(penUpX, penUpY);
            onPenUp();
            abortMotion();
            return true;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            // e.g. you start with finger and use pen
            // if (event.getPointerId(0) != penID) return true;

            abortMotion();
            getPage().draw(view.canvas);
            view.invalidate();
            return true;
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) { // start move gesture
            if (penID != -1)
                return true; // ignore, we are currently moving a control point
            if (fingerId1 == -1)
                return true; // ignore after move finished
            if (fingerId2 != -1)
                return true; // ignore more than 2 fingers
            int idx2 = event.getActionIndex();
            oldX2 = newX2 = event.getX(idx2);
            oldY2 = newY2 = event.getY(idx2);
            float dx = newX2 - newX1;
            float dy = newY2 - newY1;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance >= getMoveGestureMinDistance()) {
                fingerId2 = event.getPointerId(idx2);
            }
            // Log.v(TAG, "ACTION_POINTER_DOWN "+fingerId2+" + "+fingerId1+" "+oldX1+"
            // "+oldY1+" "+oldX2+" "+oldY2);
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            int idx = event.getActionIndex();
            int id = event.getPointerId(idx);
            if (getMoveGestureWhileWriting() && (id == fingerId1 || id == fingerId2) && fingerId1 != -1
                    && fingerId2 != -1) {
                Page page = getPage();

                Transformation t = pinchZoomTransform(page.getTransform(), oldX1, newX1, oldX2, newX2, oldY1, newY1,
                        oldY2, newY2);
                page.setTransform(t, view.canvas);

                page.draw(view.canvas);
                view.invalidate();
                abortMotion();
            }
        }
        return false;
    }

    protected void onPenUp() {

        boolean isNew = (newGraphicsObject != null);
        CallbackEvent callbackEvent = new CallbackEvent();
        float deltaX = Math.abs(penUpX - penDownX);
        float deltaY = Math.abs(penUpY - penDownY);
        RectF drawingBoundingBox;
        Rect drawingBoundingBoxRoundOut = new Rect();

        if (isNew) {
            if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.IMAGE) {
                if (deltaX >= GraphicsImage.DEFAULT_IMAGE_SIZE && deltaY >= GraphicsImage.DEFAULT_IMAGE_SIZE) {
                    /**
                     * Add new Image
                     */
                    view.setNowEditedGraphics(newGraphicsObject);
                    view.addPhotoControlDialog.show();
                } else {
                    if (mActionUpInBoundGraphics != null && mActionUpInBoundGraphics.equals(mActionDownInBoundGraphics)) {
                        if (checkGraphicsImageSizeAndRedraw(deltaX, deltaY)) {
                            view.setNowEditedGraphics(newGraphicsObject);
                            view.addPhotoControlDialog.show();
                        } else {
                            view.photoControlDialog.show();
                            view.setNowEditedGraphics(mActionUpInBoundGraphics);
                        }
                    } else {
                        ((GraphicsImage) newGraphicsObject).resizeGraphics(GraphicsImage.DEFAULT_IMAGE_SIZE, GraphicsImage.DEFAULT_IMAGE_SIZE);
                        view.setNowEditedGraphics(newGraphicsObject);

                        drawingBoundingBox = newGraphicsObject.getBoundingBox();
                        drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                        drawingBoundingBox.round(drawingBoundingBoxRoundOut);
                        getPage().draw(view.canvas, drawingBoundingBox);
                        newGraphicsObject.draw(view.canvas);
                        view.invalidate(drawingBoundingBoxRoundOut);

                        view.addPhotoControlDialog.show();
                    }
                }
            } else if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.TEXT) {
                if (deltaX > TextBox.NEW_TEXT_BOX_WIDTH_THRESHOLD) {
                    /**
                     * Add new TextBox.
                     */
                    if (deltaX < TextBox.MIN_TEXT_BOX_WIDTH) {
                        ((TextBox) newGraphicsObject).resizeGraphics(TextBox.MIN_TEXT_BOX_WIDTH, TextBox.MIN_TEXT_BOX_HEIGHT);
                        view.setNowEditedGraphics(newGraphicsObject);
                        drawingBoundingBox = newGraphicsObject.getBoundingBox();
                        drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                        drawingBoundingBox.round(drawingBoundingBoxRoundOut);
                        newGraphicsObject.draw(view.canvas);
                        view.invalidate(drawingBoundingBoxRoundOut);
                    } else
                        view.setNowEditedGraphics(newGraphicsObject);

                    view.showTextBoxEditorDialog("");

                } else {
                    if (mActionDownInBoundGraphics == null) {
                        if (view.getNowEditedGraphics() != null && view.getNowEditedGraphics() instanceof TextBox) {
                            /**
                             * Unselected current TextBox.
                             */
                            view.setNowEditedGraphics(null);
                            drawingBoundingBox = newGraphicsObject.getBoundingBox();
                            drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                            getPage().draw(view.canvas, drawingBoundingBox);
                        } else {
                            /**
                             * Add new TextBox.
                             */
                            ((TextBox) newGraphicsObject).resizeGraphics(TextBox.MIN_TEXT_BOX_WIDTH, TextBox.MIN_TEXT_BOX_HEIGHT);
                            view.setNowEditedGraphics(newGraphicsObject);

                            drawingBoundingBox = newGraphicsObject.getBoundingBox();
                            drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                            drawingBoundingBox.round(drawingBoundingBoxRoundOut);
                            newGraphicsObject.draw(view.canvas);
                            view.invalidate(drawingBoundingBoxRoundOut);

                            view.showTextBoxEditorDialog("");
                        }
                    } else {
                        drawingBoundingBox = newGraphicsObject.getBoundingBox();
                        drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                        getPage().draw(view.canvas, drawingBoundingBox);

                        if (view.getNowEditedGraphics() != null
                                && mActionDownInBoundGraphics.equals(view.getNowEditedGraphics())
                                && ((TextBox) view.getNowEditedGraphics()).isSelected()) {
                            /**
                             * Edit the selected TextBox.
                             */
                            view.showTextBoxEditorDialog(view.getCurrentEditTextBox().getTextStr());
                        } else {
                            /**
                             * TextBox set selected.
                             */
                            view.setNowEditedGraphics(mActionDownInBoundGraphics);
                        }
                    }
                }
            } else if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.NOOSE) {
                RectF oldRange = newGraphicsObject.getBoundingBox();
                RectF newRange = getPage().getSelectedRangeRect(newGraphicsObject.getBoundingBox());
                if (!newRange.isEmpty()) {
                    view.setNowEditedGraphics(newGraphicsObject);
                    ((GraphicsNoose) newGraphicsObject).reSetRang(newRange);
                    getPage().nooseArt.add((GraphicsNoose) newGraphicsObject);
                    callbackEvent.setMessage(CallbackEvent.NOOSE_COPY_AND_DELETE_AND_CUT_BTN_VISIBLE);
                    EventBus.getDefault().post(callbackEvent);
                } else {
                    callbackEvent.setMessage(CallbackEvent.NOOSE_ALL_BTN_GONE);
                    EventBus.getDefault().post(callbackEvent);
                }

                getPage().draw(view.canvas, oldRange);
                Rect invalidateRect = new Rect();
                oldRange.roundOut(invalidateRect);
                view.invalidate(invalidateRect);

                if (!newRange.isEmpty()) {
                    getPage().drawSelectedObjects(view.canvas, newRange);
                    newRange.roundOut(invalidateRect);
                    view.invalidate(invalidateRect);
                }

            } else if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.POLYGON_NOOSE) {

                Point[] array = polygon.toArray(new Point[polygon.size()]);

                if (array.length < 3) {
                    return;
                }

                getPage().setPolygonSelectedObject(getPage().calculatePolygonSelectedObject(array));

                RectF newRange = getPage().getPolygonSelectedObjectRange().range;
                if (!newRange.isEmpty()) {
                    view.setNowEditedGraphics(newGraphicsObject);
                    ((GraphicsNoose) newGraphicsObject).reSetRang(newRange);
                    getPage().nooseArt.add((GraphicsNoose) newGraphicsObject);
                    getPage().drawSelectedObjects(view.canvas, newRange);
                    Rect invalidateRect = new Rect();
                    newRange.roundOut(invalidateRect);
                    view.invalidate(invalidateRect);

                    callbackEvent.setMessage(CallbackEvent.NOOSE_COPY_AND_DELETE_AND_CUT_BTN_VISIBLE);
                    EventBus.getDefault().post(callbackEvent);
                } else {
                    callbackEvent.setMessage(CallbackEvent.NOOSE_ALL_BTN_GONE);
                    EventBus.getDefault().post(callbackEvent);
                }

                nDrawHelper.NDrawSwitch(false);
                nDrawIsOpen = false;
            } else {
                saveGraphics(newGraphicsObject);
            }
        }

        // if shape is rectangle or oval, redraw shape to square or circle when it is in the restrict range.
        GraphicsControlPoint graphics = activeControlPoint.getGraphics();
        int currentTool = ToolboxConfiguration.getInstance().getCurrentTool();
        if (currentTool == Tool.RECTANGLE || currentTool == Tool.OVAL) {
            final float dr = graphics.controlPointRadius();
            activeControlPoint.restrictShape();
            RectF newBoundingBox = graphics.getBoundingBox();
            newBoundingBox.inset(-dr, -dr);
            bBox.set(newBoundingBox);
            bBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
            getPage().draw(view.canvas, bBox);
            bBox.roundOut(rect);
            view.invalidate(rect);
        }

        if (!isNew) {
            if (currentTool == Tool.NOOSE || currentTool == Tool.POLYGON_NOOSE) {
                ControlPoint center = graphics.controlPoints.getLast();
                float offsetX = center.screenX() - lastCenterScreenX;
                float offsetY = center.screenY() - lastCenterScreenY;
                getPage().moveSelectedObjects(offsetX, offsetY);
                getPage().getPolygonSelectedObjectRange().range.offset(offsetX, offsetY);
                RectF redrawRectF = new RectF(getPage().getPolygonSelectedObjectRange().range);
                Rect invalidateRect = new Rect();
                redrawRectF.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
                redrawRectF.roundOut(invalidateRect);
                getPage().draw(view.canvas, redrawRectF);
                view.invalidate(invalidateRect);

                view.modifyGraphicsList(getPage().get_mSelectedObjects().getAllSelectedGraphics());
            } else {
                view.modifyGraphics(graphics);
            }
        }

        newGraphicsObject = null;
    }

    private boolean checkGraphicsImageSizeAndRedraw(float deltaX, float deltaY) {
        RectF drawingBoundingBox;
        Rect roundOut = new Rect();

        if (deltaX > GraphicsImage.MIN_IMAGE_SIZE && deltaY > GraphicsImage.MIN_IMAGE_SIZE) {
            if (deltaX < GraphicsImage.DEFAULT_IMAGE_SIZE)
                ((GraphicsImage) newGraphicsObject).resizeGraphics(GraphicsImage.DEFAULT_IMAGE_SIZE, -1);

            if (deltaY < GraphicsImage.DEFAULT_IMAGE_SIZE)
                ((GraphicsImage) newGraphicsObject).resizeGraphics(-1, GraphicsImage.DEFAULT_IMAGE_SIZE);

            drawingBoundingBox = newGraphicsObject.getBoundingBox();
            drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
            drawingBoundingBox.round(roundOut);
            getPage().draw(view.canvas, drawingBoundingBox);
            newGraphicsObject.draw(view.canvas);
            view.invalidate(roundOut);
            return true;
        } else {
            drawingBoundingBox = newGraphicsObject.getBoundingBox();
            drawingBoundingBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
            getPage().draw(view.canvas, drawingBoundingBox);
            return false;
        }
    }

    private void abortMotion() {
        penID = fingerId1 = fingerId2 = -1;
        newGraphicsObject = null;
        activeControlPoint = null;
        getPage().touch();
    }

    @Override
    protected void draw(Canvas canvas, Bitmap bitmap) {
        if (fingerId2 != -1) {
            drawPinchZoomPreview(canvas, bitmap, oldX1, newX1, oldX2, newX2, oldY1, newY1, oldY2, newY2);
        } else {
            canvas.drawBitmap(bitmap, 0, 0, null);
            drawControlPoints(canvas);
        }
    }

    protected void drawControlPoints(Canvas canvas) {
        for (GraphicsControlPoint graphics : getGraphicsObjects()) {
            graphics.drawControlPoints(canvas);
        }
        // If to do invalidate here, the full refresh will not work.
        // view.invalidate();
    }

    /**
     * @return all graphics objects of the given type (e.g. all images) on the page
     */
    protected abstract LinkedList<? extends GraphicsControlPoint> getGraphicsObjects();

    /**
     * Create a new graphics object
     *
     * @param x        initial x position
     * @param y        initial y position
     * @param pressure initial pressure
     * @return a new object derived from GraphicsControlPoint
     */
    protected abstract GraphicsControlPoint newGraphics(float x, float y, float pressure);

    /**
     * Save the graphics object to the current page
     */
    protected void saveGraphics(GraphicsControlPoint graphics) {
        view.saveGraphics(graphics);
    }

    private final RectF bBox = new RectF();
    private final Rect rect = new Rect();

    private void drawOutline(float newX, float newY) {
        GraphicsControlPoint graphics = activeControlPoint.getGraphics();

        //Dylan : Polygon select not draw control point when selecting
        if (ToolboxConfiguration.getInstance().getCurrentTool() == Tool.POLYGON_NOOSE
                && !graphics.isCenterControlPoint(activeControlPoint)) {
            return;
        }

        RectF oldBoundingBox = graphics.getBoundingBox();
        oldBoundingBox.inset(-10, -10);

        /**
         * 1. activeControlPoint is center. (MOVE)
         *      the region of moving is inside the bounding of view.
         *
         * 2. activeControlPoint is corner of image (RESIZE)
         *      keep the image ratio, let the (newX, newY) be inside the bounding of view.
         *
         * 3. activeControlPoint is corner of graphics
         *      let the (newX, newY) be inside the bounding of view.
         */
        if (graphics.isCenterControlPoint(activeControlPoint)) {
            float halfWidth = (float) graphics.getOutLineBounding().width() / 2;
            float halfHeight = (float) graphics.getOutLineBounding().height() / 2;
            int drawRegion_left = view.getLeft() + MOVE_BOUNDING_GAP;
            int drawRegion_top = view.getTop() + MOVE_BOUNDING_GAP;
            int drawRegion_right = view.getRight() - MOVE_BOUNDING_GAP;
            int drawRegion_bottom = view.getBottom() - MOVE_BOUNDING_GAP;

            if (graphics.getOutLineBounding().width() < drawRegion_right - drawRegion_left)
                newX = newX - halfWidth < drawRegion_left ? drawRegion_left + halfWidth : (newX + halfWidth > drawRegion_right ? drawRegion_right - halfWidth : newX);
            else {
                // if only if graphics is TextBox and the width of TextBox is over the region
                newX = newX - halfWidth < drawRegion_left ? drawRegion_left + halfWidth : newX;
            }

            if (graphics.getOutLineBounding().height() < drawRegion_bottom - drawRegion_top) {
                if (graphics instanceof TextBox)
                    newY = newY < drawRegion_top ? drawRegion_top : (newY + graphics.getOutLineBounding().height() > drawRegion_bottom ? drawRegion_bottom - graphics.getOutLineBounding().height() : newY);
                else
                    newY = newY - halfHeight < drawRegion_top ? drawRegion_top + halfHeight : (newY + halfHeight > drawRegion_bottom ? drawRegion_bottom - halfHeight : newY);
            } else {
                // if only if graphics is TextBox and the height of TextBox is over the region
                newY = newY < drawRegion_top ? drawRegion_top : newY;
            }
        } else if (newGraphicsObject == null
                && graphics instanceof GraphicsImage) {

            int drawRegion_left = view.getLeft();
            int drawRegion_top = view.getTop();
            int drawRegion_right = view.getRight();
            int drawRegion_bottom = view.getBottom();

            float[] newXY = ((GraphicsImage) graphics).getNewXyInsideRegionKeepImageRatio(activeControlPoint, newX, newY, drawRegion_left, drawRegion_top, drawRegion_right, drawRegion_bottom);
            newX = newXY[0];
            newY = newXY[1];
        } else {
            int gap = graphics.getBoundingGap();
            int drawRegion_left = view.getLeft() + gap;
            int drawRegion_top = view.getTop() + gap;
            int drawRegion_right = view.getRight() - gap;
            int drawRegion_bottom = view.getBottom() - gap;
            newX = newX < drawRegion_left ? drawRegion_left : (newX > drawRegion_right ? drawRegion_right : newX);
            newY = newY < drawRegion_top ? drawRegion_top : (newY > drawRegion_bottom ? drawRegion_bottom : newY);
        }

        activeControlPoint.move(newX, newY);
        RectF newBoundingBox = graphics.getBoundingBox();
        final float dr = graphics.controlPointRadius();
        if (graphics instanceof GraphicsNoose) {
            newBoundingBox.inset(-10, -10);
            if (!getPage().isSelectedObjectsEmpty()) {
                ControlPoint center = graphics.controlPoints.getLast();
                float offsetX = center.screenX() - lastCenterScreenX;
                float offsetY = center.screenY() - lastCenterScreenY;
                lastCenterScreenX = center.screenX();
                lastCenterScreenY = center.screenY();
                getPage().moveSelectedObjects(offsetX, offsetY);
            }
        } else {
            newBoundingBox.inset(-dr, -dr);
        }
        bBox.union(oldBoundingBox);
        bBox.union(newBoundingBox);
        bBox.inset(PLUS_REDRAW_RANGE, PLUS_REDRAW_RANGE);
        bBox.roundOut(rect);
        /**
         * Resize/move GraphicsImage: just draw InsertImageView
         */
        if (newGraphicsObject == null && graphics instanceof GraphicsImage) {
            getPage().drawImage(InsertImageView.canvas, bBox);
            InsertImageView.doInvalidate(rect);
            return;
        }

        getPage().draw(view.canvas, bBox);
        graphics.drawAssistLine(view.canvas);
        if (newGraphicsObject != null) {
            if (newGraphicsObject instanceof TextBox)
                newGraphicsObject.drawOutLine(view.canvas);
            else
                newGraphicsObject.draw(view.canvas);
        }
        view.invalidate(rect);
        bBox.set(newBoundingBox);
    }

    /**
     * Maximal distance to select control point (measured in dp)
     */
    protected float maxDistanceControlPointScreen() {
        /**
         * 2019.09.25 Karote modify.
         */
        //return 15f;
        return 25f;
    }

    /**
     * Maximal distance to select control point (in page coordinate units)
     *
     * @return
     */
    protected float maxDistanceControlPoint() {
        final Transformation transform = getPage().getTransform();
        return maxDistanceControlPointScreen() * view.screenDensity / transform.scale;
    }

    /**
     * Find the closest control point to a given screen position
     *
     * @param xScreen X screen coordinate
     * @param yScreen Y screen coordinate
     * @return The closest ControlPoint or null if there is none within MAX_DISTANCE
     */
    protected ControlPoint findControlPoint(float xScreen, float yScreen) {
        final Transformation transform = getPage().getTransform();
        final float x = transform.inverseX(xScreen);
        final float y = transform.inverseY(yScreen);
        final float rMax = maxDistanceControlPoint();

        float rMin2 = rMax * rMax;
        ControlPoint closest = null;
        for (GraphicsControlPoint graphics : getGraphicsObjects())
            for (GraphicsControlPoint.ControlPoint p : graphics.controlPoints) {
                final float dx = x - p.x;
                final float dy = y - p.y;
                final float r2 = dx * dx + dy * dy;
                if (r2 < rMin2) {
                    rMin2 = r2;
                    closest = p;
                }
            }
        return closest;
    }

    /**
     * Is a given screen position in bound
     *
     * @param xScreen X screen coordinate
     * @param yScreen Y screen coordinate
     * @return is a given screen position in image bound
     */
    private GraphicsControlPoint isPointInBound(float xScreen, float yScreen) {

        GraphicsControlPoint inBoundGraphics = null;
        for (int i = getGraphicsObjects().size() - 1; i >= 0; i--) {
            inBoundGraphics = getGraphicsObjects().get(i);

            RectF graphicsBoundingBox = inBoundGraphics.getBoundingBox();

            if ((graphicsBoundingBox.left < xScreen) && (xScreen < graphicsBoundingBox.right)
                    && (graphicsBoundingBox.top < yScreen) && (yScreen < graphicsBoundingBox.bottom)) {
                return inBoundGraphics;
            }
        }
        return null;
    }

}
