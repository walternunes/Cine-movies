package jnuneslab.com.cinemovies.model;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import jnuneslab.com.cinemovies.data.MovieContract.MovieEntry;

/**
 * Movie object that contains all movie information retrieved by API
 */
@SuppressWarnings("unused")
public class Movie {

    /**
     * Movie attributes contentValues keys
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
     * Movie attributes retrieved by the API
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
    private int favorite;


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
     * Constructor that receives a JSONObject and fallback to the default constructor to populate the movie attributes
     */
    public Movie(JSONObject movieObject) throws JSONException {
        this(movieObject.getInt(KEY_ID), movieObject.getInt(KEY_VOTE_COUNT), movieObject.getDouble(KEY_VOTE_AVERAGE), movieObject.getDouble(KEY_POPULARITY), movieObject.getString(KEY_TITLE), movieObject.getString(KEY_OVERVIEW), movieObject.getString(KEY_POSTER_PATH), movieObject.getString(KEY_RELEASE_DATE), movieObject.getString(KEY_LANGUAGE));
    }

    /**
     * Load the movie attributes into the content values
     *
     * @return - ContentValues with all attributes of the movie
     */
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

        return movieValues;
    }

    /**
     * Getters and Setters
     */

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