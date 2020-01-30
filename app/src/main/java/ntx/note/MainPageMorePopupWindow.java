package ntx.note;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.greenrobot.eventbus.EventBus;

import ntx.note2.R;

import static ntx.note.ActivityAsyncBase.IsInitialize;

public class MainPageMorePopupWindow extends RelativePopupWindow {
    private static MainPageMorePopupWindow mInstance;

    public static MainPageMorePopupWindow getInstance(Activity ctx) {
        synchronized (MainPageMorePopupWindow.class) {
            if (mInstance == null) {
                mInstance = new MainPageMorePopupWindow(ctx);
            }
            return mInstance;
        }
    }

    private MainPageMorePopupWindow(Activity ctx) {
        super(ctx);
        setContentView(LayoutInflater.from(ctx).inflate(R.layout.popupwindow_main_page_more, null));
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View popupView = getContentView();

        initView(popupView);
    }

    private void initView(View v) {
        ImageButton btnSort = (ImageButton) v.findViewById(R.id.ibtn_more_sort);
        ImageButton btnPreview = (ImageButton) v.findViewById(R.id.ibtn_more_preview);
        ImageButton btnImport = (ImageButton) v.findViewById(R.id.ibtn_more_import);
        ImageButton btnManage = (ImageButton) v.findViewById(R.id.ibtn_more_manage);
        ImageButton btnTag = (ImageButton) v.findViewById(R.id.ibtn_more_tag);
        ImageButton btnBackup = (ImageButton) v.findViewById(R.id.ibtn_more_backup);

        btnSort.setOnClickListener(onBtnClickListener);
        btnPreview.setOnClickListener(onBtnClickListener);
        btnImport.setOnClickListener(onBtnClickListener);
        btnManage.setOnClickListener(onBtnClickListener);
        btnTag.setOnClickListener(onBtnClickListener);
        btnBackup.setOnClickListener(onBtnClickListener);

    }

    private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibtn_more_sort:
                    callEvent(CallbackEvent.MORE_SORT);
                    break;
                case R.id.ibtn_more_preview:
                    callEvent(CallbackEvent.MORE_PREVIEW);
                    break;
                case R.id.ibtn_more_import:
                    callEvent(CallbackEvent.MORE_IMPORT);
                    break;
                case R.id.ibtn_more_manage:
                    callEvent(CallbackEvent.MORE_MANAGE);
                    break;
                case R.id.ibtn_more_tag:
                    callEvent(CallbackEvent.MORE_TAG);
                    break;
                case R.id.ibtn_more_backup:
                    callEvent(CallbackEvent.MORE_BACKUP);
                    break;
            }
            dismiss();
        }
    };

    private void callEvent(String event) {
        CallbackEvent callbackEvent = new CallbackEvent();
        callbackEvent.setMessage(event);
        EventBus.getDefault().post(callbackEvent);
    }
}
