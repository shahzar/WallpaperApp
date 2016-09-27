package com.shzlabs.wallpaper.data.model;

import java.text.DecimalFormat;

/**
 * Created by shaz on 14/8/16.
 */
public class Download {

    int downloadProgress;
    Double currentFileSize;
    Double totalFileSize;
    DecimalFormat df;

    public Download() {
        df = new DecimalFormat("0.0");
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public String getCurrentFileSize() {
        return df.format(currentFileSize);
    }

    public void setCurrentFileSize(Double currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public String getTotalFileSize() {
        return df.format(totalFileSize);
    }

    public void setTotalFileSize(Double totalFileSize) {
        this.totalFileSize = totalFileSize;
    }
}
