package jnuneslab.com.cinemovies;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import jnuneslab.com.cinemovies.data.MovieContract;


/**
 * Main Activity fragment containing a gridView.
 * Created by Walter on 14/09/2015.
 */
public class MainActivityFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MOVIE_LOADER = 0;

    // Grid Adapter to be used in the Grid View
    GridAdapter mGridAdapter;

    // Number of Page is a incremental variable used to know which page will be fetch next
    int mNumPage = 0;

    // Task used to control flag to not fetch a new page while the previous fetch has not been completed
    FetchMovieTask movieTask;

    boolean mOnlyFavorites = false;

    Menu menu;
    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite_search) {
            mOnlyFavorites = true;
            item.setVisible(false);
            menu.findItem(R.id.action_normal_search).setVisible(true);
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            return true;
        }
        if (id == R.id.action_normal_search) {
            mOnlyFavorites = false;
            item.setVisible(false);
            menu.findItem(R.id.action_favorite_search).setVisible(true);
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch a new page of movies.
     *
     * @param numPage - Number of the page to be fetched
     */
    private void updateMovies(int numPage) {

        // First check if it is the first page
        // Do not fetch a new page if one is current in progress
        if (numPage > 0 && (movieTask == null || movieTask.getStatus() != AsyncTask.Status.FINISHED )) {
            return;
        }

        // Set the flag and fetch the next page
        movieTask = (FetchMovieTask) new FetchMovieTask(getContext(), mGridAdapter).execute(numPage + 1);
        mNumPage++;
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Check if it was the Sort key that was changed
        if(key.equals(getContext().getString(R.string.pref_sort_key))) {
            // Delete all contents to not blend old results with the new criteria
           getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    "-1", null);
            mNumPage = 0;

            // Start to fetch the movies from the first page
            updateMovies(0);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Case is only Favorite View restart the loader to get the changes of the new movies added or removed
        if(mOnlyFavorites)
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the GridAdapter that will be used to populate the GridView
        GridView gridview = (GridView) rootView.findViewById(R.id.grid_view);
        mGridAdapter = new GridAdapter(getActivity(),null, 0);

        gridview.setAdapter(mGridAdapter);

        // Set Item Click listener to open the Detail Activity of the selected movie poster selected
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v,
                                    int position,
                                    long id) {
                Cursor currentData = (Cursor) parent.getItemAtPosition(position);
                if (currentData != null) {
                    final int MOVIE_ID_COL = currentData.getColumnIndex(MovieContract.MovieEntry._ID);
                    Uri movieUri = MovieContract.MovieEntry.buildMovieWithId(currentData.getInt(MOVIE_ID_COL));

                    ((Callback) getActivity())
                            .onItemSelected(movieUri);
                }
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
                            if(!mOnlyFavorites && mNumPage > 0){
                                updateMovies(mNumPage);
                            }

                        }
                    }
                }

        );

         return rootView;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        updateMovies(0);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrderSetting = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
        String sortOrder;
        String clause;
        int NUMBER_OF_MOVIES = 20*(mNumPage);

        //TODO Remove SortOrder from API when the sync were made by SyncAdapter
        if (sortOrderSetting.equals(getString(R.string.pref_sort_popular))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
            sortOrder = MovieContract.MovieEntry.COLUMN_API_SORT + " ASC";
            clause = MovieContract.MovieEntry.COLUMN_API_SORT + " >= 0";
        } else {
            //sort by rating
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
            sortOrder = MovieContract.MovieEntry.COLUMN_API_SORT  + " DESC";
            clause = MovieContract.MovieEntry.COLUMN_API_SORT + " < 0";
        }

        if(mOnlyFavorites){
            clause = MovieContract.MovieEntry.COLUMN_FAVORITE + " = 1";
            sortOrder = MovieContract.MovieEntry.COLUMN_FAVORITE  + " DESC";
        }
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_POSTER_URL},
                clause,null,
                sortOrder + " LIMIT " + NUMBER_OF_MOVIES);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mGridAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridAdapter.swapCursor(null);
    }
}
