package jnuneslab.com.cinemovies;

import android.net.Uri;

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
}
