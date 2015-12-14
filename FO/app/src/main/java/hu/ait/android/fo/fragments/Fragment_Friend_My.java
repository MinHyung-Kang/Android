package hu.ait.android.fo.fragments;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import hu.ait.android.fo.FriendActivity;
import hu.ait.android.fo.R;
import hu.ait.android.fo.adapter.RequestsAdapter;
import hu.ait.android.fo.data.User;

/**
 * Created by user on 2015-12-12.
 * Shows the requests sent and received by this user
 */
public class Fragment_Friend_My extends Fragment {

    private RequestsAdapter requestsAdapter;
    private RecyclerView myRecyclerView;
    private List<User> requestList;
    private boolean receivedReady, sentReady;
    private ArrayList<String> requestsSent;
    private ArrayList<String> requestsReceived;
    private TextView tv_Friend_My_NoData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_friend_my, container, false);
        myRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rv_Friend_My);
        requestsSent = new ArrayList<>();
        requestsReceived = new ArrayList<>();
        tv_Friend_My_NoData = (TextView) inflatedView.findViewById(R.id.tv_Friend_My_NoData);
        return inflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestList = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(requestList);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(requestsAdapter);

        getEvents();
    }

    //Get the events
    private void getEvents() {
        requestsAdapter.deleteAll();
        requestList.clear();
        requestsSent.clear();
        requestsReceived.clear();
        receivedReady = false;
        sentReady = false;

        getSentRequests();
    }

    private void getSentRequests() {
        //Get the list of requests sent
        ParseQuery<ParseObject> query_Sent = ParseQuery.getQuery(User.REQUESTS);
        query_Sent.whereEqualTo(User.USER_ID, FriendActivity.getMyID());
        query_Sent.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        requestsSent.add(object.getString(User.RECEIVER_ID));
                    }
                    getRequestUsers(requestsSent, 1);
                    sentReady = true;
                    getReceivedRequests();
                } else {
                    Log.d("LOGLOG", getActivity().getString(R.string.result_null));
                }
            }
        });
    }

    private void getReceivedRequests() {
        //Get the list of requests received
        ParseQuery<ParseObject> query_Received = ParseQuery.getQuery(User.REQUESTS);
        query_Received.whereEqualTo(User.RECEIVER_ID, FriendActivity.getMyID());
        query_Received.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        requestsReceived.add(object.getString(User.USER_ID));
                    }

                    getRequestUsers(requestsReceived, 0);
                    receivedReady = true;
                } else {
                    Log.d("LOGLOG", getActivity().getString(R.string.result_null));
                }
            }
        });
    }


    // Get the user that mathces the requests
    private void getRequestUsers(ArrayList<String> users, final int isSent) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn(User.USER_ID, users);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    int isFriend = (isSent == 0) ? 0 : 2;
                    for (ParseUser user : users) {
                        User tempUser = new User();
                        tempUser.setUserID(user.getString(User.USER_ID));
                        tempUser.setFirstName(user.getString(User.FIRSTNAME));
                        tempUser.setLastName(user.getString(User.LASTNAME));
                        tempUser.setPhoneNumber(user.getString(User.PHONE));
                        tempUser.setDescription(user.getString(User.DESC));
                        tempUser.setEmail(user.getEmail());
                        tempUser.setIsFriend(isFriend);
                        requestList.add(tempUser);
                    }
                    updateData();
                } else {
                }
            }
        });
    }

    // When both query was successful, add the data
    private void updateData() {
        if (receivedReady && sentReady) {
            if (requestList.size() == 0) {
                tv_Friend_My_NoData.setVisibility(View.VISIBLE);
            } else {
                tv_Friend_My_NoData.setVisibility(View.GONE);
                requestsAdapter.setRequestList(requestList);
            }
        }
    }
}
