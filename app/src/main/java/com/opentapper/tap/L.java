package com.opentapper.tap;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** 多言語文字列。キー=値 の行テーブル。未定義キーは英語→日本語にフォールバック */
public class L {
    public static final String[] CODES = {"ja","en","zh","zh-TW","ko","es","fr","de","pt","ru","it","hi","id","vi","th","tr","ar"};
    public static final String[] NAMES = {"日本語","English","中文（简体）","中文（繁體）","한국어","Español","Français","Deutsch","Português","Русский","Italiano","हिन्दी","Bahasa Indonesia","Tiếng Việt","ไทย","Türkçe","العربية"};

    private static final Map<String, Map<String, String>> T = new LinkedHashMap<String, Map<String, String>>();
    private static String cur = "en";

    public static void init(Context c) {
        SharedPreferences p = c.getSharedPreferences("opentapper", Context.MODE_PRIVATE);
        String saved = p.getString("lang", null);
        if (saved == null) {
            Locale lc = Locale.getDefault();
            String lang = lc.getLanguage(), country = lc.getCountry();
            if (lang.equals("zh")) saved = (country.equals("TW") || country.equals("HK") || country.equals("MO")) ? "zh-TW" : "zh";
            else saved = lang;
            if (!T.containsKey(saved)) saved = "en";
        }
        cur = T.containsKey(saved) ? saved : "en";
    }

    public static void set(Context c, String code) {
        cur = T.containsKey(code) ? code : "en";
        c.getSharedPreferences("opentapper", Context.MODE_PRIVATE).edit().putString("lang", cur).apply();
    }

    public static String current() { return cur; }

    public static String s(String key) {
        Map<String, String> m = T.get(cur);
        String v = m == null ? null : m.get(key);
        if (v == null) v = T.get("en").get(key);
        if (v == null) v = T.get("ja").get(key);
        return v == null ? key : v;
    }

    public static String f(String key, Object... args) {
        try { return String.format(s(key), args); } catch (Throwable t) { return s(key); }
    }

    private static void put(String code, String table) {
        Map<String, String> m = T.get(code);
        if (m == null) { m = new HashMap<String, String>(); T.put(code, m); }
        for (String line : table.split("\n")) {
            int i = line.indexOf('=');
            if (i > 0) m.put(line.substring(0, i).trim(), line.substring(i + 1).trim());
        }
    }

    static {
        put("ja",
        "sec_smart=自動制御\n" +
        "auto_pause=対象アプリを離れたら自動停止\n" +
        "keep_awake=実行中は画面消灯を防ぐ\n" +
        "auto_paused=アプリが切り替わったため停止しました\n" +
        "sec_backup=バックアップ\n" +
        "export=書き出し\n" +
        "import=読み込み\n" +
        "exported=設定をコピーしました\n" +
        "imported=設定を読み込みました\n" +
        "import_failed=クリップボードに有効な設定が見つかりません\n" +
        "vol_key=音量ダウンキーで開始/停止\n" +
        "status_on=● 稼働中\nstatus_off=○ 未接続\nbig_on=タップして起動\nbig_off=ユーザー補助を有効化\n" +
        "hint_on=フローティングパネルとマーカーを表示します\nhint_off=設定 → ユーザー補助 → OpenTapper をオンにしてください\n" +
        "stat_total=累計タップ\nstat_interval=間隔\nstat_points=ポイント\n" +
        "act_a11y=ユーザー補助を開く\nact_a11y_sub=アクセシビリティ設定\nact_help=使い方\nact_help_sub=パネルの操作\n" +
        "act_settings=設定\nact_settings_sub=言語など\nlanguage=言語\n" +
        "step_run=開始と停止。実行中はマーカーが半透明になりタップが素通りします\n" +
        "step_int=間隔(ms)。数字タップでプリセット切替、⚙内に細かいスライダー\n" +
        "step_pt=マーカーを追加／削除。ドラッグで押す位置へ（最大10点）\n" +
        "step_gear=押下時間・順番モード・自動停止・プロファイル・統計\n" +
        "step_handle=ドラッグでパネル移動。タップで⚡ボタンに収納\n" +
        "step_widget=ホーム画面に置くと 起動 → 開始 → 停止 をワンタッチ\nwidget=ウィジェット\n" +
        "foot=オープンソース · 描画権限不要 · データ収集なし\n" +
        "need_a11y=先にアクセシビリティを有効化してください\nadd_point_first=「●+」でタップ位置を追加してください\n" +
        "limit_count=回数制限に到達\nlimit_time=時間制限に到達\nmax_points=最大10点まで\nmin_points=最低1点は必要です\n" +
        "settings=設定\nclose=閉じる\nprofile=プロファイル\nslot=スロット%d\nloaded=スロット%dを読込\n" +
        "sec_tap=タップ\ninterval_fine=間隔（細かく）\nhold=押下時間\n" +
        "speed_note=※ 下限10ms。これより短くするとAndroid本体（system_server）が落ちる恐れがあるため制限しています\n" +
        "round_robin=順番モード（1点ずつ順にタップ）\nsec_autostop=自動停止\nstop_count=回数で停止\ncount=回数\nstop_time=時間で停止\nseconds=秒数\n" +
        "sec_feedback=フィードバック\nvibrate=バイブ（開始/停止）\nnotify=通知バーに表示\nsec_stats=統計\n" +
        "stats_fmt=累計 %d 回 / 今回 %d 回 / %.1f 回/秒\nreset_total=累計をリセット\n" +
        "u_ms=ms\nu_times=回\nu_sec=秒\n" +
        "w_launch=起動\nw_start=開始\nw_stop=停止\nnotif_title=OpenTapper 実行中\nnotif_text=間隔 %dms / %d点\nchannel=OpenTapper 実行状態\n");

        put("en",
        "sec_smart=Smart control\n" +
        "auto_pause=Auto-stop when leaving the target app\n" +
        "keep_awake=Keep screen on while running\n" +
        "auto_paused=Stopped because the app switched\n" +
        "sec_backup=Backup\n" +
        "export=Export\n" +
        "import=Import\n" +
        "exported=Config copied to clipboard\n" +
        "imported=Config imported\n" +
        "import_failed=No valid config found on the clipboard\n" +
        "vol_key=Volume-down key starts/stops\n" +
        "status_on=● Running\nstatus_off=○ Not connected\nbig_on=Tap to launch\nbig_off=Enable Accessibility\n" +
        "hint_on=Shows the floating panel and tap markers\nhint_off=Settings → Accessibility → turn on OpenTapper\n" +
        "stat_total=Total taps\nstat_interval=Interval\nstat_points=Points\n" +
        "act_a11y=Open Accessibility\nact_a11y_sub=System settings\nact_help=How to use\nact_help_sub=Panel controls\n" +
        "act_settings=Settings\nact_settings_sub=Language & more\nlanguage=Language\n" +
        "step_run=Start / stop. While running, markers turn translucent and let taps pass through\n" +
        "step_int=Interval (ms). Tap the number for presets; fine slider inside ⚙\n" +
        "step_pt=Add / remove markers. Drag to the spot to tap (up to 10)\n" +
        "step_gear=Hold time, round-robin, auto-stop, profiles, stats\n" +
        "step_handle=Drag to move the panel. Tap to collapse into a ⚡ bubble\n" +
        "step_widget=Put it on your home screen: launch → start → stop in one tap\nwidget=Widget\n" +
        "foot=Open source · No overlay permission · No data collection\n" +
        "need_a11y=Enable the accessibility service first\nadd_point_first=Add a tap point with “●+”\n" +
        "limit_count=Tap limit reached\nlimit_time=Time limit reached\nmax_points=Up to 10 points\nmin_points=At least 1 point is required\n" +
        "settings=Settings\nclose=Close\nprofile=Profile\nslot=Slot %d\nloaded=Loaded slot %d\n" +
        "sec_tap=Tap\ninterval_fine=Interval (fine)\nhold=Hold time\n" +
        "speed_note=Minimum 10 ms. Shorter intervals can crash Android itself (system_server), so it is capped\n" +
        "round_robin=Round-robin (one point at a time)\nsec_autostop=Auto stop\nstop_count=Stop after count\ncount=Count\nstop_time=Stop after time\nseconds=Seconds\n" +
        "sec_feedback=Feedback\nvibrate=Vibrate (start/stop)\nnotify=Show in notification bar\nsec_stats=Stats\n" +
        "stats_fmt=Total %d / Session %d / %.1f taps/s\nreset_total=Reset total\n" +
        "u_ms=ms\nu_times=x\nu_sec=s\n" +
        "w_launch=Launch\nw_start=Start\nw_stop=Stop\nnotif_title=OpenTapper running\nnotif_text=Interval %dms / %d points\nchannel=OpenTapper status\n");

        put("zh",
        "vol_key=音量减键开始/停止\n" +
        "status_on=● 运行中\nstatus_off=○ 未连接\nbig_on=点按启动\nbig_off=开启无障碍服务\n" +
        "hint_on=显示悬浮面板和点击标记\nhint_off=设置 → 无障碍 → 开启 OpenTapper\n" +
        "stat_total=累计点击\nstat_interval=间隔\nstat_points=点位\n" +
        "act_a11y=打开无障碍设置\nact_a11y_sub=系统设置\nact_help=使用方法\nact_help_sub=面板操作\n" +
        "act_settings=设置\nact_settings_sub=语言等\nlanguage=语言\n" +
        "step_run=开始/停止。运行时标记变半透明，点击可穿透\n" +
        "step_int=间隔(ms)。点数字切换预设，⚙内有精细滑块\n" +
        "step_pt=添加/删除标记。拖到要点击的位置（最多10个）\n" +
        "step_gear=按压时长、轮流模式、自动停止、配置、统计\n" +
        "step_handle=拖动移动面板。点按收起为⚡按钮\n" +
        "step_widget=放到主屏幕：启动 → 开始 → 停止 一键完成\nwidget=小部件\n" +
        "foot=开源 · 无需悬浮窗权限 · 不收集数据\n" +
        "need_a11y=请先开启无障碍服务\nadd_point_first=请用“●+”添加点击位置\n" +
        "limit_count=已达次数上限\nlimit_time=已达时间上限\nmax_points=最多10个点\nmin_points=至少需要1个点\n" +
        "settings=设置\nclose=关闭\nprofile=配置\nslot=槽位%d\nloaded=已载入槽位%d\n" +
        "sec_tap=点击\ninterval_fine=间隔（精细）\nhold=按压时长\n" +
        "speed_note=注：1ms 等极短间隔设备无法处理，实际速度上限约 20–50ms\n" +
        "round_robin=轮流模式（逐点点击）\nsec_autostop=自动停止\nstop_count=按次数停止\ncount=次数\nstop_time=按时间停止\nseconds=秒\n" +
        "sec_feedback=反馈\nvibrate=振动（开始/停止）\nnotify=显示在通知栏\nsec_stats=统计\n" +
        "stats_fmt=累计 %d / 本次 %d / %.1f 次/秒\nreset_total=重置累计\n" +
        "u_ms=ms\nu_times=次\nu_sec=秒\n" +
        "w_launch=启动\nw_start=开始\nw_stop=停止\nnotif_title=OpenTapper 运行中\nnotif_text=间隔 %dms / %d 点\nchannel=OpenTapper 状态\n");

        put("zh-TW",
        "vol_key=音量減鍵開始/停止\n" +
        "status_on=● 執行中\nstatus_off=○ 未連線\nbig_on=點按啟動\nbig_off=開啟無障礙服務\n" +
        "hint_on=顯示懸浮面板與點擊標記\nhint_off=設定 → 無障礙 → 開啟 OpenTapper\n" +
        "stat_total=累計點擊\nstat_interval=間隔\nstat_points=點位\n" +
        "act_a11y=開啟無障礙設定\nact_a11y_sub=系統設定\nact_help=使用方法\nact_help_sub=面板操作\n" +
        "act_settings=設定\nact_settings_sub=語言等\nlanguage=語言\n" +
        "step_run=開始/停止。執行時標記變半透明，點擊可穿透\n" +
        "step_int=間隔(ms)。點數字切換預設，⚙內有精細滑桿\n" +
        "step_pt=新增/刪除標記。拖到要點擊的位置（最多10個）\n" +
        "step_gear=按壓時間、輪流模式、自動停止、設定檔、統計\n" +
        "step_handle=拖曳移動面板。點按收合為⚡按鈕\n" +
        "step_widget=放到主畫面：啟動 → 開始 → 停止 一鍵完成\nwidget=小工具\n" +
        "foot=開源 · 無需懸浮視窗權限 · 不蒐集資料\n" +
        "need_a11y=請先開啟無障礙服務\nadd_point_first=請用「●+」新增點擊位置\n" +
        "limit_count=已達次數上限\nlimit_time=已達時間上限\nmax_points=最多10個點\nmin_points=至少需要1個點\n" +
        "settings=設定\nclose=關閉\nprofile=設定檔\nslot=槽位%d\nloaded=已載入槽位%d\n" +
        "sec_tap=點擊\ninterval_fine=間隔（精細）\nhold=按壓時間\n" +
        "speed_note=註：1ms 等極短間隔裝置無法處理，實際速度上限約 20–50ms\n" +
        "round_robin=輪流模式（逐點點擊）\nsec_autostop=自動停止\nstop_count=依次數停止\ncount=次數\nstop_time=依時間停止\nseconds=秒\n" +
        "sec_feedback=回饋\nvibrate=震動（開始/停止）\nnotify=顯示在通知列\nsec_stats=統計\n" +
        "stats_fmt=累計 %d / 本次 %d / %.1f 次/秒\nreset_total=重設累計\n" +
        "u_ms=ms\nu_times=次\nu_sec=秒\n" +
        "w_launch=啟動\nw_start=開始\nw_stop=停止\nnotif_title=OpenTapper 執行中\nnotif_text=間隔 %dms / %d 點\nchannel=OpenTapper 狀態\n");

        put("ko",
        "vol_key=볼륨 다운 키로 시작/정지\n" +
        "status_on=● 실행 중\nstatus_off=○ 연결 안 됨\nbig_on=탭하여 시작\nbig_off=접근성 켜기\n" +
        "hint_on=플로팅 패널과 탭 마커를 표시합니다\nhint_off=설정 → 접근성 → OpenTapper 켜기\n" +
        "stat_total=누적 탭\nstat_interval=간격\nstat_points=포인트\n" +
        "act_a11y=접근성 열기\nact_a11y_sub=시스템 설정\nact_help=사용법\nact_help_sub=패널 조작\n" +
        "act_settings=설정\nact_settings_sub=언어 등\nlanguage=언어\n" +
        "step_run=시작/정지. 실행 중에는 마커가 반투명해지고 탭이 통과합니다\n" +
        "step_int=간격(ms). 숫자를 탭하면 프리셋 전환, ⚙ 안에 세밀 슬라이더\n" +
        "step_pt=마커 추가/삭제. 누를 위치로 드래그 (최대 10개)\n" +
        "step_gear=누름 시간, 순서 모드, 자동 정지, 프로필, 통계\n" +
        "step_handle=드래그로 패널 이동. 탭하면 ⚡ 버튼으로 접기\n" +
        "step_widget=홈 화면에 두면 시작 → 실행 → 정지를 한 번에\nwidget=위젯\n" +
        "foot=오픈소스 · 오버레이 권한 불필요 · 데이터 수집 없음\n" +
        "need_a11y=먼저 접근성 서비스를 켜 주세요\nadd_point_first=“●+”로 탭 위치를 추가하세요\n" +
        "limit_count=횟수 제한 도달\nlimit_time=시간 제한 도달\nmax_points=최대 10개\nmin_points=최소 1개가 필요합니다\n" +
        "settings=설정\nclose=닫기\nprofile=프로필\nslot=슬롯 %d\nloaded=슬롯 %d 불러옴\n" +
        "sec_tap=탭\ninterval_fine=간격(세밀)\nhold=누름 시간\n" +
        "speed_note=※ 1ms 같은 극단적으로 짧은 간격은 기기가 처리하지 못해 실제 속도는 20~50ms 부근에서 한계에 이릅니다\n" +
        "round_robin=순서 모드 (한 점씩 차례로)\nsec_autostop=자동 정지\nstop_count=횟수로 정지\ncount=횟수\nstop_time=시간으로 정지\nseconds=초\n" +
        "sec_feedback=피드백\nvibrate=진동 (시작/정지)\nnotify=알림 표시줄에 표시\nsec_stats=통계\n" +
        "stats_fmt=누적 %d / 이번 %d / %.1f 회/초\nreset_total=누적 초기화\n" +
        "u_ms=ms\nu_times=회\nu_sec=초\n" +
        "w_launch=실행\nw_start=시작\nw_stop=정지\nnotif_title=OpenTapper 실행 중\nnotif_text=간격 %dms / %d 포인트\nchannel=OpenTapper 상태\n");

        put("es",
        "vol_key=Tecla bajar volumen: iniciar/detener\n" +
        "status_on=● Activo\nstatus_off=○ Sin conexión\nbig_on=Toca para abrir\nbig_off=Activar Accesibilidad\n" +
        "hint_on=Muestra el panel flotante y los marcadores\nhint_off=Ajustes → Accesibilidad → activa OpenTapper\n" +
        "stat_total=Toques totales\nstat_interval=Intervalo\nstat_points=Puntos\n" +
        "act_a11y=Abrir Accesibilidad\nact_a11y_sub=Ajustes del sistema\nact_help=Cómo usar\nact_help_sub=Controles del panel\n" +
        "act_settings=Ajustes\nact_settings_sub=Idioma y más\nlanguage=Idioma\n" +
        "step_run=Iniciar / detener. Al ejecutarse, los marcadores se vuelven translúcidos y dejan pasar los toques\n" +
        "step_int=Intervalo (ms). Toca el número para preajustes; control fino en ⚙\n" +
        "step_pt=Añadir / quitar marcadores. Arrástralos al punto a tocar (hasta 10)\n" +
        "step_gear=Duración, modo alterno, parada automática, perfiles, estadísticas\n" +
        "step_handle=Arrastra para mover el panel. Toca para plegarlo en un botón ⚡\n" +
        "step_widget=Ponlo en la pantalla de inicio: abrir → iniciar → detener con un toque\nwidget=Widget\n" +
        "foot=Código abierto · Sin permiso de superposición · Sin recopilar datos\n" +
        "need_a11y=Activa primero el servicio de accesibilidad\nadd_point_first=Añade un punto con “●+”\n" +
        "limit_count=Límite de toques alcanzado\nlimit_time=Límite de tiempo alcanzado\nmax_points=Máximo 10 puntos\nmin_points=Se necesita al menos 1 punto\n" +
        "settings=Ajustes\nclose=Cerrar\nprofile=Perfil\nslot=Ranura %d\nloaded=Ranura %d cargada\n" +
        "sec_tap=Toque\ninterval_fine=Intervalo (fino)\nhold=Duración\n" +
        "speed_note=Nota: intervalos muy cortos (p. ej. 1 ms) no pueden procesarse; la velocidad real tope está en 20–50 ms\n" +
        "round_robin=Modo alterno (un punto cada vez)\nsec_autostop=Parada automática\nstop_count=Detener por número\ncount=Número\nstop_time=Detener por tiempo\nseconds=Segundos\n" +
        "sec_feedback=Respuesta\nvibrate=Vibrar (inicio/parada)\nnotify=Mostrar en notificaciones\nsec_stats=Estadísticas\n" +
        "stats_fmt=Total %d / Sesión %d / %.1f toques/s\nreset_total=Reiniciar total\n" +
        "u_ms=ms\nu_times=x\nu_sec=s\n" +
        "w_launch=Abrir\nw_start=Iniciar\nw_stop=Detener\nnotif_title=OpenTapper activo\nnotif_text=Intervalo %dms / %d puntos\nchannel=Estado de OpenTapper\n");

        put("fr",
        "vol_key=Touche volume − : démarrer/arrêter\n" +
        "status_on=● En marche\nstatus_off=○ Non connecté\nbig_on=Touchez pour lancer\nbig_off=Activer l’accessibilité\n" +
        "hint_on=Affiche le panneau flottant et les repères\nhint_off=Paramètres → Accessibilité → activez OpenTapper\n" +
        "stat_total=Total des taps\nstat_interval=Intervalle\nstat_points=Points\n" +
        "act_a11y=Ouvrir l’accessibilité\nact_a11y_sub=Paramètres système\nact_help=Mode d’emploi\nact_help_sub=Commandes du panneau\n" +
        "act_settings=Réglages\nact_settings_sub=Langue et plus\nlanguage=Langue\n" +
        "step_run=Démarrer / arrêter. En marche, les repères deviennent translucides et laissent passer les taps\n" +
        "step_int=Intervalle (ms). Touchez le nombre pour les préréglages ; curseur fin dans ⚙\n" +
        "step_pt=Ajouter / retirer des repères. Glissez-les à l’endroit à toucher (10 max)\n" +
        "step_gear=Durée d’appui, mode alterné, arrêt auto, profils, statistiques\n" +
        "step_handle=Glissez pour déplacer le panneau. Touchez pour le replier en bulle ⚡\n" +
        "step_widget=Sur l’écran d’accueil : lancer → démarrer → arrêter d’un seul geste\nwidget=Widget\n" +
        "foot=Open source · Sans permission de superposition · Aucune collecte de données\n" +
        "need_a11y=Activez d’abord le service d’accessibilité\nadd_point_first=Ajoutez un point avec « ●+ »\n" +
        "limit_count=Limite de taps atteinte\nlimit_time=Limite de temps atteinte\nmax_points=10 points maximum\nmin_points=Au moins 1 point requis\n" +
        "settings=Réglages\nclose=Fermer\nprofile=Profil\nslot=Emplacement %d\nloaded=Emplacement %d chargé\n" +
        "sec_tap=Tap\ninterval_fine=Intervalle (fin)\nhold=Durée d’appui\n" +
        "speed_note=Remarque : des intervalles très courts (ex. 1 ms) ne peuvent pas être traités ; la vitesse réelle plafonne vers 20–50 ms\n" +
        "round_robin=Mode alterné (un point à la fois)\nsec_autostop=Arrêt automatique\nstop_count=Arrêter après N taps\ncount=Nombre\nstop_time=Arrêter après un délai\nseconds=Secondes\n" +
        "sec_feedback=Retour\nvibrate=Vibrer (début/fin)\nnotify=Afficher dans les notifications\nsec_stats=Statistiques\n" +
        "stats_fmt=Total %d / Session %d / %.1f taps/s\nreset_total=Réinitialiser le total\n" +
        "u_ms=ms\nu_times=x\nu_sec=s\n" +
        "w_launch=Lancer\nw_start=Démarrer\nw_stop=Arrêter\nnotif_title=OpenTapper en marche\nnotif_text=Intervalle %dms / %d points\nchannel=État d’OpenTapper\n");

        put("de",
        "vol_key=Leiser-Taste startet/stoppt\n" +
        "status_on=● Aktiv\nstatus_off=○ Nicht verbunden\nbig_on=Tippen zum Starten\nbig_off=Bedienungshilfe aktivieren\n" +
        "hint_on=Zeigt das schwebende Panel und die Tipp-Marker\nhint_off=Einstellungen → Bedienungshilfen → OpenTapper einschalten\n" +
        "stat_total=Tipps gesamt\nstat_interval=Intervall\nstat_points=Punkte\n" +
        "act_a11y=Bedienungshilfen öffnen\nact_a11y_sub=Systemeinstellungen\nact_help=Anleitung\nact_help_sub=Panel-Bedienung\n" +
        "act_settings=Einstellungen\nact_settings_sub=Sprache & mehr\nlanguage=Sprache\n" +
        "step_run=Start / Stopp. Während der Ausführung werden Marker durchscheinend und lassen Tipps durch\n" +
        "step_int=Intervall (ms). Zahl antippen für Voreinstellungen; Feinregler in ⚙\n" +
        "step_pt=Marker hinzufügen / entfernen. Zum Tipp-Ziel ziehen (max. 10)\n" +
        "step_gear=Haltezeit, Reihum-Modus, Auto-Stopp, Profile, Statistik\n" +
        "step_handle=Ziehen, um das Panel zu bewegen. Antippen zum Einklappen in eine ⚡-Blase\n" +
        "step_widget=Auf den Startbildschirm legen: Öffnen → Start → Stopp mit einem Tipp\nwidget=Widget\n" +
        "foot=Open Source · Keine Overlay-Berechtigung · Keine Datensammlung\n" +
        "need_a11y=Bitte zuerst den Bedienungshilfen-Dienst aktivieren\nadd_point_first=Mit „●+“ einen Tipp-Punkt hinzufügen\n" +
        "limit_count=Tipp-Limit erreicht\nlimit_time=Zeitlimit erreicht\nmax_points=Maximal 10 Punkte\nmin_points=Mindestens 1 Punkt nötig\n" +
        "settings=Einstellungen\nclose=Schließen\nprofile=Profil\nslot=Slot %d\nloaded=Slot %d geladen\n" +
        "sec_tap=Tipp\ninterval_fine=Intervall (fein)\nhold=Haltezeit\n" +
        "speed_note=Hinweis: Extrem kurze Intervalle (z. B. 1 ms) kann das Gerät nicht verarbeiten; real sind etwa 20–50 ms das Limit\n" +
        "round_robin=Reihum-Modus (ein Punkt nach dem anderen)\nsec_autostop=Auto-Stopp\nstop_count=Nach Anzahl stoppen\ncount=Anzahl\nstop_time=Nach Zeit stoppen\nseconds=Sekunden\n" +
        "sec_feedback=Rückmeldung\nvibrate=Vibrieren (Start/Stopp)\nnotify=In der Benachrichtigungsleiste anzeigen\nsec_stats=Statistik\n" +
        "stats_fmt=Gesamt %d / Sitzung %d / %.1f Tipps/s\nreset_total=Gesamt zurücksetzen\n" +
        "u_ms=ms\nu_times=x\nu_sec=s\n" +
        "w_launch=Öffnen\nw_start=Start\nw_stop=Stopp\nnotif_title=OpenTapper läuft\nnotif_text=Intervall %dms / %d Punkte\nchannel=OpenTapper-Status\n");

        put("pt",
        "vol_key=Tecla volume − inicia/para\n" +
        "status_on=● Em execução\nstatus_off=○ Não conectado\nbig_on=Toque para abrir\nbig_off=Ativar Acessibilidade\n" +
        "hint_on=Mostra o painel flutuante e os marcadores\nhint_off=Configurações → Acessibilidade → ative o OpenTapper\n" +
        "stat_total=Toques totais\nstat_interval=Intervalo\nstat_points=Pontos\n" +
        "act_a11y=Abrir Acessibilidade\nact_a11y_sub=Configurações do sistema\nact_help=Como usar\nact_help_sub=Controles do painel\n" +
        "act_settings=Configurações\nact_settings_sub=Idioma e mais\nlanguage=Idioma\n" +
        "step_run=Iniciar / parar. Em execução, os marcadores ficam translúcidos e deixam os toques passar\n" +
        "step_int=Intervalo (ms). Toque no número para predefinições; ajuste fino em ⚙\n" +
        "step_pt=Adicionar / remover marcadores. Arraste até o ponto a tocar (até 10)\n" +
        "step_gear=Duração, modo alternado, parada automática, perfis, estatísticas\n" +
        "step_handle=Arraste para mover o painel. Toque para recolher em um botão ⚡\n" +
        "step_widget=Coloque na tela inicial: abrir → iniciar → parar com um toque\nwidget=Widget\n" +
        "foot=Código aberto · Sem permissão de sobreposição · Sem coleta de dados\n" +
        "need_a11y=Ative primeiro o serviço de acessibilidade\nadd_point_first=Adicione um ponto com “●+”\n" +
        "limit_count=Limite de toques atingido\nlimit_time=Limite de tempo atingido\nmax_points=Máximo de 10 pontos\nmin_points=É necessário ao menos 1 ponto\n" +
        "settings=Configurações\nclose=Fechar\nprofile=Perfil\nslot=Slot %d\nloaded=Slot %d carregado\n" +
        "sec_tap=Toque\ninterval_fine=Intervalo (fino)\nhold=Duração\n" +
        "speed_note=Obs.: intervalos muito curtos (ex. 1 ms) não são processados; a velocidade real satura por volta de 20–50 ms\n" +
        "round_robin=Modo alternado (um ponto por vez)\nsec_autostop=Parada automática\nstop_count=Parar por quantidade\ncount=Quantidade\nstop_time=Parar por tempo\nseconds=Segundos\n" +
        "sec_feedback=Resposta\nvibrate=Vibrar (início/parada)\nnotify=Mostrar na barra de notificações\nsec_stats=Estatísticas\n" +
        "stats_fmt=Total %d / Sessão %d / %.1f toques/s\nreset_total=Zerar total\n" +
        "u_ms=ms\nu_times=x\nu_sec=s\n" +
        "w_launch=Abrir\nw_start=Iniciar\nw_stop=Parar\nnotif_title=OpenTapper em execução\nnotif_text=Intervalo %dms / %d pontos\nchannel=Status do OpenTapper\n");

        put("ru",
        "vol_key=Клавиша «тише» — старт/стоп\n" +
        "status_on=● Работает\nstatus_off=○ Не подключено\nbig_on=Нажмите для запуска\nbig_off=Включить спец. возможности\n" +
        "hint_on=Показывает плавающую панель и маркеры\nhint_off=Настройки → Спец. возможности → включите OpenTapper\n" +
        "stat_total=Всего нажатий\nstat_interval=Интервал\nstat_points=Точки\n" +
        "act_a11y=Открыть спец. возможности\nact_a11y_sub=Системные настройки\nact_help=Как пользоваться\nact_help_sub=Управление панелью\n" +
        "act_settings=Настройки\nact_settings_sub=Язык и другое\nlanguage=Язык\n" +
        "step_run=Старт / стоп. Во время работы маркеры становятся полупрозрачными и пропускают нажатия\n" +
        "step_int=Интервал (мс). Нажмите на число для пресетов; точный ползунок в ⚙\n" +
        "step_pt=Добавить / убрать маркеры. Перетащите в нужное место (до 10)\n" +
        "step_gear=Время удержания, поочерёдный режим, автостоп, профили, статистика\n" +
        "step_handle=Перетащите, чтобы переместить панель. Нажмите, чтобы свернуть в кнопку ⚡\n" +
        "step_widget=Добавьте на главный экран: запуск → старт → стоп одним нажатием\nwidget=Виджет\n" +
        "foot=Открытый код · Без разрешения на наложение · Без сбора данных\n" +
        "need_a11y=Сначала включите службу спец. возможностей\nadd_point_first=Добавьте точку кнопкой «●+»\n" +
        "limit_count=Достигнут лимит нажатий\nlimit_time=Достигнут лимит времени\nmax_points=Не более 10 точек\nmin_points=Нужна хотя бы 1 точка\n" +
        "settings=Настройки\nclose=Закрыть\nprofile=Профиль\nslot=Слот %d\nloaded=Загружен слот %d\n" +
        "sec_tap=Нажатие\ninterval_fine=Интервал (точно)\nhold=Время удержания\n" +
        "speed_note=Примечание: слишком короткие интервалы (например 1 мс) устройство не обработает; реальный предел около 20–50 мс\n" +
        "round_robin=Поочерёдный режим (по одной точке)\nsec_autostop=Автостоп\nstop_count=Остановить по количеству\ncount=Количество\nstop_time=Остановить по времени\nseconds=Секунды\n" +
        "sec_feedback=Обратная связь\nvibrate=Вибрация (старт/стоп)\nnotify=Показывать в уведомлениях\nsec_stats=Статистика\n" +
        "stats_fmt=Всего %d / Сеанс %d / %.1f наж./с\nreset_total=Сбросить итог\n" +
        "u_ms=мс\nu_times=раз\nu_sec=с\n" +
        "w_launch=Запуск\nw_start=Старт\nw_stop=Стоп\nnotif_title=OpenTapper работает\nnotif_text=Интервал %dмс / точек: %d\nchannel=Состояние OpenTapper\n");

        put("it",
        "vol_key=Tasto volume − avvia/ferma\n" +
        "status_on=● In esecuzione\nstatus_off=○ Non connesso\nbig_on=Tocca per avviare\nbig_off=Attiva Accessibilità\n" +
        "hint_on=Mostra il pannello flottante e i marcatori\nhint_off=Impostazioni → Accessibilità → attiva OpenTapper\n" +
        "stat_total=Tocchi totali\nstat_interval=Intervallo\nstat_points=Punti\n" +
        "act_a11y=Apri Accessibilità\nact_a11y_sub=Impostazioni di sistema\nact_help=Come si usa\nact_help_sub=Controlli del pannello\n" +
        "act_settings=Impostazioni\nact_settings_sub=Lingua e altro\nlanguage=Lingua\n" +
        "step_run=Avvia / ferma. Durante l’esecuzione i marcatori diventano traslucidi e lasciano passare i tocchi\n" +
        "step_int=Intervallo (ms). Tocca il numero per i preset; cursore fine in ⚙\n" +
        "step_pt=Aggiungi / rimuovi marcatori. Trascinali sul punto da toccare (max 10)\n" +
        "step_gear=Durata, modalità alternata, arresto automatico, profili, statistiche\n" +
        "step_handle=Trascina per spostare il pannello. Tocca per ridurlo a un pulsante ⚡\n" +
        "step_widget=Mettilo nella schermata Home: apri → avvia → ferma con un tocco\nwidget=Widget\n" +
        "foot=Open source · Nessun permesso overlay · Nessuna raccolta dati\n" +
        "need_a11y=Attiva prima il servizio di accessibilità\nadd_point_first=Aggiungi un punto con “●+”\n" +
        "limit_count=Limite di tocchi raggiunto\nlimit_time=Limite di tempo raggiunto\nmax_points=Massimo 10 punti\nmin_points=Serve almeno 1 punto\n" +
        "settings=Impostazioni\nclose=Chiudi\nprofile=Profilo\nslot=Slot %d\nloaded=Slot %d caricato\n" +
        "sec_tap=Tocco\ninterval_fine=Intervallo (fine)\nhold=Durata\n" +
        "speed_note=Nota: intervalli troppo brevi (es. 1 ms) non vengono elaborati; la velocità reale si ferma intorno a 20–50 ms\n" +
        "round_robin=Modalità alternata (un punto alla volta)\nsec_autostop=Arresto automatico\nstop_count=Ferma dopo N tocchi\ncount=Numero\nstop_time=Ferma dopo un tempo\nseconds=Secondi\n" +
        "sec_feedback=Feedback\nvibrate=Vibrazione (avvio/arresto)\nnotify=Mostra nelle notifiche\nsec_stats=Statistiche\n" +
        "stats_fmt=Totale %d / Sessione %d / %.1f tocchi/s\nreset_total=Azzera totale\n" +
        "u_ms=ms\nu_times=x\nu_sec=s\n" +
        "w_launch=Apri\nw_start=Avvia\nw_stop=Ferma\nnotif_title=OpenTapper in esecuzione\nnotif_text=Intervallo %dms / %d punti\nchannel=Stato di OpenTapper\n");

        put("hi",
        "status_on=● चालू\nstatus_off=○ कनेक्ट नहीं\nbig_on=शुरू करने के लिए टैप करें\nbig_off=एक्सेसिबिलिटी चालू करें\n" +
        "hint_on=फ्लोटिंग पैनल और टैप मार्कर दिखाता है\nhint_off=सेटिंग्स → एक्सेसिबिलिटी → OpenTapper चालू करें\n" +
        "stat_total=कुल टैप\nstat_interval=अंतराल\nstat_points=बिंदु\n" +
        "act_a11y=एक्सेसिबिलिटी खोलें\nact_a11y_sub=सिस्टम सेटिंग्स\nact_help=उपयोग विधि\nact_help_sub=पैनल नियंत्रण\n" +
        "act_settings=सेटिंग्स\nact_settings_sub=भाषा और अधिक\nlanguage=भाषा\n" +
        "step_run=शुरू / रोकें। चलते समय मार्कर पारदर्शी हो जाते हैं और टैप पार हो जाते हैं\n" +
        "step_int=अंतराल (ms)। प्रीसेट के लिए संख्या टैप करें; ⚙ में सूक्ष्म स्लाइडर\n" +
        "step_pt=मार्कर जोड़ें / हटाएँ। टैप स्थान पर खींचें (अधिकतम 10)\n" +
        "step_gear=दबाव समय, क्रम मोड, स्वतः रोक, प्रोफ़ाइल, आँकड़े\n" +
        "step_handle=पैनल हिलाने के लिए खींचें। ⚡ बटन में समेटने के लिए टैप करें\n" +
        "step_widget=होम स्क्रीन पर रखें: खोलें → शुरू → रोकें, एक टैप में\nwidget=विजेट\n" +
        "foot=ओपन सोर्स · ओवरले अनुमति नहीं चाहिए · कोई डेटा संग्रह नहीं\n" +
        "need_a11y=पहले एक्सेसिबिलिटी सेवा चालू करें\nadd_point_first=“●+” से टैप बिंदु जोड़ें\n" +
        "limit_count=टैप सीमा पूरी\nlimit_time=समय सीमा पूरी\nmax_points=अधिकतम 10 बिंदु\nmin_points=कम से कम 1 बिंदु चाहिए\n" +
        "settings=सेटिंग्स\nclose=बंद करें\nprofile=प्रोफ़ाइल\nslot=स्लॉट %d\nloaded=स्लॉट %d लोड हुआ\n" +
        "sec_tap=टैप\ninterval_fine=अंतराल (सूक्ष्म)\nhold=दबाव समय\n" +
        "speed_note=नोट: 1 ms जैसे बहुत छोटे अंतराल डिवाइस संभाल नहीं पाता; वास्तविक गति लगभग 20–50 ms पर सीमित होती है\n" +
        "round_robin=क्रम मोड (एक बार में एक बिंदु)\nsec_autostop=स्वतः रोक\nstop_count=संख्या पर रोकें\ncount=संख्या\nstop_time=समय पर रोकें\nseconds=सेकंड\n" +
        "sec_feedback=फीडबैक\nvibrate=कंपन (शुरू/रोक)\nnotify=सूचना पट्टी में दिखाएँ\nsec_stats=आँकड़े\n" +
        "stats_fmt=कुल %d / सत्र %d / %.1f टैप/से\nreset_total=कुल रीसेट करें\n" +
        "u_ms=ms\nu_times=बार\nu_sec=से\n" +
        "w_launch=खोलें\nw_start=शुरू\nw_stop=रोकें\nnotif_title=OpenTapper चालू है\nnotif_text=अंतराल %dms / %d बिंदु\nchannel=OpenTapper स्थिति\n");

        put("id",
        "status_on=● Berjalan\nstatus_off=○ Tidak terhubung\nbig_on=Ketuk untuk membuka\nbig_off=Aktifkan Aksesibilitas\n" +
        "hint_on=Menampilkan panel melayang dan penanda ketuk\nhint_off=Setelan → Aksesibilitas → aktifkan OpenTapper\n" +
        "stat_total=Total ketukan\nstat_interval=Interval\nstat_points=Titik\n" +
        "act_a11y=Buka Aksesibilitas\nact_a11y_sub=Setelan sistem\nact_help=Cara pakai\nact_help_sub=Kontrol panel\n" +
        "act_settings=Setelan\nact_settings_sub=Bahasa & lainnya\nlanguage=Bahasa\n" +
        "step_run=Mulai / berhenti. Saat berjalan, penanda menjadi transparan dan ketukan tembus\n" +
        "step_int=Interval (ms). Ketuk angka untuk preset; slider halus di ⚙\n" +
        "step_pt=Tambah / hapus penanda. Seret ke titik yang diketuk (maks 10)\n" +
        "step_gear=Durasi tekan, mode bergilir, berhenti otomatis, profil, statistik\n" +
        "step_handle=Seret untuk memindahkan panel. Ketuk untuk melipat jadi tombol ⚡\n" +
        "step_widget=Taruh di layar utama: buka → mulai → berhenti dalam sekali ketuk\nwidget=Widget\n" +
        "foot=Open source · Tanpa izin overlay · Tanpa pengumpulan data\n" +
        "need_a11y=Aktifkan layanan aksesibilitas dulu\nadd_point_first=Tambahkan titik dengan “●+”\n" +
        "limit_count=Batas ketukan tercapai\nlimit_time=Batas waktu tercapai\nmax_points=Maksimal 10 titik\nmin_points=Minimal 1 titik diperlukan\n" +
        "settings=Setelan\nclose=Tutup\nprofile=Profil\nslot=Slot %d\nloaded=Slot %d dimuat\n" +
        "sec_tap=Ketuk\ninterval_fine=Interval (halus)\nhold=Durasi tekan\n" +
        "speed_note=Catatan: interval sangat pendek (mis. 1 ms) tidak bisa diproses perangkat; kecepatan nyata mentok sekitar 20–50 ms\n" +
        "round_robin=Mode bergilir (satu titik bergantian)\nsec_autostop=Berhenti otomatis\nstop_count=Berhenti setelah jumlah\ncount=Jumlah\nstop_time=Berhenti setelah waktu\nseconds=Detik\n" +
        "sec_feedback=Umpan balik\nvibrate=Getar (mulai/berhenti)\nnotify=Tampilkan di bilah notifikasi\nsec_stats=Statistik\n" +
        "stats_fmt=Total %d / Sesi %d / %.1f ketuk/dtk\nreset_total=Reset total\n" +
        "u_ms=ms\nu_times=x\nu_sec=dtk\n" +
        "w_launch=Buka\nw_start=Mulai\nw_stop=Berhenti\nnotif_title=OpenTapper berjalan\nnotif_text=Interval %dms / %d titik\nchannel=Status OpenTapper\n");

        put("vi",
        "status_on=● Đang chạy\nstatus_off=○ Chưa kết nối\nbig_on=Chạm để mở\nbig_off=Bật Trợ năng\n" +
        "hint_on=Hiển thị bảng nổi và các điểm chạm\nhint_off=Cài đặt → Trợ năng → bật OpenTapper\n" +
        "stat_total=Tổng số chạm\nstat_interval=Khoảng cách\nstat_points=Điểm\n" +
        "act_a11y=Mở Trợ năng\nact_a11y_sub=Cài đặt hệ thống\nact_help=Cách dùng\nact_help_sub=Điều khiển bảng\n" +
        "act_settings=Cài đặt\nact_settings_sub=Ngôn ngữ & khác\nlanguage=Ngôn ngữ\n" +
        "step_run=Bắt đầu / dừng. Khi chạy, điểm đánh dấu trở nên mờ và cho phép chạm xuyên qua\n" +
        "step_int=Khoảng cách (ms). Chạm vào số để đổi preset; thanh trượt tinh chỉnh trong ⚙\n" +
        "step_pt=Thêm / xóa điểm. Kéo tới vị trí cần chạm (tối đa 10)\n" +
        "step_gear=Thời gian giữ, chế độ luân phiên, tự dừng, hồ sơ, thống kê\n" +
        "step_handle=Kéo để di chuyển bảng. Chạm để thu gọn thành nút ⚡\n" +
        "step_widget=Đặt lên màn hình chính: mở → bắt đầu → dừng chỉ với một chạm\nwidget=Tiện ích\n" +
        "foot=Mã nguồn mở · Không cần quyền hiển thị đè · Không thu thập dữ liệu\n" +
        "need_a11y=Hãy bật dịch vụ trợ năng trước\nadd_point_first=Thêm điểm chạm bằng “●+”\n" +
        "limit_count=Đã đạt giới hạn số lần\nlimit_time=Đã đạt giới hạn thời gian\nmax_points=Tối đa 10 điểm\nmin_points=Cần ít nhất 1 điểm\n" +
        "settings=Cài đặt\nclose=Đóng\nprofile=Hồ sơ\nslot=Ô %d\nloaded=Đã tải ô %d\n" +
        "sec_tap=Chạm\ninterval_fine=Khoảng cách (tinh)\nhold=Thời gian giữ\n" +
        "speed_note=Lưu ý: khoảng cách quá ngắn (vd. 1 ms) thiết bị không xử lý kịp; tốc độ thực tế tối đa khoảng 20–50 ms\n" +
        "round_robin=Chế độ luân phiên (từng điểm một)\nsec_autostop=Tự động dừng\nstop_count=Dừng theo số lần\ncount=Số lần\nstop_time=Dừng theo thời gian\nseconds=Giây\n" +
        "sec_feedback=Phản hồi\nvibrate=Rung (bắt đầu/dừng)\nnotify=Hiện trên thanh thông báo\nsec_stats=Thống kê\n" +
        "stats_fmt=Tổng %d / Phiên %d / %.1f chạm/giây\nreset_total=Đặt lại tổng\n" +
        "u_ms=ms\nu_times=lần\nu_sec=giây\n" +
        "w_launch=Mở\nw_start=Bắt đầu\nw_stop=Dừng\nnotif_title=OpenTapper đang chạy\nnotif_text=Khoảng cách %dms / %d điểm\nchannel=Trạng thái OpenTapper\n");

        put("th",
        "status_on=● กำลังทำงาน\nstatus_off=○ ยังไม่เชื่อมต่อ\nbig_on=แตะเพื่อเปิด\nbig_off=เปิดการช่วยเหลือพิเศษ\n" +
        "hint_on=แสดงแผงลอยและจุดแตะ\nhint_off=การตั้งค่า → การช่วยเหลือพิเศษ → เปิด OpenTapper\n" +
        "stat_total=แตะทั้งหมด\nstat_interval=ช่วงเวลา\nstat_points=จุด\n" +
        "act_a11y=เปิดการช่วยเหลือพิเศษ\nact_a11y_sub=การตั้งค่าระบบ\nact_help=วิธีใช้\nact_help_sub=การควบคุมแผง\n" +
        "act_settings=การตั้งค่า\nact_settings_sub=ภาษาและอื่น ๆ\nlanguage=ภาษา\n" +
        "step_run=เริ่ม / หยุด ขณะทำงานจุดจะโปร่งใสและให้การแตะทะลุผ่าน\n" +
        "step_int=ช่วงเวลา (ms) แตะตัวเลขเพื่อสลับค่าที่ตั้งไว้ มีแถบเลื่อนละเอียดใน ⚙\n" +
        "step_pt=เพิ่ม / ลบจุด ลากไปยังตำแหน่งที่ต้องการแตะ (สูงสุด 10)\n" +
        "step_gear=เวลากด, โหมดสลับจุด, หยุดอัตโนมัติ, โปรไฟล์, สถิติ\n" +
        "step_handle=ลากเพื่อย้ายแผง แตะเพื่อย่อเป็นปุ่ม ⚡\n" +
        "step_widget=วางบนหน้าจอหลัก: เปิด → เริ่ม → หยุด ในแตะเดียว\nwidget=วิดเจ็ต\n" +
        "foot=โอเพนซอร์ส · ไม่ต้องขอสิทธิ์ซ้อนทับ · ไม่เก็บข้อมูล\n" +
        "need_a11y=โปรดเปิดบริการช่วยเหลือพิเศษก่อน\nadd_point_first=เพิ่มจุดแตะด้วย “●+”\n" +
        "limit_count=ถึงจำนวนครั้งที่กำหนด\nlimit_time=ถึงเวลาที่กำหนด\nmax_points=สูงสุด 10 จุด\nmin_points=ต้องมีอย่างน้อย 1 จุด\n" +
        "settings=การตั้งค่า\nclose=ปิด\nprofile=โปรไฟล์\nslot=ช่อง %d\nloaded=โหลดช่อง %d แล้ว\n" +
        "sec_tap=แตะ\ninterval_fine=ช่วงเวลา (ละเอียด)\nhold=เวลากด\n" +
        "speed_note=หมายเหตุ: ช่วงเวลาที่สั้นมาก (เช่น 1 ms) อุปกรณ์ประมวลผลไม่ทัน ความเร็วจริงจะจำกัดที่ราว 20–50 ms\n" +
        "round_robin=โหมดสลับจุด (ทีละจุด)\nsec_autostop=หยุดอัตโนมัติ\nstop_count=หยุดตามจำนวน\ncount=จำนวน\nstop_time=หยุดตามเวลา\nseconds=วินาที\n" +
        "sec_feedback=การตอบสนอง\nvibrate=สั่น (เริ่ม/หยุด)\nnotify=แสดงในแถบการแจ้งเตือน\nsec_stats=สถิติ\n" +
        "stats_fmt=รวม %d / รอบนี้ %d / %.1f ครั้ง/วิ\nreset_total=รีเซ็ตยอดรวม\n" +
        "u_ms=ms\nu_times=ครั้ง\nu_sec=วิ\n" +
        "w_launch=เปิด\nw_start=เริ่ม\nw_stop=หยุด\nnotif_title=OpenTapper กำลังทำงาน\nnotif_text=ช่วงเวลา %dms / %d จุด\nchannel=สถานะ OpenTapper\n");

        put("tr",
        "status_on=● Çalışıyor\nstatus_off=○ Bağlı değil\nbig_on=Başlatmak için dokun\nbig_off=Erişilebilirliği etkinleştir\n" +
        "hint_on=Yüzen paneli ve dokunma işaretlerini gösterir\nhint_off=Ayarlar → Erişilebilirlik → OpenTapper’ı aç\n" +
        "stat_total=Toplam dokunma\nstat_interval=Aralık\nstat_points=Nokta\n" +
        "act_a11y=Erişilebilirliği aç\nact_a11y_sub=Sistem ayarları\nact_help=Nasıl kullanılır\nact_help_sub=Panel kontrolleri\n" +
        "act_settings=Ayarlar\nact_settings_sub=Dil ve daha fazlası\nlanguage=Dil\n" +
        "step_run=Başlat / durdur. Çalışırken işaretler yarı saydam olur ve dokunmaları geçirir\n" +
        "step_int=Aralık (ms). Ön ayarlar için sayıya dokun; ince kaydırıcı ⚙ içinde\n" +
        "step_pt=İşaret ekle / kaldır. Dokunulacak yere sürükle (en fazla 10)\n" +
        "step_gear=Basılı tutma süresi, sıralı mod, otomatik durdurma, profiller, istatistik\n" +
        "step_handle=Paneli taşımak için sürükle. ⚡ baloncuğuna küçültmek için dokun\n" +
        "step_widget=Ana ekrana koy: aç → başlat → durdur tek dokunuşla\nwidget=Widget\n" +
        "foot=Açık kaynak · Kaplama izni gerekmez · Veri toplanmaz\n" +
        "need_a11y=Önce erişilebilirlik hizmetini etkinleştirin\nadd_point_first=“●+” ile bir nokta ekleyin\n" +
        "limit_count=Dokunma sınırına ulaşıldı\nlimit_time=Süre sınırına ulaşıldı\nmax_points=En fazla 10 nokta\nmin_points=En az 1 nokta gerekli\n" +
        "settings=Ayarlar\nclose=Kapat\nprofile=Profil\nslot=Yuva %d\nloaded=Yuva %d yüklendi\n" +
        "sec_tap=Dokunma\ninterval_fine=Aralık (ince)\nhold=Basılı tutma\n" +
        "speed_note=Not: 1 ms gibi çok kısa aralıklar cihaz tarafından işlenemez; gerçek hız yaklaşık 20–50 ms’de sınırlanır\n" +
        "round_robin=Sıralı mod (her seferinde bir nokta)\nsec_autostop=Otomatik durdurma\nstop_count=Sayıya göre durdur\ncount=Sayı\nstop_time=Süreye göre durdur\nseconds=Saniye\n" +
        "sec_feedback=Geri bildirim\nvibrate=Titreşim (başlat/durdur)\nnotify=Bildirim çubuğunda göster\nsec_stats=İstatistik\n" +
        "stats_fmt=Toplam %d / Oturum %d / %.1f dokunma/sn\nreset_total=Toplamı sıfırla\n" +
        "u_ms=ms\nu_times=kez\nu_sec=sn\n" +
        "w_launch=Aç\nw_start=Başlat\nw_stop=Durdur\nnotif_title=OpenTapper çalışıyor\nnotif_text=Aralık %dms / %d nokta\nchannel=OpenTapper durumu\n");

        put("ar",
        "status_on=● يعمل\nstatus_off=○ غير متصل\nbig_on=اضغط للتشغيل\nbig_off=تفعيل إمكانية الوصول\n" +
        "hint_on=يعرض اللوحة العائمة وعلامات النقر\nhint_off=الإعدادات ← إمكانية الوصول ← فعّل OpenTapper\n" +
        "stat_total=إجمالي النقرات\nstat_interval=الفاصل\nstat_points=النقاط\n" +
        "act_a11y=فتح إمكانية الوصول\nact_a11y_sub=إعدادات النظام\nact_help=طريقة الاستخدام\nact_help_sub=أزرار اللوحة\n" +
        "act_settings=الإعدادات\nact_settings_sub=اللغة والمزيد\nlanguage=اللغة\n" +
        "step_run=بدء / إيقاف. أثناء التشغيل تصبح العلامات شفافة وتمرر النقرات\n" +
        "step_int=الفاصل (مللي ثانية). اضغط الرقم للإعدادات المسبقة؛ شريط دقيق داخل ⚙\n" +
        "step_pt=إضافة / إزالة علامات. اسحبها إلى موضع النقر (حتى 10)\n" +
        "step_gear=مدة الضغط، الوضع التناوبي، الإيقاف التلقائي، الملفات، الإحصاءات\n" +
        "step_handle=اسحب لتحريك اللوحة. اضغط لطيّها في زر ⚡\n" +
        "step_widget=ضعه على الشاشة الرئيسية: فتح ← بدء ← إيقاف بلمسة واحدة\nwidget=أداة\n" +
        "foot=مفتوح المصدر · بدون إذن العرض فوق التطبيقات · بدون جمع بيانات\n" +
        "need_a11y=فعّل خدمة إمكانية الوصول أولاً\nadd_point_first=أضف نقطة نقر بواسطة “●+”\n" +
        "limit_count=تم بلوغ حد النقرات\nlimit_time=تم بلوغ حد الوقت\nmax_points=10 نقاط كحد أقصى\nmin_points=يلزم نقطة واحدة على الأقل\n" +
        "settings=الإعدادات\nclose=إغلاق\nprofile=الملف\nslot=الخانة %d\nloaded=تم تحميل الخانة %d\n" +
        "sec_tap=النقر\ninterval_fine=الفاصل (دقيق)\nhold=مدة الضغط\n" +
        "speed_note=ملاحظة: الفواصل القصيرة جدًا (مثل 1 مللي ثانية) لا يستطيع الجهاز معالجتها؛ السرعة الفعلية تتوقف عند نحو 20–50 مللي ثانية\n" +
        "round_robin=الوضع التناوبي (نقطة واحدة كل مرة)\nsec_autostop=إيقاف تلقائي\nstop_count=إيقاف بعد عدد\ncount=العدد\nstop_time=إيقاف بعد وقت\nseconds=ثوانٍ\n" +
        "sec_feedback=التنبيه\nvibrate=اهتزاز (بدء/إيقاف)\nnotify=إظهار في شريط الإشعارات\nsec_stats=الإحصاءات\n" +
        "stats_fmt=الإجمالي %d / الجلسة %d / %.1f نقرة/ث\nreset_total=إعادة تعيين الإجمالي\n" +
        "u_ms=ms\nu_times=مرة\nu_sec=ث\n" +
        "w_launch=فتح\nw_start=بدء\nw_stop=إيقاف\nnotif_title=OpenTapper يعمل\nnotif_text=الفاصل %dms / %d نقاط\nchannel=حالة OpenTapper\n");

        put("ja", "notif_stop=強制停止\ndonate=☕ 開発を支援する\nsec_overlay=表示スタイル\nstyle_full=フル\nstyle_compact=コンパクト\nstyle_minimal=ミニマル\nhide_overlay=オーバーレイを閉じる\n");
        put("en", "notif_stop=Force Stop\ndonate=☕ Support development\nsec_overlay=Overlay style\nstyle_full=Full\nstyle_compact=Compact\nstyle_minimal=Minimal\nhide_overlay=Close overlay\n");
        put("zh", "notif_stop=强制停止\ndonate=☕ 支持开发\nsec_overlay=显示样式\nstyle_full=完整\nstyle_compact=紧凑\nstyle_minimal=极简\nhide_overlay=关闭悬浮窗\n");
        put("zh-TW", "notif_stop=強制停止\ndonate=☕ 支持開發\nsec_overlay=顯示樣式\nstyle_full=完整\nstyle_compact=精簡\nstyle_minimal=極簡\nhide_overlay=關閉懸浮視窗\n");
        put("ko", "notif_stop=강제 종료\ndonate=☕ 개발 후원하기\nsec_overlay=표시 스타일\nstyle_full=전체\nstyle_compact=간단히\nstyle_minimal=미니멀\nhide_overlay=오버레이 닫기\n");
        put("es", "notif_stop=Forzar detención\ndonate=☕ Apoyar el desarrollo\nsec_overlay=Estilo del panel\nstyle_full=Completo\nstyle_compact=Compacto\nstyle_minimal=Mínimo\nhide_overlay=Cerrar panel\n");
        put("fr", "notif_stop=Arrêt forcé\ndonate=☕ Soutenir le développement\nsec_overlay=Style du panneau\nstyle_full=Complet\nstyle_compact=Compact\nstyle_minimal=Minimal\nhide_overlay=Fermer le panneau\n");
        put("de", "notif_stop=Erzwingen stoppen\ndonate=☕ Entwicklung unterstützen\nsec_overlay=Panel-Stil\nstyle_full=Voll\nstyle_compact=Kompakt\nstyle_minimal=Minimal\nhide_overlay=Panel schließen\n");
        put("pt", "notif_stop=Forçar parada\ndonate=☕ Apoiar o desenvolvimento\nsec_overlay=Estilo do painel\nstyle_full=Completo\nstyle_compact=Compacto\nstyle_minimal=Mínimo\nhide_overlay=Fechar painel\n");
        put("ru", "notif_stop=Принудительно остановить\ndonate=☕ Поддержать разработку\nsec_overlay=Стиль панели\nstyle_full=Полный\nstyle_compact=Компактный\nstyle_minimal=Минимальный\nhide_overlay=Закрыть панель\n");
        put("it", "notif_stop=Arresto forzato\ndonate=☕ Sostieni lo sviluppo\nsec_overlay=Stile pannello\nstyle_full=Completo\nstyle_compact=Compatto\nstyle_minimal=Minimo\nhide_overlay=Chiudi pannello\n");
        put("hi", "notif_stop=ज़बरदस्ती रोकें\ndonate=☕ विकास का समर्थन करें\nsec_overlay=पैनल शैली\nstyle_full=पूर्ण\nstyle_compact=संक्षिप्त\nstyle_minimal=न्यूनतम\nhide_overlay=पैनल बंद करें\n");
        put("id", "notif_stop=Hentikan paksa\ndonate=☕ Dukung pengembangan\nsec_overlay=Gaya panel\nstyle_full=Penuh\nstyle_compact=Ringkas\nstyle_minimal=Minimal\nhide_overlay=Tutup panel\n");
        put("vi", "notif_stop=Buộc dừng\ndonate=☕ Ủng hộ phát triển\nsec_overlay=Kiểu bảng điều khiển\nstyle_full=Đầy đủ\nstyle_compact=Gọn\nstyle_minimal=Tối giản\nhide_overlay=Đóng bảng điều khiển\n");
        put("th", "notif_stop=บังคับหยุด\ndonate=☕ สนับสนุนการพัฒนา\nsec_overlay=รูปแบบแผงควบคุม\nstyle_full=เต็มรูปแบบ\nstyle_compact=กะทัดรัด\nstyle_minimal=ขั้นต่ำ\nhide_overlay=ปิดแผงควบคุม\n");
        put("tr", "notif_stop=Zorla durdur\ndonate=☕ Geliştirmeyi destekle\nsec_overlay=Panel stili\nstyle_full=Tam\nstyle_compact=Kompakt\nstyle_minimal=Minimal\nhide_overlay=Paneli kapat\n");
        put("ar", "notif_stop=إيقاف قسري\ndonate=☕ ادعم التطوير\nsec_overlay=نمط اللوحة\nstyle_full=كامل\nstyle_compact=مضغوط\nstyle_minimal=بسيط\nhide_overlay=إغلاق اللوحة\n");
    }
}
