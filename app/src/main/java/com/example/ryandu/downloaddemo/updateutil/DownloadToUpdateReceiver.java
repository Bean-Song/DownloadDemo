package com.example.ryandu.downloaddemo.updateutil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadToUpdateReceiver extends BroadcastReceiver {

    //更新服务的实例对象
    private DownloadToUpdateService downloadToUpdateService;

    public DownloadToUpdateReceiver(DownloadToUpdateService service) {
        this.downloadToUpdateService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("action = " + action);

        if (action.equals(DownloadToUpdateService.ACTION_PAUSE)) { //取消更新
            downloadToUpdateService.mBinder.cancelDownload();
        } else if (action.equals(DownloadToUpdateService.ACTION_SUCCESS)) { //安装已经下载完成的更新文件
            downloadToUpdateService.installApk();
        }

    }

}
