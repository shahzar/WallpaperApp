package com.shzlabs.wallsplash.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shzlabs.wallsplash.MainFragment;
import com.shzlabs.wallsplash.R;
import com.shzlabs.wallsplash.data.model.Wallpaper;

import java.util.List;

/**
 * Created by shaz on 22/5/16.
 */
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.MyViewHolder> {

    private static final String TAG = ImageListAdapter.class.getSimpleName();
    Context ctx;
    private static RecyclerViewClickListener itemListener;

    public interface RecyclerViewClickListener
    {
        public void recyclerViewListClicked(View v, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView thumbnail;
        public TextView textView;
        public ProgressBar progressBar;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            thumbnail = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            textView = (TextView) itemView.findViewById(R.id.urlTextView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(ctx, "onClick works!", Toast.LENGTH_SHORT).show();
            Log.d("ImageListAdapter", "onClick works! Position: " + this.getLayoutPosition() + " clicked!");
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }

    public ImageListAdapter(Context context, RecyclerViewClickListener itemListener) {
        ctx = context;
        this.itemListener = itemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(ctx).inflate(R.layout.image_list_item, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Wallpaper wallpaper = MainFragment.wallpaperList.get(position);
        String imageUrl = "";
        switch (PreferenceManager.getDefaultSharedPreferences(ctx).getString("thumbnail_quality", "1")){
            case "0": {
                imageUrl = wallpaper.getUrls().getThumb();
                break;
            }
            case "1": {
                imageUrl = wallpaper.getUrls().getSmall();
                break;
            }

        }
        holder.textView.setText(imageUrl);
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(ctx).load(imageUrl)
                .thumbnail(0.5f)
                .crossFade()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.thumbnail.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.thumbnail);
    }


    @Override
    public int getItemCount() {
        return MainFragment.wallpaperList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void addImages(List<Wallpaper> list){
        MainFragment.wallpaperList.addAll(list);
        notifyDataSetChanged();
    }

    public void clearList(){
        MainFragment.wallpaperList.clear();
    }

    public List<Wallpaper> getItemList(){
        return MainFragment.wallpaperList;
    }

}
