package app.loto.analysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Alarms don't survive a restart or an update: arm the next check again. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) return;
        final PendingResult done = goAsync();
        new Thread(() -> {
            try {
                Notifier.schedule(context);
            } finally {
                done.finish();
            }
        }).start();
    }
}
