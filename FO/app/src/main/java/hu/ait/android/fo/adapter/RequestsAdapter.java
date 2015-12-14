package hu.ait.android.fo.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import hu.ait.android.fo.FriendActivity;
import hu.ait.android.fo.R;
import hu.ait.android.fo.data.User;

/**
 * Created by user on 2015-12-12.
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<User> requestList;

    public void setRequestList(List<User> requestList) {
        this.requestList = requestList;
        notifyDataSetChanged();
    }

    public RequestsAdapter(List<User> requestList) {
        this.requestList = requestList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_request, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = requestList.get(position);
        holder.tv_RowRequest_Name.setText(user.getFirstName() + " " + user.getLastName());
        holder.tv_RowRequest_Description.setText(user.getDescription());

        switchCases(holder, position, user);
        findUser(holder, user);
    }

    private void findUser(final ViewHolder holder, User user) {
        // Search the user who posted the query
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.USER_ID, user.getUserID());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    setImages(objects.get(0), holder);
                }
            }
        });
    }

    private void switchCases(ViewHolder holder, int position, User user) {
        switch (user.getIsFriend()) {
            case 0:
                holder.ll_RowRequest_Buttons.setVisibility(View.VISIBLE);
                holder.tv_RowRequest_Pending.setVisibility(View.INVISIBLE);
                setOnClickListeners(holder, position);
                break;
            case 1:
                // Should not occur
                break;
            case 2:
                holder.ll_RowRequest_Buttons.setVisibility(View.INVISIBLE);
                holder.tv_RowRequest_Pending.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Set the profile pictures for each row
    private void setImages(ParseUser user, final ViewHolder viewHolder) {
        ParseFile parseFile = user.getParseFile(User.PIC);
        if (parseFile != null) {
            parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        viewHolder.iv_RowRequest_ProfilePic.setImageBitmap(bmp);
                    } else {
                        viewHolder.iv_RowRequest_ProfilePic.setImageResource(R.drawable.logo);
                    }
                }
            });
        }
    }

    // Add onclick listeners to two buttons
    private void setOnClickListeners(ViewHolder holder, final int position) {
        final User user = requestList.get(position);
        holder.btn_RowRequest_Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest(user, position);
                addFriendForUser(user);
                addFriendForRequester(user);
                notifyDataSetChanged();
            }
        });
        holder.btn_RowRequest_Decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest(user, position);
                notifyDataSetChanged();
            }
        });
    }

    private void addFriendForRequester(User user) {
        ParseQuery<ParseObject> query_sender = ParseQuery.getQuery(User.FRIENDS);
        query_sender.whereEqualTo(User.USER_ID, user.getUserID());
        query_sender.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    objects.get(0).addUnique(User.FRIEND_LIST, FriendActivity.getMyID());
                    objects.get(0).saveInBackground();
                } else {

                }
            }
        });
    }

    private void addFriendForUser(final User user) {
        ParseQuery<ParseObject> query_user = ParseQuery.getQuery(User.FRIENDS);
        query_user.whereEqualTo(User.USER_ID, FriendActivity.getMyID());
        query_user.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    objects.get(0).addUnique(User.FRIEND_LIST, user.getUserID());
                    objects.get(0).saveInBackground();
                } else {

                }
            }
        });
    }

    private void deleteRequest(User user, int position) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(User.REQUESTS);
        query.whereEqualTo(User.RECEIVER_ID, FriendActivity.getMyID());
        query.whereEqualTo(User.USER_ID, user.getUserID());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    objects.get(0).deleteInBackground();
                } else {

                }
            }
        });
        requestList.remove(position);
        notifyDataSetChanged();
    }

    public void deleteAll() {
        requestList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_RowRequest_ProfilePic;
        private TextView tv_RowRequest_Name;
        private TextView tv_RowRequest_Description;
        private Button btn_RowRequest_Accept, btn_RowRequest_Decline;
        private TextView tv_RowRequest_Pending;
        private LinearLayout ll_RowRequest_Buttons;

        public ViewHolder(View itemView) {
            super(itemView);
            findViews(itemView);
        }

        private void findViews(View itemView) {
            iv_RowRequest_ProfilePic = (ImageView) itemView.findViewById(R.id.iv_RowRequest_ProfilePic);
            tv_RowRequest_Name = (TextView) itemView.findViewById(R.id.tv_RowRequest_Name);
            tv_RowRequest_Description = (TextView) itemView.findViewById(R.id.tv_RowRequest_Description);
            tv_RowRequest_Pending = (TextView) itemView.findViewById(R.id.tv_RowRequest_Pending);
            btn_RowRequest_Accept = (Button) itemView.findViewById(R.id.btn_RowRequest_Accept);
            btn_RowRequest_Decline = (Button) itemView.findViewById(R.id.btn_RowRequest_Decline);
            ll_RowRequest_Buttons = (LinearLayout) itemView.findViewById(R.id.ll_RowRequest_Buttons);
        }

    }

}
