package com.example.qrhunterapp_t11;

import android.content.Context;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotoAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> photos;

    PhotoAdapter(Context context, ArrayList<String> photos){
        this.context = context;
        this.photos = photos;
    }
    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView= new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(context).load(photos.get(position)).into(imageView);
        System.out.println("HELLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        System.out.println(photos.get(position));

        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }


}
