package jnuneslab.com.cinemovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * GridAdapter used to load the imagesViews into the gridView
 * Created by Walter on 14/09/2015.
 */
public class GridAdapter extends BaseAdapter {

    // Context of the application
    private Context mContext;

    // Height of the image poster in the imageView
    private int mHeight;

    // Width of the image poster in the imageView
    private int mWidth;

    // Array containing all the movies to be loaded in the gridView
    private ArrayList<Movie> mMoviesArray;

    /**
     * GridAdapter constructor that will initialize the class variables
     *
     * @param c - context of the application
     */
    public GridAdapter(Context c) {
        mContext = c;
        mMoviesArray = new ArrayList<Movie>();
        mWidth = Math.round(mContext.getResources().getDimension(R.dimen.poster_width));
        mHeight = Math.round(mContext.getResources().getDimension(R.dimen.poster_height));
    }

    /**
     * Remove all the movies of the grid adapter
     */
    public void clear() {
        mMoviesArray.clear();
    }

    /**
     * Add all the movies of the @param movies into the array mMoviesArray once the GridView will use an Array and not a Movies[]
     *
     * @param movies - containing the movies that will be added in the GridView
     */
    public void addAll(Movie[] movies) {
        if (movies == null) {
            Toast.makeText(mContext, mContext.getString(R.string.connection_problem),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mMoviesArray.addAll(Arrays.asList(movies));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMoviesArray.size();
    }

    @Override
    public Movie getItem(int position) {
        if (position < 0 || position >= mMoviesArray.size()) {
            return null;
        }
        return mMoviesArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        Movie movie = getItem(position);
        if (movie == null) {
            return -1;
        }

        return movie.getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ImageView iview;

        Movie movie = getItem(position);

        // If the view is null, initialize it with the primal values
        if (view == null) {
            iview = new ImageView(mContext);
            iview.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
            iview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iview.setAdjustViewBounds(true);
        } else {
            iview = (ImageView) view;
        }

        // Build the URI of the poster and resize the image to make all the image of the same size once the api provid different size of images
        Uri posterUri = movie.buildFullPosterPath(mContext.getString(R.string.poster_size_default));
        Picasso.with(mContext)
                .load(posterUri)
                .fit().centerCrop()
                .into(iview);
        return iview;
    }
}