package hu.ait.android.fo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hu.ait.android.fo.CreateEventActivity;
import hu.ait.android.fo.EventDetailActivity;
import hu.ait.android.fo.MainActivity;
import hu.ait.android.fo.R;
import hu.ait.android.fo.data.FoEvent;
import hu.ait.android.fo.data.User;

/**
 * Created by user on 2015-11-30.
 * Map Fragment that displays a map
 */
public class Fragment_Map_Tab extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String POSTER_ID = "poster_id";
    public static final String POSTER_FIRSTNAME = "poster_FirstName";
    public static final String POSTER_LASTNAME = "poster_LastName";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String LOC_NAME = "loc_name";
    public static final String LOC_COORD = "loc_coord";
    public static final String TYPE = "type";
    public static final String SUBTYPE = "subtype";
    public static final String GOING = "going";
    public static final String GOING_ID = "going_id";
    public static final String POST = "Post";

    private MapView mMapView;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Bundle mBundle;
    private static Marker marker;
    private Marker fixedMarker;

    private Map<Marker, FoEvent> allMarkersMap;
    private double fixedLat, fixedLng;
    private Date currentDate;


    public static ParseGeoPoint getMyGeoPoint() {
        return myGeoPoint;
    }

    private static ParseGeoPoint myGeoPoint;

    private Activity mActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        currentDate = new Date(System.currentTimeMillis());
        allMarkersMap = new HashMap<Marker, FoEvent>();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        startLocationMonitoring();
        View inflatedView = inflater.inflate(R.layout.fragment_map, container, false);

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView = (MapView) inflatedView.findViewById(R.id.map);
        mMapView.onCreate(mBundle);

        setUpMapIfNeeded(inflatedView);

        if (marker != null) {
            Marker tempMarker = mMap.addMarker(new MarkerOptions()
                    .position(marker.getPosition())
                    .draggable(true).visible(true)
                    .title(marker.getTitle()));
            marker.remove();
            marker = tempMarker;
        }

        setOnClickListeners();


        if (this.getActivity() instanceof MainActivity) {
            getPosts();
        } else if (this.getActivity() instanceof CreateEventActivity) {

        } else if (this.getActivity() instanceof EventDetailActivity) {

        }


        return inflatedView;
    }

    private void setOnClickListeners() {
        setInfoWindowClickListener();
        setOnMapLongClickListener();
        setOnMarkerClickListener();
    }

    private void setOnMarkerClickListener() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(fixedMarker)) {
                    showAlertDialog();
                    return true;
                }

                return false;
            }
        });
    }

    private void setOnMapLongClickListener() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng clickPoint) {

                if (marker != null) {
                    marker.remove();
                }
                marker = mMap.addMarker(new MarkerOptions()
                        .position(clickPoint)
                        .draggable(true).visible(true)
                        .title("Temp"));
            }
        });
    }

    private void setInfoWindowClickListener() {
        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                FoEvent clickedEvent = allMarkersMap.get(marker);
                ((MainActivity) getActivity()).viewEventDetail(clickedEvent);


            }
        });
    }

    public void moveCamera(double lat, double lng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
    }

    public void placeMarker(double lat, double lng, String title, int type) {
        fixedMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .draggable(false)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(getAppropriateMarkerColor(type))));
        fixedLat = lat;
        fixedLng = lng;
    }

    public void addMarker(double lat, double lng) {
        marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .draggable(true).visible(true)
                .title("Temp"));
    }

    private void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(mActivity.getString(R.string.note));
        alertDialog.setMessage(mActivity.getString(R.string.direction_map));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mActivity.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String query = "google.navigation:q=" + fixedLat + "," + fixedLng;
                        Uri uri = Uri.parse(query);
                        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW,
                                uri);
                        startActivity(mapIntent);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mActivity.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
    }

    // If necessary, set up the map
    // Add marker to the city of interest
    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = mMapView.getMap();
            mMap.setMyLocationEnabled(true);

            try {
                Criteria cri = new Criteria();
                String provider = locationManager.getBestProvider(cri, true);

                Location myLocation = locationManager.getLastKnownLocation(provider);
                if (myLocation != null) {
                    double myLat = myLocation.getLatitude();
                    double myLong = myLocation.getLongitude();
                    LatLng myLoc = new LatLng(myLat, myLong);
                    myGeoPoint = new ParseGeoPoint(myLat, myLong);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 15));
                }
            } catch (SecurityException e) {
                e.printStackTrace();

            }


            if (mMap != null) {
                Bundle b = getActivity().getIntent().getExtras();
                if (b != null) {

                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationMonitoring();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    private void startLocationMonitoring() {
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    private void stopLocationMonitoring() {
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(getActivity(), provider + ", status : " + status, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getActivity(), provider + " enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), provider + " disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    public static Marker getMarker() {
        return marker;
    }


    public void getPosts() {
        mMap.clear();
        allMarkersMap.clear();

        ParseQuery<ParseObject> pq_friends = ParseQuery.getQuery(User.FRIENDS);
        pq_friends.whereEqualTo(User.USER_ID, MainActivity.getMyID());
        pq_friends.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    try {
                        JSONArray friendArray = objects.get(0).getJSONArray(User.FRIEND_LIST);
                        if (friendArray != null) {
                            ArrayList<String> friendNames = (ArrayList<String>) toList(friendArray);
                            getAllPosts(friendNames);
                        }else {
                            getAllPosts(new ArrayList<String>());
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                } else {

                }
            }
        });
    }

    private void getAllPosts(ArrayList<String> friendNames) {
        friendNames.add(MainActivity.getMyID());
        ParseQuery<ParseObject> pq = getParseObjectParseQuery(friendNames);
        if (pq == null) return;

        // Set Query
        pq.setLimit(
                ((MainActivity) mActivity).getMaxResultCountPreference());
        pq.whereGreaterThan(Fragment_Map_Tab.END_DATE, currentDate);
        pq.whereWithinKilometers(LOC_COORD, myGeoPoint,
                ((MainActivity) mActivity).getRadiusPreference());


        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects,
                             ParseException e) {
                StringBuilder sb = new StringBuilder();
                for (ParseObject post : parseObjects) {
                    ParseGeoPoint point = post.getParseGeoPoint(LOC_COORD);

                    Marker tempMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(point.getLatitude(), point.getLongitude()))
                            .draggable(false)
                            .title(post.getString(TITLE))
                            .icon(BitmapDescriptorFactory.defaultMarker(getAppropriateMarkerColor(post.getInt(TYPE)))));

                    FoEvent tempEvent = getFoEvent(post, point);

                    try {
                        JSONArray goingArray = post.getJSONArray(Fragment_Map_Tab.GOING);
                        JSONArray goingIDArray = post.getJSONArray(Fragment_Map_Tab.GOING_ID);
                        if (goingArray != null) {
                            ArrayList<String> goingList = (ArrayList<String>) toList(goingArray);
                            tempEvent.setGoing(goingList);
                        }
                        if (goingIDArray != null) {
                            ArrayList<String> goingIDList = (ArrayList<String>) toList(goingIDArray);
                            tempEvent.setGoing_ID(goingIDList);
                        }

                    } catch (JSONException f) {
                        f.printStackTrace();
                    }


                    allMarkersMap.put(tempMarker, tempEvent);
                }
            }
        });
    }

    @Nullable
    private ParseQuery<ParseObject> getParseObjectParseQuery(ArrayList<String> friendNames) {
        ParseQuery<ParseObject> pq_Public = ParseQuery.getQuery(POST);
        ParseQuery<ParseObject> pq_Friends = ParseQuery.getQuery(POST);
        ParseQuery<ParseObject> pq_Group = ParseQuery.getQuery(POST);
        ParseQuery<ParseObject> pq_Private = ParseQuery.getQuery(POST);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        boolean[] filter = ((MainActivity) mActivity).getFilterPreferences();
        if(filter[0]){
            pq_Public.whereEqualTo(TYPE, 0);
            queries.add(pq_Public);
        }
        if(filter[1]){
            pq_Friends.whereEqualTo(TYPE, 1);
            pq_Friends.whereContainedIn(POSTER_ID, friendNames);
            queries.add(pq_Friends);
        }
        if(filter[2]){
            pq_Group.whereEqualTo(TYPE, 2);
            queries.add(pq_Group);
        }
        if(filter[3]){
            pq_Private.whereEqualTo(TYPE, 2);
            queries.add(pq_Private);
        }

        if(queries.size() == 0){
            Log.d("LOGLOG", "Zero, returning");
            return null;
        }


        ParseQuery<ParseObject> pq = ParseQuery.or(queries);
        return pq;
    }

    @NonNull
    private FoEvent getFoEvent(ParseObject post, ParseGeoPoint point) {
        FoEvent tempEvent = new FoEvent();
        tempEvent.setTitle(post.getString(TITLE));
        tempEvent.setLat(point.getLatitude());
        tempEvent.setLng(point.getLongitude());
        tempEvent.setLocation(post.getString(LOC_NAME));
        tempEvent.setDescription(post.getString(DESCRIPTION));
        tempEvent.setStartTime(post.getDate(START_DATE));
        tempEvent.setEndTime(post.getDate(END_DATE));
        tempEvent.setType(post.getInt(TYPE));
        tempEvent.setPoster_id(post.getString(POSTER_ID));
        tempEvent.setPoster_FirstName(post.getString(POSTER_FIRSTNAME));
        tempEvent.setPoster_LastName(post.getString(POSTER_LASTNAME));
        tempEvent.setObjectId(post.getObjectId());
        return tempEvent;
    }


    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These a both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;
        private SimpleDateFormat dateTimeFormatter;

        CustomInfoWindowAdapter() {

            dateTimeFormatter = new SimpleDateFormat("M/dd E h:mm a");

            mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }


        @Override
        public View getInfoWindow(Marker marker) {
            if (marker.equals(Fragment_Map_Tab.getMarker())) {
                return null;
            } else {
                render(marker, mWindow);
                return mWindow;
            }
        }


        private void render(final Marker marker, View view) {
            final ImageView iv_InfoWindow_ProfilePic = (ImageView) view.findViewById(R.id.iv_InfoWindow_ProfilePic);
            TextView tv_InfoWindow_Title = (TextView) view.findViewById(R.id.tv_InfoWindow_Title);
            TextView tv_InfoWindow_Time = (TextView) view.findViewById(R.id.tv_InfoWindow_Time);
            TextView tv_InfoWindow_Location = (TextView) view.findViewById(R.id.tv_InfoWindow_Location);
            TextView tv_InfoWindow_Name = (TextView) view.findViewById(R.id.tv_InfoWindow_Name);


            final FoEvent clickedEvent = allMarkersMap.get(marker);

            tv_InfoWindow_Title.setText(clickedEvent.getTitle());
            tv_InfoWindow_Time.setText(clickedEvent.getStartTime().toString());
            tv_InfoWindow_Location.setText(clickedEvent.getLocation());

            if (clickedEvent.getPoster_FirstName() == null || clickedEvent.getPoster_LastName() == null) {
                tv_InfoWindow_Name.setText(clickedEvent.getPoster_id());
            } else {
                tv_InfoWindow_Name.setText(clickedEvent.getPoster_FirstName()
                        + " " + clickedEvent.getPoster_LastName());
            }
            String title = clickedEvent.getTitle();

            ForegroundColorSpan color = new ForegroundColorSpan(getAppropriateColor(clickedEvent.getType()));

            setTitle(tv_InfoWindow_Title, title, color);

            setDate(tv_InfoWindow_Time, clickedEvent);

            setLocation(tv_InfoWindow_Location, clickedEvent, title);

            String poster_Name = setName(clickedEvent);

            setTitle(tv_InfoWindow_Name, poster_Name, new ForegroundColorSpan(Color.BLACK));

            iv_InfoWindow_ProfilePic.setImageResource(R.drawable.logo);

           /* // Search the user who posted the query
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(User.USER_ID, clickedEvent.getPoster_id());
            Log.d("LOGLOG", "Title2 : " + clickedEvent.getTitle() + "  ID : " + clickedEvent.getPoster_id());
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        setImages(objects.get(0),iv_InfoWindow_ProfilePic, marker);
                    }
                }
            });*/
        }

        private String setName(FoEvent clickedEvent) {
            String poster_Name;
            if (clickedEvent.getPoster_FirstName() == null) {
                poster_Name = clickedEvent.getPoster_id();
            } else {
                poster_Name = clickedEvent.getPoster_FirstName() + " " + clickedEvent.getPoster_LastName();
            }
            return poster_Name;
        }

        private void setLocation(TextView tv_InfoWindow_Location, FoEvent clickedEvent, String title) {
            String location = clickedEvent.getLocation();
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString locationText = new SpannableString(location);
                locationText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, locationText.length(), 0);
                tv_InfoWindow_Location.setText(locationText);
            } else {
                tv_InfoWindow_Location.setText("");
            }
        }

        private void setDate(TextView tv_InfoWindow_Time, FoEvent clickedEvent) {
            Date startTime = clickedEvent.getStartTime();
            Date endTime = clickedEvent.getEndTime();
            if (startTime != null && endTime != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString startTimeText = new SpannableString(
                        dateTimeFormatter.format(startTime) + "\n ~ "
                                + dateTimeFormatter.format(endTime));
                startTimeText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, startTimeText.length(), 0);
                tv_InfoWindow_Time.setText(startTimeText);
            } else {
                tv_InfoWindow_Time.setText("");
            }
        }

        private void setTitle(TextView tv_InfoWindow_Title, String title, ForegroundColorSpan color) {
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(color, 0, titleText.length(), 0);
                tv_InfoWindow_Title.setText(titleText);
            } else {
                tv_InfoWindow_Title.setText("");
            }
        }

        /*// Set the profile pictures for each row
        private void setImages(ParseUser user, final ImageView iv_InfoWindow_ProfilePic,
                               final Marker marker) {
            ParseFile parseFile = user.getParseFile(User.PIC);
            if (parseFile != null) {
                parseFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            iv_InfoWindow_ProfilePic.setImageBitmap(bmp);
                        } else {
                            iv_InfoWindow_ProfilePic.setImageResource(R.drawable.logo);
                        }

                        if(marker != null && marker.isInfoWindowShown())
                            marker.showInfoWindow();
                    }
                });
            }
        }*/

    }



    public void applyFilter() {
        if (getActivity() != null)
            getPosts();
    }


    //Get appropriate color
    public static int getAppropriateColor(int type) {
        switch (type) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.YELLOW;
            default:
                return Color.CYAN;
        }
    }

    //Get appropriate color
    private float getAppropriateMarkerColor(int type) {
        switch (type) {
            case 0:
                return BitmapDescriptorFactory.HUE_RED;
            case 1:
                return BitmapDescriptorFactory.HUE_BLUE;
            case 2:
                return BitmapDescriptorFactory.HUE_GREEN;
            case 3:
                return BitmapDescriptorFactory.HUE_YELLOW;
            default:
                return BitmapDescriptorFactory.HUE_CYAN;
        }
    }


    // Convert JSonList to list
    private List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        int size = array.length();
        for (int i = 0; i < size; i++) {
            list.add(array.get(i));
        }
        return list;
    }


}
