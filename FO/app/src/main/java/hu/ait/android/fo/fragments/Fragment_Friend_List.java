package hu.ait.android.fo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import hu.ait.android.fo.FriendActivity;
import hu.ait.android.fo.R;
import hu.ait.android.fo.adapter.FriendsAdapter;
import hu.ait.android.fo.data.User;

/**
 * Creauted by user on 2015-12-12.
 * Gives us a list of
 */
public class Fragment_Friend_List extends Fragment {

    private FriendsAdapter friendsAdapter;
    private RecyclerView myRecyclerView;
    private List<User> friendList;
    private List<User> resultList;
    private ArrayList<String> friendNames;
    private ArrayList<String> requestSent;
    private ArrayList<String> requestReceived;
    private String myID;
    private String myFirstName;
    private String myLastName;
    private boolean noQuery;
    private ProgressBar pb_Friend_List_Progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getCurrentUser();

        View inflatedView = inflater.inflate(R.layout.fragment_friend_list, container, false);
        myRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rv_Friend_List);
        pb_Friend_List_Progress = (ProgressBar)inflatedView.findViewById(R.id.pb_Friend_List_Progress);
        setUpQuery(inflatedView);

        return inflatedView;
    }

    private void getCurrentUser() {
        ParseUser user = ParseUser.getCurrentUser();
        myID = user.getUsername();
        myFirstName = user.getString(User.FIRSTNAME);
        myLastName = user.getString(User.LASTNAME);
    }

    //Allows user to search other users
    private void setUpQuery(View inflatedView) {
        final ImageButton btn_Friend_Search = (ImageButton) inflatedView.findViewById(R.id.btn_Friend_Search);
        final EditText et_Friend_Query = (EditText) inflatedView.findViewById(R.id.et_Friend_Query);
        btn_Friend_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = et_Friend_Query.getText().toString();
                if (query.equals("")) {
                    noQuery = true;
                    pb_Friend_List_Progress.setVisibility(View.VISIBLE);
                    getFriends();
                } else {
                    pb_Friend_List_Progress.setVisibility(View.VISIBLE);
                    noQuery = false;
                    getQueryResults(query);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        friendList = new ArrayList<>();
        friendNames = new ArrayList<>();
        resultList = new ArrayList<>();
        requestSent = new ArrayList<>();
        requestReceived = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(friendList);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(friendsAdapter);
        noQuery = true;
        getFriends();
    }

    // Given a query, searches for possible matches
    private void getQueryResults(String keyword) {
        Log.d("LOGLOG", "getQueryResult");
        friendsAdapter.deleteAll();
        friendList.clear();
        resultList.clear();

        List<ParseQuery<ParseUser>> queries = getParseQueries(keyword);

        ParseQuery<ParseUser> pq = ParseQuery.or(queries);
        pq.setLimit(200);

        pq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {

                    if (users.size() == 0) {
                        ((FriendActivity) getActivity()).showSnackBarMessage("No Result Found");
                    } else {

                        for (ParseUser user : users) {
                            if (!user.getUsername().equals(myID)) {
                                User tempUser = new User();
                                tempUser.setUserID(user.getUsername());
                                tempUser.setFirstName(user.getString(User.FIRSTNAME));
                                tempUser.setLastName(user.getString(User.LASTNAME));
                                tempUser.setPhoneNumber(user.getString(User.PHONE));
                                tempUser.setDescription(user.getString(User.DESC));
                                tempUser.setEmail(user.getEmail());
                                resultList.add(tempUser);
                            }
                        }

                        processResult();
                        // The query was successful.
                    }
                } else {
                    pb_Friend_List_Progress.setVisibility(View.GONE);
                    // Something went wrong.
                }
            }
        });
    }

    private void processResult(){
        Log.d("LOGLOG", "processResult");
        friendNames.clear();
        requestSent.clear();
        requestReceived.clear();
        getFriends();
    }

    private void processFinalResult(){
        Log.d("LOGLOG", "processFinalResult");
        for(User user : resultList){
            String id = user.getUserID();
            if(friendNames.contains(id)){
                user.setIsFriend(1);
            }else if(requestSent.contains(id) || requestReceived.contains(id)){
                user.setIsFriend(2);
            }else{
                user.setIsFriend(0);
            }
        }
        friendsAdapter.setFriendList(resultList);
        pb_Friend_List_Progress.setVisibility(View.GONE);


    }


    private void getSentRequests() {
        Log.d("LOGLOG", "getSentRequests");
        //Get the list of requests sent
        ParseQuery<ParseObject> query_Sent = ParseQuery.getQuery(User.REQUESTS);
        query_Sent.whereEqualTo(User.USER_ID, FriendActivity.getMyID());
        query_Sent.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        requestSent.add(object.getString(User.RECEIVER_ID));
                    }
                    getReceivedRequests();
                } else {
                    pb_Friend_List_Progress.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getReceivedRequests() {
        Log.d("LOGLOG", "getReceivedRequests");
        //Get the list of requests received
        ParseQuery<ParseObject> query_Received = ParseQuery.getQuery(User.REQUESTS);
        query_Received.whereEqualTo(User.RECEIVER_ID, FriendActivity.getMyID());
        query_Received.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        requestReceived.add(object.getString(User.USER_ID));
                    }
                    processFinalResult();

                } else {
                    pb_Friend_List_Progress.setVisibility(View.GONE);
                }
            }
        });
    }


    @NonNull
    private List<ParseQuery<ParseUser>> getParseQueries(String keyword) {
        ParseQuery<ParseUser> query_ID = ParseUser.getQuery();
        query_ID.whereContains(User.USER_ID, keyword);

        ParseQuery<ParseUser> query_FirstName = ParseUser.getQuery();
        query_FirstName.whereContains(User.FIRSTNAME, keyword);

        ParseQuery<ParseUser> query_LastName = ParseUser.getQuery();
        query_LastName.whereContains(User.LASTNAME, keyword);

        ParseQuery<ParseUser> query_Email = ParseUser.getQuery();
        query_Email.whereContains(User.EMAIL, keyword);

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(query_ID);
        queries.add(query_FirstName);
        queries.add(query_LastName);
        queries.add(query_Email);
        return queries;
    }

    // Get the friends of the user
    private void getFriends() {
        Log.d("LOGLOG", "getFriends");
        //friendsAdapter.deleteAll();
        friendList.clear();

        ParseQuery<ParseObject> pq_FindFriend = ParseQuery.getQuery(User.FRIENDS);
        pq_FindFriend.whereEqualTo(User.USER_ID, myID);
        pq_FindFriend.setLimit(1);
        pq_FindFriend.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    for(ParseObject object : objects){
                        Log.d("LOGLOG", "Called");
                        try {
                            JSONArray friendArray = object.getJSONArray(User.FRIEND_LIST);
                            if (friendArray != null) {
                                friendNames = (ArrayList<String>) toList(friendArray);
                                if(noQuery) {
                                    getFriendDetails(friendNames);
                                }else{
                                    getSentRequests();
                                }
                            }else{
                                if(!noQuery){
                                    getSentRequests();
                                }else{
                                    ((FriendActivity) getActivity()).showSnackBarMessage("No Result Found");
                                    friendsAdapter.setFriendList(friendList);
                                    pb_Friend_List_Progress.setVisibility(View.GONE);
                                }
                            }
                        }catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }else{
                    Log.d("LOGLOG","EXCEPTION!");
                }
            }
        });

    }

    private void getFriendDetails(final ArrayList<String> friendNames){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn(User.USER_ID, friendNames);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    // The query was successful.
                    for (ParseUser user : users) {
                        User tempUser = new User();
                        tempUser.setUserID(user.getString(User.USER_ID));
                        tempUser.setFirstName(user.getString(User.FIRSTNAME));
                        tempUser.setLastName(user.getString(User.LASTNAME));
                        tempUser.setPhoneNumber(user.getString(User.PHONE));
                        tempUser.setDescription(user.getString(User.DESC));
                        tempUser.setEmail(user.getEmail());
                        tempUser.setIsFriend(1);

                        friendList.add(tempUser);
                    }

                    friendsAdapter.setFriendList(friendList);
                    pb_Friend_List_Progress.setVisibility(View.GONE);
                } else {
                    // Something went wrong.
                }
            }
        });
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
