package com.opentapper.tap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/** 透明アクティビティ：ウィジェット/ショートカットからのワンタッチ操作 */
public class QuickActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!TapService.isConnected()) {
            L.init(this);
            Toast.makeText(this, L.s("need_a11y"), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
        } else if (!TapService.isOverlayShown()) {
            TapService.requestShowOverlay(this);
        } else {
            TapService.requestToggle();
        }
        finish();
    }
}
