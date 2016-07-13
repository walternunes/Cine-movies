package jnuneslab.com.cinemovies.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * Utility class
 */
public class Utility {
    /**
     * Build the image poster path URI
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

    /**
     * Update the Database register favorite flag
     * @param context of the application
     * @param movieId Id of the movie
     * @param favorite - indicator if the movie is favorite
     * @return int - Quantity of the registers updated
     */
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

    /**
     * Check if the Shared Preference is empty or not
     * @param context
     * @return boolean - true if is empty
     */
    public static boolean isPreferredEmpty(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.contains(context.getString(R.string.pref_page_number_key));
    }


    /**
     * Projection of movie detail query interface
     */
    public interface MovieDetailQuery {
         String[] DETAIL_COLUMNS = {
                MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_POSTER_URL,
                MovieContract.MovieEntry.COLUMN_TITLE,
                MovieContract.MovieEntry.COLUMN_OVERVIEW,
                MovieContract.MovieEntry.COLUMN_POPULARITY,
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
                MovieContract.MovieEntry.COLUMN_FAVORITE,
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE };

        // Follow the same order of DETAIL_COLUMNS
        int COL_ID = 0;
        int COL_MOVIE_ID = 1;
        int COL_MOVIE_POSTER_URL = 2;
        int COL_MOVIE_TITLE = 3;
        int COL_MOVIE_OVERVIEW = 4;
        int COL_MOVIE_POPULARITY = 5;
        int COL_MOVIE_VOTE_AVERAGE = 6;
        int COL_MOVIE_VOTE_COUNT = 7;
        int COL_MOVIE_FAVORITE = 8;
        int COL_MOVIE_RELEASE_DATE = 9;
    }
}
