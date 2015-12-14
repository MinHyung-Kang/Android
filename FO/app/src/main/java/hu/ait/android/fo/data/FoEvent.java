package hu.ait.android.fo.data;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by user on 2015-11-30.
 */
public class FoEvent implements Serializable {

    private double lat;
    private double lng;
    private String objectId; // ID saved in Parse
    private String title;
    private String location;
    private String description;
    private String poster_id;
    private String poster_FirstName;
    private String poster_LastName;
    private Date startTime;
    private Date endTime;
    private int type;
    private ArrayList<String> going;
    private ArrayList<String> going_ID;

    public ArrayList<String> getGoing_ID() {
        return going_ID;
    }

    public void setGoing_ID(ArrayList<String> going_ID) {
        this.going_ID = going_ID;
    }


    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }


    public String getPoster_FirstName() {
        return poster_FirstName;
    }

    public void setPoster_FirstName(String poster_FirstName) {
        this.poster_FirstName = poster_FirstName;
    }

    public String getPoster_LastName() {
        return poster_LastName;
    }

    public void setPoster_LastName(String poster_LastName) {
        this.poster_LastName = poster_LastName;
    }


    public ArrayList<String> getGoing() {
        return going;
    }

    public void setGoing(ArrayList<String> going) {
        this.going = going;
    }


    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }


    public FoEvent() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
