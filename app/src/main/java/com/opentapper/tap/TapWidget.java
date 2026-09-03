package com.opentapper.tap;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** ホーム画面ウィジェット：ワンタッチでオーバーレイ表示／開始・停止 */
public class TapWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, build(c));
    }

    /** サービス側から状態変化時に呼ぶ */
    public static void refresh(Context c) {
        try {
            AppWidgetManager mgr = AppWidgetManager.getInstance(c);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(c, TapWidget.class));
            if (ids != null && ids.length > 0) mgr.updateAppWidget(ids, build(c));
        } catch (Throwable t) { }
    }

    private static int res(Context c, String type, String name) {
        return c.getResources().getIdentifier(name, type, c.getPackageName());
    }

    private static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), res(c, "layout", "widget"));
        boolean running = TapService.isRunning();
        boolean shown = TapService.isOverlayShown();
        rv.setInt(res(c, "id", "widget_bg"), "setBackgroundResource",
                res(c, "drawable", running ? "widget_bg_run" : "widget_bg"));
        rv.setTextViewText(res(c, "id", "widget_icon"), running ? "■" : (shown ? "▶" : "⚡"));
        L.init(c);
        rv.setTextViewText(res(c, "id", "widget_label"), running ? L.s("w_stop") : (shown ? L.s("w_start") : L.s("w_launch")));
        Intent i = new Intent(c, QuickActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(c, 0, i, flags);
        rv.setOnClickPendingIntent(res(c, "id", "widget_root"), pi);
        return rv;
    }
}
