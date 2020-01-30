package name.vbraun.view.write;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import name.vbraun.filepicker.AsyncTaskResult;
import ntx.note.Global;
import ntx.note.data.Book;

public class GetMainPagePreviewBitmapAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private Book mBook;
    private String thumbnailPath;

    public AsyncTaskResult<Bitmap> asyncTaskResult;

    public GetMainPagePreviewBitmapAsyncTask(UUID bookUuid, int pageIndex) {
        this.mBook = new Book(bookUuid, pageIndex, 1);
        this.thumbnailPath = Global.APP_DATA_PACKAGE_FILES_PATH + Global.NOTEBOOK_DIRECTORY_PREFIX + mBook.getUUID() + "/" + Global.THUMBNAIL + mBook.getPage(0).getUUID();
    }

    @Override
    protected Bitmap doInBackground(Integer... integers) {
        int width = integers[0];
        int height = integers[1];

        Page page = mBook.getPage(0);

        File thumbnail_ = new File(thumbnailPath);

        if (thumbnail_.exists()) {
            return BitmapFactory.decodeFile(thumbnailPath);
        } else {
            return renderPageBitmap(page, width, height, page.getAspectRatio());
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (asyncTaskResult != null)
            asyncTaskResult.taskFinish(bitmap);

        this.mBook = null;
        this.thumbnailPath = "";
    }

    @Override
    protected void onCancelled() {
        this.mBook = null;
    }

    private Bitmap renderPageBitmap(Page page, int width, int height, float aspect_ratio) {
        float scale = Math.min(height, width / aspect_ratio);
        setTransform(page, 0, 0, scale);
        int actual_width = (int) Math.rint(scale * aspect_ratio);
        int actual_height = (int) Math.rint(scale);

        Bitmap bitmap = Bitmap.createBitmap(actual_width, actual_height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        RectF rectF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());

        page.background.draw(canvas, rectF, page.transformation);

        for (GraphicsImage graphics : page.images) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                graphics.draw(canvas);
        }

        for (Stroke s : page.strokes) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(s.getBoundingBox(), Canvas.EdgeType.AA))
                s.drawThumbnail(canvas);
        }

        for (GraphicsControlPoint graphics : page.lineArt) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                graphics.drawThumbnail(canvas);
        }

        for (GraphicsControlPoint graphics : page.rectangleArt) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                graphics.drawThumbnail(canvas);
        }

        for (GraphicsControlPoint graphics : page.ovalArt) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                graphics.drawThumbnail(canvas);
        }

        for (GraphicsControlPoint graphics : page.triangleArt) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(graphics.getBoundingBox(), Canvas.EdgeType.AA))
                graphics.drawThumbnail(canvas);
        }

        for (TextBox textBox : page.textBoxes) {
            if (isCancelled())
                return null;

            if (!canvas.quickReject(textBox.getBoundingBox(), Canvas.EdgeType.AA))
                textBox.drawThumbnail(canvas);
        }

        saveBitmapFile(bitmap);
        return bitmap;
    }

    private void setTransform(Page page, float dx, float dy, float s) {
        page.transformation.offset_x = dx;
        page.transformation.offset_y = dy;
        page.transformation.scale = s;
        setTransformApply(page);
    }

    private void setTransformApply(Page page) {
        for (Stroke stroke : page.strokes) {
            if (isCancelled())
                return;
            stroke.setTransform(page.getTransform());
        }
        for (GraphicsControlPoint line : page.lineArt) {
            if (isCancelled())
                return;
            line.setTransform(page.getTransform());
        }
        for (GraphicsControlPoint rectangle : page.rectangleArt) {
            if (isCancelled())
                return;
            rectangle.setTransform(page.getTransform());
        }
        for (GraphicsControlPoint oval : page.ovalArt) {
            if (isCancelled())
                return;
            oval.setTransform(page.getTransform());
        }
        for (GraphicsControlPoint triangle : page.triangleArt) {
            if (isCancelled())
                return;
            triangle.setTransform(page.getTransform());
        }
        for (GraphicsImage image : page.images) {
            if (isCancelled())
                return;
            image.setTransform(page.transformation);
        }
        for (TextBox textBox : page.textBoxes) {
            if (isCancelled())
                return;
            textBox.setTransform(page.transformation);
        }
    }

    public void saveBitmapFile(Bitmap bitmap) {
        File file = new File(thumbnailPath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
