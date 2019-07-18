package com.example.ryandu.downloaddemo.updateutil;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 泛型说明：String 表示在执行AsyncTask的时候需要传入String参数给后台任务 此处为下载地址
 * Integer 表示使用整型作为进度显示单位
 * Integer 表示使用整型作为反馈执行结果
 */

public class DownloadToUpdateTask extends AsyncTask<String, Integer, Integer> {

    public static final int TYPE_SUCCESS = 0;//下载成功
    public static final int TYPE_FAILED = 1;//下载失败
    public static final int TYPE_PAUSED = 2;//下载暂停
    public static final int TYPE_CANCELED = 3;//下载取消

    private DownloadToUpdateListener listener;//下载状态回调监听

    private boolean isCanceled = false;

    private boolean isPaused = false;

    private int lastProgress;

    private String apkUrl;

    public DownloadToUpdateTask(DownloadToUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * 执行具体下载逻辑
     *
     * @param strings
     * @return 下载状态
     */
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0; //记录已下载文件的长度
            String downloadUrl = strings[0];//获取下载地址
//            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
//            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

            String fileName = System.currentTimeMillis() + "下载测试.apk";
            String directory = "/sdcard/123321/";

            file = new File(directory);
            if (!file.exists()) {
                file.mkdir();
            }

            file = new File(directory + fileName);//将文件指定下载到SD卡的Download目录下
            if (file.exists()) {//判断是否已存在文件，是：取出文件大小（字节数）
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);//获取待下载文件大小（字节数）
            if (contentLength == 0) {//长度为0，文件异常，下载失败
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {//文件长度等于已下载长度，已下载完，直接返回成功
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")//断点下载，指定从哪个字节开始下载
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength);//跳过已下载字节
                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(bytes)) != -1) {
                    if (isCanceled) {//判断是否有取消操作
                        return TYPE_CANCELED;
                    } else if (isPaused) {//判断是否有暂停操作
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        savedFile.write(bytes, 0, len);
                        //计算已下载百分比
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                apkUrl = directory + fileName;
                if ((total + downloadedLength) == contentLength)
                    return TYPE_SUCCESS;

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    /**
     * 更新下载进度
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    /**
     * 通知下载结果
     *
     * @param type
     */
    @Override
    protected void onPostExecute(Integer type) {
        switch (type) {
            case TYPE_SUCCESS:
                listener.onSuccess(apkUrl);
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }

    public void cancelDownload() {
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

}
