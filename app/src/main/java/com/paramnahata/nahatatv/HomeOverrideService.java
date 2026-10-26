package com.paramnahata.nahatatv;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/** User-approved firmware fallback. Observes only stock launcher window events, never text. */
public class HomeOverrideService extends AccessibilityService {
    private long lastLaunch;
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            !"com.google.android.tvlauncher".contentEquals(event.getPackageName()==null?"":event.getPackageName()))return;
        if(getSharedPreferences("home",MODE_PRIVATE).getBoolean("originalMode",false))return;
        long now=android.os.SystemClock.elapsedRealtime();if(now-lastLaunch<1000)return;lastLaunch=now;
        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    @Override public void onInterrupt() {}
}
