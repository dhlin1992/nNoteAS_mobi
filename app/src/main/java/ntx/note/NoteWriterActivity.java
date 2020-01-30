package ntx.note;

import android.app.Application;
import android.app.ApplicationErrorReport;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;

import name.vbraun.filepicker.RestoreDialogFragment;
import name.vbraun.lib.pen.Hardware;
import name.vbraun.lib.pen.HideBar;
import name.vbraun.lib.pen.PenEventNTX;
import name.vbraun.view.write.BackgroundView;
import name.vbraun.view.write.FastView;
import name.vbraun.view.write.Graphics;
import name.vbraun.view.write.GraphicsImage;
import name.vbraun.view.write.GraphicsLine;
import name.vbraun.view.write.GraphicsOval;
import name.vbraun.view.write.GraphicsRectangle;
import name.vbraun.view.write.GraphicsTriangle;
import name.vbraun.view.write.HandwriterView;
import name.vbraun.view.write.InsertImageView;
import name.vbraun.view.write.Page;
import name.vbraun.view.write.Paper;
import name.vbraun.view.write.Stroke;
import name.vbraun.view.write.TextBox;
import name.vbraun.view.write.TouchHandlerPenABC;
import ntx.draw.nDrawHelper;
import ntx.note.RelativePopupWindow.HorizontalPosition;
import ntx.note.RelativePopupWindow.VerticalPosition;
import ntx.note.bookshelf.CopyPageDialogFragment;
import ntx.note.bookshelf.DateNoteData;
import ntx.note.bookshelf.InformationDialogFragment;
import ntx.note.bookshelf.RenameNoteDialogFragment;
import ntx.note.data.Book;
import ntx.note.data.Bookshelf;
import ntx.note.data.Storage;
import ntx.note.data.TagManager;
import ntx.note.export.AlertDialogButtonClickListener;
import ntx.note.export.AlertDialogFragment;
import ntx.note.export.BackupDialogFragment;
import ntx.note.export.ConvertDialogFragment;
import ntx.note.export.DismissDelayPostAlertDialogFragment;
import ntx.note.image.ImageActivity;
import ntx.note.image.ImagePickerActivity;
import ntx.note.tag.TagDialogFragment;
import ntx.note.thumbnail.ThumbnailDialogFragment;
import ntx.note2.R;
import utility.HomeWatcher;
import utility.TextDialog;

import static name.vbraun.view.write.HandwriterView.KEY_PEN_OFFSET_X;
import static name.vbraun.view.write.HandwriterView.KEY_PEN_OFFSET_Y;
import static name.vbraun.view.write.HandwriterView.PAGE_REDRAW_TIME_THRESHOLD;
import static ntx.note.Global.APP_DATA_PACKAGE_FILES_PATH;
import static ntx.note.Global.NOTEBOOK_DIRECTORY_PREFIX;
import static ntx.note.data.Bookshelf.NullBook;

public class NoteWriterActivity extends ActivityAsyncBase
        implements name.vbraun.view.write.InputListener, AlertDialogButtonClickListener, View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "NoteWriterActivity";

    public static final String DRAWING_ALERT_DIALOG_TAG = "drawing_alert_dialog";

    private static final String DELETE_PAGE_ALERT_DIALOG_TAG = "delete_page_alert_dialog";
    private static final String CLEAN_PAGE_ALERT_DIALOG_TAG = "clean_page_alert_dialog";
    private static final String EMPTY_PAGE_ALERT_DIALOG_TAG = "empty_page_alert_dialog";
    private static final String MAX_PAGE_ALERT_DIALOG_TAG = "max_page_alert_dialog";
    public static final String MAX_IMAGE_ALERT_DIALOG_TAG = "max_image_alert_dialog";
    public static final String SAVE_ALERT_DIALOG_TAG = "save_alert_dialog";
    private static final String SAVE_FAIL_ALERT_DIALOG_TAG = "save_fail_alert_dialog";
    private static final String CONVERT_SUCCESS_DIALOG_TAG = "convert_success_dialog";
    private static final String OOM_DIALOG_TAG = "oom_dialog";

    private static final int PAGE_MAX_SIZE = 200; // modify page max size from 100 to 200

    private final static int REQUEST_REPORT_BACK_KEY = 1;
    private final static int REQUEST_PICK_IMAGE = 2;
    private final static int REQUEST_EDIT_IMAGE = 3;
    public final static int REQUEST_PEN_OFFSET = 4;
    private final static int REQUEST_BG_PICK_IMAGE = 5;
    public final static int REQUEST_PICK_IMAGE_OOM = 6;
    public final static int REQUEST_USER_DEFINE_BG_1_PICK_IMAGE = 7;
    public final static int REQUEST_USER_DEFINE_BG_2_PICK_IMAGE = 8;
    public final static int REQUEST_USER_DEFINE_BG_3_PICK_IMAGE = 9;
    public final static int REQUEST_USER_DEFINE_BG_4_PICK_IMAGE = 10;

    private static boolean noteEdited = false; // if edited, note can be saved.

    private Context mContext;

    private HomeWatcher mHomeWatcher = new HomeWatcher(this);

    private HandwriterView mHandwriterView;
    private BackgroundView mBackgroundView;
    private InsertImageView mInsertImageView;
    private FastView mFastView;

    private FrameLayout mDrawViewLayout;
    private LinearLayout mToolbox_vertical_left;
    private LinearLayout mToolbox_vertical_right;
    private LinearLayout mToolbox_horizontal;
    private LinearLayout mToolbox_normal_view_layout;

    private Button mBtnPageTitle;
    private Button mBtnPageNumber;


    private ImageButton mBtnUndo;
    private ImageButton mBtnRedo;

    private ImageButton mBtnPrevPage;
    private ImageButton mBtnOverview;

    private Toast mToast;
    private Handler mHandler = new Handler();

    private boolean mSharePrefSetting_isHideSystemBar;
    private boolean mSaveRunning = false;
    private boolean mCleanRunning = false;
    private boolean mDeleteRunning = false;
    private boolean mIsSettingPageBackgroundRequest = false;
    private String mStrPageBackgroundPath = "na";

    private EventBus mEventBus;

    private FileOperationsPopupWindow mFileOperationsPopupWindow;
    private PagePopupWindow mPagePopupWindow;
    private boolean mIsToolboxShown = true;

    private ArrayList mClose_nDraw_layout;
    private String mStrOpenFolderPath;
    private FrameLayout alert_dialog_container, dialog_container;

    private boolean mIsCreateNote;
    private static final long DROP_TIME_DEFAULT = 0L;

    private Runnable mRunnableInvalidateToolbox = new Runnable() {
        @Override
        public void run() {
            mToolbox_horizontal.invalidate(PenEventNTX.UPDATE_MODE_PARTIAL_GC16);
            if (ToolboxConfiguration.getInstance().isToolbarAtLeft())
                mToolbox_vertical_left.invalidate(PenEventNTX.UPDATE_MODE_PARTIAL_GC16);
            else
                mToolbox_vertical_right.invalidate(PenEventNTX.UPDATE_MODE_PARTIAL_GC16);
        }
    };

    private TextDialog SaveDialog = null;
    private Timer autoSaveTimer;
    private int autoSaveTime = 30000;
    private DisplayMetrics dm;
    private int toolbox_vertical_width;
    private String intentFrom; //Dylan : Use for what I know this intent where come from ?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dm = getResources().getDisplayMetrics();
        boolean isLandscape = getIntent().getBooleanExtra("IsLandscape", false);
        if (isLandscape && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else if (!isLandscape && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        initBookshelf(getIntent());

        mEventBus = EventBus.getDefault();
        mContext = getApplicationContext();
        Global.setResources(mContext);
        Global.prepareBitmapShader();
        mClose_nDraw_layout = new ArrayList();
        mClose_nDraw_layout.add(R.id.btn_toolbox_back);
        mClose_nDraw_layout.add(R.id.btn_note_title);
        mClose_nDraw_layout.add(R.id.btn_toolbox_create_page);
        mClose_nDraw_layout.add(R.id.btn_toolbox_delete_page);
        mClose_nDraw_layout.add(R.id.btn_toolbox_undo);
        mClose_nDraw_layout.add(R.id.btn_toolbox_redo);
        mClose_nDraw_layout.add(R.id.btn_toolbox_full_refresh);
        mClose_nDraw_layout.add(R.id.btn_toolbox_prev_page);
        mClose_nDraw_layout.add(R.id.btn_page_number);
        mClose_nDraw_layout.add(R.id.btn_toolbox_next_page);
        mClose_nDraw_layout.add(R.id.btn_toolbox_overview);
        mClose_nDraw_layout.add(R.id.btn_toolbox_normal_view);
        mClose_nDraw_layout.add(R.id.ll_toolbox_horizontal);

        Hardware.getInstance(getApplicationContext());

        /**
         * HandwriterView instance and initialization
         */
        mBackgroundView = BackgroundView.getInstance(this);
        mInsertImageView = InsertImageView.getInstance(this);
        mHandwriterView = new HandwriterView(this);
        mHandwriterView.setOnGraphicsModifiedListener(UndoManager.getUndoManager());
        mHandwriterView.setOnInputListener(this);
        mHandwriterView.getBottomHeight(getNavigationBarHeight());
        mHandwriterView.setPageAndZoomOut(Global.getCurrentBook().currentPage(), false);

        initViews();

        setKeepScreenOn();

        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                mHomeWatcher.stopWatch();
                if (getNoteEdited()) {
                    saveDialog(true);
                } else
                    finish();
            }

            @Override
            public void onHomeLongPressed() {
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initBookshelf(intent);
    }

    @Override
    protected void onResume() {
        mHomeWatcher.startWatch();
        changeEinkControlPermission(true);
        resetEinkMode();
        mHandwriterView.stopInput();
        UndoManager.setApplication(this);
        super.onResume();

        Global.closeWaitDialog(this);

        /**
         * Save dialog instance and initialization
         */
        SaveDialog = new TextDialog(this, getResources().getString(R.string.saving));
        SaveDialog.setCanceledOnTouchOutside(false);

        if (!mEventBus.isRegistered(this))
            mEventBus.register(this);

        if (Global.getCurrentBook().isLandscape()) {
            ToolboxViewBuilder toolboxLeft = new ToolboxViewBuilder(this, R.layout.toolbox_left_landscape);
            mToolbox_vertical_left.removeAllViews();
            mToolbox_vertical_left.addView(toolboxLeft);
        }

        if (Global.getCurrentBook().isLandscape()) {
            ToolboxViewBuilder toolboxRight = new ToolboxViewBuilder(this, R.layout.toolbox_right_landscape);
            mToolbox_vertical_right.removeAllViews();
            mToolbox_vertical_right.addView(toolboxRight);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        ToolboxConfiguration toolboxConfiguration = ToolboxConfiguration.getInstance();
        toolboxConfiguration.loadSettings(settings);
        mHandwriterView.loadSettings(settings);
        if (mIsToolboxShown) {
            if (toolboxConfiguration.isToolbarAtLeft()) {
                mToolbox_vertical_left.setVisibility(View.VISIBLE);
                mToolbox_vertical_right.setVisibility(View.GONE);
            } else {
                mToolbox_vertical_left.setVisibility(View.GONE);
                mToolbox_vertical_right.setVisibility(View.VISIBLE);
            }

            // Karote 20180921 : wake up issue - Toolbox areas are blank.
            mHandler.postDelayed(mRunnableInvalidateToolbox, 500);
        }

        mSharePrefSetting_isHideSystemBar = settings.getBoolean(Preferences.KEY_HIDE_SYSTEM_BAR, false);
        if (mSharePrefSetting_isHideSystemBar)
            HideBar.hideSystembar(getApplicationContext());

        if (Global.getCurrentBook() != NullBook) {
            switchToPage(Global.getCurrentBook().currentPage());
        } else {
            // Error: can not get current book.
            finish();
        }

        initToolboxSize();

        updateUndoRedoIcons();
        setKeepScreenOn();
        mHandwriterView.startInput();

        // Daniel 20181017 : enable 2-step-suspend
//        PowerEnhanceSet(1);
        nDrawHelper.NDrawInit(this);
        /**
         * 2019.03.13 Karote
         * post event for set the correct parameter to nDraw
         */
        mEventBus.post(toolboxConfiguration);


        /**
         * Setup book initialization
         */
        Global.getCurrentBook().setOnBookModifiedListener(UndoManager.getUndoManager());

        /**
         * Pre-setting the popup window location.
         */
        if (mToolbox_vertical_left.getVisibility() == View.VISIBLE)
            toolbox_vertical_width = mToolbox_vertical_left.getLayoutParams().width;
        else
            toolbox_vertical_width = mToolbox_vertical_right.getLayoutParams().width;
    }

    @Override
    protected void onPause() {
        if (autoSaveTimer != null)
            autoSaveTimer.cancel();

        changeEinkControlPermission(false);
        Log.d(TAG, "NWA<---onPause");
        mHandwriterView.stopInput();
        if (mSharePrefSetting_isHideSystemBar)
            HideBar.showSystembar(getApplicationContext());
        super.onPause();
        mHandwriterView.onPause();
        mHandwriterView.interrupt();

        saveToolBoxSetting(true);
        UndoManager.setApplication(null);
        // Daniel 20181017 : disable 2-step-suspend
//		PowerEnhanceSet(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
        nDrawHelper.NDrawSwitch(false);
        nDrawHelper.NDrawUnInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(this, NoteLibraryService.class));
    }

    @Override
    public void finish() {
        if (mHandwriterView != null)
            mHandwriterView.onFinish();
        saveToolBoxSetting(false);
        if (autoSaveTimer != null)
            autoSaveTimer.cancel();
//        stopService(new Intent(this, NoteLibraryService.class));
        if (intentFrom != null && intentFrom.equals("date")) {
            Global.openWaitDialog(this);
            try {
                ComponentName componentName = new ComponentName(Global.CALENDAR_PACKAGE, Global.CALENDAR_CLASS);
                Intent mIntent = new Intent();
                startActivity(mIntent.setComponent(componentName).setAction(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                startActivity(mIntent);
            } catch (Throwable e) {
                Global.closeWaitDialog(this);
                Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
            }
        }

        if (intentFrom != null && intentFrom.equals("home")) {
            sendBroadcast(new Intent("Ntx.InputKeyEvent").putExtra("KeyCode", KeyEvent.KEYCODE_HOME));
        }
//        Global.setCurrentBook(Bookshelf.NullBook); Dylan : Has been closed, the setting is meaningless
        android.os.Process.killProcess(android.os.Process.myPid());
        super.finish();
    }

    @Override
    protected void onInitializationFinished() {

        /**
         * Quick open by JSON file.
         */
        if (!mIsCreateNote && !Global.getCurrentBook().isAllowSave()) {
            Global.setCurrentBook(Bookshelf.getInstance().getTempCurrentBook());
            Global.getCurrentBook().replaceCurrentPage(mHandwriterView.getPage());
            mHandwriterView.setPageAndZoomOut(Global.getCurrentBook().currentPage(), false);
        }

        /**
         * auto save timer ON
         */
        if (autoSaveTimer != null)
            autoSaveTimer.cancel();
        autoSaveTimer = new Timer(false);
        autoSaveTimer.schedule(new AutoSaveTimerTask(), autoSaveTime, autoSaveTime);

        Global.getCurrentBook().setOnBookModifiedListener(UndoManager.getUndoManager());
        Storage.getInstance().saveCurrentBookUUID(Global.getCurrentBook().getUUID());
        super.onInitializationFinished();
    }

    @Override
    protected void onInitializationReload() {
        super.onInitializationReload();
        if (Global.getCurrentBook() == null || Global.getCurrentBook() == NullBook || mHandwriterView == null || mHandwriterView.getPage() == null)
            return;
        Global.getCurrentBook().replaceCurrentPage(mHandwriterView.getPage());
        mHandwriterView.setPageAndZoomOut(Global.getCurrentBook().currentPage(), false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        switch (requestCode) {
            case REQUEST_REPORT_BACK_KEY:
                if (resultCode != RESULT_OK)
                    return;
                break;

            /**
             * Insert image
             */
            case REQUEST_PICK_IMAGE:
                if (resultCode == REQUEST_PICK_IMAGE_OOM) {
                    showOOMAlertDialogFragment();
                }

                if (resultCode != RESULT_OK) {
                    // Add -> Pick -> Cancel
                    mHandwriterView.removeNowEditedGraphics();
                    UndoManager.getUndoManager().cancelLastCmd();
                    return;
                }
                setNoteEdited(true);
                String uuidStr = data.getStringExtra(ImageActivity.EXTRA_UUID);
                Assert.assertNotNull(uuidStr);
                UUID uuid = UUID.fromString(uuidStr);
                boolean constrain = data.getBooleanExtra(ImageActivity.EXTRA_CONSTRAIN_ASPECT, true);
                String uriStr = data.getStringExtra(ImageActivity.EXTRA_FILE_URI);
                if (uriStr == null) {
                    // 1:Add -> Back
                    // 2:Add -> Pick -> Back
                    mHandwriterView.removeNowEditedGraphics();
                    UndoManager.getUndoManager().cancelLastCmd();
                } else {
                    // Add -> Pick -> OK
                    uri = Uri.parse(uriStr);
                    String name = uri.getPath();
                    mHandwriterView.setImage(uuid, name, constrain);
                }
                break;

            case REQUEST_EDIT_IMAGE:
                if (resultCode == REQUEST_PICK_IMAGE_OOM) {
                    showOOMAlertDialogFragment();
                }

                if (resultCode != RESULT_OK) {
                    // 1.Edit -> Back
                    // 2.Edit -> Cancel
                    // 3.Edit -> Pick -> Back -> Back
                    return;
                }
                setNoteEdited(true);
                uuidStr = data.getStringExtra(ImageActivity.EXTRA_UUID);
                Assert.assertNotNull(uuidStr);
                uuid = UUID.fromString(uuidStr);
                constrain = data.getBooleanExtra(ImageActivity.EXTRA_CONSTRAIN_ASPECT, true);
                uriStr = data.getStringExtra(ImageActivity.EXTRA_FILE_URI);
                uri = Uri.parse(uriStr);
                String name = uri.getPath();
                mHandwriterView.setImage(uuid, name, constrain);
                break;

            /**
             * Background
             */
            case REQUEST_BG_PICK_IMAGE:
            case REQUEST_USER_DEFINE_BG_1_PICK_IMAGE:
            case REQUEST_USER_DEFINE_BG_2_PICK_IMAGE:
            case REQUEST_USER_DEFINE_BG_3_PICK_IMAGE:
            case REQUEST_USER_DEFINE_BG_4_PICK_IMAGE:
                if (resultCode != RESULT_OK)
                    break;

                // set flag
                mIsSettingPageBackgroundRequest = true;

                uri = data.getData();

                if (uri == null) {
                    Log.e(TAG, "Selected image is NULL!");
                    return;
                } else {
                    Log.d(TAG, "Selected image: " + uri);
                }

                mStrPageBackgroundPath = uri.getPath();

                if (REQUEST_USER_DEFINE_BG_1_PICK_IMAGE == requestCode)
                    ToolboxConfiguration.getInstance().setPageBackgroundUserDefined1(mStrPageBackgroundPath);
                else if (REQUEST_USER_DEFINE_BG_2_PICK_IMAGE == requestCode)
                    ToolboxConfiguration.getInstance().setPageBackgroundUserDefined2(mStrPageBackgroundPath);
                else if (REQUEST_USER_DEFINE_BG_3_PICK_IMAGE == requestCode)
                    ToolboxConfiguration.getInstance().setPageBackgroundUserDefined3(mStrPageBackgroundPath);
                else if (REQUEST_USER_DEFINE_BG_4_PICK_IMAGE == requestCode)
                    ToolboxConfiguration.getInstance().setPageBackgroundUserDefined4(mStrPageBackgroundPath);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = settings.edit();
                ToolboxConfiguration.getInstance().saveSettings(editor);
                editor.apply();

                setUpPageBackground(Paper.CUSTOMIZED);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        waitBookshelfInitCompleted(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                backFunc();
                return null;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (RelativePopupWindow.isPopupWindowShowing()) {
            if (ev.getToolType(0) != MotionEvent.TOOL_TYPE_FINGER) {
                CallbackEvent callbackEvent = new CallbackEvent();
                callbackEvent.setMessage(CallbackEvent.DISMISS_POPUPWINDOW);
                mEventBus.post(callbackEvent);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mClose_nDraw_layout.contains(v.getId()) && event.getAction() == MotionEvent.ACTION_DOWN) {
            nDrawHelper.NDrawSwitch(false);
        }
        if (RelativePopupWindow.isPopupWindowShowing()) {
            CallbackEvent callbackEvent = new CallbackEvent();
            callbackEvent.setMessage(CallbackEvent.DISMISS_POPUPWINDOW);
            mEventBus.post(callbackEvent);
            return true;
        }
        return false;
    }

    // The HandWriterView is not focusable and therefore does not receive KeyEvents
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        // Log.v(TAG, "KeyEvent "+action+" "+keyCode);
        /**
         * 2019.09.05 - Disable direction key to flip page.
         *
         switch (keyCode) {
         case KeyEvent.KEYCODE_DPAD_DOWN:
         case KeyEvent.KEYCODE_DPAD_RIGHT:
         case KeyEvent.KEYCODE_VOLUME_DOWN:
         if (action == KeyEvent.ACTION_UP) {
         flip_page_next();
         }
         return true;
         case KeyEvent.KEYCODE_DPAD_UP:
         case KeyEvent.KEYCODE_DPAD_LEFT:
         case KeyEvent.KEYCODE_VOLUME_UP:
         if (action == KeyEvent.ACTION_DOWN) {
         flip_page_prev();
         }
         return true;
         default:
         return super.dispatchKeyEvent(event);
         }
         */
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isOverview())
                return false;
            else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

    @Override
    public boolean onLongClick (View v) {
        switch (v.getId()) {
            case R.id.btn_note_title:
                System.out.println("OnLongClick btn_note_title executed from file NoteWriterActivity.java");
                Fragment renameFragment = RenameNoteDialogFragment.newInstance(Global.getCurrentBook().getUUID(), false);
                showDialogFragment(renameFragment, RenameNoteDialogFragment.class.getSimpleName());
            default:
                break;

        }
        return true;
    }

    @Override
    public void onClick(View v) {
        mDrawViewLayout.invalidate();
        nDrawHelper.NDrawSwitch(false);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            sendFeedBack(e);
        }
        switch (v.getId()) {
            case R.id.btn_toolbox_full_refresh:
                mBackgroundView.invalidate();
                mInsertImageView.invalidate();
                mHandwriterView.invalidate();
                Intent ac = new Intent("ntx.eink_control.QUICK_REFRESH");
                ac.putExtra("updatemode", PenEventNTX.UPDATE_MODE_SCREEN);
                ac.putExtra("commandFromNtxApp", true);
                sendBroadcast(ac);
                break;
            case R.id.btn_toolbox_back:
                waitBookshelfInitCompleted(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        backFunc();
                        return null;
                    }
                });
                break;
            case R.id.btn_toolbox_create_page:
                if (!Global.getCurrentBook().isAllPageReady()) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            createPage();
                            return null;
                        }
                    });
                } else
                    createPage();
                break;
            case R.id.btn_toolbox_delete_page:
                if (!Global.getCurrentBook().isAllPageReady()) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            showDeletePageConfirmDialog();
                            return null;
                        }
                    });
                } else
                    showDeletePageConfirmDialog();
                break;
            case R.id.btn_toolbox_undo:
                undo();
                break;
            case R.id.btn_toolbox_redo:
                redo();
                break;
            case R.id.btn_toolbox_next_page:
                flip_page_next();
                break;
            case R.id.btn_toolbox_prev_page:
                flip_page_prev();
                break;
            case R.id.btn_toolbox_overview:
                mToolbox_vertical_left.setVisibility(View.GONE);
                mToolbox_vertical_right.setVisibility(View.GONE);
                mToolbox_horizontal.setVisibility(View.GONE);
                mToolbox_normal_view_layout.setVisibility(View.VISIBLE);
                Global.HORIZONTAL_TOOLBOX_HEIGHT_RUN_TIME = 0;
                Global.VERTICAL_TOOLBOX_WIDTH_RUN_TIME = 0;
                mIsToolboxShown = false;
                break;
            case R.id.btn_toolbox_normal_view:
                if (ToolboxConfiguration.getInstance().isToolbarAtLeft())
                    mToolbox_vertical_left.setVisibility(View.VISIBLE);
                else
                    mToolbox_vertical_right.setVisibility(View.VISIBLE);
                mToolbox_horizontal.setVisibility(View.VISIBLE);
                mToolbox_normal_view_layout.setVisibility(View.GONE);
                Global.HORIZONTAL_TOOLBOX_HEIGHT_RUN_TIME = Global.HORIZONTAL_TOOLBOX_HEIGHT;
                Global.VERTICAL_TOOLBOX_WIDTH_RUN_TIME = Global.VERTICAL_TOOLBOX_WIDTH;
                mIsToolboxShown = true;
                break;
            case R.id.btn_toolbox_info:
            case R.id.layout_toolbox_info:
                showInfoDialog();
                break;
            case R.id.btn_note_title:
                v.setSelected(true);
                if (!Global.getCurrentBook().isAllPageReady()) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            showFileOperationsPopupWindow();
                            return null;
                        }
                    });
                } else
                    showFileOperationsPopupWindow();
                break;
            case R.id.btn_page_number:
                v.setSelected(true);
                if (!Global.getCurrentBook().isAllPageReady()) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            showPageSeekBarPopupWindow();
                            return null;
                        }
                    });
                } else
                    showPageSeekBarPopupWindow();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPickImageListener(GraphicsImage image) {
        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
        intent.putExtra(ImageActivity.EXTRA_UUID, image.getUuid().toString());
        intent.putExtra(ImageActivity.BLANK_IMAGE, ImageActivity.PICK_TYPE);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onPickImageFromPicassoListener(GraphicsImage image) {
        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
        intent.putExtra(ImageActivity.EXTRA_UUID, image.getUuid().toString());
        intent.putExtra(ImageActivity.BLANK_IMAGE, ImageActivity.PICASSO_TYPE);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onEditImageListener(GraphicsImage image) {
        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
        intent.putExtra(ImageActivity.EXTRA_UUID, image.getUuid().toString());
        intent.putExtra(ImageActivity.EXTRA_CONSTRAIN_ASPECT, image.getConstrainAspect());
        intent.putExtra(ImageActivity.EXTRA_FILE_URI, image.getFileUri().toString());
        intent.putExtra(ImageActivity.BLANK_IMAGE, ImageActivity.EDIT_TYPE);
        startActivityForResult(intent, REQUEST_EDIT_IMAGE);
    }

    @Override
    public void onPositiveButtonClick(String fragmentTag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null)
            getFragmentManager().beginTransaction().remove(fragment).commit();

        if (fragmentTag != null) {
            if (fragmentTag.equals(DELETE_PAGE_ALERT_DIALOG_TAG)) {
                deletePage();
            } else if (fragmentTag.equals(CLEAN_PAGE_ALERT_DIALOG_TAG)) {
                cleanPage();
            }
        }
    }

    @Override
    public void onNegativeButtonClick(String fragmentTag) {
        if (fragmentTag != null) {
            if (fragmentTag.equals(DELETE_PAGE_ALERT_DIALOG_TAG)) {
                mDeleteRunning = false;
            } else if (fragmentTag.equals(CLEAN_PAGE_ALERT_DIALOG_TAG)) {
                mCleanRunning = false;
            } else if (fragmentTag.equals(CONVERT_SUCCESS_DIALOG_TAG)) {
                openFolder(mStrOpenFolderPath);
            } else if (fragmentTag.equals(OOM_DIALOG_TAG)) {
                noteEdited = true;
                Global.getCurrentBook().currentPage().setModified(true);
                mHandwriterView.removeNowEditedGraphics();
                UndoManager.getUndoManager().cancelLastCmd();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PageBackgroundChangeEvent event) {
        String type = event.getBackgroundType();
        if (type.equals(Paper.CUSTOMIZED)) {
            intentToImagePicker(REQUEST_BG_PICK_IMAGE);
            return;
        } else if (type.equals(Paper.USER_DEFINED_1) || type.equals(Paper.USER_DEFINED_2)
                || type.equals(Paper.USER_DEFINED_3) || type.equals(Paper.USER_DEFINED_4)) {

            int requestCode;

            if (type.equals(Paper.USER_DEFINED_1)) {
                mStrPageBackgroundPath = ToolboxConfiguration.getInstance().getPageBackgroundUserDefined1();
                requestCode = REQUEST_USER_DEFINE_BG_1_PICK_IMAGE;
            } else if (type.equals(Paper.USER_DEFINED_2)) {
                mStrPageBackgroundPath = ToolboxConfiguration.getInstance().getPageBackgroundUserDefined2();
                requestCode = REQUEST_USER_DEFINE_BG_2_PICK_IMAGE;
            } else if (type.equals(Paper.USER_DEFINED_3)) {
                mStrPageBackgroundPath = ToolboxConfiguration.getInstance().getPageBackgroundUserDefined3();
                requestCode = REQUEST_USER_DEFINE_BG_3_PICK_IMAGE;
            } else {
                mStrPageBackgroundPath = ToolboxConfiguration.getInstance().getPageBackgroundUserDefined4();
                requestCode = REQUEST_USER_DEFINE_BG_4_PICK_IMAGE;
            }

            if (!mStrPageBackgroundPath.isEmpty()) {
                mIsSettingPageBackgroundRequest = true;
                type = Paper.CUSTOMIZED;
            } else {
                intentToImagePicker(requestCode);
                return;
            }

        } else {
            if (type.equals(mHandwriterView.getPagePaperType().toString()))
                return;
        }
        setUpPageBackground(type);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CallbackEvent event) {
        /**
         * File Operation
         */
        switch (event.getMessage()) {
            case CallbackEvent.RENAME_NOTE:
                Fragment renameFragment = RenameNoteDialogFragment.newInstance(Global.getCurrentBook().getUUID(), false);
                showDialogFragment(renameFragment, RenameNoteDialogFragment.class.getSimpleName());
                break;
            case CallbackEvent.COPY_CURRENT_PAGE_TO:
                Fragment copyPageDialogFragment = new CopyPageDialogFragment();
                showDialogFragment(copyPageDialogFragment, CopyPageDialogFragment.class.getSimpleName());
                break;
            case CallbackEvent.BACKUP_NOTE:
                backupNote();
                break;
            case CallbackEvent.RESTORE_NOTE:
                restoreNote();
                break;
            case CallbackEvent.CONVERT_NOTE:
                convertNote();
                break;
            case CallbackEvent.INFO_NOTE:
                Fragment infoFragment = InformationDialogFragment.newInstance(Global.getCurrentBook().getUUID());
                showDialogFragment(infoFragment, InformationDialogFragment.class.getSimpleName());
                break;
            default:
                break;
        }

        /**
         * Toolbox
         */
        switch (event.getMessage()) {
            case CallbackEvent.SHOW_CLEAN_DIALOG:
                showCleanDialog();
                break;
            case CallbackEvent.PAGE_ADD_QUICK_TAG:
                if (!IsInitialize) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            toggleQuickTag(true);
                            return null;
                        }
                    });
                } else {
                    toggleQuickTag(true);
                }
                break;
            case CallbackEvent.PAGE_REMOVE_QUICK_TAG:
                if (!IsInitialize) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            toggleQuickTag(false);
                            return null;
                        }
                    });
                } else {
                    toggleQuickTag(false);
                }
                break;
            case CallbackEvent.SEARCH_NOTE:
                if (!Global.getCurrentBook().isAllPageReady()) {
                    waitBookshelfInitCompleted(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            notePageThumbnail();
                            return null;
                        }
                    });
                } else
                    notePageThumbnail();
                break;
            case CallbackEvent.SAVE_NOTE:
                saveNote_complete();
                break;
            case CallbackEvent.SWITCH_VERTICAL_TOOLBAR:
                if (ToolboxConfiguration.getInstance().isToolbarAtLeft()) {
                    mToolbox_vertical_left.setVisibility(View.VISIBLE);
                    mToolbox_vertical_right.setVisibility(View.GONE);
                } else {
                    mToolbox_vertical_right.setVisibility(View.VISIBLE);
                    mToolbox_vertical_left.setVisibility(View.GONE);
                }
                mHandwriterView.setDrawRegion();
                break;
            case CallbackEvent.SHOW_SETTING_CALIBRATION_DIALOG:
                Fragment noteWriterSettingFragment = new NoteWriterSettingFragment();
                showDialogFragment(noteWriterSettingFragment, NoteWriterSettingFragment.class.getSimpleName());
                break;
            case CallbackEvent.UPDATE_PAGE_TITLE:
                updatePageTitleString();
                break;

            /**
             * Noose event
             */
            case CallbackEvent.NOOSE_DELETE:
                deleteNoose();
                break;
            case CallbackEvent.NOOSE_COPY:
                copyNoose();
                break;
            case CallbackEvent.NOOSE_PASTE:
                pasteNoose();
                break;
            case CallbackEvent.NOOSE_CUT:
                cutNoose();
                break;

            /**
             * TextBox event
             */
            case CallbackEvent.TEXT_BOX_DELETE:
                mHandwriterView.deleteCurrentSelectedTextBox();
                break;

            default:
                break;
        }

        /**
         * Show message
         */
        switch (event.getMessage()) {
            case CallbackEvent.SAVE_COMPLETE:
                showConfirmAlertDialogFragment(
                        getString(R.string.activity_base_automatic_saved),
                        R.drawable.writing_ic_successful,
                        "",
                        false,
                        true);
                break;
            case CallbackEvent.SAVE_FAIL:
                showConfirmAlertDialogFragment(
                        getString(R.string.save_fail),
                        R.drawable.writing_ic_error,
                        SAVE_FAIL_ALERT_DIALOG_TAG,
                        false,
                        true);
                break;
            case CallbackEvent.PAGE_DRAW_TASK_HEAVY:
                showDismissDelayPostAlertDialogFragment("Drawing...", 0, DRAWING_ALERT_DIALOG_TAG);
                break;
            default:
                break;
        }

        /**
         * Others
         */
        switch (event.getMessage()) {
            case CallbackEvent.DO_DRAW_VIEW_INVALIDATE:
                mDrawViewLayout.invalidate();
                break;
            case CallbackEvent.DISMISS_POPUPWINDOW:
                dismissAllPopupWindow();
                break;
            case CallbackEvent.NEXT_PAGE:
                flip_page_next();
                break;
            case CallbackEvent.PREV_PAGE:
                flip_page_prev();
                break;
            case CallbackEvent.SEEKBAR_PAGE:
                Global.getCurrentBook().setCurrentPage(Global.getCurrentBook().getPage(mPagePopupWindow.getPageNumber()));
                switchToPage(Global.getCurrentBook().currentPage());
                break;
            case CallbackEvent.SEEKBAR_PROGRESS_INFO:
                int pageSize = Global.getCurrentBook().isAllPageReady() ? Global.getCurrentBook().pagesSize() : Global.getCurrentBook().pagesSizeFromIndexFile();
                mBtnPageNumber.setText((mPagePopupWindow.getPageNumber() + 1) + "/" + pageSize);
                break;
            case CallbackEvent.RENAME_NOTE_DONE:
                updatePageTitleString();
                break;
            case CallbackEvent.RESTORE_NOTE_SUCCESS:
                if (Global.getCurrentBook() != NullBook) {
                    Page p = Global.getCurrentBook().currentPage();
                    if (mHandwriterView.getPage() != p) {
                        switchToPage(p);
                    }
                } else {
                    Log.e(TAG, "can not get current book.");
                }
                break;
            case CallbackEvent.PAGE_DRAW_COMPLETED:
                if (mHandwriterView.getPage().isCanvasDrawCompleted) {
                    mInsertImageView.setVisibility(View.VISIBLE);
                    mHandwriterView.setVisibility(View.VISIBLE);
                    mFastView.setVisibility(View.GONE);
                    mDrawViewLayout.invalidate();
                }
                break;
            case CallbackEvent.CONVERT_NOTE_EMAIL:
            case CallbackEvent.BACKUP_NOTE_EMAIL:
                sendEmail();
                break;
            default:
                break;
        }

        if (event.getMessage().equals(CallbackEvent.PAGE_TAG_SETTING)) {
            pageTagSetting();
        }
    }

    public static void setNoteEdited(boolean editedStatus) {
        noteEdited = editedStatus;
    }

    public static boolean getNoteEdited() {
        return noteEdited;
    }

    private void saveCurrentNoteBook(boolean isShowAlertMessage) {
        if (mSaveRunning)
            return;

        mSaveRunning = true;
        new SaveCurrentNoteBookAsyncTask(isShowAlertMessage).execute();
    }

    public void switchToPage(UUID pageUuid) {
        switchToPage(Global.getCurrentBook().getPage(pageUuid));
    }

    public void switchToPage(Page page) {
        if (page == null)
            return;
        mHandwriterView.clearNoose();
        mBtnPrevPage.setEnabled(Global.getCurrentBook().currentPageNumber() != 0);
        if (mHandwriterView.getPage() != null) {
            if (mHandwriterView.getPage().isModified(false)) {
                if (!mHandwriterView.getPage().savePageToStorage()) {
                    CallbackEvent event = new CallbackEvent();
                    event.setMessage(CallbackEvent.SAVE_FAIL);
                    EventBus.getDefault().post(event);
                    return;
                }

                if (mHandwriterView.getPage().isCanvasDrawCompleted) {
                    if (mHandwriterView.getPage().objectsDrawTimePredict() > PAGE_REDRAW_TIME_THRESHOLD)
                        mHandwriterView.savePagePreview();
                    else
                        mHandwriterView.deletePagePreview();
                }
            }
        }
        CallbackEvent callbackEvent = new CallbackEvent();
        callbackEvent.setMessage(CallbackEvent.NOOSE_COPY_AND_DELETE_AND_CUT_BTN_GONE);
        mEventBus.post(callbackEvent);
        mFastView.setPageAndZoomOut(page);
        mHandwriterView.setPageAndZoomOut(page, true);
        if (FastView.loadPagePreviewBitmapSuccess) {
            TouchHandlerPenABC.isFastView = true;
            mInsertImageView.setVisibility(View.INVISIBLE);
            mHandwriterView.setVisibility(View.INVISIBLE);
            mFastView.setVisibility(View.VISIBLE);
        } else {
            TouchHandlerPenABC.isFastView = false;
            mInsertImageView.setVisibility(View.VISIBLE);
            mHandwriterView.setVisibility(View.VISIBLE);
            mFastView.setVisibility(View.GONE);
        }

        updatePageTitleString();
        updatePageNumber();
        ToolboxConfiguration.getInstance().setPageBackground(page.getPaperType());
        resetNDrawUpdateMode(page);

        TagManager tagManager = Global.getCurrentBook().getTagManager();
        TagManager.TagSet pageTagSet = page.tags;
        TagManager.Tag quickTag = tagManager.findTag(TagManager.QUICK_TAG_NAME);
        ToolboxConfiguration.getInstance().setPageCheckedQuickTag(pageTagSet.contains(quickTag));
    }

    public void updatePageNumber() {
        int pageSize = Global.getCurrentBook().isAllPageReady() ? Global.getCurrentBook().pagesSize() : Global.getCurrentBook().pagesSizeFromIndexFile();
        String pageNumStr = (Global.getCurrentBook().currentPageNumber() + 1) + "/" + pageSize;
        mBtnPageNumber.setText(pageNumStr);
    }

    public void setupPathForOpenFolder(String path) {
        this.mStrOpenFolderPath = path;
    }

    public void toast(String s) {
        if (mToast == null)
            mToast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        else {
            mToast.setText(s);
        }
        mToast.show();
    }

    public void toast(int resId) {
        toast(getString(resId));
    }

    public void add(Page page, int position) {
        setNoteEdited(true);
        Global.getCurrentBook().addPage(page, position, true);
        switchToPage(Global.getCurrentBook().currentPage());
        updateUndoRedoIcons();
    }

    public void remove(Page page, int position) {
        setNoteEdited(true);
        Global.getCurrentBook().removePage(page, position, true);
        switchToPage(Global.getCurrentBook().currentPage());
        updateUndoRedoIcons();
    }

    public void add(UUID pageUuid, Graphics graphics) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.add(graphics);
        updateUndoRedoIcons();
    }

    public void remove(UUID pageUuid, Graphics graphics) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.remove(graphics);
        updateUndoRedoIcons();
    }

    public void remove_for_erase(UUID pageUuid, LinkedList<Graphics> graphics) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.remove_for_erase(graphics);
        updateUndoRedoIcons();
    }

    public void add_for_erase_revert(UUID pageUuid, LinkedList<Graphics> graphics) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.add_for_erase_revert(graphics);
        updateUndoRedoIcons();
    }

    public void remove_for_clear(UUID pageUuid) {
        setNoteEdited(true);
        checkPage(pageUuid);
        if (mFastView.getVisibility() == View.VISIBLE) {
            mInsertImageView.setVisibility(View.VISIBLE);
            mHandwriterView.setVisibility(View.VISIBLE);
            mFastView.setVisibility(View.GONE);
        }

        mHandwriterView.remove_for_clear(pageUuid);
        updateUndoRedoIcons();
    }

    public void add_for_clear_revert(UUID pageUuid,
                                     LinkedList<Stroke> strokes,
                                     LinkedList<GraphicsLine> lines,
                                     LinkedList<GraphicsRectangle> rectangles,
                                     LinkedList<GraphicsOval> ovals,
                                     LinkedList<GraphicsTriangle> triangles) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.add_for_clear_revert(strokes, lines, rectangles, ovals, triangles);
        updateUndoRedoIcons();
    }

    public void modify_graphics(UUID pageUuid, Graphics oldGraphics, Graphics newGraphics) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.modify_graphics(oldGraphics, newGraphics);
        updateUndoRedoIcons();
    }

    public void modify_graphicsList(UUID pageUuid, LinkedList<Graphics> oldGraphicsList, LinkedList<Graphics> newGraphicsList) {
        setNoteEdited(true);
        checkPage(pageUuid);
        mHandwriterView.modify_graphicsList(oldGraphicsList, newGraphicsList);
        updateUndoRedoIcons();
    }

    private void checkPage(final UUID pageUuid) {
        if (!pageUuid.equals(mHandwriterView.getPage().getUUID())) {
            if (!IsInitialize) {
                waitBookshelfInitCompleted(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Global.getCurrentBook().setCurrentPage(pageUuid);
                        switchToPage(pageUuid);
                        return null;
                    }
                });
            } else {
                Global.getCurrentBook().setCurrentPage(pageUuid);
                switchToPage(pageUuid);
            }
        }
    }

    public void applyNoteWriterSettings(int offsetX, int offsetY) {
        mHandwriterView.setPenOffsetX(offsetX);
        mHandwriterView.setPenOffsetY(offsetY);

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(KEY_PEN_OFFSET_X, offsetX)
                .putInt(KEY_PEN_OFFSET_Y, offsetY)
                .apply();

        mHandwriterView.setDrawRegion();
    }

    public void waitBookshelfInitCompleted(final Callable<Void> callableMethod) {

        Global.openAlwaysWaitDialog(this);
        Runnable waitBookshelfInitRunnable = new Runnable() {
            @Override
            public void run() {
                if (IsInitialize) {
                    Global.closeWaitDialog(NoteWriterActivity.this, 0);
                    try {
                        callableMethod.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    mHandler.postDelayed(this, 300);
                }
            }
        };
        mHandler.post(waitBookshelfInitRunnable);
    }

    public void bookshelfInitCompleted(final Callable<Void> callableMethod) {

        Runnable waitBookshelfInitRunnable = new Runnable() {
            @Override
            public void run() {
                if (IsInitialize) {
                    try {
                        callableMethod.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mHandler.postDelayed(this, 300);
                }
            }
        };
        mHandler.post(waitBookshelfInitRunnable);
    }

    public TextBox getCurrentTextBox() {
        if (mHandwriterView.getNowEditedGraphics() instanceof TextBox)
            return (TextBox) mHandwriterView.getNowEditedGraphics();
        else
            return null;
    }

    private void intentToImagePicker(int requestCode) {
        Intent intent = new Intent(NoteWriterActivity.this, ImagePickerActivity.class);
        startActivityForResult(intent, requestCode);
    }

    private void initViews() {
        setContentView(R.layout.ntx_note_write);

        alert_dialog_container = findViewById(R.id.alert_dialog_container);
        dialog_container = findViewById(R.id.dialog_container);

        mDrawViewLayout = (FrameLayout) findViewById(R.id.ll_draw_view);
        mDrawViewLayout.addView(mBackgroundView);
        mDrawViewLayout.addView(mInsertImageView);
        mDrawViewLayout.addView(mHandwriterView);

        mFastView = (FastView) findViewById(R.id.layout_fast_view);

        mToolbox_vertical_left = (LinearLayout) findViewById(R.id.ll_toolbox_vertical_left);
        mToolbox_vertical_left.setOnTouchListener(this);
        mToolbox_vertical_left.setVisibility(View.GONE);
        ToolboxViewBuilder toolboxLeft = new ToolboxViewBuilder(this, R.layout.toolbox_left);
        mToolbox_vertical_left.addView(toolboxLeft);

        mToolbox_vertical_right = (LinearLayout) findViewById(R.id.ll_toolbox_vertical_right);
        mToolbox_vertical_right.setOnTouchListener(this);
        mToolbox_vertical_right.setVisibility(View.GONE);
        ToolboxViewBuilder toolboxRight = new ToolboxViewBuilder(this, R.layout.toolbox_right);
        mToolbox_vertical_right.addView(toolboxRight);

        mToolbox_horizontal = (LinearLayout) findViewById(R.id.ll_toolbox_horizontal);
        mToolbox_horizontal.setOnTouchListener(this);

        mToolbox_normal_view_layout = findViewById(R.id.toolbox_normal_view_layout);

        ImageButton btnBack = (ImageButton) findViewById(R.id.btn_toolbox_back);
        btnBack.setOnClickListener(this);
        btnBack.setOnTouchListener(this);

        mBtnPageTitle = (Button) findViewById(R.id.btn_note_title);
        mBtnPageTitle.setOnClickListener(this);
        mBtnPageTitle.setOnTouchListener(this);
        mBtnPageTitle.setOnLongClickListener(this);

        mBtnPageNumber = (Button) findViewById(R.id.btn_page_number);
        mBtnPageNumber.setOnClickListener(this);
        mBtnPageNumber.setOnTouchListener(this);

        ImageButton btnCreatePage = (ImageButton) findViewById(R.id.btn_toolbox_create_page);
        btnCreatePage.setOnClickListener(this);
        btnCreatePage.setOnTouchListener(this);

        ImageButton btnDeletePage = (ImageButton) findViewById(R.id.btn_toolbox_delete_page);
        btnDeletePage.setOnClickListener(this);
        btnDeletePage.setOnTouchListener(this);

        mBtnUndo = (ImageButton) findViewById(R.id.btn_toolbox_undo);
        mBtnUndo.setOnClickListener(this);
        mBtnUndo.setOnTouchListener(this);

        mBtnRedo = (ImageButton) findViewById(R.id.btn_toolbox_redo);
        mBtnRedo.setOnClickListener(this);
        mBtnRedo.setOnTouchListener(this);

        ImageButton btnFullRefresh = (ImageButton) findViewById(R.id.btn_toolbox_full_refresh);
        btnFullRefresh.setOnClickListener(this);
        btnFullRefresh.setOnTouchListener(this);

        ImageButton btnNextPage = (ImageButton) findViewById(R.id.btn_toolbox_next_page);
        btnNextPage.setOnClickListener(this);
        btnNextPage.setOnTouchListener(this);

        mBtnPrevPage = (ImageButton) findViewById(R.id.btn_toolbox_prev_page);
        mBtnPrevPage.setOnClickListener(this);
        mBtnPrevPage.setOnTouchListener(this);

        mBtnOverview = (ImageButton) findViewById(R.id.btn_toolbox_overview);
        mBtnOverview.setOnClickListener(this);
        mBtnOverview.setOnTouchListener(this);

        ImageButton btnNormalView = (ImageButton) findViewById(R.id.btn_toolbox_normal_view);
        btnNormalView.setOnClickListener(this);
        btnNormalView.setOnTouchListener(this);


    }

    private void initToolboxSize() {
        int left_toolbox_width = mToolbox_vertical_left.getLayoutParams().width;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int status_bar_height = 0;
        int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            status_bar_height = getApplicationContext().getResources().getDimensionPixelSize(resourceId);
        }

        int screen_height = displayMetrics.heightPixels + status_bar_height;
        int screen_width = displayMetrics.widthPixels;
        float rate = (float) displayMetrics.widthPixels / (float) (displayMetrics.heightPixels + status_bar_height);

        Global.MACHINE_PIXEL_RATE_VALUE = rate;

        Global.SCREEN_WIDTH = screen_width;
        Global.SCREEN_HEIGHT = screen_height;

        int temp_height = (int) Math.rint((float) (screen_width - left_toolbox_width) / rate);
        temp_height = screen_height - temp_height;
        ViewGroup.LayoutParams params = mToolbox_horizontal.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        Global.HORIZONTAL_TOOLBOX_HEIGHT_RUN_TIME = Global.HORIZONTAL_TOOLBOX_HEIGHT = params.height = temp_height;
        mToolbox_horizontal.setLayoutParams(params);
        ViewGroup.LayoutParams params2 = mToolbox_horizontal.getLayoutParams();
        params2.height = temp_height;
        mToolbox_normal_view_layout.setLayoutParams(params2);
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {//20190904 temp version1
            int temp_height_landscape = left_toolbox_width;
            Global.HORIZONTAL_TOOLBOX_HEIGHT_RUN_TIME = Global.HORIZONTAL_TOOLBOX_HEIGHT = params.height = temp_height_landscape;
            mToolbox_horizontal.setLayoutParams(params);
            params2.height = temp_height_landscape;
            mToolbox_normal_view_layout.setLayoutParams(params2);
        }
    }

    private int getNavigationBarHeight() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);

        return height;
    }

    private void setKeepScreenOn() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean screenOn = settings.getBoolean(Preferences.KEY_KEEP_SCREEN_ON, false);
        if (screenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)) {
            // if device has home key, set full screen to hide navigation bar.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

    }

    private void changeEinkControlPermission(boolean isForNtxAppsOnly) {
        Intent changePermissionIntent = new Intent("ntx.eink_control.CHANGE_PERMISSION");
        changePermissionIntent.putExtra("isPermissionNtxApp", isForNtxAppsOnly);
        sendBroadcast(changePermissionIntent);
    }

    private void resetEinkMode() {
        Intent dropIntent = new Intent("ntx.eink_control.DropFrames");
        dropIntent.putExtra("period", DROP_TIME_DEFAULT);
        dropIntent.putExtra("commandFromNtxApp", true);
        sendBroadcast(dropIntent);

        Intent resetIntent = new Intent("ntx.eink_control.GLOBAL_REFRESH");
        resetIntent.putExtra("updatemode", PenEventNTX.UPDATE_MODE_GLOBAL_RESET);
        resetIntent.putExtra("commandFromNtxApp", true);
        sendBroadcast(resetIntent);
    }

    private void flip_page_prev() {
        if (!Global.getCurrentBook().isAllPageReady()) {
            waitBookshelfInitCompleted(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    flip_page_prev();
                    return null;
                }
            });
        } else {
            if (!Global.getCurrentBook().isFirstPage()) {
                switchToPage(Global.getCurrentBook().previousPage());
                Global.HAS_GREY_COLOR = false;
            }
        }
    }

    private void flip_page_next() {
        if (!Global.getCurrentBook().isAllPageReady()) {
            waitBookshelfInitCompleted(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    flip_page_next();
                    return null;
                }
            });
        } else {
            if (Global.getCurrentBook().isLastPage()) {
                if (mHandwriterView.getPage().isEmpty()) {
                    showConfirmAlertDialogFragment(
                            getString(R.string.quill_inserted_fail),
                            R.drawable.writing_ic_error,
                            EMPTY_PAGE_ALERT_DIALOG_TAG,
                            false,
                            true);
                } else {
                    if (Global.getCurrentBook().getPages().size() >= PAGE_MAX_SIZE) {
                        showConfirmAlertDialogFragment(
                                getString(R.string.msg_warning_page_numbers, PAGE_MAX_SIZE),
                                R.drawable.writing_ic_error,
                                MAX_PAGE_ALERT_DIALOG_TAG,
                                false,
                                true);
                    } else {
                        Global.HAS_GREY_COLOR = false;
                        switchToPage(Global.getCurrentBook().insertPage());
                        saveCurrentNoteBook(false);
                    }
                }
            } else {
                Global.HAS_GREY_COLOR = false;
                switchToPage(Global.getCurrentBook().nextPage());
            }
        }
    }

    private void undo() {
        CallbackEvent callbackEvent;
        callbackEvent = new CallbackEvent();
        callbackEvent.setMessage(CallbackEvent.NOOSE_ALL_BTN_GONE);
        mEventBus.post(callbackEvent);
        mHandwriterView.clearNoose();
        if (UndoManager.getUndoManager().undo())
            mHandwriterView.invalidate();
        updateUndoRedoIcons();
    }

    private void redo() {
        CallbackEvent callbackEvent;
        callbackEvent = new CallbackEvent();
        callbackEvent.setMessage(CallbackEvent.NOOSE_ALL_BTN_GONE);
        mEventBus.post(callbackEvent);
        mHandwriterView.clearNoose();
        if (UndoManager.getUndoManager().redo())
            mHandwriterView.invalidate();
        updateUndoRedoIcons();
    }

    private void updateUndoRedoIcons() {
        UndoManager mgr = UndoManager.getUndoManager();
        if (mgr.haveUndo() != mBtnUndo.isEnabled()) {
            setUndoIconEnabled(mgr.haveUndo());
        }
        if (mgr.haveRedo() != mBtnRedo.isEnabled()) {
            setRedoIconEnabled(mgr.haveRedo());
        }
    }

    private void setUndoIconEnabled(boolean active) {
        mBtnUndo.setEnabled(active);
    }

    private void setRedoIconEnabled(boolean active) {
        mBtnRedo.setEnabled(active);
    }

    private void setUpPageBackground(String type) {
        Global.getCurrentBook().currentPage().clearCustomizedBackground();

        Global.getCurrentBook().currentPage().setModified(true);
        setNoteEdited(true);

        if (!mIsSettingPageBackgroundRequest) {
            // clear
            mStrPageBackgroundPath = "na";
        }
        mIsSettingPageBackgroundRequest = false;

        mHandwriterView.setPagePaperType(PaperTypeStringToEnumValue(type), mStrPageBackgroundPath);
    }

    private Paper.Type PaperTypeStringToEnumValue(String paper) {
        for (int i = 0; i < Paper.Table.length; i++) {
            Paper.Type paperType = Paper.Table[i].getType();
            if (paperType.toString().equals(paper)) {
                return paperType;
            }
        }
        return Paper.Table[0].getType();
    }

    private boolean isOverview() {
        if (!mIsToolboxShown) {
            mToolbox_horizontal.setVisibility(View.VISIBLE);
            if (ToolboxConfiguration.getInstance().isToolbarAtLeft()) {
                mToolbox_vertical_left.setVisibility(View.VISIBLE);
                mToolbox_vertical_right.setVisibility(View.GONE);
            } else {
                mToolbox_vertical_left.setVisibility(View.GONE);
                mToolbox_vertical_right.setVisibility(View.VISIBLE);
            }
            mToolbox_normal_view_layout.setVisibility(View.GONE);
            Global.HORIZONTAL_TOOLBOX_HEIGHT_RUN_TIME = Global.HORIZONTAL_TOOLBOX_HEIGHT;
            Global.VERTICAL_TOOLBOX_WIDTH_RUN_TIME = Global.VERTICAL_TOOLBOX_WIDTH;
            mIsToolboxShown = true;
        } else
            mIsToolboxShown = false;

        return mIsToolboxShown;
    }

    private void showInfoDialog() {
    }

    private void showCleanDialog() {
        if (!mCleanRunning) {
            mCleanRunning = true;
            showConfirmAlertDialogFragment(
                    getString(R.string.msg_clear_confirm),
                    R.drawable.writing_ic_error,
                    CLEAN_PAGE_ALERT_DIALOG_TAG,
                    true,
                    true);
        }
    }

    private void saveNote_complete() {
        SaveDialog.show();
        boolean normalSaveStatus = Global.getCurrentBook().save();
        Log.d(TAG, "Normal Save Status : " + normalSaveStatus);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SaveDialog.dismiss();
            }
        }, 2000);
    }

    private void backupNote() {
        BackupDialogFragment backupDialogFragment = BackupDialogFragment.newInstance(Global.getCurrentBook().getUUID(), noteEdited, false);
        showDialogFragment(backupDialogFragment, BackupDialogFragment.class.getSimpleName());
    }

    private void convertNote() {
        ConvertDialogFragment convertDialogFragment = ConvertDialogFragment.newInstance(Global.getCurrentBook().getUUID());
        showDialogFragment(convertDialogFragment, ConvertDialogFragment.class.getSimpleName());
    }

    private void restoreNote() {
        RestoreDialogFragment restoreDialogFragment = RestoreDialogFragment.newInstance(Global.getCurrentBook().getUUID());
        showDialogFragment(restoreDialogFragment, RestoreDialogFragment.class.getSimpleName());
    }

    private void pageTagSetting() {
        TagDialogFragment tagDialogFragment = TagDialogFragment.newInstance();
        showDialogFragment(tagDialogFragment, TagDialogFragment.class.getSimpleName());
    }

    private void notePageThumbnail() {
        ThumbnailDialogFragment thumbnailDialogFragment = ThumbnailDialogFragment.newInstance(getNoteEdited());
        showDialogFragment(thumbnailDialogFragment, ThumbnailDialogFragment.class.getSimpleName());
    }

    private void showFileOperationsPopupWindow() {
        mDrawViewLayout.invalidate();

        mFileOperationsPopupWindow = new FileOperationsPopupWindow(this, dm.widthPixels - toolbox_vertical_width);
        mFileOperationsPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mBtnPageTitle.setSelected(false);
            }
        });

        if (mToolbox_vertical_left.getVisibility() == View.VISIBLE) {
            mFileOperationsPopupWindow.showOnAnchor(mBtnPageTitle, HorizontalPosition.ALIGN_LEFT, VerticalPosition.BELOW);
        } else {
            mFileOperationsPopupWindow.showOnAnchor(mBtnPageTitle, HorizontalPosition.ALIGN_LEFT, VerticalPosition.BELOW,
                    -toolbox_vertical_width, 0);
        }
    }

    private void showPageSeekBarPopupWindow() {
        mDrawViewLayout.invalidate();
        mPagePopupWindow = new PagePopupWindow(this,
                (int) ((float) dm.widthPixels / 1.5f) - toolbox_vertical_width,
                Global.getCurrentBook().getPageNumber(Global.getCurrentBook().currentPage().getUUID()),
                Global.getCurrentBook().isAllPageReady() ? Global.getCurrentBook().pagesSize() : Global.getCurrentBook().pagesSizeFromIndexFile());

        mPagePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mBtnPageNumber.setSelected(false);
            }
        });

        if (mToolbox_vertical_left.getVisibility() == View.VISIBLE) {
            mPagePopupWindow.showOnAnchor(mBtnOverview, HorizontalPosition.ALIGN_RIGHT, VerticalPosition.BELOW, 15, 0, 0, 15);
        } else {
            mPagePopupWindow.showOnAnchor((LinearLayout) findViewById(R.id.ll_toolbox_horizontal_group2),
                    HorizontalPosition.ALIGN_RIGHT, VerticalPosition.BELOW, 15, 0, 0, 15);
        }
    }

    private void deletePage() {
        Global.getCurrentBook().deletePage();
        Global.HAS_GREY_COLOR = false;
        switchToPage(Global.getCurrentBook().currentPage());
        mDeleteRunning = false;
    }

    private void cleanPage() {
        mHandwriterView.clear();
        Global.HAS_GREY_COLOR = false;
        mCleanRunning = false;
    }

    private void toggleQuickTag(boolean isCheck) {
        TagManager tagManager = Global.getCurrentBook().getTagManager();
        TagManager.TagSet currentPageTagSet = Global.getCurrentBook().currentPage().tags;
        TagManager.Tag quickTag = tagManager.findTag(TagManager.QUICK_TAG_NAME);
        if (isCheck)
            currentPageTagSet.add(quickTag);
        else
            currentPageTagSet.remove(quickTag);

        setNoteEdited(true);
        Global.getCurrentBook().currentPage().touch();
    }

    public void showConfirmAlertDialogFragment(String msg, @Nullable Integer iconResId, String tag, boolean isNegativeButtonVisible, boolean enablePositiveButton) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        AlertDialogFragment confirmAlertDialogFragment;
        TouchHandlerPenABC.isPopupwindow = true;
        confirmAlertDialogFragment = AlertDialogFragment.newInstance(msg, iconResId, enablePositiveButton, tag);
        confirmAlertDialogFragment.registerAlertDialogButtonClickListener(this, tag);
        if (isNegativeButtonVisible) {
            confirmAlertDialogFragment.setupNegativeButton(getString(AlertDialogFragment.NEGATIVE_DEFAULT_STRING));
        }

        fragmentTransaction.replace(R.id.alert_dialog_container, confirmAlertDialogFragment, tag).commit();
    }

    private void showOOMAlertDialogFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.out_of_memory), R.drawable.writing_ic_error, false, OOM_DIALOG_TAG);
        alertDialogFragment.registerAlertDialogButtonClickListener(NoteWriterActivity.this, OOM_DIALOG_TAG);
        ft.replace(R.id.alert_dialog_container, alertDialogFragment, AlertDialogFragment.class.getSimpleName())
                .commit();
    }

    private void showDismissDelayPostAlertDialogFragment(String msg, int minDismissDelayTime, String tag) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DismissDelayPostAlertDialogFragment dismissDelayPostAlertDialogFragment = DismissDelayPostAlertDialogFragment.newInstance(msg, minDismissDelayTime, tag);
        TouchHandlerPenABC.isPopupwindow = true;
        ft.replace(R.id.alert_dialog_container, dismissDelayPostAlertDialogFragment, tag).commit();
    }

    private void showDialogFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        TouchHandlerPenABC.isPopupwindow = true;
        fragmentTransaction.replace(R.id.dialog_container, fragment, tag).commit();
    }

    private void openFolder(String fullPath) {
        Uri pathUri;
        String path;
        if (fullPath.contains(Global.DIRECTORY_SDCARD_NOTE)) {
            pathUri = Uri.parse(Global.DIRECTORY_SDCARD_NOTE);
        } else if (fullPath.contains(Global.DIRECTORY_EXTERNALSD_NOTE)) {
            pathUri = Uri.parse(Global.DIRECTORY_EXTERNALSD_NOTE);
        } else if (fullPath.contains(Global.DIRECTORY_SDCARD_BOOK)) {
            pathUri = Uri.parse(Global.DIRECTORY_SDCARD_BOOK);
        } else if (fullPath.contains(Global.DIRECTORY_EXTERNALSD_BOOK)) {
            pathUri = Uri.parse(Global.DIRECTORY_EXTERNALSD_BOOK);
        } else if (fullPath.contains(Global.DIRECTORY_SDCARD_SLEEP)) {
            pathUri = Uri.parse(Global.DIRECTORY_SDCARD_SLEEP);
        } else if (fullPath.contains(Global.DIRECTORY_SDCARD_POWEROFF)) {
            pathUri = Uri.parse(Global.DIRECTORY_SDCARD_POWEROFF);
        } else {
            pathUri = Uri.parse(Global.PATH_SDCARD);
        }
        path = pathUri.toString();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pathUri, "resource/folder");
        intent.putExtra("path", path);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetNDrawUpdateMode(Page page) {
        if (page.isNDrawDuModeRequiredForGrayscale()) {
            nDrawHelper.NDrawSetUpdateMode(PenEventNTX.UPDATE_MODE_PARTIAL_DU);
        } else {
            nDrawHelper.NDrawSetUpdateMode(PenEventNTX.UPDATE_MODE_PARTIAL_A2);
        }
    }

    private void dismissAllPopupWindow() {
        if (mFileOperationsPopupWindow != null && mFileOperationsPopupWindow.isShowing())
            mFileOperationsPopupWindow.dismiss();
        if (mPagePopupWindow != null && mPagePopupWindow.isShowing())
            mPagePopupWindow.dismiss();
    }

    private void sendEmail() {
        File mailTempDir = new File(Global.PATH_SDCARD + Global.MAIL_FILE_TEMP_DIR);

        ArrayList<Uri> uris = new ArrayList<>();
        String[] children = mailTempDir.list();
        if (children != null) {
            for (String child : children) {
                uris.add(Uri.fromFile(new File(mailTempDir, child)));
            }
        }

        String subject = "File from Notes";
        String[] emailTo = {""};

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailTo);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please find the attachment.");
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        try {
            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.exportBackup_email_login)));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e("", "There are no email clients installed");
        }
    }

    private class SaveCurrentNoteBookAsyncTask extends AsyncTask<Void, Void, Void> {
        private boolean isShowAlertDialog;

        SaveCurrentNoteBookAsyncTask(boolean isShowAlertMessage) {
            this.isShowAlertDialog = isShowAlertMessage;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mHandwriterView.getPage().objectsDrawTimePredict() > PAGE_REDRAW_TIME_THRESHOLD)
                mHandwriterView.savePagePreview();
            else
                mHandwriterView.deletePagePreview();
            Global.getCurrentBook().save();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSaveRunning = false;
            setNoteEdited(false);

            if (isShowAlertDialog) {
                try {
                    ((DismissDelayPostAlertDialogFragment) getFragmentManager().findFragmentByTag(SAVE_ALERT_DIALOG_TAG)).dismiss();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startRotate() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            // default
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mHandwriterView.setCurrentRotation(getRequestedOrientation());
    }

    private void resetRotate() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void invalidateToolboxView() {
        mHandler.removeCallbacks(mRunnableInvalidateToolbox);
        mHandler.postDelayed(mRunnableInvalidateToolbox, 0);
    }

    /**
     * Control the 2-Step-Suspend for Netronix eInk devices
     *
     * @param state 1 is enable.
     *              0 is disable.
     */
    private void PowerEnhanceSet(int state) {
        Log.d(TAG, "PowerEnhanceSet = " + state);
        try {
            Settings.System.putInt(mContext.getContentResolver(), "power_enhance_enable", state);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "set POWER_ENHANCE_ENABLE error!!");
        }
    }

    private void closeAllFragmentDialog() {
        hideInputMethod();
        alert_dialog_container.removeAllViews();
        dialog_container.removeAllViews();

        mSaveRunning = false;
        mCleanRunning = false;
        mDeleteRunning = false;
        mIsSettingPageBackgroundRequest = false;
    }

    private void hideInputMethod() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDialog(final boolean instantFinish) {
        SaveDialog.show();
        final boolean saveStatus = Global.getCurrentBook().save();
        if (instantFinish) {
            finish();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SaveDialog.dismiss();
                if (saveStatus) {
                    finish();
                }
            }
        }, 2000);
    }

    private void saveToolBoxSetting(boolean apply) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        if (mHandwriterView != null)
            mHandwriterView.saveSettings(editor);
        ToolboxConfiguration.getInstance().saveSettings(editor);
        if (apply) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private void updatePageTitleString() {
        if (ToolboxConfiguration.getInstance().showMemoTheme()) {
            String mainTag = Global.getCurrentBook().currentPage().getMainTag();
            if (mainTag.isEmpty())
                mBtnPageTitle.setText(Global.getCurrentBook().getTitle());
            else
                mBtnPageTitle.setText(mainTag);
        } else
            mBtnPageTitle.setText(Global.getCurrentBook().getTitle());
    }

    private class AutoSaveTimerTask extends TimerTask {
        public void run() {
            if (autoSaveTimer != null)
                autoSaveTimer.cancel();
            boolean autoSaveStatus = Global.getCurrentBook().save(true, true);
            Log.d(TAG, "Auto Save Status : " + autoSaveStatus);
            autoSaveTimer = new Timer(false);
            autoSaveTimer.schedule(new AutoSaveTimerTask(), autoSaveTime, autoSaveTime);
        }

    }

    private void deleteNoose() {
        setNoteEdited(true);
        mHandwriterView.getPage().setModified(true);
        mHandwriterView.deleteNooseSelect();
    }

    private void copyNoose() {
        setNoteEdited(true);
        mHandwriterView.getPage().setModified(true);
        mHandwriterView.copyNooseSelect();
    }

    private void pasteNoose() {
        setNoteEdited(true);
        mHandwriterView.pasteNooseSelect();
    }

    private void cutNoose() {
        copyNoose();
        deleteNoose();
    }

    private void createPage() {
        if (Global.getCurrentBook().pagesSize() >= PAGE_MAX_SIZE) {
            showConfirmAlertDialogFragment(
                    getString(R.string.msg_warning_page_numbers, PAGE_MAX_SIZE),
                    R.drawable.writing_ic_error,
                    MAX_PAGE_ALERT_DIALOG_TAG,
                    false,
                    true);
        } else {
            if (Global.getCurrentBook().currentPage().isEmpty()) {
                showConfirmAlertDialogFragment(
                        getString(R.string.quill_inserted_fail),
                        R.drawable.writing_ic_error,
                        EMPTY_PAGE_ALERT_DIALOG_TAG,
                        false,
                        true);
            } else {
                Global.getCurrentBook().insertPage();
            }
        }
        saveCurrentNoteBook(false);
    }

    private void showDeletePageConfirmDialog() {
        if (!mDeleteRunning) {
            mDeleteRunning = true;
            showConfirmAlertDialogFragment(
                    getString(R.string.msg_delete_confirm),
                    R.drawable.writing_ic_error,
                    DELETE_PAGE_ALERT_DIALOG_TAG,
                    true,
                    true);
        }
    }

    private String generateNoteNameByJsonData(List<DateNoteData> dateNoteDataList) {
        String noteName = "Note 1";

        List<String> bookTitleList = new ArrayList<>();
        for (DateNoteData dateNoteData : dateNoteDataList) {
            bookTitleList.add(dateNoteData.getTitle());
        }

        int index = 1;
        while (bookTitleList.contains(noteName)) {
            index++;
            noteName = "Note " + index;
        }

        return noteName;
    }

    private DateNoteData loadQuickOpenJsonFile(String bookUuidString) {
        String folderPath = APP_DATA_PACKAGE_FILES_PATH + NOTEBOOK_DIRECTORY_PREFIX + bookUuidString + "/";
        String jsonFilePath = folderPath + "quickOpen.json";

        if (!new File(folderPath).exists()) {
            Log.e(TAG, "book file folder not exist");
            finish();
            return null;
        }

        DateNoteData dateNoteData;
        Gson gson = new Gson();
        BufferedReader br;
        if (new File(jsonFilePath).exists()) {
            try {
                br = new BufferedReader(new FileReader(jsonFilePath));
                dateNoteData = gson.fromJson(br, DateNoteData.class);
                return dateNoteData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e(TAG, "quick open JSON file not exist");
            return null;
        }
    }

    private void initBookshelf(Intent i) {
        mIsCreateNote = i.getBooleanExtra("CreateNote", false);
        i.removeExtra("CreateNote");
        String titleName = i.getStringExtra("titleName");
        i.removeExtra("titleName");
        final String currentBookUuidStr = i.getStringExtra("uuid");
        i.removeExtra("uuid");
        final String pageUuidString = i.getStringExtra("pageUuid");
        i.removeExtra("pageUuid");
        intentFrom = i.getStringExtra("from");
        boolean isLandscape = i.getBooleanExtra("IsLandscape", false);

        if (!mIsCreateNote && pageUuidString != null && !pageUuidString.isEmpty()) {
            if (!Global.checkBookAndPageExist(currentBookUuidStr, pageUuidString)) {
                finish();
            }
        }

        if (IsInitialize) {
            /**
             * Bookshelf is initialized complete.
             */

            if (mIsCreateNote) {
                if (titleName != null && !titleName.isEmpty())
                    Bookshelf.getInstance().newBook(titleName, isLandscape);
                else
                    Bookshelf.getInstance().newBook(Global.generateNoteName(), isLandscape);

                noteEdited = true;
            } else {
                if (currentBookUuidStr == null) {// this if judgement is to avoid crashing caused by creating note and then rotating.
                    Bookshelf.getInstance().setCurrentBook(Global.getCurrentBook().getUUID(), false);
                } else if (Bookshelf.getInstance().checkBookExist(UUID.fromString(currentBookUuidStr))) {
                    
                    /*
                    Bookshelf.getInstance().setCurrentBook(UUID.fromString(currentBookUuidStr), reload);

                    int pageIndex = i.getIntExtra("pageIndex", -1);
                    if (pageIndex >= 0) {
                        Global.getCurrentBook().setCurrentPage(Global.getCurrentBook().getPage(pageIndex));
                    }
                    */

                    DateNoteData quickOpenJsonData = loadQuickOpenJsonFile(currentBookUuidStr);
                    if (quickOpenJsonData == null || quickOpenJsonData.getCurrentPageUuid() == null || quickOpenJsonData.getCurrentPageUuid().toString().isEmpty()) {
                        /**
                         * Open note by JSON file 'FAIL'
                         *
                         * ----> load all page files and load book by index file
                         */
                        Bookshelf.getInstance().setCurrentBook(UUID.fromString(currentBookUuidStr), false);
                        if (pageUuidString != null && !pageUuidString.isEmpty()) {
                            Global.getCurrentBook().setCurrentPage(UUID.fromString(pageUuidString));
                        }
                    } else if (pageUuidString != null && !pageUuidString.isEmpty()) {
                        /**
                         * Open note by JSON file and Page UUID
                         *
                         */
                        Global.setCurrentBook(new Book(quickOpenJsonData, UUID.fromString(pageUuidString)));
                        Storage.getInstance().saveCurrentBookUUID(UUID.fromString(currentBookUuidStr));

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Bookshelf.getInstance().setCurrentBook(UUID.fromString(currentBookUuidStr), true);
                                Global.getCurrentBook().setCurrentPage(UUID.fromString(pageUuidString));
                                onInitializationReload();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                onInitializationFinished();
                            }
                        }.execute();
                    } else {
                        /**
                         * Open note by JSON file
                         *
                         */
                        Global.setCurrentBook(new Book(quickOpenJsonData, true));
                        Storage.getInstance().saveCurrentBookUUID(UUID.fromString(currentBookUuidStr));

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Bookshelf.getInstance().setCurrentBook(UUID.fromString(currentBookUuidStr), true);
                                onInitializationReload();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                onInitializationFinished();
                            }
                        }.execute();
                    }

                    if (Global.getCurrentBook().isLandscape() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    else if (!Global.getCurrentBook().isLandscape() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                } else {
                    Log.e(TAG, "can not get book.");
                    finish();
                    return;
                }
            }

            /**
             * auto save timer ON
             */
            if (autoSaveTimer != null)
                autoSaveTimer.cancel();
            autoSaveTimer = new Timer(false);
            autoSaveTimer.schedule(new AutoSaveTimerTask(), autoSaveTime, autoSaveTime);

        } else {
            if (mIsCreateNote) {
                noteEdited = true;
                if (titleName != null && !titleName.isEmpty()) {
                    /**
                     * Not initialization
                     * Create new note from 'nDate'
                     *
                     * ----> initial bookshelf in background
                     */
                    Global.setCurrentBook(new Book(titleName, isLandscape));
                    Global.getCurrentBook().saveNoWaitInitialization();
                    asynchronizeInitialStorage(null);
                } else {
                    List<DateNoteData> dateNoteDataList = Global.loadJsonFile();
                    if (dateNoteDataList != null && !dateNoteDataList.isEmpty()) {
                        /**
                         * Not initialization
                         * Create new note
                         * No title name (Not from 'nDate')
                         *
                         * ----> create new note name by JSON file data
                         * ----> initial bookshelf in background
                         */
                        Global.setCurrentBook(new Book(generateNoteNameByJsonData(dateNoteDataList), isLandscape));
                        Global.getCurrentBook().saveNoWaitInitialization();
                        asynchronizeInitialStorage(null);
                    } else {
                        /**
                         * Not initialization
                         * Create new note
                         * No title name (Not from 'nDate')
                         * No JSON file data
                         *
                         * ----> wait for initialization complete
                         */
                        synchronizeInitialStorage();
                        Bookshelf.getInstance().newBook(Global.generateNoteName(), isLandscape);

                        /**
                         * auto save timer ON
                         */
                        if (autoSaveTimer != null)
                            autoSaveTimer.cancel();
                        autoSaveTimer = new Timer(false);
                        autoSaveTimer.schedule(new AutoSaveTimerTask(), autoSaveTime, autoSaveTime);
                    }
                }
            } else {
                if (currentBookUuidStr == null || currentBookUuidStr.isEmpty()) {
                    Log.e(TAG, "book uuid is wrong");
                    finish();
                } else {
                    DateNoteData quickOpenJsonData = loadQuickOpenJsonFile(currentBookUuidStr);
                    if (quickOpenJsonData == null || quickOpenJsonData.getCurrentPageUuid() == null || quickOpenJsonData.getCurrentPageUuid().toString().isEmpty()) {
                        /**
                         * Not initialization
                         * Open note by JSON file 'FAIL'
                         *
                         * ----> load all page files and load book by index file
                         */
                        Global.setCurrentBook(new Book(UUID.fromString(currentBookUuidStr), true));
                        Storage.getInstance().saveCurrentBookUUID(UUID.fromString(currentBookUuidStr));
                        asynchronizeInitialStorage(UUID.fromString(currentBookUuidStr));
                    } else {
                        /**
                         * Not initialization
                         * Open note by JSON file
                         *
                         */
                        Global.setCurrentBook(new Book(quickOpenJsonData, true));
                        Storage.getInstance().saveCurrentBookUUID(UUID.fromString(currentBookUuidStr));
                        asynchronizeInitialStorage(UUID.fromString(currentBookUuidStr));
                    }
                }

                if (Global.getCurrentBook().isLandscape() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                else if (!Global.getCurrentBook().isLandscape() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }
        }
    }

    private void backFunc() {
        if (getNoteEdited()) {
            saveDialog(false);
        } else {
            finish();
        }
    }

    private void sendFeedBack(Exception e){


            ApplicationErrorReport report = new ApplicationErrorReport();
            report.packageName = report.processName = getApplication().getPackageName();
            report.time = System.currentTimeMillis();
            report.type = ApplicationErrorReport.TYPE_CRASH;
            report.systemApp = false;

            ApplicationErrorReport.CrashInfo crash = new ApplicationErrorReport.CrashInfo();
            crash.exceptionClassName = e.getClass().getSimpleName();
            crash.exceptionMessage = e.getMessage();

            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);
            e.printStackTrace(printer);

            crash.stackTrace = writer.toString();

            StackTraceElement stack = e.getStackTrace()[0];
            crash.throwClassName = String.valueOf(stack.getClass());
            crash.throwFileName = stack.getFileName();
            crash.throwLineNumber = stack.getLineNumber();
            crash.throwMethodName = stack.getMethodName();

            report.crashInfo = crash;

            Intent intent = new Intent (Intent.ACTION_APP_ERROR);
            intent.putExtra(Intent.EXTRA_BUG_REPORT, report);
            startActivity(intent);

    }
}
