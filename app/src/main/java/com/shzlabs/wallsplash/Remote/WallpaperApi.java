package com.shzlabs.wallsplash.Remote;

import android.net.Uri;

import com.shzlabs.wallsplash.data.model.Wallpaper;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by shaz on 21/5/16.
 */
public interface WallpaperApi {

    String BASE_URL = "https://api.unsplash.com/";
    String API_KEY = "0ae0ce713ecf0364310007dd12a371ffc5f043f1538a14d63ff166139e41d8fe";


    @GET("photos/?client_id=" + API_KEY)
    Call<List<Wallpaper>> getWallpapers(@Query("order_by") String orderBy,
                                        @Query("per_page") int perPage,
                                        @Query("page") int page);

    @GET
    Call<ResponseBody> downloadImage(@Url String fileURL);

    class Factory {
        private static WallpaperApi service;
        public static WallpaperApi getInstance() {
            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL)
                        .build();
                service = retrofit.create(WallpaperApi.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
