package hu.ait.android.fo;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseUser;

import hu.ait.android.fo.adapter.FriendPagerAdapter;
import hu.ait.android.fo.adapter.FriendsAdapter;
import hu.ait.android.fo.adapter.ViewPagerAdapter;
import hu.ait.android.fo.data.User;

public class FriendActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private CoordinatorLayout layoutContent;
    private ViewPager pager;
    private FriendPagerAdapter adapter;
    private TabLayout tabs;
    private CharSequence Titles[] = {"List", "Requests"};
    private int numofTabs = 2;

    private static String myID;
    private String myFirstName;
    private String myLastName;


    public static String getMyID() {
        return myID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        setUpDrawerLayout();

        ParseUser user = ParseUser.getCurrentUser();
        myID = user.getUsername();
        myFirstName = user.getString(User.FIRSTNAME);
        myLastName = user.getString(User.LASTNAME);

        layoutContent = (CoordinatorLayout) findViewById(R.id.friend_content);

        adapter = new FriendPagerAdapter(getSupportFragmentManager(), Titles, numofTabs);

        pager = (ViewPager) findViewById(R.id.friend_container);
        pager.setAdapter(adapter);

        tabs = (TabLayout) findViewById(R.id.friend_tabs);
        tabs.setupWithViewPager(pager);
    }

    private void setUpDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
    }

    //Used to display short snackbar message
    public void showSnackBarMessage(String message) {
        Snackbar.make(layoutContent,
                message,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
            }
        }).show();
    }


}
