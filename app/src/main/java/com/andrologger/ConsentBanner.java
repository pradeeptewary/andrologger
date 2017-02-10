package com.andrologger;
/**
 * ConsentBanner.java
 *
 * @author Pradeep Tiwari
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/** This class handles the displayed consent banner. **/
public class ConsentBanner extends Activity {
    // Initialize constants and variables
    public static final String TAG = "ConsentBannerActivity";

    /**
     * This method handles the creation of the activity.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog dialogBox = makeAndShowDialogBox();
        dialogBox.show();
    }

    /**
     * This method pops up the user consent banner and starts/stops the watcher service.
     *
     * @return The selected option.
     */
    private AlertDialog makeAndShowDialogBox() {
        // Get text values for user consent banner
        String consentBannerText = getString(R.string.user_consent_banner_text);
        String consentBannerTitle = getString(R.string.user_consent_banner_title);
        // Pop up consent dialog
        AlertDialog myDialogBox =
                new AlertDialog.Builder(this)
                        .setTitle(consentBannerTitle)
                        .setMessage(consentBannerText)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // User accepted the terms
                                Log.v(TAG, "User Consent Banner - Accepted");
                                // Close GUI
                                finish();
                                //Start the service
                                if (!isServiceRunning()) {
                                    Intent serviceIntent = new Intent("com.andrologger.WatcherService");
                                    startService(serviceIntent);
                                }
                            }
                        })
                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                                if (isServiceRunning()) {
                                    Intent serviceStopIntent = new Intent("com.andrologger.WatcherService");
                                    stopService(serviceStopIntent);
                                }
                            }
                        })
                        .create();
        return myDialogBox;
    }

    /**
     * This method checks to see if the watcher service is running.
     * @return
     */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.andrologger.WatcherService".equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
}
