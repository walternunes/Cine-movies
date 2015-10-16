package jnuneslab.com.cinemovies;

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
            if (intent != null && intent.hasExtra(Movie.EXTRA_MOVIE_BUNDLE)) {
                Movie movie = new Movie(intent.getBundleExtra(Movie.EXTRA_MOVIE_BUNDLE));

                // Set the TextViews loaded in the rootView
                ((TextView) rootView.findViewById(R.id.movie_title)).setText(movie.getTitle());
                ((TextView) rootView.findViewById(R.id.movie_votes)).setText(movie.getVote_count() + " votes");
                ((TextView) rootView.findViewById(R.id.movie_rating)).setText(movie.getVote_average().toString());
                ((TextView) rootView.findViewById(R.id.movie_overview)).setText(movie.getOverview());
                ((TextView) rootView.findViewById(R.id.movie_year)).setText(movie.getYear());

                // Build the URI path of the poster to be loaded in the Detail activity
                Uri posterUri = Utility.buildFullPosterPath(getString(R.string.poster_size_default),movie.getPoster_path());
                Picasso.with(rootView.getContext())
                        .load(posterUri)
                        .into((ImageView) rootView.findViewById(R.id.movie_poster_image));
            }

            return rootView;
        }
    }
}
