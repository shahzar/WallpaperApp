package com.shzlabs.wallsplash;


import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shzlabs.wallsplash.data.model.Wallpaper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ImageSliderFragment extends DialogFragment {

    private static final String TAG = ImageSliderFragment.class.getSimpleName();
    Context ctx;
    View rootView;
    @BindView(R.id.sliderViewPager)
    ViewPager imageViewPager;
    SliderViewPagerAdapter mPagerAdapter;
    List<Wallpaper> wallpaperList;
    int currentPosition;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mainLinearLayout)
    LinearLayout mainLayout;
    boolean toolbarShown = true;

    public ImageSliderFragment() {
        // Required empty public constructor
    }

    public static ImageSliderFragment newInstance(ArrayList<Wallpaper> wallpaperList, int position) {

        Bundle args = new Bundle();
        args.putSerializable("wallpaperList", wallpaperList);
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
        wallpaperList = (ArrayList<Wallpaper>) getArguments().getSerializable("wallpaperList");
        currentPosition = getArguments().getInt("position");

        // Setup Toolbar
        toolbar.setTitle("Title");
        toolbar.inflateMenu(R.menu.menu_image_slider);
//        ((MainActivity)getActivity()).setSupportActionBar(toolbar);

        Log.d(TAG, "onCreateView: wallpaperlist size: " + wallpaperList.size());
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

    public class SliderViewPagerAdapter extends PagerAdapter {

        LayoutInflater layoutInflater;

        public SliderViewPagerAdapter(){

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_fullscreen_preview, container, false);

            ImageView previewImageView = (ImageView) view.findViewById(R.id.image_preview);
            
            Wallpaper wallpaper = wallpaperList.get(position);

            Log.d(TAG, "instantiateItem: WallpaperURL: " + wallpaper.getUrls().getFull());

            Glide.with(getActivity()).load(wallpaper.getUrls().getRegular())
                    .thumbnail(0.5f)
                    .crossFade()
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
            return wallpaperList.size();
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
