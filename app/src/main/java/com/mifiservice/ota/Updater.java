package com.mifiservice.ota;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import com.mifiservice.device.DeviceController;
import com.mifiservice.device.WanDataController;
import com.mifiservice.service.MifiService;
import com.mifiservice.utils.NetUtil;
import com.mifiservice.utils.TextUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class Updater {
    private static final boolean DEBUG = false;
    private static final String TAG = "Updater";
    private static Updater instance;
    private long downloadId;
    private DownloadManager downloadManager;
    private String downloadMd5;
    private Context mContext;
    private PowerManager powerManager;
    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() { // from class: com.mifiservice.ota.Updater.1
        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Log.e(Updater.TAG, "doCheckUpdate ");
            Updater.this.doCheckUpdate();
        }
    };
    private BroadcastReceiver updaterReceiver = new BroadcastReceiver() { // from class: com.mifiservice.ota.Updater.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.DOWNLOAD_COMPLETE".equals(intent.getAction())) {
                long id = intent.getLongExtra("extra_download_id", -2L);
                if (Updater.this.downloadId == id) {
                    Updater.this.deleteFile("/data/update.zip");
                    Updater.this.copyFile("/sdcard/data/update.zip", "/data/update.zip");
                    Updater.this.doUpdate();
                }
            }
        }
    };
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");

    public static Updater getInstance() {
        if (instance == null) {
            instance = new Updater(MifiService.getContext());
        }
        return instance;
    }

    public Updater(Context context) {
        this.mContext = null;
        this.mContext = context;
        this.downloadManager = (DownloadManager) this.mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        this.powerManager = (PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
        this.mContext.registerReceiver(this.updaterReceiver, filter);
        this.timer.schedule(this.timerTask, 120000L, 1800000L);
    }

    public void doUpdate() {
        File file = new File("/data/update.zip");
        if (file.exists()) {
            String md5 = TextUtil.calcMd5(file);
            if (this.downloadMd5.equals(md5)) {
                try {
                    recoveryMode(this.powerManager);
                } catch (IOException e) {
                    Log.d(TAG, "err1");
                }
            }
        }
    }

    public void doUpdateNoMd5() {
        File file = new File("/data/update.zip");
        if (file.exists()) {
            try {
                recoveryMode(this.powerManager);
            } catch (IOException e) {
                Log.d(TAG, "err1");
            }
        }
    }

    public void copyFile(String oldPath, String newPath) {
        int bytesum = 0;
        try {
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while (true) {
                    int byteread = inStream.read(buffer);
                    if (byteread != -1) {
                        bytesum += byteread;
                        fs.write(buffer, 0, byteread);
                    } else {
                        inStream.close();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "err2");
        }
    }

    public void deleteFile(String filePath) {
        new File(filePath).delete();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downloadApp(String md5) {
        Log.d(TAG, "begin download update.zip :" + md5);
        String url = "http://120.77.62.229:8080/file/doDownload?md5=" + md5 + "&imei=" + WanDataController.getInstance().getIMEI();
        DownloadManager.Request down = new DownloadManager.Request(Uri.parse(url));
        down.setAllowedNetworkTypes(3);
        deleteFile("/sdcard/data/update.zip");
        down.setVisibleInDownloadsUi(false);
        down.setDestinationInExternalPublicDir("/data", "update.zip");
        down.setNotificationVisibility(2);
        this.downloadMd5 = md5;
        this.downloadId = this.downloadManager.enqueue(down);
    }

    /* loaded from: classes.dex */
    public class CheckUpdateTask extends AsyncTask<String, Integer, String> {
        public CheckUpdateTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String doInBackground(String... params) {
            try {
                String url = "http://120.77.62.229:8080/file/checkUpdate?&device=" + DeviceController.getInstance().getDeviceName() + "&swv=" + DeviceController.getInstance().getSWVersion() + "&imei=" + WanDataController.getInstance().getIMEI();
                return NetUtil.httpGet(url);
            } catch (Exception e) {
                Log.e(Updater.TAG, "err3");
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String result) {
            if (TextUtil.isEmpty(result)) {
                Log.e(Updater.TAG, "no available update");
            } else if (DeviceController.getInstance().isCharging() || DeviceController.getInstance().getBatteryCapacity() >= 30) {
                Updater.this.downloadApp(result);
            }
        }
    }

    public int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = {-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = this.downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow("bytes_so_far"));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow("total_size"));
                bytesAndStatus[2] = c.getInt(c.getColumnIndexOrThrow("status"));
            }
            return bytesAndStatus;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public void doCheckUpdate() {
        CheckUpdateTask task = new CheckUpdateTask();
        task.execute(new String[0]);
    }

    private void recoveryMode(PowerManager pm) throws IOException {
        RECOVERY_DIR.mkdirs();
        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write("--update_package=/data/update.zip");
            command.write("\n");
            command.close();
            pm.reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }
}