package jnuneslab.com.cinemovies.sync;

/**
 * Created by Walter on 30/11/2015.
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class MovieAuthService extends Service {
    private MovieAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new MovieAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
