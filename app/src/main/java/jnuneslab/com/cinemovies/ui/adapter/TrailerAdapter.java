package jnuneslab.com.cinemovies.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * TrailerAdapter used to load the trailers into the detail fragment
 */

public class TrailerAdapter extends CursorAdapter {

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.trailer_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView trailerTitleTextView = (TextView) view.findViewById(R.id.list_item_trailer_title);
        int trailerTitleColumn = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_TITLE);
        String trailerTitle = cursor.getString(trailerTitleColumn);
        trailerTitleTextView.setText(trailerTitle);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int youtubeKeyColumn = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_YOUTUBE_KEY);
                String youtubeKey = cursor.getString(youtubeKeyColumn);
                Uri videoUri = Uri.parse("https://youtu.be/" + youtubeKey);

                Intent playTrailer = new Intent(Intent.ACTION_VIEW, videoUri);
                context.startActivity(playTrailer);
            }
        });

    }
}