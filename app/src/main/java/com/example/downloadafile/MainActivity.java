package com.example.downloadafile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequestBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnProgressListener, OnDownloadListener {

    Button  btnStartDownload ;
    ProgressBar pbDownloading ;
    private int downloadId;
    private String url = "https://unsplash.com/photos/BdPPwNzsa6o/download?force=true" ;
    String dirPath ;

    private int PICK_BAR_CODE = 1, PICK_COMPANY = 2, REQUEST_CAMERA = 3, SELECT_FILE = 4, price_flag = 0, flag = 0 ;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStartDownload = findViewById(R.id.btnStartDownload);
        pbDownloading = findViewById(R.id.pbDownloading);
        btnStartDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnStartDownload:{
                flag = SELECT_FILE ;
                CheckPermission();
                break;
            }
        }
    }

    @Override
    public void onProgress(Progress progress) {
        long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
        Log.d("progressPercent", "onProgress: "+progressPercent);
    }

    @Override
    public void onDownloadComplete() {
        Log.d("onDownloadComplete", "yes");
        pbDownloading.setVisibility(View.GONE);
    }

    @Override
    public void onError(Error error) {
        Log.d("onErrorConnection", String.valueOf(error.isConnectionError()));
        Log.d("onServerError", String.valueOf(error.isServerError()));
    }

    private void CheckPermission() {
        final int hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        final int hasStoragePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionNeeded = new ArrayList<>();
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionNeeded.isEmpty()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessageOKCancel(getResources().getString(R.string.alert_message_image), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ActivityCompat.requestPermissions(MainActivity.this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), REQUEST_CODE_ASK_PERMISSIONS);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_suggestion), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
        else {
            if(flag == SELECT_FILE){
                requestToDownloadFile();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, getResources().getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                        //openBottomSheet();
                        if(flag == SELECT_FILE){
                            requestToDownloadFile();
                        }
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.toast_suggestion), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void requestToDownloadFile(){
        createFolder();

        downloadId = PRDownloader.download(url, dirPath, "myImage.jpg")
                .build()
                .setOnProgressListener(this)
                .start(this) ;

        pbDownloading.setVisibility(View.VISIBLE);
    }

    private void createFolder() {
        if (Environment.getExternalStorageState() == null) {
            //create new file directory object
            File directory = new File(Environment.getDataDirectory()
                    + "/myTestDownload/");
//            File photoDirectory = new File(Environment.getDataDirectory()
//                    + "/Robotium-Screenshots/");
//            /*
//             * this checks to see if there are any previous test photo files
//             * if there are any photos, they are deleted for the sake of
//             * memory
//             */
//            if (photoDirectory.exists()) {
//                File[] dirFiles = photoDirectory.listFiles();
//                if (dirFiles.length != 0) {
//                    for (int ii = 0; ii <= dirFiles.length; ii++) {
//                        dirFiles[ii].delete();
//                    }
//                }
//            }
            // if no directory exists, create new directory
            if (!directory.exists()) {
                directory.mkdir();
            }
            Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();
            dirPath = directory.getAbsolutePath() ;

        }
        else if (Environment.getExternalStorageState() != null) {
            // search for directory on SD card
            File directory = new File(Environment.getExternalStorageDirectory()
                    + "/myTestDownload/");
//            File photoDirectory = new File(
//                    Environment.getExternalStorageDirectory()
//                            + "/Robotium-Screenshots/");
//            if (photoDirectory.exists()) {
//                File[] dirFiles = photoDirectory.listFiles();
//                if (dirFiles.length > 0) {
//                    for (int ii = 0; ii < dirFiles.length; ii++) {
//                        dirFiles[ii].delete();
//                    }
//                    dirFiles = null;
//                }
//            }
            // if no directory exists, create new directory to store test
            // results
            if (!directory.exists()) {
                directory.mkdir();
            }
            Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();
            dirPath = directory.getAbsolutePath() ;
        }// end of SD card checking
    }
}
