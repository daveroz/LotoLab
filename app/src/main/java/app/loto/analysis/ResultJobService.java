package app.loto.analysis;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Fetches the results that are due and announces the new ones (see {@link Notifier}). */
public class ResultJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            try {
                Notifier.check(getApplicationContext());
            } catch (RuntimeException ignored) {
                // a failed check just waits for the next alarm
            } finally {
                jobFinished(params, false);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
