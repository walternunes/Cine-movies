package jnuneslab.com.cinemovies.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract;

/**
 * ReviewAdapter used to load the reviews into the detail fragment
 */
public class ReviewAdapter extends CursorAdapter {
    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView authorTextView = (TextView) view.findViewById(R.id.comment_author);
        TextView contentTextView = (TextView) view.findViewById(R.id.comment_content);

        int authorColumnIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR);
        int contentColumnIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT);

        String author = cursor.getString(authorColumnIndex);
        String content = cursor.getString(contentColumnIndex);

        authorTextView.setText(author);
        contentTextView.setText(content);
    }
}