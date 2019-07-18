package com.example.ryandu.downloaddemo.updateutil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * @author Bean
 * @description: 应用更新工具，通过传入的版本号和当前应用的版本号做对比，实现更新
 * @date :2019.7.17 下午 02:23
 */
public class DownloadToUpdateUtil {
    private Context context;
    private DownloadToUpdateService.DownloadBinder downloadBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadToUpdateService.DownloadBinder) iBinder;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    public DownloadToUpdateUtil(Context context) {
        this.context = context;
        initDownload();
    }

    private void initDownload() {
        Intent intent = new Intent(context, DownloadToUpdateService.class);
        context.startService(intent);//启动服务
        context.bindService(intent,serviceConnection,BIND_AUTO_CREATE);//绑定服务
    }

    public void startDownload() {
        if (downloadBinder == null){
            return;
        }
        downloadBinder.startDownload();
    }

    public void pauseDownload() {
        if (downloadBinder == null){
            return;
        }
        downloadBinder.pauseDownload();

    }

    public void cancelDownload() {
        if (downloadBinder == null){
            return;
        }
        downloadBinder.cancelDownload();

    }

    public void checkVersion(String version){
        if (APKVersionCodeUtils.compareVersion(version, context)) {
            showNoticeDialog();
        }
    }

    //提示语
    private String updateMsg = "有最新的版本了哦，亲快下载吧~";
    private Dialog noticeDialog;
    private void showNoticeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("马上下载", (dialog, which) -> {
            dialog.dismiss();
            startDownload();
        });
        builder.setNegativeButton("以后再说", (dialog, which) -> dialog.dismiss());
        noticeDialog = builder.create();
        noticeDialog.setCanceledOnTouchOutside(false);
        noticeDialog.show();
    }



    public void close() {
        context.unbindService(serviceConnection);
    }
}
