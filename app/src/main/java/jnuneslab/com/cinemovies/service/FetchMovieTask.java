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

import jnuneslab.com.cinemovies.ui.adapter.GridAdapter;
import jnuneslab.com.cinemovies.model.Movie;
import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract.MovieEntry;

/**
 * Async Task responsible for fetch the movies using the TMDB api
 * Created by Walter on 10/10/2015.
 */
public class FetchMovieTask extends AsyncTask<Integer, Void, Movie[]> {

    // Log variable
    private final String TAG = FetchMovieTask.class.getSimpleName();

    private GridAdapter mGridAdapter;
    private final Context mContext;

    private int mNumPage;
    private String  sortPreference;

    public FetchMovieTask(Context context, GridAdapter gridAdapter){
        mContext = context;
        mGridAdapter = gridAdapter;
    }

    /**
     * Method responsible for parse the JSON object creating a Movie object that will be returned in a vector.
     *
     * @param movieJsonStr - result containing all the information of the movies
     * @param numMovies    - number of movies to be created
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
            //TODO refactory
            // Save the order that comes from the API because if order by database it is not guaranteed that will follow the API order (Api is not always up to date)

                resultMovies[i] = new Movie(movieJSONObject);
            if (sortPreference.equals("popularity.desc")){
            resultMovies[i].setApi_sort((mNumPage - 1) * 20 + i);
             }else {resultMovies[i] = new Movie(movieJSONObject);resultMovies[i].setApi_sort(((mNumPage - 1) * 20 + i)*(-1) -1);}
            cVVector.add(resultMovies[i].loadMovieContent());
           // Log.e("test", "test " + mNumPage + ">" + i + ">" +resultMovies[i].getApi_sort());
        }

        int inserted = 0;

        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
        }

        Log.d(TAG, "FetchMovieTask Complete. " + inserted + " Inserted");

        return resultMovies;
    }

    @Override
    protected Movie[] doInBackground(Integer... params) {

        // If param is null return because there is no specified to load
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
            final String API_BASE_URL = "https://api.themoviedb.org/3/discover/";
            final String API_MOVIE_PATH = "movie";
            final String API_PAGE_PARAM = "page";
            final String API_KEY_PARAM = "api_key";
            final String API_SORT_PARAM = "sort_by";
            final String API_COUNT_PARAM = "vote_count.gte";

            // Set the sort preference choose by the user - Default sort value is popular
            sortPreference = PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .getString(
                            mContext.getString(R.string.pref_sort_key),
                            mContext.getString(R.string.pref_sort_popular)
                    );

            // Build the URI
            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendPath(API_MOVIE_PATH)
                    .appendQueryParameter(API_PAGE_PARAM, params[0].toString())
                    .appendQueryParameter(API_KEY_PARAM, mContext.getString(R.string.api_key))
                    .appendQueryParameter(API_SORT_PARAM, sortPreference)
                    .appendQueryParameter(API_COUNT_PARAM, mContext.getString(R.string.pref_count))
                    .build();

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
            return getMovieDataFromJson(movieJsonStr, numMovies);
        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            e.printStackTrace();
        }

        // This return will only be called if something went wrong during the fetch
        return null;
    }

    @Override
    protected void onPostExecute(Movie[] movies) {
        super.onPostExecute(movies);

    }
}