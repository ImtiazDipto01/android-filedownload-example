package com.example.downloadafile;

import android.app.Application;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;

public class DownloadApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(60000)
                .setConnectTimeout(60000)
                .build();
        PRDownloader.initialize(this, config);
    }
}
