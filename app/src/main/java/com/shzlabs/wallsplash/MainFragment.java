package com.shzlabs.wallsplash;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.shzlabs.wallsplash.Remote.WallpaperApi;
import com.shzlabs.wallsplash.adapter.ImageListAdapter;
import com.shzlabs.wallsplash.data.model.OrderSelectedEvent;
import com.shzlabs.wallsplash.data.model.Wallpaper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainFragment extends Fragment implements ImageListAdapter.RecyclerViewClickListener {

    private static final String TAG = MainFragment.class.getSimpleName();
    View rootView;
    Context ctx;
    @BindView(R.id.images_recycler_view)
    RecyclerView imageRecyclerView;
    @BindView(R.id.retryButton)
    Button retryButton;
    ImageListAdapter imageListAdapter;
    public static List<Wallpaper> wallpaperList;
    boolean isLoading = false;
    static int page = 1;

    // Image fetch order types as specified by UnSplash API
    public static final String ORDER_BY_OLDEST = "oldest";
    public static final String ORDER_BY_POPULAR = "popular";
    public static final String ORDER_BY_LATEST = "latest";

    public static String order = ORDER_BY_POPULAR;


    public static MainFragment newInstance() {
        
        Bundle args = new Bundle();
        
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        ctx = getActivity();

        // Set up RecyclerView
        wallpaperList = new ArrayList<>();
        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ctx, 2);
        imageListAdapter = new ImageListAdapter(ctx, this);
        imageRecyclerView.setLayoutManager(mLayoutManager);
        imageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        imageRecyclerView.setAdapter(imageListAdapter);

        // Load images on app run
        downloadImages(order);



        // Load more images onScroll end
        imageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // Check if end of page has been reached
            if( !isLoading && ((LinearLayoutManager)mLayoutManager).findLastVisibleItemPosition() == imageListAdapter.getItemCount()-1 ){
                Log.d(TAG , "End has reached, loading more images!");
                isLoading = true;
                page++;
                downloadImages(order);
            }

            }
        });

        /*imageRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                *//*Toast.makeText(ctx, "" + ((TextView)rv.findChildViewUnder(e.getX(), e.getY()).findViewById(R.id.urlTextView)).getText(), Toast.LENGTH_SHORT).show();
                List<Wallpaper> itemList= ((ImageListAdapter)rv.getAdapter()).getItemList();
                // TODO Start new activity and send this list*//*
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });*/

        return rootView;
    }

    @OnClick(R.id.retryButton)
    public void retryLoading(){
        retryButton.setVisibility(View.GONE);
        imageRecyclerView.setVisibility(View.VISIBLE);
        downloadImages(order);
    }

    /**
     * Fetch and load images via REST API using Retrofit
     *
     * @param orderBy - order of images to be retrieved
     */
    public void downloadImages(String orderBy){

        int itemsPerPage = 20;

        // Set Toolbar subtitle
        try {
            ((MainActivity)getActivity()).getSupportActionBar().setSubtitle(orderBy.substring(0,1).toUpperCase() + orderBy.substring(1));
        }catch (Exception e){
            Log.e(TAG, "downloadImages: Exception " + e );
        }

        // Retrofit connect
        WallpaperApi.Factory.getInstance().getWallpapers(orderBy, itemsPerPage, page).enqueue(new Callback<List<Wallpaper>>() {
            @Override
            public void onResponse(Call<List<Wallpaper>> call, Response<List<Wallpaper>> response) {
                for(Wallpaper wallpaper:response.body()){
                    Log.d(TAG, wallpaper.getUser().getName());
                }
                imageListAdapter.addImages(response.body());
                isLoading = false;
            }
            @Override
            public void onFailure(Call<List<Wallpaper>> call, Throwable t) {
                Log.e(TAG, "Failed " + t.getMessage());
                Toast.makeText(ctx, "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
                isLoading = false;
                // reduce page by 1 as page failed to load
                page--;
                imageRecyclerView.setVisibility(View.GONE);
                retryButton.setVisibility(View.VISIBLE);
            }
        });
    }


    @Subscribe
    public void onOrderSelectedEvent(OrderSelectedEvent event){

        // Clear and reset
        page = 1;
        imageListAdapter.clearList();

        // Set new order type
        order = event.getOrder();

        // Fetch images for selected order type
        downloadImages(order);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        ImageSliderFragment fragment = ImageSliderFragment.newInstance(position);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        fragment.show(ft, "ImageSliderFragment");
    }

    /*public static class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(ctx, "OnClicked!! " + ((TextView)v.findViewById(R.id.urlTextView)).getText() , Toast.LENGTH_SHORT).show();
        }
    }*/
}
