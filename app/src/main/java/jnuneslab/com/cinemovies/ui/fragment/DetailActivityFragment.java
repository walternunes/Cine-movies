package jnuneslab.com.cinemovies.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.squareup.picasso.Picasso;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.ui.adapter.ReviewAdapter;
import jnuneslab.com.cinemovies.ui.adapter.TrailerAdapter;
import jnuneslab.com.cinemovies.util.Utility;
import jnuneslab.com.cinemovies.data.MovieContract;
import jnuneslab.com.cinemovies.service.FetchDetailsTask;

/**
 * Detail Actitivy fragment that contains the information of the movie
 * Created by Walter on 20/10/2015.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_FAVORITED = 1;
    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    public static final String DETAIL_URI = "URI";
    private static final String MOVIE_SHARE_HASHTAG = " #Cine Movie";

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_URL,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_POSTER_URL = 2;
    public static final int COL_MOVIE_TITLE = 3;
    public static final int COL_MOVIE_OVERVIEW = 4;
    public static final int COL_MOVIE_POPULARITY = 5;
    public static final int COL_MOVIE_VOTE_AVERAGE = 6;
    public static final int COL_MOVIE_VOTE_COUNT = 7;
    public static final int COL_MOVIE_FAVORITE = 8;
    public static final int COL_MOVIE_RELEASE_DATE = 9;

    private int mMovieId;
    private int mMovieFavorite;
    private TextView mMovieTitle;
    private TextView mMovieVotes;
    private TextView mMovieRating;
    private TextView mMovieOverview;
    private TextView mMovieDate;

    private ImageView mMoviePoster;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private ShareActionProvider mShareActionProvider;
    private String mShareMovie;

    private Uri mUri;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem menuFavorite = menu.findItem(R.id.action_favorite);
        if (menuFavorite != null) {
            if (mMovieFavorite == MOVIE_FAVORITED) {
                menuFavorite.setIcon(android.R.drawable.btn_star_big_on);
            } else {
                menuFavorite.setIcon(android.R.drawable.btn_star_big_off);
            }
        }
        menuFavorite = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuFavorite);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareMovie != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareMovie + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            if(mMovieId != 0 ){
                if(mMovieFavorite == MOVIE_FAVORITED){
                    Utility.updateMovieWithFavorite(getContext(), mMovieId, 0);
                    mMovieFavorite = 0;
                    item.setIcon(android.R.drawable.btn_star_big_off);
                }else{
                    Utility.updateMovieWithFavorite(getContext(), mMovieId, MOVIE_FAVORITED);
                    mMovieFavorite = MOVIE_FAVORITED;
                    item.setIcon(android.R.drawable.btn_star_big_on);
                }

            }
            return true;
        }else if(id == R.id.action_share){
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateMovies(int movieId, int fetchType) {
        new FetchDetailsTask(getContext()).execute(movieId, fetchType);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, null, false);
        View movieDetailView = inflater.inflate(R.layout.movie_detail_item, null, false);

        // Set the TextViews loaded in the rootView
        mMoviePoster = (ImageView) movieDetailView.findViewById(R.id.movie_poster_image);
        mMovieTitle = (TextView) movieDetailView.findViewById(R.id.movie_title);
        mMovieVotes = (TextView) movieDetailView.findViewById(R.id.movie_votes);
        mMovieRating = (TextView) movieDetailView.findViewById(R.id.movie_rating);
        mMovieOverview = (TextView) movieDetailView.findViewById(R.id.movie_overview);
        mMovieDate = (TextView) movieDetailView.findViewById(R.id.movie_year);

        MergeAdapter mergeAdapter = new MergeAdapter();
        mergeAdapter.addView(movieDetailView);

        mTrailerAdapter = new TrailerAdapter(getActivity(), null, 0);
        mergeAdapter.addAdapter(mTrailerAdapter);

        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
        mergeAdapter.addAdapter(mReviewAdapter);

        ListView detailsListView = (ListView) rootView.findViewById(R.id.details_listview);
        detailsListView.setAdapter(mergeAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(id == DETAIL_LOADER) {
            if (mUri != null) {
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        }else if(id == TRAILER_LOADER){
            return new CursorLoader(
                    getActivity(),
                    MovieContract.TrailerEntry.CONTENT_URI,
                    null,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(mMovieId)},
                    null);
        }else if(id == REVIEW_LOADER){
            return new CursorLoader(
                    getActivity(),
                    MovieContract.ReviewEntry.CONTENT_URI,
                    null,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(mMovieId)},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == DETAIL_LOADER){
        if (data != null && data.moveToFirst()) {
            // Read Movie ID from cursor
            mMovieId = data.getInt(COL_MOVIE_ID);
            mMovieFavorite = data.getInt(COL_MOVIE_FAVORITE);

            // restart loader for load the trailer and review Views
            getLoaderManager().restartLoader(TRAILER_LOADER, null, this);
            getLoaderManager().restartLoader(REVIEW_LOADER, null, this);

            // Build the URI path of the poster to be loaded in the Detail activity
            Uri posterUri = Utility.buildFullPosterPath(getString(R.string.poster_size_default), data.getString(COL_MOVIE_POSTER_URL));
            Picasso.with(getContext())
                    .load(posterUri)
                    .into(mMoviePoster);

            // Read description from cursor and update view
            String description = data.getString(COL_MOVIE_OVERVIEW);
            mMovieOverview.setText(description);

            // Read title from cursor and update view
            String title = data.getString(COL_MOVIE_TITLE);
            mMovieTitle.setText(title);

            // Read votes count from cursor and update view
            String votes = data.getString(COL_MOVIE_VOTE_COUNT);
            mMovieVotes.setText(votes + " votes");

            // Read votes average from cursor and update view
            String rating = data.getString(COL_MOVIE_VOTE_AVERAGE);
            mMovieRating.setText(rating);

            // Read release date from cursor and update view
            String date = data.getString(COL_MOVIE_RELEASE_DATE);
            mMovieDate.setText(date.substring(0,4));

            mShareMovie = String.format("Movie: %s - Popularity %s", title, rating);
            //If onCreateOptionsMenu has already happened, update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }}else if (loader.getId() == TRAILER_LOADER){
            // Case the trailer fetch was already done, not fetch again the trailer list
            if(data.getCount() == 0) {
                updateMovies(mMovieId, TRAILER_LOADER);
            }
            mTrailerAdapter.swapCursor(data);
        }else if (loader.getId() == REVIEW_LOADER){
            // Case the review fetch was already done, not fetch again the review list again
            if(data.getCount() == 0) {
                updateMovies(mMovieId, REVIEW_LOADER);
            }
            mReviewAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == TRAILER_LOADER) {
            mTrailerAdapter.swapCursor(null);
        }else if(loader.getId() == REVIEW_LOADER) {
            mReviewAdapter.swapCursor(null);
        }
    }
}