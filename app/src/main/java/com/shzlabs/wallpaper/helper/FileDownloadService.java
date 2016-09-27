package com.shzlabs.wallpaper.helper;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.shzlabs.wallpaper.R;
import com.shzlabs.wallpaper.Remote.WallpaperApi;
import com.shzlabs.wallpaper.data.model.Download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by shaz on 13/8/16.
 */
public class FileDownloadService extends IntentService {

    private static final String TAG = FileDownloadService.class.getSimpleName();
    public static final String INTENT_URL = "file_url";
    public static final String INTENT_FILE_NAME = "file_name";
    public static final String INTENT_FILE_SHARE = "file_share"; // boolean

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private Download download;
    String fileURL;
    String fileName;
    boolean forShare;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FileDownloadService() {
        super("FileDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent: ");

        // Fetch Intent Data
        if(intent.getExtras() != null) {

            Bundle bundle = intent.getExtras();

            fileURL = bundle.getString(INTENT_URL);
            Log.d(TAG, "onHandleIntent: FileURL " + fileURL);

            fileName = bundle.getString(INTENT_FILE_NAME);
            Log.d(TAG, "onHandleIntent: FileName " + fileName);

            forShare = bundle.getBoolean(INTENT_FILE_SHARE);
            Log.d(TAG, "onHandleIntent: ForShare " + forShare);

        }

        // Init notification
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_get_app_black_24dp)
                .setContentTitle(getString(R.string.download_notification_title))
                .setContentText(getString(R.string.download_notification_status_ongoing))
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());

        // Start download
        initDownload();
    }

    private void initDownload() {

        Call<ResponseBody> request = WallpaperApi.Factory.getInstance()
                .downloadImage(fileURL);
        try {

            ResponseBody body = request.execute().body();
            long fileSize = body.contentLength();
            byte data[] = new byte[1024 * 4];
            int count;
            long total = 0;
            double totalFileSize;
            long startTime = System.currentTimeMillis();
            int timeCount = 1;
            download = new Download();

            InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);

            boolean directoryStatus = true;
            File path = getSavePath();

            // Create directory if not exists
            if(!path.exists()){
                directoryStatus = path.mkdir();
                // initiate media scan and put the new things into the path array to
                // make the scanner aware of the location and the files you want to see
                if(directoryStatus)
                    MediaScannerConnection.scanFile(this, new String[] {path.toString()}, null, null);
            }
            if(!directoryStatus){
                onDownloadError("Error creating directory");
                return;
            }

            File file = new File(path , fileName);
            boolean fileStatus = true;
            if(!file.exists()) {

                fileStatus = file.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(file);

                while ((count = bis.read(data)) != -1) {
                    total += count;
                    totalFileSize = (fileSize / (Math.pow(1024, 2)));
                    download.setTotalFileSize(totalFileSize);
                    double current = Math.round(total / (Math.pow(1024, 2)));
                    download.setCurrentFileSize(current);
                    int progress = (int) ((total * 100) / fileSize);
                    download.setDownloadProgress(progress);

                    long currentTime = System.currentTimeMillis() - startTime;

                    if (currentTime > 1000 * timeCount) {
                        sendNotification(download);
                        Log.d(TAG, "initDownload: Progress:- " + progress);
                        timeCount++;
                    }

                    outputStream.write(data, 0, count);

                }

                outputStream.flush();
                outputStream.close();
                bis.close();

                Log.d(TAG, "initDownload: Download successful!");
                onDownloadComplete();
            }

            if(!fileStatus){
                onDownloadError("Error creating file");
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getSavePath() {
        if(forShare) {
            return new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name)
                            + "/" + getResources().getString(R.string.directory_share));
        }else {
            return new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name));
        }
    }

    private void sendNotification(Download download){

        notificationBuilder.setProgress(100, download.getDownloadProgress(), false);
        notificationBuilder.setContentText(String.format("Downloaded (%s / %s) MB", download.getCurrentFileSize(), download.getTotalFileSize()));

        notificationManager.notify(0, notificationBuilder.build());

    }

    private void onDownloadComplete(){

        notificationManager.cancel(0);

        if(!forShare) {

            // File path
            String localFileURI = "file://" + getSavePath().getPath() + "/" + fileName;

            // Create notification with onClick to open image in gallery
            notificationBuilder.setProgress(0, 0, false);
            notificationBuilder.setContentText("File Downloaded");

            Log.d(TAG, "onDownloadComplete: Adding file link to notification " + localFileURI);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(localFileURI));
            intent.setDataAndType(Uri.parse(localFileURI), MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg"));

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder.setContentIntent(contentIntent);

            notificationManager.notify(0, notificationBuilder.build());
        }else {

            // Create share intent
            shareImage(fileName);
        }

    }

    private void onDownloadError(String error){

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText(error);
        notificationManager.notify(0, notificationBuilder.build());

    }

    public void shareImage(String fileName){

        // File path
        String savedFilePath = "file:///" + getSavePath().getPath() + "/" + fileName;

        // Share Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        Log.d(TAG, "shareImage: Share image location: " + savedFilePath);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.image_share_extra_text));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(savedFilePath));

        // Chooser Intent
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share Image");
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chooserIntent);

    }



}
