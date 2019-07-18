package com.example.ryandu.downloaddemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ryandu.downloaddemo.updateutil.DownloadToUpdateUtil;

public class MainActivity extends AppCompatActivity {
    private DownloadToUpdateUtil updateUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建更新对象
        updateUtil = new DownloadToUpdateUtil(this);
        //设置服务器版本号，判断是否需要更新
        updateUtil.checkVersion("1.1");

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    //开始下载
    public void startDownload(View view) {
        if (updateUtil != null)
            updateUtil.startDownload();
    }

    //暂停下载
    public void pauseDownload(View view) {
        if (updateUtil != null)
            updateUtil.pauseDownload();
    }

    //取消下载
    public void cancelDownload(View view) {
        if (updateUtil != null)
            updateUtil.cancelDownload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消更新服务进程
        if (updateUtil != null)
            updateUtil.close();
    }
}
