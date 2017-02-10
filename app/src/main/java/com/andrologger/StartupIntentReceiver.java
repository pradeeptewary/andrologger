package com.andrologger;
/**
 * StartupIntentReceiver.java
 *
 * @author Pradeep Tiwari
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** This class handles the automatic app startups during boot. **/
public class StartupIntentReceiver extends BroadcastReceiver {
    /**
     * This method handles the broadcasted intent.
     *
     * @param context The application context.
     * @param intent The broadcasted intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent bannerIntent = new Intent(context, ConsentBanner.class);
            bannerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(bannerIntent);
        }
    }
}

    

   


