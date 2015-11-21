package jnuneslab.com.cinemovies.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * Created by Walter on 15/10/2015.
 */
public class Utility {
    /**
     * Build the image poster path URI
     *
     * @param size - attribute that represents the size of the poster
     * @return return the full path of the poster image
     */
    public static Uri buildFullPosterPath(String size, String posterPath) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(posterPath.substring(1))
                .build();

        return builtUri;
    }

    public static int updateMovieWithFavorite(Context context, int movieId, int favorite) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, favorite);

        return context.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                values,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "= ?",
                new String[]{Integer.toString(movieId)}
        );
    }
}
