package name.vbraun.view.write;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.view.ViewGroup;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

public class BackgroundView extends ViewGroup {

    private static BackgroundView instance;

    private Bitmap bitmap;
    private Canvas canvas;

    public static BackgroundView getInstance(Context context) {
        if (instance == null)
            instance = new BackgroundView(context);

        return instance;
    }

    private BackgroundView(Context context) {
        super(context);

        setFocusable(true);
        setAlwaysDrawnWithCacheEnabled(false);
        setDrawingCacheEnabled(false);
        setWillNotDraw(false);
        setBackgroundDrawable(null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++)
            getChildAt(i).layout(l, t, r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int curW = bitmap != null ? bitmap.getWidth() : 0;
        int curH = bitmap != null ? bitmap.getHeight() : 0;
        if (curW >= w)
            curW = w;
        if (curH >= h)
            curH = h;
        if (curW < w)
            curW = w;
        if (curH < h)
            curH = h;

        Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas();
        newCanvas.setBitmap(newBitmap);
        if (bitmap != null) {
            newCanvas.drawBitmap(bitmap, 0, 0, null);
        }
        bitmap = newBitmap;
        canvas = newCanvas;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null)
            return;
        canvas.drawBitmap(bitmap, 0, 0, null);
    }


    protected void onFinish() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    protected void setPagePaperType(final Page page, Paper.Type paper_type) {
        if (paper_type == Paper.Type.DOT_SQUARE_GRID) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    page.drawBg(canvas);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                }
            });
        } else {
            page.drawBg(canvas);
            invalidate();
        }
    }

    protected void drawPageBg(Page new_page) {
        if (new_page == null)
            return;

        if (canvas == null)
            return;

        new_page.drawBg(canvas);
    }
}
