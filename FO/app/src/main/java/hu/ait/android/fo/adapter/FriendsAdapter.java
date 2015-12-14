package hu.ait.android.fo.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<User> friendList;

    public FriendsAdapter(List<User> friendList){
        this.friendList = friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_friend, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final User friend = friendList.get(position);
        holder.tv_RowFriend_Name.setText(friend.getFirstName() + " " + friend.getLastName());
        holder.tv_RowFriend_Description.setText(friend.getDescription());

        switchCases(holder, friend);
        findUser(holder, friend);
    }

    private void findUser(final ViewHolder holder, User friend) {
        // Search the user related to the field
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.USER_ID, friend.getUserID());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    setImages(objects.get(0), holder);
                }
            }
        });
    }

    private void switchCases(final ViewHolder holder, final User friend) {
        switch(friend.getIsFriend()){
            case 0:
                holder.btn_RowFriend_State.setImageResource(R.drawable.ic_person_add_black_24dp);
                holder.btn_RowFriend_State.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendFriendRequest(friend, holder);
                    }
                });
                break;
            case 1:
                holder.btn_RowFriend_State.setImageResource(R.drawable.ic_people_black_24dp);
                // TODO : Do something?
                break;
            case 2:
                holder.btn_RowFriend_State.setImageResource(R.drawable.ic_person_white_24dp);
                holder.btn_RowFriend_State.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelRequest(friend, holder);
                }
            });
        }
    }

    private void cancelRequest(User friend, final ViewHolder holder) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(User.REQUESTS);
        query.whereEqualTo(User.USER_ID, FriendActivity.getMyID());
        query.whereEqualTo(User.RECEIVER_ID, friend.getUserID());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    objects.get(0).deleteInBackground();
                    holder.btn_RowFriend_State.setImageResource(R.drawable.ic_person_add_black_24dp);
                    notifyDataSetChanged();
                }
            }
        });
    }

    private void sendFriendRequest(User friend, ViewHolder holder) {
        ParseObject post = new ParseObject(User.REQUESTS);
        post.put(User.RECEIVER_ID, friend.getUserID());
        post.put(User.USER_ID, FriendActivity.getMyID());
        post.saveInBackground();
        holder.btn_RowFriend_State.setImageResource(R.drawable.ic_person_white_24dp);
        notifyDataSetChanged();
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
                        viewHolder.iv_RowFriend_ProfilePic.setImageBitmap(bmp);
                    } else {
                        viewHolder.iv_RowFriend_ProfilePic.setImageResource(R.drawable.logo);
                    }
                }
            });
        }
    }

    public void deleteAll(){
        friendList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView iv_RowFriend_ProfilePic;
        private ImageButton btn_RowFriend_State;
        private TextView tv_RowFriend_Name;
        private TextView tv_RowFriend_Description;

        public ViewHolder(View itemView){
            super(itemView);
            findViews(itemView);
        }

        private void findViews(View itemView) {
            iv_RowFriend_ProfilePic = (ImageView)itemView.findViewById(R.id.iv_RowFriend_ProfilePic);
            btn_RowFriend_State = (ImageButton) itemView.findViewById(R.id.btn_RowFriend_State);
            tv_RowFriend_Name = (TextView) itemView.findViewById(R.id.tv_RowFriend_Name);
            tv_RowFriend_Description = (TextView) itemView.findViewById(R.id.tv_RowFriend_Description);
        }

    }

}
