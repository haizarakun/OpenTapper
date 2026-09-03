package com.opentapper.tap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;


/** アクセシビリティサービス本体：ジェスチャ送出ループ */
public class TapService extends AccessibilityService {
    private static TapService sInstance;

    Config cfg;
    Handler handler;
    OverlayUI ui;

    // 実行状態
    boolean running = false;
    int sessionTaps = 0;
    long sessionStart = 0;
    long totalTaps = 0;
    private int rrIndex = 0;

    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private String targetPackage;
    private NotificationManager nm;
    private static final int NOTIF_ID = 1337;
    private static final String CHANNEL = "opentapper_status";

    public static void requestShowOverlay(Context c) {
        TapService s = sInstance;
        if (s == null) {
            Toast.makeText(c, L.s("need_a11y"), Toast.LENGTH_LONG).show();
            return;
        }
        s.ui.show();
    }

    public static boolean isConnected() { return sInstance != null; }
    public static boolean isRunning() { TapService s = sInstance; return s != null && s.running; }
    public static boolean isOverlayShown() { TapService s = sInstance; return s != null && s.ui != null && s.ui.isShown(); }
    public static void requestToggle() { TapService s = sInstance; if (s != null) s.toggle(); }
    void widgetRefresh() { TapWidget.refresh(this); }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        L.init(this);
        handler = new Handler(Looper.getMainLooper());
        cfg = new Config(getSharedPreferences("opentapper", MODE_PRIVATE));
        cfg.load();
        totalTaps = getSharedPreferences("opentapper", MODE_PRIVATE).getLong("totalTaps", 0);
        try { vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); } catch (Throwable t) { vibrator = null; }
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "opentapper:run");
            wakeLock.setReferenceCounted(false);
        } catch (Throwable t) { wakeLock = null; }
        try {
            nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26 && nm != null) {
                NotificationChannel ch = new NotificationChannel(CHANNEL, L.s("channel"), NotificationManager.IMPORTANCE_LOW);
                nm.createNotificationChannel(ch);
            }
        } catch (Throwable t) { nm = null; }
        ui = new OverlayUI(this);
        widgetRefresh();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;
        String p = pkg.toString();
        if (p.equals(getPackageName())) return; // 自アプリのUI操作は無視
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (running && cfg != null && cfg.autoPause && targetPackage != null && !p.equals(targetPackage)) {
                stopTapping();
                if (ui != null) ui.toast(L.s("auto_paused"));
            }
            if (!running) targetPackage = p; // 実行していない間は「今見ているアプリ」を追随
        }
    }

    @Override
    public void onInterrupt() { stopTapping(); }

    /** 音量ダウンキーで開始/停止（オーバーレイ表示中のみ横取り） */
    @Override
    protected boolean onKeyEvent(android.view.KeyEvent event) {
        if (cfg == null || !cfg.volumeKey || ui == null || !ui.isShown()) return false;
        if (event.getKeyCode() != android.view.KeyEvent.KEYCODE_VOLUME_DOWN) return false;
        if (event.getAction() == android.view.KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) toggle();
        return true;
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        stopTapping();
        if (ui != null) ui.hide();
        sInstance = null;
        TapWidget.refresh(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopTapping();
        if (ui != null) ui.hide();
        sInstance = null;
        super.onDestroy();
    }

    // ---------------- 実行制御 ----------------

    void startTapping() {
        if (running) return;
        if (ui.markerCount() == 0) {
            ui.toast(L.s("add_point_first"));
            return;
        }
        if (targetPackage == null) targetPackage = getPackageName();
        running = true;
        sessionTaps = 0;
        sessionStart = System.currentTimeMillis();
        rrIndex = 0;
        ui.setMarkersPassthrough(true);
        ui.onRunningChanged();
        feedback(50);
        showNotification();
        widgetRefresh();
        if (cfg.keepAwake && wakeLock != null) {
            try { wakeLock.acquire(30 * 60 * 1000L); } catch (Throwable t) { }
        }
        seq++;
        dispatchBatch();
    }

    void stopTapping() {
        if (!running) return;
        running = false;
        if (wakeLock != null) { try { wakeLock.release(); } catch (Throwable t) { } }
        seq++;
        handler.removeCallbacks(tick);
        if (ui != null) {
            ui.setMarkersPassthrough(false);
            ui.onRunningChanged();
        }
        feedback(100);
        hideNotification();
        widgetRefresh();
        getSharedPreferences("opentapper", MODE_PRIVATE).edit().putLong("totalTaps", totalTaps).apply();
    }

    void toggle() { if (running) stopTapping(); else startTapping(); }

    // ---- 高速ループ：1ジェスチャに最大10ストロークをまとめて送出 ----
    private int seq = 0;

    private final Runnable tick = new Runnable() {
        @Override public void run() { if (running) dispatchBatch(); }
    };

    private void dispatchBatch() {
        // 制限チェック
        if (cfg.limitCount && sessionTaps >= cfg.maxCount) { stopTapping(); ui.toast(L.s("limit_count")); return; }
        if (cfg.limitTime && System.currentTimeMillis() - sessionStart >= cfg.maxSeconds * 1000L) { stopTapping(); ui.toast(L.s("limit_time")); return; }

        int n = ui.markerCount();
        if (n == 0) { stopTapping(); return; }
        // 端末保護：ストローク間に必ず空白時間を確保（同時刻の UP/DOWN 連続は system_server を落とす）
        int interval = Math.max(Config.MIN_INTERVAL, cfg.intervalMs);
        long hold = Math.max(1, Math.min(cfg.holdMs, interval - Config.MIN_GAP));
        int maxStrokes = 10;
        try { maxStrokes = GestureDescription.getMaxStrokeCount(); } catch (Throwable t) { }
        int perRound = cfg.roundRobin ? 1 : Math.min(n, maxStrokes);
        // 複数点同時タップは1ジェスチャ1ラウンド（多点×多ラウンドは避ける）。単点は最大6ラウンド/約250ms
        int rounds = perRound > 1 ? 1 : Math.max(1, Math.min(6, 250 / interval + 1));
        if (cfg.limitCount) rounds = Math.max(1, Math.min(rounds, cfg.maxCount - sessionTaps));

        GestureDescription.Builder b = new GestureDescription.Builder();
        for (int r = 0; r < rounds; r++) {
            long start = (long) r * interval;
            if (cfg.roundRobin) {
                int[] p = ui.markerCenter(rrIndex % n);
                rrIndex = (rrIndex + 1) % n;
                b.addStroke(stroke(p[0], p[1], start, hold));
            } else {
                for (int i = 0; i < perRound; i++) {
                    int[] p = ui.markerCenter(i);
                    b.addStroke(stroke(p[0], p[1], start, hold));
                }
            }
        }
        final long total = (long) rounds * interval;
        final int mySeq = ++seq;
        sessionTaps += rounds;
        totalTaps += rounds;
        ui.onTapped();
        boolean ok = false;
        try {
            ok = dispatchGesture(b.build(), new GestureResultCallback() {
                @Override public void onCompleted(GestureDescription g) { next(mySeq); }
                @Override public void onCancelled(GestureDescription g) { next(mySeq); }
            }, null);
        } catch (Throwable t) { ok = false; }
        // コールバックが来ない場合の保険
        handler.postDelayed(new Runnable() { public void run() { next(mySeq); } }, ok ? total + 60 : Math.max(60, total));
    }

    private void next(int s) {
        if (!running || s != seq) return;
        seq++; // 同じバッチからの二重呼び出しを無効化
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, Config.MIN_GAP); // 連続注入の間に必ず隙間を入れる
    }

    private static GestureDescription.StrokeDescription stroke(int x, int y, long start, long hold) {
        Path path = new Path();
        path.moveTo(Math.max(0, x), Math.max(0, y));
        return new GestureDescription.StrokeDescription(path, start, hold);
    }

    // ---------------- フィードバック ----------------

    private void feedback(long ms) {
        if (!cfg.vibrate || vibrator == null) return;
        try { vibrator.vibrate(ms); } catch (Throwable t) { }
    }

    private void showNotification() {
        if (!cfg.notify || nm == null) return;
        try {
            Notification.Builder nb = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
            nb.setContentTitle(L.s("notif_title"))
              .setContentText(L.f("notif_text", cfg.intervalMs, ui.markerCount()))
              .setSmallIcon(android.R.drawable.ic_media_play)
              .setOngoing(true);
            nm.notify(NOTIF_ID, nb.build());
        } catch (Throwable t) { }
    }

    private void hideNotification() {
        if (nm == null) return;
        try { nm.cancel(NOTIF_ID); } catch (Throwable t) { }
    }

    float tapsPerSecond() {
        long el = System.currentTimeMillis() - sessionStart;
        if (el < 500) return 0f;
        return sessionTaps * 1000f / el;
    }
}
