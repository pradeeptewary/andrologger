package com.andrologger;

import android.os.FileObserver;
import android.util.Log;

public class SDFileObserver extends FileObserver {

    public SDFileObserver(String path) {
        super(path);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onEvent(int event, String path) {
        // TODO Auto-generated method stub
        Log.i("FileObserver", "FileObserver");
        Log.e("FileObserver", "FileObserver");
    }

}
