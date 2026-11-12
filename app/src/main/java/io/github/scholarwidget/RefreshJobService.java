package io.github.scholarwidget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;

/** Fetches the profile in the background and redraws the widget. */
public class RefreshJobService extends JobService {
    private static final int JOB_PERIODIC = 1;
    private static final int JOB_NOW = 2;

    /** Ensures the recurring refresh is scheduled (safe to call repeatedly). */
    public static void schedulePeriodic(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler == null || scheduler.getPendingJob(JOB_PERIODIC) != null) {
            return;
        }
        JobInfo job = new JobInfo.Builder(JOB_PERIODIC, component(context))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(TimeUnit.HOURS.toMillis(Config.REFRESH_HOURS))
                .setPersisted(true)
                .build();
        scheduler.schedule(job);
    }

    /** Runs a one-off refresh as soon as there is a network connection. */
    public static void requestNow(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler == null) {
            return;
        }
        JobInfo job = new JobInfo.Builder(JOB_NOW, component(context))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(TimeUnit.MINUTES.toMillis(1))
                .build();
        scheduler.schedule(job);
    }

    public static void cancelAll(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler != null) {
            scheduler.cancel(JOB_PERIODIC);
            scheduler.cancel(JOB_NOW);
        }
    }

    private static ComponentName component(Context context) {
        return new ComponentName(context, RefreshJobService.class);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        final Context app = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String userId = StatsStore.getUserId(app);
                if (userId.isEmpty()) {
                    StatsStore.setRefreshing(app, false);
                } else {
                    try {
                        ScholarStats stats = ScholarFetcher.fetch(userId);
                        // Discard the result if the profile was changed mid-fetch.
                        if (userId.equals(StatsStore.getUserId(app))) {
                            StatsStore.save(app, stats);
                        }
                    } catch (Exception e) {
                        if (userId.equals(StatsStore.getUserId(app))) {
                            String message = e.getMessage();
                            StatsStore.setError(app,
                                    message != null ? message : e.getClass().getSimpleName());
                        }
                    }
                }
                ScholarWidgetProvider.updateAll(app);
                jobFinished(params, false);
            }
        }, "scholar-refresh").start();
        return true; // work continues on the background thread
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        StatsStore.setRefreshing(getApplicationContext(), false);
        return false;
    }
}
