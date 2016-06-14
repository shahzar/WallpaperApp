package com.shzlabs.wallsplash.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    List<Wallpaper> wallpaperList;
    private static RecyclerViewClickListener itemListener;

    public interface RecyclerViewClickListener
    {
        public void recyclerViewListClicked(View v, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView thumbnail;
        public TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            thumbnail = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            textView = (TextView) itemView.findViewById(R.id.urlTextView);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(ctx, "onClick works!", Toast.LENGTH_SHORT).show();
            Log.d("ImageListAdapter", "onClick works! Position: " + this.getLayoutPosition() + " clicked!");
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }

    public ImageListAdapter(Context context, List<Wallpaper> wallpaperList, RecyclerViewClickListener itemListener) {
        ctx = context;
        this.wallpaperList = wallpaperList;
        this.itemListener = itemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(ctx).inflate(R.layout.image_list_item, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Wallpaper wallpaper = wallpaperList.get(position);
        holder.textView.setText(wallpaper.getUrls().getSmall());
        Glide.with(ctx).load(wallpaper.getUrls().getSmall())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.thumbnail);
    }


    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public void addImages(List<Wallpaper> list){
        wallpaperList.addAll(list);
        notifyDataSetChanged();
    }

    public void clearList(){
        wallpaperList.clear();
    }

    public List<Wallpaper> getItemList(){
        return wallpaperList;
    }

}
