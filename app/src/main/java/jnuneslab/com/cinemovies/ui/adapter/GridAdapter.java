package jnuneslab.com.cinemovies.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.util.Utility;
import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * GridAdapter used to load the imagesViews into the gridView
 */
public class GridAdapter extends CursorAdapter {

    // Height of the image poster in the imageView
    private int mHeight;

    // Width of the image poster in the imageView
    private int mWidth;


    public GridAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mWidth = Math.round(context.getResources().getDimension(R.dimen.poster_width));
        mHeight = Math.round(context.getResources().getDimension(R.dimen.poster_height));

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

        // Build the URI of the poster and resize the image to make all the image of the same size once the api provides different size of images
        Uri posterUri = Utility.buildFullPosterPath(context.getString(R.string.poster_size_default), moviePoster);
        Picasso.with(context)
                .load(posterUri)
                .centerCrop().fit().noFade()
                .into((ImageView) view);

    }
}