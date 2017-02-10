package com.andrologger;
/**
 * PropertyManager.java
 *
 * @author Pradeep Tiwari
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** This class handles the requests for AndroLogger properties. **/
public class PropertyManager {
    public static final String TAG = "PropertyManager";

    /**
     * This method opens the asset file that contains the AndroLogger properties.
     *
     * @param context The application context.
     * @return The list of properties.
     */
    public static Properties openProperties(Context context) {
        Resources resources = context.getResources();
        AssetManager assetManager = resources.getAssets();
        // Read from the /assets directory
        try {
            InputStream inputStream = assetManager.open("andrologger.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } catch (IOException e) {
            Log.e(TAG, "Failed to open AndroLogger properties file");
            return null;
        }
    }
}
