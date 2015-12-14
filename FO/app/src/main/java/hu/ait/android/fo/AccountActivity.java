package hu.ait.android.fo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.fo.data.User;

public class AccountActivity extends AppCompatActivity {

    public static int LOAD_IMG = 13947;
    private byte[] byteArray;

    @Bind(R.id.tv_Account_ID)
    TextView tv_Account_ID;

    @Bind(R.id.tv_Account_Name)
    TextView tv_Account_Name;

    @Bind(R.id.et_Account_Desc)
    EditText et_Account_Desc;

    @Bind(R.id.et_Account_Email)
    EditText et_Account_Email;

    @Bind(R.id.et_Account_Phone)
    EditText et_Account_Phone;

    @Bind(R.id.btn_Account_Save)
    Button btn_Account_Save;

    @Bind(R.id.btn_Account_Cancel)
    Button btn_Account_Cancel;

    @Bind(R.id.btn_Account_SelectImg)
    Button btn_Account_SelectImg;

    @Bind(R.id.btn_Account_PwdChange)
    Button btn_Account_PwdChange;

    @Bind(R.id.iv_Account_Image)
    ImageView iv_Account_Image;

    @Bind(R.id.pb_Account_Progress)
    ProgressBar pb_Account_Progress;


    private String objectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ButterKnife.bind(this);

        setInitialValues();
    }


    //Set up initial values
    private void setInitialValues() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User Null");
        }
        objectId = currentUser.getObjectId();
        try {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            final ParseUser user = query.get(objectId);
            setComponents(user);
            getImage(user);
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
    }

    private void getImage(ParseUser user) {
        ParseFile parseFile = user.getParseFile(User.PIC);
        if (parseFile != null) {
            parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        iv_Account_Image.setImageBitmap(bmp);
                    } else {
                        iv_Account_Image.setImageResource(R.drawable.garfield);
                    }
                }
            });
        }
    }

    private void setComponents(ParseUser user) {
        tv_Account_ID.setText(user.getUsername());
        tv_Account_Name.setText(user.getString(User.FIRSTNAME) + " " + user.getString(User.LASTNAME));
        et_Account_Email.setText(user.getEmail());
        et_Account_Desc.setText(user.getString(User.DESC));
        et_Account_Phone.setText(user.getString(User.PHONE));
    }


    //Save the changes
    @OnClick(R.id.btn_Account_Save)
    protected void saveAccount() {
        try {
            pb_Account_Progress.setVisibility(View.VISIBLE);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            final ParseUser user = query.get(objectId);
            user.setEmail(et_Account_Email.getText().toString());
            user.put(User.PHONE, et_Account_Phone.getText().toString());
            user.put(User.DESC, et_Account_Desc.getText().toString());

            saveImageAndUserdata(user);


        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }

    }

    private void saveImageAndUserdata(final ParseUser user) {
        if (byteArray != null) {
            final ParseFile img = new ParseFile(User.PIC_NAME, byteArray);
            img.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    user.put(User.PIC, img);
                    user.saveInBackground();
                    pb_Account_Progress.setVisibility(View.GONE);
                    finish();
                }
            });
        } else {
            user.saveInBackground();
            pb_Account_Progress.setVisibility(View.GONE);
            finish();
        }
    }


    @OnClick(R.id.btn_Account_Cancel)
    protected void cancel() {
        finish();
    }

    @OnClick(R.id.btn_Account_SelectImg)
    protected void selectImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, LOAD_IMG);
    }

    //Select the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                // Set the Image in ImageView after decoding the String
                iv_Account_Image.setImageBitmap(BitmapFactory
                        .decodeFile(picturePath));

                convertImgToByteArray(picturePath);

            } else {
                Toast.makeText(this, R.string.no_img_select,
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG)
                    .show();
        }

    }

    private void convertImgToByteArray(String picturePath) {
        Bitmap bmp = BitmapFactory.decodeFile(picturePath);
        int size = 150;
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, size, size, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();
    }

    @OnClick(R.id.btn_Account_PwdChange)
    protected void changePassword(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        Log.d("LOGLOG", "email : " + currentUser.getEmail());
        ParseUser.requestPasswordResetInBackground(currentUser.getEmail(),
                new RequestPasswordResetCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(AccountActivity.this, R.string.msg_pwdChange,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountActivity.this, R.string.msg_pwdChangeFail,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
