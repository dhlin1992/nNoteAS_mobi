package ntx.note;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import name.vbraun.view.write.Graphics.Tool;
import name.vbraun.view.write.Paper;
import name.vbraun.view.write.Stroke;
import name.vbraun.view.write.TextBox;
import ntx.note2.R;

public class ToolboxConfiguration {

    public @interface PenColor {
        int BLACK = 1;
        int DARK_GRAY = 2;
        int GRAY = 3;
        int LIGHT_GRAY = 4;
        int WHITE = 5;
        int CHARCOAL = 6;
    }

    public @interface PenStyle {
        int PENCIL = 1;
        int FOUNTAINPEN = 2;
        int BRUSH = 3;
        int LINE = 4;
        int RECTANGLE = 5;
        int OVAL = 6;
        int TRIANGLE = 7;
    }

    public @interface PageBackground {
        int BLANK = 1;
        int NARROW = 2;
        int COLLEGE = 3;
        int CORNELL = 4;
        int TODO = 5;
        int MEETING = 6;
        int DIARY = 7;
        int QUADRANGLE = 8;
        int STAVE = 9;
        int CALLIGRAPHY_SMALL = 10;
        int CALLIGRAPHY_BIG = 11;
        int CUSTOMIZED = 12;
    }

    public @interface TextColor {
        int BLACK = 1;
        int DARK_GRAY = 2;
        int GRAY = 3;
        int LIGHT_GRAY = 4;
        int WHITE = 5;
    }

    private int mCurrentTool = Tool.PENCIL;
    private int mPrevTool = Tool.PENCIL;
    private int mCurrentToolViewId = R.id.btn_toolbox_pen_style;
    private int mKeepSelectedToolViewId = R.id.btn_toolbox_pen_style;
    private int mPrevSelectedToolViewId = R.id.btn_toolbox_pen_style;

    private int mPenThickness = Global.PENCIL_MIN_THICKNESS;
    private int mPenColor = PenColor.BLACK;
    private int mPenStyle = PenStyle.PENCIL;
    private int mPageBackground = PageBackground.BLANK;
    private boolean mIsToolbarExpand = false;
    private boolean mIsToolbarAtLeft = true;
    private boolean mIsPageCheckedQuickTag = false;
    private boolean mShowMemoTheme = true;

    private int mTextBoxFontSize = TextBox.DEFAULT_FONT_SIZE;
    private int mTextBoxColor = TextColor.BLACK;
    private boolean mTextBoxIsBold = false;
    private boolean mTextBoxIsItalic = false;
    private boolean mTextBoxIsUnderLine = false;

    private String mPageBackgroundUserDefined1 = "";
    private String mPageBackgroundUserDefined2 = "";
    private String mPageBackgroundUserDefined3 = "";
    private String mPageBackgroundUserDefined4 = "";

    private EventBus mEventBus;

    private ToolboxConfiguration() {
        mEventBus = EventBus.getDefault();
    }

    public static ToolboxConfiguration getInstance() {
        return ToolboxConfigurationHolder.mInstance;
    }

    private static class ToolboxConfigurationHolder {
        private static final ToolboxConfiguration mInstance = new ToolboxConfiguration();
    }

    public void loadSettings(SharedPreferences settings) {
        synchronized (ToolboxConfiguration.class) {
            mCurrentTool = settings.getInt(Preferences.KEY_PEN_TYPE, Tool.PENCIL);
            mPenThickness = settings.getInt(Preferences.KEY_PEN_THICKNESS, Global.PENCIL_MIN_THICKNESS);
            switch (settings.getInt(Preferences.KEY_PEN_COLOR, Color.BLACK)) {
                case Color.BLACK:
                    mPenColor = PenColor.BLACK;
                    break;
                case Stroke.DARK_GRAY:
                    mPenColor = PenColor.DARK_GRAY;
                    break;
                case Stroke.GRAY:
                    mPenColor = PenColor.GRAY;
                    break;
                case Stroke.LIGHT_GRAY:
                    mPenColor = PenColor.LIGHT_GRAY;
                    break;
                case Color.WHITE:
                    mPenColor = PenColor.WHITE;
                    break;
                case Stroke.CHARCOAL:
                    mPenColor = PenColor.CHARCOAL;
                    break;
                default:
                    mPenColor = PenColor.BLACK;
                    break;
            }

            if (mCurrentTool == Tool.PENCIL || mCurrentTool == Tool.FOUNTAINPEN || mCurrentTool == Tool.BRUSH
                    || mCurrentTool == Tool.LINE || mCurrentTool == Tool.RECTANGLE || mCurrentTool == Tool.OVAL || mCurrentTool == Tool.TRIANGLE) {
                mCurrentToolViewId = R.id.btn_toolbox_pen_style;

                switch (mCurrentTool) {
                    case Tool.PENCIL:
                        mPenStyle = PenStyle.PENCIL;
                        break;
                    case Tool.FOUNTAINPEN:
                        mPenStyle = PenStyle.FOUNTAINPEN;
                        break;
                    case Tool.BRUSH:
                        mPenStyle = PenStyle.BRUSH;
                        break;
                    case Tool.LINE:
                        mPenStyle = PenStyle.LINE;
                        break;
                    case Tool.RECTANGLE:
                        mPenStyle = PenStyle.RECTANGLE;
                        break;
                    case Tool.OVAL:
                        mPenStyle = PenStyle.OVAL;
                        break;
                    case Tool.TRIANGLE:
                        mPenStyle = PenStyle.TRIANGLE;
                        break;
                    default:
                        break;
                }

            } else if (mCurrentTool == Tool.ERASER) {
                mCurrentToolViewId = R.id.btn_toolbox_eraser_line;
            } else if (mCurrentTool == Tool.TEXT) {
                mCurrentToolViewId = R.id.btn_toolbox_text;
            } else if (mCurrentTool == Tool.IMAGE) {
                mCurrentToolViewId = R.id.btn_toolbox_plugin_image;
            } else if (mCurrentTool == Tool.NOOSE) {
                mCurrentToolViewId = R.id.btn_toolbox_noose;
            } else if (mCurrentTool == Tool.POLYGON_NOOSE) {
                mCurrentToolViewId = R.id.btn_toolbox_polygon;
            }

        }
        /**
         * MobiJP always expand toolbar
         */
        // mIsToolbarExpand = settings.getBoolean(Preferences.KEY_TOOLBAR_EXPAND, false);
        mIsToolbarExpand = true;
        mIsToolbarAtLeft = settings.getBoolean(Preferences.KEY_TOOLBOX_IS_ON_LEFT, true);
        mShowMemoTheme = settings.getBoolean(Preferences.KEY_PAGE_TITLE_SHOW_MEMO, true);

        mPageBackgroundUserDefined1 = settings.getString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_1, "");
        mPageBackgroundUserDefined2 = settings.getString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_2, "");
        mPageBackgroundUserDefined3 = settings.getString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_3, "");
        mPageBackgroundUserDefined4 = settings.getString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_4, "");

        mTextBoxFontSize = settings.getInt(Preferences.KEY_TEXT_BOX_FONT_SIZE, TextBox.DEFAULT_FONT_SIZE);
        mTextBoxColor = settings.getInt(Preferences.KEY_TEXT_BOX_COLOR, TextColor.BLACK);
        mTextBoxIsBold = settings.getBoolean(Preferences.KEY_TEXT_BOX_IS_BOLD, false);
        mTextBoxIsItalic = settings.getBoolean(Preferences.KEY_TEXT_BOX_IS_ITALIC, false);
        mTextBoxIsUnderLine = settings.getBoolean(Preferences.KEY_TEXT_BOX_IS_UNDER_LINE, false);

        CallbackEvent event = new CallbackEvent();
        event.setMessage(CallbackEvent.UPDATE_PAGE_TITLE);
        mEventBus.post(event);
        mEventBus.post(this);
    }

    public void saveSettings(SharedPreferences.Editor editor) {
        if (mCurrentTool == Tool.ERASER) {
            editor.putInt(Preferences.KEY_PEN_TYPE, mPrevTool);
        } else {
            editor.putInt(Preferences.KEY_PEN_TYPE, mCurrentTool);
        }
        editor.putInt(Preferences.KEY_PEN_COLOR, getPenColorInRGB());
        editor.putInt(Preferences.KEY_PEN_THICKNESS, mPenThickness);
        editor.putBoolean(Preferences.KEY_TOOLBAR_EXPAND, mIsToolbarExpand);
        editor.putBoolean(Preferences.KEY_TOOLBOX_IS_ON_LEFT, mIsToolbarAtLeft);
        editor.putBoolean(Preferences.KEY_PAGE_TITLE_SHOW_MEMO, mShowMemoTheme);

        editor.putString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_1, mPageBackgroundUserDefined1);
        editor.putString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_2, mPageBackgroundUserDefined2);
        editor.putString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_3, mPageBackgroundUserDefined3);
        editor.putString(Preferences.KEY_PAPER_BACKGROUND_USER_DEFINED_4, mPageBackgroundUserDefined4);

        editor.putInt(Preferences.KEY_TEXT_BOX_FONT_SIZE, mTextBoxFontSize);
        editor.putInt(Preferences.KEY_TEXT_BOX_COLOR, mTextBoxColor);
        editor.putBoolean(Preferences.KEY_TEXT_BOX_IS_BOLD, mTextBoxIsBold);
        editor.putBoolean(Preferences.KEY_TEXT_BOX_IS_ITALIC, mTextBoxIsItalic);
        editor.putBoolean(Preferences.KEY_TEXT_BOX_IS_UNDER_LINE, mTextBoxIsUnderLine);
    }

    public void setCurrentTool(@Tool int tool) {
        synchronized (ToolboxConfiguration.class) {
            if (tool != mCurrentTool)
                mPrevTool = mCurrentTool;

            this.mCurrentTool = tool;
        }
        CallbackEvent callbackEvent = new CallbackEvent();
        callbackEvent.setMessage(CallbackEvent.DO_DRAW_VIEW_INVALIDATE);
        mEventBus.post(callbackEvent);
        mEventBus.post(this);
    }

    public void setCurrentToolViewId(int viewId, boolean isKeepSelected) {
        synchronized (ToolboxConfiguration.class) {
            this.mCurrentToolViewId = viewId;

            if (isKeepSelected) {
                if (viewId != mKeepSelectedToolViewId)
                    this.mPrevSelectedToolViewId = mKeepSelectedToolViewId;

                this.mKeepSelectedToolViewId = viewId;
            }
        }
        mEventBus.post(this);
    }

    public void setKeepSelectedToolViewId(int viewId) {
        if (viewId == mKeepSelectedToolViewId)
            return;
        synchronized (ToolboxConfiguration.class) {
            this.mKeepSelectedToolViewId = viewId;
        }
    }

    public void setPenThickness(int thickness) {
        if (thickness == mPenThickness)
            return;
        synchronized (ToolboxConfiguration.class) {
            this.mPenThickness = thickness;
        }
        mEventBus.post(this);
    }

    public void setPenColor(int color) {
        if (color == mPenColor)
            return;
        synchronized (ToolboxConfiguration.class) {
            this.mPenColor = color;
        }
        mEventBus.post(this);
    }

    public void setPenStyle(int penStyle) {
        synchronized (ToolboxConfiguration.class) {
            this.mPenStyle = penStyle;
            @Tool int setTool = Tool.PENCIL;
            switch (mPenStyle) {
                case PenStyle.PENCIL:
                    setTool = Tool.PENCIL;
                    break;
                case PenStyle.FOUNTAINPEN:
                    setTool = Tool.FOUNTAINPEN;
                    break;
                case PenStyle.BRUSH:
                    setTool = Tool.BRUSH;
                    break;
                case PenStyle.LINE:
                    setTool = Tool.LINE;
                    break;
                case PenStyle.RECTANGLE:
                    setTool = Tool.RECTANGLE;
                    break;
                case PenStyle.OVAL:
                    setTool = Tool.OVAL;
                    break;
                case PenStyle.TRIANGLE:
                    setTool = Tool.TRIANGLE;
                    break;
                default:
                    break;
            }
            setCurrentTool(setTool);
        }
    }

    public void setPageBackground(Paper.Type paperType) {
        int pageBackground;
        switch (paperType) {
            case EMPTY:
                pageBackground = PageBackground.BLANK;
                break;
            case QUAD:
                pageBackground = PageBackground.QUADRANGLE;
                break;
            case COLLEGERULED:
                pageBackground = PageBackground.COLLEGE;
                break;
            case NARROWRULED:
                pageBackground = PageBackground.NARROW;
                break;
            case CORNELLNOTES:
                pageBackground = PageBackground.CORNELL;
                break;
            case CALLIGRAPHY_SMALL:
                pageBackground = PageBackground.CALLIGRAPHY_SMALL;
                break;
            case CALLIGRAPHY_BIG:
                pageBackground = PageBackground.CALLIGRAPHY_BIG;
                break;
            case TODOLIST:
                pageBackground = PageBackground.TODO;
                break;
            case MINUTES:
                pageBackground = PageBackground.MEETING;
                break;
            case STAVE:
                pageBackground = PageBackground.STAVE;
                break;
            case DIARY:
                pageBackground = PageBackground.DIARY;
                break;
            case CUSTOMIZED:
                pageBackground = PageBackground.CUSTOMIZED;
                break;
            default:
                pageBackground = PageBackground.BLANK;
                break;
        }

        if (pageBackground == mPageBackground)
            return;

        synchronized (ToolboxConfiguration.class) {
            this.mPageBackground = pageBackground;
        }
    }

    public void setToolbarExpand(boolean isExpand) {
        synchronized (ToolboxConfiguration.class) {
            this.mIsToolbarExpand = isExpand;
        }
        mEventBus.post(this);
    }

    public void setToolbarAtLeft(boolean isLeft) {
        synchronized (ToolboxConfiguration.class) {
            this.mIsToolbarAtLeft = isLeft;
        }
        CallbackEvent event = new CallbackEvent();
        event.setMessage(CallbackEvent.SWITCH_VERTICAL_TOOLBAR);
        mEventBus.post(event);
        mEventBus.post(this);
    }

    public void setPageCheckedQuickTag(boolean isChecked) {
        synchronized (ToolboxConfiguration.class) {
            this.mIsPageCheckedQuickTag = isChecked;
        }
        mEventBus.post(this);
    }

    public void setShowMemoTheme(boolean isShow) {
        synchronized (ToolboxConfiguration.class) {
            this.mShowMemoTheme = isShow;
        }
        CallbackEvent event = new CallbackEvent();
        event.setMessage(CallbackEvent.UPDATE_PAGE_TITLE);
        mEventBus.post(event);
        mEventBus.post(this);
    }

    public void setPageBackgroundUserDefined1(String pageBackgroundUserDefined) {
        synchronized (ToolboxConfiguration.class) {
            this.mPageBackgroundUserDefined1 = pageBackgroundUserDefined;
        }
    }

    public void setPageBackgroundUserDefined2(String pageBackgroundUserDefined) {
        synchronized (ToolboxConfiguration.class) {
            this.mPageBackgroundUserDefined2 = pageBackgroundUserDefined;
        }
    }

    public void setPageBackgroundUserDefined3(String pageBackgroundUserDefined) {
        synchronized (ToolboxConfiguration.class) {
            this.mPageBackgroundUserDefined3 = pageBackgroundUserDefined;
        }
    }

    public void setPageBackgroundUserDefined4(String pageBackgroundUserDefined) {
        synchronized (ToolboxConfiguration.class) {
            this.mPageBackgroundUserDefined4 = pageBackgroundUserDefined;
        }
    }

    public void setTextBoxSettingValue(@Nullable Integer _textFontSize, @Nullable Integer _textColor,
                                       @Nullable Boolean _isBold, @Nullable Boolean _isItalic, @Nullable Boolean _isUnderLine) {
        synchronized (ToolboxConfiguration.class) {
            if (_textFontSize != null)
                mTextBoxFontSize = _textFontSize;

            if (_textColor != null)
                mTextBoxColor = _textColor;

            if (_isBold != null)
                mTextBoxIsBold = _isBold;

            if (_isItalic != null)
                mTextBoxIsItalic = _isItalic;

            if (_isUnderLine != null)
                mTextBoxIsUnderLine = _isUnderLine;
        }
    }

    public @Tool
    int getCurrentTool() {
        synchronized (ToolboxConfiguration.class) {
            return this.mCurrentTool;
        }
    }

    public @Tool
    int getPrevTool() {
        synchronized (ToolboxConfiguration.class) {
            return this.mPrevTool;
        }
    }

    public int getCurrentToolViewId() {
        synchronized (ToolboxConfiguration.class) {
            return this.mCurrentToolViewId;
        }
    }

    public int getKeepSelectedToolViewId() {
        synchronized (ToolboxConfiguration.class) {
            return this.mKeepSelectedToolViewId;
        }
    }

    public int getPrevSelectedToolViewId() {
        synchronized (ToolboxConfiguration.class) {
            return this.mPrevSelectedToolViewId;
        }
    }

    public int getPenThickness() {
        synchronized (ToolboxConfiguration.class) {
            return this.mPenThickness;
        }
    }

    public int getPenColor() {
        synchronized (ToolboxConfiguration.class) {
            return this.mPenColor;
        }
    }

    public int getPenColorInRGB() {
        synchronized (ToolboxConfiguration.class) {
            switch (mPenColor) {
                case PenColor.BLACK:
                    return Color.BLACK;
                case PenColor.DARK_GRAY:
                    return Stroke.DARK_GRAY;
                case PenColor.GRAY:
                    return Stroke.GRAY;
                case PenColor.LIGHT_GRAY:
                    return Stroke.LIGHT_GRAY;
                case PenColor.WHITE:
                    return Color.WHITE;
                case PenColor.CHARCOAL:
                    return Stroke.CHARCOAL;
                default:
                    return Color.BLACK;
            }
        }
    }

    public int getPenStyle() {
        synchronized (ToolboxConfiguration.class) {
            return this.mPenStyle;
        }
    }

    public int getPageBackground() {
        synchronized (ToolboxConfiguration.class) {
            return this.mPageBackground;
        }
    }

    public boolean isToolbarExpand() {
        /**
         * MobiJP always expand toolbar
         */
        /*
        synchronized (ToolboxConfiguration.class) {
            return this.mIsToolbarExpand;
        }
        */
        return true;
    }

    public boolean isToolbarAtLeft() {
        synchronized (ToolboxConfiguration.class) {
            return this.mIsToolbarAtLeft;
        }
    }

    public boolean isPageCheckedQuickTag() {
        synchronized (ToolboxConfiguration.class) {
            return this.mIsPageCheckedQuickTag;
        }
    }

    public boolean showMemoTheme() {
        synchronized (ToolboxConfiguration.class) {
            return this.mShowMemoTheme;
        }
    }

    public String getPageBackgroundUserDefined1() {
        synchronized (ToolboxConfiguration.class) {
            return mPageBackgroundUserDefined1;
        }
    }

    public String getPageBackgroundUserDefined2() {
        synchronized (ToolboxConfiguration.class) {
            return mPageBackgroundUserDefined2;
        }
    }

    public String getPageBackgroundUserDefined3() {
        synchronized (ToolboxConfiguration.class) {
            return mPageBackgroundUserDefined3;
        }
    }

    public String getPageBackgroundUserDefined4() {
        synchronized (ToolboxConfiguration.class) {
            return mPageBackgroundUserDefined4;
        }
    }

    public int getTextBoxFontSize() {
        return mTextBoxFontSize;
    }

    public int getTextBoxColor() {
        return mTextBoxColor;
    }

    public boolean isTextBoxBold() {
        return mTextBoxIsBold;
    }

    public boolean isTextBoxItalic() {
        return mTextBoxIsItalic;
    }

    public boolean isTextBoxUnderLine() {
        return mTextBoxIsUnderLine;
    }
}
