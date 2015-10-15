package jnuneslab.com.cinemovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;


/**
 * Main Activity fragment containing a gridView.
 * Created by Walter on 14/09/2015.
 */
public class MainActivityFragment extends Fragment  implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Grid Adapter to be used in the Grid View
    GridAdapter mGridAdapter;

    // Number of Page is a incremental variable used to know which page will be fetch next
    int mNumPage = 0;

    // Task used to control flag to not fetch a new page while the previous fetch has not been completed
    FetchMovieTask movieTask;



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

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Check if it was the Sort key that was changed
        if(key.equals(getContext().getString(R.string.pref_sort_key))) {
            // Clear the gridView and load the list of movies according to new sort
            mGridAdapter.clear();
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
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
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
