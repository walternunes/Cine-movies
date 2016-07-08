package jnuneslab.com.cinemovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Database Movie contract
 */
public abstract class MovieContract {

    public static final String CONTENT_AUTHORITY = "jnuneslab.com.cinemovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailers";
    public static final String PATH_REVIEW = "reviews";

    /**
     * Inner class that defines the table - contents of the movie
     **/
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        // Table Columns
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
        public static final String COLUMN_FAVORITE = "favorite";

        /**
         * Creates a movie uri with the movie id (from the backend) appended
         *
         * @param movieId The ID of the movie
         * @return The uri of the movie
         */
        public static Uri buildMovieIdUri(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }

        /**
         * Get the movie Poster path
         *
         * @param uri The movie URI
         * @return The path of the Poster
         */
        public static String getPosterFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Get the movie Id from the URI
         *
         * @param uri The movie URI with the movie ID
         * @return The movie ID
         */
        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    /**
     * Inner class that defines the table - Trailers of the movie
     **/
    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = "trailers";

        // Table columns
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_YOUTUBE_KEY = "youtube_key";
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        /**
         * Get the movie ID in the URI (the ID from the Backend)
         *
         * @param uri The trailer's URI with the movie ID
         * @return The movie ID
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

    /**
     * Inner class that defines the table - Reviews of the movie
     **/
    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "reviews";

        // Table columns
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        /**
         * Get the movie ID in the URI (the ID from the Backend)
         *
         * @param uri The Uri of the review with the movie id appended
         * @return The ID of the movie
         */
        public static long getMovieIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        /**
         * Creates a trailer uri with the movie id (from the backend) appended
         *
         * @param movieId The ID of the movie
         * @return The uri of the review
         */
        public static Uri buildTrailerWithId(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }
    }
}
