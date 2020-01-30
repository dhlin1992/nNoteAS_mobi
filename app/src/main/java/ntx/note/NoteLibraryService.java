package ntx.note;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import ntx.note.data.Bookshelf;
import ntx.note.data.StorageAndroid;

public class NoteLibraryService extends Service {

    private final static String TAG = "NoteLibraryService";
    private final static int retry = 30000;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void handleStart() {

        Log.d(TAG, "start initialize");
        StorageAndroid.initialize(this);
        Bookshelf.initialize();
        Log.d(TAG, "end initialize");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleStart();
            }
        }, retry);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startId == Service.START_STICKY) {
            handleStart();
            return super.onStartCommand(intent, flags, startId);
        } else {
            return Service.START_NOT_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
