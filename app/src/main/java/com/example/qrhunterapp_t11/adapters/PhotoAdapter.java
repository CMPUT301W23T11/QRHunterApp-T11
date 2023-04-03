package com.example.qrhunterapp_t11.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter for the photos stored in the QRCodeView dialogue viewPager
 *
 * @author Sarah Thomson
 * @sources <pre>
 * <ul>
 * <li><a href="https://www.youtube.com/watch?v=tM7rwJoK-18">by Coding in Flow</a></li>
 * <li><a href="https://www.youtube.com/watch?v=Rfp4GZmZjUU">by EDMT Dev</a></li>
 * </pre>
 */
public class PhotoAdapter extends PagerAdapter {
    private final Context context;
    private final ArrayList<String> photos;

    /**
     * Constructor
     *
     * @param context
     * @param photos
     */
    public PhotoAdapter(@NonNull Context context, @NonNull ArrayList<String> photos) {
        this.context = context;
        this.photos = photos;
    }

    /**
     * Gets the count
     *
     * @return boolean view == object
     */
    @Override
    public int getCount() {
        return photos.size();
    }

    /**
     * Checks if View is from object
     *
     * @return boolean view == object
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * Instantiates an item
     *
     * @param container The containing View in which the page will be shown.
     * @param position  The page position to be instantiated.
     * @return imageView
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.with(context).load(photos.get(position)).into(imageView);

        container.addView(imageView);
        return imageView;
    }

    /**
     * Destroys an item
     *
     * @param container The containing View from which the page will be removed.
     * @param position  The page position to be removed.
     * @param object    The same object that was returned by
     *                  {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }

}
