package hu.ait.android.fo.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hu.ait.android.fo.MainActivity;
import hu.ait.android.fo.R;
import hu.ait.android.fo.adapter.FoEventsAdapter;
import hu.ait.android.fo.data.FoEvent;
import hu.ait.android.fo.data.User;

/**
 * Created by user on 2015-11-30.
 */
public class Fragment_My_Tab extends Fragment implements FoEventsAdapter.RecyclerViewClickListener {

    private FoEventsAdapter myEventsAdapter;
    private RecyclerView myRecyclerViewEvents;
    private List<FoEvent> myEventList;
    private TextView tv_My_NoData;
    private Date currentDate;
    private Activity mActivity;

    private String myID;
    private String myFirstName;
    private String myLastName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View inflatedView = inflater.inflate(R.layout.fragment_my, container, false);
        myRecyclerViewEvents = (RecyclerView) inflatedView.findViewById(R.id.rv_My);
        tv_My_NoData = (TextView) inflatedView.findViewById(R.id.tv_My_NoData);


        ParseUser user = ParseUser.getCurrentUser();
        myID = user.getUsername();
        myFirstName = user.getString(User.FIRSTNAME);
        myLastName = user.getString(User.LASTNAME);

        return inflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currentDate = new Date(System.currentTimeMillis());
        myEventList = new ArrayList<>();
        myEventsAdapter = new FoEventsAdapter(myEventList, this);
        myRecyclerViewEvents.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerViewEvents.setAdapter(myEventsAdapter);

        getEvents();
    }

    private void getEvents() {
        myEventList.clear();
        myEventsAdapter.deleteAll();

        List<ParseQuery<ParseObject>> queries = getParseQueries();

        ParseQuery<ParseObject> pq = ParseQuery.or(queries);
        pq.setLimit(50);
        pq.orderByAscending(Fragment_Map_Tab.START_DATE);
        pq.whereGreaterThan(Fragment_Map_Tab.END_DATE, currentDate);
        //Limit the event types
        boolean[] filter = ((MainActivity)mActivity).getFilterPreferences();
        List checks = new ArrayList<Integer>();
        for(int i=0; i< filter.length; i++){
            if(!filter[i]) {
                checks.add(i);
            }
        }
        pq.whereNotContainedIn(Fragment_Map_Tab.TYPE, checks);

        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects,
                             ParseException e) {

                StringBuilder sb = new StringBuilder();
                for (ParseObject post : parseObjects) {

                    FoEvent tempEvent = getFoEvent(post);
                    addGoingArrays(post, tempEvent);

                    myEventList.add(tempEvent);
                }

                if(myEventList.size() == 0){
                    tv_My_NoData.setVisibility(View.VISIBLE);

                }else{
                    tv_My_NoData.setVisibility(View.INVISIBLE);
                    myEventsAdapter.setFoEventList(myEventList);
                }
            }
        });
    }

    private void addGoingArrays(ParseObject post, FoEvent tempEvent) {
        try {
            JSONArray goingArray = post.getJSONArray(Fragment_Map_Tab.GOING);
            JSONArray goingIDArray = post.getJSONArray(Fragment_Map_Tab.GOING_ID);
            if(goingArray != null){
                ArrayList<String> goingList = (ArrayList<String>) toList(goingArray);
                tempEvent.setGoing(goingList);
            }
            if(goingIDArray != null){
                ArrayList<String> goingIDList = (ArrayList<String>) toList(goingIDArray);
                tempEvent.setGoing_ID(goingIDList);
            }

        } catch (JSONException f) {
            f.printStackTrace();
        }
    }

    @NonNull
    private List<ParseQuery<ParseObject>> getParseQueries() {
        ParseQuery<ParseObject> pq_IsAuthor = ParseQuery.getQuery(Fragment_Map_Tab.POST);
        pq_IsAuthor.whereEqualTo(Fragment_Map_Tab.POSTER_ID, myID);

        ParseQuery<ParseObject> pq_IsGoing = ParseQuery.getQuery(Fragment_Map_Tab.POST);
        pq_IsGoing.whereEqualTo(Fragment_Map_Tab.GOING_ID, myID);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(pq_IsAuthor);
        queries.add(pq_IsGoing);
        return queries;
    }

    @NonNull
    private FoEvent getFoEvent(ParseObject post) {
        FoEvent tempEvent = new FoEvent();
        tempEvent.setTitle(post.getString(Fragment_Map_Tab.TITLE));
        tempEvent.setLat(post.getParseGeoPoint(Fragment_Map_Tab.LOC_COORD).getLatitude());
        tempEvent.setLng(post.getParseGeoPoint(Fragment_Map_Tab.LOC_COORD).getLongitude());
        tempEvent.setLocation(post.getString(Fragment_Map_Tab.LOC_NAME));
        tempEvent.setDescription(post.getString(Fragment_Map_Tab.DESCRIPTION));
        tempEvent.setStartTime(post.getDate(Fragment_Map_Tab.START_DATE));
        tempEvent.setEndTime(post.getDate(Fragment_Map_Tab.END_DATE));
        tempEvent.setType(post.getInt(Fragment_Map_Tab.TYPE));
        tempEvent.setPoster_id(post.getString(Fragment_Map_Tab.POSTER_ID));
        tempEvent.setPoster_FirstName(post.getString(Fragment_Map_Tab.POSTER_FIRSTNAME));
        tempEvent.setPoster_LastName(post.getString(Fragment_Map_Tab.POSTER_LASTNAME));
        tempEvent.setObjectId(post.getObjectId());
        return tempEvent;
    }


    @Override
    public void onRowClicked(int position) {
        ((MainActivity) getActivity()).viewEventDetail(myEventList.get(position));
    }

    public void applyFilter(){
        if(getActivity() != null)
            getEvents();
    }

    private List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        int size = array.length();
        for (int i = 0; i < size; i++) {
            list.add(array.get(i));
        }
        return list;
    }

}
