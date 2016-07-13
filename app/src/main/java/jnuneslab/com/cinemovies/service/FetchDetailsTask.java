package jnuneslab.com.cinemovies.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * Async Task responsible for fetch the Trailers and Reviews of API
 */
public class FetchDetailsTask extends AsyncTask<Integer, Void, Void> {

    // Log variable
    private final String TAG = FetchDetailsTask.class.getSimpleName();

    private Integer movieID =0;
    private final Context mContext;

    private static final int FETCH_TRAILERS = 1;
    private static final int FETCH_REVIEWS = 2;

    public static final String KEY_ID = "id";
    public static final String KEY_YOUTUBE_KEY = "key";
    public static final String KEY_NAME= "name";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_CONTENT= "content";

    /**
     * Constructor
     * @param context of the application
     */
    public FetchDetailsTask(Context context){
        mContext = context;
    }

    /**
     * Method responsible for parse the JSON object creating a Movie object that will be returned in a vector.
     * @param movieJsonStr - result containing all the information of the movies
     * @param type - type that will be fetched (trailer or reviews)
     * @throws JSONException
     */
    private void getMovieDetailsFromJson(String movieJsonStr, int type)
            throws JSONException {

        // JSON object response name
        final String TMDB_RESULTS = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray moviesArray = movieJson.getJSONArray(TMDB_RESULTS);

        ContentValues contentValues;
        Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

        // Parsing the JSONArray to populate the vector
        for (int i = 0; i < moviesArray.length(); i++) {
            contentValues = new ContentValues();
            JSONObject movieJSONObject = moviesArray.getJSONObject(i);

            if(type == FETCH_TRAILERS) {
                contentValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, movieJSONObject.getString(KEY_ID));
                contentValues.put(MovieContract.TrailerEntry.COLUMN_TITLE, movieJSONObject.getString(KEY_NAME));
                contentValues.put(MovieContract.TrailerEntry.COLUMN_YOUTUBE_KEY, movieJSONObject.getString(KEY_YOUTUBE_KEY));
                contentValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieID);
            }else{
                contentValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, movieJSONObject.getString(KEY_ID));
                contentValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, movieJSONObject.getString(KEY_AUTHOR));
                contentValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, movieJSONObject.getString(KEY_CONTENT));
                contentValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieID);
            }
            cVVector.add(contentValues);
        }

        int inserted = 0;

        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            if(type == FETCH_TRAILERS) {
                inserted = mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
            }else{
                inserted = mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
            }
        }

        //Log.d(TAG, "DetailMovieTask Complete. " + inserted + " Inserted" + type);

    }

    @Override
    protected Void doInBackground(Integer... params) {

        // Return if the following parameters is not present: movieID and FetchType(trailer or review)
        if (params.length == 0 || params.length != 2) {
            return null;
        }

        int fetchMovieDetails = 0;


        movieID = params[0];
        fetchMovieDetails = params[1];

        // Connection variables
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;


        try {
            // Construct the URL for the themoviedb query
            // Possible parameters are available at https://www.themoviedb.org/documentation/api
            final String API_BASE_URL = "https://api.themoviedb.org/3/";
            final String API_TRAILER_PATH = "videos";
            final String API_REVIEW_PATH = "reviews";
            final String API_MOVIE_PATH = "movie";
            final String API_KEY_PARAM = "api_key";


            // Build the URI according to fetch type
            Uri builtUri;
            if (fetchMovieDetails == FETCH_REVIEWS) {
                builtUri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendPath(API_MOVIE_PATH)
                        .appendPath(movieID.toString())
                        .appendPath(API_REVIEW_PATH)
                        .appendQueryParameter(API_KEY_PARAM, mContext.getString(R.string.api_key))
                        .build();
            } else{
                builtUri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendPath(API_MOVIE_PATH)
                        .appendPath(movieID.toString())
                        .appendPath(API_TRAILER_PATH)
                        .appendQueryParameter(API_KEY_PARAM, mContext.getString(R.string.api_key))
                        .build();
            }

            //Log.e(TAG, "Movie URI fetch - " + builtUri);
            URL url = new URL(builtUri.toString());

            // Create the request to themoviedb api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();

        } catch (Exception ex) {
            Log.e(TAG, "Error fetching movie", ex);
            ex.printStackTrace();
            return null;
        } finally {
            // Closing the connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            // Closing the buffer
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        try {
            // Parse the Trailers or Reviews of the movie according to the request
            if(fetchMovieDetails == FETCH_TRAILERS)
                getMovieDetailsFromJson(movieJsonStr, FETCH_TRAILERS);
            else getMovieDetailsFromJson(movieJsonStr, FETCH_REVIEWS);
        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            e.printStackTrace();
        }

        return null;
    }

}