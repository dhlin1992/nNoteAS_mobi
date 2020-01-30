package name.vbraun.view.write;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import ntx.note.Global;
import ntx.note.ToolboxConfiguration;
import ntx.note.artist.Artist;
import ntx.note2.R;

import static ntx.note.Global.getResources;

public class TextBox extends GraphicsControlPoint {
    private final static String TAG = "TextBox";

    public final static int DEFAULT_FONT_SIZE = 12;

    private final static int MIN_FONT_SIZE = 9;
    private final static int MAX_FONT_SIZE = 26;
    private final static float FONT_SIZE_SCALE_FACTOR = 3.75f;
    private final static float FONT_SIZE_THUMB_SCALE_FACTOR = 0.2f;
    /**
     * 2019.8.20 Karote
     * FONT_SIZE_SCALE_RATIO_BASIC_HEIGHT = 1340
     * This value is the height of canvas when toolbar is showing at portrait mode.
     * It is used for scale canvas before drawing textLayout.
     * <p>
     * The height of toolbar is 100, and the height of overview is 1440.
     * So it is (1440 - 100)
     */
    private final static float FONT_SIZE_SCALE_RATIO_BASIC_HEIGHT = 1340f; // 1440-100

    private final static int OUTLINE_PEN_WIDTH = 3;
    private final static float MOVING_CONTROL_POINT_BITMAP_SIZE = 50.0f;
    private final static float RESIZE_CONTROL_POINT_BITMAP_SIZE = 30.0f;
    private final static float MIN_TEXT_UP_PADDING = 18.0f;
    private final static float MIN_TEXT_LEFT_PADDING = 10.0f;

    public final static int NEW_TEXT_BOX_WIDTH_THRESHOLD = 50;
    public final static int MIN_TEXT_BOX_WIDTH = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR) + (int) (MIN_TEXT_LEFT_PADDING * 2);
    public final static int MIN_TEXT_BOX_HEIGHT = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR) + (int) (MIN_TEXT_UP_PADDING * 2);

    /**
     * cp_tl: control point top left
     * cp_tr: control point top right
     * cp_bl: control point bottom left
     * cp_br: control point bottom right
     * cp_c : control point center
     */
    private ControlPoint cp_tl, cp_tr, cp_bl, cp_br, cp_c;

    private volatile boolean isSelected = false;

    private Paint pen = new Paint();
    private final Rect rect = new Rect();
    private final RectF rectF = new RectF();

    private TextPaint textPaint = new TextPaint();
    private TextPaint thumbTextPaint = new TextPaint();
    private float mTextUpPadding;
    private float mTextLeftPadding;
    private float mThumbTextUpPadding;
    private float mThumbTextLeftPadding;

    private DynamicLayout textLayout;

    private String textStr;
    private int textFontSize;
    private int textColor;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderLine;

    private String textStr_backup;
    private int textFontSize_backup;
    private int textColor_backup;
    private boolean isBold_backup;
    private boolean isItalic_backup;
    private boolean isUnderLine_backup;

    /**
     * Construct a new TextBox
     *
     * @param transform The current transformation
     * @param x         Screen x coordinate
     * @param y         Screen y coordinate
     */
    protected TextBox(Transformation transform, float x, float y) {
        super(Tool.TEXT);
        setTransform(transform);
        cp_tl = new ControlPoint(transform, x, y);
        cp_bl = new ControlPoint(transform, x, y + 1);
        cp_tr = new ControlPoint(transform, x + 1, y);
        cp_br = new ControlPoint(transform, x + 1, y + 1);
        cp_c = new ControlPoint(transform, x, y);
        controlPoints.add(cp_tl);
        controlPoints.add(cp_tr);
        controlPoints.add(cp_c);
        ToolboxConfiguration toolboxConfiguration = ToolboxConfiguration.getInstance();
        textStr = "";
        textFontSize = toolboxConfiguration.getTextBoxFontSize();
        switch (toolboxConfiguration.getTextBoxColor()) {
            case ToolboxConfiguration.TextColor.BLACK:
                textColor = Color.BLACK;
                break;
            case ToolboxConfiguration.TextColor.DARK_GRAY:
                textColor = Global.grey_5;
                break;
            case ToolboxConfiguration.TextColor.GRAY:
                textColor = Global.grey_8;
                break;
            case ToolboxConfiguration.TextColor.LIGHT_GRAY:
                textColor = Global.grey_A;
                break;
            case ToolboxConfiguration.TextColor.WHITE:
                textColor = Color.WHITE;
                break;
            default:
                textColor = Color.BLACK;
                break;
        }
        isBold = toolboxConfiguration.isTextBoxBold();
        isItalic = toolboxConfiguration.isTextBoxItalic();
        isUnderLine = toolboxConfiguration.isTextBoxUnderLine();

        setPen();
    }

    /**
     * Copy constructor
     */
    protected TextBox(final TextBox textBox) {
        super(textBox);
        cp_tl = new ControlPoint(textBox.cp_tl);
        cp_bl = new ControlPoint(textBox.cp_bl);
        cp_tr = new ControlPoint(textBox.cp_tr);
        cp_br = new ControlPoint(textBox.cp_br);
        cp_c = new ControlPoint(textBox.cp_c);
        controlPoints.add(cp_tl);
        controlPoints.add(cp_tr);
        controlPoints.add(cp_c);

        textStr = textBox.textStr;
        textFontSize = textBox.textFontSize;
        textColor = textBox.textColor;
        isBold = textBox.isBold;
        isItalic = textBox.isItalic;
        isUnderLine = textBox.isUnderLine;
        recompute_bounding_box = true;

        setPen();
    }

    public TextBox(DataInputStream in) throws IOException {
        super(Tool.TEXT);
        int version = in.readInt();
        if (version > 1)
            throw new IOException("Unknown line version!");

        tool = in.readInt();
        if (tool != Tool.TEXT)
            throw new IOException("Unknown tool type!");

        float left = in.readFloat();
        float right = in.readFloat();
        float top = in.readFloat();
        float bottom = in.readFloat();

        cp_bl = new ControlPoint(transform, left, bottom);
        cp_br = new ControlPoint(transform, right, bottom);
        cp_tl = new ControlPoint(transform, left, top);
        cp_tr = new ControlPoint(transform, right, top);
        cp_c = new ControlPoint(transform, (left + right) / 2, top);
        controlPoints.add(cp_tl);
        controlPoints.add(cp_tr);
        controlPoints.add(cp_c);

        textStr = in.readUTF();
        textFontSize = in.readInt();
        textColor = in.readInt();
        isBold = in.readBoolean();
        isItalic = in.readBoolean();
        isUnderLine = in.readBoolean();
        recompute_bounding_box = true;

        setPen();
    }

    @Override
    protected void backup() {
        backupControlPoints.clear();
        backupControlPoints.add(cp_tl.copy());
        backupControlPoints.add(cp_tr.copy());
        backupControlPoints.add(cp_bl.copy());
        backupControlPoints.add(cp_br.copy());
        backupControlPoints.add(cp_c.copy());
        textStr_backup = textStr;
        textFontSize_backup = textFontSize;
        textColor_backup = textColor;
        isBold_backup = isBold;
        isItalic_backup = isItalic;
        isUnderLine_backup = isUnderLine;
    }

    @Override
    protected void restore() {
        if (backupControlPoints.size() == 0) {
            Log.e(TAG, "restore() called without backup()");
            return;
        }
        cp_tl = backupControlPoints.get(0);
        cp_tr = backupControlPoints.get(1);
        cp_bl = backupControlPoints.get(2);
        cp_br = backupControlPoints.get(3);
        cp_c = backupControlPoints.get(4);
        controlPoints.clear();
        controlPoints.add(cp_tl);
        controlPoints.add(cp_tr);
        controlPoints.add(cp_c);
    }

    @Override
    public TextBox getBackupGraphics() {
        TextBox backupGraphics = new TextBox(this);
        backupGraphics.restore();
        if (textStr_backup == null) {
            Log.e(TAG, "restore() called without backup()");
            return null;
        } else {
            backupGraphics.textStr = textStr_backup;
            backupGraphics.textFontSize = textFontSize_backup;
            backupGraphics.textColor = textColor_backup;
            backupGraphics.isBold = isBold_backup;
            backupGraphics.isItalic = isItalic_backup;
            backupGraphics.isUnderLine = isUnderLine_backup;
        }
        return backupGraphics;
    }

    @Override
    public TextBox getCloneGraphics() {
        return new TextBox(this);
    }

    @Override
    public void replace(Graphics graphics) {
        TextBox textBox = (TextBox) graphics;
        cp_tl = new ControlPoint(textBox.cp_tl);
        cp_bl = new ControlPoint(textBox.cp_bl);
        cp_tr = new ControlPoint(textBox.cp_tr);
        cp_br = new ControlPoint(textBox.cp_br);
        cp_c = new ControlPoint(textBox.cp_c);
        controlPoints.clear();
        controlPoints.add(cp_tl);
        controlPoints.add(cp_tr);
        controlPoints.add(cp_c);

        textStr = textBox.textStr;
        textFontSize = textBox.textFontSize;
        textColor = textBox.textColor;
        isBold = textBox.isBold;
        isItalic = textBox.isItalic;
        isUnderLine = textBox.isUnderLine;

        setPen();
        recompute_bounding_box = true;
    }

    @Override
    public boolean intersects(RectF r_screen) {
        return false;
    }

    @Override
    public void draw(Canvas c) {
        computeScreenRect();
        c.clipRect(0, 0, c.getWidth(), c.getHeight(), android.graphics.Region.Op.REPLACE);
        int textLayoutWidth = (int) (rect.width() - mTextLeftPadding * 2);

        if (textLayoutWidth < (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR))
            textLayoutWidth = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR);

        textLayout = new DynamicLayout(textStr, textPaint, textLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        c.save();
        float canvasScale = transform.scale / FONT_SIZE_SCALE_RATIO_BASIC_HEIGHT;
        c.scale(canvasScale, canvasScale);
        float textLeft = rect.left + mTextLeftPadding;
        float textTop = rect.top + mTextUpPadding;
        c.translate(textLeft / canvasScale, textTop / canvasScale);
        textLayout.draw(c);
        c.restore();
    }

    @Override
    public void drawThumbnail(Canvas c) {
        computeScreenRect();
        c.clipRect(0, 0, c.getWidth(), c.getHeight(), android.graphics.Region.Op.REPLACE);
        int textLayoutWidth = (int) (rect.width() - mTextLeftPadding * 2);

        if (textLayoutWidth < (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR))
            textLayoutWidth = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR);

        textLayout = new DynamicLayout(textStr, thumbTextPaint, textLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        c.save();
        c.translate(rect.left + mThumbTextLeftPadding, rect.top + mThumbTextUpPadding);
        textLayout.draw(c);
        c.restore();
    }

    @Override
    public void drawOutLine(Canvas c) {
        pen.setStrokeWidth(OUTLINE_PEN_WIDTH);
        pen.setPathEffect(null);
        computeScreenRect();
        c.clipRect(0, 0, c.getWidth(), c.getHeight(), android.graphics.Region.Op.REPLACE);
        c.drawRect(rect, pen);
    }

    @Override
    public void convert_draw(Canvas c) {
        computeScreenRect();
        c.clipRect(0, 0, c.getWidth(), c.getHeight(), android.graphics.Region.Op.REPLACE);
        int textLayoutWidth = (int) (rect.width() - mTextLeftPadding * 2);

        if (textLayoutWidth < (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR))
            textLayoutWidth = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR);

        textLayout = new DynamicLayout(textStr, textPaint, textLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        c.save();
        float canvasScale = transform.scale / FONT_SIZE_SCALE_RATIO_BASIC_HEIGHT;
        c.scale(canvasScale, canvasScale);
        float textLeft = rect.left + mTextLeftPadding;
        float textTop = rect.top + mTextUpPadding;
        c.translate(textLeft / canvasScale, textTop / canvasScale);
        textLayout.draw(c);
        c.restore();
    }

    @Override
    protected void drawControlPoints(Canvas canvas) {
        pen.setStrokeWidth(OUTLINE_PEN_WIDTH);

        if (!isSelected)
            pen.setPathEffect(new DashPathEffect(new float[]{10, 20,}, 0));
        else
            pen.setPathEffect(null);

        computeScreenRect();
        canvas.drawRect(rect, pen);
        reLocateControlPoint();
        drawMovingControlPoint(canvas, cp_c);
        drawResizeControlPoint(canvas, cp_tl);
        drawResizeControlPoint(canvas, cp_tr);
    }

    @Override
    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(1);  // protocol #1
        out.writeInt(tool);
        out.writeFloat(cp_tl.x);
        out.writeFloat(cp_br.x);
        out.writeFloat(cp_tl.y);
        out.writeFloat(cp_br.y);
        out.writeUTF(textStr);
        out.writeInt(textFontSize);
        out.writeInt(textColor);
        out.writeBoolean(isBold);
        out.writeBoolean(isItalic);
        out.writeBoolean(isUnderLine);
    }

    @Override
    public void render(Artist artist) {
    }

    @Override
    protected ControlPoint initialControlPoint() {
        return cp_br;
    }

    @Override
    protected void computeBoundingBox() {
        LinkedList<ControlPoint> cps = new LinkedList<>();
        cps.add(cp_tl);
        cps.add(cp_tr);
        cps.add(cp_br);
        cps.add(cp_bl);
        ListIterator<ControlPoint> iter = cps.listIterator();
        ControlPoint p = iter.next();
        float xmin, xmax, ymin, ymax;
        xmin = xmax = transform.applyX(p.x);
        ymin = ymax = transform.applyY(p.y);
        while (iter.hasNext()) {
            p = iter.next();
            float x = p.screenX();
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
            float y = p.screenY();
            ymin = Math.min(ymin, y);
            ymax = Math.max(ymax, y);
        }
        bBoxFloat.set(xmin, ymin, xmax, ymax);
        float extra = boundingBoxInset();
        bBoxFloat.inset(extra, extra);
        bBoxFloat.roundOut(bBoxInt);
        recompute_bounding_box = false;
    }

    @Override
    protected float boundingBoxInset() {
        return -MOVING_CONTROL_POINT_BITMAP_SIZE;
    }

    @Override
    void controlPointMoved(ControlPoint point, float newX, float newY) {
        if (point == cp_tr && transform.applyX(newX - cp_tl.x) < MIN_TEXT_BOX_WIDTH)
            return;
        if (point == cp_tl && transform.applyX(cp_tr.x - newX) < MIN_TEXT_BOX_WIDTH)
            return;

        point.x = newX;
        if (point != cp_tl && point != cp_tr)
            point.y = newY;

        super.controlPointMoved(point, newX, newY);
        if (point == cp_c) {
            float width2 = (cp_br.x - cp_bl.x) / 2;
            float height = cp_br.y - cp_tr.y;
            cp_tr.x = cp_br.x = cp_c.x + width2;
            cp_tl.x = cp_bl.x = cp_c.x - width2;
            cp_tl.y = cp_tr.y = cp_c.y;
            cp_bl.y = cp_br.y = cp_c.y + height;
        } else {
            if (point == cp_br) {
                cp_tr.x = point.x;
                cp_bl.y = point.y;
                computeScreenRect();
            } else if (point == cp_tl) {
                //cp_tl.x = point.x;
                cp_bl.x = point.x;
                computeScreenRect();
                restrictShape(null);
                cp_c.x = transform.inverseX(rectF.left + (rectF.right - rectF.left) / 2);
                cp_c.y = transform.inverseY(rectF.top);
            } else if (point == cp_tr) {
                //cp_tr.x = point.x;
                cp_br.x = point.x;
                computeScreenRect();
                restrictShape(null);
                cp_c.x = transform.inverseX(rectF.left + (rectF.right - rectF.left) / 2);
                cp_c.y = transform.inverseY(rectF.top);
            }
        }
    }

    @Override
    void restrictShape(ControlPoint point) {
        int textLayoutWidth = (int) (rect.width() - mTextLeftPadding * 2);

        if (textLayoutWidth < (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR))
            textLayoutWidth = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR);

        textLayout = new DynamicLayout(textStr, textPaint, textLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

        float newHeight = textLayout.getHeight() + mTextUpPadding * 2;

        rectF.bottom = rectF.top + newHeight;
        cp_bl.y = cp_br.y = transform.inverseY(rectF.bottom);
        computeScreenRect();
    }

    @Override
    public void move(float offsetX, float offsetY) {
        cp_c.x += transform.inverseX(offsetX);
        cp_c.y += transform.inverseY(offsetY);
        float width2 = (cp_br.x - cp_bl.x) / 2;
        float height2 = (cp_tr.y - cp_br.y) / 2;
        cp_br.y = cp_bl.y = cp_c.y - height2;
        cp_tr.y = cp_tl.y = cp_c.y + height2;
        cp_br.x = cp_tr.x = cp_c.x + width2;
        cp_bl.x = cp_tl.x = cp_c.x - width2;

        recompute_bounding_box = true;

        computeBoundingBox();
    }

    @Override
    public boolean isCenterControlPoint(ControlPoint controlPoint) {
        return controlPoint == cp_c;
    }

    @Override
    public Rect getOutLineBounding() {
        return rect;
    }

    @Override
    public int getBoundingGap() {
        return 0;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getTextStr() {
        return textStr;
    }

    public int getTextFontSize() {
        return textFontSize;
    }

    public int getTextColor() {
        switch (textColor) {
            case Color.BLACK:
                return ToolboxConfiguration.TextColor.BLACK;
            case Global.grey_5:
                return ToolboxConfiguration.TextColor.DARK_GRAY;
            case Global.grey_8:
                return ToolboxConfiguration.TextColor.GRAY;
            case Global.grey_A:
                return ToolboxConfiguration.TextColor.LIGHT_GRAY;
            case Color.WHITE:
                return ToolboxConfiguration.TextColor.WHITE;
            default:
                return ToolboxConfiguration.TextColor.BLACK;
        }
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public boolean isUnderLine() {
        return isUnderLine;
    }

    public void setTextBoxParameters(@Nullable String _textStr, @Nullable Integer _textFontSize, @Nullable Integer _textColor,
                                     @Nullable Boolean _isBold, @Nullable Boolean _isItalic, @Nullable Boolean _isUnderLine) {
        if (_textStr != null)
            textStr = _textStr;

        if (_textFontSize != null)
            textFontSize = _textFontSize;

        if (_textColor != null) {
            switch (_textColor) {
                case ToolboxConfiguration.TextColor.BLACK:
                    textColor = Color.BLACK;
                    break;
                case ToolboxConfiguration.TextColor.DARK_GRAY:
                    textColor = Global.grey_5;
                    break;
                case ToolboxConfiguration.TextColor.GRAY:
                    textColor = Global.grey_8;
                    break;
                case ToolboxConfiguration.TextColor.LIGHT_GRAY:
                    textColor = Global.grey_A;
                    break;
                case ToolboxConfiguration.TextColor.WHITE:
                    textColor = Color.WHITE;
                    break;
                default:
                    textColor = Color.BLACK;
                    break;
            }
        }

        if (_isBold != null)
            isBold = _isBold;

        if (_isItalic != null)
            isItalic = _isItalic;

        if (_isUnderLine != null)
            isUnderLine = _isUnderLine;

        setPen();

        int textLayoutWidth = (int) (rect.width() - mTextLeftPadding * 2);

        if (textLayoutWidth < (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR))
            textLayoutWidth = (int) (MAX_FONT_SIZE * FONT_SIZE_SCALE_FACTOR);

        textLayout = new DynamicLayout(textStr, textPaint, textLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        restrictShape(null);
    }

    private void setPen() {
        pen.setAntiAlias(true);
        pen.setColor(Color.BLACK);
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeCap(Paint.Cap.ROUND);

        int textStyle = Typeface.NORMAL;

        if (isBold) {
            if (isItalic)
                textStyle = Typeface.BOLD_ITALIC;
            else
                textStyle = Typeface.BOLD;
        } else if (isItalic) {
            textStyle = Typeface.ITALIC;
        }

        textPaint.setColor(textColor);
        thumbTextPaint.setColor(textColor);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, textStyle));
        thumbTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, textStyle));

        if (isUnderLine) {
            textPaint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
            thumbTextPaint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
        } else {
            textPaint.setFlags(textPaint.getFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            thumbTextPaint.setFlags(thumbTextPaint.getFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        }

        textPaint.setTextSize(textFontSize * FONT_SIZE_SCALE_FACTOR);
        thumbTextPaint.setTextSize(textFontSize * FONT_SIZE_SCALE_FACTOR * FONT_SIZE_THUMB_SCALE_FACTOR);

        mTextLeftPadding = (float) Math.pow(textFontSize * FONT_SIZE_SCALE_FACTOR, 0.6d);
        if (mTextLeftPadding < MIN_TEXT_LEFT_PADDING)
            mTextLeftPadding = MIN_TEXT_LEFT_PADDING;

        mThumbTextLeftPadding = (float) Math.pow(textFontSize * FONT_SIZE_SCALE_FACTOR * FONT_SIZE_THUMB_SCALE_FACTOR, 0.6d);
        if (mThumbTextLeftPadding < MIN_TEXT_LEFT_PADDING * FONT_SIZE_THUMB_SCALE_FACTOR)
            mThumbTextLeftPadding = MIN_TEXT_LEFT_PADDING * FONT_SIZE_THUMB_SCALE_FACTOR;


        mTextUpPadding = (float) Math.pow(textFontSize * FONT_SIZE_SCALE_FACTOR, 0.15d);
        if (mTextUpPadding < MIN_TEXT_UP_PADDING)
            mTextUpPadding = MIN_TEXT_UP_PADDING;

        mThumbTextUpPadding = (float) Math.pow(textFontSize * FONT_SIZE_SCALE_FACTOR * FONT_SIZE_THUMB_SCALE_FACTOR, 0.15d);
        if (mThumbTextUpPadding < MIN_TEXT_UP_PADDING * FONT_SIZE_THUMB_SCALE_FACTOR)
            mThumbTextUpPadding = MIN_TEXT_UP_PADDING * FONT_SIZE_THUMB_SCALE_FACTOR;
    }

    private void computeScreenRect() {
        rectF.bottom = cp_bl.screenY();
        rectF.top = cp_tl.screenY();
        rectF.left = cp_tl.screenX();
        rectF.right = cp_tr.screenX();
        rectF.sort();
        rectF.round(rect);
    }

    private void reLocateControlPoint() {
        cp_tl.x = cp_bl.x = transform.inverseX(rectF.left);
        cp_tr.x = cp_br.x = transform.inverseX(rectF.right);
        cp_c.x = transform.inverseX(rectF.left + (rectF.right - rectF.left) / 2);

        cp_tl.y = cp_tr.y = cp_c.y = transform.inverseY(rectF.top);
        cp_bl.y = cp_br.y = transform.inverseY(rectF.bottom);
    }

    private void drawMovingControlPoint(Canvas canvas, ControlPoint p) {
        float x = p.screenX();
        float y = p.screenY();
        Bitmap icBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.writing_textbox_move);
        canvas.drawBitmap(icBitmap, x - MOVING_CONTROL_POINT_BITMAP_SIZE / 2, y - MOVING_CONTROL_POINT_BITMAP_SIZE / 2, null);
    }

    private void drawResizeControlPoint(Canvas canvas, ControlPoint p) {
        float x = p.screenX();
        float y = p.screenY();
        Bitmap icBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.writing_textbox_resize);
        canvas.drawBitmap(icBitmap, x - RESIZE_CONTROL_POINT_BITMAP_SIZE / 2, y - RESIZE_CONTROL_POINT_BITMAP_SIZE / 2, null);
    }

    public void resizeGraphics(int newWidth, int newHeight) {
        computeScreenRect();
        reLocateControlPoint();

        if (newWidth > 0) {
            if ((cp_tl.screenX() + Global.VERTICAL_TOOLBOX_WIDTH_RUN_TIME + newWidth) > Global.SCREEN_WIDTH) {
                cp_tl.x = cp_tl.x - transform.inverseX(newWidth);
                cp_bl.x = cp_bl.x - transform.inverseX(newWidth);
            }
            cp_tr.x = cp_tl.x + transform.inverseX(newWidth);
            cp_br.x = cp_bl.x + transform.inverseX(newWidth);
        }
        if (newHeight > 0) {
            if ((cp_tl.screenY() + Global.HORIZONTAL_TOOLBOX_HEIGHT_RUN_TIME + newHeight) > Global.SCREEN_HEIGHT) {
                cp_tl.y = cp_tl.y - transform.inverseY(newHeight);
                cp_tr.y = cp_tr.y - transform.inverseY(newHeight);
            }
            cp_bl.y = cp_tl.y + transform.inverseY(newHeight);
            cp_br.y = cp_tr.y + transform.inverseY(newHeight);
        }
        computeScreenRect();
        reLocateControlPoint();
        recompute_bounding_box = true;
    }
}
