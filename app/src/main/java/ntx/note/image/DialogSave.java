package ntx.note.image;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;

import ntx.note2.R;

public class DialogSave extends DialogBase {
    private final static String TAG = "DialogSave";
    private int rotation;
    private Rect crop = null;
    private Boolean isCrop = false;
    private final static String KEY_ROTATION = "rotation";
    private final static String KEY_CROP_LEFT = "crop_left";
    private final static String KEY_CROP_RIGHT = "crop_right";
    private final static String KEY_CROP_TOP = "crop_top";
    private final static String KEY_CROP_BOTTOM = "crop_bottom";
    private final static String KEY_IS_CROP = "isCrop";

    public static DialogSave newInstance(Uri sourceUri, File destination, int rotation, Rect crop, Boolean isCrop) {
        DialogSave fragment = new DialogSave();
        Bundle args = DialogBase.storeArgs(sourceUri, destination);
        args.putInt(KEY_ROTATION, rotation);
        if (crop != null) {
            args.putInt(KEY_CROP_LEFT, crop.left);
            args.putInt(KEY_CROP_RIGHT, crop.right);
            args.putInt(KEY_CROP_TOP, crop.top);
            args.putInt(KEY_CROP_BOTTOM, crop.bottom);
            args.putBoolean(KEY_IS_CROP, isCrop);
        }
        fragment.setArguments(args);
        return fragment;
    }

    protected void loadArgs(Bundle bundle) {
        super.loadArgs(bundle);
        this.rotation = bundle.getInt(KEY_ROTATION);
        if (bundle.containsKey(KEY_CROP_LEFT)) {
            int left = bundle.getInt(KEY_CROP_LEFT);
            int right = bundle.getInt(KEY_CROP_RIGHT);
            int top = bundle.getInt(KEY_CROP_TOP);
            int bottom = bundle.getInt(KEY_CROP_BOTTOM);
            this.crop = new Rect(left, top, right, bottom);
            this.isCrop = bundle.getBoolean(KEY_IS_CROP);
        }
    }

    @Override
    protected void initProgresDialog(ProgressDialog dialog) {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(getTitle());
        dialog.setMessage(getMessage());
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private String getTitle() {
        return getResources().getString(R.string.saving);
    }

    private String getMessage() {
        return getResources().getString(R.string.image_editor_saving_message);
    }

    @Override
    protected String getCancelMessage() {
        return getResources().getString(R.string.image_editor_cancel_saving_message);
    }

    @Override
    protected void setProgress(int progress) {
    }

    @Override
    protected ThreadBase makeThread(Uri source, File destination) {
        String path = source.getPath();
        if (path == null) return null;
        File sourceFile = new File(path);
        return new ThreadSave(sourceFile, destination, rotation, crop,this);
    }

    @Override
    protected void onFinish(File file,boolean oom) {
        ImageActivity activity = (ImageActivity) getActivity();
        activity.onSaveFinished(destinationFile, this.isCrop, oom);
    }

}
