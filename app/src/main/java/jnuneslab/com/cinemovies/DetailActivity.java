package jnuneslab.com.cinemovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * Detail Activity responsible for show the details of the movie. Information like: Title, votes, popularity, overview.
 * Created by Walter on 14/09/2015.
 */
public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A detail fragment containing a view.
     */
    public static class DetailFragment extends Fragment {

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

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
}
