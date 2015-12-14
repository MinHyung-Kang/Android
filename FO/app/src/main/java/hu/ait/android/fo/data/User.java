package hu.ait.android.fo.data;

/**
 * Created by user on 2015-12-12.
 */
public class User {

    public static final String EMAIL = "email";
    public static final String DESC = "Description";
    public static final String PHONE = "Phone_Number";
    public static final String FIRSTNAME = "FirstName";
    public static final String LASTNAME = "LastName";
    public static final String FRIENDS = "Friends";
    public static final String USER_ID = "username";
    public static final String RECEIVER_ID = "receiver_username";
    public static final String REQUESTS = "Requests";
    public static final String PIC = "Profile_Pic";
    public static final String PIC_NAME = "profilepic.png";
    public static final String FRIEND_LIST = "Friend_List";
    public static final String GROUPS = "Groups";

    private String firstName;
    private String lastName;
    private String userID;
    private String phoneNumber;
    private String description;
    private String email;
    private int isFriend;

    public User() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(int isFriend) {
        this.isFriend = isFriend;
    }
}
