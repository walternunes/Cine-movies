package jnuneslab.com.cinemovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import jnuneslab.com.cinemovies.R;
import jnuneslab.com.cinemovies.data.MovieContract;
import jnuneslab.com.cinemovies.service.FetchMovieTask;

/**
 * Sync adapter scheduled to auto sync the contents with the api
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    // Time in seconds to sync
    public static final int SYNC_INTERVAL = 60*60*3; // 10 hours

    // Variable name used in the sync bundle
    private static final String NUMBER_PAGE_REQUEST = "request";

    // Task used to flag before try to fetch a new page if the previous is still runnig
    private FetchMovieTask mMovieTask;

    /**
     * Default constructor
     */
    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Method responsible for prepare the request to fetch the movies
     * @param context - Context of the application
     * @param isFullRequest - Represents a full request from page 0 or the next page
     */
    public static void syncNextPage(Context context, boolean isFullRequest) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int numPage = 0;

        // create the bundle to use in sync
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        // in case to fetch the next number, increment the counter
        if(!isFullRequest) {
            numPage = sharedPref.getInt(context.getString(R.string.pref_page_number_key), 0) + 1;
        }

        // Add the current page number to fetch
        bundle.putInt(NUMBER_PAGE_REQUEST, numPage);

        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);

    }


    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        // Configure and start periodic sync
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_INTERVAL/2);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncNextPage(context, true);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Enable inexact timers in periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        int numPage = extras.getInt(NUMBER_PAGE_REQUEST);

        // Check if the previous request was finished in case that of a next page request
        if (numPage > 0 && (mMovieTask == null || mMovieTask.getStatus() == AsyncTask.Status.RUNNING )) {
            return;
        }

        // In case of full request (numPage = 0), delete old data
        if(numPage == 0){
            getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    "-1", null);
        }

        // Start the task to fetch the movie
        mMovieTask = (FetchMovieTask) new FetchMovieTask(getContext()).execute(numPage + 1);

        // Write the current page number into shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getContext().getString(R.string.pref_page_number_key), numPage);
        editor.commit();

    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        mMovieTask = null;
    }
}