package jnuneslab.com.cinemovies.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

import jnuneslab.com.cinemovies.model.Movie;
import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract.MovieEntry;

/**
 * Async Task responsible for fetch the movies of API
 */
public class FetchMovieTask extends AsyncTask<Integer, Void, Movie[]> {

    // Log variable
    private final String TAG = FetchMovieTask.class.getSimpleName();

    // Page Number to be fetched
    private int mNumPage;

    // Sort movie preference to be fetched
    private String mSortPreference;

    private final Context mContext;

    /**
     * Constructor
     * @param context of the application
     */
    public FetchMovieTask(Context context){
        mContext = context;
    }

    /**
     * Method responsible for parse the JSON object creating a Movie object that will be returned in a vector.
     *
     * @param movieJsonStr - result containing all the information of the movies
     * @param numMovies    - number of movies
     * @return - Movie[] - Vector containing all the movies fetched
     * @throws JSONException
     */
    private Movie[] getMovieDataFromJson(String movieJsonStr, int numMovies)
            throws JSONException {
        // JSON objects names that will need to be extracted.
        final String TMDB_RESULTS = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray moviesArray = movieJson.getJSONArray(TMDB_RESULTS);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

        // Parsing the JSONArray to populate the vector
        Movie[] resultMovies = new Movie[numMovies];
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieJSONObject = moviesArray.getJSONObject(i);
            resultMovies[i] = new Movie(movieJSONObject);
            cVVector.add(resultMovies[i].loadMovieContent());
        }

        int inserted = 0;

        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
        }

        //Log.d(TAG, "FetchMovieTask Complete. " + inserted + " Inserted");

        return resultMovies;
    }

    @Override
    protected Movie[] doInBackground(Integer... params) {

        // Return if the following parameters is not present: page number to be fetched
        if (params.length == 0) {
            return null;
        }

        // Connection variables
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        // Number of movies to be fetched according to API documentation the number of movies fetched by page is 20
        int numMovies = 20;

        mNumPage = params[0];
        try {
            // Construct the URL for the themoviedb query
            // Possible parameters are available at https://www.themoviedb.org/documentation/api
            final String API_BASE_URL = "https://api.themoviedb.org/3/movie/";
            final String API_MOVIE_PATH = "movie";
            final String API_PAGE_PARAM = "page";
            final String API_KEY_PARAM = "api_key";
            final String API_COUNT_PARAM = "vote_count.gte";

            // Set the sort preference choose by the user - Default sort value is popular
            mSortPreference = PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .getString(
                            mContext.getString(R.string.pref_sort_key),
                            mContext.getString(R.string.pref_sort_popular)
                    );

            // Build the URI
            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendPath(mSortPreference)
                    .appendQueryParameter(API_PAGE_PARAM, params[0].toString())
                    .appendQueryParameter(API_KEY_PARAM, mContext.getString(R.string.api_key))
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to TMDB api, and open the connection
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
            // Parse the movies from the JSON result into the Movie vector
            return getMovieDataFromJson(movieJsonStr, numMovies);
        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Movie[] movies) {
        super.onPostExecute(movies);

    }
}