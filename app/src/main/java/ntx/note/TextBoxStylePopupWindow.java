package ntx.note;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;

import name.vbraun.view.write.TextBoxSettingValue;
import ntx.note.export.MySpinner;
import ntx.note2.R;
import utility.ToggleImageButton;

import static ntx.note.Global.getResources;

public class TextBoxStylePopupWindow extends RelativePopupWindow {

    private int[] INT_ARRAY_TEXT_BOX_FONT_SIZE;

    private ImageButton mBtnTextColorBlack;
    private ImageButton mBtnTextColorDarkGray;
    private ImageButton mBtnTextColorGray;
    private ImageButton mBtnTextColorLightGray;
    private ImageButton mBtnTextColorWhite;
    private Handler mHandler = new Handler();

    public TextBoxStylePopupWindow(Activity ctx, int textFontSize, int textColor,
                                   boolean isBold, boolean isItalic, boolean isUnderLine) {
        super(ctx);

        setContentView(LayoutInflater.from(ctx).inflate(R.layout.popupwindow_text_box_style, null));
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(false);
        setFocusable(false);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View popupView = getContentView();

        initView(popupView, textFontSize, textColor, isBold, isItalic, isUnderLine);
    }

    private void initView(View v, int textFontSize, int textColor,
                          boolean isBold, boolean isItalic, boolean isUnderLine) {
        Button btnTextBoxStyleClose = (Button) v.findViewById(R.id.btn_text_box_style_close);
        btnTextBoxStyleClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        String[] STRING_ARRAY_TEXT_BOX_FONT_SIZE = getResources().getStringArray(R.array.text_box_font_size_str_array);
        INT_ARRAY_TEXT_BOX_FONT_SIZE = getResources().getIntArray(R.array.text_box_font_size_array);
        LinkedList<String> font_size_values = new LinkedList<String>();
        ArrayAdapter<CharSequence> fontSizeValuesAdapter = new ArrayAdapter(mCtx, R.layout.textbox_font_size_spinner_item, font_size_values);
        fontSizeValuesAdapter.addAll(STRING_ARRAY_TEXT_BOX_FONT_SIZE);
        MySpinner spinnerTextFontSize = (MySpinner) v.findViewById(R.id.sp_text_box_font_size);
        spinnerTextFontSize.setAdapter(fontSizeValuesAdapter);
        spinnerTextFontSize.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int fontSize = INT_ARRAY_TEXT_BOX_FONT_SIZE[position];

                TextBoxSettingValue textBoxSettingValue = new TextBoxSettingValue(null, fontSize, null, null, null, null);
                EventBus.getDefault().post(textBoxSettingValue);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CallbackEvent event = new CallbackEvent();
                        event.setMessage(CallbackEvent.DO_DRAW_VIEW_INVALIDATE);
                        EventBus.getDefault().post(event);
                    }
                }, 500);
            }
        });
        int spinnerTextFontSizeIndex = -1;
        for (int i = 0; i < INT_ARRAY_TEXT_BOX_FONT_SIZE.length; i++) {
            if (INT_ARRAY_TEXT_BOX_FONT_SIZE[i] == textFontSize)
                spinnerTextFontSizeIndex = i;
        }
        if (spinnerTextFontSizeIndex > 0)
            spinnerTextFontSize.setSelection(spinnerTextFontSizeIndex);

        ToggleImageButton btnBold = (ToggleImageButton) v.findViewById(R.id.btn_text_box_font_bold);
        btnBold.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(boolean value) {
                TextBoxSettingValue textBoxSettingValue = new TextBoxSettingValue(null, null, null, value, null, null);
                EventBus.getDefault().post(textBoxSettingValue);
            }
        });
        if (isBold)
            btnBold.setChecked();
        else
            btnBold.setUnchecked();

        ToggleImageButton btnItalic = (ToggleImageButton) v.findViewById(R.id.btn_text_box_font_italic);
        btnItalic.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(boolean value) {
                TextBoxSettingValue textBoxSettingValue = new TextBoxSettingValue(null, null, null, null, value, null);
                EventBus.getDefault().post(textBoxSettingValue);
            }
        });
        if (isItalic)
            btnItalic.setChecked();
        else
            btnItalic.setUnchecked();

        ToggleImageButton btnUnderLine = (ToggleImageButton) v.findViewById(R.id.btn_text_box_font_underline);
        btnUnderLine.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(boolean value) {
                TextBoxSettingValue textBoxSettingValue = new TextBoxSettingValue(null, null, null, null, null, value);
                EventBus.getDefault().post(textBoxSettingValue);
            }
        });
        if (isUnderLine)
            btnUnderLine.setChecked();
        else
            btnUnderLine.setUnchecked();

        mBtnTextColorBlack = (ImageButton) v.findViewById(R.id.btn_text_color_black);
        mBtnTextColorBlack.setOnClickListener(onBtnTextColorClickListener);

        mBtnTextColorDarkGray = (ImageButton) v.findViewById(R.id.btn_text_color_dark_gray);
        mBtnTextColorDarkGray.setOnClickListener(onBtnTextColorClickListener);

        mBtnTextColorGray = (ImageButton) v.findViewById(R.id.btn_text_color_gray);
        mBtnTextColorGray.setOnClickListener(onBtnTextColorClickListener);

        mBtnTextColorLightGray = (ImageButton) v.findViewById(R.id.btn_text_color_light_gray);
        mBtnTextColorLightGray.setOnClickListener(onBtnTextColorClickListener);

        mBtnTextColorWhite = (ImageButton) v.findViewById(R.id.btn_text_color_white);
        mBtnTextColorWhite.setOnClickListener(onBtnTextColorClickListener);

        setTextColorValue(textColor);
    }

    private View.OnClickListener onBtnTextColorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int textColorValue = ToolboxConfiguration.TextColor.BLACK;
            switch (view.getId()) {
                case R.id.btn_text_color_black:
                    textColorValue = ToolboxConfiguration.TextColor.BLACK;
                    break;
                case R.id.btn_text_color_dark_gray:
                    textColorValue = ToolboxConfiguration.TextColor.DARK_GRAY;
                    break;
                case R.id.btn_text_color_gray:
                    textColorValue = ToolboxConfiguration.TextColor.GRAY;
                    break;
                case R.id.btn_text_color_light_gray:
                    textColorValue = ToolboxConfiguration.TextColor.LIGHT_GRAY;
                    break;
                case R.id.btn_text_color_white:
                    textColorValue = ToolboxConfiguration.TextColor.WHITE;
                    break;
            }
            setTextColorValue(textColorValue);

            TextBoxSettingValue textBoxSettingValue = new TextBoxSettingValue(null, null, textColorValue, null, null, null);
            EventBus.getDefault().post(textBoxSettingValue);
        }
    };

    private void setTextColorValue(int color) {
        mBtnTextColorBlack.setSelected(false);
        mBtnTextColorDarkGray.setSelected(false);
        mBtnTextColorGray.setSelected(false);
        mBtnTextColorLightGray.setSelected(false);
        mBtnTextColorWhite.setSelected(false);

        switch (color) {
            case ToolboxConfiguration.TextColor.BLACK:
                mBtnTextColorBlack.setSelected(true);
                break;
            case ToolboxConfiguration.TextColor.DARK_GRAY:
                mBtnTextColorDarkGray.setSelected(true);
                break;
            case ToolboxConfiguration.TextColor.GRAY:
                mBtnTextColorGray.setSelected(true);
                break;
            case ToolboxConfiguration.TextColor.LIGHT_GRAY:
                mBtnTextColorLightGray.setSelected(true);
                break;
            case ToolboxConfiguration.TextColor.WHITE:
                mBtnTextColorWhite.setSelected(true);
                break;
        }
    }
}
