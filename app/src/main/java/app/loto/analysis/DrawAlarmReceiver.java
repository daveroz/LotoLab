package app.loto.analysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** The alarm set for just after a draw: hands over to a job that runs as soon as there's a network. */
public class DrawAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Notifier.runSoon(context);
    }
}
