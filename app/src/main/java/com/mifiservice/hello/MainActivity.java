package com.mifiservice.hello;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.mifiservice.service.MifiService;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    MenuItem mStartMenuItem;
    MenuItem mStopMenuItem;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.mStartMenuItem = menu.findItem(R.id.action_start);
        this.mStopMenuItem = menu.findItem(R.id.action_stop);
        setStartMenuEnabled(true);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_start) {
            Intent intent = new Intent(this, (Class<?>) MifiService.class);
            startService(intent);
            setStartMenuEnabled(false);
            return true;
        }
        if (item.getItemId() == R.id.action_stop) {
            Intent intent2 = new Intent(this, (Class<?>) MifiService.class);
            stopService(intent2);
            setStartMenuEnabled(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setStartMenuEnabled(boolean startMenuEnabled) {
        this.mStartMenuItem.setEnabled(startMenuEnabled);
        this.mStopMenuItem.setEnabled(!startMenuEnabled);
    }
}