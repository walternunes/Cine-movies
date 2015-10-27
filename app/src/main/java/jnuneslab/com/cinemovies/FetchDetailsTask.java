package jnuneslab.com.cinemovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

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

import jnuneslab.com.cinemovies.data.MovieContract;
import jnuneslab.com.cinemovies.data.MovieContract.MovieEntry;

/**
 * Created by Walter on 10/10/2015.
 */
/**
 * Async Task responsible for fetch the movies using the TMDB api
 */
public class FetchDetailsTask extends AsyncTask<Integer, Void, String[]> {

    // Log variable
    private final String TAG = FetchDetailsTask.class.getSimpleName();

    private Integer movieID =0;
    private final Context mContext;

    private static final int FETCH_TRAILERS = 1;
    private static final int FETCH_REVIEWS = 2;

    public static final String KEY_ID = "id";
    public static final String KEY_YOUTUBE_KEY = "key";
    public static final String KEY_NAME= "name";

    FetchDetailsTask(Context context){
        mContext = context;
    }

    /**
     * Method responsible for parse the JSON object creating a Movie object that will be returned in a vector.
     *
     * @param movieJsonStr - result containing all the information of the movies
     * @param type    - type that will be fetched (trailer or reviews)
     * @return - Movie[] - Vector containing all the movies fetched
     * @throws JSONException
     */
    private String[] getMovieDetailsFromJson(String movieJsonStr, int type)
            throws JSONException {
        // JSON objects names that will need to be extracted.
        final String TMDB_RESULTS = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray moviesArray = movieJson.getJSONArray(TMDB_RESULTS);

        ContentValues trailerValues;
        Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

        // Parsing the JSONArray to populate the vector
      //  Movie[] resultMovies = new Movie[numMovies];
        for (int i = 0; i < moviesArray.length(); i++) {
            trailerValues = new ContentValues();
            JSONObject movieJSONObject = moviesArray.getJSONObject(i);

            trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, movieJSONObject.getString(KEY_ID));
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_TITLE, movieJSONObject.getString(KEY_NAME));
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_YOUTUBE_KEY, movieJSONObject.getString(KEY_YOUTUBE_KEY));
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieID);

            cVVector.add(trailerValues);
        }

        int inserted = 0;

        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
        }

        Log.d(TAG, "DetailMovieTask Complete. " + inserted + " Inserted");

        String [] a = {"aaa", "bbb"};
        return a;
    }

    @Override
    protected String[] doInBackground(Integer... params) {


        // If param is null or different than 2 return because there is no specified to load
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


            Log.e("test", "test " + builtUri);
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
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();

        } catch (Exception ex) {
            // If the code didn't successfully get the movie data, there's no point in attempting to parse it.
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
            // Parse the movies from the JSON result into the Movie vector
            return getMovieDetailsFromJson(movieJsonStr, 0);
        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            e.printStackTrace();
        }

        // This return will only be called if something went wrong during the fetch
        return null;
    }
/*
    @Override
    protected void onPostExecute(Movie[] movies) {
        super.onPostExecute(movies);
        //mIsLoading = false;
        //if (movies != null) {
        //    mGridAdapter.addAll(movies);
        //    mNumPage++;

        //  }


    }*/
}