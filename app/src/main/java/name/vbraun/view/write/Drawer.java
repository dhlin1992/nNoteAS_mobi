package name.vbraun.view.write;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import ntx.note.CallbackEvent;
import ntx.note.Global;

import static java.lang.Thread.sleep;

public class Drawer {

    private static Drawer mInstance;

    private DrawThread mDrawThread;

    private Page mPage;

    private EventBus mEventBus;
    private Handler mIntervalInvalidateDrawHandler;

    private Runnable intervalInvalidateDrawRunnable = new Runnable() {
        @Override
        public void run() {
            CallbackEvent event = new CallbackEvent();
            event.setMessage(CallbackEvent.DO_DRAW_VIEW_INVALIDATE);
            mEventBus.post(event);
            mIntervalInvalidateDrawHandler.postDelayed(this, 500);
        }
    };

    public static Drawer getInstance() {
        if (mInstance == null)
            mInstance = new Drawer();
        return mInstance;
    }

    private Drawer() {
        mEventBus = EventBus.getDefault();
        mIntervalInvalidateDrawHandler = new Handler();
    }


    public void drawPage(Page page, Canvas canvas, boolean doInvalidate) {
        if (mDrawThread != null) {
            mDrawThread.interrupt();
            mDrawThread = null;

            try {
                sleep(100);
            } catch (InterruptedException e) {

            }
        }

        mPage = page;

        mDrawThread = new DrawThread(canvas, doInvalidate);
        mDrawThread.start();
    }

    private class DrawThread extends Thread {
        Canvas canvas;
        boolean doInvalidate;

        public DrawThread(Canvas _canvas, boolean _doInvalidate) {
            this.canvas = _canvas;
            this.doInvalidate = _doInvalidate;
        }

        @Override
        public void run() {
            mPage.isCanvasDrawCompleted = false;

            Paint clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(clearPaint);

            if (doInvalidate)
                mIntervalInvalidateDrawHandler.postDelayed(intervalInvalidateDrawRunnable, 500);

            for (Stroke s : Collections.unmodifiableList(new ArrayList<>(mPage.strokes))) {
                Global.checkNeedRefresh(s.getStrokeColor());
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(s.getBoundingBox(), Canvas.EdgeType.AA))
                        s.draw(canvas);
                } else
                    return;
            }

            for (GraphicsControlPoint graphics : Collections.unmodifiableList(new ArrayList<>(mPage.lineArt))) {
                Global.checkNeedRefresh(graphics.getGraphicsColor());
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                        graphics.draw(canvas);
                } else
                    return;
            }

            for (GraphicsControlPoint graphics : Collections.unmodifiableList(new ArrayList<>(mPage.rectangleArt))) {
                Global.checkNeedRefresh(graphics.getGraphicsColor());
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                        graphics.draw(canvas);
                } else
                    return;
            }

            for (GraphicsControlPoint graphics : Collections.unmodifiableList(new ArrayList<>(mPage.ovalArt))) {
                Global.checkNeedRefresh(graphics.getGraphicsColor());
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                        graphics.draw(canvas);
                } else
                    return;
            }

            for (GraphicsControlPoint graphics : Collections.unmodifiableList(new ArrayList<>(mPage.triangleArt))) {
                Global.checkNeedRefresh(graphics.getGraphicsColor());
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                        graphics.draw(canvas);
                } else
                    return;
            }

            for (GraphicsControlPoint graphics : Collections.unmodifiableList(new ArrayList<>(mPage.nooseArt))) {
                Global.checkNeedRefresh(graphics.getGraphicsColor());
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                        graphics.draw(canvas);
                } else
                    return;
            }

            for (TextBox textBox : Collections.unmodifiableList(new ArrayList<>(mPage.textBoxes))) {
                if (!currentThread().isInterrupted()) {
                    if (!canvas.quickReject(textBox.getBoundingBox(), Canvas.EdgeType.AA))
                        textBox.draw(canvas);
                } else
                    return;
            }

            mPage.isCanvasDrawCompleted = true;

            if (doInvalidate) {
                mIntervalInvalidateDrawHandler.removeCallbacks(intervalInvalidateDrawRunnable);
            }

            CallbackEvent event = new CallbackEvent();
            event.setMessage(CallbackEvent.PAGE_DRAW_COMPLETED);
            mEventBus.post(event);
        }
    }
}
