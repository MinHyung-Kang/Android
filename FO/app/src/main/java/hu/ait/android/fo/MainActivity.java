package hu.ait.android.fo;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import hu.ait.android.fo.adapter.ViewPagerAdapter;
import hu.ait.android.fo.data.FoEvent;
import hu.ait.android.fo.data.User;
import hu.ait.android.fo.fragments.FilterDialogFragment;
import hu.ait.android.fo.fragments.Fragment_List_Tab;
import hu.ait.android.fo.fragments.Fragment_Map_Tab;
import hu.ait.android.fo.fragments.Fragment_My_Tab;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1385;
    public static final String CLICKED_EVENT = "CLICKED_EVENT";
    public static final String FILTER_NAME = "FilterSettings";
    public static final String FILTER_CHECK = "FilterCheck";
    public static final String FILTER_SIZE = "FilterSize";
    public static final String SETTING_NAME = "Settings";
    public static final String RADIUS_SETTING = "Radius Setting";
    public static final String MAX_RESULT_COUNT = "Max_Result_Count";
    public static final int TYPE_SIZE = 4;

    private static String[] eventTypes;

    private DrawerLayout drawerLayout;
    private CoordinatorLayout layoutContent;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private TabLayout tabs;
    private CharSequence Titles[] = {"Map", "List", "My"};

    private int numofTabs = 3;

    private static String myID;
    private String myFirstName;
    private String myLastName;


    public static String getMyID(){
        return myID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseUser user = ParseUser.getCurrentUser();
        myID = user.getUsername();
        myFirstName = user.getString(User.FIRSTNAME);
        myLastName = user.getString(User.LASTNAME);

        layoutContent = (CoordinatorLayout) findViewById(R.id.main_content);

        setUpDrawerLayout();
        setUpNavigationView();
        setUpViewPager();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showCreateEvent = new Intent(MainActivity.this, CreateEventActivity.class);
                startActivity(showCreateEvent);
            }
        });

        initializeFilterSetting();
        eventTypes = getResources().getStringArray(R.array.eventType_array);

    }

    private void setUpViewPager() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, numofTabs);

        // Set up the ViewPager with the sections adapter.
        pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(adapter);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    //If the setting was never saved, initialize it
    private void initializeFilterSetting() {
        SharedPreferences sp = getSharedPreferences(MainActivity.FILTER_NAME, MODE_PRIVATE);
        if (!sp.contains(FILTER_CHECK + "0")) {
            boolean[] checkedItems = new boolean[TYPE_SIZE];
            for (int i = 0; i < TYPE_SIZE; i++) {
                checkedItems[i] = true;
            }
            savePreference(checkedItems);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        //TODO : implement searching
        /*if (menuItem.getItemId() == R.id.action_search) {
        Layout :
            <item
        android:id="@+id/action_search"
        android:icon="@android:drawable/ic_search_category_default"
        android:orderInCategory="301"
        android:title="Search..."
        app:actionViewClass="android.support.v7.widget.SearchView"
        app:showAsAction="ifRoom|collapseActionView" />
        } else */
        if (menuItem.getItemId() == R.id.action_refresh) {
            refresh();

        } else if (menuItem.getItemId() == R.id.action_filter) {
            FragmentManager manager = getSupportFragmentManager();
            FilterDialogFragment filterDialogFragment = new FilterDialogFragment();
            filterDialogFragment.show(manager, "fragment_filter_result");
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void refresh() {
        //TODO : Error caused when refresh clicked outside map
        Fragment_Map_Tab mapFragment = adapter.getMapFragment();
        if (mapFragment != null)
            mapFragment.getPosts();
        Fragment_List_Tab listFragment = adapter.getListFragment();
        if (listFragment != null)
            listFragment.applyFilter();
        Fragment_My_Tab myFragment = adapter.getMyFragment();
        if (myFragment != null)
            myFragment.applyFilter();
    }

    private void setUpDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setUpNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);

        setUpHeader(navigationView);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.action_account:
                                actionAccount();
                                break;
                            case R.id.action_friends:
                                actionFriends();
                                break;
                            case R.id.action_options:
                                actionOptions();
                                break;
                            case R.id.action_about:
                                actionAbout();
                                break;
                            case R.id.action_help:
                                actionHelp();
                                break;
                            case R.id.action_logout:
                                logout();
                                break;
                        }

                        return false;
                    }
                });
    }

    private void actionHelp() {
        drawerLayout.closeDrawer(GravityCompat.START);
        showDialogMsg(getResources().getString(R.string.title_help),
                getResources().getString(R.string.text_help));
    }

    private void actionAbout() {
        drawerLayout.closeDrawer(GravityCompat.START);
        showDialogMsg(getResources().getString(R.string.title_about),
                getResources().getString(R.string.text_about));
    }

    private void actionOptions() {
        showSnackBarMessage(getString(R.string.options));
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void actionFriends() {
        Intent showFriends = new Intent();
        showFriends.setClass(MainActivity.this, FriendActivity.class);
        startActivity(showFriends);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void actionAccount() {
        Intent showAccount = new Intent();
        showAccount.setClass(MainActivity.this, AccountActivity.class);
        startActivity(showAccount);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    //Sets up the header accordingly
    private void setUpHeader(NavigationView navigationView) {
        View headerView = navigationView.inflateHeaderView(R.layout.drawer_header);
        TextView tv_Drawer_Email = (TextView) headerView.findViewById(R.id.tv_Drawer_Email);
        TextView tv_Drawer_Name = (TextView) headerView.findViewById(R.id.tv_Drawer_Name);
        final com.makeramen.roundedimageview.RoundedImageView profile_picture =
                (com.makeramen.roundedimageview.RoundedImageView)headerView.findViewById(R.id.profile_picture);

        ParseUser user = ParseUser.getCurrentUser();
        tv_Drawer_Email.setText(user.getEmail());
        tv_Drawer_Name.setText(user.getString(User.FIRSTNAME) + " " + user.getString(User.LASTNAME));

        try {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            final ParseUser userOnline = query.get(user.getObjectId());
            ParseFile parseFile = userOnline.getParseFile(User.PIC);

            if (parseFile != null) {
                parseFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            profile_picture.setImageBitmap(bmp);
                        } else {
                            profile_picture.setImageResource(R.drawable.logo);
                        }
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    //Used to display short snackbar message
    public void showSnackBarMessage(String message) {
        Snackbar.make(layoutContent,
                message,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
            }
        }).show();
    }


    public void viewEventDetail(FoEvent event) {
        Intent viewDetail = new Intent();
        viewDetail.setClass(MainActivity.this, EventDetailActivity.class);
        viewDetail.putExtra(CLICKED_EVENT, event);
        startActivity(viewDetail);
    }

    public void saveMaxResultCountPreference(int resultMax) {
        SharedPreferences sp = getSharedPreferences(SETTING_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(MAX_RESULT_COUNT, resultMax);
        editor.commit();
    }

    public int getMaxResultCountPreference() {
        SharedPreferences sp = getSharedPreferences(SETTING_NAME, MODE_PRIVATE);
        return sp.getInt(MAX_RESULT_COUNT, 200);
    }

    public void saveRadiusPreference(int kilometer) {
        SharedPreferences sp = getSharedPreferences(SETTING_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(RADIUS_SETTING, kilometer);
        editor.commit();
    }

    public int getRadiusPreference() {
        SharedPreferences sp = getSharedPreferences(SETTING_NAME, MODE_PRIVATE);
        return sp.getInt(RADIUS_SETTING, 10);
    }


    // Saves the filter preferences to SharedPreferences
    // Called from FilterDialogFragment
    public void savePreference(boolean[] checkedItems) {
        SharedPreferences sp = getSharedPreferences(MainActivity.FILTER_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(FILTER_SIZE, checkedItems.length);
        for (int i = 0; i < checkedItems.length; i++) {
            String key = FILTER_CHECK + i;
            editor.putBoolean(key, checkedItems[i]);
        }
        editor.commit();
    }

    // Get filter preferences from SharedPrefences
    // Called from FilterDialogFragment
    public boolean[] getFilterPreferences() {
        SharedPreferences sp = getSharedPreferences(MainActivity.FILTER_NAME, MODE_PRIVATE);
        int size = sp.getInt(FILTER_SIZE, 0);
        boolean[] checkedItems = new boolean[size];
        for (int i = 0; i < size; i++) {
            String key = FILTER_CHECK + i;
            checkedItems[i] = sp.getBoolean(key, false);
        }

        return checkedItems;
    }

    //When filter value is changed, apply the change to available fragment
    public void applyPreference() {
        Fragment_Map_Tab mapFragment = adapter.getMapFragment();
        Fragment_List_Tab listFragment = adapter.getListFragment();
        Fragment_My_Tab myFragment = adapter.getMyFragment();
        if (mapFragment != null)
            mapFragment.applyFilter();
        if (listFragment != null)
            listFragment.applyFilter();
        if (myFragment != null)
            myFragment.applyFilter();

    }

    public static String[] getEventTypeNames() {
        return eventTypes;
    }

    //Create a new dialog that informs user about the program
    private void showDialogMsg(String title, String msg) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton(R.string.ok, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    // Logs out the user
    private void logout() {
        Toast.makeText(MainActivity.this, R.string.logout_success,
                Toast.LENGTH_SHORT).show();
        ParseUser.logOut();
        Intent goLogin = new Intent();
        goLogin.setClass(MainActivity.this, LoginActivity.class);
        goLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goLogin);
    }
}
