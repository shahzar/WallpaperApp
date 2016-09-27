
package com.shzlabs.wallpaper.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Links__ {

    @SerializedName("self")
    @Expose
    private String self;
    @SerializedName("html")
    @Expose
    private String html;
    @SerializedName("download")
    @Expose
    private String download;

    /**
     * 
     * @return
     *     The self
     */
    public String getSelf() {
        return self;
    }

    /**
     * 
     * @param self
     *     The self
     */
    public void setSelf(String self) {
        this.self = self;
    }

    /**
     * 
     * @return
     *     The html
     */
    public String getHtml() {
        return html;
    }

    /**
     * 
     * @param html
     *     The html
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * 
     * @return
     *     The download
     */
    public String getDownload() {
        return download;
    }

    /**
     * 
     * @param download
     *     The download
     */
    public void setDownload(String download) {
        this.download = download;
    }

}
