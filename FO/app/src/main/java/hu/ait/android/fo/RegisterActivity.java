package hu.ait.android.fo;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.android.fo.data.User;

public class RegisterActivity extends AppCompatActivity {

    @Bind(R.id.et_Register_ID)
    EditText et_Register_ID;

    @Bind(R.id.et_Register_Password)
    EditText et_Register_Password;

    @Bind(R.id.et_Register_PasswordAgn)
    EditText et_Register_PasswordAgn;

    @Bind(R.id.et_Register_FirstName)
    EditText et_Register_FirstName;

    @Bind(R.id.et_Register_LastName)
    EditText et_Register_LastName;

    @Bind(R.id.et_Register_Email)
    EditText et_Register_Email;

    @Bind(R.id.et_Register_Phone)
    EditText et_Register_Phone;

    @Bind(R.id.et_Register_Description)
    EditText et_Register_Description;

    @Bind(R.id.btn_Register_Register)
    Button btn_Register_Register;

    @Bind(R.id.btn_Register_Cancel)
    Button btn_Register_Cancel;

    @Bind(R.id.pb_Register_Progress)
    ProgressBar pb_Register_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

    }


    @OnClick(R.id.btn_Register_Register)
    protected void registerUser() {
        pb_Register_Progress.setVisibility(View.VISIBLE);
        if (checkValues()) {
            pb_Register_Progress.setVisibility(View.GONE);
            return;
        }

        ParseUser user = setUpUser();
        signUpUser(user);

        ParseObject newFriends = new ParseObject(User.FRIENDS);
        newFriends.put(User.USER_ID, et_Register_ID.getText().toString());
        newFriends.saveInBackground();
    }

    private void signUpUser(ParseUser user) {
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                pb_Register_Progress.setVisibility(View.GONE);

                if (e == null) {
                    Toast.makeText(RegisterActivity.this, R.string.registration_success,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.registration_fail) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @NonNull
    private ParseUser setUpUser() {
        ParseUser user = new ParseUser();

        user.setUsername(et_Register_ID.getText().toString());
        user.setPassword(et_Register_Password.getText().toString());
        user.put(User.FIRSTNAME, et_Register_FirstName.getText().toString());
        user.put(User.LASTNAME, et_Register_LastName.getText().toString());
        user.setEmail(et_Register_Email.getText().toString());
        user.put(User.PHONE, et_Register_Phone.getText().toString());
        user.put(User.DESC, et_Register_Description.getText().toString());
        return user;
    }

    // Check the input values
    private boolean checkValues() {
        if (checkID()) return true;

        if (checkPassword()) return true;

        if (checkName()) return true;
        return false;
    }

    private boolean checkName() {
        if (et_Register_FirstName.getText().toString().equals("")) {
            et_Register_FirstName.setError(getString(R.string.err_firstname));
            return true;
        }

        if (et_Register_LastName.getText().toString().equals("")) {
            et_Register_LastName.setError(getString(R.string.err_lastname));
            return true;
        }
        return false;
    }

    private boolean checkID() {
        if (et_Register_ID.getText().toString().equals("")) {
            et_Register_ID.setError(getString(R.string.err_type_id));
            return true;
        }

        int len = et_Register_ID.getText().toString().length();
        if(len < 6 || len > 15){
            et_Register_ID.setError(getString(R.string.err_id_limit));
            return true;
        }
        return false;
    }

    private boolean checkPassword() {
        if (et_Register_Password.getText().toString().equals("")) {
            et_Register_Password.setError(getString(R.string.err_pwd));
            return true;
        }
        if (et_Register_PasswordAgn.getText().toString().equals("")) {
            et_Register_PasswordAgn.setError(getString(R.string.err_pwd_again));
            return true;
        }

        if (!et_Register_Password.getText().toString().equals(et_Register_PasswordAgn.getText().toString())) {
            et_Register_PasswordAgn.setText("");
            et_Register_PasswordAgn.setError(getString(R.string.err_pwd_mismatch));
            return true;
        }
        return false;
    }


    @OnClick(R.id.btn_Register_Cancel)
    protected void cancel() {
        finish();
    }

}
