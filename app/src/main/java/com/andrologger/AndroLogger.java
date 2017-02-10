package com.andrologger;
/**
 * AndroLogger.java
 *
 * @author Pradeep Tiwari
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/** This class displays an activity when the app is selected from the launch menu. **/
public class AndroLogger extends Activity implements OnClickListener {
    Button btn, btnSend;
    public static final String TAG = "AndroLoggerActivity";

    /**
     * This method creates an activity.
     *
     * @param savedInstanceState
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_andrologger);
        //New Code for API level > 9
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        /*FileObserver observer = new FileObserver("/storage/sdcard0/DCIM/Camera/") {
			public void onEvent(int event, String file) {
				Log.i("ASH", "path VSLLED");
				Toast.makeText(AndroLogger.this, "FileObserver", Toast.LENGTH_LONG).show();
			}
		};
		observer.startWatching();*/

    }

    /**
     * This method pops up the user consent banner.
     */
    @Override
    public void onStart() {
        super.onStart();
        //Pop up consent dialog
        Intent alertIntent = new Intent();
        alertIntent.setClass(this, ConsentBanner.class);
        alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alertIntent);
        btn = (Button) findViewById(R.id.button);
        btnSend = (Button) findViewById(R.id.buttonSend);
        btn.setOnClickListener(this);
        btnSend.setOnClickListener(this);
		/*btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clicker(v);
			}
		});*/
    }
/*	public void clicker(View v) {
		Intent dbmanager = new Intent(this, AndroidDatabaseManager.class);
		startActivity(dbmanager);
	}*/

    /**
     * This method displays the options menu.
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_andrologger, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == btn) {
            Intent dbmanager = new Intent(this, AndroidDatabaseManager.class);
            startActivity(dbmanager);
        }
        if (view == btnSend) {
            Intent sendServer = new Intent(this, QuestionGeneration.class);
            startActivity(sendServer);
        }
    }
}
