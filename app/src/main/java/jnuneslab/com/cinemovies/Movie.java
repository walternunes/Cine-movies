package jnuneslab.com.cinemovies;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import jnuneslab.com.cinemovies.data.MovieContract.MovieEntry;

/**
 * Movie object that contains all the movie information
 * Created by Walter on 14/09/2015.
 */
public class Movie {

    private final String TAG = Movie.class.getSimpleName();

    /**
     * Keys of the movie attributes used to populate the bundle
     */
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_POSTER_PATH = "poster_path";
    public static final String KEY_VOTE_AVERAGE = "vote_average";
    public static final String KEY_VOTE_COUNT = "vote_count";
    public static final String KEY_POPULARITY = "popularity";
    public static final String KEY_RELEASE_DATE = "release_date";
    public static final String KEY_LANGUAGE = "original_language";
    public static final String KEY_FAVORITE = "favorite";
    public static final String EXTRA_MOVIE_BUNDLE = "jnuneslab.com.cinemovies.EXTRA_MOVIE_BUNDLE";

    /**
     * Attributes of the movie retrieved by the API
     */
    private int id;
    private int vote_count;
    private Double vote_average;
    private Double popularity;
    private String title;
    private String overview;
    private String poster_path;
    private String releaseDate;
    private String language;
    private String favorite = "";


    /**
     * Constructor used to populate the attributes of the movie
     */
    Movie(int id, int vote_count, double vote_average, double popularity, String title, String overview, String poster_path, String releaseDate, String language) {
        this.id = id;
        this.vote_count = vote_count;
        this.vote_average = vote_average;
        this.popularity = popularity;
        this.title = title;
        this.overview = overview;
        this.poster_path = poster_path;
        this.releaseDate = releaseDate;
        this.language = language;
    }

    /**
     * Constructor that receives a JSONObject and fallback to the next constructor to populate the movie attributes
     */
    Movie(JSONObject movieObject) throws JSONException {
        this(movieObject.getInt(KEY_ID), movieObject.getInt(KEY_VOTE_COUNT), movieObject.getDouble(KEY_VOTE_AVERAGE), movieObject.getDouble(KEY_POPULARITY), movieObject.getString(KEY_TITLE), movieObject.getString(KEY_OVERVIEW), movieObject.getString(KEY_POSTER_PATH), movieObject.getString(KEY_RELEASE_DATE), movieObject.getString(KEY_LANGUAGE));
    }

    /**
     * Constructor that receives a Bundle and fallback to the next constructor to populate the movie attributes
     */
    Movie(Bundle movieBundle) {
        this(movieBundle.getInt(KEY_ID), movieBundle.getInt(KEY_VOTE_COUNT), movieBundle.getDouble(KEY_VOTE_AVERAGE), movieBundle.getDouble(KEY_POPULARITY), movieBundle.getString(KEY_TITLE), movieBundle.getString(KEY_OVERVIEW), movieBundle.getString(KEY_POSTER_PATH), movieBundle.getString(KEY_RELEASE_DATE), movieBundle.getString(KEY_LANGUAGE));
    }

    /**
     * Build the image poster path URI
     *
     * @param size - attribute that represents the size of the poster
     * @return return the full path of the poster image
     */
    public Uri buildFullPosterPath(String size) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(poster_path)
                .build();

        return builtUri;
    }

    /**
     * Load the attributes of the movies into a bundle to be passed by an intent
     *
     * @return - Bundle with all attributes of the movie
     */
    public Bundle loadMovieBundle() {
        Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_ID, id);
        resultBundle.putInt(KEY_VOTE_COUNT, vote_count);
        resultBundle.putDouble(KEY_VOTE_AVERAGE, vote_average);
        resultBundle.putDouble(KEY_POPULARITY, popularity);
        resultBundle.putString(KEY_TITLE, title);
        resultBundle.putString(KEY_OVERVIEW, overview);
        resultBundle.putString(KEY_POSTER_PATH, poster_path);
        resultBundle.putString(KEY_RELEASE_DATE, releaseDate);
        resultBundle.putString(KEY_LANGUAGE, language);
        return resultBundle;

    }

    public ContentValues loadMovieContent(){
        ContentValues movieValues = new ContentValues();

        movieValues.put(MovieEntry.COLUMN_MOVIE_ID, id);
        movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, vote_count);
        movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
        movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
        movieValues.put(MovieEntry.COLUMN_TITLE, title);
        movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
        movieValues.put(MovieEntry.COLUMN_POSTER_URL, poster_path);
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        movieValues.put(MovieEntry.COLUMN_LANGUAGE, language);
        movieValues.put(MovieEntry.COLUMN_FAVORITE, favorite);

        return movieValues;
    }

    /**
     * Parse the release date to return only the year attribute
     *
     * @return - Year that the movie was released
     */
    public String getYear() {
        return this.releaseDate.substring(0, 4);
    }


    /**
     * Getters and Setters
     */

    public String getFavorite() { return favorite;}

    public void setFavorite(String favorite){ this.favorite = favorite; }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }

    public Double getVote_average() {
        return vote_average;
    }

    public void setVote_average(Double vote_average) { this.vote_average = vote_average; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


}
