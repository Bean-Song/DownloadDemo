package com.example.ryandu.downloaddemo.updateutil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.ryandu.downloaddemo.R;

import java.io.File;

public class DownloadToUpdateService extends Service {

    public static final String ACTION_PAUSE = "Pause";
    public static final String ACTION_SUCCESS = "Success";
    public static final String UPDATE_URL = "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk";

    private DownloadToUpdateTask downloadTask;

    private String downloadUrl;
    private String apkUrl;

    /**
     * 下载的监听实例，
     */
    private DownloadToUpdateListener downloadListener = new DownloadToUpdateListener() {
        private int progress;
        @Override
        public void onProgress(int progress) {
            //构建显示下载进度的通知，并触发通知
            getNotificationManager().notify(1, getNotification("下载中,点击取消 ...",progress));
            this.progress = progress;
        }

        @Override
        public void onSuccess(String apkUrl) {
            downloadTask = null;
            //下载成功将前台服务关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载完成,点击安装",-1));
            Toast.makeText(DownloadToUpdateService.this,"下载完成，加载中 ...",Toast.LENGTH_SHORT).show();
            DownloadToUpdateService.this.apkUrl = apkUrl;
            installApk();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //下载失败将前台服务关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败",-1));
            Toast.makeText(DownloadToUpdateService.this,"下载失败",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPaused() {
            downloadTask = null;
            getNotificationManager().notify(1, getNotification("已暂停，点击取消", progress));
            Toast.makeText(DownloadToUpdateService.this,"暂停下载",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadToUpdateService.this,"取消下载",Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 安装已经下载好的APK
     */
    public void installApk() {
        File apkFile = new File(apkUrl);
        if (!apkFile.exists()) {
            stopForeground(true);
            Toast.makeText(DownloadToUpdateService.this,"更新文件未下载或已经删除",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(i);
    }

    public DownloadToUpdateService() {
    }

    public DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 在service创建时注册一个广播，用来获取通知栏点击事件
     * 点击事件在下载的时候点击后可以取消下载
     * 在下载完成之后点击可以安装已经下载好的APK
     */
    private DownloadToUpdateReceiver downLoadReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        downLoadReceiver = new DownloadToUpdateReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_SUCCESS);
        registerReceiver(downLoadReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        // service 停掉时同时把已经注册的广播取消掉
        if (downLoadReceiver != null)
            unregisterReceiver(downLoadReceiver);
        super.onDestroy();
    }


    /**
     * Binder 用来绑定 Activity 和 service，让他们之间可以交互
     */
    class DownloadBinder extends Binder {
        public void startDownload(){
            if (downloadTask == null){
                downloadUrl = UPDATE_URL;
                downloadTask = new DownloadToUpdateTask(downloadListener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("准备下载中 ... ",0));
                Toast.makeText(DownloadToUpdateService.this,"开始下载 ... ",Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload(){
            if (downloadTask != null){
                downloadTask.pauseDownload();
            }else {
                if (downloadUrl != null){
                    //取消下载是删除文件，关闭通知
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadToUpdateService.this,"取消更新",Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void cancelDownload(){
            if (downloadTask != null){
                downloadTask.cancelDownload();
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {

        Intent downIntent = new Intent(ACTION_PAUSE);
        if (title.equals("下载完成,点击安装"))
            downIntent = new Intent(ACTION_SUCCESS);
        PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), 100, downIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0) {
            //当progress大于或等0时才需要显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

}
