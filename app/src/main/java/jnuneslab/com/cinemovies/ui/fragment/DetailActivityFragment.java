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
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_FAVORITED = 1;
    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    public static final String DETAIL_URI = "URI";
    private static final String MOVIE_SHARE_HASHTAG = " #Cine Movie";


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
    MenuItem  menuFavorite;
    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menuFavorite = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuFavorite);

        menuFavorite = menu.findItem(R.id.action_favorite);
        updateFavoriteIcon();


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

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            if (mMovieId != 0) {
                if (mMovieFavorite == MOVIE_FAVORITED) {
                    Utility.updateMovieWithFavorite(getContext(), mMovieId, 0);
                    mMovieFavorite = 0;
                } else {
                    Utility.updateMovieWithFavorite(getContext(), mMovieId, MOVIE_FAVORITED);
                    mMovieFavorite = MOVIE_FAVORITED;
                }
                updateFavoriteIcon();

            }
            return true;
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

    private void updateFavoriteIcon(){
        if(menuFavorite != null){
            if (menuFavorite != null) {
                if (mMovieFavorite == MOVIE_FAVORITED) {
                    menuFavorite.setIcon(android.R.drawable.btn_star_big_on);
                } else {
                    menuFavorite.setIcon(android.R.drawable.btn_star_big_off);
                }
            }
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == DETAIL_LOADER) {
            if (mUri != null) {
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        Utility.MovieDetailQuery.DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        } else if (id == TRAILER_LOADER) {
            Uri trailerURI = MovieContract.TrailerEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(mMovieId)).build();
            return new CursorLoader(
                    getActivity(),
                    trailerURI,
                    null,
                    null,
                    null,
                    null);
        } else if (id == REVIEW_LOADER) {
            Uri reviewURI = MovieContract.ReviewEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(mMovieId)).build();
            return new CursorLoader(
                    getActivity(),
                    reviewURI,
                    null,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == DETAIL_LOADER) {
            if (data != null && data.moveToFirst()) {
                // Read Movie ID from cursor
                mMovieId = data.getInt(Utility.MovieDetailQuery.COL_MOVIE_ID);
                mMovieFavorite = data.getInt(Utility.MovieDetailQuery.COL_MOVIE_FAVORITE);
                updateFavoriteIcon();
                // restart loader for load the trailer and review Views
                getLoaderManager().restartLoader(TRAILER_LOADER, null, this);
                getLoaderManager().restartLoader(REVIEW_LOADER, null, this);

                // Build the URI path of the poster to be loaded in the Detail activity
                Uri posterUri = Utility.buildFullPosterPath(getString(R.string.poster_size_default), data.getString(Utility.MovieDetailQuery.COL_MOVIE_POSTER_URL));
                Picasso.with(getContext())
                        .load(posterUri)
                        .into(mMoviePoster);

                // Get the information of the fields and update de view
                String description = data.getString(Utility.MovieDetailQuery.COL_MOVIE_OVERVIEW);
                mMovieOverview.setText(description);

                String title = data.getString(Utility.MovieDetailQuery.COL_MOVIE_TITLE);
                mMovieTitle.setText(title);

                String votes = data.getString(Utility.MovieDetailQuery.COL_MOVIE_VOTE_COUNT);
                mMovieVotes.setText(votes + getContext().getString(R.string.votes_detail_text));

                String rating = data.getString(Utility.MovieDetailQuery.COL_MOVIE_VOTE_AVERAGE);
                mMovieRating.setText(rating);

                String date = data.getString(Utility.MovieDetailQuery.COL_MOVIE_RELEASE_DATE);
                mMovieDate.setText(date.substring(0, 4));

                mShareMovie = String.format(getContext().getString(R.string.share_text), title, rating);

                //If onCreateOptionsMenu has already happened, update the share intent now.
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareMovieIntent());
                }
            }
        } else if (loader.getId() == TRAILER_LOADER) {
            // Case the trailer fetch was already done, not fetch again the trailer list
            if (data.getCount() == 0) {
                updateMovies(mMovieId, TRAILER_LOADER);
            }
            mTrailerAdapter.swapCursor(data);
        } else if (loader.getId() == REVIEW_LOADER) {
            // Case the review fetch was already done, not fetch again the review list again
            if (data.getCount() == 0) {
                updateMovies(mMovieId, REVIEW_LOADER);
            }
            mReviewAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == TRAILER_LOADER) {
            mTrailerAdapter.swapCursor(null);
        } else if (loader.getId() == REVIEW_LOADER) {
            mReviewAdapter.swapCursor(null);
        }
    }
}