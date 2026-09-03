package com.opentapper.tap;

import android.content.SharedPreferences;
import java.util.ArrayList;

/** 設定の永続化（SharedPreferences、プロファイル別キー） */
public class Config {
    public static final int MIN_INTERVAL = 10;
    public static final int MIN_GAP = 5;      // ストローク間の最小空白(ms)
    public static final int MAX_INTERVAL = 3000;

    // 基本
    public int intervalMs = 100;
    public int holdMs = 5;          // タップ押下時間
    public boolean roundRobin = false;
    // 制限
    public boolean limitCount = false;
    public int maxCount = 100;
    public boolean limitTime = false;
    public int maxSeconds = 60;
    // フィードバック
    public boolean vibrate = true;
    public boolean notify = true;
    public boolean volumeKey = true;
    public boolean autoPause = false;   // 対象アプリを離れたら自動停止
    public boolean keepAwake = true;    // 実行中は画面消灯を防ぐ
    // 位置
    public ArrayList<int[]> points = new ArrayList<int[]>();
    // パネル位置
    public int panelX = 0, panelY = 30;
    public boolean minimized = false;

    private final SharedPreferences prefs;
    private int profile;

    public Config(SharedPreferences prefs) {
        this.prefs = prefs;
        this.profile = prefs.getInt("profile", 0);
    }

    public int profile() { return profile; }

    private String k(String key) { return "p" + profile + "_" + key; }

    public void load() {
        intervalMs = clampInterval(prefs.getInt(k("interval"), 100));
        holdMs = prefs.getInt(k("hold"), 5);
        roundRobin = prefs.getBoolean(k("rr"), false);
        limitCount = prefs.getBoolean(k("limitCount"), false);
        maxCount = prefs.getInt(k("maxCount"), 100);
        limitTime = prefs.getBoolean(k("limitTime"), false);
        maxSeconds = prefs.getInt(k("maxSeconds"), 60);
        vibrate = prefs.getBoolean("vibrate", true);
        notify = prefs.getBoolean("notify", true);
        volumeKey = prefs.getBoolean("volumeKey", true);
        autoPause = prefs.getBoolean(k("autoPause"), false);
        keepAwake = prefs.getBoolean("keepAwake", true);
        panelX = prefs.getInt("panelX", 0);
        panelY = prefs.getInt("panelY", 30);
        points.clear();
        int n = prefs.getInt(k("n"), 0);
        for (int i = 0; i < n; i++) {
            points.add(new int[]{prefs.getInt(k("x" + i), 0), prefs.getInt(k("y" + i), 0)});
        }
    }

    public void save() {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("profile", profile);
        e.putInt(k("interval"), intervalMs);
        e.putInt(k("hold"), holdMs);
        e.putBoolean(k("rr"), roundRobin);
        e.putBoolean(k("limitCount"), limitCount);
        e.putInt(k("maxCount"), maxCount);
        e.putBoolean(k("limitTime"), limitTime);
        e.putInt(k("maxSeconds"), maxSeconds);
        e.putBoolean("vibrate", vibrate);
        e.putBoolean("notify", notify);
        e.putBoolean("volumeKey", volumeKey);
        e.putBoolean(k("autoPause"), autoPause);
        e.putBoolean("keepAwake", keepAwake);
        e.putInt("panelX", panelX);
        e.putInt("panelY", panelY);
        e.putInt(k("n"), points.size());
        for (int i = 0; i < points.size(); i++) {
            e.putInt(k("x" + i), points.get(i)[0]);
            e.putInt(k("y" + i), points.get(i)[1]);
        }
        e.apply();
    }

    public void switchProfile(int p) {
        save();
        profile = p;
        prefs.edit().putInt("profile", p).apply();
        load();
    }

    public static int clampInterval(int v) {
        return Math.max(MIN_INTERVAL, Math.min(MAX_INTERVAL, v));
    }

    /** 設定を1行のテキストにエクスポート（共有・バックアップ用） */
    public String exportText() {
        StringBuilder b = new StringBuilder("OT1|");
        b.append(intervalMs).append(',').append(holdMs).append(',').append(roundRobin ? 1 : 0).append(',')
         .append(limitCount ? 1 : 0).append(',').append(maxCount).append(',').append(limitTime ? 1 : 0).append(',')
         .append(maxSeconds).append(',').append(vibrate ? 1 : 0).append(',').append(notify ? 1 : 0).append('|');
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) b.append(';');
            b.append(points.get(i)[0]).append(',').append(points.get(i)[1]);
        }
        return b.toString();
    }

    /** exportText() の文字列を取り込む。失敗時は false を返し何も変更しない */
    public boolean importText(String text) {
        try {
            if (text == null) return false;
            text = text.trim();
            if (!text.startsWith("OT1|")) return false;
            String[] parts = text.substring(4).split("\\|", -1);
            String[] nums = parts[0].split(",");
            int newInterval = clampInterval(Integer.parseInt(nums[0].trim()));
            int newHold = Integer.parseInt(nums[1].trim());
            boolean newRR = nums[2].trim().equals("1");
            boolean newLC = nums[3].trim().equals("1");
            int newMC = Integer.parseInt(nums[4].trim());
            boolean newLT = nums[5].trim().equals("1");
            int newMS = Integer.parseInt(nums[6].trim());
            boolean newVib = nums[7].trim().equals("1");
            boolean newNotify = nums[8].trim().equals("1");
            ArrayList<int[]> newPoints = new ArrayList<int[]>();
            if (parts.length > 1 && parts[1].length() > 0) {
                for (String pt : parts[1].split(";")) {
                    String[] xy = pt.split(",");
                    newPoints.add(new int[]{Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim())});
                }
            }
            intervalMs = newInterval; holdMs = newHold; roundRobin = newRR;
            limitCount = newLC; maxCount = newMC; limitTime = newLT; maxSeconds = newMS;
            vibrate = newVib; notify = newNotify;
            if (!newPoints.isEmpty()) { points.clear(); points.addAll(newPoints); }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
