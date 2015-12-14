package hu.ait.android.fo;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by user on 2015-12-06.
 * Application class to start the connection with Parse
 */
public class FoApplication extends Application {

    public static final String APP_ID =
            "2bsKl0qMBYi6PfJkSU8UR82jw1BFfMD1uSfrbwkl";
    public static final String CLIENT_ID =
            "7p8wt6WIIM4TlFaYnqpAPYl2XGbcgZtivgTKmvDL";

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, APP_ID, CLIENT_ID);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
