package com.shzlabs.wallsplash;


import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shzlabs.wallsplash.Remote.WallpaperApi;
import com.shzlabs.wallsplash.data.model.Wallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ImageSliderFragment extends DialogFragment implements Toolbar.OnMenuItemClickListener {

    private static final String TAG = ImageSliderFragment.class.getSimpleName();
    Context ctx;
    View rootView;
    @BindView(R.id.sliderViewPager)
    ViewPager imageViewPager;
    SliderViewPagerAdapter mPagerAdapter;
    int currentPosition;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    boolean toolbarShown = true;

    public ImageSliderFragment() {
        // Required empty public constructor
    }

    public static ImageSliderFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt("position", position);

        ImageSliderFragment fragment = new ImageSliderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_image_slider, container, false);
        ButterKnife.bind(this, rootView);
        ctx = getActivity();

        // Fetch data
//        wallpaperList = (ArrayList<Wallpaper>) getArguments().getSerializable("wallpaperList");
        currentPosition = getArguments().getInt("position");

        // Setup Toolbar
//        toolbar.setTitle("Title");
        toolbar.inflateMenu(R.menu.menu_image_slider);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        toolbar.setOnMenuItemClickListener(this);

        Log.d(TAG, "onCreateView: wallpaperlist size: " + MainFragment.wallpaperList.size());
        Log.d(TAG, "onCreateView: currentPosition: " + currentPosition);

        // Set up viewpager
        mPagerAdapter = new SliderViewPagerAdapter();
        imageViewPager.setAdapter(mPagerAdapter);
        imageViewPager.setCurrentItem(currentPosition);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.info :{
                Toast.makeText(getActivity(), "Info!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.download :{
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Select image size to download:");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1);
                adapter.add("Small");
                adapter.add("Regular");
                adapter.add("Full (Recommended)");
                adapter.add("Raw");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Wallpaper wallpaper = MainFragment.wallpaperList.get(imageViewPager.getCurrentItem());
                        String imageUri = "";
                        String fileName = "";
                        switch (which){
                            case 0:{
                                imageUri = wallpaper.getUrls().getSmall();
                                fileName = "photo-" + wallpaper.getId() + "-" + wallpaper.getUser().getId() + "-small.jpg";
                                break;
                            }
                            case 1:{
                                imageUri = wallpaper.getUrls().getRegular();
                                fileName = "photo-" + wallpaper.getId() + "-" + wallpaper.getUser().getId() + "-regular.jpg";
                                break;
                            }
                            case 2:{
                                imageUri = wallpaper.getUrls().getFull();
                                fileName = "photo-" + wallpaper.getId() + "-" + wallpaper.getUser().getId() + "-full.jpg";
                                break;
                            }
                            case 3:{
                                imageUri = wallpaper.getUrls().getRaw();
                                fileName = "photo-" + wallpaper.getId() + "-" + wallpaper.getUser().getId() + "-raw.jpg";
                                break;
                            }
                        }


                        // Set up External Storage Write for Marshmellow and above
                        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1){
                            if(!(getActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED)){
                                Log.d(TAG, "onClick: Asking for permissions...");
                                String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                                requestPermissions(perms, 200);

                            }
                        }

                        downloadImageToLocal(fileName, imageUri);


                    }
                });
                builder.show();
                break;
            }
            case R.id.share :{
                Wallpaper wallpaper = MainFragment.wallpaperList.get(imageViewPager.getCurrentItem());
                String imageUri = wallpaper.getUrls().getSmall();
                String fileName = "photo-" + wallpaper.getId() + "-" + wallpaper.getUser().getId() + "-small.jpg";

                // Set up External Storage Write for Marshmellow and above
                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1){
                    if(!(getActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED)){
                        Log.d(TAG, "onClick: Asking for permissions...");
                        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                        requestPermissions(perms, 200);

                    }
                }

                downloadImageToLocal(fileName, imageUri);

                break;
            }

        }
        return false;
    }

    public void downloadImageToLocal(String fileName, String imageUri){

        // Download image
        final String finalFileName = fileName.toLowerCase();
        WallpaperApi.Factory.getInstance()
                .downloadImage(imageUri)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        boolean directoryStatus = true;
                        File path = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name));
                        // Create directory if not exists
                        if(!path.exists()){
                            directoryStatus = path.mkdir();
                            // initiate media scan and put the new things into the path array to
                            // make the scanner aware of the location and the files you want to see
                            if(directoryStatus)
                                MediaScannerConnection.scanFile(ctx, new String[] {path.toString()}, null, null);
                        }
                        if(!directoryStatus){
                            Toast.makeText(ctx, "Error creating directory.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        File file = new File(path , finalFileName);
                        boolean fileStatus = true;
                        if(!file.exists())
                            try {
                                fileStatus = file.createNewFile();
                                FileOutputStream fop = new FileOutputStream(file);
                                fop.write(response.body().bytes());
                                fop.flush();
                                fop.close();

                                Toast.makeText(ctx, "Download successful!", Toast.LENGTH_SHORT).show();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        if(!fileStatus){
                            Toast.makeText(ctx, "Error creating file.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(ctx, "Error fetching image.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 200:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(ctx, "Permission Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ctx, "Permission Denied by user. Please grant access to continue.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class SliderViewPagerAdapter extends PagerAdapter {

        LayoutInflater layoutInflater;

        public SliderViewPagerAdapter(){

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_fullscreen_preview, container, false);

            final ImageView previewImageView = (ImageView) view.findViewById(R.id.image_preview);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

            Wallpaper wallpaper = MainFragment.wallpaperList.get(position);

            Log.d(TAG, "instantiateItem: WallpaperURL: " + wallpaper.getUrls().getFull());

            Glide.with(getActivity()).load(wallpaper.getUrls().getRegular())
                    .thumbnail(0.5f)
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.d(TAG, "onResourceReady: Loaded!");
                            progressBar.setVisibility(View.GONE);
                            previewImageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(previewImageView);
            container.addView(view);

            // Show/Hide actionbar on click anywhere on screen
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHideToolbar();
                }
            });

            return view;
        }

        @Override
        public int getCount() {
            return MainFragment.wallpaperList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View)object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }

    private void showHideToolbar(){

        if(toolbarShown){
            // Hide toolbar
            toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator())
                    .start();
        }else{
            // Show toolbar
            toolbar.setVisibility(View.VISIBLE);
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();

        }

        // Animation listener
        toolbar.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if(toolbarShown){
                    toolbar.setVisibility(View.GONE);
                    toolbarShown = false;
                }else{
                    toolbarShown = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

}
