package jnuneslab.com.cinemovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Walter on 27/09/2015.
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "jnuneslab.com.cinemovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://jnuneslab.com.cinemovies/movies/ is a valid path for
    // looking at movie data.

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailers";
    public static final String PATH_REVIEW = "reviews";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movie_id"; // the movie id from the API
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_API_SORT = "api_sort";
        public static final String COLUMN_FAVORITE = "favorite";


        public static Uri buildMovieIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoviePosterURI(String posterPath){
            return CONTENT_URI.buildUpon().appendPath(posterPath.substring(1)).build(); // Remove the first slash of the URL
        }

        public static String getPosterFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        public static Uri buildMovieWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class TrailerEntry implements BaseColumns {
        // Content URI for the TrailerEntry
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        // Constant strings to tell the difference between a list of items (CONTENT_TYPE)
        // and a singe item (CONTENT_ITEM_TYPE)
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = "trailers";

        // columns
        public static final String COLUMN_TITLE = "title"; //trailer title
        public static final String COLUMN_YOUTUBE_KEY = "youtube_key";
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_MOVIE_ID = "movie_id"; // the movie id from the backend (used for joins)

        /**
         * Get the movie ID in the URI (the ID from the Backend)
         *
         * @param uri The trailer's URI with the movie ID
         * @return The movie ID or -1 if doesn't exist
         */
        public static long getMovieIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        /**
         * Creates a trailer uri with the movie id (from the backend) appended
         *
         * @param movieId The movie ID
         * @return the URI of the trailer
         */
        public static Uri buildTrailerWithId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        // Content URI for the ReviewEntry
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        // Constant strings to tell the difference between a list of items (CONTENT_TYPE)
        // and a singe item (CONTENT_ITEM_TYPE)
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "reviews";

        // columns
        public static final String COLUMN_AUTHOR = "author"; //trailer title
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_MOVIE_ID = "movie_id"; // the movie id from the backend (used for joins)

        /**
         * Get the movie ID in the URI (the ID from the Backend)
         *
         * @param uri The Uri of the review with the movie id appended
         * @return The ID of the movie, or -1 if doesn't exist
         */
        public static long getMovieIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        /**
         * Creates a trailer uri with the movie id (from the backend) appended
         *
         * @param insertedId The ID of the movie
         * @return The uri of the review
         */
        public static Uri buildTrailerWithId(long insertedId) {
            return ContentUris.withAppendedId(CONTENT_URI, insertedId);
        }
    }
}
