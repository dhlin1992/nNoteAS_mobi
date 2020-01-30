package ntx.note;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.UUID;

import name.vbraun.lib.pen.Hardware;
import name.vbraun.view.write.HandwriterView;
import ntx.note.data.Bookshelf;
import ntx.note.data.StorageAndroid;

public class ActivityAsyncBase extends Activity {

    private final static String TAG = "ActivityAsyncBase";
    private static Context context;
    private static boolean firstRun = true;
    public static boolean IsInitialize = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        StorageAndroid.initialize(context);
    }

    private void removeUnusedPreferences() {
        final String KEY_DEBUG_OPTIONS = HandwriterView.KEY_DEBUG_OPTIONS;
        final String KEY_HIDE_SYSTEM_BAR = Preferences.KEY_HIDE_SYSTEM_BAR;
        final String KEY_OVERRIDE_PEN_TYPE = Hardware.KEY_OVERRIDE_PEN_TYPE;
        final String KEY_ONLY_PEN_INPUT_OBSOLETE = "only_pen_input";

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(KEY_ONLY_PEN_INPUT_OBSOLETE);  // obsoleted

        if (Global.releaseModeOEM) {
            editor.remove(KEY_HIDE_SYSTEM_BAR);
            editor.remove(KEY_DEBUG_OPTIONS);
            editor.remove(KEY_OVERRIDE_PEN_TYPE);
        }
        editor.commit();
    }

    public void asynchronizeInitialStorage(@Nullable final UUID bookUUID) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                IsInitialize = false;
                Log.d(TAG, "Start initial storage");
                boolean isInitial = Bookshelf.initialize();

                if (isInitial) {
                    Bookshelf.getInstance().setCurrentBook(bookUUID, true);
                    onInitializationReload();
                }

                if (firstRun) {
                    removeUnusedPreferences();
                    firstRun = false;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                Log.d(TAG, "End initial storage");
                onInitializationFinished();
            }
        }.execute();
    }

    public void synchronizeInitialStorage() {
        IsInitialize = false;
        Log.d(TAG, "Start initial storage");
        Bookshelf.initialize();
        Log.d(TAG, "End initial storage");
        IsInitialize = true;
        if (firstRun) {
            removeUnusedPreferences();
            firstRun = false;
        }
    }

    protected void onInitializationFinished() {
        IsInitialize = true;
    }

    protected void onInitializationReload() {
        //Need override
    }
}
