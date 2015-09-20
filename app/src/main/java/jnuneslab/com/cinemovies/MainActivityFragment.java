package jnuneslab.com.cinemovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Main Activity fragment containing a gridView.
 * Created by Walter on 14/09/2015.
 */
public class MainActivityFragment extends Fragment  implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Grid Adapter to be used in the Grid View
    GridAdapter mGridAdapter;

    // Number of Page is a incremental variable used to know which page will be fetch next
    int mNumPage = 0;

    // Control flag used to not fetch a new page while the previous fetch has not been completed
    boolean mIsLoading = false;

    /**
     * Async Task responsible for fetch the movies using the TMDB api
     */
    private class FetchMovieTask extends AsyncTask<Integer, Void, Movie[]> {

        // Log variable
        private final String TAG = FetchMovieTask.class.getSimpleName();

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

            // Parsing the JSONArray to populate the vector
            Movie[] resultMovies = new Movie[numMovies];
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movieJSONObject = moviesArray.getJSONObject(i);
                resultMovies[i] = new Movie(movieJSONObject);
            }
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

            try {
                // Construct the URL for the themoviedb query
                // Possible parameters are available at https://www.themoviedb.org/documentation/api
                final String API_BASE_URL = "https://api.themoviedb.org/3/discover/";
                final String API_MOVIE_PATH = "movie";
                final String API_PAGE_PARAM = "page";
                final String API_KEY_PARAM = "api_key";
                final String API_SORT_PARAM = "sort_by";

                // Set the sort preference choose by the user - Default sort value is popular
                String sortPreference = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString(
                                getString(R.string.pref_sort_key),
                                getString(R.string.pref_sort_popular)
                        );

                // Build the URI
                Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendPath(API_MOVIE_PATH)
                        .appendQueryParameter(API_PAGE_PARAM, params[0].toString())
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .appendQueryParameter(API_SORT_PARAM, sortPreference)
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
            mIsLoading = false;
            if (movies != null) {
                mGridAdapter.addAll(movies);
                mNumPage++;
            }


        }
    }

    /**
     * Fetch a new page of movies.
     *
     * @param numPage - Number of the page to be fetched
     */
    private void updateMovies(int numPage) {

        // Do not fetch a new page if one is current in progress
        if (mIsLoading) {
            return;
        }

        // Set the flag and fetch the next page
        mIsLoading = true;
        new FetchMovieTask().execute(numPage + 1);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Check if it was the Sort key that was changed
        if(key.equals(getContext().getString(R.string.pref_sort_key))) {
            // Clear the gridView and load the list of movies according to new sort
            mGridAdapter.clear();
            mIsLoading = false;
            // Start to fetch the movies from the first page
            updateMovies(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the GridAdapter that will be used to populate the GridView
        GridView gridview = (GridView) rootView.findViewById(R.id.grid_view);
        mGridAdapter = new GridAdapter(rootView.getContext());
        gridview.setAdapter(mGridAdapter);

        // Clear the Adapter to not have old results in the create view lifecycle
        mGridAdapter.clear();
        mIsLoading = false;

        // Start to fetch the movies from the first page
        updateMovies(0);

        // Set Item Click listener to open the Detail Activity of the selected movie poster selected
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v,
                                    int position,
                                    long id) {

                // Get the movie from the adapter of the selected item
                GridAdapter adapter = (GridAdapter) parent.getAdapter();
                Movie movie = adapter.getItem(position);

                if (movie == null) {
                    return;
                }

                // Create an intent and put an Extra into it with a bundle that contains all the information of the movie to be used in Detail Activity
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Movie.EXTRA_MOVIE_BUNDLE, movie.loadMovieBundle());

                // Call DetailActivity
                getActivity().startActivity(intent);
            }
        });

        // Set Scroll Listener to fetch the next page of movies when the scroll page reach the end of the screen
        gridview.setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int lastInScreen = firstVisibleItem + visibleItemCount;
                        if (lastInScreen == totalItemCount) {
                            if(mNumPage > 0)
                            updateMovies(mNumPage);
                        }
                    }
                }

        );

         return rootView;
    }
}
