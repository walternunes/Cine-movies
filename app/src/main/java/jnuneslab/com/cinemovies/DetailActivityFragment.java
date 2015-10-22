package jnuneslab.com.cinemovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class DetailActivityFragment extends Fragment {

    private int mMovieId;
    private int mFavorite;

    private final int MOVIE_FAVORITED = 1;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem menuFavorite = menu.findItem(R.id.action_favorite);
        if(mFavorite == MOVIE_FAVORITED){
            menuFavorite.setIcon(android.R.drawable.btn_star_big_on);
        }else{
            menuFavorite.setIcon(android.R.drawable.btn_star_big_off);
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
                if(mFavorite == MOVIE_FAVORITED){
                    Utility.updateMovieWithFavorite(getContext(), mMovieId, 0);
                    mFavorite = 0;
                    item.setIcon(android.R.drawable.btn_star_big_off);
                }else{
                    Utility.updateMovieWithFavorite(getContext(), mMovieId, MOVIE_FAVORITED);
                    mFavorite = MOVIE_FAVORITED;
                    item.setIcon(android.R.drawable.btn_star_big_on);
                }

            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for movie data.
        Intent intent = getActivity().getIntent();
        Uri movieURI = intent.getData();

        //TODO need to fetch the movie ID here

        Cursor movieDetailsCursor = getActivity().getContentResolver()
                .query(movieURI, null, null, null, null);

        if (!movieDetailsCursor.moveToFirst()) {
            return null;
        }
        mMovieId = movieDetailsCursor.getInt(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
        mFavorite = movieDetailsCursor.getInt(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_FAVORITE));

        // Set the TextViews loaded in the rootView
        ((TextView) rootView.findViewById(R.id.movie_title)).setText(movieDetailsCursor.getString(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
        ((TextView) rootView.findViewById(R.id.movie_votes)).setText(movieDetailsCursor.getString(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_COUNT))+ " votes");
        ((TextView) rootView.findViewById(R.id.movie_rating)).setText(movieDetailsCursor.getString(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
        ((TextView) rootView.findViewById(R.id.movie_overview)).setText(movieDetailsCursor.getString(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
        ((TextView) rootView.findViewById(R.id.movie_year)).setText(movieDetailsCursor.getString(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));

        // Build the URI path of the poster to be loaded in the Detail activity
        Uri posterUri = Utility.buildFullPosterPath(getString(R.string.poster_size_default), movieDetailsCursor.getString(movieDetailsCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_URL)));
        Picasso.with(rootView.getContext())
                .load(posterUri)
                .into((ImageView) rootView.findViewById(R.id.movie_poster_image));


        return rootView;
    }
}