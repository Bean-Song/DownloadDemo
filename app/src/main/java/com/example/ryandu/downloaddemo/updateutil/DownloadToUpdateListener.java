package com.example.ryandu.downloaddemo.updateutil;


public interface DownloadToUpdateListener {
    //当前下载进度
    void onProgress(int progress);

    //下载成功
    void onSuccess(String apkUrl);

    //下载失败
    void onFailed();

    //下载暂停
    void onPaused();

    //下载取消
    void onCanceled();
}
