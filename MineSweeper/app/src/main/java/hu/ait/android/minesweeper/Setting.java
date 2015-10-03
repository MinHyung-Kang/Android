package hu.ait.android.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
    Setting activity that allows you to change the dimension of the game
*/

public class Setting extends AppCompatActivity {

    public static final String WIDTH = "Width";             // Final constants for bundle keys
    public static final String HEIGHT = "Height";
    private final int MIN_WIDTH = 5, MIN_HEIGHT = 5,        // Boundaries of the dimensions
            MAX_WIDTH = 15, MAX_HEIGHT = 15;
    private int width, height;                              // Desired dimensions


    private Button btn_Apply, btn_Cancel;                   // Buttons in activities

    private EditText txt_Width, txt_Height;                 // Editable textfields for each dimension


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initializeComponents();

        btn_Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processInput();
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeComponents() {
        txt_Width = (EditText) findViewById(R.id.txt_Width);
        txt_Height = (EditText) findViewById(R.id.txt_Height);
        btn_Apply = (Button) findViewById(R.id.btn_Apply);
        btn_Cancel = (Button) findViewById(R.id.btn_Cancel);
    }

    private void processInput() {
        if (!"".equals(txt_Width.getText().toString()) && !"".equals(txt_Height.getText().toString())) {
            width = Integer.parseInt(txt_Width.getText().toString());
            height = Integer.parseInt(txt_Height.getText().toString());

            //Check if the values are valid and act accordingly
            if (!isValidWidth(width)) {
                String msg = getString(R.string.setting_tell_limit, getString(R.string.width), MIN_WIDTH, MAX_WIDTH);
                txt_Width.setError(msg);
            } else if (!isValidHeight(height)) {
                String msg = getString(R.string.setting_tell_limit, getString(R.string.height), MIN_HEIGHT, MAX_HEIGHT);
                txt_Height.setError(msg);
            } else {
                restartGame();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    R.string.msg_SpecifyVal, Toast.LENGTH_SHORT).show();
        }
    }

    // restart the game, using specified new values (save the values to intent)
    private void restartGame() {
        Intent intentNewGame = new Intent(this, MainActivity.class);
        intentNewGame.putExtra(WIDTH, width);
        intentNewGame.putExtra(HEIGHT, height);
        intentNewGame.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentNewGame);
        finish();
    }


    private boolean isValidWidth(int w) {
        return w >= MIN_WIDTH && w <= MAX_WIDTH;
    }

    private boolean isValidHeight(int h) {
        return h >= MIN_HEIGHT && h <= MAX_HEIGHT;
    }

}
