package jnuneslab.com.cinemovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.support.v4.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * GridAdapter used to load the imagesViews into the gridView
 * Created by Walter on 14/09/2015.
 */
public class GridAdapter extends CursorAdapter {

    // Height of the image poster in the imageView
    private int mHeight;

    // Width of the image poster in the imageView
    private int mWidth;

    // Array containing all the movies to be loaded in the gridView
    private ArrayList<Movie> mMoviesArray;


    public GridAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mMoviesArray = new ArrayList<Movie>();
        mWidth = Math.round(context.getResources().getDimension(R.dimen.poster_width));
        mHeight = Math.round(context.getResources().getDimension(R.dimen.poster_height));

    }

    /**
     * Remove all the movies of the grid adapter
     */
    public void clear() {
        mMoviesArray.clear();
       // notifyDataSetChanged();
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView iview;
        iview = new ImageView(context);
        iview.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
        iview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iview.setAdjustViewBounds(true);
        return iview;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int moviePosterColumn = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_URL);
        String moviePoster = cursor.getString(moviePosterColumn);

        // Build the URI of the poster and resize the image to make all the image of the same size once the api provid different size of images
        Uri posterUri = Utility.buildFullPosterPath(context.getString(R.string.poster_size_default),moviePoster);
        Picasso.with(context)
                .load(posterUri)
                .centerCrop().fit().noFade()
                .into((ImageView) view);

    }
}