package name.vbraun.view.write;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

import name.vbraun.lib.pen.Hardware;
import ntx.note.Global;
import ntx.note.artist.Artist;
import ntx.note.artist.LineStyle;


public class Background {
    public static final String TAG = "Background";
    private static final float INCH_in_CM = 2.54f;
    private static final float INCH_in_MM = INCH_in_CM * 10;

    private static final float marginMm = 5;

    private static final float LEGALRULED_SPACING = 10.0f;
    private static final float COLLEGERULED_SPACING = 10.0f;
    private static final float NARROWRULED_SPACING = 6.4f;
    private static final float TODOLIST_SPACING = 10.0f;
    private static final float STAVE_SPACING = 11.0f; //8.0f;
    private static final float STAVE_MARGIN = 8.0f; //20.0f;
    private final float portraitScale = 1340f;
    private final float landscapeScale = 1023.75f;

//    public static boolean flag_zoom = false;

    private Paper.Type paperType = Paper.Type.EMPTY;
    private String paperPath = "na";
    private AspectRatio aspectRatio = AspectRatio.Table[0];
    private float heightMm, widthMm;
    private int centerWidth, centerHeight = 0;
    private Bitmap bitmap = null;

    private final RectF paper = new RectF();
    private final Paint paint = new Paint();

    private int CALLIGRAPHY_TYPE_SMALL = 0;
    private int CALLIGRAPHY_TYPE_BIG = 1;

    private int shade = Color.BLACK;
    private float threshold = 1500;

    public void setPaperType(Paper.Type paper) {
        paperType = paper;
        paint.setStrokeCap(Cap.BUTT);
    }

    public Paper.Type getPaperType() {
        return paperType;
    }

    public void setPaperPath(String paper_path) {
        paperPath = paper_path;
    }

    public void setAspectRatio(float aspect) {
        aspectRatio = AspectRatio.closestMatch(aspect);
        heightMm = aspectRatio.guessHeightMm();
        widthMm = aspectRatio.guessWidthMm();
    }

    private int paperColour = Color.WHITE;

    public int getPaperColour() {
        return paperColour;
    }

    public void setPaperColour(int paperColour) {
        this.paperColour = paperColour;
    }

    private void drawGreyFrame(Canvas canvas, RectF bBox, Transformation t) {
        paper.set(t.offset_x, t.offset_y,
                t.offset_x + aspectRatio.ratio * t.scale, t.offset_y + t.scale);
        if (!paper.contains(bBox))
            canvas.drawARGB(0xff, 0xaa, 0xaa, 0xaa);
    }

    private void drawEmptyFrame(Canvas canvas, RectF bBox, Transformation t) {
        paper.set(t.offset_x, t.offset_y,
                t.offset_x + aspectRatio.ratio * t.scale, t.offset_y + t.scale);
        if (!paper.contains(bBox))
            canvas.drawARGB(Color.alpha(paperColour),
                    Color.red(paperColour) ^ 0xff, Color.green(paperColour) ^ 0xff, Color.blue(paperColour) ^ 0xff);
    }

    /**
     * Artis: For EPD, set the black frame to cover and hide the out boundary strokes
     *
     * @param canvas
     * @param bBox
     * @param t
     */
    private void drawBlackFrame(Canvas canvas, RectF bBox, Transformation t) {
        paper.set(t.offset_x, t.offset_y,
                t.offset_x + aspectRatio.ratio * t.scale, t.offset_y + t.scale);
        if (!paper.contains(bBox))
            canvas.drawARGB(0xff, 0x00, 0x00, 0x00);
    }


    /**
     * This is where we clear the (possibly uninitialized) backing bitmap in the canvas.
     * The background is filled with white, which is most suitable for printing.
     *
     * @param canvas The canvas to draw on
     * @param bBox   The damage area
     * @param t      The linear transformation from paper to screen
     */
    public void drawWhiteBackground(Canvas canvas, RectF bBox, Transformation t) {
//		drawGreyFrame(canvas, bBox, t);		// original
        drawBlackFrame(canvas, bBox, t);    // Artis: for EPD
        paint.setARGB(0xff, 0xff, 0xff, 0xff);
        paint.setColor(Color.BLACK);
        canvas.drawRect(paper, paint);
    }

    /**
     * This is where we clear the (possibly uninitialized) backing bitmap in the canvas.
     *
     * @param canvas The canvas to draw on
     * @param bBox   The damage area
     * @param t      The linear transformation from paper to screen
     */
    public void drawEmptyBackground(Canvas canvas, RectF bBox, Transformation t) {
//		drawGreyFrame(canvas, bBox, t);		// original
//		drawBlackFrame(canvas, bBox, t);	// Artis: for EPD
        drawEmptyFrame(canvas, bBox, t);
        paint.setColor(paperColour);
        canvas.drawRect(paper, paint);
    }

    public void drawTransparentBackground(Canvas canvas, RectF bBox, Transformation t) {
        drawEmptyFrame(canvas, bBox, t);
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(paper, paint);
    }

    public void draw(Canvas canvas, RectF bBox, Transformation t) {
        drawEmptyBackground(canvas, bBox, t);
        switch (paperType) {
            case EMPTY:
                return;
            case RULED:
                draw_ruled(canvas, t, LEGALRULED_SPACING, 31.75f);
                return;
            case COLLEGERULED:
                draw_ruled(canvas, t, COLLEGERULED_SPACING, 25.0f);
                return;
            case NARROWRULED:
                draw_ruled(canvas, t, NARROWRULED_SPACING, 25.0f);
                return;
            case QUAD:
                draw_quad(canvas, t);
                return;
            case CORNELLNOTES:
                draw_cornellnotes(canvas, t);
                return;
            case DAYPLANNER:
                draw_dayplanner(canvas, t, Calendar.getInstance());
                return;
            case MUSIC:
                draw_music_manuscript(canvas, t);
                return;
            case CALLIGRAPHY_SMALL:
                draw_calligraphy(canvas, t, CALLIGRAPHY_TYPE_SMALL);
                return;
            case CALLIGRAPHY_BIG:
                draw_calligraphy(canvas, t, CALLIGRAPHY_TYPE_BIG);
                return;
            case TODOLIST:
                draw_todolist(canvas, t, TODOLIST_SPACING, 20.0f);
                return;
            case MINUTES:
                draw_minutes(canvas, t, TODOLIST_SPACING, 20.0f);
                return;
            case STAVE:
                draw_stave(canvas, t, STAVE_SPACING, STAVE_MARGIN);
                return;
            case DIARY:
                draw_diary(canvas, t, TODOLIST_SPACING, 20.0f);
                return;
            case HEX:
                // TODO
                return;
            case CUSTOMIZED:
                draw_customized(canvas, bBox);
                return;
            case DOT_RULED:
                draw_dot_ruled(canvas, t, COLLEGERULED_SPACING);
                return;
            case DOT_SQUARE_GRID:
                draw_dot_square_grid(canvas, t);
                return;
            case DOT_MATRIX:
                draw_dot_matrix(canvas, t);
                return;
            case DOT_COLLEGE:
                draw_dot_college(canvas, t, COLLEGERULED_SPACING);
                return;
            case TODO_DOT_LINE:
                if (aspectRatio.isPortrait())
                    draw_todo_dot_line(canvas, t, COLLEGERULED_SPACING);
                else
                    draw_todo_two_column_dot_line(canvas, t, COLLEGERULED_SPACING);
                return;
            case CENTRAL_CROSS:
                draw_central_cross(canvas, t);
                return;
            case SUBJECT_HEADLINE:
                if (aspectRatio.isPortrait())
                    draw_subject_headline_portrait(canvas, t);
                else
                    draw_subject_headline_landscape(canvas, t);
                return;
            case YEARLY_PLANNER:
                draw_yearly_planner(canvas, t);
                return;
            case MONTHLY_PLANNER:
                if (aspectRatio.isPortrait())
                    draw_monthly_planner_vertical(canvas, t);
                else
                    draw_monthly_planner_horizontal(canvas, t);
                return;
            case WEEKLY_PLANNER:
                if (aspectRatio.isPortrait())
                    draw_weekly_planner_vertical(canvas, t);
                else
                    draw_weekly_planner_horizontal(canvas, t);
                return;
            case DAILY_PLANNER:
                if (aspectRatio.isPortrait())
                    draw_daily_planner(canvas, t, COLLEGERULED_SPACING);
                else
                    draw_daily_planner_two_column(canvas, t, COLLEGERULED_SPACING);
                return;
        }
    }

    public void drawPNG(Canvas canvas, RectF bBox, Transformation t) {
        // the paper is 1 high and aspect_ratio wide
        drawEmptyBackground(canvas, bBox, t);

        drawPNG_customized(canvas, bBox);

    }

    private void draw_dayplanner(Canvas c, Transformation t, Calendar calendar) {
        float x0, x1, y, y0, y1;
        float textHeight;
        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        //paint.setARGB(0xff, shade, shade, shade);
        paint.setStrokeWidth(0);
        paint.setColor(Color.BLACK);

        Typeface font = Typeface.create(Typeface.SERIF, Typeface.BOLD);
        paint.setTypeface(font);
        paint.setAntiAlias(true);

        // Header
        float headerHeightMm = 30f;
        x0 = t.applyX(marginMm / heightMm);
        x1 = t.applyX((widthMm - marginMm) / heightMm);
        y = t.applyY(headerHeightMm / heightMm);
        c.drawLine(x0, y, x1, y, paint);

        textHeight = t.scaleText(24f);
        paint.setTextSize(textHeight);
        y = t.applyY(marginMm / heightMm) + textHeight;
        c.drawText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), x0, y, paint);

// I'm leaving this out for now; Should there be a gui to pick the day of the year? Or just let the user write the date?
//		y0 = t.applyY((widthMm-marginMm)/widthMm);
//		c.drawText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), x0, y0, paint);
//
//		paint.setTextSize(t.scaleText(12f));
//
//		c.drawText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()), x0 + t.applyX(2*marginMm/heightMm), y0 + t.applyY(marginMm/heightMm), paint);
//
//		paint.setTextSize(t.scaleText(10f));
//		font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
//		paint.setTextAlign(Align.RIGHT);
//		c.drawText("Week " + calendar.get(Calendar.WEEK_OF_YEAR),t.applyX((widthMm-marginMm)/heightMm), t.applyY((float) (marginMm*1.75/widthMm)), paint);

        // Details
        paint.setTextAlign(Align.LEFT);
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        float spacingMm = COLLEGERULED_SPACING;
        int n = (int) Math.floor((heightMm - headerHeightMm - marginMm) / spacingMm);

        x0 = t.applyX(marginMm / heightMm);
        x1 = t.applyX((widthMm - marginMm) / heightMm);

        int hourMarker = 7;
        textHeight = t.scaleText(10f);
        paint.setTextSize(textHeight);

        for (int i = 1; i <= n; i++) {
            y = t.applyY((headerHeightMm + i * spacingMm) / heightMm);
            c.drawLine(x0, y, x1, y, paint);

            if (i % 2 == 1) {
                y = t.applyY((headerHeightMm + (i - 0.5f) * spacingMm) / heightMm) + textHeight / 2;
                c.drawText(hourMarker + ":", x0, y, paint);

                hourMarker++;
                if (hourMarker == 13)
                    hourMarker = 1;
            }

        }
    }

    private void draw_cornellnotes(Canvas c, Transformation t) {

        float x0, x1, y0, y1;
        final float MARGIN = 1.25f;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);


        // Details
        float spacingMm = COLLEGERULED_SPACING;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        int n = (int) Math.floor((heightMm - (MARGIN * INCH_in_MM) - 2 * marginMm) / spacingMm);

        x0 = t.applyX((MARGIN * INCH_in_MM) / widthMm + marginMm / heightMm);
        x1 = t.applyX((widthMm - marginMm) / heightMm);

        for (int i = 1; i <= n - 3; i++) {
            float y = t.applyY(((heightMm - n * spacingMm - MARGIN * INCH_in_MM) + i * spacingMm) / heightMm);
            c.drawLine(x0, y, x1, y, paint);
        }

        // Cue Column
        x0 = t.applyX((MARGIN * INCH_in_MM) / widthMm);
        x1 = x0;
        y0 = t.applyY(0);
        y1 = t.applyY((heightMm - spacingMm * 2 - (MARGIN * INCH_in_MM)) / heightMm);

        c.drawLine(x0, y0, x1, y1, paint);

        // Summary area at base of page
        x0 = t.applyX(0);
        x1 = t.applyX(widthMm / heightMm);
        y0 = t.applyY((heightMm - spacingMm * 2 - (MARGIN * INCH_in_MM)) / heightMm);
        y1 = y0;

        c.drawLine(x0, y0, x1, y1, paint);

    }


    private void draw_ruled(Canvas c, Transformation t, float lineSpacing, float margin) {

        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        float vertLineMm = margin;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);


        if (lineSpacing == COLLEGERULED_SPACING) {

            for (int i = 0; i <= n; i++) {
                float y = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
                c.drawLine(x0, y, x1, y, paint);
            }

        } else if (lineSpacing == NARROWRULED_SPACING) {

            for (int i = 1; i <= n - 1; i++) {
                float y = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
                c.drawLine(x0, y, x1, y, paint);
            }

        } else {

            for (int i = 1; i <= n - 2; i++) {
                float y = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
                c.drawLine(x0, y, x1, y, paint);
            }

        }

        // Paint margin
        if (margin > 0.0f) {
            paint.setARGB(0xff, shade, shade, shade);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            float y0 = t.applyY(marginMm / heightMm);
            float y1 = 0;

            if (lineSpacing == COLLEGERULED_SPACING) {

                y1 = t.applyY((heightMm - marginMm - spacingMm) / heightMm);

            } else if (lineSpacing == NARROWRULED_SPACING) {

                y1 = t.applyY((heightMm - marginMm - spacingMm * 1.5f) / heightMm);

            } else {

                y1 = t.applyY((heightMm - marginMm - spacingMm * 2) / heightMm);

            }

            float x = t.applyX(vertLineMm / widthMm);
            c.drawLine(x, y0, x, y1, paint);
        }
    }

    private void draw_dot_ruled(Canvas c, Transformation t, float lineSpacing) {

        float spacingMm = lineSpacing + 0.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);
        int x0i = Math.round(x0) + 2;
        int x1i = Math.round(x1) - 6;
        for (int i = 0; i <= n; i++) {
            float y = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm) + 30;
            for (int j = x0i; j < x1i; j++) {
                if (j % 10 == 0) {
                    c.drawCircle(j, y, 2, paint);
                }
            }
        }
    }

    private void draw_quad(Canvas c, Transformation t) {
        float spacingMm = 10f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        int nx, ny;
        float x, x0, x1, y, y0, y1;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        ny = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm);
        nx = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm);
        float marginXMm = (widthMm - nx * spacingMm) / 2;
        float marginYMm = (heightMm - ny * spacingMm) / 2;
        x0 = t.applyX(marginXMm / heightMm);
        x1 = t.applyX((widthMm - marginXMm) / heightMm);
        y0 = t.applyY(marginYMm / heightMm);
        y1 = t.applyY((heightMm - marginYMm - spacingMm) / heightMm);
        for (int i = 0; i < ny; i++) {
            y = t.applyY((marginYMm + i * spacingMm) / heightMm);
            c.drawLine(x0, y, x1, y, paint);
        }
        for (int i = 0; i <= nx; i++) {
            x = t.applyX((marginXMm + i * spacingMm) / heightMm);
            c.drawLine(x, y0, x, y1, paint);
        }
    }

    private void draw_dot_square_grid(Canvas c, Transformation t) {
        float spacingMm = 6.4f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        int nx, ny;
        float x, x1, y, y1;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setPathEffect(new DashPathEffect(new float[]{2, 4}, 0));
        ny = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm);
        nx = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm);
        x1 = t.applyX(widthMm);
        y1 = t.applyY(heightMm);
        float marginXMm = ((widthMm - nx * spacingMm) / 2);
        float marginYMm = ((heightMm - ny * spacingMm) / 2) + 2f;

        for (int i = 0; i < ny + 1; i++) {
            y = t.applyY((marginYMm + i * spacingMm) / heightMm);
            c.drawLine(0, y, x1, y, paint);
        }

        for (int i = 0; i <= nx; i++) {
            x = t.applyX((marginXMm + i * spacingMm) / heightMm);
            c.drawLine(x, 0, x, y1, paint);
        }
    }

    private void draw_dot_matrix(Canvas c, Transformation t) {
        float spacingMm = 5.8f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        int ny;
        float x0, x1, y;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4);
        ny = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm);
        float marginXMm = ((heightMm - ny * spacingMm) / 2) + 2.5f;
        float marginYMm = ((heightMm - ny * spacingMm) / 2) + 3f;
        x0 = t.applyX(marginXMm / heightMm);
        x1 = t.applyX((widthMm - marginXMm) / heightMm);

        float dash = t.applyY((marginYMm + 2 * spacingMm) / heightMm) - t.applyY((marginYMm + 1 * spacingMm) / heightMm);
        paint.setPathEffect(new DashPathEffect(new float[]{4, dash}, 0));
        for (int i = 0; i < ny + 1; i++) {
            y = t.applyY((marginYMm + i * spacingMm) / heightMm);
            c.drawLine(x0, y, x1, y, paint);
        }
    }

    private void draw_calligraphy(Canvas c, Transformation t, int type) {
        int positionBufferNum = 0;// base on the max value of ny and nx
        float spacingMmBase = 10f; // base on quad paper
        float spacingMm = 0;

        //calculate spacing according to type
        if (type == CALLIGRAPHY_TYPE_SMALL)
            spacingMm = (float) 6 * spacingMmBase;
        else
            // CALLIGRAPHY_TYPE_BIG
            spacingMm = (float) 8 * spacingMmBase;

		/*
		if (Hardware.isEink6InchHardwareType()) {
			spacingMm = (float) (spacingMm * 1.7);
		}
		*/

        int nx, ny;
        float x, x0, x1, y, y0, y1;
        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        //paint.setStrokeWidth(6);
        paint.setStrokeWidth(2);
        ny = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm);
        nx = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm);
        float marginXMm = (widthMm - nx * spacingMm) / 2;
        float marginYMm = (heightMm - ny * spacingMm) / 2;
        x0 = t.applyX(marginXMm / heightMm);
        x1 = t.applyX((widthMm - marginXMm) / heightMm);
        y0 = t.applyY((marginYMm * 2 / 3) / heightMm);
        y1 = t.applyY((heightMm - marginYMm * 4 / 3) / heightMm);

        if (nx >= ny)
            positionBufferNum = nx + 1;
        else
            positionBufferNum = ny + 1;

        //for recording x, y positions
        float[] position_x = new float[positionBufferNum];
        float[] position_y = new float[positionBufferNum];

        //draw frame line
        //draw "-" line
        for (int i = 0; i <= ny; i++) {
            y = t.applyY((marginYMm * 2 / 3 + i * spacingMm) / heightMm);
            c.drawLine(x0, y, x1, y, paint);
            position_y[i] = y;
        }
        //draw "|" line
        for (int i = 0; i <= nx; i++) {
            x = t.applyX((marginXMm + i * spacingMm) / heightMm);
            c.drawLine(x, y0, x, y1, paint);
            position_x[i] = x;
        }

        //debug position info
		/*
		for (int i=0; i<=nx; i++) {
			ALog.debug("position_x["+i+"]:" +position_x[i]);
		}
		for (int i=0; i<=ny; i++) {
			ALog.debug("position_y["+i+"]:" +position_y[i]);
		}
		*/

        //draw "x" and '+' line
        //paint.setStrokeWidth(2);
        for (int i = 0; i < ny; i++) {
            for (int j = 0; j < nx; j++) {
                // draw "\" line
                c.drawLine(position_x[j], position_y[i], position_x[j + 1], position_y[i + 1], paint);
                // draw "|" line
                c.drawLine((position_x[j] + position_x[j + 1]) / 2, position_y[i],
                        (position_x[j] + position_x[j + 1]) / 2, position_y[ny], paint);
                // draw "/" line
                c.drawLine(position_x[j + 1], position_y[i], position_x[j], position_y[i + 1], paint);
                // draw "-" line
                c.drawLine(position_x[j], (position_y[i] + position_y[i + 1]) / 2, position_x[nx],
                        (position_y[i] + position_y[i + 1]) / 2, paint);
            }
        }
    }

    private void draw_music_manuscript(Canvas c, Transformation t) {
        float lineSpacingMm = 2.5f;
        float staveHeight = 4 * lineSpacingMm;
        float staffTopMarginMm = 25.0f;
        float staffBottomMarginMm = 15.0f;
        float staffSideMarginMm = 15.0f;
        int staveCount;
        if (aspectRatio.isPortrait())
            staveCount = 12;
        else
            staveCount = 8;
        float staffTotal = staffTopMarginMm + staffBottomMarginMm + staveCount * staveHeight;
        float staffSpacing = staveHeight + (heightMm - staffTotal) / (staveCount - 1);

        float x0, x1, y;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(0);

        x0 = t.applyX(staffSideMarginMm / heightMm);
        x1 = t.applyX((widthMm - staffSideMarginMm) / heightMm);

        for (int i = 0; i < staveCount; i++) {
            for (int j = 0; j < 5; j++) {
                y = t.applyY((staffTopMarginMm + i * staffSpacing + j * lineSpacingMm) / heightMm);
                c.drawLine(x0, y, x1, y, paint);
            }
        }
    }

    private void draw_todolist(Canvas c, Transformation t, float lineSpacing, float margin) {

        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        float vertLineMm = margin;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);
        float y0 = t.applyY(((heightMm - n * spacingMm) / 2) / heightMm);
        float y1 = t.applyY(((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm);
        float x2, x3, y2, y3;

        float checkboxmargin = (y1 - y0) / 8;
        float checkboxspaceing = checkboxmargin * 6;
        for (int i = 1; i <= n - 1; i++) {
            y0 = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
            x2 = x0 + checkboxmargin;
            x3 = x2 + checkboxspaceing;
            y2 = y0 - checkboxspaceing - checkboxmargin;
            y3 = y2 + checkboxspaceing;
            c.drawLine(x0, y0, x1, y0, paint);
            paint.setStyle(Paint.Style.STROKE);
            c.drawRect(x2, y2, x3, y3, paint);
        }
        paint.setStyle(Paint.Style.FILL);

    }

    private void draw_minutes(Canvas c, Transformation t, float lineSpacing, float margin) {
        Log.v(TAG, "draw_minutes=====>>>");
        float spacingMm = lineSpacing;
        int nIndex = 1;
        boolean bEink6Inch = false;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
            bEink6Inch = true;
            nIndex = 0;
        }
        float vertLineMm = margin;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);
        float y0 = t.applyY(((heightMm - n * spacingMm) / 2) / heightMm);
        float y1 = t.applyY(((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm);
        float x2, x3, y2, y3;

        float checkboxmargin = (y1 - y0) / 8;
        float checkboxspaceing = checkboxmargin * 6;

        Log.v(TAG, "margin:" + margin + " lineSpacing:" + lineSpacing + " spacingMm:" + spacingMm);
        Log.v(TAG, "x0:" + x0 + " x1:" + x1);
        Log.v(TAG, " widthMm:" + widthMm + ", heightMm:" + heightMm);

        for (int i = nIndex; i <= n; i++) {
            y0 = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
            c.drawLine(x0, y0, x1, y0, paint);
            Log.v(TAG, "x0:" + x0 + ", x1:" + x1 + ", y0:" + y0);
        }


        float textxoffset = lineSpacing;
        float lineyoffset = margin / lineSpacing;
        float textsize = margin * 2;

        paint.setTextSize(t.scaleText(42));

        if (bEink6Inch) {
            for (int i = 0; i <= 8; i++) {
                y0 = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
                y1 = y0 - spacingMm;
                y2 = y0 - lineyoffset;
                if (i == 0) {
                    y2 = y0 - lineyoffset * 10;
                    c.drawText("Page", x0, y2, paint);
                    y2 = y0 - lineyoffset * 2;
                    c.drawLine(x0, y2, x1, y2, paint);
                }
                if (i == 1) {
                    y2 = y0 - checkboxspaceing - checkboxmargin;
                    y3 = y2 + checkboxspaceing;
                    x2 = x0 + textsize * 3;
                    x3 = (x1 - x0) / 2 - textxoffset;
                    c.drawText("Date", x0, y1, paint);
                    c.drawText("Time", x3 + textxoffset, y1, paint);
                    c.drawLine(x0, y0 - lineyoffset, x1, y0 - lineyoffset, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    c.drawRect(x2, y2, x3, y3, paint);
                    x2 = x3 + textsize * 3;
                    x3 = x1;
                    c.drawRect(x2, y2, x3, y3, paint);
                }
                if (i == 2) {
                    paint.setStyle(Paint.Style.FILL);
                    c.drawText("Agenda", x0, y1, paint);
                    c.drawLine(x0, y2, x1, y2, paint);
                }
                if (i == 4) {
                    c.drawText("Attendees", x0, y1, paint);
                    c.drawLine(x0, y2, x1, y2, paint);
                }
                if (i == 7) {
                    c.drawText("Note", x0, y1, paint);
                    c.drawLine(x0, y2, x1, y2, paint);
                }
            }
        } else {
            for (int i = 1; i <= 9; i++) {
                y0 = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
                y1 = y0 - spacingMm;
                y2 = y0 - lineyoffset;
                if (i == 1) {
                    y2 = y0 - lineyoffset * 10;
                    c.drawText("Page", x0, y2, paint);
                    y2 = y0 - lineyoffset * 2;
                    c.drawLine(x0, y2, x1, y2, paint);
                }
                if (i == 2) {
                    y2 = y0 - checkboxspaceing - checkboxmargin;
                    y3 = y2 + checkboxspaceing;
                    x2 = x0 + textsize * 3;
                    x3 = (x1 - x0) / 2 - textxoffset;
                    c.drawText("Date", x0, y1, paint);
                    c.drawText("Time", x3 + textxoffset, y1, paint);
                    c.drawLine(x0, y0 - lineyoffset, x1, y0 - lineyoffset, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    c.drawRect(x2, y2, x3, y3, paint);
                    x2 = x3 + textsize * 3;
                    x3 = x1;
                    c.drawRect(x2, y2, x3, y3, paint);
                }
                if (i == 3) {
                    paint.setStyle(Paint.Style.FILL);
                    c.drawText("Agenda", x0, y1, paint);
                    c.drawLine(x0, y2, x1, y2, paint);
                }
                if (i == 5) {
                    c.drawText("Attendees", x0, y1, paint);
                    c.drawLine(x0, y2, x1, y2, paint);
                }
                if (i == 8) {
                    c.drawText("Note", x0, y1, paint);
                    c.drawLine(x0, y2, x1, y2, paint);
                }
            }
        }
        paint.setStyle(Paint.Style.FILL);

    }

    private void draw_stave(Canvas c, Transformation t, float lineSpacing, float margin) {

        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);
        float y0 = t.applyY(((heightMm - n * spacingMm) / 2) / heightMm);
        float y1 = t.applyY(((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm);

        float linemargin = (y1 - y0) / 4;

        for (int i = 0; i <= n - 1; i += 3) {
            y0 = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
            for (int j = 0; j < 5; j++) {
                y1 = y0 + linemargin * j;
                c.drawLine(x0, y1, x1, y1, paint);
            }
        }

    }

    private void draw_diary(Canvas c, Transformation t, float lineSpacing, float margin) {

        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));
        paint.setARGB(0xff, shade, shade, shade);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);
        float y0 = t.applyY(((heightMm - n * spacingMm) / 2) / heightMm);
        float y1 = t.applyY(((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm);
        float offsety = y0 + (y1 - y0) / 2 - t.applyY(spacingMm / heightMm);

        float textsize = margin * 2;
        paint.setTextSize(t.scaleText(42));

        c.drawText("Date                /", x0, offsety, paint);
        for (int i = 0; i <= n; i++) {
            y0 = t.applyY(((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm);
            c.drawLine(x0, y0, x1, y0, paint);
        }

    }


    public void render(Artist artist) {
        if (!artist.getBackgroundVisible()) return;

        switch (paperType) {
            case EMPTY:
                return;
            case RULED:
                render_ruled(artist, LEGALRULED_SPACING, 31.75f);
                return;
            case COLLEGERULED:
                render_ruled(artist, COLLEGERULED_SPACING, 25.0f);
                return;
            case NARROWRULED:
                render_ruled(artist, NARROWRULED_SPACING, 25.0f);
                return;
            case QUAD:
                render_quad(artist);
                return;
            case CORNELLNOTES:
                render_cornellnotes(artist);
                return;
            case DAYPLANNER:
                return;
            case MUSIC:
                render_music_manuscript(artist);
            case CALLIGRAPHY_SMALL:
                render_calligraphy(artist, CALLIGRAPHY_TYPE_SMALL);
                return;
            case CALLIGRAPHY_BIG:
                render_calligraphy(artist, CALLIGRAPHY_TYPE_BIG);
                return;
            case TODOLIST:
                render_todolist(artist, TODOLIST_SPACING, 25.0f);
                return;
            case MINUTES:
                render_minutes(artist, TODOLIST_SPACING, 25.0f);
                return;
            case STAVE:
                render_stave(artist, STAVE_SPACING, 25.0f);
                return;
            case DIARY:
                render_diary(artist, TODOLIST_SPACING, 25.0f);
                return;
            case HEX:
                return;
            case CUSTOMIZED:
                render_customized(artist);
        }
    }


    private void render_ruled(Artist artist, float lineSpacing, float margin) {
        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        float vertLineMm = margin;
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = marginMm / heightMm;
        float x1 = (widthMm - marginMm) / heightMm;
        if (lineSpacing == COLLEGERULED_SPACING) {
            for (int i = 0; i <= n; i++) {
                float y = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
                artist.drawLine(x0, y, x1, y, line);
            }
        } else if (lineSpacing == NARROWRULED_SPACING) {

            for (int i = 1; i <= n - 1; i++) {
                float y = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
                artist.drawLine(x0, y, x1, y, line);
            }

        } else {

            for (int i = 1; i <= n - 2; i++) {
                float y = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
                artist.drawLine(x0, y, x1, y, line);
            }

        }


        // Paint margin
        if (margin > 0.0f) {
            line.setColor(1f, 0f, 0f);
            line.setWidth(0);
            float y0 = marginMm / heightMm;
            float y1;

            if (lineSpacing == COLLEGERULED_SPACING) {

                y1 = (heightMm - marginMm - spacingMm) / heightMm;

            } else if (lineSpacing == NARROWRULED_SPACING) {

                y1 = (heightMm - marginMm - spacingMm * 1.5f) / heightMm;

            } else {

                y1 = (heightMm - marginMm - spacingMm * 2) / heightMm;

            }

            float x = vertLineMm / widthMm;
            artist.drawLine(x, y0, x, y1, line);
        }
    }

    private void render_quad(Artist artist) {
        float spacingMm = 10f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        int nx, ny;
        float x, x0, x1, y, y0, y1;
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        ny = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm);
        nx = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm);
        float marginXMm = (widthMm - nx * spacingMm) / 2;
        float marginYMm = (heightMm - ny * spacingMm) / 2;
        x0 = marginXMm / heightMm;
        x1 = (widthMm - marginXMm) / heightMm;
        y0 = marginYMm / heightMm;
        y1 = (heightMm - marginYMm - spacingMm) / heightMm;
        for (int i = 0; i < ny; i++) {
            y = (marginYMm + i * spacingMm) / heightMm;
            artist.drawLine(x0, y, x1, y, line);
        }
        for (int i = 0; i <= nx; i++) {
            x = (marginXMm + i * spacingMm) / heightMm;
            artist.drawLine(x, y0, x, y1, line);
        }
    }

    private void render_calligraphy(Artist artist, int type) {
        int positionBufferNum = 0; // base on the max value of ny and nx
        float spacingMmBase = 10f; // base on quad paper
        float spacingMm = 0;

        // calculate spacing according to type
        if (type == CALLIGRAPHY_TYPE_SMALL)
            spacingMm = (float) 6 * spacingMmBase;
        else
            // CALLIGRAPHY_TYPE_BIG
            spacingMm = (float) 8 * spacingMmBase;

		/*
		if (Hardware.isEink6InchHardwareType()) {
			spacingMm = (float) (spacingMm * 1.7);
		}
		*/
        int nx, ny;
        float x, x0, x1, y, y0, y1;

        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        ny = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm);
        nx = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm);
        float marginXMm = (widthMm - nx * spacingMm) / 2;
        float marginYMm = (heightMm - ny * spacingMm) / 2;
        x0 = marginXMm / heightMm;
        x1 = (widthMm - marginXMm) / heightMm;
        y0 = (marginYMm * 2 / 3) / heightMm;
        y1 = (heightMm - marginYMm * 4 / 3) / heightMm;

        if (nx >= ny)
            positionBufferNum = nx + 1;
        else
            positionBufferNum = ny + 1;

        // for recording x, y positions
        float[] position_x = new float[positionBufferNum];
        float[] position_y = new float[positionBufferNum];

        // draw frame line
        // draw "-" line
        for (int i = 0; i <= ny; i++) {
            y = (marginYMm * 2 / 3 + i * spacingMm) / heightMm;
            artist.drawLine(x0, y, x1, y, line);
            position_y[i] = y;
        }
        // draw "|" line
        for (int i = 0; i <= nx; i++) {
            x = (marginXMm + i * spacingMm) / heightMm;
            artist.drawLine(x, y0, x, y1, line);
            position_x[i] = x;
        }

        // debug position info
		/*
		for (int i=0; i<=nx; i++) {
			ALog.debug("position_x["+i+"]:" +position_x[i]);
		}
		for (int i=0; i<=ny; i++) {
			ALog.debug("position_y["+i+"]:" +position_y[i]);
		}
		 */

        // draw "x" and '+' line
        line.setWidth(0);
        for (int i = 0; i < ny; i++) {
            for (int j = 0; j < nx; j++) {
                // draw "\" line
                artist.drawLine(position_x[j], position_y[i], position_x[j + 1], position_y[i + 1], line);
                // draw "|" line
                artist.drawLine((position_x[j] + position_x[j + 1]) / 2, position_y[i],
                        (position_x[j] + position_x[j + 1]) / 2, position_y[ny], line);
                // draw "/" line
                artist.drawLine(position_x[j + 1], position_y[i], position_x[j], position_y[i + 1], line);
                // draw "-" line
                artist.drawLine(position_x[j], (position_y[i] + position_y[i + 1]) / 2, position_x[nx],
                        (position_y[i] + position_y[i + 1]) / 2, line);
            }
        }
    }

    private void render_cornellnotes(Artist artist) {
        float x0, x1, y0, y1;
        final float MARGIN = 1.25f;
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);

        // Details
        float spacingMm = COLLEGERULED_SPACING;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        int n = (int) Math.floor((heightMm - (MARGIN * INCH_in_MM) - 2 * marginMm) / spacingMm);

        x0 = (MARGIN * INCH_in_MM) / widthMm + marginMm / heightMm;
        x1 = (widthMm - marginMm) / heightMm;

        for (int i = 1; i <= n - 3; i++) {
            float y = (heightMm - n * spacingMm - MARGIN * INCH_in_MM + i * spacingMm) / heightMm;
            artist.drawLine(x0, y, x1, y, line);
        }

        // Cue Column
        x0 = (MARGIN * INCH_in_MM) / widthMm;
        x1 = x0;
        y0 = 0f;
        y1 = (heightMm - spacingMm * 2 - (MARGIN * INCH_in_MM)) / heightMm;
        artist.drawLine(x0, y0, x1, y1, line);

        // Summary area at base of page
        x0 = 0f;
        x1 = widthMm / heightMm;
        y0 = (heightMm - spacingMm * 2 - (MARGIN * INCH_in_MM)) / heightMm;
        y1 = y0;
        artist.drawLine(x0, y0, x1, y1, line);
    }

    private void render_music_manuscript(Artist artist) {
        float lineSpacingMm = 2.0f;
        float staveHeight = 4 * lineSpacingMm;
        float staffTopMarginMm = 25.0f;
        float staffBottomMarginMm = 15.0f;
        float staffSideMarginMm = 15.0f;

        int staveCount = 12;
        if (aspectRatio.isPortrait())
            staveCount = 12;
        else
            staveCount = 8;
        float staffTotal = staffTopMarginMm + staffBottomMarginMm + staveCount * staveHeight;
        float staffSpacing = staveHeight + (heightMm - staffTotal) / (staveCount - 1);

        float x0, x1, y;
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);

        line.setWidth(0);
        staffSpacing = staveHeight + (heightMm - staffTotal) / (staveCount - 1);

        x0 = staffSideMarginMm / heightMm;
        x1 = (widthMm - staffSideMarginMm) / heightMm;

        for (int i = 0; i < staveCount; i++) {
            for (int j = 0; j < 5; j++) {
                y = (staffTopMarginMm + i * staffSpacing + j * lineSpacingMm) / heightMm;
                artist.drawLine(x0, y, x1, y, line);
            }
        }
    }

    private void render_todolist(Artist artist, float lineSpacing, float margin) {

        Log.v(TAG, "render_todolist ===>>");
        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        float vertLineMm = margin;
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = marginMm / heightMm;
        float x1 = (widthMm - marginMm) / heightMm;
        float y0 = ((heightMm - n * spacingMm) / 2) / heightMm;
        float y1 = ((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm;
        float x2, x3, y2, y3;
        float checkboxmargin = (y1 - y0) / 8;
        float checkboxspaceing = checkboxmargin * 6;
        Log.v(TAG, "heightMm:" + heightMm + ", widthMm:" + widthMm + ", marginMm:" + marginMm + ", lineN:" + n + ", spacingMm:" + spacingMm);
        Log.v(TAG, "x0:" + x0 + ", x1:" + x1);
        Log.v(TAG, "y0:" + y0 + ",y1:" + y1 + ",checkboxmargin:" + checkboxmargin + ", checkboxspaceing:" + checkboxspaceing);

        for (int i = 1; i <= n - 1; i++) {
            y0 = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
            Log.v(TAG, "Y:" + y0);
            artist.drawLine(x0, y0, x1, y0, line);
            x2 = x0 + checkboxmargin;
            x3 = x2 + checkboxspaceing;
            y2 = y0 - checkboxspaceing - checkboxmargin;
            y3 = y2 + checkboxspaceing;
            artist.drawLine(x2, y2, x3, y2, line);
            artist.drawLine(x3, y2, x3, y3, line);
            artist.drawLine(x3, y3, x2, y3, line);
            artist.drawLine(x2, y3, x2, y2, line);
        }

    }

    private void render_minutes(Artist artist, float lineSpacing, float margin) {
        Log.v(TAG, "render_minutes ====>>>");
        float spacingMm = lineSpacing;
        float textsize = margin / widthMm / spacingMm / 2;
        int nIndex = 1;
        boolean bEink6Inch = false;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
            textsize = margin / widthMm / spacingMm;
            bEink6Inch = true;
            nIndex = 0;
        }
        float vertLineMm = margin;
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = marginMm / heightMm;
        float x1 = (widthMm - marginMm) / heightMm;
        float y0 = ((heightMm - n * spacingMm) / 2) / heightMm;
        float y1 = ((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm;
        float x2, x3, y2, y3;
        float checkboxmargin = (y1 - y0) / 8;
        float checkboxspaceing = checkboxmargin * 6;
//		float textxoffset = lineSpacing/widthMm/10;
        float textxoffset = lineSpacing / widthMm / spacingMm;
        float lineyoffset = checkboxmargin / 6;

        for (int i = nIndex; i <= n; i++) {
            y0 = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
            artist.drawLine(x0, y0, x1, y0, line);
        }

        Log.v(TAG, "margin:" + margin + " lineSpacing:" + lineSpacing + " spacingMm:" + spacingMm + " widthMm:" + widthMm + " heightMm:" + heightMm);
        Log.v(TAG, "x0:" + x0 + " x1:" + x1);

        if (bEink6Inch) {
            for (int i = 0; i <= 8; i++) {
                y0 = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
                y1 = y0 - spacingMm;
                y2 = y0 - lineyoffset;
                Log.v(TAG, "y0:" + y0 + " y1:" + y1 + " y2:" + y2);
                if (i == 0) {
                    y2 = y0 - lineyoffset * 2;
                    artist.drawLine(x0, y2, x1, y2, line);
                }
                if (i == 1) {
                    y2 = y0 - checkboxspaceing - checkboxmargin;
                    y3 = y2 + checkboxspaceing;
                    x2 = x0 + textsize * 12;
                    x3 = (x1 - x0) / 2 - textxoffset;
                    artist.drawLine(x0, y0 - lineyoffset, x1, y0 - lineyoffset, line);
                    artist.drawLine(x2, y2, x3, y2, line);
                    artist.drawLine(x3, y2, x3, y3, line);
                    artist.drawLine(x3, y3, x2, y3, line);
                    artist.drawLine(x2, y3, x2, y2, line);
                    x2 = x3 + textsize * 12;
                    x3 = x1;
                    artist.drawLine(x2, y2, x3, y2, line);
                    artist.drawLine(x3, y2, x3, y3, line);
                    artist.drawLine(x3, y3, x2, y3, line);
                    artist.drawLine(x2, y3, x2, y2, line);
                }
                if (i == 2 || i == 4 || i == 7) {
                    //draw text ?
                    artist.drawLine(x0, y2, x1, y2, line);
                }
            }
        } else {
            for (int i = 1; i <= 9; i++) {
                y0 = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
                y1 = y0 - spacingMm;
                y2 = y0 - lineyoffset;
                Log.v(TAG, "y0:" + y0 + " y1:" + y1 + " y2:" + y2);
                if (i == 1) {
                    y2 = y0 - lineyoffset * 2;
                    artist.drawLine(x0, y2, x1, y2, line);
                }
                if (i == 2) {
                    y2 = y0 - checkboxspaceing - checkboxmargin;
                    y3 = y2 + checkboxspaceing;
                    x2 = x0 + textsize * 12;
                    x3 = (x1 - x0) / 2 - textxoffset;
                    artist.drawLine(x0, y0 - lineyoffset, x1, y0 - lineyoffset, line);
                    artist.drawLine(x2, y2, x3, y2, line);
                    artist.drawLine(x3, y2, x3, y3, line);
                    artist.drawLine(x3, y3, x2, y3, line);
                    artist.drawLine(x2, y3, x2, y2, line);
                    x2 = x3 + textsize * 12;
                    x3 = x1;
                    artist.drawLine(x2, y2, x3, y2, line);
                    artist.drawLine(x3, y2, x3, y3, line);
                    artist.drawLine(x3, y3, x2, y3, line);
                    artist.drawLine(x2, y3, x2, y2, line);
                }
                if (i == 3 || i == 5 || i == 8) {
                    //draw text ?
                    artist.drawLine(x0, y2, x1, y2, line);
                }
            }
        }

    }

    private void render_stave(Artist artist, float lineSpacing, float margin) {
        if (Hardware.isEinkUsingLargerUI()) {
            offset_bottom = 0.056f;
        } else {
            offset_bottom = 0.033f;
        }

        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = marginMm / heightMm;
        float x1 = (widthMm - marginMm) / heightMm;
        float y0 = ((heightMm - n * spacingMm) / 2) / heightMm;
        float y1 = ((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm;

        float linemargin = (y1 - y0) / 4;

        for (int i = 0; i <= n - 1; i += 3) {
            y0 = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
            for (int j = 0; j < 5; j++) {
                y1 = y0 + linemargin * j;
                artist.drawLine(x0, y1, x1, y1, line);
            }
        }
    }

    private void render_diary(Artist artist, float lineSpacing, float margin) {

        Log.v(TAG, "render_diary ===>>");
        float spacingMm = lineSpacing;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.7);
        }
        LineStyle line = new LineStyle();
        line.setColor(0f, 0f, 0f);
        line.setWidth(0);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        float x0 = marginMm / heightMm;
        float x1 = (widthMm - marginMm) / heightMm;
        float y0;

        for (int i = 0; i <= n; i++) {
            y0 = ((heightMm - n * spacingMm) / 2 + i * spacingMm) / heightMm;
            artist.drawLine(x0, y0, x1, y0, line);
        }

    }

    private Bitmap loadBitmap(String file_path, RectF bBox) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(file_path, options);
        int oldwidth = bitmap.getWidth();
        int oldheight = bitmap.getHeight();

        float scale;

        Matrix matrix = new Matrix();

        if (oldwidth >= oldheight) {
            scale = bBox.width() / (float) oldwidth;
        } else {
            scale = bBox.height() / (float) oldheight;
        }

        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(bitmap, 0, 0, oldwidth, oldheight, matrix, true);
    }

    public void ClearBitmap() {
        bitmap = null;
    }

    private void draw_customized(Canvas canvas, RectF bBox) {

        File file = new File(paperPath);
        if (!file.exists()) return;
        if (!paperPath.equals("na")) {
            //  2018 0921  Alan   no matter if bitmap exists or not, load bitmap again to fit to screen when entering overview mode
//        	else if (bitmap!=null ){

            if (bitmap == null || (bitmap.getHeight() != canvas.getHeight() && bitmap.getWidth() != canvas.getWidth())) {
                bBox.top = 0;
                bBox.bottom = canvas.getHeight();
                bBox.left = 0;
                bBox.right = canvas.getWidth();
                bitmap = loadBitmap(paperPath, bBox);
                centerWidth = (int) (bBox.width() - bitmap.getWidth()) / 2;
                centerHeight = (int) (bBox.height() - bitmap.getHeight()) / 2;
            }
        }

        canvas.drawBitmap(bitmap,
                centerWidth,
                centerHeight,
                null);
    }

    private void drawPNG_customized(Canvas canvas, RectF bBox) {
        File file = new File(paperPath);
        if (!file.exists()) return;

        if (!paperPath.equals("na")) {
//            if (bitmap == null) {
            bitmap = loadBitmap(paperPath, bBox);
            centerWidth = (int) (bBox.width() - bitmap.getWidth()) / 2;
            centerHeight = (int) (bBox.height() - bitmap.getHeight()) / 2;
//            }
            canvas.drawBitmap(bitmap,
                    centerWidth,
                    centerHeight,
                    null);
            bitmap = null;
        }
    }

    private void render_customized(Artist artist) {
        Bitmap bmp = loadPdfBitmap(paperPath, artist.getPdf().getWidth() * 2, artist.getPdf().getHeight() * 2);

        File file = savebitmap(bmp);

        if (!file.exists()) return;

        artist.imageBackground(file, 0, 0, artist.getPdf().getWidth(), artist.getPdf().getHeight());

    }

    float offset_left, offset_right, offset_top, offset_bottom;

    private Bitmap loadPdfBitmap(String file_path, float width, float height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        Bitmap bmp = BitmapFactory.decodeFile(file_path, options);
        int oldwidth = bmp.getWidth();
        int oldheight = bmp.getHeight();

        float rate = Global.MACHINE_PIXEL_RATE_VALUE;
        int calHeight = (int) Math.rint(width / rate);

        Matrix matrix = new Matrix();
        float scale;

        if (oldwidth >= oldheight) {
            scale = ((width) / (float) oldwidth);
        } else {
            scale = (calHeight / (float) oldheight);
        }
        matrix.postScale(scale, scale);

        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, oldwidth, oldheight, matrix, true);

        // 設定畫布大小
        Bitmap fittingBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fittingBitmap);
        canvas.drawColor(Color.WHITE);    //white background

        int centerWidth = (int) (width - newBmp.getWidth()) / 2;
        int centerHeight = (calHeight - newBmp.getHeight()) / 2;

        canvas.drawBitmap(newBmp,
                centerWidth,
                centerHeight,
                null);
        return fittingBitmap;
    }

    private File savebitmap(Bitmap bmp) {
        String tempFile_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/nNote";
        String tempFile_name = "temp.jpg";

        File dir = new File(tempFile_path);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, tempFile_name);

        OutputStream outStream = null;

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("file", "" + file);
        return file;

    }

    /**
     * @param c           : Canvas canvas
     * @param t           : Transformation t
     * @param lineSpacing : COLLEGERULED_SPACING
     */
    private void draw_dot_college(Canvas c, Transformation t, float lineSpacing) {

        float spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.44);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;

        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);

        int x0i = Math.round(x0) + 2;
        int x1i = Math.round(x1) - 6;

        for (int i = 0; i <= n; i++) {
            float y = t.applyY((marginMm + (i + 1) * spacingMm) / heightMm);

            for (int j = x0i; j < x1i; j++) {
                if (j % 10 == 0) {
                    c.drawCircle(j, y, 2, paint);

                }
            }
        }

        float lineX = t.applyX((widthMm - marginMm * 2) / heightMm) / 4;
        float lineY0 = t.applyY(marginMm / heightMm);
        float lineY1 = t.applyY(((heightMm - marginMm * 3)) / heightMm);
        paint.setStrokeWidth(1);
        c.drawLine(lineX, lineY0, lineX, lineY1, paint);

    }

    /**
     * @param c           : Canvas canvas
     * @param t           : Transformation t
     * @param lineSpacing : COLLEGERULED_SPACING
     */
    private void draw_todo_dot_line(Canvas c, Transformation t, float lineSpacing) {

        float spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.44);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;

        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);

        int x0i = Math.round(x0) + 2;
        int x1i = Math.round(x1) - 6;

        float y0 = t.applyY(((heightMm - n * spacingMm) / 2) / heightMm);
        float y1 = t.applyY(((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm);
        float x2, x3, y2, y3;

        float checkboxmargin = (y1 - y0) / 8;
        float checkboxspaceing = (checkboxmargin * 6) / 2;
        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        for (int i = 0; i <= n; i++) {
            float y = t.applyY((marginMm + (i + 1) * spacingMm) / heightMm);
            paint.setStyle(Paint.Style.FILL);
            for (int j = x0i; j < x1i; j++) {
                if (j % 10 == 0) {
                    if (i % 5 == 0) {
                        c.drawLine(j - lineOffset, y, x1i + lineOffset, y, paint);
                        break;
                    }
                    c.drawCircle(j, y, 2, paint);
                }
            }

            x2 = x0 + checkboxmargin;
            x3 = x2 + checkboxspaceing;
            y2 = y + t.applyY((spacingMm / 2) / heightMm) - (checkboxspaceing / 2);
            y3 = y2 + checkboxspaceing;
            paint.setStyle(Paint.Style.STROKE);
            c.drawRect(x2, y2, x3, y3, paint);
        }
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_central_cross(Canvas c, Transformation t) {

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);
        float y = t.applyY(((heightMm - marginMm) / 2) / heightMm);

        c.drawLine(x0, y, x1, y, paint);

        float x = t.applyX(((widthMm - marginMm) / 2) / heightMm);
        float y0 = t.applyY(marginMm / heightMm);
        float y1 = t.applyY((heightMm - marginMm) / heightMm);

        if (widthMm > heightMm) y1 -= marginMm * 8;

        c.drawLine(x, y0, x, y1, paint);
    }

    /**
     * @param c           : Canvas canvas
     * @param t           : Transformation t
     * @param lineSpacing : COLLEGERULED_SPACING
     */
    private void draw_todo_two_column_dot_line(Canvas c, Transformation t, float lineSpacing) {

        float spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.6);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;

        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);

        int x0i = Math.round(x0) + 2;
        int x1i = Math.round(x1) - 6;

        float y0 = t.applyY(((heightMm - n * spacingMm) / 2) / heightMm);
        float y1 = t.applyY(((heightMm - n * spacingMm) / 2 + spacingMm) / heightMm);
        float x2, x3, y2, y3;

        float checkboxmargin = (y1 - y0) / 8;
        float checkboxspaceing = (checkboxmargin * 6) / 2;
        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        float centerX = t.applyX(((widthMm - marginMm * 2) / 2) / heightMm);

        for (int i = 0; i <= n; i++) {
            float y = t.applyY((marginMm + (i + 1) * spacingMm) / heightMm);
            paint.setStyle(Paint.Style.FILL);
            for (int j = x0i; j < x1i; j++) {
                if (j % 10 == 0) {
                    if (i % 2 == 0) {
                        if (i == 0) {
                            paint.setStrokeWidth(4);
                            c.drawLine(j - lineOffset, y, x1i + lineOffset, y, paint);
                        } else {
                            c.drawLine(j - lineOffset, y, centerX - lineOffset, y, paint);
                            c.drawLine(centerX + lineOffset, y, x1i + lineOffset, y, paint);
                        }
                        break;
                    }

                    if ((j < centerX - lineOffset) || (j > centerX + lineOffset)) {

                        c.drawCircle(j, y, 2, paint);
                    }
                }
            }

            // draw check rect
            if (i % 2 == 0) {
                x2 = x0 + checkboxmargin;
                x3 = x2 + checkboxspaceing;
                y2 = y + t.applyY((spacingMm / 2) / heightMm) - (checkboxspaceing / 2);
                y3 = y2 + checkboxspaceing;

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                c.drawRect(x2, y2, x3, y3, paint);
                c.drawRect(x2 + centerX, y2, x3 + centerX, y3, paint);
            }
        }
    }

    /**
     * @param c             : Canvas canvas
     * @param t             : Transformation t
     * @param //lineSpacing : COLLEGERULED_SPACING
     */
    private void draw_yearly_planner(Canvas c, Transformation t) {
        String[] month = {
                "J A N U A R Y", "F E B R U A R Y", "M A R C H",
                "A P R I L", "M A Y", "J U N E",
                "J U L Y", "A U G U S T", "S E P T E M B E R",
                "O C T O B E R", "N O V E M B E R", "D E C E M B E R"
        };

        int row = 3;
        int column = 4;

        if (widthMm > heightMm) {
            row = 4;
            column = 3;
        }
        float spacingMm_Width = (widthMm - marginMm * 2) / row;
        float spacingMm_Height = (heightMm - marginMm * 2) / column;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        // === drawLine paint ===
        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(2);

        // === drawText Month Number paint ===
        Rect numBounds = new Rect();
        Paint paintN = new Paint();
        paintN.setARGB(0xff, shade, shade, shade);
        paintN.setTextSize(t.scaleText(40));
        paintN.setColor(Color.GRAY);
        paintN.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // === drawText Month String paint ===
        Rect monthBounds = new Rect();
        Paint paintS = new Paint();
        paintS.setARGB(0xff, shade, shade, shade);
        paintS.setTextSize(t.scaleText(20));
        paintS.setColor(Color.BLACK);
        paintS.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintS.setTextScaleX(0.9f);

        int n_Width = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm_Width);
        int n_Height = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm_Height);

        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        float margin = t.applyX(marginMm / heightMm);

        float breakX = t.applyX(spacingMm_Width / heightMm);
        for (int i = 0; i < n_Height; i++) {
            float y = t.applyY((marginMm + (i + 1) * spacingMm_Height) / heightMm);

            for (int k = 0; k < n_Width; k++) {
                // drawLine Horizontal
                if (i < n_Height - 1)
                    c.drawLine(margin + (breakX * k), y, margin + (breakX * (k + 1)) - lineOffset, y, paint);

                // === month Number ===
                String m = String.valueOf((i * n_Width) + (k + 1));
                paintN.getTextBounds(m, 0, m.length(), numBounds);
                c.drawText(m, margin * 2 + (breakX * k), margin * 2.8f + (breakX * i) - numBounds.exactCenterY(), paintN);

                // === month String ===
                int s = (i * n_Width) + k;
                paintS.getTextBounds(month[s], 0, month[s].length(), monthBounds);
                c.drawText(month[s], margin * 3.6f + (breakX * k) + numBounds.width(), margin * 2.3f + (breakX * i) - numBounds.exactCenterY(), paintS);

            }
        }

        float breakY = t.applyY(spacingMm_Height / heightMm);
        for (int i = 0; i < n_Width - 1; i++) {
            float x = t.applyY((marginMm + (i + 1) * spacingMm_Width) / heightMm);

            // drawLine Vertical
            for (int k = 0; k < n_Height; k++)
                c.drawLine(x, margin + (breakY * k), x, margin + (breakY * (k + 1)) - lineOffset, paint);
        }
    }

    /**
     * @param c           : Canvas canvas
     * @param t           : Transformation t
     * @param lineSpacing : COLLEGERULED_SPACING
     */
    private void draw_daily_planner(Canvas c, Transformation t, float lineSpacing) {
        final String memo = "M E M O";
        float spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 0.865);
        }

        float spacingMm_Width = (widthMm - marginMm * 2) / 3.3f;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        // === drawText Number paint ===
        Rect numBounds = new Rect();
        Paint paintN = new Paint();
        paintN.setARGB(0xff, shade, shade, shade);
        paintN.setTextSize(t.scaleText(22));
        paintN.setColor(Color.BLACK);
        paintN.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;

        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);

        int x0i = Math.round(x0) + 2;
        int x1i = Math.round(x1) - 6;

        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        float margin = t.applyX(marginMm / heightMm);
        int item_start = 7;
        float breakX = t.applyX(spacingMm_Width / heightMm);

        for (int i = 0; i < n; i++) {
            float y = t.applyY((marginMm + (i + 2) * spacingMm) / heightMm);
            paint.setStyle(Paint.Style.FILL);
            for (int j = x0i; j < x1i; j++) {
                if (j % 10 == 0) {
                    if (i == 0) {
                        paint.setColor(Color.BLACK);
                        c.drawLine(j - lineOffset, y, x1i + lineOffset, y, paint);
                        break;
                    }

                    if (i == 1)
                        break;

                    paint.setColor(Color.GRAY);

                    if (i % 2 == 0) {
                        item_start++;
                        String num = "" + item_start;
                        paintN.getTextBounds(num, 0, num.length(), numBounds);
                        c.drawText(num, j + margin / 2, y + numBounds.height() / 2, paintN);

                        if (i == 10)
                            paint.setColor(Color.BLACK);

                        c.drawLine(j + margin * 2, y, margin + (breakX * 2) - lineOffset, y, paint);
                        break;
                    }
                    if (j < (breakX * 2) - margin)
                        c.drawCircle(j + margin * 2, y, 2, paint);
                }
            }
        }

        float head_y = t.applyY(((marginMm * 1.5f) + (2 * spacingMm)) / heightMm);
        float bottom_y = t.applyY((heightMm - (marginMm * 2)) / heightMm);
        c.drawLine(margin + (breakX * 2), head_y, margin + (breakX * 2), bottom_y, paint);

        // Memo column
        float n2_spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            n2_spacingMm = (float) (n2_spacingMm * 0.865);
        }

        final int n2 = (int) Math.floor((heightMm - 2 * marginMm) / n2_spacingMm) - 2;
        final int n2_row = 12;
        final float n2_X0 = margin + (breakX * 2) + lineOffset;
        final float n2_X1 = t.applyX((widthMm - marginMm) / heightMm);

        paint.setColor(Color.GRAY);
        final int rect_qty = 5;
        int rect_count = rect_qty; // memo CheckBox rect quantity

        for (int i = 0; i < n2_row; i++) {
            float y = t.applyY((marginMm + (i + 2) * n2_spacingMm) / heightMm);
            for (int j = x0i; j < x1i; j++) {
                if (j % 10 == 0) {
                    if (i == 0)
                        break;

                    if (i % 2 == 0) {
                        c.drawLine(n2_X0, y, n2_X1, y, paint);
                        break;
                    }
                }
            }

            // draw CheckBox Rect
            float y0 = t.applyY(((heightMm - n2 * n2_spacingMm) / 2) / heightMm);
            float y1 = t.applyY(((heightMm - n2 * n2_spacingMm) / 2 + n2_spacingMm) / heightMm);
            float x2, x3, y2, y3;
            float checkboxmargin = (y1 - y0) / 5;
            float checkboxspaceing = (checkboxmargin * 6) / 2;

            if (i % 2 == 0 && rect_count > 0) {
                rect_count--;
                x2 = n2_X0 + checkboxmargin;
                x3 = x2 + checkboxspaceing;
                y2 = y + t.applyY(n2_spacingMm / heightMm) - (checkboxspaceing / 2);
                y3 = y2 + checkboxspaceing;
                paint.setStyle(Paint.Style.STROKE);
                c.drawRect(x2, y2, x3, y3, paint);

            }
        }

        paintN.setTextScaleX(0.9f);
        c.drawText(memo, n2_X0 + lineOffset, t.applyY((marginMm + ((n2_row + 1) * n2_spacingMm)) / heightMm), paintN);
    }

    /**
     * @param c           : Canvas canvas
     * @param t           : Transformation t
     * @param lineSpacing : COLLEGERULED_SPACING
     */
    private void draw_daily_planner_two_column(Canvas c, Transformation t, float lineSpacing) {
        final String memo = "M E M O";
        float spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1);
        }

        float spacingMm_Width = (widthMm - marginMm * 2) / 3.3f;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        // === drawText Number paint ===
        Rect numBounds = new Rect();
        Paint paintN = new Paint();
        paintN.setARGB(0xff, shade, shade, shade);
        paintN.setTextSize(t.scaleText(22));
        paintN.setColor(Color.BLACK);
        paintN.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;

        float x0 = t.applyX(marginMm / heightMm);
        float x1 = t.applyX((widthMm - marginMm) / heightMm);

        int x0i = Math.round(x0) + 2;
        int x1i = Math.round(x1) - 6;


        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        float margin = t.applyX(marginMm / heightMm);
        int item_start = 7;
        float breakX = t.applyX(spacingMm_Width / heightMm);

        for (int i = 0; i < n; i++) {
            float y = t.applyY((i * spacingMm + spacingMm * 2) / heightMm);

            paint.setStyle(Paint.Style.FILL);
            for (int j = x0i; j < x1i; j++) {
                if (i == 0) {
                    paint.setColor(Color.BLACK);
                    c.drawLine(j, y, x1i, y, paint);
                    break;
                }

                if (i == 1) break;

                paint.setColor(Color.GRAY);

                item_start++;
                String num = "" + item_start;
                paintN.getTextBounds(num, 0, num.length(), numBounds);
                c.drawText(num, j + margin / 2, y + numBounds.height() / 2, paintN);

                if (i == 6)
                    paint.setColor(Color.BLACK);

                c.drawLine(j + margin * 2, y, margin + (breakX) - lineOffset, y, paint);
                c.drawLine(margin + (breakX) + lineOffset, y, margin + (breakX * 2) - lineOffset, y, paint);

                break;

            }
        }

        float head_y = t.applyY((marginMm + (2 * spacingMm)) / heightMm);
        float bottom_y = t.applyY((heightMm - (marginMm * 2)) / heightMm);
        c.drawLine(margin + (breakX * 2), head_y, margin + (breakX * 2), bottom_y, paint);

        for (int y = (int) head_y; y < bottom_y; y++) {
            if (y % 10 == 0) {
                c.drawCircle(margin + breakX, y, 2, paint);
            }
        }

        // Memo column
        float n2_spacingMm = lineSpacing + 1.15f;
        if (Hardware.isEinkUsingLargerUI()) {
            n2_spacingMm = (float) (n2_spacingMm * 1.33);
        }

        final int n2 = (int) Math.floor((heightMm - 2 * marginMm) / n2_spacingMm) - 2;
        final int n2_row = 7;
        final float n2_X0 = margin + (breakX * 2) + lineOffset;
        final float n2_X1 = t.applyX((widthMm - marginMm) / heightMm);

        paint.setColor(Color.GRAY);
        final int rect_qty = 6;
        int rect_count = rect_qty; // memo CheckBox rect quantity

        for (int i = 0; i < n2_row; i++) {
            float y = t.applyY((i * n2_spacingMm + spacingMm * 2) / heightMm);

            for (int j = x0i; j < x1i; j++) {
                if (i == 0)
                    break;

                c.drawLine(n2_X0, y, n2_X1, y, paint);
            }

            // draw CheckBox Rect
            float y0 = t.applyY(((heightMm - n2 * spacingMm) / 2) / heightMm);
            float y1 = t.applyY(((heightMm - n2 * spacingMm) / 2 + spacingMm) / heightMm);
            float x2, x3, y2, y3;
            float checkboxmargin = (y1 - y0) / 5;
            float checkboxspaceing = (checkboxmargin * 6) / 2;

            if (rect_count > 0) {
                rect_count--;
                x2 = n2_X0 + checkboxmargin;
                x3 = x2 + checkboxspaceing;
                y2 = y + t.applyY((n2_spacingMm / 2) / heightMm) - (checkboxspaceing / 2);
                y3 = y2 + checkboxspaceing;
                paint.setStyle(Paint.Style.STROKE);
                c.drawRect(x2, y2, x3, y3, paint);

            }
        }

        paintN.setTextScaleX(0.9f);
        c.drawText(memo, n2_X0 + lineOffset, t.applyY(((n2_row + 1) * n2_spacingMm) / heightMm), paintN);
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_monthly_planner_vertical(Canvas c, Transformation t) {

        float spacingMm = 5.8f;
        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.44);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setStrokeWidth(2);
        paint.setTextSize(t.scaleText(24f));
        int n = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm) - 2;
        int x0 = 0;
        float x1 = Math.round(t.applyX(widthMm / heightMm));
        float y = Math.round(t.applyY(heightMm / heightMm));
        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        paint.setColor(Global.color_tate);
        c.drawRect(0,
                t.applyY((marginMm + 13 * spacingMm) / heightMm),
                Math.round(t.applyX(widthMm / heightMm)),
                t.applyY((marginMm + 23 * spacingMm) / heightMm),
                paint);
        paint.setColor(Color.BLACK);
        c.drawLine((x1 - x0) * 0.37f, 0, (x1 - x0) * 0.37f, y, paint);
        c.drawLine((x1 - x0) * 0.67f, 0, (x1 - x0) * 0.67f, y, paint);

        for (int i = 0; i <= n; i++) {

            if (i >= 31) {
                break;
            }

            float y0 = t.applyY((marginMm + (i + 3) * spacingMm) / heightMm);
            float y1 = t.applyY((marginMm + (i + 4) * spacingMm) / heightMm);

            paint.setStyle(Paint.Style.FILL);
            for (int j = x0; j < x1; j++) {
                if (j % 10 == 0) {
                    if (i == 0) {
                        c.drawLine(j - lineOffset, y0, x1 + lineOffset, y0, paint);
                        break;
                    } else {
                        c.drawCircle(j, y0, 2, paint);
                    }
                }
            }

            c.drawText(String.valueOf(i + 1), (x1 - x0) * 0.03f, y0 + ((y1 - y0) * 3 / 4), paint);
        }
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_monthly_planner_horizontal(Canvas c, Transformation t) {
        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setStrokeWidth(2);
        paint.setTextSize(t.scaleText(24f));
        int x0 = 0;
        float x1 = Math.round(t.applyX(widthMm / heightMm));
        float y0 = 0;
        float y1 = Math.round(t.applyY(heightMm / heightMm));

        float startX = (x1 - x0) * 0.08f;
        float spacing = (x1 - x0) * 0.0285f;
        float startY = (y1 - y0) * 0.1092f;

        paint.setColor(Global.color_tate);

        c.drawRect(startX + spacing * 10,
                startY,
                startX + spacing * 20,
                y1,
                paint);

        paint.setColor(Color.BLACK);

        c.drawLine(x0, startY, x1, startY, paint);
        c.drawLine(x0, (y1 - y0) * 0.4188f, x1, (y1 - y0) * 0.4188f, paint);
        c.drawLine(x0, (y1 - y0) * 0.6926f, x1, (y1 - y0) * 0.6926f, paint);

        for (int i = 0; i <= 30; i++) {
            paint.setStyle(Paint.Style.FILL);

            if (String.valueOf(i + 1).length() > 1) {
                c.drawText(String.valueOf(i + 1), startX + spacing / 5, startY + spacing, paint);
            } else {
                c.drawText(String.valueOf(i + 1), startX + spacing / 3, startY + spacing, paint);
            }

            if (i == 0) {
                c.drawLine(startX,
                        startY,
                        startX,
                        y1,
                        paint);

                startX += spacing;

                continue;
            }
            for (int j = (int) startY; j < y1; j++) {
                if (j % 10 == 0) {
                    c.drawCircle(startX, j, 2, paint);
                }
            }

            startX += spacing;
        }
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_weekly_planner_vertical(Canvas c, Transformation t) {

        float initY = 23.63f;
        float spacingMm = 26.53f;
        float margin = 7f;

        float appliedWidth = t.applyX(widthMm / heightMm);
        float appliedHeight = t.applyX(heightMm / heightMm);
        float appliedLeftX1 = 0.135f * appliedWidth;
        float appliedLeftX2 = 0.552f * appliedWidth;
        float appliedTextLeft = 0.0479f * appliedWidth;
        float appliedLineToTextDistance = 0.116f * appliedHeight;
        float appliedDesiredTextWidth = 0.042f * appliedWidth;

        if (Hardware.isEinkUsingLargerUI()) {
            spacingMm = (float) (spacingMm * 1.44);
        }

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        int lineNumber = (int) Math.floor((heightMm - 2 * margin) / spacingMm);

        float startX = t.applyX(margin / heightMm);
        float stopX = t.applyX((widthMm - margin) / heightMm);

        int drawStartX = Math.round(startX);
        int drawStopX = Math.round(stopX);

        for (int i = 0; i < lineNumber; i++) {// draw horizontal lines
            float drawY = t.applyY((initY + i * spacingMm) / heightMm);
            c.drawLine(drawStartX, drawY, drawStopX, drawY, paint);
        }
        int drawStartY = Math.round(t.applyY(initY / heightMm));
        int drawStopY = Math.round(t.applyY((heightMm - margin) / heightMm));

        for (int y = drawStartY; y < drawStopY; y += 10) {//draw vertical lines

            c.drawCircle(appliedLeftX1, y, 1, paint);
            c.drawCircle(appliedLeftX2, y, 1, paint);

        }

        String date[] = {"M O N", "TU E", "W E D", "TH U", "F R I", "S A T", "S U N"};
        setTextSizeForWidth(paint, appliedDesiredTextWidth, "MON");
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        for (int i = 0; i < lineNumber; i++) {
            float drawY = t.applyY((initY + i * spacingMm) / heightMm) + appliedLineToTextDistance;
            c.drawText(date[i], appliedTextLeft, drawY, paint);
        }


        float left = 2;
        float top = 1f + t.applyY((initY + (lineNumber - 1) * spacingMm) / heightMm);
        float bottom = t.applyY(heightMm / heightMm);
        float right = t.applyY(widthMm / heightMm) - 2;

        RectF rectF = new RectF(left, top, right, bottom);
        paint.setColor(Global.color_tate);
        Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DARKEN);
        paint.setXfermode(mXfermode);
        c.drawRect(rectF, paint);
    }

    private void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
        final float initialTextSize = 48f;
        paint.setTextSize(initialTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float desiredTextSize = initialTextSize * desiredWidth / bounds.width();
        paint.setTextSize(desiredTextSize);
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_weekly_planner_horizontal(Canvas c, Transformation t) {
        String[] week = {"", "M O N", "T U E", "W E D", "T H U", "F R I", "S A T", "S U N"};

        int row = 4;
        int column = 2;

        float spacingMm_Width = (widthMm - marginMm * 2) / row;
        float spacingMm_Height = (heightMm - marginMm * 2) / column;

        if (t.scale < threshold)
            shade += (int) ((threshold - t.scale) / threshold * (0xff - shade));

        // === drawLine paint ===
        Paint paint = new Paint();
        paint.setARGB(0xff, shade, shade, shade);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(2);

        // === drawText Week String paint ===
        Rect weekBounds = new Rect();
        Paint paintS = new Paint();
        paintS.setARGB(0xff, shade, shade, shade);
        paintS.setTextSize(t.scaleText(20));
        paintS.setColor(Color.BLACK);
        paintS.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        int n_Width = (int) Math.floor((widthMm - 2 * marginMm) / spacingMm_Width);
        int n_Height = (int) Math.floor((heightMm - 2 * marginMm) / spacingMm_Height);

        float lineOffset = t.applyX((marginMm / 2) / heightMm);
        float margin = t.applyX(marginMm / heightMm);

        float breakX = t.applyX(spacingMm_Width / heightMm);
        float headSpace = margin * 2;
        int n = 6;
        float n_space = t.applyY((spacingMm_Height - marginMm * 2) / heightMm) / n;

        // set Rect Background color
        float left = margin + (breakX * 3);
        float top = t.applyY((marginMm * 3 + spacingMm_Height) / heightMm);
        float right = t.applyY(widthMm / heightMm);
        float bottom = t.applyY(heightMm / heightMm);

        paint.setColor(Global.color_tate);
        c.drawRect(left, top, right, bottom, paint);

        paint.setColor(Color.GRAY);
        for (int i = 0; i < n_Height; i++) {
            float y = t.applyY((marginMm * 3 + i * spacingMm_Height) / heightMm);

            for (int k = 0; k < n_Width; k++) {
                int s = (i * 4) + k;
                paintS.getTextBounds(week[s], 0, week[s].length(), weekBounds);
                if (i == 0) {
                    c.drawText(week[s], margin + (breakX * (k + 1)) - margin - weekBounds.width(), y + headSpace + (n_space / 2), paintS);
                } else if (i == 1) {
                    c.drawText(week[s], margin + (breakX * (k + 1)) - margin - weekBounds.width(), y + headSpace, paintS);

                }

                // drawLine Horizontal
                if (i == 0) {
                    c.drawLine(margin + (breakX * k), y + headSpace, margin + (breakX * (k + 1)) - lineOffset, y + headSpace, paint);
                    if (k == 0) {
                        for (int p = 0; p < n - 1; p++) {
                            float x0 = margin * 1.5f + (breakX * k);
                            float x1 = margin + (breakX * (k + 1)) - lineOffset;
                            float y0 = y + headSpace + (n_space * (p + 1));
                            c.drawLine(x0, y0, x1, y0, paint);
                        }
                    }

                    if (k > 0) {
                        for (int x = (int) (margin + (breakX * k)); x < (breakX * (k + 1)); x++) {
                            if (x % 10 == 0) {
                                c.drawCircle(x, y + headSpace + margin * 3, 2, paint);
                            }
                        }
                    }
                } else {
                    c.drawLine(margin + (breakX * k), y, margin + (breakX * (k + 1)) - lineOffset, y, paint);

                    for (int x = (int) (margin + (breakX * k)); x < (breakX * (k + 1)); x++) {
                        if (x % 10 == 0) {
                            c.drawCircle(x, y + margin * 3, 2, paint);
                        }
                    }
                }
            }
        }

        float breakY = t.applyY(spacingMm_Height / heightMm);
        for (int i = 0; i < n_Width - 1; i++) {
            float x = t.applyY((marginMm + (i + 1) * spacingMm_Width) / heightMm);

            // drawLine Vertical
            c.drawLine(x, margin * 5 + (breakY * 0), x, margin * 3 + (breakY * (0 + 1)) - lineOffset, paint);
            c.drawLine(x, margin * 3 + (breakY * 1), x, margin + (breakY * (1 + 1)) - lineOffset, paint);

        }
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_subject_headline_portrait(Canvas c, Transformation t) {

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        final float scale = t.scale / portraitScale;

        float x0 = scale * 180;
        float y0 = scale * 250;
        float x1 = t.applyX((widthMm) / heightMm);
        float y1 = y0;

        float fontSize = t.scaleText(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(fontSize);
        paint.setStrokeWidth(scale * 5);

        y0 = y1 = scale * 250;
        c.drawLine(x0, y0, x1, y1, paint); // Line 1

        y0 = scale * (250 + fontSize + 10);
        drawText(" SUBJECT", (int) fontSize, x0, y0, fontSize / 3, c, paint); // text

        y0 = y1 = scale * 450;
        c.drawLine(x0, y0, x1, y1, paint); // Line 2

        y0 = y1 = scale * 560;
        c.drawLine(x0, y0, x1, y1, paint); // Line 3

        int n = 8; // circle point line
        float space = scale * 78;
        for (int i = 0; i < n; i++) {
            float y = y0 + (i + 1) * space;
            paint.setStyle(Paint.Style.FILL);
            for (int j = (int) (x0 + scale * 20); j < (int) x1; j++) {
                if (j % 10 == 0) {
                    c.drawCircle(j, y, 2, paint);
                }
            }
        }

        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        float triangleX = 0;
        float triangleY = 0;
        float triangleWidth = t.applyX((widthMm) / heightMm) / 3.5f;

        path.moveTo(triangleX, triangleY);
        path.lineTo(triangleX + triangleWidth, triangleY);
        path.lineTo(triangleX, triangleY + triangleWidth);
        path.close();
        c.drawPath(path, paint);
    }

    /**
     * @param c : Canvas canvas
     * @param t : Transformation t
     */
    private void draw_subject_headline_landscape(Canvas c, Transformation t) {

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        final float scale = t.scale / landscapeScale;

        float x0 = scale * 235;
        float y0 = scale * 230;
        float x1 = t.applyX((widthMm) / heightMm);
        float y1 = y0;

        paint.setStrokeWidth(scale * 5);
        c.drawLine(x0, y0, x1, y1, paint);

        float fontSize = t.scaleText(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(fontSize);

        y0 = y0 + (scale * (fontSize + 8));
        drawText(" SUBJECT", (int) fontSize, x0, y0, fontSize / 3, c, paint);

        y0 = y1 = scale * 420;
        c.drawLine(x0, y0, x1, y1, paint);

        y0 = y1 = scale * 550;
        c.drawLine(x0, y0, x1, y1, paint);

        int n = 4; // circle point line
        float space = scale * 78;
        for (int i = 0; i < n; i++) {
            float y = y0 + (i + 1) * space;
            paint.setStyle(Paint.Style.FILL);
            for (int j = (int) x0; j < (int) x1; j++) {
                if (j % 10 == 0) {
                    c.drawCircle(j, y, 2, paint);
                }
            }
        }

        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        float triangleX = 0;
        float triangleY = 0;
        float triangleWidth = scale * (t.applyX((widthMm) / heightMm) / 4.6f);

        path.moveTo(triangleX, triangleY);
        path.lineTo(triangleX + triangleWidth, triangleY);
        path.lineTo(triangleX, triangleY + triangleWidth);
        path.close();
        c.drawPath(path, paint);
    }

    /**
     * Each word increase space.
     *
     * @param text     : text string
     * @param fontSize : font size
     * @param x        : start x coordinate
     * @param y        : start y coordinate
     * @param space    : each word space
     * @param c        : Canvas
     * @param paint    : Paint
     */
    private void drawText(String text, int fontSize, float x, float y, float space, Canvas c, Paint paint) {
        for (int i = 0; i < text.length(); i++) {
            c.drawText(text.substring(i, i + 1), x + i * (fontSize / 2 + space), y, paint);
        }
    }
}
