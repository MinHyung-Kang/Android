package hu.ait.android.minesweeper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import hu.ait.android.minesweeper.view.MSView;

/*
    Main activity that allows play of the MineSweeper Game
 */

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutContent;        // Layout of the game

    private TextView numMine;                  // Textview that keeps count of number of remaining mines
    private Chronometer timer;                 // Timer that keeps track of how much time user has spent
    private MSView msView;                     // viewer program for minesweeper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
        defineRestartButton();
        showSnackBarMessage(getString(R.string.msg_flagInstruction));
    }

    private void initializeComponents() {
        layoutContent = (LinearLayout) findViewById(R.id.layoutContent);
        numMine = (TextView) findViewById(R.id.numMine);
        msView = (MSView) findViewById(R.id.MSView);
        timer = (Chronometer) findViewById(R.id.timer);

        retrieveDimensions();
    }

    // If there was a passed in values, use those values to reconstruct the game
    private void retrieveDimensions() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int width = bundle.getInt(Setting.WIDTH);
            int height = bundle.getInt(Setting.HEIGHT);
            msView.setDimension(width, height);
        }
    }

    private void defineRestartButton() {
        Button btnRestart = (Button) findViewById(R.id.buttonRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackBarMessage("Game Restarted");
                msView.resetGame();
            }
        });
    }

    public void showSnackBarMessage(String msg) {
        Snackbar.make(layoutContent, msg, Snackbar.LENGTH_LONG).show();
    }

    public void startTimer() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    public void stopTimer() {
        timer.stop();
    }

    public void resetTimer() {
        timer.setBase(SystemClock.elapsedRealtime());
    }

    // Displays the count of remaining mines
    public void setNumMine(String s) {
        if (numMine != null)
            numMine.setText(s);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            loadSettingMenu();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
        } else if (id == R.id.action_instructions) {
            showInstructionDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSettingMenu() {
        Intent settingMenu = new Intent();
        settingMenu.setClass(this, Setting.class);
        startActivity(settingMenu);
    }

    //Create a new dialog that informs user about the program
    private void showAboutDialog() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(R.string.msg_about);
        dlgAlert.setTitle(getResources().getString(R.string.action_about));
        dlgAlert.setPositiveButton(R.string.ok, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    //Create a new dialog that gives user instructions
    private void showInstructionDialog() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(R.string.msg_instructions);
        dlgAlert.setTitle(getResources().getString(R.string.action_about));
        dlgAlert.setPositiveButton(R.string.ok, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }


}
