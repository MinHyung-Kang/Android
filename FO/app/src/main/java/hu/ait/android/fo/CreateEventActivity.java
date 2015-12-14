package hu.ait.android.fo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.fo.data.FoEvent;
import hu.ait.android.fo.data.User;
import hu.ait.android.fo.fragments.Fragment_List_Tab;
import hu.ait.android.fo.fragments.Fragment_Map_Tab;

public class CreateEventActivity extends AppCompatActivity {

    private Spinner spinnerEventType;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat dateTimeFormatter;
    private boolean editMode = false;

    private Fragment_Map_Tab myMap;

    private FoEvent editEvent;

    private String myID;
    private String myFirstName;
    private String myLastName;

    public static final long HOUR = 3600 * 1000;

    @Bind(R.id.tv_StartDate)
    TextView tv_StartDate;

    @Bind(R.id.tv_StartTime)
    TextView tv_StartTime;

    @Bind(R.id.tv_EndDate)
    TextView tv_EndDate;

    @Bind(R.id.tv_EndTime)
    TextView tv_EndTime;

    @Bind(R.id.et_Description)
    TextView et_Description;

    @Bind(R.id.et_Title)
    EditText et_Title;

    @Bind(R.id.et_Location)
    EditText et_Location;

    @Bind(R.id.btn_Post)
    Button btn_post;

    @Bind(R.id.sv_ScrollView)
    ScrollView sv_ScrollView;

    @Bind(R.id.iv_Transparent)
    ImageView iv_Transparent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        ButterKnife.bind(this);


        ParseUser user = ParseUser.getCurrentUser();
        myID = user.getUsername();
        myFirstName = user.getString(User.FIRSTNAME);
        myLastName = user.getString(User.LASTNAME);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatter.setTimeZone(TimeZone.getDefault());
        dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        timeFormatter = new SimpleDateFormat("HH:mm");
        timeFormatter.setTimeZone(TimeZone.getDefault());

        spinnerEventType = (Spinner) findViewById(R.id.spinner_EventType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eventType_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
        myMap = (Fragment_Map_Tab)getSupportFragmentManager().findFragmentById(R.id.create_mapFragment);

        interceptMotionFromScrollView();

        setInitialDates();

        retrieveEditEvent();

    }

    // Retrieve edit event if it exists
    private void retrieveEditEvent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            editEvent = (FoEvent) bundle.getSerializable(EventDetailActivity.EDIT_EVENT);
            editMode = true;
            setEditContent();
        }
    }

    //Setup touch listener for transparent image to intercept any touches
    private void interceptMotionFromScrollView() {
        iv_Transparent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        sv_ScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        sv_ScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        sv_ScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }


    //GIven the editing contents, fill the values with previous values
    private void setEditContent() {
        et_Title.setText(editEvent.getTitle());
        spinnerEventType.setSelection(editEvent.getType());
        tv_StartDate.setText(dateFormatter.format(editEvent.getStartTime()));
        tv_StartTime.setText(timeFormatter.format(editEvent.getStartTime()));
        tv_EndDate.setText(dateFormatter.format(editEvent.getEndTime()));
        tv_EndTime.setText(timeFormatter.format(editEvent.getEndTime()));
        et_Location.setText(editEvent.getLocation());
        et_Description.setText(editEvent.getDescription());

        Marker marker = Fragment_Map_Tab.getMarker();
        if (marker == null) {
            myMap.addMarker(editEvent.getLat(), editEvent.getLng());
        } else {
            marker.setPosition(new LatLng(editEvent.getLat(), editEvent.getLng()));
        }

    }


    private void setInitialDates() {
        Date startDate = Calendar.getInstance().getTime();
        tv_StartDate.setText(dateFormatter.format(startDate));
        tv_StartTime.setText(timeFormatter.format(startDate));

        Date endDate = new Date(startDate.getTime() + HOUR);
        tv_EndDate.setText(dateFormatter.format(endDate));
        tv_EndTime.setText(timeFormatter.format(endDate));
    }

    //Get the date picked by the user
    @OnClick({R.id.tv_StartDate, R.id.tv_EndDate})
    public void getDate(final View v) {

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                if (v.getId() == R.id.tv_StartDate) {
                    tv_StartDate.setText(dateFormatter.format(newDate.getTime()));
                } else if (v.getId() == R.id.tv_EndDate)
                    tv_EndDate.setText(dateFormatter.format(newDate.getTime()));
                adjustTime();
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    // Get the time chosen by the user
    @OnClick({R.id.tv_StartTime, R.id.tv_EndTime})
    public void getTime(final View v) {

        Calendar newCalendar = Calendar.getInstance();
        int hour = newCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = newCalendar.get(Calendar.MINUTE);

        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            public void onTimeSet(TimePicker view, int selectedHour, int selectedMin) {
                if (v.getId() == R.id.tv_StartTime)
                    tv_StartTime.setText(String.format("%02d", selectedHour) + ":" + selectedMin);
                else if (v.getId() == R.id.tv_EndTime)
                    tv_EndTime.setText(String.format("%02d", selectedHour) + ":" + selectedMin);
                adjustTime();


            }

        }, hour, minute, true);

        timePickerDialog.show();
    }

    // Adjust the time automatically so that endtime is always after start time
    private void adjustTime() {

        try {
            Date startDate = dateTimeFormatter.parse(tv_StartDate.getText() + " " + tv_StartTime.getText().toString());

            //If user chose start time before current time
            Date currentDate = new Date();
            if (startDate.getTime() < currentDate.getTime()) {
                tv_StartDate.setText(dateFormatter.format(currentDate));
                tv_StartTime.setText(timeFormatter.format(currentDate));
            }

            Date endDate = dateTimeFormatter.parse(tv_EndDate.getText() + " " + tv_EndTime.getText().toString());
            //If user chose endTime before current time
            if (endDate.getTime() < currentDate.getTime()) {
                tv_EndDate.setText(dateFormatter.format(currentDate));
                tv_EndTime.setText(timeFormatter.format(currentDate));
            }

            //If user chose end time to be before start time
            if (endDate.getTime() < startDate.getTime()) {
                tv_EndDate.setText(dateFormatter.format(startDate.getTime()));
                tv_EndTime.setText(tv_StartTime.getText());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // When user clicks the duration button, automatically calculates the
    @OnClick({R.id.btn_HalfHour, R.id.btn_OneHalfHour, R.id.btn_OneHour, R.id.btn_TwoHour})
    public void addTime(final View v) {

        double factor = 0;
        if (v.getId() == R.id.btn_HalfHour) {
            factor = 0.5;
        } else if (v.getId() == R.id.btn_OneHour) {
            factor = 1.0;
        } else if (v.getId() == R.id.btn_OneHalfHour) {
            factor = 1.5;
        } else if (v.getId() == R.id.btn_TwoHour) {
            factor = 2.0;
        }

        try {
            Date startDate = dateTimeFormatter.parse(tv_StartDate.getText() + " " + tv_StartTime.getText().toString());
            Date endDate = new Date(startDate.getTime() + (long) (factor * HOUR));
            tv_EndDate.setText(dateFormatter.format(endDate));
            tv_EndTime.setText(timeFormatter.format(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // When user clicks the duration button, automatically calculates the
    @OnClick(R.id.btn_Post)
    public void onClick() {

        final String title = et_Title.getText().toString();
        final String location = et_Location.getText().toString();
        final String description = et_Description.getText().toString();
        final Date startDate;
        final Date endDate;
        final ParseGeoPoint geoPoint;

        if (checkTitle(title)) return;

        if (checkLocation(location)) return;

        try {
            startDate = dateTimeFormatter.parse(tv_StartDate.getText() + " " + tv_StartTime.getText().toString());
            endDate = dateTimeFormatter.parse(tv_EndDate.getText() + " " + tv_EndTime.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        geoPoint = checkLocation();
        if (geoPoint == null) return;


        if(editMode){
            // Adds the user's id and name to the going list
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Fragment_Map_Tab.POST);
            query.getInBackground(editEvent.getObjectId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, com.parse.ParseException e) {
                    if (e == null) {
                        editEvent(object, startDate, endDate, title, description, location, geoPoint);
                    }
                    Fragment_Map_Tab.getMarker().remove();
                    finish();
                }
            });
        }else{
            addEvent(title, location, description, startDate, endDate, geoPoint);
        }

        Fragment_Map_Tab.getMarker().remove();

        Intent goMain = new Intent(CreateEventActivity.this, MainActivity.class);
        goMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goMain);

    }

    private void addEvent(String title, String location, String description, Date startDate, Date endDate, ParseGeoPoint geoPoint) {
        ParseObject post = new ParseObject(Fragment_Map_Tab.POST);
        post.put(Fragment_Map_Tab.POSTER_ID, myID);
        post.put(Fragment_Map_Tab.POSTER_FIRSTNAME, myFirstName);
        post.put(Fragment_Map_Tab.POSTER_LASTNAME, myLastName);
        post.put(Fragment_Map_Tab.START_DATE, startDate);
        post.put(Fragment_Map_Tab.END_DATE, endDate);
        post.put(Fragment_Map_Tab.TITLE, title);
        post.put(Fragment_Map_Tab.DESCRIPTION, description);
        post.put(Fragment_Map_Tab.LOC_NAME, location);
        post.put(Fragment_Map_Tab.LOC_COORD, geoPoint);
        post.put(Fragment_Map_Tab.TYPE, spinnerEventType.getSelectedItemPosition());

        //TODO : Setup Subtypes
        JSONArray subtypes = new JSONArray();
        post.put(Fragment_Map_Tab.SUBTYPE, subtypes);

        JSONArray going = new JSONArray();
        JSONArray going_id = new JSONArray();
        post.put(Fragment_Map_Tab.GOING, going);
        post.put(Fragment_Map_Tab.GOING_ID, going_id);
        post.add(Fragment_Map_Tab.GOING, myFirstName + " " + myLastName);
        post.add(Fragment_Map_Tab.GOING_ID, myID);
        post.saveInBackground();
    }

    private void editEvent(ParseObject object, Date startDate, Date endDate, String title, String description, String location, ParseGeoPoint geoPoint) {
        object.put(Fragment_Map_Tab.POSTER_ID, myID);
        object.put(Fragment_Map_Tab.POSTER_FIRSTNAME, myFirstName);
        object.put(Fragment_Map_Tab.POSTER_LASTNAME, myLastName);
        object.put(Fragment_Map_Tab.START_DATE, startDate);
        object.put(Fragment_Map_Tab.END_DATE, endDate);
        object.put(Fragment_Map_Tab.TITLE, title);
        object.put(Fragment_Map_Tab.DESCRIPTION, description);
        object.put(Fragment_Map_Tab.LOC_NAME, location);
        object.put(Fragment_Map_Tab.LOC_COORD, geoPoint);
        object.put(Fragment_Map_Tab.TYPE, spinnerEventType.getSelectedItemPosition());
        object.saveInBackground();
    }

    @Nullable
    private ParseGeoPoint checkLocation() {
        ParseGeoPoint geoPoint;
        if (Fragment_Map_Tab.getMarker() == null) {
            Toast.makeText(CreateEventActivity.this, R.string.guide_select_location, Toast.LENGTH_SHORT).show();
            return null;
        } else {
            geoPoint = new ParseGeoPoint(Fragment_Map_Tab.getMarker().getPosition().latitude,
                    Fragment_Map_Tab.getMarker().getPosition().longitude);
        }
        return geoPoint;
    }

    private boolean checkLocation(String location) {
        if (location.equals("")) {
            et_Location.setError("Input Location");
            return true;
        }
        return false;
    }

    private boolean checkTitle(String title) {
        if (title.equals("")) {
            et_Title.setError("Input Title");
            return true;
        }
        return false;
    }

}
