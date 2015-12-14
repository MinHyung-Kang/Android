package hu.ait.android.fo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.fo.data.FoEvent;
import hu.ait.android.fo.data.User;
import hu.ait.android.fo.fragments.Fragment_Map_Tab;

public class EventDetailActivity extends AppCompatActivity {


    public static String EDIT_EVENT = "Edit_Event";
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat dateTimeFormatter;
    private FoEvent currentEvent;
    private boolean isHost;
    private boolean hostIsGoing;
    private String myID;
    private String myFirstName;
    private String myLastName;

    @Bind(R.id.tv_Detail_Title)
    TextView tv_Detail_Title;

    @Bind(R.id.tv_Detail_FOerTitle)
    TextView tv_Detail_FOerTitle;

    @Bind(R.id.tv_Detail_FOer)
    TextView tv_Detail_FOer;

    @Bind(R.id.tv_Detail_LocationTitle)
    TextView tv_Detail_LocationTitle;

    @Bind(R.id.tv_Detail_Location)
    TextView tv_Detail_Location;

    @Bind(R.id.tv_Detail_Start)
    TextView tv_Detail_Start;

    @Bind(R.id.tv_Detail_StartDate)
    TextView tv_Detail_StartDate;

    @Bind(R.id.tv_Detail_StartTime)
    TextView tv_Detail_StartTime;

    @Bind(R.id.tv_Detail_End)
    TextView tv_Detail_End;

    @Bind(R.id.tv_Detail_EndDate)
    TextView tv_Detail_EndDate;

    @Bind(R.id.tv_Detail_EndTime)
    TextView tv_Detail_EndTime;

    @Bind(R.id.tv_Detail_EventType)
    TextView tv_Detail_EventType;

    @Bind(R.id.tv_Detail_Type)
    TextView tv_Detail_Type;

    @Bind(R.id.tv_Detail_DescriptionTitle)
    TextView tv_Detail_DescriptionTitle;

    @Bind(R.id.tv_Detail_Description)
    TextView tv_Detail_Description;

    @Bind(R.id.tv_Detail_MemberTitle)
    TextView tv_Detail_MemberTitle;

    @Bind(R.id.tv_Detail_Member)
    TextView tv_Detail_Member;

    @Bind(R.id.btn_Detail_Join)
    Button btn_Detail_Join;

    @Bind(R.id.btn_Detail_Quit)
    Button btn_Detail_Quit;

    @Bind(R.id.sv_Detail_ScrollView)
    ScrollView sv_Detail_ScrollView;

    @Bind(R.id.iv_Detail_Transparent)
    ImageView iv_Detail_Transparent;

    @Bind(R.id.btn_Detail_Edit)
    Button btn_Detail_Edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentEvent = (FoEvent) bundle.getSerializable(MainActivity.CLICKED_EVENT);
            setContent(currentEvent);
        } else {
            throw new RuntimeException(getString(R.string.empty_exception));
        }

        interceptMotionFromScrollView();
    }

    private void setContent(FoEvent currentEvent) {

        isHost = false;
        hostIsGoing = false;

        tv_Detail_Title.setText(currentEvent.getTitle());
        String eventType = getEventType(currentEvent);

        //TODO : CLEANUP NAME PART
        setUpComponents(currentEvent, eventType);

        if (currentEvent.getPoster_id().equals(myID))
            isHost = true;

        setUpGoingLists(currentEvent);

        changeButtons(true, "Join", Color.BLUE, false, "<---", Color.GRAY, View.GONE);
        changeButtonForMember(currentEvent);
        changeButtonForAuthor(currentEvent);


        Fragment_Map_Tab detail_MapFragment = (Fragment_Map_Tab) getSupportFragmentManager().findFragmentById(R.id.detail_MapFragment);
        detail_MapFragment.moveCamera(currentEvent.getLat(), currentEvent.getLng());
        detail_MapFragment.placeMarker(currentEvent.getLat(), currentEvent.getLng(), currentEvent.getTitle(), currentEvent.getType());

    }

    private void setUpGoingLists(FoEvent currentEvent) {
        ArrayList<String> going = currentEvent.getGoing();
        ArrayList<String> going_id = currentEvent.getGoing_ID();
        String goingList = "";
        if (going != null && going_id != null) {
            for (int i = 0; i < going.size(); i++) {

                if (currentEvent.getPoster_id().equals(going_id.get(i))) {
                    goingList += going.get(i) + " (host)\n";
                } else {
                    goingList += going.get(i) + "\n";
                }
            }
        }
        tv_Detail_Member.setText(goingList);
    }

    private void setUpComponents(FoEvent currentEvent, String eventType) {
        if (currentEvent.getPoster_FirstName() == null)
            tv_Detail_FOer.setText(currentEvent.getPoster_id());
        else
            tv_Detail_FOer.setText(currentEvent.getPoster_FirstName() + " " + currentEvent.getPoster_LastName());
        tv_Detail_Type.setText(eventType);
        tv_Detail_StartDate.setText(dateFormatter.format(currentEvent.getStartTime()));
        tv_Detail_StartTime.setText(timeFormatter.format(currentEvent.getStartTime()));
        tv_Detail_EndDate.setText(dateFormatter.format(currentEvent.getStartTime()));
        tv_Detail_EndTime.setText(timeFormatter.format(currentEvent.getStartTime()));
        tv_Detail_Location.setText(currentEvent.getLocation());
        tv_Detail_Description.setText(currentEvent.getDescription());
    }

    @NonNull
    private String getEventType(FoEvent currentEvent) {
        String eventType = "";
        switch (currentEvent.getType()) {
            case 0:
                eventType = getString(R.string.type_public);
                break;
            case 1:
                eventType = getString(R.string.type_friends);
                break;
            case 2:
                eventType = getString(R.string.type_group);
                break;
            case 3:
                eventType = getString(R.string.type_private);
                break;
            default:
                eventType = getString(R.string.type_unknown);
                break;
        }
        return eventType;
    }

    private void changeButtons(boolean join_clickable, String join_text, int join_color,
                               boolean quit_clickable, String quit_text, int quit_color,
                               int edit_visibility) {
        btn_Detail_Join.setClickable(join_clickable);
        btn_Detail_Join.setText(join_text);
        btn_Detail_Join.setBackgroundColor(join_color);
        btn_Detail_Quit.setClickable(quit_clickable);
        btn_Detail_Quit.setText(quit_text);
        btn_Detail_Quit.setBackgroundColor(quit_color);
        btn_Detail_Edit.setVisibility(edit_visibility);
        btn_Detail_Edit.setBackgroundColor(Color.LTGRAY);
    }


    private void changeButtonForMember(FoEvent currentEvent) {
        ArrayList<String> going_id = currentEvent.getGoing_ID();
        if (going_id != null) {
            for (String id : going_id) {

                //TODO : Change hardcoded id
                if (id.equals(myID)) {
                    changeButtons(false, "--->", Color.GRAY, true, "Quit", Color.RED,View.GONE);
                    break;
                }
            }
        }
    }

    private void changeButtonForAuthor(FoEvent currentEvent) {
        //TODO : Change hardcoded id
        if (isHost) {
            ArrayList<String> going_id = currentEvent.getGoing_ID();
            for (int i = 0; i < going_id.size(); i++) {
                if (going_id.get(i).equals(myID)) {
                    hostIsGoing = true;
                    changeButtons(true, "Delete", Color.YELLOW, true, "Quit", Color.RED,View.VISIBLE);
                    break;
                }
            }

            if (!hostIsGoing) {
                changeButtons(true, "Delete", Color.YELLOW, true, "Join", Color.BLUE,View.VISIBLE);
            }
        }
    }

    //Setup touch listener for transparent image to intercept any touches
    private void interceptMotionFromScrollView() {
        iv_Detail_Transparent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        sv_Detail_ScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        sv_Detail_ScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        sv_Detail_ScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    @OnClick(R.id.btn_Detail_Join)
    public void join() {

        //This is case when user is the poster, and decides to delete
        if (isHost) {

            showDeleteDialog();
        } else {

            // Adds the user's id and name to the going list
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Fragment_Map_Tab.POST);
            query.getInBackground(currentEvent.getObjectId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        Log.d("LOGLOG", "Title : " + object.getString(Fragment_Map_Tab.TITLE));
                        object.add(Fragment_Map_Tab.GOING,
                                myFirstName + " " + myLastName);
                        object.add(Fragment_Map_Tab.GOING_ID,
                                myID);
                        object.saveInBackground();
                    }
                    finish();
                }
            });

        }
    }

    // Allows the user to edit the event
    @OnClick(R.id.btn_Detail_Edit)
    public void edit(){
        Intent viewEdit = new Intent();
        viewEdit.setClass(EventDetailActivity.this, CreateEventActivity.class);
        viewEdit.putExtra(EDIT_EVENT, currentEvent);
        startActivity(viewEdit);
    }

    // Allows the user to quit
    @OnClick(R.id.btn_Detail_Quit)
    public void quit() {
        if (!hostIsGoing && isHost) {
            // Adds the user's id and name to the going list
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Fragment_Map_Tab.POST);
            query.getInBackground(currentEvent.getObjectId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        object.add(Fragment_Map_Tab.GOING,
                                myFirstName + " " + myLastName);
                        object.add(Fragment_Map_Tab.GOING_ID,
                                myID);
                        object.saveInBackground();
                    }
                    finish();
                }
            });
        } else {
            // Removes the user's id and name to the going list
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Fragment_Map_Tab.POST);
            query.getInBackground(currentEvent.getObjectId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        ArrayList<String> going_id = currentEvent.getGoing_ID();
                        ArrayList<String> going = currentEvent.getGoing();
                        for (int i = 0; i < going_id.size(); i++) {
                            if (going_id.get(i).equals(myID)) {
                                going_id.remove(i);
                                going.remove(i);
                                break;
                            }
                        }
                        object.remove(Fragment_Map_Tab.GOING);
                        object.remove(Fragment_Map_Tab.GOING_ID);
                        object.addAll(Fragment_Map_Tab.GOING, going);
                        object.addAll(Fragment_Map_Tab.GOING_ID, going_id);

                        object.saveInBackground();
                        finish();
                    }
                }
            });
        }

    }

    private void showDeleteDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete entry");
        alertDialog.setMessage("Are you sure you want to delete this post?");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int which) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(Fragment_Map_Tab.POST);
                        query.getInBackground(currentEvent.getObjectId(), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if (e == null) {
                                    object.deleteInBackground();
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        });
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


}
