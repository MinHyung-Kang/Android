package hu.ait.android.fo.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import hu.ait.android.fo.MainActivity;
import hu.ait.android.fo.R;
import hu.ait.android.fo.data.FoEvent;
import hu.ait.android.fo.data.User;
import hu.ait.android.fo.fragments.Fragment_Map_Tab;

/**
 * Created by user on 2015-12-10.
 */
public class FoEventsAdapter extends RecyclerView.Adapter<FoEventsAdapter.ViewHolder> {

    //Inner Viewholder class
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_Row_ProfilePic;
        private TextView tv_Row_Name, tv_Row_Title, tv_Row_Time, tv_Row_Location, tv_Row_Type;

        public ViewHolder(View itemView, final RecyclerViewClickListener listener) {
            super(itemView);
            findViews(itemView);

            //Set on click listener for either the whole view or separate component
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onRowClicked(getAdapterPosition());
                }
            });
        }

        // Find the views of each row
        private void findViews(View itemView) {
            iv_Row_ProfilePic = (ImageView) itemView.findViewById(R.id.iv_Row_ProfilePic);
            tv_Row_Name = (TextView) itemView.findViewById(R.id.tv_Row_Name);
            tv_Row_Title = (TextView) itemView.findViewById(R.id.tv_Row_Title);
            tv_Row_Time = (TextView) itemView.findViewById(R.id.tv_Row_Time);
            tv_Row_Location = (TextView) itemView.findViewById(R.id.tv_Row_Location);
            tv_Row_Type = (TextView) itemView.findViewById(R.id.tv_Row_Type);

        }
    }

    private List<FoEvent> foEventList;
    private RecyclerViewClickListener listener;
    private SimpleDateFormat dateTimeFormatter;
    private DecimalFormat meterFormat, kmFormat;

    public FoEventsAdapter(List<FoEvent> foEventList) {
        dateTimeFormatter = new SimpleDateFormat("M/dd E h:m a");
        meterFormat = new DecimalFormat("###");
        kmFormat = new DecimalFormat("###.##");
        this.foEventList = foEventList;
    }

    public void setListener(RecyclerViewClickListener listener) {
        this.listener = listener;
    }

    public void setFoEventList(List<FoEvent> foEventList) {
        this.foEventList = foEventList;
        notifyDataSetChanged();
    }

    public FoEventsAdapter(List<FoEvent> foEventList, RecyclerViewClickListener listener) {
        dateTimeFormatter = new SimpleDateFormat("M/dd E h:mm a");
        meterFormat = new DecimalFormat("###");
        kmFormat = new DecimalFormat("###.##");
        this.foEventList = foEventList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_foevent, viewGroup, false);
        ViewHolder vh = new ViewHolder(v, listener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        FoEvent currentItem = foEventList.get(i);
        setComponents(viewHolder, currentItem);

        // Search the user who posted the query
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.USER_ID, currentItem.getPoster_id());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    setImages(objects.get(0), viewHolder);
                }
            }
        });

    }

    private void setComponents(ViewHolder viewHolder, FoEvent currentItem) {
        if (currentItem.getPoster_FirstName() == null) {
            viewHolder.tv_Row_Name.setText(currentItem.getPoster_id());
        } else {
            viewHolder.tv_Row_Name.setText(currentItem.getPoster_FirstName() + " " + currentItem.getPoster_LastName());
        }
        viewHolder.tv_Row_Title.setText(currentItem.getTitle());
        viewHolder.tv_Row_Time.setText(
                dateTimeFormatter.format(currentItem.getStartTime()) + " ~ "
                        + dateTimeFormatter.format(currentItem.getEndTime()));
        viewHolder.tv_Row_Location.setText(currentItem.getLocation() + " " +
                getDistanceToLocation(currentItem));

        int color = Fragment_Map_Tab.getAppropriateColor(currentItem.getType());
        viewHolder.tv_Row_Title.setTextColor(color);
        viewHolder.tv_Row_Type.setTextColor(color);
        viewHolder.tv_Row_Type.setText(MainActivity.getEventTypeNames()[currentItem.getType()]);
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
                        viewHolder.iv_Row_ProfilePic.setImageBitmap(bmp);
                    } else {
                        viewHolder.iv_Row_ProfilePic.setImageResource(R.drawable.logo);
                    }
                }
            });
        }
    }

    // Get distance to selected location from current location
    private String getDistanceToLocation(FoEvent currentItem) {
        String distance;
        ParseGeoPoint targetLocation = new ParseGeoPoint(currentItem.getLat(), currentItem.getLng());
        ParseGeoPoint currentLocation = Fragment_Map_Tab.getMyGeoPoint();
        if (targetLocation == null || currentLocation == null) {
            distance = " (? km)";
        } else {

            double dist = currentLocation.distanceInKilometersTo(targetLocation);
            if (dist >= 1.0) {
                distance = " (" + kmFormat.format(dist) + "km)";
            } else {
                distance = " (" + meterFormat.format(dist * 1000.0) + "m)";
            }
        }
        return distance;
    }

    @Override
    public int getItemCount() {
        return foEventList.size();
    }

    public void deleteAll() {
        foEventList.clear();
        notifyDataSetChanged();
    }

    //Interface for implementing Row click
    public interface RecyclerViewClickListener {
        void onRowClicked(int position);
        // void onViewClicked(View v, int position);
    }
}
