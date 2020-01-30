package ntx.note.bookshelf;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import ntx.note2.R;

/**
 * Created by karote on 2018/11/21.
 */

public class CreateNewNotePopupWindow extends PopupWindow {
    private Context mCtx;
    private View.OnClickListener callback;

    public CreateNewNotePopupWindow(Context ctx) {
        this.mCtx = ctx;

        setContentView(LayoutInflater.from(ctx).inflate(R.layout.popupwindow_create_new_note, null));
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View popupView = getContentView();

        LinearLayout btnCreateVerticalNote = (LinearLayout) popupView.findViewById(R.id.btn_create_vertical_note);
        LinearLayout btnCreateHorizontalNote = (LinearLayout) popupView.findViewById(R.id.btn_create_horizontal_note);
        btnCreateVerticalNote.setTag(false);
        btnCreateHorizontalNote.setTag(true);
        btnCreateVerticalNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onClick(view);
            }
        });
        btnCreateHorizontalNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onClick(view);
            }
        });
    }

    public void setOnButtonClickListener(View.OnClickListener listener) {
        this.callback = listener;
    }

}
