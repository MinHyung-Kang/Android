package hu.ait.android.fo.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
public class Fragment_List_Tab extends Fragment implements FoEventsAdapter.RecyclerViewClickListener {

    private FoEventsAdapter foEventsAdapter;
    private RecyclerView recyclerViewEvents;
    private List<FoEvent> foEventList;
    private Date currentDate;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View inflatedView = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerViewEvents = (RecyclerView)inflatedView.findViewById(R.id.rv_List);


        return inflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        currentDate = new Date(System.currentTimeMillis());
        super.onActivityCreated(savedInstanceState);
        foEventList = new ArrayList<>();
        foEventsAdapter = new FoEventsAdapter(foEventList,this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewEvents.setAdapter(foEventsAdapter);

        getEvents();
    }

    public void getEvents() {
        foEventList.clear();
        foEventsAdapter.deleteAll();

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


    private void getAllPosts(ArrayList<String> friendNames){
        friendNames.add(MainActivity.getMyID());
        List<ParseQuery<ParseObject>> queries = getParseQueries(friendNames);
        if (queries == null) return;

        ParseQuery<ParseObject> pq = ParseQuery.or(queries);
        pq.orderByAscending(Fragment_Map_Tab.START_DATE);
        pq.whereGreaterThan(Fragment_Map_Tab.END_DATE, currentDate);
        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects,
                             ParseException e) {
                for (ParseObject post : parseObjects) {

                    FoEvent tempEvent = getFoEvent(post);

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

                    foEventList.add(tempEvent);
                }

                foEventsAdapter.setFoEventList(foEventList);
            }
        });
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

    @Nullable
    private List<ParseQuery<ParseObject>> getParseQueries(ArrayList<String> friendNames) {
        ParseQuery<ParseObject> pq_Public = ParseQuery.getQuery(Fragment_Map_Tab.POST);
        ParseQuery<ParseObject> pq_Friends = ParseQuery.getQuery(Fragment_Map_Tab.POST);
        ParseQuery<ParseObject> pq_Group = ParseQuery.getQuery(Fragment_Map_Tab.POST);
        ParseQuery<ParseObject> pq_Private = ParseQuery.getQuery(Fragment_Map_Tab.POST);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        boolean[] filter = ((MainActivity) mActivity).getFilterPreferences();
        if(filter[0]){
            pq_Public.whereEqualTo(Fragment_Map_Tab.TYPE, 0);
            queries.add(pq_Public);
        }
        if(filter[1]){
            pq_Friends.whereEqualTo(Fragment_Map_Tab.TYPE, 1);
            pq_Friends.whereContainedIn(Fragment_Map_Tab.POSTER_ID, friendNames);
            queries.add(pq_Friends);
        }
        if(filter[2]){
            pq_Group.whereEqualTo(Fragment_Map_Tab.TYPE, 2);
            queries.add(pq_Group);
        }
        if(filter[3]){
            pq_Private.whereEqualTo(Fragment_Map_Tab.TYPE, 3);
            queries.add(pq_Private);
        }

        if(queries.size() == 0){
            return null;
        }
        return queries;
    }


    @Override
    public void onRowClicked(int position) {
        ((MainActivity)getActivity()).viewEventDetail(foEventList.get(position));
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
