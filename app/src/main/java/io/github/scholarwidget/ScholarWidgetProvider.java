package io.github.scholarwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

public class ScholarWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "io.github.scholarwidget.action.REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        RefreshJobService.schedulePeriodic(context);
        if (StatsStore.load(context) == null && !StatsStore.getUserId(context).isEmpty()) {
            StatsStore.setRefreshing(context, true);
            RefreshJobService.requestNow(context);
        }
        manager.updateAppWidget(appWidgetIds, buildViews(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH.equals(intent.getAction())) {
            refreshNow(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        RefreshJobService.cancelAll(context);
    }

    /** Shows "Refreshing…" and starts a one-off fetch. */
    public static void refreshNow(Context context) {
        if (!StatsStore.getUserId(context).isEmpty()) {
            StatsStore.setRefreshing(context, true);
            RefreshJobService.requestNow(context);
        }
        updateAll(context);
    }

    /** Redraws every placed instance of the widget from the stored snapshot. */
    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ScholarWidgetProvider.class));
        if (ids != null && ids.length > 0) {
            manager.updateAppWidget(ids, buildViews(context));
        }
    }

    private static RemoteViews buildViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_scholar);
        String userId = StatsStore.getUserId(context);
        ScholarStats s = StatsStore.load(context);
        String error = StatsStore.getError(context);
        boolean refreshing = StatsStore.isRefreshing(context);
        String placeholder = context.getString(R.string.placeholder);

        if (s != null) {
            NumberFormat nf = NumberFormat.getIntegerInstance();
            views.setTextViewText(R.id.title,
                    s.name.isEmpty() ? context.getString(R.string.app_name) : s.name);
            views.setTextViewText(R.id.citations, nf.format(s.citationsAll));
            views.setTextViewText(R.id.hindex, nf.format(s.hAll));
            views.setTextViewText(R.id.i10, nf.format(s.i10All));

            String since = s.sinceYear > 0 ? "since " + s.sinceYear + ": " : "recent: ";
            views.setTextViewText(R.id.citations_since, since + nf.format(s.citationsSince));
            views.setTextViewText(R.id.hindex_since, since + nf.format(s.hSince));
            views.setTextViewText(R.id.i10_since, since + nf.format(s.i10Since));

            Bitmap chart = ChartRenderer.render(context, s);
            if (chart != null) {
                views.setImageViewBitmap(R.id.chart, chart);
                views.setViewVisibility(R.id.chart, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.chart, View.GONE);
            }
        } else {
            // No snapshot (first run, or the profile was just changed): blank everything.
            views.setTextViewText(R.id.title, context.getString(R.string.app_name));
            views.setTextViewText(R.id.citations, placeholder);
            views.setTextViewText(R.id.hindex, placeholder);
            views.setTextViewText(R.id.i10, placeholder);
            views.setTextViewText(R.id.citations_since, "");
            views.setTextViewText(R.id.hindex_since, "");
            views.setTextViewText(R.id.i10_since, "");
            views.setViewVisibility(R.id.chart, View.GONE);
        }

        String status;
        if (userId.isEmpty()) {
            status = "Tap to choose a Scholar profile";
        } else if (refreshing) {
            status = "Refreshing…";
        } else if (s == null) {
            status = error != null ? "Failed: " + error + " (tap ↻ to retry)" : "Tap ↻ to load";
        } else {
            String when = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(new Date(s.fetchedAt));
            status = "Updated " + when + (error != null ? " · last try failed: " + error : "");
        }
        views.setTextViewText(R.id.status, status);

        // Refresh icon -> fetch now.
        Intent refresh = new Intent(context, ScholarWidgetProvider.class).setAction(ACTION_REFRESH);
        views.setOnClickPendingIntent(R.id.refresh, PendingIntent.getBroadcast(
                context, 0, refresh,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        // Anywhere else -> open the profile in the browser, or the app if none is set.
        Intent open;
        if (userId.isEmpty()) {
            open = new Intent(context, MainActivity.class);
        } else {
            open = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.profileUrl(userId)));
        }
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(
                context, 1, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        return views;
    }
}
