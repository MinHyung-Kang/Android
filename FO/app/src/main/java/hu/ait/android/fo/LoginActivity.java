package hu.ait.android.fo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.et_Login_ID)
    EditText et_Login_ID;

    @Bind(R.id.et_Login_Password)
    EditText et_Login_Password;

    @Bind(R.id.btn_Login_Login)
    Button btn_Login_Login;

    @Bind(R.id.btn_Login_Register)
    Button btn_Login_Register;

    @Bind(R.id.pb_Login_Progress)
    ProgressBar pb_Login_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null){
               goMain();
        }
    }

    @OnClick(R.id.btn_Login_Login)
    protected void login(){
        pb_Login_Progress.setVisibility(View.VISIBLE);

        if (checkParams()){
            pb_Login_Progress.setVisibility(View.GONE);
            return;
        }

        ParseUser.logInInBackground(et_Login_ID.getText().toString(), et_Login_Password.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {

                pb_Login_Progress.setVisibility(View.GONE);

                if (e == null) {
                    Toast.makeText(LoginActivity.this, R.string.login_success,
                            Toast.LENGTH_SHORT).show();
                    goMain();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_fail) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkParams() {
        if(et_Login_ID.getText().toString().equals("")){
            et_Login_ID.setError(getString(R.string.type_id));
            return true;
        }

        if(et_Login_Password.getText().toString().equals("")){
            et_Login_Password.setError(getString(R.string.type_password));
            return true;
        }
        return false;
    }

    @OnClick(R.id.btn_Login_Register)
    protected void register(){
        Intent goRegister = new Intent();
        goRegister.setClass(LoginActivity.this, RegisterActivity.class);
        startActivity(goRegister);
    }

    //Go to Main Screen
    private void goMain(){
        Intent goMain = new Intent();
        goMain.setClass(LoginActivity.this, MainActivity.class);
        goMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goMain);
        finish();
    }
}
