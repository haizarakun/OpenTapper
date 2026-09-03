package com.opentapper.tap;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int BG = 0xFF07090F;
    private static final int BG2 = 0xFF10131D;
    private static final int CARD = 0xFF151A26;
    private static final int LINE = 0xFF232A3B;
    private static final int ACCENT = 0xFF4F8CFF;
    private static final int ACCENT2 = 0xFF9B5CFF;
    private static final int CYAN = 0xFF3DDCFF;
    private static final int OK = 0xFF34C759;
    private static final int WARN = 0xFFFF9F0A;
    private static final int TEXT = 0xFFF5F7FF;
    private static final int SUB = 0xFF8A93A8;

    private TextView pill, bigBtn, bigLabel, hint, statTotal, statInterval, statPoints;
    private View ring1, ring2, guide;
    private ValueAnimator pulse;
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.init(this);
        density = getResources().getDisplayMetrics().density;
        try {
            if (getActionBar() != null) getActionBar().hide();
            Window w = getWindow();
            w.setStatusBarColor(BG);
            w.setNavigationBarColor(BG);
        } catch (Throwable t) { }

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BG, BG2, BG}));

        // 背景の淡いグロー
        View glow = new View(this);
        GradientDrawable gl = new GradientDrawable();
        gl.setShape(GradientDrawable.OVAL);
        gl.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        gl.setGradientRadius(dp(260));
        gl.setColors(new int[]{0x554F8CFF, 0x00000000});
        glow.setBackground(gl);
        FrameLayout.LayoutParams glp = new FrameLayout.LayoutParams(dp(520), dp(520), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        glp.topMargin = dp(20);
        root.addView(glow, glp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(22), dp(24), dp(22), dp(28));

        // ---- ヘッダー ----
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("OPENTAPPER", 13, SUB);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setLetterSpacing(0.25f);
        head.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        pill = text("", 11, TEXT);
        pill.setTypeface(Typeface.DEFAULT_BOLD);
        pill.setPadding(dp(10), dp(5), dp(10), dp(5));
        head.addView(pill);
        TextView gear = text("⚙", 20, SUB);
        gear.setPadding(dp(12), 0, 0, 0);
        gear.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { showSettings(); } });
        head.addView(gear);
        col.addView(head, match());

        // ---- 巨大ボタン ----
        FrameLayout stage = new FrameLayout(this);
        int size = dp(200);
        ring2 = ring(0x334F8CFF, dp(2));
        ring1 = ring(0x664F8CFF, dp(2));
        stage.addView(ring2, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        stage.addView(ring1, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));

        bigBtn = text("⚡", 64, Color.WHITE);
        bigBtn.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ACCENT, ACCENT2});
        bg.setShape(GradientDrawable.OVAL);
        bg.setStroke(dp(3), 0x66FFFFFF);
        bigBtn.setBackground(bg);
        bigBtn.setElevation(dp(18));
        bigBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TapService.isConnected()) TapService.requestShowOverlay(MainActivity.this);
                else openAccessibility();
            }
        });
        stage.addView(bigBtn, new FrameLayout.LayoutParams(dp(160), dp(160), Gravity.CENTER));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(260), dp(260));
        slp.topMargin = dp(28);
        col.addView(stage, slp);

        bigLabel = text("", 20, TEXT);
        bigLabel.setTypeface(Typeface.DEFAULT_BOLD);
        bigLabel.setGravity(Gravity.CENTER);
        col.addView(bigLabel);
        hint = text("", 12, SUB);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(16), dp(6), dp(16), 0);
        col.addView(hint);

        // ---- 統計タイル ----
        LinearLayout tiles = new LinearLayout(this);
        tiles.setOrientation(LinearLayout.HORIZONTAL);
        statTotal = tile(tiles, L.s("stat_total"), "0");
        statInterval = tile(tiles, L.s("stat_interval"), "100ms");
        statPoints = tile(tiles, L.s("stat_points"), "1");
        LinearLayout.LayoutParams tlp = match();
        tlp.topMargin = dp(26);
        col.addView(tiles, tlp);

        // ---- サブアクション ----
        LinearLayout acts = new LinearLayout(this);
        acts.setOrientation(LinearLayout.HORIZONTAL);
        acts.addView(smallAction(L.s("act_a11y"), L.s("act_a11y_sub"), new View.OnClickListener() {
            public void onClick(View v) { openAccessibility(); }
        }));
        acts.addView(smallAction(L.s("act_settings"), L.s("act_settings_sub"), new View.OnClickListener() {
            public void onClick(View v) { showSettings(); }
        }));
        acts.addView(smallAction(L.s("act_help"), L.s("act_help_sub"), new View.OnClickListener() {
            public void onClick(View v) { guide.setVisibility(guide.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE); }
        }));
        LinearLayout.LayoutParams alp = match();
        alp.topMargin = dp(10);
        col.addView(acts, alp);

        // ---- 使い方（折りたたみ） ----
        LinearLayout g = card();
        g.addView(step("▶ / ■", L.s("step_run")));
        g.addView(step("− 100 +", L.s("step_int")));
        g.addView(step("●+ / ●−", L.s("step_pt")));
        g.addView(step("⚙", L.s("step_gear")));
        g.addView(step("⋮⋮", L.s("step_handle")));
        g.addView(step(L.s("widget"), L.s("step_widget"), true));
        guide = g;
        guide.setVisibility(View.GONE);
        LinearLayout.LayoutParams gp = match();
        gp.topMargin = dp(10);
        col.addView(guide, gp);

        TextView foot = text(L.s("foot"), 11, SUB);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(22), 0, 0);
        col.addView(foot, match());

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.addView(col, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(sv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    private void showSettings() {
        int cur = 0;
        for (int i = 0; i < L.CODES.length; i++) if (L.CODES[i].equals(L.current())) cur = i;
        new android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setTitle(L.s("language"))
            .setSingleChoiceItems(L.NAMES, cur, new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface d, int which) {
                    L.set(MainActivity.this, L.CODES[which]);
                    d.dismiss();
                    TapWidget.refresh(MainActivity.this);
                    recreate();
                }
            })
            .setNegativeButton(L.s("close"), null)
            .show();
    }

    private void openAccessibility() {
        try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); } catch (Throwable t) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean on = TapService.isConnected();
        pill.setText(on ? L.s("status_on") : L.s("status_off"));
        pill.setTextColor(on ? OK : WARN);
        pill.setBackground(round(on ? 0x2234C759 : 0x22FF9F0A, 20));
        bigLabel.setText(on ? L.s("big_on") : L.s("big_off"));
        hint.setText(on ? L.s("hint_on") : L.s("hint_off"));
        bigBtn.setText(on ? "⚡" : "⚙");

        android.content.SharedPreferences p = getSharedPreferences("opentapper", MODE_PRIVATE);
        int prof = p.getInt("profile", 0);
        statTotal.setText(fmt(p.getLong("totalTaps", 0)));
        statInterval.setText(p.getInt("p" + prof + "_interval", 100) + "ms");
        statPoints.setText(String.valueOf(Math.max(1, p.getInt("p" + prof + "_n", 1))));

        startPulse();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pulse != null) { pulse.cancel(); pulse = null; }
    }

    private void startPulse() {
        if (pulse != null) return;
        pulse = ValueAnimator.ofFloat(0f, 1f);
        pulse.setDuration(1800);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator a) {
                float f = (Float) a.getAnimatedValue();
                float s1 = 1f + 0.25f * f;
                ring1.setScaleX(s1); ring1.setScaleY(s1); ring1.setAlpha(1f - f);
                float f2 = (f + 0.5f) % 1f;
                float s2 = 1f + 0.25f * f2;
                ring2.setScaleX(s2); ring2.setScaleY(s2); ring2.setAlpha(1f - f2);
            }
        });
        pulse.start();
    }

    private static String fmt(long v) {
        String s = String.valueOf(v);
        StringBuilder b = new StringBuilder();
        int c = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            b.append(s.charAt(i));
            if (++c % 3 == 0 && i > 0) b.append(',');
        }
        return b.reverse().toString();
    }

    // ---------------- 部品 ----------------

    private View ring(int color, int stroke) {
        View v = new View(this);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(Color.TRANSPARENT);
        g.setStroke(stroke, color);
        v.setBackground(g);
        return v;
    }

    private TextView tile(LinearLayout parent, String label, String value) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(14), dp(8), dp(14));
        GradientDrawable g = round(CARD, 16);
        g.setStroke(dp(1), LINE);
        t.setBackground(g);
        TextView v = text(value, 20, TEXT);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        TextView l = text(label, 11, SUB);
        l.setPadding(0, dp(4), 0, 0);
        t.addView(v);
        t.addView(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        parent.addView(t, lp);
        return v;
    }

    private View smallAction(String title, String sub, View.OnClickListener l) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable g = round(CARD, 16);
        g.setStroke(dp(1), LINE);
        t.setBackground(g);
        t.setClickable(true);
        t.setOnClickListener(l);
        TextView a = text(title, 14, TEXT);
        a.setTypeface(Typeface.DEFAULT_BOLD);
        TextView b = text(sub + "  ›", 11, CYAN);
        b.setPadding(0, dp(3), 0, 0);
        t.addView(a);
        t.addView(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        t.setLayoutParams(lp);
        return t;
    }

    private View step(String key, String desc) { return step(key, desc, false); }

    private View step(String key, String desc, boolean last) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, last ? dp(2) : dp(7));
        TextView k = text(key, 12, TEXT);
        k.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        k.setGravity(Gravity.CENTER);
        k.setPadding(dp(8), dp(5), dp(8), dp(5));
        k.setBackground(round(LINE, 8));
        k.setMinWidth(dp(70));
        row.addView(k);
        TextView d = text(desc, 12, SUB);
        d.setPadding(dp(12), 0, 0, 0);
        d.setLineSpacing(0, 1.2f);
        row.addView(d, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(10), dp(16), dp(10));
        GradientDrawable g = round(CARD, 16);
        g.setStroke(dp(1), LINE);
        c.setBackground(g);
        return c;
    }

    private LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private TextView text(String t, int sp, int color) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        return tv;
    }

    private GradientDrawable round(int color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    private int dp(int v) { return (int) (v * density + 0.5f); }
}
