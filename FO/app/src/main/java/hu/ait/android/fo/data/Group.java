package hu.ait.android.fo.data;

import java.util.ArrayList;

/**
 * Created by user on 2015-12-12.
 * Data structure that represents a group of members
 */
public class Group {

    private String creatorID;
    private String groupName;
    private ArrayList<String> members;

    public Group(String creatorID, String groupName) {
        this.creatorID = creatorID;
        this.groupName = groupName;
        members = new ArrayList<String>();
    }

    public void addMember(String newID){
        if(!members.contains(newID))
            members.add(newID);
    }

    public void deleteMember(String userID){
        if(members.contains(userID)) {
            members.remove(userID);
        }
    }




}
