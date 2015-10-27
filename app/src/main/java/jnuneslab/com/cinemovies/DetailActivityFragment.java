package jnuneslab.com.cinemovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * Created by Walter on 20/10/2015.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_FAVORITED = 1;
    private static final int DETAIL_LOADER = 0;
    static final String DETAIL_URI = "URI";

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
        super.onCreateOptionsMenu(menu, inflater);
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
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Set the TextViews loaded in the rootView
        mMoviePoster = (ImageView) rootView.findViewById(R.id.movie_poster_image);
        mMovieTitle = (TextView) rootView.findViewById(R.id.movie_title);
        mMovieVotes = (TextView) rootView.findViewById(R.id.movie_votes);
        mMovieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        mMovieOverview = (TextView) rootView.findViewById(R.id.movie_overview);
        mMovieDate = (TextView) rootView.findViewById(R.id.movie_year);




        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.e("test", "test > on create" + mUri);
        if(mUri != null) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read Movie ID from cursor
            mMovieId = data.getInt(COL_MOVIE_ID);
            mMovieFavorite = data.getInt(COL_MOVIE_FAVORITE);

            //TODO flag to not make update several times
            // Update other fields if already it is no first load
            Log.e("test" , "test" + mMovieId);
            updateMovies(mMovieId, 1);
            // Use placeholder Image

            //TODO put into Utility
            // mMoviePoster.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
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
            mMovieVotes.setText(votes + "votes");

            // Read votes average from cursor and update view
            String rating = data.getString(COL_MOVIE_VOTE_AVERAGE);
            mMovieRating.setText(rating);

            // Read release date from cursor and update view
            String date = data.getString(COL_MOVIE_RELEASE_DATE);
            mMovieDate.setText(date);


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            //  if (mShareActionProvider != null) {
            //     mShareActionProvider.setShareIntent(createShareForecastIntent());
            //   }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}