package jnuneslab.com.cinemovies.ui.fragment;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import jnuneslab.com.cinemovies.sync.MovieSyncAdapter;
import jnuneslab.com.cinemovies.ui.adapter.GridAdapter;
import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract;
import jnuneslab.com.cinemovies.service.FetchMovieTask;
import jnuneslab.com.cinemovies.util.Utility;


/**
 * Main Activity fragment containing a gridView.
 */
public class MainActivityFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener,  SwipeRefreshLayout.OnRefreshListener {

    private static final int MOVIE_LOADER = 0;

    SwipeRefreshLayout mSwipeRefreshLayout;

    // Grid Adapter to be used in the Grid View
    GridAdapter mGridAdapter;

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

        int id = item.getItemId();
/*
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
*/
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch movies
     * @param fullRequest - represents the type of request
     */
    private void updateMovies(boolean fullRequest) {

            MovieSyncAdapter.syncNextPage(getActivity(), fullRequest);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Check if it was the Sort key that was changed and if was not selected only favorite movies
        if(key.equals(getContext().getString(R.string.pref_sort_key)) && !sharedPreferences.equals(getString(R.string.pref_sort_favorite))) {

            // Delete all contents to not blend old results with the new criteria
            getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    "-1", null);
            // Start to fetch the movies from the first page
            updateMovies(true);
            // Refresh loader
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    @Override
    public void onRefresh() {

        // Force full update request when scroll down the movie grid if not in Only favorite movies option otherwise just cancel swipe refresh and not fetch
        if(!mOnlyFavorites) {
            updateMovies(true);
        }else{
            postRefreshing(false);
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
     //   if(mOnlyFavorites)
          //  getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        // Register listener
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister listener
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Method responsible for control the refresh icon
     * @param refreshing - boolean
     */
    private void postRefreshing(final boolean refreshing) {

        // Check if layout is not null and then add or remove the refresh icon according to the parameter
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.post(new Runnable() {
                @Override public void run() {
                    if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(refreshing);
                }
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the GridAdapter that will be used to populate the GridView
        GridView gridview = (GridView) rootView.findViewById(R.id.grid_view);
        mGridAdapter = new GridAdapter(getActivity(),null, 0);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
      //  mSwipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.swipe_progress_colors));

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
                    Uri movieUri = MovieContract.MovieEntry.buildMovieIdUri(currentData.getInt(MOVIE_ID_COL));

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

                        // check if the list is not empty and if it is the last position to load more content
                        if (lastInScreen == totalItemCount && totalItemCount != 0) {
                            if(!mOnlyFavorites){
                               updateMovies(false);
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
        if (Utility.isPreferredEmpty(getActivity())) {
            Log.e("teste", "test sync no if empty" );
            updateMovies(true);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrderSetting = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
        String sortOrder;
        String clause;

        //TODO Remove SortOrder from API when the sync were made by SyncAdapter
        if (sortOrderSetting.equals(getString(R.string.pref_sort_popular))) {
            mOnlyFavorites = false;
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
            sortOrder = MovieContract.MovieEntry.COLUMN_FAVORITE + " DESC";
          //  clause = MovieContract.MovieEntry.COLUMN_API_SORT + " >= 0";
            clause = null;
        } else if(sortOrderSetting.equals(getString(R.string.pref_sort_rated))) {
            mOnlyFavorites = false;
            //sort by rating
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
            sortOrder = MovieContract.MovieEntry.COLUMN_FAVORITE  + " DESC" ;
           // clause = MovieContract.MovieEntry.COLUMN_API_SORT + " < 0";
            clause = null;
        }else{
            mOnlyFavorites = true;
            sortOrder = MovieContract.MovieEntry.COLUMN_FAVORITE + " DESC";
            clause = null;
        }

        if(mOnlyFavorites){
            clause = MovieContract.MovieEntry.COLUMN_FAVORITE + " = 1";
            sortOrder = MovieContract.MovieEntry.COLUMN_FAVORITE  + " DESC";
        }
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_POSTER_URL},
                clause,null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mGridAdapter.swapCursor(cursor);
        postRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridAdapter.swapCursor(null);
        postRefreshing(false);
    }
}
