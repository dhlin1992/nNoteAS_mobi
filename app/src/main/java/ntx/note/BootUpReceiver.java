package ntx.note;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
    private final static String TAG = "BootUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Log.d(TAG, "Boot completed ! Start NoteLibraryService");
//            Intent startServiceIntent = new Intent(context, NoteLibraryService.class);
//            context.startService(startServiceIntent);
        }
    }
}
