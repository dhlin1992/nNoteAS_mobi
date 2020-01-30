package ntx.note;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import name.vbraun.view.write.Paper;
import ntx.note.ToolboxConfiguration.PageBackground;
import ntx.note.asynctask.BitmapWorkerTask;
import ntx.note.image.ImagePickerActivity;
import ntx.note2.R;

import static ntx.note.NoteWriterActivity.REQUEST_USER_DEFINE_BG_1_PICK_IMAGE;
import static ntx.note.NoteWriterActivity.REQUEST_USER_DEFINE_BG_2_PICK_IMAGE;
import static ntx.note.NoteWriterActivity.REQUEST_USER_DEFINE_BG_3_PICK_IMAGE;
import static ntx.note.NoteWriterActivity.REQUEST_USER_DEFINE_BG_4_PICK_IMAGE;

public class PageBackgroundPopupWindow extends RelativePopupWindow {
    private ToolboxConfiguration mToolboxConfiguration;

    private ImageButton mBtnPageBgBlank;
    private ImageButton mBtnPageBgNarrow;
    private ImageButton mBtnPageBgCollege;
    private ImageButton mBtnPageBgCornell;
    private ImageButton mBtnPageBgTodo;
    private ImageButton mBtnPageBgMeeting;
    private ImageButton mBtnPageBgDiary;
    private ImageButton mBtnPageBgQuadrangle;
    private ImageButton mBtnPageBgStave;
    private ImageButton mBtnPageBgCalligraphySmall;
    private ImageButton mBtnPageBgCalligraphyBig;
    private ImageButton mBtnPageBgCustomized;

    private ImageButton mBtnCustomBg1;
    private ImageButton mBtnCustomBg2;
    private ImageButton mBtnCustomBg3;
    private ImageButton mBtnCustomBg4;

    private ImageButton mBtnCustomBgSetting;

    private LinearLayout mLayoutCustomBgButtons1;
    private LinearLayout mLayoutCustomBgButtons2;
    private LinearLayout mLayoutCustomBgButtons3;
    private LinearLayout mLayoutCustomBgButtons4;

    private boolean mCustomBgBtnSettingMode = false;

    public PageBackgroundPopupWindow(Activity ctx) {
        super(ctx);

        setContentView(LayoutInflater.from(ctx).inflate(R.layout.popupwindow_page_background, null));
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(false);
        setFocusable(false);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View popupView = getContentView();

        mToolboxConfiguration = ToolboxConfiguration.getInstance();

        initView(popupView);
    }

    @Override
    public void showOnAnchor(View anchor, int horizPos, int vertPos, int padL, int padT, int padR, int padB) {
        // Karote 20180921 : Renew selection icon before show up.
        setPageBackgroundValue(mToolboxConfiguration.getPageBackground());
        setUserDefinedButtonView();
        mCustomBgBtnSettingMode = false;
        changeUserDefineButtonMode();
        super.showOnAnchor(anchor, horizPos, vertPos, padL, padT, padR, padB);
    }

    private void initView(View v) {

        mBtnPageBgBlank = (ImageButton) v.findViewById(R.id.btn_page_bg_blank);
        mBtnPageBgNarrow = (ImageButton) v.findViewById(R.id.btn_page_bg_narrow);
        mBtnPageBgCollege = (ImageButton) v.findViewById(R.id.btn_page_bg_college);
        mBtnPageBgCornell = (ImageButton) v.findViewById(R.id.btn_page_bg_cornell);
        mBtnPageBgTodo = (ImageButton) v.findViewById(R.id.btn_page_bg_todo);
        mBtnPageBgMeeting = (ImageButton) v.findViewById(R.id.btn_page_bg_meeting);
        mBtnPageBgDiary = (ImageButton) v.findViewById(R.id.btn_page_bg_diary);
        mBtnPageBgQuadrangle = (ImageButton) v.findViewById(R.id.btn_page_bg_quadrangle);
        mBtnPageBgStave = (ImageButton) v.findViewById(R.id.btn_page_bg_stave);
        mBtnPageBgCalligraphySmall = (ImageButton) v.findViewById(R.id.btn_page_bg_calligraphy_small);
        mBtnPageBgCalligraphyBig = (ImageButton) v.findViewById(R.id.btn_page_bg_calligraphy_big);
        mBtnPageBgCustomized = (ImageButton) v.findViewById(R.id.btn_page_bg_customized);

        mBtnCustomBg1 = (ImageButton) v.findViewById(R.id.btn_custom_bg_1);
        mBtnCustomBg2 = (ImageButton) v.findViewById(R.id.btn_custom_bg_2);
        mBtnCustomBg3 = (ImageButton) v.findViewById(R.id.btn_custom_bg_3);
        mBtnCustomBg4 = (ImageButton) v.findViewById(R.id.btn_custom_bg_4);

        mLayoutCustomBgButtons1 = (LinearLayout) v.findViewById(R.id.layout_custom_bg_buttons_1);
        mLayoutCustomBgButtons2 = (LinearLayout) v.findViewById(R.id.layout_custom_bg_buttons_2);
        mLayoutCustomBgButtons3 = (LinearLayout) v.findViewById(R.id.layout_custom_bg_buttons_3);
        mLayoutCustomBgButtons4 = (LinearLayout) v.findViewById(R.id.layout_custom_bg_buttons_4);

        ImageButton btnCustomBgChange1 = (ImageButton) v.findViewById(R.id.btn_custom_bg_change_1);
        ImageButton btnCustomBgChange2 = (ImageButton) v.findViewById(R.id.btn_custom_bg_change_2);
        ImageButton btnCustomBgChange3 = (ImageButton) v.findViewById(R.id.btn_custom_bg_change_3);
        ImageButton btnCustomBgChange4 = (ImageButton) v.findViewById(R.id.btn_custom_bg_change_4);
        ImageButton btnCustomBgDelete1 = (ImageButton) v.findViewById(R.id.btn_custom_bg_delete_1);
        ImageButton btnCustomBgDelete2 = (ImageButton) v.findViewById(R.id.btn_custom_bg_delete_2);
        ImageButton btnCustomBgDelete3 = (ImageButton) v.findViewById(R.id.btn_custom_bg_delete_3);
        ImageButton btnCustomBgDelete4 = (ImageButton) v.findViewById(R.id.btn_custom_bg_delete_4);

        btnCustomBgChange1.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgChange2.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgChange3.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgChange4.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgDelete1.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgDelete2.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgDelete3.setOnClickListener(onCustomBgButtonsClickListener);
        btnCustomBgDelete4.setOnClickListener(onCustomBgButtonsClickListener);

        mBtnCustomBgSetting = (ImageButton) v.findViewById(R.id.btn_custom_bg_setting);
        mBtnCustomBgSetting.setOnClickListener(onBtnClickListener);

        mBtnPageBgBlank.setOnClickListener(onBtnClickListener);
        mBtnPageBgNarrow.setOnClickListener(onBtnClickListener);
        mBtnPageBgCollege.setOnClickListener(onBtnClickListener);
        mBtnPageBgCornell.setOnClickListener(onBtnClickListener);
        mBtnPageBgTodo.setOnClickListener(onBtnClickListener);
        mBtnPageBgMeeting.setOnClickListener(onBtnClickListener);
        mBtnPageBgDiary.setOnClickListener(onBtnClickListener);
        mBtnPageBgQuadrangle.setOnClickListener(onBtnClickListener);
        mBtnPageBgStave.setOnClickListener(onBtnClickListener);
        mBtnPageBgCalligraphySmall.setOnClickListener(onBtnClickListener);
        mBtnPageBgCalligraphyBig.setOnClickListener(onBtnClickListener);
        mBtnPageBgCustomized.setOnClickListener(onBtnClickListener);

        mBtnCustomBg1.setOnClickListener(onBtnClickListener);
        mBtnCustomBg2.setOnClickListener(onBtnClickListener);
        mBtnCustomBg3.setOnClickListener(onBtnClickListener);
        mBtnCustomBg4.setOnClickListener(onBtnClickListener);

        setPageBackgroundValue(mToolboxConfiguration.getPageBackground());
        setUserDefinedButtonView();
    }

    private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pageBackground = PageBackground.BLANK;
            PageBackgroundChangeEvent event = new PageBackgroundChangeEvent();
            switch (v.getId()) {
                case R.id.btn_page_bg_blank:
                    pageBackground = PageBackground.BLANK;
                    event.setBackgroundType(Paper.EMPTY);
                    break;
                case R.id.btn_page_bg_narrow:
                    pageBackground = PageBackground.NARROW;
                    event.setBackgroundType(Paper.NARROWRULED);
                    break;
                case R.id.btn_page_bg_college:
                    pageBackground = PageBackground.COLLEGE;
                    event.setBackgroundType(Paper.COLLEGERULED);
                    break;
                case R.id.btn_page_bg_cornell:
                    pageBackground = PageBackground.CORNELL;
                    event.setBackgroundType(Paper.CORNELLNOTES);
                    break;
                case R.id.btn_page_bg_todo:
                    pageBackground = PageBackground.TODO;
                    event.setBackgroundType(Paper.TODOLIST);
                    break;
                case R.id.btn_page_bg_meeting:
                    pageBackground = PageBackground.MEETING;
                    event.setBackgroundType(Paper.MINUTES);
                    break;
                case R.id.btn_page_bg_diary:
                    pageBackground = PageBackground.DIARY;
                    event.setBackgroundType(Paper.DIARY);
                    break;
                case R.id.btn_page_bg_quadrangle:
                    pageBackground = PageBackground.QUADRANGLE;
                    event.setBackgroundType(Paper.QUADPAPER);
                    break;
                case R.id.btn_page_bg_stave:
                    pageBackground = PageBackground.STAVE;
                    event.setBackgroundType(Paper.STAVE);
                    break;
                case R.id.btn_page_bg_calligraphy_small:
                    pageBackground = PageBackground.CALLIGRAPHY_SMALL;
                    event.setBackgroundType(Paper.CALLIGRAPHY_SMALL);
                    break;
                case R.id.btn_page_bg_calligraphy_big:
                    pageBackground = PageBackground.CALLIGRAPHY_BIG;
                    event.setBackgroundType(Paper.CALLIGRAPHY_BIG);
                    break;
                case R.id.btn_page_bg_customized:
                    pageBackground = PageBackground.CUSTOMIZED;
                    event.setBackgroundType(Paper.CUSTOMIZED);
                    break;
                case R.id.btn_custom_bg_1:
                    pageBackground = PageBackground.CUSTOMIZED;
                    event.setBackgroundType(Paper.USER_DEFINED_1);
                    break;
                case R.id.btn_custom_bg_2:
                    pageBackground = PageBackground.CUSTOMIZED;
                    event.setBackgroundType(Paper.USER_DEFINED_2);
                    break;
                case R.id.btn_custom_bg_3:
                    pageBackground = PageBackground.CUSTOMIZED;
                    event.setBackgroundType(Paper.USER_DEFINED_3);
                    break;
                case R.id.btn_custom_bg_4:
                    pageBackground = PageBackground.CUSTOMIZED;
                    event.setBackgroundType(Paper.USER_DEFINED_4);
                    break;
                case R.id.btn_custom_bg_setting:
                    mCustomBgBtnSettingMode = !mCustomBgBtnSettingMode;
                    changeUserDefineButtonMode();
                    return;
            }
            EventBus.getDefault().post(event);
            setPageBackgroundValue(pageBackground);
            dismiss();
        }
    };

    private View.OnClickListener onCustomBgButtonsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_custom_bg_delete_1:
                    mToolboxConfiguration.setPageBackgroundUserDefined1("");
                    mBtnCustomBg1.setImageBitmap(null);
                    mBtnCustomBg1.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
                    mLayoutCustomBgButtons1.setVisibility(View.GONE);
                    break;
                case R.id.btn_custom_bg_delete_2:
                    mToolboxConfiguration.setPageBackgroundUserDefined2("");
                    mBtnCustomBg2.setImageBitmap(null);
                    mBtnCustomBg2.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
                    mLayoutCustomBgButtons2.setVisibility(View.GONE);
                    break;
                case R.id.btn_custom_bg_delete_3:
                    mToolboxConfiguration.setPageBackgroundUserDefined3("");
                    mBtnCustomBg3.setImageBitmap(null);
                    mBtnCustomBg3.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
                    mLayoutCustomBgButtons3.setVisibility(View.GONE);
                    break;
                case R.id.btn_custom_bg_delete_4:
                    mToolboxConfiguration.setPageBackgroundUserDefined4("");
                    mBtnCustomBg4.setImageBitmap(null);
                    mBtnCustomBg4.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
                    mLayoutCustomBgButtons4.setVisibility(View.GONE);
                    break;
            }

            int requestCode;
            switch (view.getId()) {
                case R.id.btn_custom_bg_change_1:
                    requestCode = REQUEST_USER_DEFINE_BG_1_PICK_IMAGE;
                    break;
                case R.id.btn_custom_bg_change_2:
                    requestCode = REQUEST_USER_DEFINE_BG_2_PICK_IMAGE;
                    break;
                case R.id.btn_custom_bg_change_3:
                    requestCode = REQUEST_USER_DEFINE_BG_3_PICK_IMAGE;
                    break;
                case R.id.btn_custom_bg_change_4:
                    requestCode = REQUEST_USER_DEFINE_BG_4_PICK_IMAGE;
                    break;
                default:
                    return;

            }
            Intent intent = new Intent(mCtx, ImagePickerActivity.class);
            mCtx.startActivityForResult(intent, requestCode);
            dismiss();
        }
    };

    private void setPageBackgroundValue(int pageBackground) {
        mBtnPageBgBlank.setSelected(false);
        mBtnPageBgNarrow.setSelected(false);
        mBtnPageBgCollege.setSelected(false);
        mBtnPageBgCornell.setSelected(false);
        mBtnPageBgTodo.setSelected(false);
        mBtnPageBgMeeting.setSelected(false);
        mBtnPageBgDiary.setSelected(false);
        mBtnPageBgQuadrangle.setSelected(false);
        mBtnPageBgStave.setSelected(false);
        mBtnPageBgCalligraphySmall.setSelected(false);
        mBtnPageBgCalligraphyBig.setSelected(false);
        mBtnPageBgCustomized.setSelected(false);

        mBtnCustomBg1.setSelected(false);
        mBtnCustomBg2.setSelected(false);
        mBtnCustomBg3.setSelected(false);
        mBtnCustomBg4.setSelected(false);

        switch (pageBackground) {
            case PageBackground.BLANK:
                mBtnPageBgBlank.setSelected(true);
                break;
            case PageBackground.NARROW:
                mBtnPageBgNarrow.setSelected(true);
                break;
            case PageBackground.COLLEGE:
                mBtnPageBgCollege.setSelected(true);
                break;
            case PageBackground.CORNELL:
                mBtnPageBgCornell.setSelected(true);
                break;
            case PageBackground.TODO:
                mBtnPageBgTodo.setSelected(true);
                break;
            case PageBackground.MEETING:
                mBtnPageBgMeeting.setSelected(true);
                break;
            case PageBackground.DIARY:
                mBtnPageBgDiary.setSelected(true);
                break;
            case PageBackground.QUADRANGLE:
                mBtnPageBgQuadrangle.setSelected(true);
                break;
            case PageBackground.STAVE:
                mBtnPageBgStave.setSelected(true);
                break;
            case PageBackground.CALLIGRAPHY_SMALL:
                mBtnPageBgCalligraphySmall.setSelected(true);
                break;
            case PageBackground.CALLIGRAPHY_BIG:
                mBtnPageBgCalligraphyBig.setSelected(true);
                break;
            case PageBackground.CUSTOMIZED:
                mBtnPageBgCustomized.setSelected(true);
                break;
        }
    }

    private void setUserDefinedButtonView() {
        String bgPath1 = mToolboxConfiguration.getPageBackgroundUserDefined1();
        String bgPath2 = mToolboxConfiguration.getPageBackgroundUserDefined2();
        String bgPath3 = mToolboxConfiguration.getPageBackgroundUserDefined3();
        String bgPath4 = mToolboxConfiguration.getPageBackgroundUserDefined4();
        int reqWidth, reqHeight;
        if (Global.getCurrentBook().isLandscape()) {
            reqWidth = 140;
            reqHeight = 105;
        } else {
            reqWidth = 105;
            reqHeight = 140;
        }

        if (!bgPath1.isEmpty() && new File(bgPath1).exists()) {
            new BitmapWorkerTask(mBtnCustomBg1, reqWidth, reqHeight).execute(bgPath1);
            mBtnCustomBg1.setBackgroundResource(R.drawable.ic_page_bg_user_defined);
        } else {
            mBtnCustomBg1.setImageBitmap(null);
            mBtnCustomBg1.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
            mLayoutCustomBgButtons1.setVisibility(View.GONE);
        }

        if (!bgPath2.isEmpty() && new File(bgPath2).exists()) {
            new BitmapWorkerTask(mBtnCustomBg2, reqWidth, reqHeight).execute(bgPath2);
            mBtnCustomBg2.setBackgroundResource(R.drawable.ic_page_bg_user_defined);
        } else {
            mBtnCustomBg2.setImageBitmap(null);
            mBtnCustomBg2.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
            mLayoutCustomBgButtons2.setVisibility(View.GONE);
        }

        if (!bgPath3.isEmpty() && new File(bgPath3).exists()) {
            new BitmapWorkerTask(mBtnCustomBg3, reqWidth, reqHeight).execute(bgPath3);
            mBtnCustomBg3.setBackgroundResource(R.drawable.ic_page_bg_user_defined);
        } else {
            mBtnCustomBg3.setImageBitmap(null);
            mBtnCustomBg3.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
            mLayoutCustomBgButtons3.setVisibility(View.GONE);
        }

        if (!bgPath4.isEmpty() && new File(bgPath4).exists()) {
            new BitmapWorkerTask(mBtnCustomBg4, reqWidth, reqHeight).execute(bgPath4);
            mBtnCustomBg4.setBackgroundResource(R.drawable.ic_page_bg_user_defined);
        } else {
            mBtnCustomBg4.setImageBitmap(null);
            mBtnCustomBg4.setBackgroundResource(R.drawable.ic_page_bg_user_defined_add);
            mLayoutCustomBgButtons4.setVisibility(View.GONE);
        }
    }

    private void changeUserDefineButtonMode() {
        if (mCustomBgBtnSettingMode) {
            if (!mToolboxConfiguration.getPageBackgroundUserDefined1().isEmpty())
                mLayoutCustomBgButtons1.setVisibility(View.VISIBLE);

            if (!mToolboxConfiguration.getPageBackgroundUserDefined2().isEmpty())
                mLayoutCustomBgButtons2.setVisibility(View.VISIBLE);

            if (!mToolboxConfiguration.getPageBackgroundUserDefined3().isEmpty())
                mLayoutCustomBgButtons3.setVisibility(View.VISIBLE);

            if (!mToolboxConfiguration.getPageBackgroundUserDefined4().isEmpty())
                mLayoutCustomBgButtons4.setVisibility(View.VISIBLE);

            mBtnCustomBgSetting.setBackgroundResource(R.drawable.ic_customized_page_bg_close);
        } else {
            mLayoutCustomBgButtons1.setVisibility(View.GONE);
            mLayoutCustomBgButtons2.setVisibility(View.GONE);
            mLayoutCustomBgButtons3.setVisibility(View.GONE);
            mLayoutCustomBgButtons4.setVisibility(View.GONE);
            mBtnCustomBgSetting.setBackgroundResource(R.drawable.ic_customized_page_bg_setting);
        }
    }
}
