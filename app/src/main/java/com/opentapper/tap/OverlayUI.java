package com.opentapper.tap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;

/** フローティングUI：コントロールパネル / マーカー / 設定シート */
public class OverlayUI {
    private static final int C_BG = 0xE6202124;
    private static final int C_CARD = 0xF21E1E22;
    private static final int C_ACCENT = 0xFF4F8CFF;
    private static final int C_RUN = 0xFF34C759;
    private static final int C_STOP = 0xFFFF453A;
    private static final int C_BTN = 0x33FFFFFF;
    private static final int C_TEXT = 0xFFF2F2F2;
    private static final int C_SUB = 0xFFA0A0A8;
    private static final int[] MARKER_COLORS = {0xFFFF453A, 0xFF34C759, 0xFF4F8CFF, 0xFFFF9F0A, 0xFFBF5AF2, 0xFF64D2FF};

    private final TapService s;
    private final WindowManager wm;
    private final float density;
    private final int markerSize;
    private final int screenW, screenH;

    private LinearLayout panel;
    private WindowManager.LayoutParams panelLp;
    private TextView btnToggle, lblInterval;
    private View bubble;
    private WindowManager.LayoutParams bubbleLp;
    private View sheet;
    private boolean shown = false;
    boolean isShown() { return shown; }

    private final ArrayList<FrameLayout> markers = new ArrayList<FrameLayout>();
    private final ArrayList<WindowManager.LayoutParams> markerLps = new ArrayList<WindowManager.LayoutParams>();

    OverlayUI(TapService s) {
        this.s = s;
        wm = (WindowManager) s.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = s.getResources().getDisplayMetrics();
        density = dm.density;
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
        markerSize = dp(44);
    }

    // ---------------- 表示/非表示 ----------------

    void show() {
        if (shown) return;
        shown = true;
        buildPanel();
        for (int[] p : s.cfg.points) addMarker(p[0], p[1]);
        if (markers.isEmpty()) addMarker(screenW / 2 - markerSize / 2, screenH / 2 - markerSize / 2);
        if (s.cfg.minimized) minimize(); else wm.addView(panel, panelLp);
        refreshInterval();
        refreshStats();
        s.widgetRefresh();
    }

    void hide() {
        if (!shown) return;
        s.stopTapping();
        syncPointsToConfig();
        s.cfg.save();
        for (FrameLayout m : markers) safeRemove(m);
        markers.clear();
        markerLps.clear();
        if (panel != null && panel.getParent() != null) safeRemove(panel);
        if (bubble != null && bubble.getParent() != null) safeRemove(bubble);
        if (sheet != null && sheet.getParent() != null) safeRemove(sheet);
        panel = null; bubble = null; sheet = null;
        shown = false;
        s.widgetRefresh();
    }

    private void safeRemove(View v) {
        try { wm.removeView(v); } catch (Throwable t) { }
    }

    void toast(String msg) {
        try { Toast.makeText(s, msg, Toast.LENGTH_SHORT).show(); } catch (Throwable t) { }
    }

    // ---------------- パネル ----------------

    private TextView lblLive;
    private final Runnable liveTick = new Runnable() {
        public void run() {
            if (!s.running || lblLive == null) return;
            lblLive.setText(s.sessionTaps + " · " + String.format("%.1f", s.tapsPerSecond()) + "/s");
            s.handler.postDelayed(this, 250);
        }
    };

    private void buildPanel() {
        switch (s.cfg.overlayStyle) {
            case 1: buildCompactPanel(); return;
            case 2: buildMinimalPanel(); return;
            default: buildFullPanel(); return;
        }
    }

    private void buildFullPanel() {
        panel = new LinearLayout(s);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(roundRect(C_BG, 14));
        panel.setPadding(dp(6), dp(6), dp(6), dp(6));
        panel.setElevation(dp(8));

        // 1段目：ハンドル / 再生 / 間隔
        LinearLayout row1 = new LinearLayout(s);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);
        TextView handle = label("⋮⋮", C_SUB, 17);
        handle.setGravity(Gravity.CENTER);
        handle.setPadding(dp(8), dp(8), dp(8), dp(8));
        handle.setBackground(roundRect(0x1AFFFFFF, 8));
        row1.addView(handle);

        btnToggle = btn("▶", C_RUN, new View.OnClickListener() { public void onClick(View v) { haptic(v); s.toggle(); } });
        btnToggle.setMinWidth(dp(52));
        row1.addView(btnToggle);

        row1.addView(btn("−", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); changeInterval(-step()); } }));
        lblInterval = label("100", C_TEXT, 14);
        lblInterval.setTypeface(Typeface.DEFAULT_BOLD);
        lblInterval.setMinWidth(dp(44));
        lblInterval.setGravity(Gravity.CENTER);
        lblInterval.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { haptic(v); cyclePreset(); } });
        row1.addView(lblInterval);
        row1.addView(btn("+", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); changeInterval(step()); } }));
        panel.addView(row1);

        // 2段目：マーカー / 設定 / 閉じる / ライブ表示
        LinearLayout row2 = new LinearLayout(s);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER_VERTICAL);
        row2.setPadding(0, dp(5), 0, 0);
        row2.addView(btn("●+", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); addMarkerNearLast(); } }));
        row2.addView(btn("●−", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); removeLastMarker(); } }));
        row2.addView(btn("⚙", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); toggleSheet(); } }));
        lblLive = label("", C_SUB, 11);
        lblLive.setGravity(Gravity.CENTER);
        row2.addView(lblLive, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row2.addView(btn("×", C_STOP, new View.OnClickListener() { public void onClick(View v) { haptic(v); hide(); } }));
        panel.addView(row2);

        panelLp = newLp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        panelLp.x = s.cfg.panelX;
        panelLp.y = s.cfg.panelY;
        Runnable saved = new Runnable() {
            public void run() { s.cfg.panelX = panelLp.x; s.cfg.panelY = panelLp.y; }
        };
        handle.setOnTouchListener(new Dragger(panel, panelLp, saved) {
            @Override void onTap() { haptic(panel); minimize(); }
        });
        panel.setOnTouchListener(new Dragger(panel, panelLp, saved));
        row1.setOnTouchListener(new Dragger(panel, panelLp, saved));
        row2.setOnTouchListener(new Dragger(panel, panelLp, saved));
    }

    /** コンパクト表示：1段のみの省スペース版 */
    private void buildCompactPanel() {
        panel = new LinearLayout(s);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setBackground(roundRect(C_BG, 14));
        panel.setPadding(dp(5), dp(5), dp(5), dp(5));
        panel.setElevation(dp(8));

        TextView handle = label("⋮⋮", C_SUB, 15);
        handle.setGravity(Gravity.CENTER);
        handle.setPadding(dp(6), dp(6), dp(6), dp(6));
        handle.setBackground(roundRect(0x1AFFFFFF, 8));
        panel.addView(handle);

        btnToggle = btn("▶", C_RUN, new View.OnClickListener() { public void onClick(View v) { haptic(v); s.toggle(); } });
        panel.addView(btnToggle);

        lblInterval = label("100", C_TEXT, 13);
        lblInterval.setTypeface(Typeface.DEFAULT_BOLD);
        lblInterval.setMinWidth(dp(38));
        lblInterval.setGravity(Gravity.CENTER);
        lblInterval.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { haptic(v); cyclePreset(); } });
        panel.addView(lblInterval);

        panel.addView(btn("●+", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); addMarkerNearLast(); } }));
        panel.addView(btn("●−", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); removeLastMarker(); } }));
        panel.addView(btn("⚙", C_BTN, new View.OnClickListener() { public void onClick(View v) { haptic(v); toggleSheet(); } }));
        panel.addView(btn("×", C_STOP, new View.OnClickListener() { public void onClick(View v) { haptic(v); hide(); } }));

        panelLp = newLp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        panelLp.x = s.cfg.panelX;
        panelLp.y = s.cfg.panelY;
        Runnable saved = new Runnable() {
            public void run() { s.cfg.panelX = panelLp.x; s.cfg.panelY = panelLp.y; }
        };
        handle.setOnTouchListener(new Dragger(panel, panelLp, saved) {
            @Override void onTap() { haptic(panel); minimize(); }
        });
        panel.setOnTouchListener(new Dragger(panel, panelLp, saved));
    }

    /** ミニマル表示：円形の開始/停止ボタン1つのみ（タップ=開始/停止、長押し=設定） */
    private void buildMinimalPanel() {
        panel = new LinearLayout(s);
        panel.setOrientation(LinearLayout.VERTICAL);

        TextView t = label(s.running ? "■" : "▶", Color.WHITE, 18);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackground(circle(s.running ? C_STOP : C_RUN, Color.WHITE, dp(2)));
        t.setElevation(dp(8));
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
        btnToggle = t;
        lblInterval = null;
        panel.addView(t);

        panelLp = newLp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        panelLp.x = s.cfg.panelX;
        panelLp.y = s.cfg.panelY;
        Runnable saved = new Runnable() {
            public void run() { s.cfg.panelX = panelLp.x; s.cfg.panelY = panelLp.y; }
        };
        Dragger d = new Dragger(panel, panelLp, saved) {
            @Override void onTap() { haptic(panel); s.toggle(); }
            @Override void onLongPress() { haptic(panel); toggleSheet(); }
        };
        panel.setOnTouchListener(d);
        t.setOnTouchListener(d);
    }

    private void haptic(View v) {
        try { v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP); } catch (Throwable t) { }
    }

    private void minimize() {
        s.cfg.minimized = true;
        if (panel.getParent() != null) safeRemove(panel);
        if (sheet != null && sheet.getParent() != null) safeRemove(sheet);
        if (bubble == null) {
            TextView b = label("⚡", Color.WHITE, 17);
            b.setGravity(Gravity.CENTER);
            b.setBackground(circle(C_ACCENT, Color.WHITE, dp(2)));
            b.setElevation(dp(6));
            bubble = b;
            bubbleLp = newLp(dp(46), dp(46));
            bubbleLp.x = panelLp.x;
            bubbleLp.y = panelLp.y;
            bubble.setOnTouchListener(new Dragger(bubble, bubbleLp, null) {
                @Override void onTap() { restore(); }
            });
        }
        if (bubble.getParent() == null) wm.addView(bubble, bubbleLp);
    }

    private void restore() {
        s.cfg.minimized = false;
        if (bubble != null && bubble.getParent() != null) safeRemove(bubble);
        panelLp.x = bubbleLp.x;
        panelLp.y = bubbleLp.y;
        if (panel.getParent() == null) wm.addView(panel, panelLp);
    }

    void onRunningChanged() {
        if (btnToggle == null) return;
        btnToggle.setText(s.running ? "■" : "▶");
        btnToggle.setBackground(s.cfg.overlayStyle == 2
                ? circle(s.running ? C_STOP : C_RUN, Color.WHITE, dp(2))
                : roundRect(s.running ? C_STOP : C_RUN, 8));
        if (panel != null) panel.setAlpha(s.running ? 0.9f : 1f);
        if (bubble != null) bubble.setBackground(circle(s.running ? C_STOP : C_ACCENT, Color.WHITE, dp(2)));
        if (lblLive != null) {
            s.handler.removeCallbacks(liveTick);
            if (s.running) s.handler.post(liveTick);
            else lblLive.setText(s.sessionTaps > 0 ? s.sessionTaps + " · " + String.format("%.1f", s.tapsPerSecond()) + "/s" : "");
        }
    }

    void onTapped() {
        if (s.sessionTaps % 5 == 0) refreshStats();
    }

    private void refreshStats() { }

    private int step() { int v = s.cfg.intervalMs; return v < 50 ? 5 : (v < 100 ? 10 : (v < 500 ? 25 : 100)); }

    private void changeInterval(int d) {
        s.cfg.intervalMs = Config.clampInterval(s.cfg.intervalMs + d);
        refreshInterval();
        s.cfg.save();
    }

    private static final int[] PRESETS = {10, 20, 50, 100, 250, 500, 1000};

    private void cyclePreset() {
        int cur = s.cfg.intervalMs, next = PRESETS[0];
        for (int p : PRESETS) if (p > cur) { next = p; break; }
        s.cfg.intervalMs = next;
        refreshInterval();
        s.cfg.save();
    }

    private void refreshInterval() {
        if (lblInterval != null) lblInterval.setText(String.valueOf(s.cfg.intervalMs));
    }

    // ---------------- マーカー ----------------

    int markerCount() { return markers.size(); }

    int[] markerCenter(int i) {
        WindowManager.LayoutParams lp = markerLps.get(i);
        return new int[]{lp.x + markerSize / 2, lp.y + markerSize / 2};
    }

    private void addMarkerNearLast() {
        if (markers.size() >= 10) { toast(L.s("max_points")); return; }
        int x = screenW / 2 - markerSize / 2, y = screenH / 2 - markerSize / 2;
        if (!markerLps.isEmpty()) {
            WindowManager.LayoutParams last = markerLps.get(markerLps.size() - 1);
            x = Math.min(screenW - markerSize, last.x + markerSize + dp(8));
            y = last.y;
        }
        addMarker(x, y);
        syncPointsToConfig();
        s.cfg.save();
    }

    private void addMarker(int x, int y) {
        int idx = markers.size();
        int color = MARKER_COLORS[idx % MARKER_COLORS.length];
        FrameLayout m = new FrameLayout(s);
        m.setBackground(circle((color & 0x00FFFFFF) | 0x99000000, Color.WHITE, dp(2)));
        TextView num = label(String.valueOf(idx + 1), Color.WHITE, 14);
        num.setTypeface(Typeface.DEFAULT_BOLD);
        num.setGravity(Gravity.CENTER);
        m.addView(num, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WindowManager.LayoutParams lp = newLp(markerSize, markerSize);
        lp.x = Math.max(0, Math.min(screenW - markerSize, x));
        lp.y = Math.max(0, Math.min(screenH - markerSize, y));
        final FrameLayout mv = m;
        final TextView numv = num;
        final WindowManager.LayoutParams lpv = lp;
        m.setOnTouchListener(new Dragger(m, lp, null) {
            @Override void onDrag() { numv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9); numv.setText((lpv.x + markerSize / 2) + "\n" + (lpv.y + markerSize / 2)); }
            @Override void onRelease() { numv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); numv.setText(String.valueOf(markers.indexOf(mv) + 1)); }
            @Override void onLongPress() { haptic(mv); removeMarker(mv); }
        });
        wm.addView(m, lp);
        markers.add(m);
        markerLps.add(lp);
    }

    private void removeMarker(FrameLayout m) {
        if (markers.size() <= 1) { toast(L.s("min_points")); return; }
        int i = markers.indexOf(m);
        if (i < 0) return;
        markers.remove(i);
        markerLps.remove(i);
        safeRemove(m);
        renumber();
        syncPointsToConfig();
        s.cfg.save();
    }

    private void renumber() {
        for (int i = 0; i < markers.size(); i++) {
            FrameLayout m = markers.get(i);
            int color = MARKER_COLORS[i % MARKER_COLORS.length];
            m.setBackground(circle((color & 0x00FFFFFF) | 0x99000000, Color.WHITE, dp(2)));
            View c = m.getChildAt(0);
            if (c instanceof TextView) ((TextView) c).setText(String.valueOf(i + 1));
        }
    }

    private void removeLastMarker() {
        if (markers.size() <= 1) { toast(L.s("min_points")); return; }
        FrameLayout m = markers.remove(markers.size() - 1);
        markerLps.remove(markerLps.size() - 1);
        safeRemove(m);
        syncPointsToConfig();
        s.cfg.save();
    }

    void setMarkersPassthrough(boolean pass) {
        for (int i = 0; i < markers.size(); i++) {
            WindowManager.LayoutParams lp = markerLps.get(i);
            if (pass) lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            else lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            markers.get(i).setAlpha(pass ? 0.45f : 1f);
            try { wm.updateViewLayout(markers.get(i), lp); } catch (Throwable t) { }
        }
    }

    private void syncPointsToConfig() {
        s.cfg.points.clear();
        for (WindowManager.LayoutParams lp : markerLps) s.cfg.points.add(new int[]{lp.x, lp.y});
    }

    // ---------------- 設定シート ----------------

    void closeSheet() { if (sheet != null && sheet.getParent() != null) safeRemove(sheet); }

    private void toggleSheet() {
        if (sheet != null && sheet.getParent() != null) { safeRemove(sheet); return; }
        sheet = buildSheet();
        WindowManager.LayoutParams lp = newLp(Math.min(dp(340), screenW - dp(24)), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        lp.x = 0; lp.y = 0;
        lp.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        sheet.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getActionMasked() == MotionEvent.ACTION_OUTSIDE) { closeSheet(); return true; }
                return false;
            }
        });
        wm.addView(sheet, lp);
    }

    private View buildSheet() {
        final Config c = s.cfg;
        LinearLayout col = new LinearLayout(s);
        col.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        col.setPadding(pad, pad, pad, pad);

        LinearLayout head = new LinearLayout(s);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label(L.s("settings"), C_TEXT, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        head.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(btn(L.s("close"), C_BTN, new View.OnClickListener() { public void onClick(View v) { toggleSheet(); } }));
        col.addView(head);

        // プロファイル
        col.addView(section(L.s("profile")));
        LinearLayout prow = new LinearLayout(s);
        prow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 3; i++) {
            final int p = i;
            TextView b = btn(L.f("slot", i + 1), c.profile() == i ? C_ACCENT : C_BTN, new View.OnClickListener() {
                public void onClick(View v) {
                    syncPointsToConfig();
                    c.switchProfile(p);
                    reloadMarkers();
                    refreshInterval();
                    toggleSheet(); toggleSheet();
                    toast(L.f("loaded", p + 1));
                }
            });
            prow.addView(b);
        }
        col.addView(prow);

        // 表示スタイル
        col.addView(section(L.s("sec_overlay")));
        LinearLayout srow = new LinearLayout(s);
        srow.setOrientation(LinearLayout.HORIZONTAL);
        final String[] styleKeys = {"style_full", "style_compact", "style_minimal"};
        for (int i = 0; i < styleKeys.length; i++) {
            final int style = i;
            TextView b = btn(L.s(styleKeys[i]), c.overlayStyle == i ? C_ACCENT : C_BTN, new View.OnClickListener() {
                public void onClick(View v) { haptic(v); switchOverlayStyle(style); }
            });
            srow.addView(b);
        }
        col.addView(srow);
        col.addView(btn(L.s("hide_overlay"), C_BTN, new View.OnClickListener() {
            public void onClick(View v) { haptic(v); closeSheet(); hide(); }
        }));

        col.addView(section(L.s("sec_tap")));
        col.addView(seek(L.s("interval_fine"), c.intervalMs, 10, 300, L.s("u_ms"), new IntSetter() { public void set(int v) { c.intervalMs = v; refreshInterval(); } }));
        col.addView(seek(L.s("hold"), c.holdMs, 1, 300, L.s("u_ms"), new IntSetter() { public void set(int v) { c.holdMs = v; } }));
        col.addView(label(L.s("speed_note"), C_SUB, 11));
        col.addView(sw(L.s("round_robin"), c.roundRobin, new BoolSetter() { public void set(boolean v) { c.roundRobin = v; } }));

        col.addView(section(L.s("sec_autostop")));
        col.addView(sw(L.s("stop_count"), c.limitCount, new BoolSetter() { public void set(boolean v) { c.limitCount = v; } }));
        col.addView(seek(L.s("count"), c.maxCount, 10, 5000, L.s("u_times"), new IntSetter() { public void set(int v) { c.maxCount = v; } }));
        col.addView(sw(L.s("stop_time"), c.limitTime, new BoolSetter() { public void set(boolean v) { c.limitTime = v; } }));
        col.addView(seek(L.s("seconds"), c.maxSeconds, 5, 3600, L.s("u_sec"), new IntSetter() { public void set(int v) { c.maxSeconds = v; } }));

        col.addView(section(L.s("sec_smart")));
        col.addView(sw(L.s("auto_pause"), c.autoPause, new BoolSetter() { public void set(boolean v) { c.autoPause = v; } }));
        col.addView(sw(L.s("keep_awake"), c.keepAwake, new BoolSetter() { public void set(boolean v) { c.keepAwake = v; } }));

        col.addView(section(L.s("sec_feedback")));
        col.addView(sw(L.s("vibrate"), c.vibrate, new BoolSetter() { public void set(boolean v) { c.vibrate = v; } }));
        col.addView(sw(L.s("notify"), c.notify, new BoolSetter() { public void set(boolean v) { c.notify = v; } }));
        col.addView(sw(L.s("vol_key"), c.volumeKey, new BoolSetter() { public void set(boolean v) { c.volumeKey = v; } }));

        col.addView(section(L.s("sec_backup")));
        LinearLayout brow = new LinearLayout(s);
        brow.setOrientation(LinearLayout.HORIZONTAL);
        brow.addView(btn(L.s("export"), C_BTN, new View.OnClickListener() {
            public void onClick(View v) {
                syncPointsToConfig();
                try {
                    ClipboardManager cm = (ClipboardManager) s.getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("OpenTapper", c.exportText()));
                    toast(L.s("exported"));
                } catch (Throwable t) { }
            }
        }));
        brow.addView(btn(L.s("import"), C_BTN, new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ClipboardManager cm = (ClipboardManager) s.getSystemService(Context.CLIPBOARD_SERVICE);
                    CharSequence cs = (cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0)
                            ? cm.getPrimaryClip().getItemAt(0).getText() : null;
                    if (cs != null && c.importText(cs.toString())) {
                        c.save();
                        reloadMarkers();
                        refreshInterval();
                        toast(L.s("imported"));
                    } else {
                        toast(L.s("import_failed"));
                    }
                } catch (Throwable t) { toast(L.s("import_failed")); }
            }
        }));
        col.addView(brow);

        col.addView(section(L.s("sec_stats")));
        final TextView stats = label(L.f("stats_fmt", s.totalTaps, s.sessionTaps, s.tapsPerSecond()), C_SUB, 13);
        col.addView(stats);
        col.addView(btn(L.s("reset_total"), C_BTN, new View.OnClickListener() {
            public void onClick(View v) { s.totalTaps = 0; s.sessionTaps = 0; stats.setText(L.f("stats_fmt", 0, 0, 0f)); refreshStats(); }
        }));

        ScrollView sv = new ScrollView(s);
        sv.setBackground(roundRect(C_CARD, 18));
        sv.setElevation(dp(10));
        sv.addView(col);
        sv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.min(dp(520), (int) (screenH * 0.8f))));
        FrameLayout wrap = new FrameLayout(s);
        wrap.addView(sv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.min(dp(520), (int) (screenH * 0.8f))));
        return wrap;
    }

    private void switchOverlayStyle(int style) {
        if (s.cfg.overlayStyle == style) { closeSheet(); return; }
        syncPointsToConfig();
        s.cfg.overlayStyle = style;
        s.cfg.minimized = false;
        s.cfg.save();
        closeSheet();
        if (bubble != null && bubble.getParent() != null) safeRemove(bubble);
        bubble = null;
        if (panel != null && panel.getParent() != null) safeRemove(panel);
        buildPanel();
        wm.addView(panel, panelLp);
        refreshInterval();
        onRunningChanged();
    }

    private void reloadMarkers() {
        for (FrameLayout m : markers) safeRemove(m);
        markers.clear();
        markerLps.clear();
        for (int[] p : s.cfg.points) addMarker(p[0], p[1]);
        if (markers.isEmpty()) addMarker(screenW / 2 - markerSize / 2, screenH / 2 - markerSize / 2);
    }

    interface IntSetter { void set(int v); }
    interface BoolSetter { void set(boolean v); }

    private View section(String t) {
        TextView tv = label(t, C_ACCENT, 12);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, dp(14), 0, dp(4));
        return tv;
    }

    private View sw(String t, boolean val, final BoolSetter setter) {
        Switch w = new Switch(s);
        w.setText(t);
        w.setTextColor(C_TEXT);
        w.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        w.setChecked(val);
        w.setPadding(0, dp(6), 0, dp(6));
        w.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean v) { setter.set(v); s.cfg.save(); }
        });
        return w;
    }

    private View seek(final String t, int val, final int min, final int max, final String unit, final IntSetter setter) {
        LinearLayout row = new LinearLayout(s);
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout head = new LinearLayout(s);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        final TextView lbl = label(t + "  " + unit, C_SUB, 12);
        head.addView(lbl, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final EditText edit = new EditText(s);
        edit.setInputType(InputType.TYPE_CLASS_NUMBER);
        edit.setText(String.valueOf(val));
        edit.setTextColor(C_TEXT);
        edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        edit.setGravity(Gravity.CENTER);
        edit.setSingleLine(true);
        edit.setBackground(roundRect(0x22FFFFFF, 6));
        edit.setPadding(dp(8), dp(4), dp(8), dp(4));
        edit.setMinWidth(dp(56));
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        head.addView(edit);
        row.addView(head);

        final SeekBar sb = new SeekBar(s);
        sb.setMax(max - min);
        sb.setProgress(Math.max(0, Math.min(max - min, val - min)));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                int v = p + min;
                setter.set(v);
                if (u) {
                    edit.setText(String.valueOf(v));
                    edit.setSelection(edit.getText().length());
                }
            }
            public void onStartTrackingTouch(SeekBar b) { }
            public void onStopTrackingTouch(SeekBar b) { s.cfg.save(); }
        });
        row.addView(sb);

        OnEditorActionListener applyEdit = new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent ke) {
                try {
                    int raw = Integer.parseInt(edit.getText().toString().trim());
                    int clamped = Math.max(min, Math.min(max, raw));
                    setter.set(clamped);
                    sb.setProgress(clamped - min);
                    edit.setText(String.valueOf(clamped));
                    edit.setSelection(edit.getText().length());
                    s.cfg.save();
                } catch (Throwable t) {
                    edit.setText(String.valueOf(Math.max(min, Math.min(max, sb.getProgress() + min))));
                }
                try {
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                            s.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                } catch (Throwable t) { }
                edit.clearFocus();
                return true;
            }
        };
        edit.setOnEditorActionListener(applyEdit);
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean has) {
                if (!has) applyEdit.onEditorAction(edit, EditorInfo.IME_ACTION_DONE, null);
            }
        });
        return row;
    }

    // ---------------- 部品 ----------------

    private TextView label(String t, int color, int sp) {
        TextView tv = new TextView(s);
        tv.setText(t);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return tv;
    }

    private TextView btn(String t, int bg, View.OnClickListener l) {
        TextView tv = label(t, Color.WHITE, 13);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(7), dp(10), dp(7));
        tv.setMinWidth(dp(34));
        tv.setBackground(roundRect(bg, 8));
        tv.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(2), 0, dp(2), 0);
        tv.setLayoutParams(lp);
        return tv;
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private GradientDrawable circle(int fill, int stroke, int strokePx) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        g.setStroke(strokePx, stroke);
        return g;
    }

    private WindowManager.LayoutParams newLp(int w, int h) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(w, h,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        return lp;
    }

    int dp(int v) { return (int) (v * density + 0.5f); }

    /** ドラッグ処理（短いタップは onTap） */
    private class Dragger implements View.OnTouchListener {
        private final View view;
        private final WindowManager.LayoutParams lp;
        private final Runnable onMoved;
        private int startX, startY;
        private float touchX, touchY;
        private boolean moved;

        Dragger(View view, WindowManager.LayoutParams lp, Runnable onMoved) {
            this.view = view; this.lp = lp; this.onMoved = onMoved;
        }

        void onTap() { }
        void onDrag() { }
        void onRelease() { }
        void onLongPress() { }
        private boolean longFired;
        private final Runnable longCheck = new Runnable() {
            public void run() { if (!moved) { longFired = true; onLongPress(); } }
        };

        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = lp.x; startY = lp.y;
                    touchX = e.getRawX(); touchY = e.getRawY();
                    moved = false; longFired = false;
                    s.handler.postDelayed(longCheck, 600);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (longFired) return true;
                    int dx = (int) (e.getRawX() - touchX), dy = (int) (e.getRawY() - touchY);
                    if (Math.abs(dx) > dp(4) || Math.abs(dy) > dp(4)) { moved = true; s.handler.removeCallbacks(longCheck); }
                    if (!moved) return true;
                    lp.x = Math.max(0, Math.min(screenW - view.getWidth(), startX + dx));
                    lp.y = Math.max(0, Math.min(screenH - view.getHeight(), startY + dy));
                    try { wm.updateViewLayout(view, lp); } catch (Throwable t) { }
                    onDrag();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    s.handler.removeCallbacks(longCheck);
                    if (longFired) return true;
                    if (moved) {
                        onRelease();
                        if (onMoved != null) onMoved.run();
                        syncPointsToConfig();
                        s.cfg.save();
                    } else {
                        onTap();
                    }
                    return true;
            }
            return false;
        }
    }
}
